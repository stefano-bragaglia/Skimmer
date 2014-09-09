/**
 * 
 */
package bragaglia.skimmer.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * @author stefano
 *
 */
public class Lombok {

	public static void main(String[] args) throws Exception {
		String name = "Person";
		String content = //
		"import lombok.Data;\n" + //
				"public @Data class " + name + " {\n" + //
				"    private String name;\n" + //
				"}\n";
		System.out.println(content);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager manager = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		List<String> options = new ArrayList<String>(Arrays.asList("-classpath", System.getProperty("java.class.path")));
		List<JavaFileObject> files = new ArrayList<JavaFileObject>();
		files.add(new MemoryJavaFileObject(name, content));
		compiler.getTask(null, manager, null, options, null, files).call();

		ClassLoader theClassLoader = manager.getClassLoader(null);
		Class<?> theClass = theClassLoader.loadClass(name);
		Method theMethod = theClass.getMethod("setName", String.class);
		Object theInstance = theClass.newInstance();
		theMethod.invoke(theInstance, "foobar");
		System.out.println(theInstance);
	}

}
