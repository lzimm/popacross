package com.qorporation.popacross.logic;

import java.util.List;

import javax.servlet.http.Cookie;

import net.sf.json.JSONObject;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.qorporation.popacross.entity.definition.Identity;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.entity.definition.UserToken;
import com.qorporation.popacross.entity.manager.IdentityManager;
import com.qorporation.popacross.entity.manager.UserManager;
import com.qorporation.popacross.entity.manager.UserTokenManager;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewRequest;
import com.qorporation.qluster.view.ViewType;

public class AuthenticationLogic extends LogicController implements ViewAuthenticator<User> {
	private static final String TWITTER_API_KEY = "H5uZEKTMNJPyYMf64hAfA";
	private static final String TWITTER_API_SECRET = "s41DTy3g3nFYXzJ3pUA3hTmc8u6iYRQj10va9cZyo";
	private static final String TWITTER_OAUTH_CALLBACK = "http://popacross.com/auth/callback/twitter";
	private static final String TWITTER_VERIFY_URL = "http://api.twitter.com/1/account/verify_credentials.json";

	private static final String FACEBOOK_API_KEY = "194068833990049";
	private static final String FACEBOOK_API_SECRET = "70e3f6014eb8d078a1181589ed02b6d4";
	private static final String FACEBOOK_OAUTH_CALLBACK = "http://popacross.com/auth/callback/facebook";
	private static final String FACEBOOK_VERIFY_URL = "https://graph.facebook.com/me";
	
	private class IdentifierInfo {
		private Token token = null;
		private String user = null;
		private String name = null;
		private String url = null;
		private String avatar = null;
		private IdentifierInfo(Token token, String user, String name, String url, String photo) {
			this.token = token;
			this.user = user;
			this.name = name;
			this.url = url;
			this.avatar = photo;
		}
	}
	
	private UserManager userManager = null;
	private UserTokenManager tokenManager = null;
	private IdentityManager identityManager = null;
	private UserLogic userLogic = null;
	
	private OAuthService twitterService = null;
	private OAuthService facebookService = null;
	
