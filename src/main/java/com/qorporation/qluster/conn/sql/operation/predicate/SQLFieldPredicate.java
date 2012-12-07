package com.qorporation.qluster.conn.sql.operation.predicate;

import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.typesafety.Key;

public class SQLFieldPredicate<T extends SQLBackedDefinition, K> extends SQLPredicate<T> {

	public static enum Comparator {
		EQ(" = "), 
		LT(" < "), 
		LTE(" <= "),
		GT(" > "),
		GTE(" >= "),
		NE(" != "),
		LIKE(" LIKE "),
		ISNULL(" IS NULL ", false),
		ISNOTNULL(" IS NOT NULL ", false);
		
		Comparator(String op) {
			this.op = op;
			this.binary = true;
		}
		
		Comparator(String op, boolean binary) {
			this.op = op;
			this.binary = binary;
		}
		
		private boolean binary;
		private String op = null;
		private String getOp() { return this.op; }
		private boolean isBinary() { return this.binary; }
	}
	
	private Key<K> key = null;
	private K val = null;
	private Comparator comparator = null;
	
	public SQLFieldPredicate(Key<K> key, K val) {
		this(key, Comparator.EQ, val);
	}
	
	public SQLFieldPredicate(Key<K> key, Comparator comparator) {
		this(key, comparator, null);
	}

	public SQLFieldPredicate(Key<K> key, Comparator comparator, K val) {
		this.key = key;
		this.val = val;
		this.comparator = comparator;
	}

	@Override
	public String stream(SQLBackedEntityManager<T> manager) {
		StringBuilder streamed = new StringBuilder().append('`')
									.append(this.key.getName())
									.append('`')
									.append(this.comparator.getOp());
		
		if (this.comparator.isBinary()) {
			streamed.append('\'');
			streamed.append(stream(manager, this.key, this.val));
			streamed.append('\'');
		}
		
		return streamed.toString();
	}
	
}
