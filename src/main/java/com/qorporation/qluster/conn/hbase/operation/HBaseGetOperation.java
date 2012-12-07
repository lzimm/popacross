package com.qorporation.qluster.conn.hbase.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;

import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.conn.hbase.HBaseBackedEntityManager;
import com.qorporation.qluster.conn.hbase.HBaseConnection;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.operation.interfaces.PrefetchableOperation;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.Serialization;

public class HBaseGetOperation<T extends HBaseBackedDefinition> extends HBaseOperation<T, Boolean, Map<String, Entity<T>>> implements PrefetchableOperation<T> {

	private List<String> keys = null;
	
	public HBaseGetOperation(HBaseBackedEntityManager<T> manager, Transaction transaction, List<String> keys) {
		super(manager, transaction);
		this.keys = keys;
	}
	
	@Override
	protected Triple<Boolean, Boolean, Map<String, Entity<T>>> op() {
		HBaseConnection conn = this.manager.acquireConnection();
		
		Map<String, Entity<T>> ret = new HashMap<String, Entity<T>>();
		
		try {
			for (String key: keys) {
				Get getOp = new Get(Serialization.serialize(key));
				Result data = conn.get(this.manager.getTable(), getOp);
				if (!data.isEmpty()) {
					ret.put(key, this.manager.wrapResult(key, data));
				}
			}
		} finally {
			conn.release();
		}
		
		return new Triple<Boolean, Boolean, Map<String, Entity<T>>>(true, true, ret);
	}

	@Override
	public Collection<? extends Entity<T>> getEntities() {
		return this.getValue().values();
	}

	@Override
	public Manager<T> getManager() {
		return this.manager;
	}

}
