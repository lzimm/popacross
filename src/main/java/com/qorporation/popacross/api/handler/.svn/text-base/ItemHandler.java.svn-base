package com.qorporation.popacross.api.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.getopt.util.hash.MurmurHash;

import com.qorporation.popacross.api.APIHandler;
import com.qorporation.popacross.api.APIRequest;
import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemComment;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.qluster.annotation.AuthenticationPolicy;
import com.qorporation.qluster.annotation.Routing;
import com.qorporation.qluster.annotation.AuthenticationPolicy.AuthenticationLevel;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.common.TripleZipIterator;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class ItemHandler extends APIHandler {
	private static final File TMP_UPLOAD_PATH = new File("media/usr/image_uploads");
	
	@Override
	public void init() {
		if (!ItemHandler.TMP_UPLOAD_PATH.exists()) {
			ItemHandler.TMP_UPLOAD_PATH.mkdir();
		}
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/comment/list/(?<item>[A-Za-z0-9_\\-]*)"})
	public void getComments(APIRequest request) {
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
				
		Map<String, Object> res = new HashMap<String, Object>();
		List<Entity<ItemComment>> comments = this.logicService.get(ItemLogic.class).getComments(item);
		Map<String, String> commentStrings = new HashMap<String, String>();
		for (Entity<ItemComment> c: comments) {
			byte[] cachedVal = this.logicService.get(ItemLogic.class).getRenderedComment(c, ItemLogic.CacheTag.RENDERED_COMMENT);
			if (cachedVal != null) {
				commentStrings.put(c.getKey(), Serialization.deserializeString(cachedVal));
			} else {
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("comment", c);
				String renderedVal = request.renderBlock("/blocks/comment.html", vars);
				commentStrings.put(c.getKey(), renderedVal);
				this.logicService.get(ItemLogic.class).cacheRenderedComment(c, ItemLogic.CacheTag.RENDERED_COMMENT, Serialization.serialize(renderedVal));
			}
		}
		
		res.put("item", item.get(Item.token));		
		res.put("comments", commentStrings);
		
		request.sendResponse(res);
	}

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/comment/remove/(?<item>[A-Za-z0-9_\\-]*)"})
	public void removeComment(APIRequest request) {
		String comment = request.getParameter("comment");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).removeComment(item, comment, request.getUser()));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/comment/add/(?<type>twitter|facebook|anonymous)/(?<item>[A-Za-z0-9_\\-]*)"})
	public void addComment(APIRequest request) {
		String type = request.getParameter("type");
		String comment = request.getParameter("comment");
		String twitter = request.getParameter("twitter", "");
		String facebook = request.getParameter("facebook", "");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
				
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		
		if (comment.trim().length() > 0) {
			Entity<ItemComment> itemComment = this.logicService.get(ItemLogic.class).addComment(item, request.getUser(), comment, type.equals("twitter"), twitter, type.equals("facebook"), facebook);
			res.put("comment", itemComment.getKey());
		}
		
		List<Entity<ItemComment>> comments = this.logicService.get(ItemLogic.class).getComments(item);
		Map<String, String> commentStrings = new HashMap<String, String>();
		for (Entity<ItemComment> c: comments) {
			byte[] cachedVal = this.logicService.get(ItemLogic.class).getRenderedComment(c, ItemLogic.CacheTag.RENDERED_COMMENT);
			if (cachedVal != null) {
				commentStrings.put(c.getKey(), Serialization.deserializeString(cachedVal));
			} else {
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("comment", c);
				String renderedVal = request.renderBlock("/blocks/comment.html", vars);
				commentStrings.put(c.getKey(), renderedVal);
				this.logicService.get(ItemLogic.class).cacheRenderedComment(c, ItemLogic.CacheTag.RENDERED_COMMENT, Serialization.serialize(renderedVal));
			}
		}
			
		res.put("comments", commentStrings);
		
		request.sendResponse(res);
	}

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/list/(?<item>[A-Za-z0-9_\\-]*)"})
	public void getProperties(APIRequest request) {		
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).getPropertyMap(item));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/namespace/(?<item>[A-Za-z0-9_\\-]*)"})
	public void getPropertyNamespace(APIRequest request) {
		String namespace = request.getParameter("namespace");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).getPropertyMap(item, namespace));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/get/(?<item>[A-Za-z0-9_\\-]*)"})
	public void getProperty(APIRequest request) {
		String namespace = request.getParameter("namespace");
		String property = request.getParameter("property");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).get(request.getParameter("item"));
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).getProperty(item, namespace, property));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/remove/(?<item>[A-Za-z0-9_\\-]*)"})
	public void removeProperty(APIRequest request) {
		String namespace = request.getParameter("namespace");
		String property = request.getParameter("property");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).removeProperty(item, namespace, property));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/update/(?<item>[A-Za-z0-9_\\-]*)"})
	public void updateProperty(APIRequest request) {
		String namespace = request.getParameter("namespace");
		String property = request.getParameter("property");
		String expect = request.getParameter("expect");
		String update = request.getParameter("update");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).updateProperty(item, namespace, property, expect, update));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/property/add/(?<item>[A-Za-z0-9_\\-]*)"})
	public void addProperty(APIRequest request) {
		String namespace = request.getParameter("namespace");
		String property = request.getParameter("property");
		String value = request.getParameter("value");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).addProperty(item, namespace, property, value));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/photo/add/(?<item>[A-Za-z0-9_\\-]*)"})
	public void addPhoto(APIRequest request) {
		String caption = request.getParameter("caption");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;				
		}
		
		File uploadDir = new File(TMP_UPLOAD_PATH + File.separator + item.get(Item.token));
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		
		Map<String, FileItem> files = request.getUploadedFiles();
		for (Entry<String, FileItem> f: files.entrySet()) {
			FileItem file = f.getValue();
			String extension = '.' + file.getName().substring(file.getName().lastIndexOf('.'));
			
			for (int attempt = 0; attempt < 50; attempt++) {
				String fileName = Integer.toHexString(MurmurHash.hash(file.getName().getBytes(), attempt)) + Integer.toHexString(MurmurHash.hash(file.getName().getBytes(), (int) file.getSize()));
				File filePath = new File(uploadDir + File.separator + fileName + extension);
				
				if (!filePath.exists()) {
					try {
						file.write(filePath);
						
						if (filePath.exists()) {
							Entity<ItemPhoto> itemPhoto = this.logicService.get(ItemLogic.class).addPhoto(item, filePath, caption, true);
							res.put("key", itemPhoto.getKey());
							res.put("path", filePath.getPath());
							res.put("width", itemPhoto.get(ItemPhoto.width));
							res.put("height", itemPhoto.get(ItemPhoto.height));
						}
						
						break;
					} catch (Exception e) {
						ErrorControl.logException(e);
						request.sendError(HttpServletResponse.SC_BAD_REQUEST);
					}
				}
			}
		}
		
		request.sendResponse(res);
	}

	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/photo/update/(?<item>[A-Za-z0-9_\\-]+)"})
	public void updatePhoto(APIRequest request) {
		String photo = request.getParameter("photo");
		String caption = request.getParameter("caption");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).updatePhoto(item, photo, caption));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/photo/remove/(?<item>[A-Za-z0-9_\\-]+)"})
	public void removePhoto(APIRequest request) {
		String photo = request.getParameter("photo");
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("result", this.logicService.get(ItemLogic.class).removePhoto(item, photo));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/update/(?<item>[A-Za-z0-9_\\-]*)"})
	public void update(APIRequest request) {
		String label = request.getParameter("label");
		String description = request.getParameter("description");
		String price = request.getParameter("price", "0");
		String location = request.getParameter("location");
		String lat = request.getParameter("lat", "0");
		String lng = request.getParameter("lng", "0");
		String startTime = request.getParameter("startTime", "0");
		String endTime = request.getParameter("endTime", "0");
		GeoPoint geo = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lng));
		Map<String, String> captions = request.getPrefixedParams("caption_", false);
		
		this.logger.info(String.format("updating item %s at location %s (%s)", request.getParameter("item"), location, geo.geohash()));
		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("item", item.get(Item.token));
		res.put("link", String.format(Item.DIRECT_LINK_FORMAT, item.get(Item.token)));
		res.put("result", this.logicService.get(ItemLogic.class).update(item, label, description, price, location, geo, Long.parseLong(startTime), Long.parseLong(endTime), captions));
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/info/(?<item>[A-Za-z0-9_\\-]*)"})
	public void info(APIRequest request) {		
		Entity<Item> item = this.logicService.get(ItemLogic.class).resume(request.getParameter("item"), request.getUser());
		if (item == null) {
			request.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("result", item.getKey());

		Map<String, Object> photoSet = new HashMap<String, Object>();
		Triple<List<Entity<ItemPhoto>>, List<String>, List<String>> entries = this.logicService.get(ItemLogic.class).getProcessedThumbsAndURLs(item);
		for (Triple<Entity<ItemPhoto>, String, String> e: new TripleZipIterator<Entity<ItemPhoto>, String, String>(entries.a(), entries.b(), entries.c())) {
			photoSet.put(e.a().getKey(), new String[]{ e.b(), e.c() });
		}
		
		res.put("photos", photoSet);
		
		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/search"})
	public void search(APIRequest request) {
		String query = request.getParameter("q");
		String lat = request.getParameter("lat", "0");
		String lng = request.getParameter("lng", "0");
		GeoPoint geo = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lng));
		
		this.logger.info(String.format("Searching for %s at %s (%s, %s)", query, geo.geohash(), lat, lng));
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("result", query);

		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		List<Entity<Item>> results = this.logicService.get(ItemLogic.class).search(geo, query);
		if (results != null) {
			Map<String, Entity<ItemPhoto>> photos = this.logicService.get(ItemLogic.class).getPreviewPhotos(results);
			for (Entity<Item> item: results) {
				Map<String, Object> itemData = new HashMap<String, Object>();
				itemData.put("id", item.getKey());
				itemData.put("token", item.get(Item.token));
				itemData.put("label", item.get(Item.label));
				itemData.put("lat", item.get(Item.position).getLat());
				itemData.put("lng", item.get(Item.position).getLng());
				itemData.put("price", item.get(Item.price));
				itemData.put("link", String.format(Item.DIRECT_LINK_FORMAT, item.get(Item.token)));
				itemData.put("posted", new Date(item.get(Item.startTime).getTime()));
				
				if (photos.containsKey(item.getKey())) {
					itemData.put("photo", ItemLogic.ItemPhotoType.SMALLTHUMB.getMapper().f(photos.get(item.getKey())));
				} else {
					itemData.put("photo", "");
				}
				
				items.add(itemData);
			}
		}
		res.put("items", items);

		request.sendResponse(res);
	}
	
	@AuthenticationPolicy(level=AuthenticationLevel.PUBLIC)
	@Routing(patterns={"/item/search/suggest"})
	public void suggest(APIRequest request) {
		String query = request.getParameter("q");
		String lat = request.getParameter("lat", "0");
		String lon = request.getParameter("lon", "0");
		GeoPoint geo = new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon));
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("result", query);

		List<String> results = this.logicService.get(ItemLogic.class).suggest(geo, query);
		res.put("suggestions", results);
		
		request.sendResponse(res);
	}
	
}