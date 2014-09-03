/**
 * 
 */
package bragaglia.skimmer.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.KnowledgeBase;
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

	public static void main(String[] args) {
		Engine engine = new Engine();
		engine.insert("Hello");
		engine.inject("rule \"Prova\" when then System.out.println(\"We have injected and executed a rule!\"); end");
		engine.inject("rule \"Prova\" when $s: String() then System.out.println(\"> '\" + $s + \"'\"); end");
		engine.inject("cacca!");
	}

	private Set<String> imports;

	private StatefulKnowledgeSession session;

	public Engine() {
		this.imports = new HashSet<>();
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		builder.add(ResourceFactory.newClassPathResource("Basic.drl"), ResourceType.DRL);
		if (builder.hasErrors()) {
			for (KnowledgeBuilderError error : builder.getErrors())
				System.out.println(error.toString());
			System.err.println("Unable to start the XMLSource engine...");
			System.exit(-1);
		}
		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase();
		base.addKnowledgePackages(builder.getKnowledgePackages());
		this.session = base.newStatefulKnowledgeSession();
		this.session.fireAllRules();
		assert invariant() : "Illegal state in Engine()";
	}

	public void inject(Collection<String> resources) {
		if (null == resources)
			throw new IllegalArgumentException("Illegal 'resources' argument in Engine.inject(Collection<String>): " + resources);
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		builder.add(ResourceFactory.newByteArrayResource(String.join("\n", imports).getBytes()), ResourceType.DRL);
		for (String resource : resources)
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

	public void inject(String resource) {
		if (null == resource)
			throw new IllegalArgumentException("Illegal 'resource' argument in Engine.inject(String): " + resource);
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
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
		
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		builder.add(ResourceFactory.newByteArrayResource(("import " + type + ";\n").getBytes()), ResourceType.DRL);
		KnowledgeBase base = session.getKnowledgeBase();
		base.addKnowledgePackages(builder.getKnowledgePackages());

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
		return (null != imports && null != session);
	}

}
