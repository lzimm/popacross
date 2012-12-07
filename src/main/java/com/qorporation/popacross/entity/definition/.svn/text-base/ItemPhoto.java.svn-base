package com.qorporation.popacross.entity.definition;

import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class ItemPhoto extends SQLTable {
	
	public enum PhotoState {
		STARTED,
		PROCESSED,
		DEPLOYED;
		
		public boolean lessThan(PhotoState other) { return this.ordinal() < other.ordinal(); }
		public boolean isEqual(PhotoState other) { return this.ordinal() == other.ordinal(); }
	}
	
	@Indexed
	public static FieldKey<Entity<Item>> item;
	
	public static FieldKey<String> photo;
	public static FieldKey<String> photoName;
	public static FieldKey<String> photoExt;
	
	public static FieldKey<String> caption;
	
	public static FieldKey<PhotoState> photoState;
	public static FieldKey<PhotoState> bgState;
	public static FieldKey<PhotoState> thumbState;
	public static FieldKey<PhotoState> scaledState;
	
	public static FieldKey<Integer> width;
	public static FieldKey<Integer> height;
	public static FieldKey<Integer> rgb;
	public static FieldKey<String> rgbString;
	
	public static FieldKey<Boolean> deleted;
	
	@Indexed(fields={"item", "photo"}, unique=true)
	public static IndexMetaKey uniquePhoto;
	
}
