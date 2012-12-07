package com.qorporation.qluster.conn.sql.operation;

import java.util.Map;
import java.util.Map.Entry;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.SQLNode;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.Transaction;

public class SQLInsertOperation<T extends SQLBackedDefinition> extends SQLSaveOperation<T> {
	
	public SQLInsertOperation(SQLBackedEntityManager<T> manager, Transaction transaction, Entity<T> entity) {
		super(manager, transaction, entity);
	}

	@Override
	public boolean runQuery(SQLConnection conn) {
		Map<String, String> streamed = this.manager.getStreamer().stream(this.entity);
		
		StringBuilder keys = new StringBuilder();
		StringBuilder values = new StringBuilder();
		
		for (Entry<String, String> e: streamed.entrySet()) {
			if (e.getKey().equals(this.manager.getPrimaryKey().getName())) continue;

			keys.append('`').append(e.getKey()).append("`,");
			values.append('\'').append(e.getValue()).append("\',");
		}
		
		String keyPart = keys.substring(0, keys.length() - 1);
		String valuePart = values.substring(0, values.length() - 1);
		
		StringBuilder query = new StringBuilder(this.manager.insertStart())
										.append(keyPart)
										.append(") VALUES (")
										.append(valuePart)
										.append(')');
		
		Object keyValue = conn.insert(query.toString());
		boolean success = keyValue != null;
		
		if (success) {
			this.entity.setKey(keyValue);
			((SQLNode<T>) entity.getNode()).markCreated(true);
		}
		
		return success;
	}

}
