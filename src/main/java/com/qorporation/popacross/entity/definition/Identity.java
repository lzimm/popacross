package com.qorporation.popacross.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class Identity extends SQLTable {

	public enum IdentityType {
		FACEBOOK,
		TWITTER;
	}
	
	public static FieldKey<IdentityType> type;
	
	public static FieldKey<String> token;
	public static FieldKey<String> secret;
	
	@Indexed
	public static FieldKey<String> user;
	public static FieldKey<String> name;
	public static FieldKey<String> url;
	public static FieldKey<String> avatar;
	
	@Indexed(fields={"type", "user"}, unique=true)
	public static IndexMetaKey uniqueTypeUser;
	
}
