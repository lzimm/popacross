package com.qorporation.popacross.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class SearchToken extends SQLTable {

	public static FieldKey<Entity<Item>> item;
	
	public static FieldKey<String> token;
	public static FieldKey<GeoPoint> geopoint;
	public static FieldKey<Timestamp> age;
	public static FieldKey<Integer> score;
	
	@Indexed(fields={"item", "token"}, unique=true)
	public static IndexMetaKey uniqueToken;
	
	@Indexed(fields={"token", "age", "geopoint", "score"})
	public static IndexMetaKey tokenAgeGeoScore;
	
}