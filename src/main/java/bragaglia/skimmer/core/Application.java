/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author stefano
 *
 */
public class Application {

	private static LinkOption[] options = new LinkOption[0];

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.err.println("Skimmer 1.0");
		System.err.println("");
		System.out.println();
		System.out.println(String.format("Usage:     java -jar skimmer.jar  [options]  [files]"));
		System.out.println();
		System.out.println("Options:");
		System.out.println();
		System.out.println("  --ignore,-i <text>  : Ignore entities with given text");
		System.out.println("  --xml,-x <file>     : Load given XML <file>");
		System.out.println();
		System.out.println("Files:");
		System.out.println();
		System.out.println("  any DRL file without package definition");
		System.out.println();
	
		Path path;
		String name;
		Set<String> ignores = new HashSet<>();
		Set<String> xmls = new HashSet<>();
		Set<String> rules = new LinkedHashSet<>();
		for (int i = 0; i < args.length; i++)
			switch (args[i]) {
				case "-i":
				case "--ignore":
					ignores.add(args[++i]);
					break;
				case "-x":
				case "--xml":
					path = Paths.get(args[++i]);
					name = args[i];
					if (name.lastIndexOf("/") > -1)
						name = name.substring(name.lastIndexOf("/"));
					if (Files.exists(path, options) && !Files.isDirectory(path, options))
						xmls.add(path.toString());
					else
						System.out.println("The file '" + name + "' is not a valid XML source...");
					break;
				default:
					path = Paths.get(args[i]);
					name = args[i];
					if (name.lastIndexOf("/") > -1)
						name = name.substring(name.lastIndexOf("/"));
					try {
						if (Files.exists(path, options) && !Files.isDirectory(path, options))
							rules.add(new String(Files.readAllBytes(path)));
						else
							System.out.println("The file '" + name + "' is not a valid RULE source...");
					} catch (IOException e) {
						System.out.println("The file '" + name + "' is not a valid RULE source...");
					}
			}
		XMLSource source;
		Engine engine = new Engine();
		for (String xml : xmls) {
			source = new XMLSource(xml);
			source.ignore(ignores);
			source.execute(engine);
		}
		if (!rules.isEmpty())
			engine.inject(rules);
		System.err.println("Done.");
	}

}
