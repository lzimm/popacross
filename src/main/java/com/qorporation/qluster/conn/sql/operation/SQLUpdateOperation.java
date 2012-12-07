package com.qorporation.qluster.conn.sql.operation;

import java.util.Map;
import java.util.Map.Entry;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.transaction.Transaction;

public class SQLUpdateOperation<T extends SQLBackedDefinition> extends SQLSaveOperation<T> {
	
	public SQLUpdateOperation(SQLBackedEntityManager<T> manager, Transaction transaction, Entity<T> entity) {
		super(manager, transaction, entity);
	}

	@Override
	public boolean runQuery(SQLConnection conn) {		
		Map<String, String> streamed = this.manager.getStreamer().stream(this.entity);
		if (streamed.size() == 0) return false;
		
		StringBuilder values = new StringBuilder();
		for (Entry<String, String> e: streamed.entrySet()) {
			if (e.getKey().equals(this.manager.getPrimaryKey().getName())) continue;

			if (e.getValue() != null) {
				values.append('`')
						.append(e.getKey())
						.append("` = '")
						.append(e.getValue())
						.append("',");
			} else {
				values.append('`')
						.append(e.getKey())
						.append("` = NULL,");
			}
		}
		
		String valuePart = values.substring(0, values.length() - 1);
		
		StringBuilder query = new StringBuilder(this.manager.updateStart())
										.append(valuePart)
										.append(" WHERE `")
										.append(this.manager.getPrimaryKey().getName())
										.append("` = '")
										.append(this.entity.getKey())
										.append("'");
		
		return conn.update(query.toString()) > 0;
	}

}
