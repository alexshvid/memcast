package com.shvid.memcast;

public class MutualExclusion {

	static class LongHolder {
		
		private volatile long value = 0;
		
		void increment() {
			value++;
		}
		
	}
	
	
	public static void main(String[] args) {
		
		LongHolder holder = new LongHolder();
		
		long time0 = System.currentTimeMillis();
		
		for (int i = 0; i != 1000000000; ++i) {
			holder.increment();
		}
		
		long time1 = System.currentTimeMillis() - time0;
		
		System.out.println("time = " + time1);
		
		
	}
	
}
