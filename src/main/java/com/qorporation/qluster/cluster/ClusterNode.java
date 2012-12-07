package com.qorporation.qluster.cluster;

import java.net.InetAddress;

public class ClusterNode {

	public static ClusterNode NONE = new ClusterNode(null);
	
	private InetAddress address = null;
	
	public ClusterNode(InetAddress address) {
		this.address = address;
	}
	
	public InetAddress getAddress() { return this.address; }
	
}
