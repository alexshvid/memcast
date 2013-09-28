package com.shvid.memcast.perm;

import java.io.IOException;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

public class InMemoryClassLoader extends ClassLoader {

	private static final Logger log = Logger.getLogger(InMemoryClassLoader.class);
	
	private static final String PRIMITIVE_CLASS = "com.shvid.memcast.perm.PrimitiveClass";
	private static final String PRIMITIVE_CLASS_PREFIX = PRIMITIVE_CLASS + "$";
	
	private ClassPool pool;
	private CtClass ctClass;
	
	public InMemoryClassLoader() throws NotFoundException {
	
		pool = new ClassPool(true);
	    ctClass = pool.get(PRIMITIVE_CLASS);
		
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
	
		
		if (name.startsWith(PRIMITIVE_CLASS_PREFIX)) {
			
			ctClass.defrost();
			ctClass.setName(name);
			try {
				//return ctClass.toClass(this);
				byte[] blob = ctClass.toBytecode();
				
				return defineClass(this, name, blob);
				
			} catch (Exception e) {
				throw new ClassNotFoundException(name, e);
			}

			
		}
		
		return super.loadClass(name);
	}

    
    public static Class<?> defineClass(ClassLoader classLoader, String className, byte[] blob) {
            // override classDefine (as it is protected) and define the class.
            Class<?> clazz = null;
            try {
                    java.lang.reflect.Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });

                    // protected method invocaton
                    method.setAccessible(true);
                    try {
                            Object[] args = new Object[] { className, blob, new Integer(0), new Integer(blob.length) };
                            clazz = (Class<?>) method.invoke(classLoader, args);
                    } finally {
                            method.setAccessible(false);
                    }
            } catch (Exception e) {
                    log.error("fail to define class " + className, e);
            }
            return clazz;
    }
	
}
