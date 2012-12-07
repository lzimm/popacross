package com.qorporation.popacross.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class ItemTag extends SQLTable {

	@Indexed
	public static FieldKey<Entity<Item>> item;
	
	@Indexed
	public static FieldKey<String> tag;
	
	@Indexed(fields={"item", "tag"}, unique=true)
	public static IndexMetaKey uniqueTag;
	
}
