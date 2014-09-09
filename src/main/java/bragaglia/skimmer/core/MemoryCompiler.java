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

	public static String createLombokContent(String packageName, String className, Map<String, Object> fields) {
		if (null == packageName)
			throw new IllegalArgumentException("Illegal 'packageName' argument in MemoryCompiler.createLombokContent(String, String, Map<String, Object>): "
					+ packageName);
		packageName = packageName.trim();
		if (null == className || (className = className.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'className' argument in MemoryCompiler.createLombokContent(String, String, Map<String, Object>): "
					+ className);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in MemoryCompiler.createLombokContent(String, String, Map<String, Object>): "
					+ fields);
		String result = "";
		if (!packageName.isEmpty())
			result += "package " + packageName + ";\n";
		result += "import lombok.Data;\n";
		for (Object type : fields.values()) {
			String path = type.getClass().getName();
			if (path.toLowerCase().startsWith("listof"))
				path = ArrayList.class.getName();
			if (path.contains(".") && !path.substring(0, path.lastIndexOf(".")).equals(packageName) && !result.contains(path))
				result += "import " + path + ";\n";
		}
		result += "@Data\n";
		result += "public class " + className + " {\n";
		for (String field : fields.keySet()) {
			// TODO arrays?
			String type = fields.get(field).getClass().getSimpleName();
			if (type.toLowerCase().startsWith("listof"))
				type = ArrayList.class.getSimpleName();
			result += "    private " + type + " " + field + ";\n";
		}
		result += "}\n";
		return result;
	}

	public static String createPath(String packageName, String className) {
		if (null == packageName)
			throw new IllegalArgumentException("Illegal 'packageName' argument in MemoryCompiler.createPath(String, String): " + packageName);
		packageName = packageName.trim();
		if (null == className || (className = className.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'className' argument in MemoryCompiler.createPath(String, String): " + className);
		return packageName.isEmpty() ? className : packageName + "." + className;
	}

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

	private JavaCompiler compiler;

	private JavaFileManager manager;

	private Map<Class<?>, Map<String, Method>> methods;

	private List<String> options;

	public MemoryCompiler() {
		this.compiler = ToolProvider.getSystemJavaCompiler();
		this.manager = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		this.methods = new HashMap<>();
		this.options = new ArrayList<String>(Arrays.asList("-classpath", System.getProperty("java.class.path")));
		assert invariant() : "Illegal state in MemoryCompiler()";
	}

	public Class<?> compile(String packageName, String className, Map<String, Object> fields) {
		if (null == packageName)
			throw new IllegalArgumentException("Illegal 'packageName' argument in MemoryCompiler.compile(String, String, Map<String, Object>): " + packageName);
		packageName = packageName.trim();
		if (null == className || (className = className.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'className' argument in MemoryCompiler.compile(String, String, Map<String, Object>): " + className);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in MemoryCompiler.compile(String, String, Map<String, Object>): " + fields);
		List<JavaFileObject> files = new ArrayList<JavaFileObject>();
		String path = createPath(packageName, className);
		files.add(new MemoryJavaFileObject(path, createLombokContent(packageName, className, fields)));
		compiler.getTask(null, manager, null, options, null, files).call();
		Class<?> result;
		try {
			result = manager.getClassLoader(null).loadClass(path);
		} catch (ClassNotFoundException e) {
			result = null;
		}
		assert invariant() : "Illegal state in MemoryCompiler.compile(String, String, Map<String, Object>)";
		return result;
	}

	public Object newInstance(String packageName, String className, Map<String, Object> fields) {
		if (null == packageName)
			throw new IllegalArgumentException("Illegal 'packageName' argument in MemoryCompiler.newInstance(String, String, Map<String, Object>): "
					+ packageName);
		packageName = packageName.trim();
		if (null == className || (className = className.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'className' argument in MemoryCompiler.newInstance(String, String, Map<String, Object>): " + className);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in MemoryCompiler.newInstance(String, String, Map<String, Object>): " + fields);
		String path = createPath(packageName, className);

		Object result;
		try {
			Class<?> theClass = classLoader().loadClass(path);
			if (null == theClass)
				theClass = compile(packageName, className, fields);
			Map<String, Method> setters = methods.get(theClass);
			if (null == setters) {
				setters = new HashMap<>();
				for (String field : fields.keySet())
					try {
						// TODO changes in ArrayList may generate errors here 
						Method method = theClass.getMethod("set" + toClassName(field), fields.get(field).getClass());
						setters.put(field, method);
					} catch (NoSuchMethodException | SecurityException e) {
						// Couldn't find such a method (shouldn't happen)
					}
				methods.put(theClass, setters);
			}
			result = theClass.newInstance();
			if (null != result) {
				for (String field : fields.keySet()) {
					Method setter = setters.get(field);
					if (null != setter)
						try {
							setter.invoke(result, fields.get(field));
						} catch (IllegalArgumentException | InvocationTargetException e) {
							// Couldn't update the instance (shouldn't happen)
						}
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			result = null;
		}
		assert invariant() : "Illegal state in MemoryCompiler.newInstance(String, String, Map<String, Object>)";
		return result;
	}

	public void discard(String name) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'name' argument in MemoryCompiler.discard(String): " + name);
		methods.remove(name);
		assert invariant() : "Illegal state in MemoryCompiler.discard(String)";
	}

	public void flush() {
		methods.clear();
		System.gc();
	}

	public ClassLoader classLoader() {
		ClassLoader result = manager.getClassLoader(null);
		assert invariant() : "Illegal state in MemoryCompiler.classLoader()";
		return result;
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != compiler && null != manager && null != methods && null != options);
	}

	public void setOption(Collection<String> options) {
		if (null == options)
			throw new IllegalArgumentException("Illegal 'options' argument in MemoryCompiler.setOption(Collection<String>): " + options);
		this.options.clear();
		this.options.addAll(options);
		assert invariant() : "Illegal state in MemoryCompiler.setOption(Collection<String>)";
	}

}
