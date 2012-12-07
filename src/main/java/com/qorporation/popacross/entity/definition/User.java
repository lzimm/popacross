package com.qorporation.popacross.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.annotation.AllowNull;
import com.qorporation.qluster.entity.annotation.Prefetch;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class User extends SQLTable {
	
	@AllowNull
	@Indexed(unique=true)
	public static FieldKey<String> token;
	
	public static FieldKey<String> name;
	public static FieldKey<String> avatar;
	public static FieldKey<String> status;
	
	@AllowNull
	@Indexed(unique=true)
	public static FieldKey<String> email;
	public static FieldKey<String> pendingEmail;
	
	public static FieldKey<String> password;
	public static FieldKey<Boolean> isAdmin;
	
	@AllowNull
	@Prefetch
	@Indexed(unique=true)
	public static FieldKey<Entity<Identity>> facebookIdentity;

	@AllowNull
	@Prefetch
	@Indexed(unique=true)
	public static FieldKey<Entity<Identity>> twitterIdentity;
	
}
