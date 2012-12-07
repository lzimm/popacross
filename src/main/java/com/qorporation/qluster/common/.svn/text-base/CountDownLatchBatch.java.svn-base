package com.qorporation.qluster.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.qorporation.qluster.util.ErrorControl;

public class CountDownLatchBatch extends CountDownLatch {

	private List<CountDownLatch> latches = null;

	public CountDownLatchBatch() {
		super(0);
		this.latches = new LinkedList<CountDownLatch>();
	}
	
	public void add(CountDownLatch latch) { this.latches.add(latch); }
	public void add(CountDownLatch ... latches) { this.add(Arrays.asList(latches)); }
	public void add(List<CountDownLatch> latches) {
		for (CountDownLatch latch: latches) {
			this.add(latch);
		}
	}
	
	@Override
	public void await() {
		for (CountDownLatch latch: this.latches) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				ErrorControl.logException(e);
			}
		}
	}
	
	@Override
	public boolean await(long timeout, TimeUnit unit) {
		boolean res = true;
		
		for (CountDownLatch latch: this.latches) {
			try {
				if (!latch.await(timeout, unit)) {
					res = false;
				}
			} catch (InterruptedException e) {
				ErrorControl.logException(e);
			}
		}
		
		return res;
	}
	
	@Override
	public void countDown() {
		for (CountDownLatch latch: this.latches) {
			latch.countDown();
		}
	}
	
	@Override
	public long getCount() {
		long ret = 0;
		
		for (CountDownLatch latch: this.latches) {
			ret += latch.getCount();
		}
		
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		for (CountDownLatch latch: this.latches) {
			ret.append(latch.toString());
		}
		
		return ret.toString();
	}

}
