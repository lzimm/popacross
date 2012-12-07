package com.qorporation.qluster.conn.sql.operation.predicate;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.typesafety.Key;

public class SQLOrderPredicate<T extends SQLBackedDefinition, K> extends SQLPredicate<T> {

	public static enum Order {
		ASC("ASC"), 
		DESC("DESC");
		
		Order(String order) {
			this.order = order;
		}
		
		private String order = null;
		private String getOrder() { return this.order; }
	}
	
	private SQLPredicate<T> wherePredicate = null;
	private Key<K> key = null;
	private Order order = null;
	
	public SQLOrderPredicate(SQLPredicate<T> wherePredicate, Key<K> key, Order order) {
		this.wherePredicate = wherePredicate;
		this.key = key;
		this.order = order;
	}
	
	@Override
	public String stream(SQLBackedEntityManager<T> manager) {
		StringBuilder streamed = new StringBuilder(this.wherePredicate.stream(manager)).append(" ORDER BY `").append(this.key.getName()).append("` ").append(this.order.getOrder());
		return streamed.toString();
	}
	
}
