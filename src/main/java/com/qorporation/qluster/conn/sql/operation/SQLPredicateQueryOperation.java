package com.qorporation.qluster.conn.sql.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.SQLNode;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLPredicate;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.operation.interfaces.PrefetchableOperation;
import com.qorporation.qluster.transaction.Transaction;

public class SQLPredicateQueryOperation<T extends SQLBackedDefinition> extends SQLOperation<T, Boolean, List<Entity<T>>> implements PrefetchableOperation<T> {

	private SQLPredicate<T> predicate = null;
	
	public SQLPredicateQueryOperation(SQLBackedEntityManager<T> manager, Transaction transaction, SQLPredicate<T> predicate) {
		super(manager, transaction);
		this.predicate = predicate;
	}
	
	@Override
	protected Triple<Boolean, Boolean, List<Entity<T>>> op() {
		SQLConnection conn = this.manager.acquireConnection();
		
		List<Entity<T>> ret = new ArrayList<Entity<T>>();

		try {
			String predicateClause = this.predicate.stream(this.manager);
			StringBuilder query = new StringBuilder(this.manager.queryStart())
						.append(predicateClause);
			
			List<Map<String, Object>> res = conn.query(query.toString());
			for (Map<String, Object> r: res) {
				String primary = this.manager.getStreamer().getPrimaryStreamer().asString(r.get(this.manager.getPrimaryKey().getName()));
				Map<String, Object> parsed = this.manager.getStreamer().parse(r);
				ret.add(this.manager.wrap(primary, new SQLNode<T>(this.manager.getDefinition(), this.manager, parsed, true)));
			}
		} finally {
			conn.release();
		}
		
		return new Triple<Boolean, Boolean, List<Entity<T>>>(true, true, ret);
	}

	@Override
	public Collection<? extends Entity<T>> getEntities() {
		return this.getValue();
	}

	@Override
	public Manager<T> getManager() {
		return this.manager;
	}

}
