package com.qorporation.qluster.conn.hbase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;

import com.qorporation.qluster.conn.ConnectionPool;
import com.qorporation.qluster.conn.cache.CacheConnection;
import com.qorporation.qluster.conn.hbase.operation.HBaseDeleteOperation;
import com.qorporation.qluster.conn.hbase.operation.HBaseGetOperation;
import com.qorporation.qluster.conn.hbase.operation.HBaseSaveOperation;
import com.qorporation.qluster.conn.hbase.streamer.HBaseEntityStreamer;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.Node;
import com.qorporation.qluster.entity.Operation;
import com.qorporation.qluster.entity.ProxyEntity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.MetaKey;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.transaction.Transaction;

public class HBaseBackedEntityManager<T extends HBaseBackedDefinition> extends Manager<T> {

	protected ConnectionPool<CacheConnection> cachePool = null;
	protected ConnectionPool<HBaseConnection> hbasePool = null;
	
	protected String tableName = null;
	
	protected HBaseEntityStreamer<T> entityStreamer = null;

	@SuppressWarnings("unchecked")
	public void setup(EntityService entityService, Class<T> entityType, List<FieldKey<?>> keys, PrimaryKey<?, ?> primaryKey, List<MetaKey> metaKeys) {
		super.setup(entityService, entityType, keys, primaryKey, metaKeys);

		this.cachePool = this.entityService.getConnectionService().getPool(
				CacheConnection.class, ConnectionPool.class);
		this.hbasePool = this.entityService.getConnectionService().getPool(
				HBaseConnection.class, ConnectionPool.class);
		
		this.tableName = entityType.getSimpleName();
	}
	
	public HBaseConnection acquireConnection() { return this.hbasePool.acquire(); }
	public String getTable() { return this.tableName; }
	public HBaseEntityStreamer<T> getStreamer() { return this.entityStreamer; }
	
	public Entity<T> wrapResult(String key, Result result) {
		Map<String, Object> parsed = this.entityStreamer.parse(result);
		return wrap(key, new Node<T>(this.def, this, parsed));
	}
	
	@Override
	public Entity<T> wrap(String key, Node<T> node) {
		return new Entity<T>(this.entityService, this.entityType, key, node);
	}
	
	@Override
	public Entity<T> getProxy(String key) {
		return new ProxyEntity<T>(this.entityService, this.entityType, key);
	}

	@Override
	public Entity<T> create(String key) {
		if (key == null) {
			key = this.entityStreamer.getPrimaryStreamer().generateKey();
		}
		
		return wrap(key, new Node<T>(this.def, this, new HashMap<String, Object>()));
	}

	@Override
	public Operation<Boolean, Map<String, Entity<T>>> get(List<String> keys, Transaction transaction) {
		HBaseGetOperation<T> op = new HBaseGetOperation<T>(this, transaction, keys);
		return op;
	}

	@Override
	public Operation<Boolean, Void> save(Entity<T> entity, Transaction transaction) {
		HBaseSaveOperation<T> op = new HBaseSaveOperation<T>(this, transaction, entity);
		return op;
	}

	@Override
	public Operation<Boolean, Void> delete(String key, Transaction transaction) {
		HBaseDeleteOperation<T> op = new HBaseDeleteOperation<T>(this, transaction, key);
		return op;
	}

}
