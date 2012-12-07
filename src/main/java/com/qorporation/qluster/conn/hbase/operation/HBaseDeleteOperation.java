package com.qorporation.qluster.conn.hbase.operation;

import org.apache.hadoop.hbase.client.Delete;

import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.conn.hbase.HBaseBackedEntityManager;
import com.qorporation.qluster.conn.hbase.HBaseConnection;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.Serialization;

public class HBaseDeleteOperation<T extends HBaseBackedDefinition> extends HBaseOperation<T, Boolean, Void> {

	private String key = null;
	
	public HBaseDeleteOperation(HBaseBackedEntityManager<T> manager, Transaction transaction, String key) {
		super(manager, transaction);
		this.key = key;
	}

	@Override
	protected Triple<Boolean, Boolean, Void> op() {
		HBaseConnection conn = this.manager.acquireConnection();
		
		boolean success = false;

		try {
			Delete deleteOp = new Delete(Serialization.serialize(this.key));
			conn.delete(this.manager.getTable(), deleteOp);
		} finally {
			conn.release();
		}
		
		return new Triple<Boolean, Boolean, Void>(success, success, null);
	}
	
}
