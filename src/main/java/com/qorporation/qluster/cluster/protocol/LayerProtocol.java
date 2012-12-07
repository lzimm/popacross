package com.qorporation.qluster.cluster.protocol;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.qorporation.qluster.cluster.ClusterNode;
import com.qorporation.qluster.cluster.ClusterProtocol;
import com.qorporation.qluster.cluster.ClusterService;
import com.qorporation.qluster.cluster.layer.ClusterLayer;
import com.qorporation.qluster.cluster.socket.SocketConnection;
import com.qorporation.qluster.common.FutureResponse;
import com.qorporation.qluster.common.ImmediateFuture;
import com.qorporation.qluster.service.EventDispatcher;
import com.qorporation.qluster.service.ServiceManager;

public class LayerProtocol extends ClusterProtocol {
	
	public static enum Operation {
		ACTIVATE, DEACTIVATE
	}
	
	private ServiceManager serviceManager = null;
	private ConcurrentHashMap<Long, FutureResponse<Object>> responses = null;
	
	public LayerProtocol(EventDispatcher eventDispatcher, ClusterService clusterService, Integer port) {
		super(eventDispatcher, clusterService, port);
	}
	
	@Override
	public void init(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	@Override
	public Future<byte[]> onRequest(byte[] request) {
		ByteBuffer buffer = ByteBuffer.wrap(request);
		
		buffer.getLong();
		
		Class<? extends ClusterLayer> cls = this.clusterService.getLayerManager().getLayer(buffer.getInt());
		ClusterLayer layer = this.clusterService.getLayerManager().getLayer(cls);
		
		switch (Operation.values()[buffer.getInt()]) {
			case ACTIVATE:
				layer.activate(this.serviceManager);
				this.clusterService.getHeartbeat().addLayer(cls);
			break;
			case DEACTIVATE:
				this.clusterService.getHeartbeat().removeLayer(cls);
				layer.deactivate(this.serviceManager);
			break;
		}
		
		return new ImmediateFuture<byte[]>(request);
	}

	@Override
	public Future<byte[]> onResponse(byte[] response) {		
		ByteBuffer buffer = ByteBuffer.wrap(response);
		
		long reqID = buffer.getLong();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		
		this.responses.get(reqID).set(new String(bytes));
		
		return new ImmediateFuture<byte[]>(response);
	}
	
	public FutureResponse<Object> hintNode(ClusterNode node, ClusterLayer layer, Operation operation) {
		long reqID = this.requestCounter.getAndIncrement();

		SocketConnection connection = this.connectionPool.getConnection(node);
		
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4);
		
		buffer.putLong(reqID);
		buffer.putInt(this.clusterService.getLayerManager().getOrdinal(layer.getClass()));
		buffer.putInt(operation.ordinal());
		
		FutureResponse<Object> ret = new FutureResponse<Object>();
		this.responses.put(reqID, ret);
		
		connection.send(buffer.array());
		
		return ret;
	}

}
