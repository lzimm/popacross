package com.qorporation.qluster.conn.sql.operation;

import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.Transaction;

public abstract class SQLSaveOperation<T extends SQLBackedDefinition> extends SQLOperation<T, Boolean, Void> {

	protected Entity<T> entity = null;
	
	public SQLSaveOperation(SQLBackedEntityManager<T> manager, Transaction transaction, Entity<T> entity) {
		super(manager, transaction);
		this.entity = entity;
	}
	
	@Override
	protected Triple<Boolean, Boolean, Void> op() {		
		SQLConnection conn = this.manager.acquireConnection();
		
		boolean success = false;
		
		try {
			success = runQuery(conn);
			if (success) {
				entity.mark();
			}
		} finally {
			conn.release();
		}
		
		return new Triple<Boolean, Boolean, Void>(success, success, null);
	}
	
	public abstract boolean runQuery(SQLConnection conn);

}
