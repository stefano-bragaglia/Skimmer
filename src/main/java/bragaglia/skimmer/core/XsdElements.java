/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author stefano
 *
 */
public class XsdElements {

	public static void main(String args[]) {
		try {
			File file = new File("iFF708.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			long time;
			System.err.print("Reading XML file... ");
			time = System.nanoTime();
			DocumentBuilder xmlBuilder = factory.newDocumentBuilder();
			Document xmlDocument = xmlBuilder.parse(file);
			Element xmlRoot = xmlDocument.getDocumentElement();
			String attribute = xmlRoot.getAttribute("xmlns");
			URL xsdURL = new URL(attribute);
			time = System.nanoTime() - time;
			System.err.println((time / 1_000_000) + "ms");

			System.err.println(xsdURL.toExternalForm().toString());
			File target = new File("sbml.xsd");
			System.out.println(target.getAbsolutePath());
			if (!target.exists()) {
				InputStream inputStream = xsdURL.openStream();
				FileOutputStream outputStream = new FileOutputStream(target);
				byte[] buffer = new byte[8192];
				int read;
				while ((read = inputStream.read(buffer)) > 0)
					outputStream.write(buffer, 0, read);
				outputStream.flush();
				outputStream.close();
			}

			System.err.print("Reading XSD file... ");
			time = System.nanoTime();
			DocumentBuilder xsdBuilder = factory.newDocumentBuilder();
			Document xsdDocument = xsdBuilder.parse(xsdURL.openStream());
			Element xsdRoot = xsdDocument.getDocumentElement();
			String xsdPrefix = xsdRoot.getNodeName();
			xsdPrefix = xsdPrefix.substring(0, xsdPrefix.indexOf(":"));
			NodeList xsdList = xsdDocument.getElementsByTagName(xsdPrefix + ":element");
			time = System.nanoTime() - time;
			System.err.println((time / 1_000_000) + "ms");

			System.out.println(xsdPrefix);
			// loop to print data
			for (int i = 0; i < xsdList.getLength(); i++) {
				Element element = (Element) xsdList.item(i);
				if (element.hasAttributes())
					System.out.println("public " + element.getAttribute("type") + " " + element.getAttribute("name") + ";");
			}

			// URL website = new URL("http://www.website.com/information.asp");
			// Files.copy(website.openStream(), target,
			// StandardCopyOption.REPLACE_EXISTING);
			// // parse the document
			// File file = new File("first_html.xsd");
			// DocumentBuilderFactory factory =
			// DocumentBuilderFactory.newInstance();
			// DocumentBuilder builder = factory.newDocumentBuilder();
			// Document document = builder.parse(file);
			// NodeList list = document.getElementsByTagName("xs:element");
			// // Element root = document.getDocumentElement();
			// // root.normalize();
			//
			// // loop to print data
			// for (int i = 0; i < list.getLength(); i++) {
			// Element element = (Element) list.item(i);
			// if (element.hasAttributes()) {
			// System.out.println("public " + element.getAttribute("type") + " "
			// + element.getAttribute("name") + ";");
			// // String nm = element.getAttribute("name");
			// // System.out.println(nm);
			// // String nm1 = element.getAttribute("type");
			// // System.out.println(nm1);
			// }
			// }
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
