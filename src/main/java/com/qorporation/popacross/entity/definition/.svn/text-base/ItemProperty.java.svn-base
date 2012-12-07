package com.qorporation.popacross.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class ItemProperty extends SQLTable {

	@Indexed
	public static FieldKey<Entity<Item>> item;
	
	@Indexed
	public static FieldKey<String> namespace;
	
	public static FieldKey<String> property;
	public static FieldKey<String> value;
	
	@Indexed(fields={"item", "namespace", "property"}, unique=true)
	public static IndexMetaKey uniqueNamespaceProperty;
	
}

