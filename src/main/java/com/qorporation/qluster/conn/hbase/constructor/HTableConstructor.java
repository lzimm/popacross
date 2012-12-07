package com.qorporation.qluster.conn.hbase.constructor;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

import com.qorporation.qluster.common.functional.F1;
import com.qorporation.qluster.util.ErrorControl;

public class HTableConstructor implements F1<String, HTable> {

	private HBaseConfiguration config = null;
	
	public HTableConstructor(HBaseConfiguration config) {
		this.config = config;
	}
	
	@Override
	public HTable f(String a) {
		try {
			return new HTable(this.config, a);
		} catch (Exception e) {
			ErrorControl.logException(e);
			return null;
		}
	}

}
