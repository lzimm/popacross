package com.qorporation.qluster.common;

import java.nio.ByteBuffer;

public class FutureByteResponse extends FutureResponse<byte[]> {

	private byte[] header = null;
	
	public FutureByteResponse(byte[] header) {
		this.header = header;
	}
	
	@Override
	public void set(byte[] val) {
		if (this.header != null) {
			ByteBuffer b = ByteBuffer.allocate(header.length + val.length);
			b.put(header);
			b.put(val);
			val = b.array();
		}
		
		this.val.set(val);
		this.latch.countDown();
	}
	
}
