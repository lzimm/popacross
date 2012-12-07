package com.qorporation.qluster.conn.sql.operation.predicate;

import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.typesafety.Key;

@SuppressWarnings("unused")
public class SQLGeoPredicate<T extends SQLBackedDefinition> extends SQLPredicate<T> {

	private Key<GeoPoint> field = null;
	private GeoPoint near = null;
	private long radius = 0;
	
	public SQLGeoPredicate(Key<GeoPoint> field, GeoPoint near, long radius) {
		this.field = field;
		this.near = near;
		this.radius = radius;
	}
	
	@Override
	public String stream(SQLBackedEntityManager<T> manager) {
		return null;
	}

}
