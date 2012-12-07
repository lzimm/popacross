package com.qorporation.qluster.conn.hbase.operation;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Put;

import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.conn.hbase.HBaseBackedEntityManager;
import com.qorporation.qluster.conn.hbase.HBaseConnection;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class HBaseSaveOperation<T extends HBaseBackedDefinition> extends HBaseOperation<T, Boolean, Void> {

	private Entity<T> entity = null;
	
	public HBaseSaveOperation(HBaseBackedEntityManager<T> manager, Transaction transaction, Entity<T> entity) {
		super(manager, transaction);
		this.entity = entity;
	}

	@Override
	protected Triple<Boolean, Boolean, Void> op() {
		HBaseConnection conn = this.manager.acquireConnection();
		
		boolean success = false;

		try {
			Put put = new Put(Serialization.serialize(this.entity.getKey()));
			for (KeyValue kv: this.manager.getStreamer().stream(entity)) {
				put.add(kv);
			}
			
			conn.put(this.manager.getTable(), put);
			
			this.entity.mark();
			
			success = true;
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {
			conn.release();
		}
		
		return new Triple<Boolean, Boolean, Void>(success, success, null);
	}
	
}
