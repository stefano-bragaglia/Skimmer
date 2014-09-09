/**
 * 
 */
package bragaglia.skimmer.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SecureClassLoader;
import java.util.Map;

import org.drools.core.util.ClassUtils;

/**
 * @author stefano
 *
 */
public class MemoryClassLoader extends SecureClassLoader {
	
	private Map<String, MemoryJavaClassObject> objects;

	

	public MemoryClassLoader() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MemoryClassLoader(ClassLoader parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		MemoryJavaClassObject object = objects.get(name);
		if (null != object) {
			byte[] b = object.getBytes();
			defineClass(name, b, 0, b.length);
		}
		Class<?> result = findLoadedClass(name);
		return result;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		String className = ClassUtils.convertResourceToClassName(name);
		if (objects.containsKey(className)) {
			return new ByteArrayInputStream(objects.get(className).getBytes());
		}
		return super.getResourceAsStream(name);
	}

}
