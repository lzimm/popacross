package com.qorporation.qluster.service;

import com.qorporation.qluster.common.MultiMap;
import com.qorporation.qluster.event.Event;
import com.qorporation.qluster.event.EventHandler;
import com.qorporation.qluster.util.Reflection;

public class EventDispatcher {
	
	private MultiMap<Class<? extends Event>, EventHandler<? extends Event>> handlers;
	
	public EventDispatcher() {
		this.handlers = new MultiMap<Class<? extends Event>, EventHandler<? extends Event>>();
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Event> void registerHandler(EventHandler<E> handler) {
		Class<? extends Event> cls = (Class<? extends Event>) Reflection.getParamType(handler.getClass(), 0);
		this.handlers.add(cls, handler);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Event> void dispatchEvent(E e) {
		for (EventHandler<? extends Event> handler: this.handlers.get(e.getClass())) {
			((EventHandler<E>) handler).handle(e);
		}
	}
	
}
