/**
 * 
 */
package bragaglia.skimmer.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stefano
 *
 */
public class Refactor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Setting the strings that we are going to use...
		String packageName = "bragaglia.skimmer.data";
		String className = "Person";
		Map<String, Object> fields = new HashMap<>();
		fields.put("name", "HAL");
		fields.put("age", 2001);

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
		rules += "    System.out.println(\"Object> \" + $o.toString());\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Person1\"\n";
		rules += "when\n";
		rules += "    $p: Object()\n";
		rules += "then\n";
		rules += "    if ($p.getClass().getSimpleName().equals(\"Person\"))\n";
		rules += "        System.out.println(\"Object/Person> \" + $p.toString());\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Person2\"\n";
		rules += "when\n";
		rules += "    $p: Person()\n";
		rules += "then\n";
		rules += "    System.out.println(\"Person> \" + $p.toString());\n";
		rules += "end\n";
		rules += "\n";
		rules += "rule \"Person3\"\n";
		rules += "when\n";
		rules += "    $p: bragaglia.skimmer.data.Person()\n";
		rules += "then\n";
		rules += "    System.out.println(\"bragaglia.skimmer.data.Person> \" + $p.toString());\n";
		rules += "end\n";
		rules += "\n";

		MemoryCompiler compiler = new MemoryCompiler();
		Class<?> theClass = compiler.compile(packageName, className, fields);
		System.out.println("We are going to work with '" + theClass.getName() + "'...");
		Object theInstance = compiler.newInstance(packageName, className, fields);
		System.out.println(theInstance);
		System.out.println("We get a proper toString(), so Person is now a class compiled in memory, augmented by Lombok and loaded by the given ClassLoader.");

		Engine engine = new Engine(compiler);
		engine.insert(theInstance);
		engine.load(rules);

		System.out.println("Done.");
	}

}
