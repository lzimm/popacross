package com.qorporation.popacross.api.handler;

import java.util.HashMap;
import java.util.Map;

import com.qorporation.popacross.api.APIHandler;
import com.qorporation.popacross.api.APIRequest;
import com.qorporation.popacross.entity.definition.Identity;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.logic.AuthenticationLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class AuthHandler extends APIHandler {

	private AuthenticationLogic authLogic = null;

	@Override
	public void init() {
		this.authLogic = this.logicService.get(AuthenticationLogic.class);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/register"})
	public void register(APIRequest request) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("result", false);
		
		Entity<User> user = this.authLogic.register(email, password);
		
		if (user != null) {
			request.setUser(user);
			res.put("result", true);
		}	
		
		request.sendResponse(res);
	}

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/login"})
	public void login(APIRequest request) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("result", false);
		
		Entity<User> user = this.authLogic.login(email, password);
		
		if (user != null) {
			request.setUser(user);
			res.put("result", true);
		}	
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/refresh"})
	public void refresh(APIRequest request) {
		Entity<User> user = request.getUser();
		
		Map<String, Object> res = new HashMap<String, Object>();
		if (user != null) {
			res.put("status", true);
			res.put("email", user.get(User.email));
			
			Entity<Identity> facebookIdentity = user.get(User.facebookIdentity);
			if (facebookIdentity != null) {
				res.put("facebook", facebookIdentity.get(Identity.user));
				res.put("facebook_name", facebookIdentity.get(Identity.name));
				res.put("facebook_url", facebookIdentity.get(Identity.url));
			}
			
			Entity<Identity> twitterIdentity = user.get(User.twitterIdentity);
			if (twitterIdentity != null) {
				res.put("twitter", twitterIdentity.get(Identity.user));
				res.put("twitter_name", twitterIdentity.get(Identity.name));
				res.put("twitter_url", twitterIdentity.get(Identity.url));
			}
		} else {
			res.put("status", false);
		}
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/auth/twitter/remove"})
	public void removeTwitter(APIRequest request) {
		Entity<User> user = request.getUser();		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("status", this.authLogic.removeTwitter(user));
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.USER)
	@Routing(patterns={"/auth/facebook/remove"})
	public void removeFacebook(APIRequest request) {
		Entity<User> user = request.getUser();		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("status", this.authLogic.removeFacebook(user));
		request.sendResponse(res);
	}
	
}
