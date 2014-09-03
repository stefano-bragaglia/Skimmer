/**
 * 
 */
package bragaglia.skimmer.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * @author stefano
 *
 */
public class MemoryCompiler {

	private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	public static String toClassName(String name) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.toClassName(String): " + name);
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public static String toFieldName(String name) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.toFieldName(String): " + name);
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	private Map<String, Class<?>> classes;

	private Map<String, Map<String, Method>> methods;

	private List<String> options;

	public MemoryCompiler() {
		this.classes = new HashMap<>();
		this.methods = new HashMap<>();
		this.options = new ArrayList<String>(Arrays.asList("-classpath", System.getProperty("java.class.path")));
		assert invariant() : "Illegal state in MemoryCompiler()";
	}

	public Class<?> compile(String name, Map<String, Object> fields) {
		if (null == name || (name = toClassName(name.trim())).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.compile(String, Map<String, Object>): " + name);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in MemoryCompiler.compile(String, Map<String, Object>): " + fields);
		Class<?> result = classes.get(name);
		if (null == result) {
			String content = "import lombok.Data;\n";
			for (Object type : fields.values()) {
				String path = type.getClass().getName();
				if (path.toLowerCase().startsWith("listof"))
					path = ArrayList.class.getName();
				if (path.contains(".") && !content.contains(path))
					content += "import " + path + ";\n";
			}
			content += "public @Data class " + name + " {\n";
			for (String field : fields.keySet()) {
				// TODO arrays?
				String type = fields.get(field).getClass().getSimpleName();
				if (type.toLowerCase().startsWith("listof"))
					type = ArrayList.class.getSimpleName();
				content += "    private " + type + " " + field + ";\n";
			}
			content += "}\n";

			JavaFileManager manager = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
			List<JavaFileObject> files = new ArrayList<JavaFileObject>();
			files.add(new MemoryJavaFileObject(name, content));
			compiler.getTask(null, manager, null, options, null, files).call();
			try {
				result = manager.getClassLoader(null).loadClass(name);
				classes.put(name, result);
			} catch (ClassNotFoundException e) {
				result = null;
			}
		}
		assert invariant() : "Illegal state in MemoryCompiler.compile(String, Map<String, Object>)";
		return result;
	}

	public boolean contains(String name) {
		if (null == name || (name = toClassName(name.trim())).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.contains(String): " + name);
		boolean result = classes.containsKey(name);
		assert invariant() : "Illegal state in MemoryCompiler.contains(String)";
		return result;
	}

	public Object create(String name, Map<String, Object> fields) {
		if (null == name || (name = toClassName(name.trim())).isEmpty() || !classes.containsKey(name))
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.create(String, Map<String, Object>): " + name);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in MemoryCompiler.create(String, Map<String, Object>): " + fields);
		Object result;
		Class<?> theClass = classes.get(name);
		try {
			result = theClass.newInstance();
			Map<String, Method> setters = methods.get(name);
			if (null == setters) {
				setters = new HashMap<>();
				for (String field : fields.keySet())
					try {
						Method method = theClass.getMethod("set" + toClassName(field), fields.get(field).getClass());
						setters.put(field, method);
					} catch (NoSuchMethodException | SecurityException e) {
						// Couldn't find such a method (shouldn't happen)
					}
			}
			for (String field : fields.keySet()) {
				Method setter = setters.get(field);
				if (null != setter) {
					try {
						setter.invoke(result, fields.get(field));
					} catch (IllegalArgumentException | InvocationTargetException e) {
						// Couldn't update the instance (shouldn't happen)
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			result = null;
		}
		assert invariant() : "Illegal state in MemoryCompiler.create(String, Map<String, Object>)";
		return result;
	}

	public void discard(String name) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.discard(String): " + name);
		classes.remove(name);
		methods.remove(name);
		assert invariant() : "Illegal state in MemoryCompiler.discard(String)";
	}

	public void flush() {
		classes.clear();
		methods.clear();
		System.gc();
	}

	public Class<?> get(String name) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.get(String): " + name);
		Class<?> result = classes.get(name);
		assert invariant() : "Illegal state in MemoryCompiler.get(String)";
		return result;
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != classes && null != methods && null != options);
	}

	public void setOptions(Collection<? extends String> options) {
		if (null == options)
			throw new IllegalArgumentException("Illegal 'options' argument in MemoryCompiler.setOptions(Collection<String>): " + options);
		this.options.clear();
		this.options.addAll(options);
		assert invariant() : "Illegal state in MemoryCompiler.setOptions(Collection<? extends String>)";
	}

}
