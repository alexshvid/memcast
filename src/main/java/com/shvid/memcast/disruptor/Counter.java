package com.shvid.memcast.disruptor;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.twitter.jsr166e.LongAdder;

/**
 * Shared Memory disk creation on MacOs
 * 
 * diskutil erasevolume HFS+ 'SHM_DISK' `hdiutil attach -nomount ram://1024`
 * 
 * You will get disk like this /dev/disk4, create a file as minimum 8 bytes
 * 
 * echo "12345678" > /Volumes/SHM_DISK/test-file
 * 
 * @author ashvid
 *
 */

public class Counter {
	
	private static String SHM_FILE = "/Volumes/SHM_DISK/test-file";
	private static String SSD_FILE = "./data/ssd-test-file";
	
	private static final long ITER = 500000000;
	//private static final long ITER = 1000000;

	static CyclicBarrier barrier = new CyclicBarrier(1);

	static long counter = 0;

	static final Callable<Long> SIMPLE = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				counter++;
			}
			return System.currentTimeMillis() - n0;
		}

	};

	
	static class UnsafeLong {
		
		private static long valueOffset;
		
		volatile long value;
		
		void increment() {
			long current = UnsafeUtil.UNSAFE.getLong(this, valueOffset);
			UnsafeUtil.UNSAFE.putLong(this, valueOffset, current + 1);
		}
		
		static {
			try {
		        valueOffset = UnsafeUtil.UNSAFE.objectFieldOffset(UnsafeLong.class.getDeclaredField("value"));
			}
			catch(Exception e) {
				throw new Error(e);
			}
		}
	}
	
	static final UnsafeLong unsafeLong = new UnsafeLong();
	
	static final Callable<Long> SIMPLE_HEAP_UNSAFE = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				unsafeLong.increment();
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	static MappedFile mappedSHMFile = null;
	
	static final Callable<Long> SIMPLE_SHARED_MEM = new Callable<Long>() {

		public Long call() {
			await();
			if (mappedSHMFile == null) {
				return 0L;
			}
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				long current = UnsafeUtil.UNSAFE.getLong(mappedSHMFile.getAddr());
				UnsafeUtil.UNSAFE.putLong(mappedSHMFile.getAddr(), current + 1);
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	static MappedFile mappedSSDFile = null;
	
	static final Callable<Long> SIMPLE_SSD_MEM = new Callable<Long>() {

		public Long call() {
			await();
			if (mappedSSDFile == null) {
				return 0L;
			}
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				long current = UnsafeUtil.UNSAFE.getLong(mappedSSDFile.getAddr());
				UnsafeUtil.UNSAFE.putLong(mappedSSDFile.getAddr(), current + 1);
			}
			return System.currentTimeMillis() - n0;
		}

	};	

	
	static final long offHeapAddress;
	
	static {
		try {
			offHeapAddress = UnsafeUtil.UNSAFE.allocateMemory(8);
			
			if (new File(SHM_FILE).exists()) {
				mappedSHMFile = new MappedFile(SHM_FILE, 8);
			}
			else {
				System.out.println("Warning: Shared memory file not found " + SHM_FILE);
			}
			
			if (new File(SSD_FILE).exists()) {
				mappedSSDFile = new MappedFile(SSD_FILE, 8);
			}
			else {
				System.out.println("Warning: SSD file not found " + SSD_FILE);
			}
			
		}
		catch(Exception e) {
			throw new Error(e);
		}
	}
	
	static final Callable<Long> SIMPLE_OFFHEAP_UNSAFE = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				long current = UnsafeUtil.UNSAFE.getLong(offHeapAddress);
				UnsafeUtil.UNSAFE.putLong(offHeapAddress, current + 1);
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	
	static final LongBuffer heapBuffer = ByteBuffer.allocate(8).asLongBuffer();
	
	static final Callable<Long> SIMPLE_HEAP_DIRECT = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				long current = heapBuffer.get(0);
				heapBuffer.put(0, current + 1);
			}
			return System.currentTimeMillis() - n0;
		}

	};		
	
	static final LongBuffer directBuffer = ByteBuffer.allocateDirect(8).asLongBuffer();
	
	
	static final Callable<Long> SIMPLE_OFFHEAP_DIRECT = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				long current = directBuffer.get(0);
				directBuffer.put(0, current + 1);
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	
	static class UnsafeCASLong {
		
		private static long valueOffset;
		
		long value;
		
		void increment() {
			while (true) {
				long current = UnsafeUtil.UNSAFE.getLong(this, valueOffset);
				long next = current + 1;
				if (UnsafeUtil.UNSAFE.compareAndSwapLong(this, valueOffset, current, next)) {
					return;
				}
			}
		}
		
		static {
			try {
		        valueOffset = UnsafeUtil.UNSAFE.objectFieldOffset(UnsafeCASLong.class.getDeclaredField("value"));
			}
			catch(Exception e) {
				throw new Error(e);
			}
		}
	}
	
	static final UnsafeCASLong unsafeCASLong = new UnsafeCASLong();
	
	static final Callable<Long> UNSAFE_CAS = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				unsafeCASLong.increment();
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	
	static volatile long vcounter = 0;

	static final Callable<Long> VOLATILE = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				vcounter++;
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	
	static final Object mutex = new Object();

	static final Callable<Long> SYNCHRONIZED = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				synchronized (mutex) {
					counter++;
				}
			}
			return System.currentTimeMillis() - n0;
		}

	};	

	
	static final ReentrantLock lock = new ReentrantLock();

	static final Callable<Long> REENTRANT_LOCK = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				lock.lock();
				try {
					counter++;
				} finally {
					lock.unlock();
				}
			}
			return System.currentTimeMillis() - n0;
		}

	};	

	
	static final AtomicLong atomicCounter = new AtomicLong(0);

	static final Callable<Long> ATOMIC_LONG = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				atomicCounter.incrementAndGet();
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	
	static final LongAdder adder = new LongAdder();

	static final Callable<Long> LONG_ADDER = new Callable<Long>() {

		public Long call() {
			await();
			long n0 = System.currentTimeMillis();
			for (long i = 0; i != ITER; ++i) {
				adder.increment();
			}
			return System.currentTimeMillis() - n0;
		}

	};	
	
	public static void main(String[] args) {

		try {

			System.out.println("One Thread long counter from 0 to " + ITER
					+ " example");
			barrier = new CyclicBarrier(1);
			
			long n1 = SIMPLE.call();
			System.out.println("Simple loop " + n1);

			long shn1 = SIMPLE_HEAP_UNSAFE.call();

			System.out.println("Simple.HEAP.Unsafe loop " + shn1 + " slower in "
					+ (shn1 / n1) + "x");
			
			long ohn1 = SIMPLE_OFFHEAP_UNSAFE.call();

			System.out.println("Simple.OFFHEAP.Unsafe loop " + ohn1 + " slower in "
					+ (ohn1 / n1) + "x");
			
			
			long smn1 = SIMPLE_SHARED_MEM.call();

			System.out.println("Simple.SHARED_MEM loop " + smn1 + " slower in "
					+ (smn1 / n1) + "x");
			
			long ssdn1 = SIMPLE_SSD_MEM.call();

			System.out.println("Simple.SSD_MEM loop " + ssdn1 + " slower in "
					+ (ssdn1 / n1) + "x");
			
			long shdn1 = SIMPLE_HEAP_DIRECT.call();

			System.out.println("Simple.HEAP.Direct loop " + shdn1 + " slower in "
					+ (shdn1 / n1) + "x");
			
			
			long sohdn1 = SIMPLE_OFFHEAP_DIRECT.call();

			System.out.println("Simple.OFFHEAP.Direct loop " + sohdn1 + " slower in "
					+ (sohdn1 / n1) + "x");			

			
			long vn1 = VOLATILE.call();

			System.out.println("Volatile loop " + vn1 + " slower in "
					+ (vn1 / n1) + "x - concurrent");

			
			long un1 = UNSAFE_CAS.call();

			System.out.println("HEAP.Unsafe.CAS loop " + un1 + " slower in "
					+ (un1 / n1) + "x - concurrent");
			
			long an1 = ATOMIC_LONG.call();

			System.out.println("AtomicLong loop " + an1 + " slower in "
					+ (an1 / n1) + "x - concurrent");

			long lan1 = LONG_ADDER.call();

			System.out.println("LongAdder loop " + lan1 + " slower in "
					+ (lan1 / n1) + "x - concurrent");
			
			long rn1 = REENTRANT_LOCK.call();

			System.out.println("ReentrantLock loop " + rn1 + " slower in "
					+ (rn1 / n1) + "x - concurrent");

			long sn1 = SYNCHRONIZED.call();

			System.out.println("Synchronized loop " + sn1 + " slower in "
					+ (sn1 / n1) + "x - concurrent");

						
			System.out.println("Two Threads long counter from 0 to " + ITER
					+ " example");
			barrier = new CyclicBarrier(2);
			ExecutorService service = Executors.newFixedThreadPool(2);
			
			
			Future<Long> f1 = service.submit(SIMPLE);
			Future<Long> f2 = service.submit(SIMPLE);
			f1.get();
			n1 = f2.get();
			System.out.println("Simple loop = " + n1);

			f1 = service.submit(SIMPLE_HEAP_UNSAFE);
			f2 = service.submit(SIMPLE_HEAP_UNSAFE);
			f1.get();
			shn1 = f2.get();
			System.out.println("Simple.HEAP.Unsafe loop " + shn1 + " slower in "
					+ (shn1 / n1) + "x");
			
			f1 = service.submit(SIMPLE_OFFHEAP_UNSAFE);
			f2 = service.submit(SIMPLE_OFFHEAP_UNSAFE);
			f1.get();
			ohn1 = f2.get();
			System.out.println("Simple.OFFHEAP.Unsafe loop " + ohn1 + " slower in "
					+ (ohn1 / n1) + "x");

			
			f1 = service.submit(SIMPLE_SHARED_MEM);
			f2 = service.submit(SIMPLE_SHARED_MEM);
			f1.get();
			smn1 = f2.get();
			System.out.println("Simple.SHARED_MEM loop " + smn1 + " slower in "
					+ (smn1 / n1) + "x");
			
			
			f1 = service.submit(SIMPLE_SSD_MEM);
			f2 = service.submit(SIMPLE_SSD_MEM);
			f1.get();
			ssdn1 = f2.get();
			System.out.println("Simple.SSD_MEM loop " + ssdn1 + " slower in "
					+ (ssdn1 / n1) + "x");
			
			
			f1 = service.submit(SIMPLE_HEAP_DIRECT);
			f2 = service.submit(SIMPLE_HEAP_DIRECT);
			f1.get();
			shdn1 = f2.get();
			System.out.println("Simple.HEAP.Direct loop " + shdn1 + " slower in "
					+ (shdn1 / n1) + "x");
			
			
			f1 = service.submit(SIMPLE_OFFHEAP_DIRECT);
			f2 = service.submit(SIMPLE_OFFHEAP_DIRECT);
			f1.get();
			sohdn1 = f2.get();
			System.out.println("Simple.OFFHEAP.Direct loop " + sohdn1 + " slower in "
					+ (sohdn1 / n1) + "x");
			
			
			f1 = service.submit(VOLATILE);
			f2 = service.submit(VOLATILE);
			f1.get();
			vn1 = f2.get();
			System.out.println("Volatile loop " + vn1 + " slower in "
					+ (vn1 / n1) + "x - concurrent");
			
			
			f1 = service.submit(UNSAFE_CAS);
			f2 = service.submit(UNSAFE_CAS);
			f1.get();
			un1 = f2.get();
			System.out.println("HEAP.Unsafe.CAS loop " + un1 + " slower in "
					+ (un1 / n1) + "x - concurrent");
			
			
			f1 = service.submit(ATOMIC_LONG);
			f2 = service.submit(ATOMIC_LONG);
			f1.get();
			an1 = f2.get();

			System.out.println("AtomicLong loop " + an1 + " slower in "
					+ (an1 / n1) + "x - concurrent");

			f1 = service.submit(LONG_ADDER);
			f2 = service.submit(LONG_ADDER);
			f1.get();
			lan1 = f2.get();

			System.out.println("LongAdder loop " + lan1 + " slower in "
					+ (lan1 / n1) + "x - concurrent");
			
			f1 = service.submit(REENTRANT_LOCK);
			f2 = service.submit(REENTRANT_LOCK);
			f1.get();
			rn1 = f2.get();

			System.out.println("ReentrantLock loop " + rn1 + " slower in "
					+ (rn1 / n1) + "x - concurrent");

			
			f1 = service.submit(SYNCHRONIZED);
			f2 = service.submit(SYNCHRONIZED);
			f1.get();
			sn1 = f2.get();

			System.out.println("Synchronized loop " + sn1 + " slower in "
					+ (sn1 / n1) + "x - concurrent");
			
			service.shutdown();
			
			mappedSHMFile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void await() {
		try {
			barrier.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
