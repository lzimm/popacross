package com.qorporation.qluster.conn.hbase.operation;

import com.qorporation.qluster.conn.hbase.HBaseBackedEntityManager;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.entity.Operation;
import com.qorporation.qluster.transaction.Transaction;

public abstract class HBaseOperation<T extends HBaseBackedDefinition, R, V> extends Operation<R, V> {

	protected HBaseBackedEntityManager<T> manager = null;
	
	public HBaseOperation(HBaseBackedEntityManager<T> manager,
			Transaction transaction) {
		super(transaction);
		this.manager = manager;
	}

}
