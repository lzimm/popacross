package com.qorporation.qluster.conn.sql.operation.predicate;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.typesafety.Key;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.util.EscapeUtil;
import com.qorporation.qluster.util.Serialization;

public abstract class SQLPredicate<T extends SQLBackedDefinition> {

	public abstract String stream(SQLBackedEntityManager<T> manager);
	
	protected <K> String stream(SQLBackedEntityManager<T> manager, Key<K> key, K value) {
		String streamed = null;
		
		if (PrimaryKey.class.isAssignableFrom(key.getClass())) {
			streamed = manager.getStreamer().getPrimaryStreamer()
						.write(value, Serialization.serialize(key.getName()), System.currentTimeMillis());
		} else {
			streamed = manager.getStreamer().getStreamer(key.getName())
						.write(value, Serialization.serialize(key.getName()), System.currentTimeMillis());
		}
		
		return EscapeUtil.escape(streamed, EscapeUtil.BACKSLASH, EscapeUtil.MYSQL_CHARS);
	}
	
	public SQLPredicate<T> and(SQLPredicate<T> predicate) {
		return new SQLAndPredicate<T>(this, predicate);
	}

	@SuppressWarnings("rawtypes")
	public SQLPredicate<T> andAll(SQLPredicate... predicates) {
		return new SQLAndPredicate<T>(this, new SQLAndPredicate<T>(predicates));
	}
	
	@SuppressWarnings("rawtypes")
	public SQLPredicate<T> andAny(SQLPredicate... predicates) {
		return new SQLAndPredicate<T>(this, new SQLOrPredicate<T>(predicates));
	}
	
	public SQLPredicate<T> or(SQLPredicate<T> predicate) {
		return new SQLOrPredicate<T>(this, predicate);
	}
	
	@SuppressWarnings("rawtypes")
	public SQLPredicate<T> orAll(SQLPredicate... predicates) {
		return new SQLOrPredicate<T>(this, new SQLAndPredicate<T>(predicates));
	}
	
	@SuppressWarnings("rawtypes")
	public SQLPredicate<T> orAny(SQLPredicate... predicates) {
		return new SQLOrPredicate<T>(this, new SQLOrPredicate<T>(predicates));
	}
	
	public <K> SQLPredicate<T> and(Key<K> key, K value) {
		return new SQLAndPredicate<T>(this, new SQLFieldPredicate<T, K>(key, value));
	}
	
	public <K> SQLPredicate<T> or(Key<K> key, K value) {
		return new SQLOrPredicate<T>(this, new SQLFieldPredicate<T, K>(key, value));
	}

	public <K> SQLPredicate<T> order(Key<K> key, SQLOrderPredicate.Order order) {
		return new SQLOrderPredicate<T, K>(this, key, order);
	}
	
	public SQLPredicate<T> limit(int limit) {
		return new SQLLimitPredicate<T>(this, limit);
	}

}
