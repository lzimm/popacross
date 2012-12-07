package com.qorporation.qluster.conn.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.qorporation.qluster.common.ConstructingHashMap;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.conn.hbase.constructor.HTableConstructor;
import com.qorporation.qluster.util.ErrorControl;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

public class HBaseConnection extends Connection {
	
	private HBaseConfiguration config = null;
	private ConstructingHashMap<String, HTable> tables = null;
	
	public HBaseConnection(Config config, String poolKey) {
    	super(config, poolKey);
    	
		this.config = config.hbaseConfiguration;
		this.tables = new ConstructingHashMap<String, HTable>(new HTableConstructor(this.config));
	}
	
	public Result get(String table, Get get) {
		try {
			return this.tables.get(table).get(get);
		} catch (IOException e) {
			ErrorControl.logException(e);
			return null;
		}
	}
	
	public void put(String table, Put put) {
		try {
			this.tables.get(table).put(put);
		} catch (IOException e) {
			ErrorControl.logException(e);
		}
	}
	
	public void put(String table, List<Put> puts) {
		try {
			this.tables.get(table).put(puts);
		} catch (IOException e) {
			ErrorControl.logException(e);
		}
	}
	
	public void delete(String table, Delete delete) {
		try {
			this.tables.get(table).delete(delete);
		} catch (IOException e) {
			ErrorControl.logException(e);
		}
	}
	
	public List<Result> scan(String table, Scan scan) {
		List<Result> results = new ArrayList<Result>();
		
		ResultScanner scanner = null;
		
		try {
			scanner = this.tables.get(table).getScanner(scan);
			Iterator<Result> itr = scanner.iterator();
			
			while (itr.hasNext()) {
				results.add(itr.next());
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		return results;
	}
	
	@Override
	protected void close() {
	}

}
