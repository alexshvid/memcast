package com.shvid.memcast.dutythread;

import java.util.Random;

public class LightThread implements Runnable {

	public LightThread() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		Long l = 0l;
		while (true) {
			l++;
			try {
				Thread.sleep(new Random().nextInt(10));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (l == Long.MAX_VALUE) {
				l = 0l;
			}
		}
	}
}