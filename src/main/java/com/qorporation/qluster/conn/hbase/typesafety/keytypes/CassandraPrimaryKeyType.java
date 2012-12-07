package com.qorporation.qluster.conn.hbase.typesafety.keytypes;

import com.qorporation.qluster.conn.hbase.streamer.HBasePrimaryKeyStreamer;
import com.qorporation.qluster.entity.typesafety.PrimaryKeyType;

public interface CassandraPrimaryKeyType<T, P extends HBasePrimaryKeyStreamer<T>> extends PrimaryKeyType<T> {

}
