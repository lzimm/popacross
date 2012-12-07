package com.qorporation.qluster.conn.sql.typesafety.keytypes;

import com.qorporation.qluster.conn.sql.streamer.SQLPrimaryKeyStreamer;
import com.qorporation.qluster.entity.typesafety.PrimaryKeyType;

public interface SQLPrimaryKeyType<T, P extends SQLPrimaryKeyStreamer<T>> extends PrimaryKeyType<T> {

}
