package com.shvid.memcast.unsafe;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public final class DirectMemoryAccess {

	private static final Unsafe unsafe;
	
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final static Unsafe getUnsafe() {
		return unsafe;
	}

}
