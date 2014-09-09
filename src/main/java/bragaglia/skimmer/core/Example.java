/**
 * 
 */
package bragaglia.skimmer.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * @author stefano
 *
 */
public class Example {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Setting the strings that we are going to use...
		String packageName = "bragaglia.skimmer.data";
		String className = "Person";
		String name = packageName + "." + className;
		String content = "package " + packageName + ";\n";
		content += "import lombok.Data;\n";
		content += "public class " + className + " {\n";
		content += "    private String name;\n";
		// content += "    public Person() {\n";
		// content += "    }\n";
		// content += "    public Person(String name) {\n";
		// content += "        this.name = name;\n";
		// content += "    }\n";
		// content += "    public String getName() {\n";
		// content += "        return name;\n";
		// content += "    }\n";
		// content += "    public void setName(String name) {\n";
		// content += "        this.name = name;\n";
		// content += "    }\n";
		// content += "    @Override\n";
		// content += "    public String toString() {\n";
		// content += "        return \"Hello, \" + name + \"!\";\n";
		// content += "    }\n";
		content += "}\n";
		String value = "HAL";
		String rules = "package org.drools.test;\n";
		rules += "import " + packageName + ".*;\n";
		rules += "\n";
		rules += "rule \"Alive\"\n";
		rules += "when\n";
		rules += "then\n";
		rules += "    System.out.println(\"I'm alive!\");\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Print\"\n";
		rules += "when\n";
		rules += "    $o: Object()\n";
		rules += "then\n";
		rules += "    System.out.println(\"OBJ> \" + $o.toString());\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Person1\"\n";
		rules += "when\n";
		rules += "    $p: bragaglia.skimmer.data.Person()\n";
		rules += "then\n";
		rules += "    System.out.println(\"Person> \" + $p.toString());\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Person2\"\n";
		rules += "when\n";
		rules += "    $p: Object()\n";
		rules += "then\n";
		rules += "    if ($p.getClass().getSimpleName().equals(\"Person\"))\n";
		rules += "        System.out.println(\"Person> \" + $p.toString());\n";
		rules += "end\n";

		// Compiling the given class in memory
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		List<String> options = new ArrayList<String>(Arrays.asList("-classpath", System.getProperty("java.class.path")));
		JavaFileManager manager = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		ClassLoader classLoader = manager.getClassLoader(null);
		List<JavaFileObject> files = new ArrayList<JavaFileObject>();
		files.add(new MemoryJavaFileObject(name, content));
		compiler.getTask(null, manager, null, options, null, files).call();

		try {
			// Instantiate and set the new class
			Class<?> person = classLoader.loadClass(name);
			System.out.println("We are going to work with '" + person.getName() + "'...");
			Method method = person.getMethod("setName", String.class);
			Object instance = person.newInstance();
			method.invoke(instance, value);
			System.out.println(instance);
			System.out.println("We get a salutation, so Person is now a compiled class in memory loaded by the given ClassLoader.");

			KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, classLoader);
			KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase(config);
			KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(base);
			builder.add(ResourceFactory.newByteArrayResource(rules.getBytes()), ResourceType.DRL);
			if (builder.hasErrors()) {
				for (KnowledgeBuilderError error : builder.getErrors())
					System.out.println(error.toString());
				System.exit(-1);
			}
			StatefulKnowledgeSession session = base.newStatefulKnowledgeSession();
			session.insert(instance);
			session.fireAllRules();
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found!");
		} catch (IllegalAccessException e) {
			System.out.println("Illegal access!");
		} catch (InstantiationException e) {
			System.out.println("Instantiation!");
		} catch (NoSuchMethodException e) {
			System.out.println("No such method!");
		} catch (InvocationTargetException e) {
			System.out.println("Invocation target!");
		}
		System.out.println("Done.");
	}

}
