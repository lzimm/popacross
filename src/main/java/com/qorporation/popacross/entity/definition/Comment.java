package com.qorporation.popacross.entity.definition;

import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Column;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.annotation.AllowNull;
import com.qorporation.qluster.entity.annotation.Prefetch;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class Comment extends SQLTable {

	@Indexed
	public static FieldKey<String> key;
	
	public static FieldKey<Float> rank;
	public static FieldKey<Timestamp> created;
	public static FieldKey<Boolean> deleted;
	
	@Column(length=0)
	public static FieldKey<String> comment;
	
	@AllowNull
	public static FieldKey<String> facebookPost;
	
	@AllowNull
	public static FieldKey<String> twitterPost;
	
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
	
	@Indexed(fields={"key", "rank"}, unique=false)
	public static IndexMetaKey keyRank;

}
