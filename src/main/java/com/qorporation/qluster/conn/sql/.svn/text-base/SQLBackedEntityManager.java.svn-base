package com.qorporation.qluster.conn.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qorporation.qluster.conn.ConnectionPool;
import com.qorporation.qluster.conn.cache.CacheConnection;
import com.qorporation.qluster.conn.sql.operation.SQLDeleteOperation;
import com.qorporation.qluster.conn.sql.operation.SQLGetOperation;
import com.qorporation.qluster.conn.sql.operation.SQLInsertOperation;
import com.qorporation.qluster.conn.sql.operation.SQLPredicateQueryOperation;
import com.qorporation.qluster.conn.sql.operation.SQLPredicateUpdateOperation;
import com.qorporation.qluster.conn.sql.operation.SQLQueryOperation;
import com.qorporation.qluster.conn.sql.operation.SQLSaveOperation;
import com.qorporation.qluster.conn.sql.operation.SQLUpdateOperation;
import com.qorporation.qluster.conn.sql.operation.params.SQLValueList;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLPredicate;
import com.qorporation.qluster.conn.sql.streamer.SQLEntityStreamer;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;

import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.Node;
import com.qorporation.qluster.entity.Operation;
import com.qorporation.qluster.entity.ProxyEntity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.Key;
import com.qorporation.qluster.entity.typesafety.MetaKey;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.transaction.Transaction;

public class SQLBackedEntityManager<T extends SQLBackedDefinition> extends Manager<T> {
	
	protected ConnectionPool<CacheConnection> cachePool = null;
	protected ConnectionPool<SQLConnection> sqlPool = null;
	
	protected String table = null;
	protected String getStart = null;
	protected String updateStart = null;
	protected String insertStart = null;
	protected String deleteStart = null;
	protected String queryStart = null;
	
	protected SQLEntityStreamer<T> entityStreamer = null;

	@SuppressWarnings("unchecked")
	public void setup(EntityService entityService, Class<T> entityType, List<FieldKey<?>> keys, PrimaryKey<?, ?> primaryKey, List<MetaKey> metaKeys) {
		super.setup(entityService, entityType, keys, primaryKey, metaKeys);

		this.cachePool = this.entityService.getConnectionService().getPool(
				CacheConnection.class, ConnectionPool.class);
		this.sqlPool = this.entityService.getConnectionService().getPool(
				SQLConnection.class, ConnectionPool.class);
		
		this.table = entityType.getSimpleName().toLowerCase();
		
		this.getStart = new StringBuilder("SELECT * FROM `")
									.append(this.table)
									.append("` WHERE `")
									.append(this.primaryKey.getName())
									.append("` IN (").toString();
		
		this.updateStart = new StringBuilder("UPDATE `")
									.append(this.table)
									.append("` SET ").toString();
		
		this.insertStart = new StringBuilder("INSERT INTO `")
									.append(this.table)
									.append("` (").toString();
		
		this.deleteStart = new StringBuilder("DELETE FROM `")
									.append(this.table)
									.append("` WHERE `")
									.append(this.primaryKey.getName())
									.append("` = '").toString();
		
		this.queryStart = new StringBuilder("SELECT * FROM `")
									.append(this.table)
									.append("` WHERE ").toString();
		
		this.entityStreamer = new SQLEntityStreamer<T>(entityService, entityType, keys, primaryKey);
	}
	
	public SQLConnection acquireConnection() { return this.sqlPool.acquire(); }
	public String getTable() { return this.table; }
	
	public String getStart() { return this.getStart; }
	public String updateStart() { return this.updateStart; }
	public String insertStart() { return this.insertStart; }
	public String deleteStart() { return this.deleteStart; }
	public String queryStart() { return this.queryStart; }
	public SQLEntityStreamer<T> getStreamer() { return this.entityStreamer; }

	public Entity<T> wrap(String key, Node<T> node) {
		return new Entity<T>(this.entityService, this.entityType, key, node);
	}

	@Override
	public Entity<T> create(String key) {
		if (key == null) {
			key = this.entityStreamer.getPrimaryStreamer().generateKey();
		}
		
		return wrap(key, new SQLNode<T>(this.def, this, new HashMap<String, Object>(), false));
	}
	
	@Override
	public Operation<Boolean, Map<String, Entity<T>>> get(List<String> keys, Transaction transaction) {
		SQLGetOperation<T> op = new SQLGetOperation<T>(this, transaction, keys);
		return op;
	}

	@Override
	public Operation<Boolean, Void> save(Entity<T> entity, Transaction transaction) {
		SQLSaveOperation<T> op;
		
		boolean created = ((SQLNode<T>) entity.getNode()).isCreated();
		
		if (created) {
			op = new SQLUpdateOperation<T>(this, transaction, entity);
		} else {
			op = new SQLInsertOperation<T>(this, transaction, entity);
		}
		
		return op;
	}

	@Override
	public Operation<Boolean, Void> delete(String key, Transaction transaction) {
		SQLDeleteOperation<T> op = new SQLDeleteOperation<T>(this, transaction, key);
		return op;
	}

	@Override
	public Entity<T> getProxy(String key) {
		return new ProxyEntity<T>(this.entityService, this.entityType, key);
	}
	
	public <K> List<Entity<T>> query(Key<K> key, K val) { return query(key, val, this.entityService.getGlobalTransaction()).getValue(); }
	public <K> Operation<Boolean, List<Entity<T>>> query(Key<K> key, K val, Transaction transaction) {
		SQLQueryOperation<T, K> op = new SQLQueryOperation<T, K>(this, transaction, key, val);
		return op;
	}
	
	public <K> List<Entity<T>> query(SQLPredicate<T> predicate) { return query(predicate, this.entityService.getGlobalTransaction()).getValue(); }
	public <K> Operation<Boolean, List<Entity<T>>> query(SQLPredicate<T> predicate, Transaction transaction) {
		SQLPredicateQueryOperation<T> op = new SQLPredicateQueryOperation<T>(this, transaction, predicate);
		return op;
	}
	
	public <K> Integer update(SQLValueList<T> values, SQLPredicate<T> predicate) { return update(values, predicate, this.entityService.getGlobalTransaction()).getValue(); }
	public <K> Operation<Boolean, Integer> update(SQLValueList<T> values, SQLPredicate<T> predicate, Transaction transaction) {
		SQLPredicateUpdateOperation<T> op = new SQLPredicateUpdateOperation<T>(this, transaction, values, predicate);
		return op;
	}

}
