package com.qorporation.qluster.view;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qorporation.qluster.annotation.ViewMapping;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Reflection;

public class ViewService extends Service {
	
	private EntityService entityService;
	private LogicService logicService;
	private ViewAuthenticator<? extends Definition<? extends Connection>> authenticator;
	
	private Map<Class<? extends ViewType>, Class<? extends ViewManager<?, ?>>> managerTypeMap;
	private Map<Class<? extends ViewType>, Class<? extends ViewHandler<?, ?>>> handlerTypeMap;
	private Map<Class<? extends ViewType>, Class<? extends ViewMethod<?, ?>>> methodTypeMap;
	private Map<Class<? extends ViewType>, Class<? extends ViewRequest<?, ?>>> requestTypeMap;
	
	private Map<Class<? extends ViewType>, ViewManager<?, ?>> managerMap;
	private Map<String, ViewManager<?, ?>> prefixMap;
	
	private ViewManager<?, ?> primaryManager;

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading view service");
		
		this.entityService = serviceManager.getService(EntityService.class);
		this.logicService = serviceManager.getService(LogicService.class);
		
		Iterator<Class<?>> authenticatorItr = new ClassWalker(ClassWalkerFilter.implementingWithParam(ViewAuthenticator.class, config.userDefinition));
		if (authenticatorItr.hasNext()) {
			Class<?> cls = authenticatorItr.next();
			if (LogicController.class.isAssignableFrom(cls)) {
				this.logger.info(String.format("Using logic controller as view authenticator: %s", cls));
				this.authenticator = (ViewAuthenticator<? extends Definition<? extends Connection>>) this.logicService.get((Class<? extends LogicController>) cls);
			} else {
				try {
					this.logger.info(String.format("Using raw view authenticator: %s", cls));
					this.authenticator = ((Class<ViewAuthenticator<?>>) cls).newInstance();
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
		} else {
			this.logger.error(String.format("Could not find view authenticator for type: %s", config.userDefinition));
		}
		
		setupViews();
	}

	@SuppressWarnings("unchecked")
	private void setupViews() {
		this.logger.info("Setting up views");
		
		this.managerTypeMap = new HashMap<Class<? extends ViewType>, Class<? extends ViewManager<?, ?>>>();
		this.handlerTypeMap = new HashMap<Class<? extends ViewType>, Class<? extends ViewHandler<?, ?>>>();
		this.methodTypeMap = new HashMap<Class<? extends ViewType>, Class<? extends ViewMethod<?, ?>>>();
		this.requestTypeMap = new HashMap<Class<? extends ViewType>, Class<? extends ViewRequest<?, ?>>>();
		
		this.managerMap = new HashMap<Class<? extends ViewType>, ViewManager<?, ?>>();
		this.prefixMap = new HashMap<String, ViewManager<?, ?>>();
		
		Iterator<Class<?>> managerItr = new ClassWalker(ClassWalkerFilter.extending(ViewManager.class));
		while (managerItr.hasNext()) {
			Class<? extends ViewManager<?, ?>> cls = (Class<? extends ViewManager<?, ?>>) managerItr.next();
			if (cls.equals(ViewManager.class)) {
				this.managerTypeMap.put(ViewType.class, cls);
				continue;
			} else if (cls.getSuperclass().equals(ViewManager.class)) {
				this.managerTypeMap.put((Class<? extends ViewType>) Reflection.getParamType(cls, 0), cls);
			}
		}
		
		Iterator<Class<?>> handlerItr = new ClassWalker(ClassWalkerFilter.extending(ViewHandler.class));
		while (handlerItr.hasNext()) {
			Class<? extends ViewHandler<?, ?>> cls = (Class<? extends ViewHandler<?, ?>>) handlerItr.next();
			if (cls.equals(ViewHandler.class)) {
				this.handlerTypeMap.put(ViewType.class, cls);
				continue;
			} else if (cls.getSuperclass().equals(ViewHandler.class)) {
				this.handlerTypeMap.put((Class<? extends ViewType>) Reflection.getParamType(cls, 0), cls);
			}
		}
		
		Iterator<Class<?>> methodItr = new ClassWalker(ClassWalkerFilter.extending(ViewMethod.class));
		while (methodItr.hasNext()) {
			Class<? extends ViewMethod<?, ?>> cls = (Class<? extends ViewMethod<?, ?>>) methodItr.next();
			if (cls.equals(ViewMethod.class)) {
				this.methodTypeMap.put(ViewType.class, cls);
				continue;
			} else if (cls.getSuperclass().equals(ViewMethod.class)) {
				this.methodTypeMap.put((Class<? extends ViewType>) Reflection.getParamType(cls, 0), cls);
			}
		}
		
		Iterator<Class<?>> requestItr = new ClassWalker(ClassWalkerFilter.extending(ViewRequest.class));
		while (requestItr.hasNext()) {
			Class<? extends ViewRequest<?, ?>> cls = (Class<? extends ViewRequest<?, ?>>) requestItr.next();
			if (cls.equals(ViewRequest.class)) {
				this.requestTypeMap.put(ViewType.class, cls);
				continue;
			} else if (cls.getSuperclass().equals(ViewRequest.class)) {
				this.requestTypeMap.put((Class<? extends ViewType>) Reflection.getParamType(cls, 0), cls);
			}
		}
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(ViewType.class));
		
		while (itr.hasNext()) {
			Class<? extends ViewType> cls = (Class<? extends ViewType>) itr.next();
			if (cls.equals(ViewType.class)) continue;
			
			this.logger.info(String.format("Found view type: %s", cls.getName()));
			
			ViewMapping mapping = cls.getAnnotation(ViewMapping.class);
			if (mapping == null) {
				ErrorControl.fatal(String.format("View type: %s has no mapping annotation", cls.getName()));
			}
			
			Class<? extends ViewManager<?, ?>> managerType = this.managerTypeMap.get(cls);
			if (managerType == null) {
				managerType = this.managerTypeMap.get(ViewType.class);
				this.managerTypeMap.put(cls, managerType);
				this.logger.info(String.format("Using generic manager: %s", managerType.getName()));
			} else {
				this.logger.info(String.format("Found manager: %s", managerType.getName()));
			}
			
			Class<? extends ViewHandler<?, ?>> handlerType = this.handlerTypeMap.get(cls);
			if (handlerType == null) {
				handlerType = this.handlerTypeMap.get(ViewType.class);
				this.handlerTypeMap.put(cls, handlerType);
				this.logger.info(String.format("Using generic handler type: %s", handlerType.getName()));
			} else {
				this.logger.info(String.format("Found handler type: %s", handlerType.getName()));
			}
			
			Class<? extends ViewMethod<?, ?>> methodType = this.methodTypeMap.get(cls);
			if (methodType == null) {
				methodType = this.methodTypeMap.get(ViewType.class);
				this.methodTypeMap.put(cls, methodType);
				this.logger.info(String.format("Using generic method type: %s", methodType.getName()));
			} else {
				this.logger.info(String.format("Found method type: %s", methodType.getName()));
			}
			
			Class<? extends ViewRequest<?, ?>> requestType = this.requestTypeMap.get(cls);
			if (requestType == null) {
				requestType = this.requestTypeMap.get(ViewType.class);
				this.requestTypeMap.put(cls, requestType);
				this.logger.info(String.format("Using generic request type: %s", requestType.getName()));
			} else {
				this.logger.info(String.format("Found request type: %s", requestType.getName()));
			}
			
			try {
				Constructor<? extends ViewManager<?, ?>> ctor = managerType.getConstructor(EntityService.class, LogicService.class, ViewAuthenticator.class);
				ViewManager<?, ?> manager = ctor.newInstance(this.entityService, this.logicService, this.authenticator);
				manager.setup(cls, handlerType, methodType, requestType);
				this.managerMap.put(cls, manager);
				
				if (mapping.prefixPath().length() > 0) {
					this.logger.info(String.format("Mapping view type %s to path: %s", cls.getName(), mapping.prefixPath()));
					this.prefixMap.put(mapping.prefixPath(), manager);
				} else {
					if (this.primaryManager == null) {
						this.logger.info(String.format("Mapping view type %s to primary manager", cls.getName()));
						this.primaryManager = manager;
					} else {
						ErrorControl.fatal("found multiple view managers");
					}
				}
			} catch (Exception e) {
				ErrorControl.logException(e);
			}
		}
		
		if (this.primaryManager == null) {
			ErrorControl.fatal("no primary view manager found");
		}
	}

	public void handle(HttpServletRequest request, HttpServletResponse response) {
		for (Entry<String, ViewManager<?, ?>> e: this.prefixMap.entrySet()) {
			if (request.getRequestURI().startsWith(e.getKey())) {
				e.getValue().handle(request, response);
				return;
			}
		}
		
		this.primaryManager.handle(request, response);
	}

}