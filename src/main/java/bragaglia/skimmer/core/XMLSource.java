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
public class XMLSource {

	private static Boolean asBoolean(String value) {
		if (null == value || (value = value.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'value' argument in XMLSource.asBoolean(String): " + value);
		if (value.equalsIgnoreCase("true"))
			return true;
		if (value.equalsIgnoreCase("false"))
			return false;
		return null;
	}

	private static Double asDouble(String value) {
		if (null == value || (value = value.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'value' argument in XMLSource.asDouble(String): " + value);
		Double result;
		try {
			result = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			result = null;
		}
		return result;
	}

	private static Long asLong(String value) {
		if (null == value || (value = value.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'value' argument in XMLSource.asLong(String): " + value);
		Long result;
		try {
			result = Long.parseLong(value);
		} catch (NumberFormatException e) {
			result = null;
		}
		return result;
	}

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

	private File file;

	private Set<String> ignores;

	public XMLSource(String xml) {
		File file;
		if (null == xml || (xml = xml.trim()).isEmpty() || !(file = new File(xml)).exists() || file.isDirectory())
			throw new IllegalArgumentException("Illegal 'xml' argument in XMLSource(xml): " + xml);
		this.file = file;
		this.ignores = new HashSet<>();
		assert invariant() : "Illegal state in XMLSource(String)";
	}

	public void execute(Engine engine) {
		if (null == engine)
			throw new IllegalArgumentException("Illegal 'engine' argument in XMLSource.execute(Engine): " + engine);
		long time = System.nanoTime();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			Element root = document.getDocumentElement();
			root.normalize();
			MemoryCompiler compiler = new MemoryCompiler();
			visit(root, compiler, engine);
			compiler.flush();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println(e.toString());
		}
		time = System.nanoTime() - time;
		System.out.format("XML file '%s' skimmed in %.3fs\n", file.getName(), time / 1_000_000_000.0);
		assert invariant() : "Illegal state in XMLSource.execute(Engine)";
	}

	public void heed(Collection<? extends String> nodes) {
		if (null == nodes)
			throw new IllegalArgumentException("Illegal 'nodes' argument in Skimmer.heed(Collection<String>): " + nodes);
		ignores.removeAll(nodes);
		assert invariant() : "Illegal state in Skimmer.heed(Collection<String>)";
	}

	public void heed(String node) {
		if (null == node || (node = node.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'node' argument in Skimmer.heed(String): " + node);
		ignores.remove(node);
		assert invariant() : "Illegal state in Skimmer.heed(String)";
	}

	public void ignore(Collection<? extends String> nodes) {
		if (null == nodes)
			throw new IllegalArgumentException("Illegal 'nodes' argument in Skimmer.ignore(Collection<String>): " + nodes);
		ignores.addAll(nodes);
		assert invariant() : "Illegal state in Skimmer.ignore(Collection<String>)";
	}

	public void ignore(String node) {
		if (null == node || (node = node.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'node' argument in Skimmer.ignore(String): " + node);
		ignores.add(node);
		assert invariant() : "Illegal state in Skimmer.ignore(String)";
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != file && file.exists() && !file.isDirectory() && null != ignores);
	}

	private Object visit(Node node, MemoryCompiler compiler, Engine engine) {
		if (null == node)
			throw new IllegalArgumentException("Illegal 'node' argument in Skimmer.visit(Node, MemoryCompiler, Engine): " + node);
		if (null == compiler)
			throw new IllegalArgumentException("Illegal 'compiler' argument in Skimmer.visit(Node, MemoryCompiler, Engine): " + compiler);
		if (null == engine)
			throw new IllegalArgumentException("Illegal 'engine' argument in Skimmer.visit(Node, MemoryCompiler, Engine): " + engine);
		String name = node.getNodeName();
		Map<String, Object> values = new HashMap<>();
		NamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Node param = map.item(i);
			String content = param.getNodeValue();
			Object value = asLong(content);
			if (null == value)
				value = asDouble(content);
			if (null == value)
				value = asBoolean(content);
			if (null == value)
				value = content;
			values.put(param.getNodeName(), value);
		}
		if (isList(node)) {
			List<Object> value = new ArrayList<>();
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String field = child.getNodeName();
					if (ignores.contains(field))
						// TODO Create a method ad hoc
						value.add(child.getTextContent());
					else
						value.add(visit(child, compiler, engine));
				}
			}
			values.put(name, value);
		} else {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String field = child.getNodeName();
					if (ignores.contains(field))
						// TODO Create a method ad hoc
						values.put(field, child.getTextContent());
					else
						values.put(field, visit(child, compiler, engine));
				}
			}
		}
		if (null == compiler.get(name))
			compiler.compile(name, values);
		Object result = compiler.create(name, values);
		engine.insert(result);
		assert invariant() : "Illegal state in Skimmer.visit(Node, MemoryCompiler, Engine)";
		return result;
	}

}
