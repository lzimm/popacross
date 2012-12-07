package com.qorporation.qluster.view;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qorporation.qluster.common.MultiMap;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ViewRequest<T extends ViewType, U extends Definition<? extends Connection>> {
	private static final String USER = "USER";
	
	public HttpServletRequest request = null;
	public HttpServletResponse response = null;
	public Map<String, String> routeParams = null;
	public MultiMap<String, String> parameterMap = null;
	
	public ViewRequest(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.routeParams = new HashMap<String, String>();
		this.parameterMap = this.buildParameterMap();
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, FileItem> getUploadedFiles() {
		Map<String, FileItem> ret = new HashMap<String, FileItem>();
		
		try {
			if (ServletFileUpload.isMultipartContent(request)) {
				ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
				List<FileItem> fileList = uploadHandler.parseRequest(this.request);
				for (FileItem fileItem: fileList) {
					if (!fileItem.isFormField()) {
						ret.put(fileItem.getFieldName(), fileItem);
					}
				}
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return ret;
	}
	
	public ServletInputStream getInputStream() {
		try {
			return this.request.getInputStream();
		} catch (Exception e) {
			ErrorControl.logException(e);
			return null;
		}
	}
	
	public String getPostString() {
		try {
			int length = this.request.getContentLength();
			int read = 0;
			byte[] bytes = new byte[length];
			ServletInputStream in = request.getInputStream();
			
			while (read < length) {
				int c = in.read(bytes, read, length - read);
				if (c > 0) {
					read += c;
				} else {
					Thread.sleep(10);
				}
			}
			
			in.close();
			
			return Serialization.deserializeString(bytes);
		} catch (Exception e) {
			ErrorControl.logException(e);
			return null;
		}		
	}
	
	public String getPath() {
		return this.request.getRequestURI();
	}
	
	public String getIP() {
		return this.request.getRemoteAddr();
	}

	public void sendError(int code) {
		try {
			this.response.sendError(code);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public String getParameter(String key) { return this.getParameter(key, ""); }
	public String getParameter(String key, String defaultValue) {
		if (this.routeParams.containsKey(key)) {
			return this.routeParams.get(key);
		}

		return this.parameterMap.first(key, defaultValue);
	}
	
	public String removeParameter(String key) { return this.removeParameter(key, ""); }
	public String removeParameter(String key, String defaultValue) {
		return this.parameterMap.getFirstAndRemove(key, defaultValue);
	}
	
	public Map<String, String> getPrefixedParams(String prefix) { return this.getPrefixedParams(prefix, "", true); }
	public Map<String, String> getPrefixedParams(String prefix, boolean keepPrefix) { return this.getPrefixedParams(prefix, "", keepPrefix); }
	public Map<String, String> getPrefixedParams(String prefix, String defaultValue, boolean keepPrefix) { 
		Map<String, String> ret = new HashMap<String, String>();
		for (String key: this.parameterMap.keySet()) {
			if (key.startsWith(prefix)) {
				ret.put(keepPrefix ? key : key.replaceFirst(prefix, ""), this.parameterMap.first(key, defaultValue));
			}
		}
		
		return ret;
	}

	
	public MultiMap<String, String> getParameterMap() {
		return this.parameterMap;
	}
	
	public MultiMap<String, String> buildParameterMap() {
		MultiMap<String, String> ret = new MultiMap<String, String>();
		
		Enumeration<?> keys = this.request.getParameterNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			for (String val: this.request.getParameterValues(key)) {
				ret.add(key, val);
			}
		}
		
		return ret;
	}

	public void setParameter(String key, String value) {
		this.routeParams.put(key, value);
	}

	public void setSessionVariable(String key, Object object) {
		this.request.getSession(true).setAttribute(key, object);
	}
	
	public Object getSessionVariable(String key) { return getSessionVariable(key, null); }
	public Object getSessionVariable(String key, Object def) {
		Object ret = this.request.getSession(true).getAttribute(key);
		if (ret == null) ret = def;
		return ret;
	}
	
	public void setUser(Entity<U> user) {
		this.setSessionVariable(USER, user);
	}
	
	@SuppressWarnings("unchecked")
	public Entity<U> getUser() {
		return (Entity<U>) this.getSessionVariable(USER);
	}
	
}
