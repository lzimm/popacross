package com.qorporation.popacross.page.handler;

import java.util.List;

import com.qorporation.popacross.entity.definition.Comment;
import com.qorporation.popacross.logic.CommentLogic;
import com.qorporation.popacross.logic.UserLogic;
import com.qorporation.popacross.page.PageHandler;
import com.qorporation.popacross.page.PageRequest;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.ErrorControl;

public class HomeHandler extends PageHandler {
	
	private UserLogic userLogic = null;
	private CommentLogic commentLogic = null;
	
	@Override
	public void init() {
		this.userLogic = this.logicService.get(UserLogic.class);
		this.commentLogic = this.logicService.get(CommentLogic.class);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/"})
	public void index(PageRequest request) {
		List<Entity<Comment>> comments = this.commentLogic.getComments("home");
		request.addRenderVar("comments", comments);
		request.renderTemplate("home/index.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/pop"})
	public void pop(PageRequest request) {
		request.renderTemplate("home/pop.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/beta"})
	public void beta(PageRequest request) {
		String email = request.getParameter("email", "");
		if (email.length() > 0) {
			this.userLogic.addBetaEmail(email.toLowerCase());
		}
		
		request.renderTemplate("home/beta.html");
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/redeploy"})
	public void redeploy(PageRequest request) {
		try {
			Process svnProcess = Runtime.getRuntime().exec("svn update");
			this.logger.info("Subversion return code: " + svnProcess.waitFor());
			
			Process mvnProcess = Runtime.getRuntime().exec("mvn clean compile");
			this.logger.info("Maven return code: " + mvnProcess.waitFor());
			
			Runtime.getRuntime().exec("./server.sh restart &");
			this.logger.info("Restarting...");
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		request.renderTemplate("home/index.html");
	}
	
}
