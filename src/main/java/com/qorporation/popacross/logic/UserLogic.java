package com.qorporation.popacross.logic;

import java.util.List;

import com.qorporation.popacross.entity.definition.BetaUser;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.entity.manager.BetaUserManager;
import com.qorporation.popacross.entity.manager.UserManager;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicController;

public class UserLogic extends LogicController {
	
	private UserManager userManager = null;
	private BetaUserManager betaManager = null;
	
	@Override
	public void init() {
		this.userManager = this.serviceManager.getService(EntityService.class).getManager(User.class, UserManager.class);
		this.betaManager = this.serviceManager.getService(EntityService.class).getManager(BetaUser.class, BetaUserManager.class);
	}
	
	public Entity<User> create(String email) {
		Entity<User> user = this.userManager.create();
		user.set(User.email, email);
		
		if (this.userManager.save(user)) {
			return user;
		} else {
			return null;
		}
	}
	
	public void delete(String key) {
		this.userManager.delete(key);
	}
	
	public void save(Entity<User> user) {
		this.userManager.save(user);
	}
	
	public Entity<User> get(String key) {
		return this.userManager.get(key);
	}
	
	public Entity<User> getByEmail(String email) {
		List<Entity<User>> found = this.userManager.query(User.email, email);
		if (found.size() > 0) {
			return found.get(0);
		} else {
			return null;
		}
	}
	
	public Entity<BetaUser> addBetaEmail(String email) {
		Entity<BetaUser> betaUser = this.betaManager.create();
		betaUser.set(BetaUser.email, email);
		if (!this.betaManager.save(betaUser)) {
			return null;
		}
		
		return betaUser;
	}
	
}