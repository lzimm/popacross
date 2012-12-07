package com.qorporation.qluster.conn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;

public abstract class Connection {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ConnectionPool<? extends Connection> pool = null;
	protected String poolKey = null;
	
	public Connection(Config config, String poolKey) {
		this.poolKey = poolKey;
	}
	
	protected void hookPool(ConnectionPool<? extends Connection> pool) {
		this.logger.info("Binding new client to pool");
		this.pool = pool;
	}
	
	public void release() {
		this.pool.release(this);
	}
	
	public String getPoolKey() { return this.poolKey; }
	
	protected boolean isAlive() { return true; }
	protected abstract void close();
	
}
