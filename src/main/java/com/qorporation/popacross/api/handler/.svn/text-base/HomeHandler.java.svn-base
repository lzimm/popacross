package com.qorporation.popacross.api.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qorporation.popacross.api.APIHandler;
import com.qorporation.popacross.api.APIRequest;
import com.qorporation.popacross.entity.definition.Comment;
import com.qorporation.popacross.logic.CommentLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.Serialization;

public class HomeHandler extends APIHandler {

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/home/comment/remove/(?<key>[A-Za-z0-9_\\-]*)"})
	public void removeComment(APIRequest request) {
		String comment = request.getParameter("comment");
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("key", request.getParameter("key"));
		res.put("result", this.logicService.get(CommentLogic.class).removeComment(request.getParameter("key"), comment, request.getUser()));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/home/comment/add/(?<key>[A-Za-z0-9_\\-]*)"})
	public void addComment(APIRequest request) {
		String comment = request.getParameter("comment");
		String twitter = request.getParameter("twitter", "");
		String facebook = request.getParameter("facebook", "");
		
		Map<String, Object> res = new HashMap<String, Object>();
		Entity<Comment> pageComment = this.logicService.get(CommentLogic.class).addComment(request.getParameter("key"), request.getUser(), comment, twitter, facebook);
		List<Entity<Comment>> comments = this.logicService.get(CommentLogic.class).getComments(request.getParameter("key"));
		Map<String, String> commentStrings = new HashMap<String, String>();
		for (Entity<Comment> c: comments) {
			byte[] cachedVal = this.logicService.get(CommentLogic.class).getRenderedComment(c.getKey(), CommentLogic.CacheTag.RENDERED_COMMENT);
			if (cachedVal != null) {
				commentStrings.put(c.getKey(), Serialization.deserializeString(cachedVal));
			} else {
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("comment", c);
				String renderedVal = request.renderBlock("/blocks/comment.html", vars);
				commentStrings.put(c.getKey(), renderedVal);
				this.logicService.get(CommentLogic.class).cacheRenderedComment(c.getKey(), CommentLogic.CacheTag.RENDERED_COMMENT, Serialization.serialize(renderedVal));
			}
		}
		
		res.put("key", request.getParameter("key"));		
		res.put("comment", pageComment.getKey());
		res.put("comments", commentStrings);
		
		request.sendResponse(res);
	}
	
}
