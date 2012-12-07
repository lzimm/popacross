package com.qorporation.popacross.page.handler;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemComment;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.entity.definition.ItemProperty;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.popacross.page.PageHandler;
import com.qorporation.popacross.page.PageRequest;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.common.Quad;
import com.qorporation.qluster.entity.Entity;

public class ItemHandler extends PageHandler {

	private ItemLogic itemLogic = null;
	
	@Override
	public void init() {
		this.itemLogic = this.logicService.get(ItemLogic.class);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/v/(?<item>[A-Za-z0-9_\\-]*)"})
	public void view(PageRequest request) {
		byte[] cachedVal = this.itemLogic.getRenderedItemDisplay(request.getParameter("item"), ItemLogic.CacheTag.RENDERED_PAGE);
		if (cachedVal != null) {
			//request.renderCached(cachedVal);
			//return;
		}
		
		Quad<Entity<Item>, List<Entity<ItemPhoto>>, List<Entity<ItemProperty>>, List<Entity<ItemComment>>> itemInfo = this.itemLogic.find(request.getParameter("item"));
		
		if (itemInfo != null) {
			request.addRenderVar("item", itemInfo.a());
			request.addRenderVar("posted", new Date(itemInfo.a().get(Item.created).getTime()));
			request.addRenderVar("facebookIdentity", itemInfo.a().get(Item.facebookIdentity));
			request.addRenderVar("twitterIdentity", itemInfo.a().get(Item.twitterIdentity));
			request.addRenderVar("itemPhotos", itemInfo.b());
			request.addRenderVar("backgrounds", ItemLogic.ItemPhotoType.BACKGROUND.getPairMapper().map(itemInfo.b()));
			request.addRenderVar("photos", ItemLogic.ItemPhotoType.PHOTO.getPairMapper().map(itemInfo.b()));
			request.addRenderVar("bigthumbs", ItemLogic.ItemPhotoType.BIGTHUMB.getPairMapper().map(itemInfo.b()));
			request.addRenderVar("smallthumbs", ItemLogic.ItemPhotoType.SMALLTHUMB.getPairMapper().map(itemInfo.b()));
			request.addRenderVar("scaled", ItemLogic.ItemPhotoType.SCALED.getPairMapper().map(itemInfo.b()));
			request.addRenderVar("properties", itemInfo.c());
			request.addRenderVar("comments",itemInfo.d());
			
			byte[] renderedVal = request.renderTemplateAndGetBytes("item/view.html");
			this.itemLogic.cacheRenderedItemDisplay(request.getParameter("item"), ItemLogic.CacheTag.RENDERED_PAGE, renderedVal);
		} else {
			request.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
}
