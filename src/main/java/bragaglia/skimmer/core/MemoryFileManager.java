/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

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

	private Map<String, MemoryJavaClassObject> objects;

	private SecureClassLoader loader = new SecureClassLoader() {

		private Map<String, Class<?>> classes = new HashMap<>();

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			Class<?> result = classes.get(name);
			if (null == result) {
				MemoryJavaClassObject object = objects.get(name);
				if (null == object)
					throw new ClassNotFoundException("Class '" + name + "' not found in this ClassLoader.");
				byte[] b = object.getBytes();
				classes.put(name, super.defineClass(name, b, 0, b.length));
				objects.remove(name);
			}
			return result;
		}
	};

	public MemoryFileManager(StandardJavaFileManager manager) {
		super(manager);
		this.objects = new HashMap<>();
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return loader;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws IOException {
		MemoryJavaClassObject object = new MemoryJavaClassObject(name, kind);
		objects.put(name, object);
		return object;
	}

}
