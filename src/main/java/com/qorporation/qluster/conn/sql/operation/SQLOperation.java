package com.qorporation.qluster.conn.sql.operation;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Operation;
import com.qorporation.qluster.transaction.Transaction;

public abstract class SQLOperation<T extends SQLBackedDefinition, R, V> extends Operation<R, V> {

	protected SQLBackedEntityManager<T> manager = null;
	
	public SQLOperation(SQLBackedEntityManager<T> manager,
			Transaction transaction) {
		super(transaction);
		this.manager = manager;
	}

}
