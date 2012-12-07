package com.qorporation.qluster.logic;

import java.util.HashMap;
import java.util.Iterator;

import com.qorporation.qluster.async.AsyncService;
import com.qorporation.qluster.cluster.ClusterService;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.external.ExternalService;
import com.qorporation.qluster.geo.GeoService;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;
import com.qorporation.qluster.util.ErrorControl;

public class LogicService extends Service {
	
	private ServiceManager serviceManager = null;
	
	private EntityService entityService = null;
	private ClusterService clusterService = null;
	private ExternalService externalService = null;
	private GeoService geoService = null;
	private AsyncService asyncService = null;

	private HashMap<Class<? extends LogicController>, LogicController> controllers;
	
	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.logger.info("Loading logic service");
		
		this.serviceManager = serviceManager;
		
		this.entityService = this.serviceManager.getService(EntityService.class);
		this.clusterService = this.serviceManager.getService(ClusterService.class);
		this.externalService = this.serviceManager.getService(ExternalService.class);
		this.geoService = this.serviceManager.getService(GeoService.class);
		this.asyncService = this.serviceManager.getService(AsyncService.class);
		
		setupControllers();
	}

	@SuppressWarnings("unchecked")
	private void setupControllers() {
		this.logger.info("Setting up controllers");
		
		this.controllers = new HashMap<Class<? extends LogicController>, LogicController>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(LogicController.class));
		
		while (itr.hasNext()) {
			Class<? extends LogicController> cls = (Class<? extends LogicController>) itr.next();
			
			try {
				LogicController controller = cls.newInstance();
				this.controllers.put(cls, controller);
			} catch (Exception e) {
				ErrorControl.logException(e);
			}
		}
	}
	
	@Override
	public void postInit() {
		for (LogicController controller: this.controllers.values()) {
			controller.setup(this.serviceManager, this);
			controller.init();
		}
	}
	
	public EntityService getEntityService() { return this.entityService; }
	public ClusterService getClusterService() { return this.clusterService; }
	public GeoService getGeoService() { return this.geoService; }
	public ExternalService getExternalService() { return this.externalService; }
	public AsyncService getAsyncService() { return this.asyncService; }
	
	@SuppressWarnings("unchecked")
	public <T extends LogicController> T get(Class<T> cls) {
		return (T) this.controllers.get(cls);
	}

}
