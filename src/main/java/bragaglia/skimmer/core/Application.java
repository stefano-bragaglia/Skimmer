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
		Application application = new Application();
		application.parse(args);
		application.execute();
	}

	private boolean errors;
	private boolean help;
	private Set<String> ignores;
	private Set<String> rules;
	private boolean version;

	private Set<String> xmls;

	public Application() {
		this.ignores = new HashSet<>();
		this.rules = new LinkedHashSet<>();
		this.xmls = new HashSet<>();
		this.clear();
	}

	public void clear() {
		this.errors = false;
		this.ignores.clear();
		this.help = false;
		this.rules.clear();
		this.version = false;
		this.xmls.clear();
	}

	public void execute() {
		if (!errors) {
			if (help)
				printHelp();
			else if (version)
				printVersion();
			else {
				XMLSource source;
				Engine engine = new Engine(MemoryCompiler.getClassLoader());
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
	}

	public void parse(String[] args) {
		printTitle();
		Path path;
		String name;
		for (int i = 0; i < args.length; i++)
			switch (args[i]) {
				case "-h":
				case "--help":
					help = true;
					break;
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
					else {
						errors = true;
						System.err.println("The file '" + name + "' is not a valid XML source...");
					}
					break;
				case "-v":
				case "--version":
					version = true;
					break;
				default:
					path = Paths.get(args[i]);
					name = args[i];
					if (name.lastIndexOf("/") > -1)
						name = name.substring(name.lastIndexOf("/"));
					try {
						if (Files.exists(path, options) && !Files.isDirectory(path, options))
							rules.add(new String(Files.readAllBytes(path)));
						else {
							errors = true;
							System.err.println("The file '" + name + "' is not a valid RULE source...");
						}
					} catch (IOException e) {
						errors = true;
						System.err.println("The file '" + name + "' is not a valid RULE source...");
					}
			}
	}

	private void printHelp() {
		System.err.println("Usage:     java -jar skimmer.jar  [options]  [files]");
		System.err.println();
		System.err.println("Options:");
		System.err.println();
		System.err.println("  --help,-h           : Print this help and exit");
		System.err.println("  --ignore,-i <name>  : Ignore element with given <name>");
		System.err.println("  --version,-v        : Print version information and exit");
		System.err.println("  --xml,-x <file>     : Load given XML <file>");
		System.err.println();
		System.err.println("Example:   java -jar skimmer.jar  -x iFF708.xml  -i annotation  -i notes  -i kineticLaw  script.drl");
		System.err.println();
	}

	private void printTitle() {
		System.err.println("Skimmer 1.0");
		System.err.println();
	}

	private void printVersion() {
		System.err.println("Copyright (c) Stefano Bragaglia");
		System.err.println();
		System.err.println("GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>");
		System.err.println("'skimmer' is free software: you are free to change and redistribute it.");
		System.err.println("There is NO WARRANTY, to the extent permitted by law.");
		System.err.println();
	}

}
