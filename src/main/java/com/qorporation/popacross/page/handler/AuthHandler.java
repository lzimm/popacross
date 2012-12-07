package com.qorporation.popacross.page.handler;

import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.logic.AuthenticationLogic;
import com.qorporation.popacross.page.PageHandler;
import com.qorporation.popacross.page.PageRequest;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;

public class AuthHandler extends PageHandler {
	
	private AuthenticationLogic authLogic = null;
	
	@Override
	public void init() {
		this.authLogic = this.logicService.get(AuthenticationLogic.class);
	}

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/register"})
	public void register(PageRequest request) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		if (email.length() > 0 && password.length() > 0) {
			Entity<User> user = this.authLogic.register(email, password);
			
			if (user != null) {
				request.setUser(user);
				
				String redirectPath = (String) request.getSessionVariable(PageRequest.REDIRECT_PATH, "/");
				
				if (redirectPath == null) {
					redirectPath = "/home";
				}
				
				request.redirectTo(redirectPath);
				return;
			}
		}
		
		request.renderTemplate("auth/register.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/login"})
	public void login(PageRequest request) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		Entity<User> user = this.authLogic.login(email, password);
		
		if (user != null) {
			String redirectPath = (String) request.getSessionVariable(PageRequest.REDIRECT_PATH, "/");
			request.setUser(user);
			request.redirectTo(redirectPath);
			return;
		}
		
		request.renderTemplate("auth/login.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/login/twitter"})
	public void loginTwitter(PageRequest request) {
		request.redirectTo(this.authLogic.getTwitterAuthURL());
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/callback/twitter"})
	public void callbackTwitter(PageRequest request) {
		String token = request.getParameter("oauth_token");
		String verifier = request.getParameter("oauth_verifier");
		
		if (request.getUser() == null) {
			Entity<User> user = this.authLogic.getOrCreateWithTwitterVerifier(token, verifier);
			if (user != null) {
				request.setUser(user);
			}
		} else {
			this.authLogic.updateTwitterVerifier(request.getUser(), token, verifier);
		}
		
		request.renderTemplate("auth/callback/twitter.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/login/facebook"})
	public void loginFacebook(PageRequest request) {
		request.redirectTo(this.authLogic.getFacebookAuthURL());
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/auth/callback/facebook"})
	public void callbackFacebook(PageRequest request) {
		String verifier = request.getParameter("code");
		
		if (request.getUser() == null) {
			Entity<User> user = this.authLogic.getOrCreateWithFacebookVerifier(verifier);
			if (user != null) {
				request.setUser(user);
			}
		} else {
			this.authLogic.updateFacebookVerifier(request.getUser(), verifier);
		}
		
		request.renderTemplate("auth/callback/facebook.html");
	}
	
}