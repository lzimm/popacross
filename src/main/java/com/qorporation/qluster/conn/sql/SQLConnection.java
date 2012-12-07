package com.qorporation.qluster.conn.sql;

import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.util.ErrorControl;

public class SQLConnection extends Connection {
	private static long CONNECTION_TIMEOUT = 1000*60*60*4;
	
	private String URL = "jdbc:mysql://localhost";
	private String DB = "vurs";
	private String User = "root";
	private String Pass = "";
	
	private long lastOp = System.currentTimeMillis();

    private java.sql.Connection conn = null;

	public SQLConnection(Config config, String poolKey) {
    	super(config, poolKey);
    	
    	this.URL = config.mysqlURL;
    	this.DB = config.mysqlDB;
    	this.User = config.mysqlUser;
    	this.Pass = config.mysqlPass;
    	
		setupTransport();
	}

	private void setupTransport() {        
        try {
        	StringBuilder mysql = new StringBuilder(URL)
        								.append('/')
        								.append(DB)
        								.append("?user=")
        								.append(User)
        								.append("&password=")
        								.append(Pass);

        	this.conn = DriverManager.getConnection(mysql.toString());
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
    }
	
	@Override
	protected boolean isAlive() {
		if (this.lastOp > CONNECTION_TIMEOUT) {
			try {
				this.close();
			} catch (Exception e) {
				ErrorControl.logException(e);
			} finally {
				this.setupTransport();
			}
		}
		
		return true;
	}
	
	@Override
	protected void close() {
		try {
			this.conn.close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public List<Map<String, Object>> query(String query) {
		this.logger.debug(String.format("Running query: %s", query));
		
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = this.conn.createStatement();
			rs = st.executeQuery(query);
			
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			
			while (rs.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 1; i <= cols; i++) {
					row.put(md.getColumnLabel(i), rs.getObject(i));
				}
				ret.add(row);
			}
			
			this.lastOp = System.currentTimeMillis();
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
			
			if (st != null) {
				try {
					st.close();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
		}
		
		return ret;
	}
	
	public int update(String query) {
		this.logger.debug(String.format("Running update: %s", query));
		
		int ret = 0;
		
		Statement st = null;
		
		try {
			st = this.conn.createStatement();
			ret = st.executeUpdate(query);
			this.lastOp = System.currentTimeMillis();
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {			
			if (st != null) {
				try {
					st.close();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
		}
		
		return ret;
	}
	
	public Object insert(String query) {
		this.logger.debug(String.format("Running insert: %s", query));
		
		Object ret = null;
		
		Statement st = null;
		
		try {
			st = this.conn.createStatement();
			if (st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS) > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) ret = rs.getObject(1);
				rs.close();
			}
			
			this.lastOp = System.currentTimeMillis();
		} catch (Exception e) {
			ErrorControl.logException(e);
		} finally {			
			if (st != null) {
				try {
					st.close();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
		}
		
		return ret;
	}
	
}
