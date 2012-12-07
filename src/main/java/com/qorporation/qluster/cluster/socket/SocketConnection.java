package com.qorporation.qluster.cluster.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import com.qorporation.qluster.cluster.ClusterProtocol;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class SocketConnection {
	protected final int IO_SIZE = 128*1024;
	protected final int QUEUE_SIZE = 64;
	
	protected Socket socket = null;
	protected SocketConnectionPool pool = null;
	protected ClusterProtocol protocol = null;
	protected InetAddress address = null;
	protected int port = -1;
	
	protected BufferedInputStream input = null;
	protected BufferedOutputStream output = null;
	
	protected int expected = -1;
	
	protected int inputOffset = 0;
	protected byte[] inbytes = null;
	
	protected ArrayBlockingQueue<byte[]> requests = null;
	
	public SocketConnection(SocketConnectionPool pool, ClusterProtocol protocol, InetAddress address, int port) {
		this.pool = pool;
		this.protocol = protocol;
		this.address = address;
		this.port = port;
		this.inbytes = new byte[IO_SIZE];
		this.requests = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
		
		try {
			this.socket = new Socket(address.getHostAddress(), port);
			this.input = new BufferedInputStream(this.socket.getInputStream());
			this.output = new BufferedOutputStream(this.socket.getOutputStream());
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public boolean handle() {
		boolean doneWork = false;
		
		try {
			int available = this.input.available();
			while (available > 0) {
				if (this.expected < 0 && available >= 4) {
					byte[] expectedBytes = new byte[4];
					this.input.read(expectedBytes, 0, 4);
					this.expected = Serialization.deserializeInt(expectedBytes);
					available = this.input.available();
				} else {
					break;
				}
				
				if (this.expected > 0) {
					this.inputOffset += this.input.read(this.inbytes, 
														this.inputOffset, 
														this.expected - this.inputOffset);
				}
				
				if (this.inputOffset == this.expected) {
					byte[] response = new byte[this.expected];
					System.arraycopy(this.inbytes, 0, response, 0, this.expected);
					
					// TODO: REMOVE THIS DEBUGGING STUFF
					System.out.println(String.format("REMOVE THIS LOG: SocketConnection (System.arraycopy(this.inbytes, 0, response, 0, this.expected)): %s", Arrays.toString(response)));
					
					this.protocol.onResponse(response);
					this.expected = -1;
					this.inputOffset = 0;
				}
				
				available = this.input.available();
				doneWork = true;
			}
			
			while (!this.requests.isEmpty()) {
				byte[] request = this.requests.poll();
				
				// TODO: REMOVE THIS DEBUGGING STUFF
				System.out.println(String.format("REMOVE THIS LOG: SocketConnection (byte[] request = this.requests.poll()): %s", Arrays.toString(request)));
				
				this.output.write(Serialization.serialize(request.length));
				this.output.write(request);
				doneWork = true;
			}
			
			this.output.flush();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return doneWork;
	}
	
	public void close() {
		try {
			this.pool.removeConnection(this);
			socket.close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	public boolean isConnected() {
		return socket.isConnected();
	}
	
	public void send(byte[] request) {
		this.requests.add(request);
	}
	
	public InetAddress getAddress() { return this.address; }
	
}
