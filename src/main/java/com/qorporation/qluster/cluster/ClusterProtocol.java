package com.qorporation.qluster.cluster;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import com.qorporation.qluster.cluster.socket.SocketAcceptor;
import com.qorporation.qluster.cluster.socket.SocketConnectionPool;
import com.qorporation.qluster.cluster.socket.SocketWorkerPool;
import com.qorporation.qluster.service.EventDispatcher;
import com.qorporation.qluster.service.ServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterProtocol {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected Integer port = null;
	
	protected EventDispatcher eventDispatcher = null;
	protected ClusterService clusterService = null;
	protected ClusterHeartbeat heartbeat = null;
	
	protected SocketWorkerPool workerPool = null;
	protected SocketAcceptor acceptor = null;
	protected SocketConnectionPool connectionPool = null;
	
	protected AtomicLong requestCounter = null;
	
	public ClusterProtocol(EventDispatcher eventDispatcher, ClusterService clusterService, Integer port) {
		this.eventDispatcher = eventDispatcher;
		this.clusterService = clusterService;
		this.port = port;
		
		this.heartbeat = this.clusterService.getHeartbeat();
		this.requestCounter = new AtomicLong();
		
		this.workerPool = new SocketWorkerPool(this);
		this.acceptor = new SocketAcceptor(this, this.workerPool, this.port);		
		this.connectionPool = new SocketConnectionPool(this, this.port);
		
		this.acceptor.start();
	}
	
	public void init(ServiceManager serviceManager) {}
	
	public void onNodeDiscovery(ClusterNode node) {}
	public void onNodeFailure(ClusterNode node) {}
	
	public abstract Future<byte[]> onRequest(byte[] request);
	public abstract Future<byte[]> onResponse(byte[] response);
	
}
