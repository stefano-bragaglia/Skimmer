/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import org.drools.core.util.ClassUtils;

/**
 * @author stefano
 *
 */
public class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	private Map<String, MemoryJavaClassObject> objects;

	private SecureClassLoader loader = new SecureClassLoader() {

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			// name = ClassUtils.convertResourceToClassName(name);
			MemoryJavaClassObject object = objects.get(name);
			if (null != object) {
				byte[] bytes = object.getBytes();
				return defineClass(name, bytes, 0, bytes.length);
			}
			return findLoadedClass(name);
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			name = ClassUtils.convertResourceToClassName(name);
			MemoryJavaClassObject object = objects.get(name);
			if (null != object)
				return new ByteArrayInputStream(object.getBytes());
			else
				return super.getResourceAsStream(name);
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
