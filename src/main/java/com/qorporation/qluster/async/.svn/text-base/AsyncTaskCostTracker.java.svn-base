package com.qorporation.qluster.async;

import java.util.concurrent.atomic.AtomicLong;

public class AsyncTaskCostTracker {

	protected static class IncrementalCheckout {
		long originalCost = 0l;
		long currentCost = 0l;
		long reduction = 0l;
		public IncrementalCheckout(long originalCost) { 
			this.originalCost = originalCost; 
			this.currentCost = originalCost;
			this.reduction = 0l;
		}
	}
	
	private long maxCost = 0l;
	private AtomicLong currentCost = null;
	
	public AsyncTaskCostTracker(long maxCost) {
		this.maxCost = maxCost;
		this.currentCost = new AtomicLong(0l);
	}
	
	public boolean checkout(IncrementalCheckout incremental) {
		if (!this.checkout(incremental.currentCost)) {
			if (incremental.reduction == 0l) {
				incremental.reduction = incremental.currentCost / 2;
			}
			
			if (this.checkout(incremental.reduction)) {
				incremental.currentCost -= incremental.reduction;
				incremental.reduction = 0l;
			} else {
				incremental.reduction /= 2;
			}
			
			return false;
		} else {
			return true;
		}
	}
	
	public boolean checkout(long cost) {
		if (this.maxCost > 0) {
			if (this.currentCost.addAndGet(cost) > this.maxCost) {
				this.currentCost.addAndGet(-cost);
				return false;
			}
		}
		
		return true;
	}
	
	public boolean checkin(IncrementalCheckout incremental) { return checkin(incremental.originalCost); }
	public boolean checkin(long cost) {
		if (this.maxCost > 0) {
			this.currentCost.addAndGet(-cost);
		}
		
		return true;
	}
	
}
