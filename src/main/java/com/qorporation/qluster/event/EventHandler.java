package com.qorporation.qluster.event;

public interface EventHandler<E extends Event> {

	public void handle(E event);
	
}
