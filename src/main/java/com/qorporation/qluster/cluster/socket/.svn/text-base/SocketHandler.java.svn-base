package com.qorporation.qluster.cluster.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import com.qorporation.qluster.cluster.ClusterProtocol;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class SocketHandler {
	protected final int IO_SIZE = 128*1024;
	protected final int QUEUE_SIZE = 64;
	
	protected Socket socket = null;
	protected SocketWorkerPool pool = null;
	protected ClusterProtocol protocol = null;
	
	protected BufferedInputStream input = null;
	protected BufferedOutputStream output = null;
	
	protected int expected = -1;
	
	protected int inputOffset = 0;
	protected byte[] inbytes = null;
	
	protected ArrayBlockingQueue<byte[]> responses = null;
	
	public SocketHandler(Socket socket, SocketWorkerPool pool, ClusterProtocol protocol) {
		this.socket = socket;
		this.pool = pool;
		this.protocol = protocol;
		
		this.inbytes = new byte[IO_SIZE];
		
		this.responses = new ArrayBlockingQueue<byte[]>(QUEUE_SIZE);
		this.pool.addHandler(this, this.responses);
		
		try {
			this.input = new BufferedInputStream(socket.getInputStream());
			this.output = new BufferedOutputStream(socket.getOutputStream());
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
					byte[] request = new byte[this.expected];
					System.arraycopy(this.inbytes, 0, request, 0, this.expected);
					this.pool.addRequest(this, request);
					this.expected = -1;
					this.inputOffset = 0;
				}
				
				available = this.input.available();
				doneWork = true;
			}
			
			while (!this.responses.isEmpty()) {
				byte[] response = this.responses.poll();
				this.output.write(Serialization.serialize(response.length));
				this.output.write(response);
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
			this.pool.removeHandler(this);
			socket.close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	public boolean isConnected() {
		return socket.isConnected();
	}
	
}
