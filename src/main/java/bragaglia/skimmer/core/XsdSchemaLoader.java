/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * @author stefano
 *
 */
public class XsdSchemaLoader {

	/**
	 * @param name
	 * @return
	 */
	public static Schema loadSchema(String name) {
		Schema schema = null;
		try {
			// getting the default implementation of XML Schema factory
			String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
			SchemaFactory factory = SchemaFactory.newInstance(language);
			System.out.println();
			System.out.println("Schema Language: " + language);
			System.out.println("Factory Class: " + factory.getClass().getName());

			// parsing the schema file
			schema = factory.newSchema(new File(name));
			System.out.println();
			System.out.println("Schema File: " + name);
			System.out.println("Schema Class: " + schema.getClass().getName());

		} catch (Exception e) {
			// catching all exceptions
			System.out.println();
			System.out.println(e.toString());
		}
		return schema;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println("java XsdSchemaLoader schema_file_name");
		} else {
			String name = args[0];
			Schema schema = loadSchema(name);
		}
	}
}
