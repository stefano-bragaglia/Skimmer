/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author stefano
 *
 */
public class XMLReader {

	private static boolean isList(Node node) {
		if (null == node)
			throw new IllegalArgumentException("Illegal 'node' argument in XMLSource.isList(Node): " + node);
		boolean result = true;
		if (!node.getNodeName().toLowerCase().startsWith("listof")) {
			Set<String> names = new HashSet<>();
			NodeList list = node.getChildNodes();
			for (int i = 0; result && i < list.getLength(); i++) {
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					names.add(child.getNodeName());
					result = (1 == names.size());
				}
			}
		}
		return result;
	}

	private static Object asValue(String value) {
		if (null == value || (value = value.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'value' argument in XMLSource.asBoolean(String): " + value);
		Object result;
		try {
			result = Long.parseLong(value);
		} catch (NumberFormatException eLong) {
			try {
				result = Double.parseDouble(value);
			} catch (NumberFormatException eDouble) {
				if (value.equalsIgnoreCase("true"))
					result = true;
				else if (value.equalsIgnoreCase("false"))
					result = false;
				else
					result = value;
			}
		}
		return result;
	}

	private Engine engine;

	public XMLReader(Engine engine) {
		if (null == engine)
			throw new IllegalArgumentException("Illegal 'engine' argument in XMLReader(Engine): " + engine);
		this.engine = engine;
		assert invariant() : "Illegal state in XMLReader(Engine)";
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != engine);
	}

	public void load(String xml, Collection<String> ignores) {
		File file;
		if (null == xml || (xml = xml.trim()).isEmpty() || !(file = new File(xml)).exists() || file.isDirectory())
			throw new IllegalArgumentException("Illegal 'xml' argument in XMLReader.load(String, Collection<String>): " + xml);
		if (null == ignores)
			throw new IllegalArgumentException("Illegal 'nodes' argument in XMLReader.load(String, Collection<String>): " + ignores);
		long time = System.nanoTime();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			Element root = document.getDocumentElement();
			root.normalize();
			visit(root, ignores, engine);
			engine.flush();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e.toString());
		}
		time = System.nanoTime() - time;
		System.err.format("XML file '%s' skimmed in %.3fs\n", file.getName(), time / 1_000_000_000.0);

		assert invariant() : "Illegal state in XMLReader.load(String, Collection<String>)";
	}

	private Object visit(Node node, Collection<String> ignores, Engine engine) {
		if (null == node)
			throw new IllegalArgumentException("Illegal 'node' argument in Skimmer.visit(Node, Collection<String>, Engine): " + node);
		if (null == ignores)
			throw new IllegalArgumentException("Illegal 'ignores' argument in Skimmer.visit(Node, Collection<String>, Engine): " + ignores);
		if (null == engine)
			throw new IllegalArgumentException("Illegal 'engine' argument in Skimmer.visit(Node, Collection<String>, Engine): " + engine);
		String className = node.getNodeName();
		Map<String, Object> fields = new HashMap<>();
		NamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Node param = map.item(i);
			fields.put(param.getNodeName(), asValue(param.getNodeValue()));
		}
		boolean listed = isList(node);
		List<Object> value = new ArrayList<>();
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				String field = child.getNodeName();
				if (ignores.contains(field))
					// TODO Create a method ad hoc
					if (listed)
						value.add(child.getTextContent());
					else
						fields.put(field, child.getTextContent());
				else if (listed)
					value.add(visit(child, ignores, engine));
				else
					fields.put(field, visit(child, ignores, engine));
			}
			if (listed)
				fields.put(className, value);
		}
		Object result = engine.insert("bragaglia.skimmer.data", className, fields);
		assert invariant() : "Illegal state in Skimmer.visit(Node, Collection<String>, Engine)";
		return result;
	}
}
