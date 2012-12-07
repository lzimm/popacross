package com.qorporation.qluster.geo;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.external.ExternalService;
import com.qorporation.qluster.service.Service;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.RelativePath;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

@SuppressWarnings("unused")
public class GeoService extends Service {
	public static final String FORGED_IP_ON_LOCALHOST = "96.49.202.135";
	public static final Pair<Double, Double> DEFAULT_LAT_LON = new Pair<Double, Double>(0.0D, 0.0D);
	public static final Triple<String, Double, Double> DEFAULT_CITY_LAT_LON = new Triple<String, Double, Double>("", DEFAULT_LAT_LON.a(), DEFAULT_LAT_LON.b());
	
	private ExternalService externalService = null;
	private LookupService lookupService = null;
	
	@Override
    public void init(ServiceManager serviceManager, Config config) {
		this.externalService = serviceManager.getService(ExternalService.class);
		
    	try {
    		File dataFile = new File(RelativePath.root().getAbsolutePath()
    				.concat(File.separator)
    				.concat("data")
    				.concat(File.separator)
    				.concat("geoip")
    				.concat(File.separator)
    				.concat("GeoIPCity.dat"));
    		
    		this.lookupService = new LookupService(dataFile.getPath(), LookupService.GEOIP_MEMORY_CACHE);
    	} catch (Exception e) {
    		ErrorControl.logException(e);
    	}
    }
    
    public Location lookupIP(String ip) {
    	if (ip.equals("127.0.0.1")) {
    		ip = FORGED_IP_ON_LOCALHOST;
    	}
    	
    	return this.lookupService.getLocation(ip);
    }
	
}
