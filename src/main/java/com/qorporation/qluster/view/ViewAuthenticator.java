package com.qorporation.qluster.view;

import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;

public interface ViewAuthenticator<U extends Definition<? extends Connection>> {
	
	public abstract boolean authenticate(AuthenticationLevel level, Entity<U> user);
	public abstract Entity<U> fromToken(String key, String token);
	public abstract <T extends ViewType> Entity<U> getUserFromViewRequest(ViewRequest<T, U> req);

}
