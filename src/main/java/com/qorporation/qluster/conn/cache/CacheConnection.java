package com.qorporation.qluster.conn.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.util.ErrorControl;

public class CacheConnection extends Connection {
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, byte[]>> STATIC_DUMMY_CACHE = new ConcurrentHashMap<String, ConcurrentHashMap<String, byte[]>>();
	
    public CacheConnection(Config config, String poolKey) {
    	super(config, poolKey);
		setupTransport();
	}

	private void setupTransport() {        
        try {
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
    }
	
	@Override
	protected void close() {
	}
	
	public byte[] get(String namespace, String key) { return this.get(namespace, Arrays.asList(key)).get(key); }
	public Map<String, byte[]> get(String namespace, Collection<String> keys) {
		Map<String, byte[]> ret = new HashMap<String, byte[]>();
		
		if (!CacheConnection.STATIC_DUMMY_CACHE.containsKey(namespace)) {
			CacheConnection.STATIC_DUMMY_CACHE.putIfAbsent(namespace, new ConcurrentHashMap<String, byte[]>());
		}
		
		ConcurrentHashMap<String, byte[]> cacheInstance = CacheConnection.STATIC_DUMMY_CACHE.get(namespace);
		for (String key: keys) {
			ret.put(key, cacheInstance.get(key));
		}
		
		return ret;
	}
	
	public boolean set(String namespace, String key, byte[] val) { Map<String, byte[]> values = new HashMap<String, byte[]>(); values.put(key, val); return this.set(namespace, values).get(key); }
	public Map<String, Boolean> set(String namespace, Map<String, byte[]> values) {
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		if (!CacheConnection.STATIC_DUMMY_CACHE.containsKey(namespace)) {
			CacheConnection.STATIC_DUMMY_CACHE.putIfAbsent(namespace, new ConcurrentHashMap<String, byte[]>());
		}
		
		ConcurrentHashMap<String, byte[]> cacheInstance = CacheConnection.STATIC_DUMMY_CACHE.get(namespace);
		for (Entry<String, byte[]> e: values.entrySet()) {
			cacheInstance.put(e.getKey(), e.getValue());
			ret.put(e.getKey(), true);
		}
		
		return ret;
	}
	
	public Boolean clear(String namespace, String key) { return this.clear(namespace, Arrays.asList(key)).get(key); }
	public Map<String, Boolean> clear(String namespace, Collection<String> keys) {
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		if (!CacheConnection.STATIC_DUMMY_CACHE.containsKey(namespace)) {
			CacheConnection.STATIC_DUMMY_CACHE.putIfAbsent(namespace, new ConcurrentHashMap<String, byte[]>());
		}
		
		ConcurrentHashMap<String, byte[]> cacheInstance = CacheConnection.STATIC_DUMMY_CACHE.get(namespace);
		for (String key: keys) {
			ret.put(key, cacheInstance.remove(key) != null);
		}
		
		return ret;
	}
	
}
