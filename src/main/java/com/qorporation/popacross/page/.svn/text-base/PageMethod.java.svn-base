package com.qorporation.popacross.page;

import java.lang.reflect.Method;
import java.util.List;

import com.google.code.regexp.NamedPattern;

import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewHandler;
import com.qorporation.qluster.view.ViewMethod;
import com.qorporation.qluster.view.ViewRequest;

public class PageMethod extends ViewMethod<PageView, User> {

	public PageMethod(Method method, ViewHandler<PageView, User> handler,
			List<NamedPattern> patterns, ViewAuthenticator<User> authenticator) {
		super(method, handler, patterns, authenticator);
	}
	
	public boolean authenticate(ViewRequest<PageView, User> req) {
		Entity<User> user = req.getUser();
		
		if (user == null || user.getKey() == null || user.getKey().equals("0")) {
			user = authenticator.getUserFromViewRequest(req);
			req.setUser(user);
		}
		
		return authenticator.authenticate(method.getAnnotation(AuthenticationPolicy.class).level(), user);
	}

}
