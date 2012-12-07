package com.qorporation.qluster.conn.sql.operation.predicate;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;

public class SQLEntityPredicate<T extends SQLBackedDefinition> extends SQLPredicate<T> {

	private Entity<T> val = null;
	
	public SQLEntityPredicate(Entity<T> val) {
		this.val = val;
	}
	
	@Override
	public String stream(SQLBackedEntityManager<T> manager) {
		StringBuilder streamed = new StringBuilder().append("`id` = \'").append(val.getKey()).append('\'');		
		return streamed.toString();
	}

}
