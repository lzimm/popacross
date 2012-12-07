package com.qorporation.qluster.conn.hbase.streamer;

public interface HBasePrimaryKeyStreamer<T> {
	
	public String generateKey();
	
}
