/**
 * 
 */
package bragaglia.skimmer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
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

	private Set<String> imports;

	private ClassLoader loader;

	private StatefulKnowledgeSession session;

	public Engine(ClassLoader loader) {
		if (null == loader)
			throw new IllegalArgumentException("Illegal 'loader' argument in Engine(ClassLoader): " + loader);
		this.imports = new HashSet<>();
		this.loader = loader;
		KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, loader);
		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase(configuration);
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(base);
		builder.add(ResourceFactory.newClassPathResource("Basic.drl"), ResourceType.DRL);
		if (builder.hasErrors()) {
			for (KnowledgeBuilderError error : builder.getErrors())
				System.out.println(error.toString());
			System.exit(-1);
		}
		this.session = base.newStatefulKnowledgeSession();
		this.session.fireAllRules();
		assert invariant() : "Illegal state in Engine(ClassLoader)";
	}

	private KnowledgeBuilder createBuilder() {
		KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(null, loader);
		KnowledgeBuilder result = KnowledgeBuilderFactory.newKnowledgeBuilder(configuration);
		assert invariant() : "Illegal state in Engine.createBuilder()";
		return result;
	}

	public void inject(Collection<String> resources) {
		if (null == resources)
			throw new IllegalArgumentException("Illegal 'resources' argument in Engine.inject(Collection<String>): " + resources);
		for (String resource : resources)
			inject(resource);
		assert invariant() : "Illegal state in Engine.inject(Collection<String>)";
	}

	public void inject(String resource) {
		if (null == resource)
			throw new IllegalArgumentException("Illegal 'resources' argument in Engine.inject(String): " + resource);
		KnowledgeBuilder builder = createBuilder();
		builder.add(ResourceFactory.newByteArrayResource(String.join("\n", imports).getBytes()), ResourceType.DRL);
		// TODO remove package?
		builder.add(ResourceFactory.newByteArrayResource(resource.getBytes()), ResourceType.DRL);
		if (builder.hasErrors()) {
			for (KnowledgeBuilderError error : builder.getErrors())
				System.out.println(error.toString());
			System.err.println("The XMLSource engine has been stopped...");
			System.exit(-1);
		}
		KnowledgeBase base = session.getKnowledgeBase();
		try {
			base.removeRule("bragaglia.skimmer.core", "Ready");
		} catch (IllegalArgumentException e) {
		}
		base.addKnowledgePackages(builder.getKnowledgePackages());
		session.fireAllRules();
		assert invariant() : "Illegal state in Engine.inject(String)";
	}

	public void insert(Object object) {
		if (null == object)
			throw new IllegalArgumentException("Illegal 'object' argument in Engine.insert(Object): " + object);
		String type = object.getClass().getName();
		if (type.toLowerCase().startsWith("listof"))
			type = ArrayList.class.getName();
		if (type.contains("."))
			imports.add("import " + type);
		this.session.insert(object);
		assert invariant() : "Illegal state in Engine.insert(Object)";
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (null != imports && null != loader && null != session);
	}

}
