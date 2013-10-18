package com.shvid.memcast.disruptor;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {

	private static final int ITER = 500000000;

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

	public static void main(String[] args) {

		try {

			System.out.println("One Thread long counter from 0 to " + ITER
					+ " example");
			barrier = new CyclicBarrier(1);
			
			long n1 = SIMPLE.call();
			System.out.println("Simple loop " + n1);

			long vn1 = VOLATILE.call();

			System.out.println("Volatile loop " + vn1 + " slower in "
					+ (vn1 / n1) + "x");

			long an1 = ATOMIC_LONG.call();

			System.out.println("AtomicLong loop " + an1 + " slower in "
					+ (an1 / n1) + "x");

			long rn1 = REENTRANT_LOCK.call();

			System.out.println("ReentrantLock loop " + rn1 + " slower in "
					+ (rn1 / n1) + "x");

			long sn1 = SYNCHRONIZED.call();

			System.out.println("Synchronized loop " + sn1 + " slower in "
					+ (sn1 / n1) + "x");

						
			System.out.println("Two Threads long counter from 0 to " + ITER
					+ " example");
			barrier = new CyclicBarrier(2);
			ExecutorService service = Executors.newFixedThreadPool(2);
			
			
			Future<Long> f1 = service.submit(SIMPLE);
			Future<Long> f2 = service.submit(SIMPLE);
			f1.get();
			n1 = f2.get();
			System.out.println("Simple loop = " + n1);

			f1 = service.submit(VOLATILE);
			f2 = service.submit(VOLATILE);
			f1.get();
			vn1 = f2.get();
			System.out.println("Volatile loop " + vn1 + " slower in "
					+ (vn1 / n1) + "x");
			
			
			f1 = service.submit(ATOMIC_LONG);
			f2 = service.submit(ATOMIC_LONG);
			f1.get();
			an1 = f2.get();

			System.out.println("AtomicLong loop " + an1 + " slower in "
					+ (an1 / n1) + "x");

			
			f1 = service.submit(REENTRANT_LOCK);
			f2 = service.submit(REENTRANT_LOCK);
			f1.get();
			rn1 = f2.get();

			System.out.println("ReentrantLock loop " + rn1 + " slower in "
					+ (rn1 / n1) + "x");

			
			f1 = service.submit(SYNCHRONIZED);
			f2 = service.submit(SYNCHRONIZED);
			f1.get();
			sn1 = f2.get();

			System.out.println("Synchronized loop " + sn1 + " slower in "
					+ (sn1 / n1) + "x");
			
			service.shutdown();

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
