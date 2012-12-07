package com.qorporation.qluster.view.undefined;

import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewRequest;
import com.qorporation.qluster.view.ViewType;

public class UndefinedUserAuthenticator implements ViewAuthenticator<UndefinedUser> {

	@Override
	public boolean authenticate(AuthenticationLevel level,
			Entity<com.qorporation.qluster.view.undefined.UndefinedUser> user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Entity<com.qorporation.qluster.view.undefined.UndefinedUser> fromToken(
			String key, String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ViewType> Entity<UndefinedUser> getUserFromViewRequest(
			ViewRequest<T, UndefinedUser> req) {
		// TODO Auto-generated method stub
		return null;
	}

}
