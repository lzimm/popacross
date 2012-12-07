package com.qorporation.popacross.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qorporation.popacross.entity.definition.Comment;
import com.qorporation.popacross.entity.definition.Identity;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.entity.manager.CommentManager;
import com.qorporation.qluster.conn.ConnectionService;
import com.qorporation.qluster.conn.cache.CacheConnection;
import com.qorporation.qluster.conn.cache.CacheConnectionPool;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLOrderPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityHelper;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.util.Serialization;

public class CommentLogic extends LogicController {

	private CommentManager commentManager = null;
    private CacheConnectionPool cacheConnectionPool = null;
    
	@Override
	public void init() {
		this.commentManager = this.serviceManager.getService(EntityService.class).getManager(Comment.class, CommentManager.class);
		this.cacheConnectionPool = this.serviceManager.getService(ConnectionService.class).getPool(CacheConnection.class, CacheConnectionPool.class);
	}
	
	public List<Entity<Comment>> getComments(String key) {
		return this.commentManager.query(new SQLFieldPredicate<Comment, String>(Comment.key, key)
				.and(new SQLFieldPredicate<Comment, Boolean>(Comment.deleted, false))
				.order(Comment.rank, SQLOrderPredicate.Order.DESC));
	}
	
	public List<Map<String, Object>> getCommentMaps(String key) {
		List<FieldKey<?>> filtered = new ArrayList<FieldKey<?>>(1);
		filtered.add(Comment.user);
		
		List<EntityHelper.RecursiveFieldKeySet<?>> recursive = new ArrayList<EntityHelper.RecursiveFieldKeySet<?>>(2);
		recursive.add(new EntityHelper.RecursiveFieldKeySet<Entity<Identity>>(Comment.twitterIdentity, Identity.secret, Identity.token, Identity.user));
		recursive.add(new EntityHelper.RecursiveFieldKeySet<Entity<Identity>>(Comment.facebookIdentity, Identity.secret, Identity.token, Identity.user));

		return this.commentManager.getHelper().getListOfMaps(this.getComments(key), filtered, recursive);
	}
	
	public boolean removeComment(String key, String id, Entity<User> user) {
		Entity<Comment> comment = this.commentManager.get(id);
		if (comment == null || comment.getKey().length() == 0) {
			return false;
		}
		
		if (!comment.getKey().equals(key)) {
			return false;
		}
		
		Entity<User> commentOwner = comment.get(Comment.user);
		boolean hasCommentOwner = commentOwner != null && !commentOwner.getKey().equals("0");
		boolean isCommentOwner = commentOwner.getKey().equals(user.getKey());
		
		if (!(isCommentOwner && hasCommentOwner)) {
			return false;
		}
		
		comment.set(Comment.deleted, true);
		if (!this.commentManager.save(comment)) {
			return false;
		}
		
		return true;
	}
	
	public Entity<Comment> addComment(String key, Entity<User> user, String body, String twitter, String facebook) {
		Entity<Comment> comment = this.commentManager.create();
		comment.set(Comment.key, key);
		comment.set(Comment.rank, System.currentTimeMillis() * 1.f);
		comment.set(Comment.comment, body);
		
		if (user != null && !user.getKey().equals("0")) {
			comment.set(Comment.user, user);
			
			if (user.get(User.facebookIdentity) != null && !user.get(User.facebookIdentity).getKey().equals("0")) {
				comment.set(Comment.facebookIdentity, user.get(User.facebookIdentity));
			}

			if (user.get(User.twitterIdentity) != null && !user.get(User.twitterIdentity).getKey().equals("0")) {
				comment.set(Comment.twitterIdentity, user.get(User.twitterIdentity));
			}
		}
		
		if (!this.commentManager.save(comment)) {
			return null;
		}
		
		return comment;
	}
	
	public static enum CacheTag {
		RENDERED_COMMENT;
		public byte[] getTagBytes() { return Serialization.serialize(this.name()); }
	}
	
	public boolean cacheRenderedComment(String comment, CacheTag cacheTag, byte[] value){ return this.cacheConnectionPool.acquire().set(CommentLogic.class.getName(), this.composeCommentCacheKey(comment, cacheTag), value); }
	public byte[] getRenderedComment(String comment, CacheTag cacheTag) { return this.cacheConnectionPool.acquire().get(CommentLogic.class.getName(), this.composeCommentCacheKey(comment, cacheTag)); }	
	public boolean clearRenderedComment(String comment) {
		boolean ret = true;
		for (CacheTag tag: CacheTag.values()) {
			ret = ret && this.cacheConnectionPool.acquire().clear(CommentLogic.class.getName(), this.composeCommentCacheKey(comment, tag));
		}
		return ret;
	}

	private String composeCommentCacheKey(String comment, CacheTag cacheTag) {
		return new StringBuilder(cacheTag.name()).append(':').append(comment).toString();
	}
	
}
