package com.qorporation.qluster.entity.operation.interfaces;

import java.util.Collection;

import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.Manager;

public interface PrefetchableOperation<T extends Definition<?>> {
	
	public Manager<T> getManager();
	public Collection<? extends Entity<T>> getEntities();
	
}
