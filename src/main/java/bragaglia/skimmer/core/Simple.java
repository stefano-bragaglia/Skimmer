/**
 * 
 */
package bragaglia.skimmer.core;

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
public class Simple {

	public static void main(String[] args) throws Exception {
		String name = "Person";
		String content = //
		"public class " + name + " {\n" + //
				"    @Override\n" + //
				"    public String toString() {\n" + //
				"        return \"Hello, world!\";\n" + //
				"    }\n" + //
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
		Object theInstance = theClass.newInstance();
		System.out.println(theInstance);
	}

}
