package com.qorporation.popacross.page;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;

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

public class PageRequest extends ViewRequest<PageView, User> {
	public static final String REDIRECT_PATH = "REDIRECT_PATH";
	public static final String IP_LOCATION = "IP_LOCATION";

	protected EntityService entityService = null;
	protected Configuration templateConfig = null;
	protected LogicService logicService = null;
	protected GeoService geoService = null;
	
	private Map<String, Object> renderVars = null;
	
	public PageRequest(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	public PageRequest(EntityService entityService, LogicService logicService, Configuration templateConfig, HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		this.entityService = entityService;
		this.logicService = logicService;
		this.geoService = logicService.getGeoService();
		this.templateConfig = templateConfig;
		this.renderVars = new HashMap<String, Object>();
		updateTemplateWithGlobalVariables(this.renderVars);
	}
	
	public void renderTemplate(String path) {
		try {
			Template template = this.templateConfig.getTemplate(path);
			template.process(this.renderVars, this.response.getWriter());
	        this.response.flushBuffer();
		} catch (Exception e) {
			sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public byte[] renderTemplateAndGetBytes(String path) {
		byte[] ret = null;
		
		try {
			CharArrayWriter writer = new CharArrayWriter();
			
			Template template = this.templateConfig.getTemplate(path);
			template.process(this.renderVars, writer);
			
			char[] buf = writer.toCharArray();
			ret = Serialization.serialize(new String(buf));
			
			this.response.getWriter().write(buf);
	        this.response.flushBuffer();
		} catch (Exception e) {
			sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
		return ret;
	}
	
	public void renderCached(byte[] cachedVal) {
		try {
			this.response.getOutputStream().write(cachedVal);
			this.response.flushBuffer();
		} catch (Exception e) {
			ErrorControl.logException(e);
			sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	public void addRenderVar(String key, Object object) {
		this.renderVars.put(key, object);
	}
	
	public Object getRenderVar(String key) {
		return this.renderVars.get(key);
	}

	@SuppressWarnings("unchecked")
	private void updateTemplateWithGlobalVariables(Map<String, Object> root) {
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
	
	public void redirectTo(String path) {
		try {
			path = this.response.encodeRedirectURL(path);
			this.response.sendRedirect(path);
			this.response.flushBuffer();
		} catch (IOException e) {
			sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
