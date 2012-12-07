package com.qorporation.popacross.search.cluster;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.search.SearchService;
import com.qorporation.qluster.cluster.ClusterNode;
import com.qorporation.qluster.cluster.ClusterProtocol;
import com.qorporation.qluster.cluster.ClusterService;
import com.qorporation.qluster.cluster.socket.SocketConnection;
import com.qorporation.qluster.common.BatchFutureResponse;
import com.qorporation.qluster.common.FutureResponse;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.common.ImmediateFuture;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.service.EventDispatcher;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.Serialization;

public class SearchProtocol extends ClusterProtocol {
	
	public static enum Operation {
		ADDITEM, SEARCHITEMS
	}
	
	private ServiceManager serviceManager = null;
	private SearchService searchService = null;
	
	private ConcurrentHashMap<Long, FutureResponse<Object>> responses = null;
	
	public SearchProtocol(EventDispatcher eventDispatcher, ClusterService clusterService, Integer port) {
		super(eventDispatcher, clusterService, port);
		this.responses = new ConcurrentHashMap<Long, FutureResponse<Object>>();
	}
	
	@Override
	public void init(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.searchService = this.serviceManager.getService(SearchService.class);
	}

	@Override
	public Future<byte[]> onRequest(byte[] request) {
		ByteBuffer buffer = ByteBuffer.wrap(request);
		
		long reqID = buffer.getLong();
		int operation = buffer.getInt();
		
		switch (Operation.values()[operation]) {
			case ADDITEM: {				
				int itemLen = buffer.getInt();
				byte[] itemBytes = new byte[itemLen];
				buffer.get(itemBytes);
				String item = new String(itemBytes);
				
				this.searchService.endpointAddItem(item);
				
				ByteBuffer response = ByteBuffer.allocate(8 + 4 + itemLen);
				response.putLong(reqID);
				response.putInt(operation);
				response.put(itemBytes);
				
				return new ImmediateFuture<byte[]>(response.array());
			}
			
			case SEARCHITEMS: {
				double lat = buffer.getDouble();
				double lng = buffer.getDouble();
				GeoPoint geo = new GeoPoint(lat, lng);
				
				int queryLen = buffer.getInt();
				byte[] queryBytes = new byte[queryLen];
				buffer.get(queryBytes);
				String query = new String(queryBytes);
				
				List<Pair<Float, String>> resultKeys = this.searchService.endpointSearchItems(geo, query);
				
				int totalBytes = 0;
				for (Pair<Float, String> res: resultKeys) {
					totalBytes += 8;
					totalBytes += 4;
					totalBytes += res.b().getBytes().length;
				}
				
				byte[] resultBytes = new byte[totalBytes];
				int resultOffset = 0;
				for (Pair<Float, String> res: resultKeys) {
					byte[] scoreBytes = Serialization.serialize(res.a());
					byte[] keyBytes = res.b().getBytes();
					byte[] lenBytes = Serialization.serialize(res.b().getBytes().length);

					System.arraycopy(resultBytes, resultOffset, scoreBytes, 0, scoreBytes.length);
					resultOffset += scoreBytes.length;
					
					System.arraycopy(resultBytes, resultOffset, lenBytes, 0, lenBytes.length);
					resultOffset += lenBytes.length;
					
					System.arraycopy(resultBytes, resultOffset, keyBytes, 0, keyBytes.length);
					resultOffset += keyBytes.length;
				}
				
				ByteBuffer response = ByteBuffer.allocate(8 + 4 + 4 + resultBytes.length);
				response.putLong(reqID);
				response.putInt(operation);
				response.putInt(resultKeys.size());
				response.put(resultBytes);
				
				return new ImmediateFuture<byte[]>(response.array());
			}
		}
		
		ByteBuffer response = ByteBuffer.allocate(8 + 4);
		response.putLong(reqID);
		response.putInt(operation);
		
		return new ImmediateFuture<byte[]>(response.array());
	}

	@Override
	public Future<byte[]> onResponse(byte[] response) {
		ByteBuffer buffer = ByteBuffer.wrap(response);
		
		long reqID = buffer.getLong();
		int operation = buffer.getInt();
		
		switch (Operation.values()[operation]) {
			case ADDITEM: {
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				
				this.responses.get(reqID).set(new String(bytes));
				break;
			}
			
			case SEARCHITEMS: {
				int resultCount = buffer.getInt();
				
				List<Pair<Float, String>> results = new ArrayList<Pair<Float, String>>(resultCount);
				for (int i = 0; i < resultCount; i++) {
					float resultScore = buffer.getFloat();
					int resultLen = buffer.getInt();
					byte[] resultBytes = new byte[resultLen];
					buffer.get(resultBytes);
					String result = new String(resultBytes);
					results.add(new Pair<Float, String>(resultScore, result));
				}
				
				this.responses.get(reqID).set(results);
				
				break;
			}
		}
		
		return new ImmediateFuture<byte[]>(response);
	}
	
	public FutureResponse<Object> addItem(Entity<Item> item) {
		long reqID = this.requestCounter.getAndIncrement();
		
		ClusterNode node = this.heartbeat.hashPosition(item.getKey());
		SocketConnection connection = this.connectionPool.getConnection(node);
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + item.getKey().getBytes().length);
		
		buffer.putLong(reqID);
		buffer.putInt(Operation.ADDITEM.ordinal());
		buffer.putInt(item.getKey().getBytes().length);
		buffer.put(item.getKey().getBytes());
		
		FutureResponse<Object> ret = new FutureResponse<Object>();
		this.responses.put(reqID, ret);
		
		connection.send(buffer.array());
		
		return ret;
	}
	
	public BatchFutureResponse<Pair<Float, String>> searchItems(GeoPoint geo, String query) {
		Collection<ClusterNode> nodes = this.heartbeat.getAllNodes(SearchLayer.class);
		BatchFutureResponse<Pair<Float, String>> ret = new BatchFutureResponse<Pair<Float, String>>(nodes.size());
		
		for (ClusterNode node: nodes) {
			this.searchItems(ret, node, geo, query);
		}
		
		return ret;
	}
	
	private FutureResponse<Object> searchItems(BatchFutureResponse<Pair<Float, String>> batchResponse, ClusterNode node, GeoPoint geo, String query) {
		long reqID = this.requestCounter.getAndIncrement();
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 8 + 8 + 4 + query.getBytes().length);
		
		buffer.putLong(reqID);
		buffer.putInt(Operation.ADDITEM.ordinal());
		buffer.putDouble(geo.getLat());
		buffer.putDouble(geo.getLng());
		buffer.putInt(query.getBytes().length);
		buffer.put(query.getBytes());
		
		FutureResponse<Object> ret = batchResponse.getComponentFuture(Object.class);
		this.responses.put(reqID, ret);
		
		SocketConnection connection = this.connectionPool.getConnection(node);
		connection.send(buffer.array());
		
		return ret;
	}

}
