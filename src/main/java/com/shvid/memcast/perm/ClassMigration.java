package com.shvid.memcast.perm;

import javassist.ClassPool;
import javassist.CtClass;

public class ClassMigration {

	private static final String PRIMITIVE_CLASS = "com.shvid.memcast.perm.PrimitiveClass";

	public static Class<?> migrate(int num) throws Exception {
		
		ClassPool pool = new ClassPool(true);
		
		CtClass ctClass = pool.get(PRIMITIVE_CLASS);
		
		//String newName = PRIMITIVE_CLASS + "$" + Double.toString(Math.random()).substring(2);
		
		ctClass.setName(PRIMITIVE_CLASS + "$" + num);
		
		return ctClass.toClass();
		
	}
	
}
