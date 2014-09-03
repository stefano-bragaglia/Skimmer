/**
 * 
 */
package bragaglia.skimmer.core;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * @author stefano
 *
 */
public class MemoryJavaFileObject extends SimpleJavaFileObject {

	private CharSequence content;

	protected MemoryJavaFileObject(String className, CharSequence content) {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.content = content;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return content;
	}

}