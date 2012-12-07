package com.qorporation.popacross.api;

import java.io.CharArrayWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import com.maxmind.geoip.Location;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.entity.manager.UserManager;
import com.qorporation.popacross.page.directives.NewlineDirective;
import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.geo.GeoService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;
import com.qorporation.qluster.view.ViewRequest;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class APIRequest extends ViewRequest<APIView, User> {
	public static final String IP_LOCATION = "IP_LOCATION";
	
	protected EntityService entityService = null;
	protected Configuration templateConfig = null;
	protected LogicService logicService = null;
	protected GeoService geoService = null;
	
	public APIRequest(EntityService entityService, LogicService logicService, Configuration templateConfig, HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		this.entityService = entityService;
		this.logicService = logicService;
		this.geoService = logicService.getGeoService();
		this.templateConfig = templateConfig;
	}
	
	public void sendResponse(Object res) {
		try{
			JSON json = null;
			
			if (res instanceof JSON) {
				json = (JSON) res;
			} else {
				json = JSONSerializer.toJSON(res);
			}
			
			String jsonString = json.toString();
			byte[] jsonStringBytes = Serialization.serialize(jsonString);
			
			this.response.setContentLength(jsonStringBytes.length);
			this.response.getOutputStream().write(jsonStringBytes);
			this.response.getOutputStream().flush();
			this.response.getOutputStream().close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	public String renderBlock(String path, Map<String, Object> vars) {
		String ret = null;
		
		try {
			CharArrayWriter writer = new CharArrayWriter();
			
			this.updateVarsWithGlobals(vars);
			
			Template template = this.templateConfig.getTemplate(path);
			template.process(vars, writer);
			
			char[] buf = writer.toCharArray();
			ret = new String(buf);
		} catch (Exception e) {
			sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private void updateVarsWithGlobals(Map<String, Object> root) {
		Entity<User> user = this.getUser();
		if (user == null) {
			user = this.entityService.getManager(User.class, UserManager.class).getDummy();
		}
		
		Triple<String, Double, Double> geoip = (Triple<String, Double, Double>) this.getSessionVariable(IP_LOCATION);
		if (geoip == null) {
			Location ipLocation = this.geoService.lookupIP(this.getIP());
			if (ipLocation != null) {
				geoip = new Triple<String, Double, Double>(ipLocation.city, Double.valueOf(ipLocation.latitude), Double.valueOf(ipLocation.longitude));
			} else {
				geoip = GeoService.DEFAULT_CITY_LAT_LON;
			}
			
			this.setSessionVariable(IP_LOCATION, geoip);
		}
		
		root.put("me", user);
		root.put("geoip", geoip);
		root.put("newlines", new NewlineDirective());
	}

}