	@Override
	public void init() {
		this.userManager = this.serviceManager.getService(EntityService.class).getManager(User.class, UserManager.class);
		this.tokenManager = this.serviceManager.getService(EntityService.class).getManager(UserToken.class, UserTokenManager.class);
		this.identityManager = this.serviceManager.getService(EntityService.class).getManager(Identity.class, IdentityManager.class);
		this.userLogic  = this.logicService.get(UserLogic.class);

		try {
			this.twitterService = new ServiceBuilder().provider(TwitterApi.class).apiKey(TWITTER_API_KEY).apiSecret(TWITTER_API_SECRET).callback(TWITTER_OAUTH_CALLBACK).build();
			this.facebookService = new ServiceBuilder().provider(FacebookApi.class).apiKey(FACEBOOK_API_KEY).apiSecret(FACEBOOK_API_SECRET).callback(FACEBOOK_OAUTH_CALLBACK).build();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public Entity<User> register(String email, String password) {
		Entity<User> user = this.userLogic.getByEmail(email);
		
		if (user == null) {
			user = this.userLogic.create(email);
			user.set(User.password, password);
			this.userLogic.save(user);
		}
		
		return user;
	}

	public Entity<User> login(String email, String password) {
		Entity<User> user = this.userLogic.getByEmail(email);
		
		if (user != null) {
			String userPassword = user.get(User.password);
			if (userPassword != null && password == null || !password.equals(userPassword)) {
				user = null;
			}
		}
		
		return user;
	}
	
	public boolean authenticate(AuthenticationLevel level, Entity<User> user) {
		if (level == AuthenticationLevel.PUBLIC) {
			return true;
		}
		
		if (level == AuthenticationLevel.USER && user != null) {
			return true;
		}
		
		if (level == AuthenticationLevel.ADMIN && user != null) {
			Boolean isAdmin = user.get(User.isAdmin);
			if (isAdmin != null && isAdmin.booleanValue()) {
				return true;
			}
		}
		
		return false;
	}

	public Entity<User> fromToken(String key, String token) {
		if (key == null || token == null) return null;
		
		Entity<User> user = null;
		Entity<UserToken> userToken = this.tokenManager.get(token);
		
		if (userToken != null && userToken.get(UserToken.key).equals(key)) {
			user = userToken.get(UserToken.user);
		}
		
		return user;
	}

	@Override
	public <T extends ViewType> Entity<User> getUserFromViewRequest(ViewRequest<T, User> req) {
		if (req.getUser() != null) {
			return req.getUser();
		}
		
		String authHash = req.getParameter("hash");
		List<Entity<UserToken>> token = this.tokenManager.query(UserToken.hash, authHash);
		
		if (token.size() == 1 && token.get(0) != null && token.get(0).getKey() != null) {
			Entity<User> user = token.get(0).get(UserToken.user);
			if (user.getKey().equals("0")) {
				return null;
			}
			
			return user;
		}
		
		if (req.request != null && req.request.getCookies() != null) {
			for (Cookie cookie: req.request.getCookies()) {
				if (cookie.getName().equals(String.format("fbs_%s", AuthenticationLogic.FACEBOOK_API_KEY))) {
					String accessTokenString = null;
					String secretString = null;
					
					String[] params = cookie.getValue().split("&");
					for (String param: params) {
						String[] parts = param.split("=");
						if (parts[0].equals("access_token")) {
							accessTokenString = parts[1];
						} else if (parts[0].equals("secret")) {
							secretString = parts[1];
						}
					}
					
					if (accessTokenString != null && secretString != null) {
						Token accessToken = new Token(accessTokenString, secretString);
					    OAuthRequest request = new OAuthRequest(Verb.GET, FACEBOOK_VERIFY_URL);
					    this.facebookService.signRequest(accessToken, request);
					    Response response = request.send();
					    JSONObject json = JSONObject.fromObject(response.getBody());
					    if (!json.containsKey("error")) {
					    	IdentifierInfo identInfo = new IdentifierInfo(accessToken, json.getString("id"), json.getString("name"), json.getString("link"), String.format("https://graph.facebook.com/%s/picture", json.getString("id")));
					    	return this.getOrCreateUserWithIdentifierInfo(User.facebookIdentity, Identity.IdentityType.FACEBOOK, identInfo, true); 
					    }
					}
				}
			}
		}
		
		return null;
	}
	
	public String getTwitterAuthURL() { 
		return this.twitterService.getAuthorizationUrl(this.twitterService.getRequestToken());
	}
	
	public IdentifierInfo processTwitterVerifier(String token, String verifier) {
	    Token accessToken = this.twitterService.getAccessToken(new Token(token, TWITTER_API_SECRET), new Verifier(verifier));
	    if (accessToken == null || accessToken.getToken() == null || accessToken.getSecret() == null) {
	    	return null;
	    }
	    
	    OAuthRequest request = new OAuthRequest(Verb.GET, TWITTER_VERIFY_URL);
	    this.twitterService.signRequest(accessToken, request);
	    Response response = request.send();
	    JSONObject json = JSONObject.fromObject(response.getBody());
	    if (json.containsKey("error")) {
	    	return null;
	    }
	    
	    return new IdentifierInfo(accessToken, json.getString("screen_name"), json.getString("name"), String.format("http://twitter.com/%s", json.getString("screen_name")), json.getString("profile_image_url"));
	}
	
	public Entity<User> getOrCreateWithTwitterVerifier(String token, String verifier) {
		IdentifierInfo identInfo = this.processTwitterVerifier(token, verifier);
	    if (identInfo == null) {
	    	return null;
	    }
	    
	    return this.getOrCreateUserWithIdentifierInfo(User.twitterIdentity, Identity.IdentityType.TWITTER, identInfo, true);
	}

	public boolean updateTwitterVerifier(Entity<User> user, String token, String verifier) {
		IdentifierInfo identInfo = this.processTwitterVerifier(token, verifier);
	    if (identInfo == null) {
	    	return false;
	    }
	    
	    Entity<User> existingUser = this.getOrCreateUserWithIdentifierInfo(User.twitterIdentity, Identity.IdentityType.TWITTER, identInfo, false);
	    if (existingUser != null && !this.removeTwitter(existingUser)) {
    		return false;
    	}
	    
    	Entity<Identity> identity = this.getOrCreateIdentityWithIdentifierInfo(Identity.IdentityType.TWITTER, identInfo, true);
	    user.set(User.twitterIdentity, identity);
	    return this.userManager.save(user);
	}
	
	public boolean removeTwitter(Entity<User> user) {
	    user.set(User.twitterIdentity, null);
    	return this.userManager.save(user);
	}

	public String getFacebookAuthURL() { 
		return this.facebookService.getAuthorizationUrl(null);
	}
	
	public IdentifierInfo processFacebookVerifier(String verifier) {
	    Token accessToken = this.facebookService.getAccessToken(null, new Verifier(verifier));
	    if (accessToken == null || accessToken.getToken() == null || accessToken.getSecret() == null) {
	    	return null;
	    }
	    
	    OAuthRequest request = new OAuthRequest(Verb.GET, FACEBOOK_VERIFY_URL);
	    this.facebookService.signRequest(accessToken, request);
	    Response response = request.send();
	    JSONObject json = JSONObject.fromObject(response.getBody());
	    if (json.containsKey("error")) {
	    	return null;
	    }
	    
	    return new IdentifierInfo(accessToken, json.getString("id"), json.getString("name"), json.getString("link"), String.format("https://graph.facebook.com/%s/picture", json.getString("id")));
	}
	
	public Entity<User> getOrCreateWithFacebookVerifier(String verifier) {
		IdentifierInfo identInfo = this.processFacebookVerifier(verifier);
	    if (identInfo == null) {
	    	return null;
	    }
	    
	    return this.getOrCreateUserWithIdentifierInfo(User.facebookIdentity, Identity.IdentityType.FACEBOOK, identInfo, true);
	}
	
	public boolean updateFacebookVerifier(Entity<User> user, String verifier) {
		IdentifierInfo identInfo = this.processFacebookVerifier(verifier);
	    if (identInfo == null) {
	    	return false;
	    }
	    
	    Entity<User> existingUser = this.getOrCreateUserWithIdentifierInfo(User.facebookIdentity, Identity.IdentityType.FACEBOOK, identInfo, false);
    	if (existingUser != null && !this.removeFacebook(existingUser)) {
    		return false;
    	}
	    
    	Entity<Identity> identity = this.getOrCreateIdentityWithIdentifierInfo(Identity.IdentityType.FACEBOOK, identInfo, true);
	    user.set(User.facebookIdentity, identity);
	    return this.userManager.save(user);
	}

	public boolean removeFacebook(Entity<User> user) {
		user.set(User.facebookIdentity, null);
    	return this.userManager.save(user);
	}

	/*
	private Entity<User> getOrCreateWithIdentifierInfo(IdentifierParams identParams, IdentifierInfo identInfo, boolean create) {
		List<Entity<User>> existing = this.userManager.query(identParams.userField, identInfo.user);
	    if (existing.size() == 0 && create) {
	    	Entity<User> user = this.userManager.create();
		    
		    user.set(identParams.userField, identInfo.user);
		    user.set(identParams.tokenField, identInfo.token.getToken());
		    user.set(identParams.secretField, identInfo.token.getSecret());
		    user.set(identParams.nameField, identInfo.name);
		    user.set(identParams.urlField, identInfo.url);
		    user.set(User.avatarImg, identInfo.photo);
		    user.set(User.name, identInfo.name);
		    
		    if (this.userManager.save(user)) {
		    	return user;
		    } else {
		    	return this.getOrCreateWithIdentifierInfo(identParams, identInfo, create);
		    }
	    } else if (existing.size() > 0) {
	    	return existing.get(0);
	    }
	    
	    return null;
	}
	*/
	
	private Entity<User> getOrCreateUserWithIdentifierInfo(FieldKey<Entity<Identity>> identField, Identity.IdentityType identType, IdentifierInfo identInfo, boolean create) {
		Entity<Identity> identity = this.getOrCreateIdentityWithIdentifierInfo(identType, identInfo, create);
		if (identity == null) {
			return null;
		}
		
		List<Entity<User>> existing = this.userManager.query(identField, identity);
		if (existing.size() == 0 && create) {
			Entity<User> user = this.userManager.create();
			user.set(identField, identity);
		    if (this.userManager.save(user)) {
		    	return user;
		    } else {
		    	return this.getOrCreateUserWithIdentifierInfo(identField, identType, identInfo, create);
		    }			
		} else if (existing.size() > 0) {
			return existing.get(0);
		}
		
		return null;
	}
	
	private Entity<Identity> getOrCreateIdentityWithIdentifierInfo(Identity.IdentityType identType, IdentifierInfo identInfo, boolean create) {
		List<Entity<Identity>> existing = this.identityManager.query(new SQLFieldPredicate<Identity, Identity.IdentityType>(Identity.type, identType).and(Identity.user, identInfo.user));
	    if (existing.size() == 0 && create) {
	    	Entity<Identity> identity = this.identityManager.create();
		    
	    	identity.set(Identity.type, identType);
	    	identity.set(Identity.user, identInfo.user);
	    	identity.set(Identity.token, identInfo.token.getToken());
	    	identity.set(Identity.secret, identInfo.token.getSecret());
	    	identity.set(Identity.name, identInfo.name);
	    	identity.set(Identity.url, identInfo.url);
	    	identity.set(Identity.avatar, identInfo.avatar);
		    
		    if (this.identityManager.save(identity)) {
		    	return identity;
		    } else {
		    	return this.getOrCreateIdentityWithIdentifierInfo(identType, identInfo, create);
		    }
	    } else if (existing.size() > 0) {
	    	return existing.get(0);
	    }
	    
	    return null;
	}
	
}
