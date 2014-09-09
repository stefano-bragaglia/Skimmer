/**
 * 
 */
package bragaglia.skimmer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class Engine {

	private static void check(KnowledgeBuilder builder) {
		if (builder.hasErrors()) {
			for (KnowledgeBuilderError error : builder.getErrors())
				System.out.println(error.toString());
			System.exit(-1);
		}
	}

	private KnowledgeBase base;

	private MemoryCompiler compiler;

	private Set<String> imports;

	private StatefulKnowledgeSession session;

	public Engine(MemoryCompiler compiler) {
		if (null == compiler)
			throw new IllegalArgumentException("Illegal 'compiler' argument in Engine(MemoryCompiler): " + compiler);
		this.compiler = compiler;
		this.imports = new HashSet<>();
		KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, compiler.classLoader());
		this.base = KnowledgeBaseFactory.newKnowledgeBase(config);
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(base);
		builder.add(ResourceFactory.newClassPathResource("Basic.drl"), ResourceType.DRL);
		check(builder);
		this.session = base.newStatefulKnowledgeSession();
		this.session.fireAllRules();
		assert invariant() : "Illegal state in Engine(ClassLoader)";
	}

	/**
	 * 
	 */
	public void flush() {
		compiler.flush();
		assert invariant() : "Illegal state in Engine.flush()";
	}

	public Object insert(Object object) {
		if (null == object)
			throw new IllegalArgumentException("Illegal 'object' argument in Engine.insert(Object): " + object);
		String type = object.getClass().getName();
		if (type.toLowerCase().startsWith("listof"))
			type = ArrayList.class.getName();
		if (type.contains("."))
			imports.add("import " + type);
		this.session.insert(object);
		assert invariant() : "Illegal state in Engine.insert(Object)";
		return object;
	}

	public Object insert(String packageName, String className, Map<String, Object> fields) {
		if (null == packageName)
			throw new IllegalArgumentException("Illegal 'packageName' argument in Engine.insert(String, String, Map<String, Object>): " + packageName);
		if (null == className || (className = className.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'className' argument in Engine.insert(String, String, Map<String, Object>): " + className);
		if (null == fields)
			throw new IllegalArgumentException("Illegal 'fields' argument in Engine.insert(String, String, Map<String, Object>): " + fields);
		Object result = insert(compiler.newInstance(packageName, className, fields));
		assert invariant() : "Illegal state in Engine.insert(String, String, Map<String, Object>)";
		return result;
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != base && null != compiler && null != imports && null != session);
	}

	public void load(Collection<String> resources) {
		if (null == resources)
			throw new IllegalArgumentException("Illegal 'resources' argument in Engine.load(Collection<String>): " + resources);
		try {
			base.removeRule("bragaglia.skimmer.core", "Ready");
		} catch (IllegalArgumentException e) {
		}
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(base);
		if (!imports.isEmpty())
			builder.add(ResourceFactory.newByteArrayResource(String.join("\n", imports).getBytes()), ResourceType.DRL);
		for (String resource : resources)
			builder.add(ResourceFactory.newByteArrayResource(resource.getBytes()), ResourceType.DRL);
		check(builder);
		session.fireAllRules();
		assert invariant() : "Illegal state in Engine.load(Collection<String>)";
	}

	public void load(String resource) {
		if (null == resource || (resource = resource.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'resource' argument in Engine.load(String): " + resource);
		try {
			base.removeRule("bragaglia.skimmer.core", "Ready");
		} catch (IllegalArgumentException e) {
		}
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(base);
		if (!imports.isEmpty())
			builder.add(ResourceFactory.newByteArrayResource(String.join("\n", imports).getBytes()), ResourceType.DRL);
		builder.add(ResourceFactory.newByteArrayResource(resource.getBytes()), ResourceType.DRL);
		check(builder);
		session.fireAllRules();
		assert invariant() : "Illegal state in Engine.load(String)";
	}

}
