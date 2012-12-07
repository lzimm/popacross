package com.qorporation.qluster.conn.hbase.typesafety;

import com.qorporation.qluster.conn.hbase.typesafety.keytypes.ColumnIndexType;

public interface HBaseOpenColumn<K, C extends ColumnIndexType<K, ?>, V> extends HBaseBackedDefinition {

}
