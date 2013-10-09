package com.shvid.memcast.unsafe;

import java.lang.reflect.Field;

import sun.misc.Unsafe;


public class OffHeapFileSharing implements Runnable {

    private static final Unsafe unsafe;
    static
    {
        try
        {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
	public final static int NUM_THREADS = 4; // change
	public final static long ITERATIONS = 500L * 1000L * 1000L;
	private final int arrayIndex;

	private static long address;
	
	static {
		
		final long requiredHeap = VolatileLongOffHeap.size * NUM_THREADS;
		address = unsafe.allocateMemory(requiredHeap);
		
		System.out.println("address = " + Long.toHexString(address));
	}

	public OffHeapFileSharing(final int arrayIndex) {
		this.arrayIndex = arrayIndex;
	}

	public static void main(final String[] args) throws Exception {
		final long start = System.nanoTime();
		runTest();
		System.out.println("duration = " + (System.nanoTime() - start));
		
		System.out.println("pages = " + unsafe.pageSize());
		
	}

	private static void runTest() throws InterruptedException {
		Thread[] threads = new Thread[NUM_THREADS];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new OffHeapFileSharing(i));
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}
	}

	public void run() {
		long i = ITERATIONS + 1;
		while (0 != --i) {
			
			long objectOffset = address + (arrayIndex * VolatileLongOffHeap.size);			
			VolatileLongOffHeap.setValue(this, objectOffset, i);
		}
	}


	public final static class VolatileLong {
		public volatile long value = 0L;
		public long p1, p2, p3, p4, p5, p6; // comment out
		public long s1, s2, s3, s4, s5, s6, s7, s8; // comment out
		
		
		
		
	}
	
	public final static class VolatileLongOffHeap {
		
		public static final long valueOffset = 0L;
		public static final long size = 128L;
		//public static final long size = 65536 * 8L;

		public final static void setValue(OffHeapFileSharing thread, long objectOffset, long value) {
			//unsafe.putLongVolatile(thread, objectOffset + valueOffset, value);
			unsafe.putLong(objectOffset + valueOffset, value);
		}
		
	}
	
	
}
