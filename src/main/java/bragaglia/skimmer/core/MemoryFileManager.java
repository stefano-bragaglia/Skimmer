/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.IOException;
import java.security.SecureClassLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

/**
 * @author stefano
 *
 */
public class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	private MemoryJavaClassObject object;

	public MemoryFileManager(StandardJavaFileManager manager) {
		super(manager);
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return new SecureClassLoader() {
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				byte[] b = object.getBytes();
				return super.defineClass(name, object.getBytes(), 0, b.length);
			}
		};
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws IOException {
		object = new MemoryJavaClassObject(name, kind);
		return object;
	}
	
}
