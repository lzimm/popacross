package com.qorporation.qluster.external;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;

import com.freebase.api.Freebase;

public class ExternalService extends Service {

	private Freebase freebase = null;

	@Override
	public void init(ServiceManager serviceManager, Config config) {
		this.freebase = Freebase.getFreebase();
	}
	
	public Freebase getFreebase() { return this.freebase; }
	
}
