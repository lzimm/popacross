package com.qorporation.popacross.entity.definition;

import java.sql.Timestamp;

import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Column;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.annotation.AllowNull;
import com.qorporation.qluster.entity.annotation.Prefetch;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class Item extends SQLTable {
	public static final String SHORT_DOMAIN_FORMAT = "http://pops.io/%s";
	public static final String DIRECT_LINK_FORMAT = "/v/%s";
	
	@Indexed(unique=true)
	@Column(caseSensitive=true)
	public static FieldKey<String> token;
	
	@Indexed
	public static FieldKey<String> type;
	
	public static FieldKey<String> label;
	public static FieldKey<String> price;
	public static FieldKey<String> location;
	public static FieldKey<GeoPoint> position;
	
	@Column(length=0)
	public static FieldKey<String> description;
	
	public static FieldKey<Timestamp> startTime;
	public static FieldKey<Timestamp> endTime;
	
	public static FieldKey<Timestamp> created;
	
	@Indexed
	public static FieldKey<Entity<User>> user;
	
	@AllowNull
	@Prefetch
	@Indexed
	public static FieldKey<Entity<Identity>> facebookIdentity;

	@AllowNull
	@Prefetch
	@Indexed
	public static FieldKey<Entity<Identity>> twitterIdentity;
	
}
