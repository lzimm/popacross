package com.qorporation.qluster.view;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.ErrorControl;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;

public class ViewMethod<T extends ViewType, U extends Definition<? extends Connection>> {

	protected Method method = null;
	protected ViewHandler<T, U> handler = null;
	protected List<NamedPattern> patterns = null;
	protected ViewAuthenticator<U> authenticator = null;
	
	public ViewMethod(Method method, ViewHandler<T, U> handler, List<NamedPattern> patterns, ViewAuthenticator<U> authenticator) {
		this.method = method;
		this.handler = handler;
		this.patterns = patterns;
		this.authenticator = authenticator;
	}
	
	public boolean match(ViewRequest<T, U> req) {
		for (NamedPattern pattern: patterns) {
			NamedMatcher m = pattern.matcher(req.getPath().toLowerCase());
			if (m.matches()) {
				for (String group: pattern.groupNames()) {
					req.setParameter(group, req.getPath().substring(m.start(group), m.end(group)));
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public void run(ViewRequest<T, U> req) {
		try {
			boolean authenticated = true;
			
			if (method.isAnnotationPresent(AuthenticationPolicy.class)) {
				authenticated = authenticate(req);
			}
			
			if (authenticated) {
				method.invoke(handler, req);
			} else {
				req.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public boolean authenticate(ViewRequest<T, U> req) {
		Entity<U> user = req.getUser();
		return this.authenticator.authenticate(method.getAnnotation(AuthenticationPolicy.class).level(), user);
	}
	
}
