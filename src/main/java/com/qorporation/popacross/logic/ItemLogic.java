package com.qorporation.popacross.logic;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.qorporation.popacross.entity.definition.Identity;
import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemComment;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.entity.definition.ItemProperty;
import com.qorporation.popacross.entity.definition.ItemTag;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.popacross.entity.manager.ItemCommentManager;
import com.qorporation.popacross.entity.manager.ItemManager;
import com.qorporation.popacross.entity.manager.ItemPhotoManager;
import com.qorporation.popacross.entity.manager.ItemPropertyManager;
import com.qorporation.popacross.entity.manager.ItemTagManager;
import com.qorporation.popacross.logic.tasks.DeployPhotoTask;
import com.qorporation.popacross.logic.tasks.ProcessBackgroundTask;
import com.qorporation.popacross.logic.tasks.ProcessPhotoTask;
import com.qorporation.popacross.logic.tasks.ProcessScaledTask;
import com.qorporation.popacross.logic.tasks.ProcessThumbnailTask;
import com.qorporation.popacross.search.SearchService;
import com.qorporation.qluster.async.AsyncQueue;
import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.common.Pair;
import com.qorporation.qluster.common.Quad;
import com.qorporation.qluster.common.Triple;
import com.qorporation.qluster.common.functional.F1;
import com.qorporation.qluster.common.functional.Mappable;
import com.qorporation.qluster.conn.ConnectionService;
import com.qorporation.qluster.conn.cache.CacheConnection;
import com.qorporation.qluster.conn.cache.CacheConnectionPool;
import com.qorporation.qluster.conn.sql.operation.params.SQLValue;
import com.qorporation.qluster.conn.sql.operation.params.SQLValueList;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLEntityPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLFieldPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLInPredicate;
import com.qorporation.qluster.conn.sql.operation.predicate.SQLOrderPredicate;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityHelper;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.util.BaseConversion;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class ItemLogic extends LogicController {
	public static final Point BACKGROUND_SIZE = new Point(1280, 1024);
	public static final Point SCALED_SIZE = new Point(900, 900);
	public static final Point THUMB_SIZE_SMALL = new Point(40, 40);
	public static final Point THUMB_SIZE_LARGE = new Point(400, 400);
	
	public enum ItemPhotoType {
		BACKGROUND(String.format("proc.bg."), new F1<ItemPhotoType, Void>() { public Void f(ItemPhotoType a) { a.stateField = ItemPhoto.bgState; return null; }}, "jpg"),
		SCALED(String.format("proc.scaled.%d.%d.", SCALED_SIZE.x, SCALED_SIZE.y), new F1<ItemPhotoType, Void>() { public Void f(ItemPhotoType a) { a.stateField = ItemPhoto.scaledState; return null; }}, null),
		BIGTHUMB(String.format("proc.thumb.%d.%d.", THUMB_SIZE_LARGE.x, THUMB_SIZE_LARGE.y), new F1<ItemPhotoType, Void>() { public Void f(ItemPhotoType a) { a.stateField = ItemPhoto.thumbState; return null; }}, null), 
		SMALLTHUMB(String.format("proc.thumb.%d.%d.", THUMB_SIZE_SMALL.x, THUMB_SIZE_SMALL.y), new F1<ItemPhotoType, Void>() { public Void f(ItemPhotoType a) { a.stateField = ItemPhoto.thumbState; return null; }}, null),
		PHOTO(String.format(""), new F1<ItemPhotoType, Void>() { public Void f(ItemPhotoType a) { a.stateField = ItemPhoto.photoState; return null; }}, null);
		
		private Mappable<Entity<ItemPhoto>, String> mapper = null;
		private Mappable<Entity<ItemPhoto>, Pair<Entity<ItemPhoto>, String>> pairMapper = null;
		public Mappable<Entity<ItemPhoto>, String> getMapper() { return this.mapper; }
		public Mappable<Entity<ItemPhoto>, Pair<Entity<ItemPhoto>, String>> getPairMapper() { return this.pairMapper; }
		
		private F1<ItemPhotoType, Void> postLoader = null;
		public void postLoad() { this.postLoader.f(this); }
		
		private FieldKey<ItemPhoto.PhotoState> stateField = null;
		private String prefix = null;
		private String ext = null;
		private ItemPhotoType(String prefix, F1<ItemPhotoType, Void> postLoader, String ext) {
			this.prefix = prefix;
			this.postLoader = postLoader;
			this.ext = ext;
			this.mapper = new Mappable<Entity<ItemPhoto>, String>() {
				@Override public String f(Entity<ItemPhoto> a) {
					if (a.get(ItemPhotoType.this.stateField).lessThan(ItemPhoto.PhotoState.PROCESSED)) {
						return null;
					}
					
					if (a.get(ItemPhotoType.this.stateField).isEqual(ItemPhoto.PhotoState.DEPLOYED)) {
						String bucket = ItemPhotoType.getBucket(a.get(ItemPhoto.item));
						return String.format("https://s3.amazonaws.com/%s/%s.%s%s.%s", bucket, a.get(ItemPhoto.item).get(Item.token), ItemPhotoType.this.prefix, a.get(ItemPhoto.photoName), ItemPhotoType.this.ext == null ? a.get(ItemPhoto.photoExt) : ItemPhotoType.this.ext);
					} else {
						return String.format("/media/usr/image_uploads/%s/%s%s.%s", a.get(ItemPhoto.item).get(Item.token), ItemPhotoType.this.prefix, a.get(ItemPhoto.photoName), ItemPhotoType.this.ext == null ? a.get(ItemPhoto.photoExt) : ItemPhotoType.this.ext);
					}
				}
			};
			
			this.pairMapper = new Mappable<Entity<ItemPhoto>, Pair<Entity<ItemPhoto>, String>>() {
				@Override public Pair<Entity<ItemPhoto>, String> f(Entity<ItemPhoto> a) {
					return new Pair<Entity<ItemPhoto>, String>(a, ItemPhotoType.this.mapper.f(a));
				}
			};
		}
		
		@SuppressWarnings("deprecation")
		public static String getBucket(Entity<Item> item) {
			Timestamp ts = item.get(Item.created);
			return String.format("vendien.attachments.%d.%d.%d", ts.getYear(), ts.getMonth(), ts.getDate());
		}
	}
	
	private ItemManager itemManager = null;
	private ItemPhotoManager itemPhotoManager = null;
	private ItemPropertyManager itemPropertyManager = null;
	private ItemCommentManager itemCommentManager = null;
	private ItemTagManager itemTagManager = null;
	private AsyncQueue photoProcessingQueue = null;
	private AsyncQueue bgProcessingQueue = null;
	private AsyncQueue thumbProcessingQueue = null;
	private AsyncQueue scaledProcessingQueue = null;
	private AsyncQueue deploymentQueue = null;
    private AmazonS3Client s3Client = null;
    private SecureRandom random = null;
    private CacheConnectionPool cacheConnectionPool = null;
	private SearchService searchService = null;
    
	@Override
	public void init() {
		System.setProperty("java.awt.headless", "true");
		ProcessStarter.setGlobalSearchPath("/usr/bin:/opt/local/bin");
		
		for (ItemPhotoType t: ItemPhotoType.values()) {
			t.postLoad();
		}
		
		this.itemManager = this.serviceManager.getService(EntityService.class).getManager(Item.class, ItemManager.class);
		this.itemPhotoManager = this.serviceManager.getService(EntityService.class).getManager(ItemPhoto.class, ItemPhotoManager.class);
		this.itemPropertyManager = this.serviceManager.getService(EntityService.class).getManager(ItemProperty.class, ItemPropertyManager.class);
		this.itemCommentManager = this.serviceManager.getService(EntityService.class).getManager(ItemComment.class, ItemCommentManager.class);
		this.itemTagManager = this.serviceManager.getService(EntityService.class).getManager(ItemTag.class, ItemTagManager.class);
		
		this.photoProcessingQueue = new AsyncQueue(1000000l);
		this.bgProcessingQueue = new AsyncQueue(1000000l);
		this.thumbProcessingQueue = new AsyncQueue(1000000l);
		this.scaledProcessingQueue = new AsyncQueue(1000000l);
		this.deploymentQueue = new AsyncQueue(6000000l);
		
		try {
			InputStream awsProps = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("aws.properties").getFile());
			this.s3Client = new AmazonS3Client(new PropertiesCredentials(awsProps));
			
			this.random = SecureRandom.getInstance("SHA1PRNG");
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		this.cacheConnectionPool = this.serviceManager.getService(ConnectionService.class).getPool(CacheConnection.class, CacheConnectionPool.class);
		this.searchService = this.serviceManager.getService(SearchService.class);
	}
	
	public Quad<Entity<Item>, List<Entity<ItemPhoto>>, List<Entity<ItemProperty>>, List<Entity<ItemComment>>> find(String token) {
		List<Entity<Item>> items = this.itemManager.query(Item.token, token);
		if (items.size() > 0) {
			Entity<Item> item = items.get(0);
			return new Quad<Entity<Item>, List<Entity<ItemPhoto>>, List<Entity<ItemProperty>>, List<Entity<ItemComment>>>(item, 
					this.itemPhotoManager.query(new SQLFieldPredicate<ItemPhoto, Entity<Item>>(ItemPhoto.item, item)
						.and(new SQLFieldPredicate<ItemPhoto, Boolean>(ItemPhoto.deleted, false))
						.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.photoState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED))
						.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.scaledState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED))
						.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.thumbState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED))),
					this.itemPropertyManager.query(new SQLFieldPredicate<ItemProperty, Entity<Item>>(ItemProperty.item, item)),
					this.itemCommentManager.query(new SQLFieldPredicate<ItemComment, Entity<Item>>(ItemComment.item, item)
						.and(new SQLFieldPredicate<ItemComment, Boolean>(ItemComment.deleted, false))
						.order(ItemComment.rank, SQLOrderPredicate.Order.DESC)));
		}
		
		return null;
	}
	
	public Triple<List<Entity<ItemPhoto>>, List<String>, List<String>> getProcessedThumbsAndURLs(Entity<Item> item) {
		List<Entity<ItemPhoto>> photos = this.itemPhotoManager.query(new SQLFieldPredicate<ItemPhoto, Entity<Item>>(ItemPhoto.item, item)
				.and(new SQLFieldPredicate<ItemPhoto, Boolean>(ItemPhoto.deleted, false))
				.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.photoState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED))
				.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.scaledState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED))
				.and(new SQLFieldPredicate<ItemPhoto, ItemPhoto.PhotoState>(ItemPhoto.thumbState, SQLFieldPredicate.Comparator.GTE, ItemPhoto.PhotoState.PROCESSED)));
		return new Triple<List<Entity<ItemPhoto>>, List<String>, List<String>>(photos, ItemPhotoType.PHOTO.mapper.map(photos), ItemPhotoType.SMALLTHUMB.mapper.map(photos));
	}
	
	public Map<String, Entity<ItemPhoto>> getPreviewPhotos(List<Entity<Item>> items) {
		Map<String, Entity<ItemPhoto>> ret = new HashMap<String, Entity<ItemPhoto>>();
		
		if (items != null && items.size() > 0) {
			List<Entity<ItemPhoto>> photos = this.itemPhotoManager.query(new SQLInPredicate<ItemPhoto, Entity<Item>>(ItemPhoto.item, items));
			for (Entity<ItemPhoto> photo: photos) {
				if (!ret.containsKey(photo.get(ItemPhoto.item).getKey())) {
					ret.put(photo.get(ItemPhoto.item).getKey(), photo);
				}
			}
		}
		
		return ret;
	}
	
	public Entity<Item> get(String token) {
		List<Entity<Item>> res = this.itemManager.query(Item.token, token);
		if (res.size() == 0) {
			return null;
		}
		
		return res.get(0);
	}
	
	public Entity<Item> resume(String token, Entity<User> user) {		
		List<Entity<Item>> items = this.itemManager.query(Item.token, token);
		if (items.size() > 0) {
			Entity<Item> item = items.get(0);
			Entity<User> owner = item.get(Item.user);
			if (!owner.getKey().equals("0") && (user == null || !owner.getKey().equals(user.getKey()))) {
				return null;
			} else if (user != null && !user.getKey().equals("0")) {
				item.set(Item.user, user);
				this.itemManager.save(item);
			}
			
			return item;
		} else {
			Entity<Item> item = this.itemManager.create();
			item.set(Item.type, "default");
			item.set(Item.created, new Timestamp(System.currentTimeMillis()));
			if (user != null && !user.getKey().equals("0")) {
				item.set(Item.user, user);
				item.set(Item.facebookIdentity, user.get(User.facebookIdentity));
				item.set(Item.twitterIdentity, user.get(User.twitterIdentity));
			}
			
			if (token == null || token.length() == 0) {
				byte[] rand = new byte[1];
				this.random.nextBytes(rand);
				token = BaseConversion.toBase(BaseConversion.BASE62DIGITS, BigInteger.valueOf(rand[0]).shiftLeft(64).add(BigInteger.valueOf(System.currentTimeMillis())));
				
				item.set(Item.description, "");
				item.set(Item.token, token);
				while (!this.itemManager.save(item)) {
					this.random.nextBytes(rand);
					token = BaseConversion.toBase(BaseConversion.BASE62DIGITS, BigInteger.valueOf(rand[0]).shiftLeft(32).add(BigInteger.valueOf(System.currentTimeMillis()).shiftRight(32)));
				}
				
				return item;
			} else {
				item.set(Item.token, token);
				if (this.itemManager.save(item)) {
					return item;
				}
			}
			
			return this.resume(token, user);
		}
	}
	
	public boolean update(Entity<Item> item, String label, String description, String price, String location, GeoPoint position, long startTime, long endTime, Map<String, String> captions) {
		item.set(Item.label, label);
		item.set(Item.description, description);
		item.set(Item.price, price);
		item.set(Item.location, location);
		item.set(Item.position, position);
		item.set(Item.startTime, new Timestamp(startTime));
		item.set(Item.endTime, new Timestamp(startTime));
		
		Entity<User> user = item.get(Item.user);
		if (user != null && !user.getKey().isEmpty() && !user.getKey().equals("0")) {
			item.set(Item.facebookIdentity, user.get(User.facebookIdentity));
			item.set(Item.twitterIdentity, user.get(User.twitterIdentity));
		}
		
		if (!this.itemManager.save(item)) {
			return false;
		}
		
		Map<String, Entity<ItemPhoto>> itemPhotos = this.itemPhotoManager.get(Arrays.asList(captions.keySet().toArray(new String[0])));
		for (Entry<String, Entity<ItemPhoto>> e: itemPhotos.entrySet()) {
			Entity<ItemPhoto> itemPhoto = e.getValue();
			itemPhoto.set(ItemPhoto.caption, captions.get(e.getKey()));
			this.itemPhotoManager.save(itemPhoto);
		}
		
		this.clearRenderedItemDisplays(item);
		this.addToSearchIndex(item, true);
		
		return true;
	}

	public boolean removePhoto(Entity<Item> item, String photo) {
		Entity<ItemPhoto> itemPhoto = this.itemPhotoManager.get(photo);
		if (itemPhoto == null || itemPhoto.getKey().length() == 0) {
			return false;
		}

		if (!itemPhoto.get(ItemPhoto.item).getKey().equals(item.getKey())) {
			return false;
		}
		
		itemPhoto.set(ItemPhoto.deleted, true);
		if (!this.itemPhotoManager.save(itemPhoto)) {
			return false;
		}
		
		this.clearRenderedItemDisplays(item);
		
		return true;
	}
	
	public boolean updatePhoto(Entity<Item> item, String photo, String caption) {
		Entity<ItemPhoto> itemPhoto = this.itemPhotoManager.get(photo);
		if (itemPhoto == null || itemPhoto.getKey().length() == 0) {
			return false;
		}

		if (!itemPhoto.get(ItemPhoto.item).getKey().equals(item.getKey())) {
			return false;
		}
		
		itemPhoto.set(ItemPhoto.caption, caption);
		if (!this.itemPhotoManager.save(itemPhoto)) {
			return false;
		}
		
		this.clearRenderedItemDisplays(item);
		
		return true;
	}
	
	public Entity<ItemPhoto> addPhoto(Entity<Item> item, File photo, String caption, boolean processAsync) {
		List<Entity<ItemPhoto>> existingPhotos = this.itemPhotoManager.query(new SQLFieldPredicate<ItemPhoto, Entity<Item>>(ItemPhoto.item, item)
				.and(new SQLFieldPredicate<ItemPhoto, String>(ItemPhoto.photo, photo.getName())));
		if (existingPhotos.size() > 0) {
			Entity<ItemPhoto> existingPhoto = existingPhotos.get(0);
			
			if (existingPhoto.get(ItemPhoto.deleted)) {
				existingPhoto.set(ItemPhoto.deleted, false);
				if (!this.itemPhotoManager.save(existingPhoto)) {
					return null;
				}
				
				this.clearRenderedItemDisplays(item);
			}
			
			return existingPhoto;
		}
		
		String photoPath = photo.getName();
    	String[] photoNameParts = photoPath.split("\\.");
    	if (photoNameParts.length == 0) {
    		this.logger.error(String.format("Could not parse photoNameParts out of: %s, index: %s", photoPath));
    		return null;
    	}
    	
    	String photoExt = photoNameParts[photoNameParts.length - 1];
    	String photoName = photo.getName().replace("." + photoExt, "");
		
		Entity<ItemPhoto> itemPhoto = this.itemPhotoManager.create();
		itemPhoto.set(ItemPhoto.item, item);
		itemPhoto.set(ItemPhoto.photo, photo.getName());
		itemPhoto.set(ItemPhoto.photoName, photoName);
		itemPhoto.set(ItemPhoto.photoExt, photoExt);
		itemPhoto.set(ItemPhoto.caption, caption);
		itemPhoto.set(ItemPhoto.photoState, ItemPhoto.PhotoState.STARTED);
		itemPhoto.set(ItemPhoto.bgState, ItemPhoto.PhotoState.STARTED);
		itemPhoto.set(ItemPhoto.thumbState, ItemPhoto.PhotoState.STARTED);
		itemPhoto.set(ItemPhoto.scaledState, ItemPhoto.PhotoState.STARTED);
		itemPhoto.set(ItemPhoto.deleted, false);
		
		try {
			BufferedImage image = ImageIO.read(photo);
			itemPhoto.set(ItemPhoto.width, image.getWidth());
			itemPhoto.set(ItemPhoto.height, image.getHeight());
		
			File dstPhoto = new File(new StringBuilder(photo.getParentFile().getAbsolutePath()).append(File.separator).append("proc.pixel.").append(photo.getName()).toString());
			int imgWidth = itemPhoto.get(ItemPhoto.width);
			int imgHeight = itemPhoto.get(ItemPhoto.height);
			
			IMOperation op = new IMOperation();
			op.size(imgWidth, imgHeight);
			op.addImage(photo.getPath());	
			op.autoOrient();
			op.filter("box");
			op.resize(1, 1, '!');
			op.addImage(dstPhoto.getPath());
			ConvertCmd cmd = new ConvertCmd();
			cmd.run(op);
			
			BufferedImage imagePixel = ImageIO.read(dstPhoto);
			int[] pixels = imagePixel.getRGB(0, 0, imagePixel.getWidth(), imagePixel.getHeight(), null, 0, imagePixel.getWidth());
			long R = 0;
			long G = 0;
			long B = 0;
			
			for (int p: pixels) {
				int r = p >> 16 & 0xFF;
				int g = p >> 8 & 0xFF;
				int b = p >> 0 & 0xFF;
				R += r;
				G += g;
				B += b;
			}
			
			R = R / pixels.length;
			G = G / pixels.length;
			B = B / pixels.length;
			
			int rgb = (int) (((R & 0xFF) << 16) | ((G & 0xFF) << 8) | ((B & 0xFF) << 0));
			itemPhoto.set(ItemPhoto.rgb, rgb);
			itemPhoto.set(ItemPhoto.rgbString, String.format("rgb(%s,%s,%s)", R, G, B));
		} catch (Exception e) {
			ErrorControl.logException(e);
			return null;
		}
		
		if (!this.itemPhotoManager.save(itemPhoto)) {
			return null;
		}

		this.photoProcessingQueue.queue(new ProcessPhotoTask(this, this.logger, this.serviceManager, this.itemPhotoManager, item, itemPhoto.branch(), photo), processAsync);
		this.bgProcessingQueue.queue(new ProcessBackgroundTask(this, this.logger, this.serviceManager, this.itemPhotoManager, item, itemPhoto.branch(), photo), processAsync);
		this.thumbProcessingQueue.queue(new ProcessThumbnailTask(this, this.logger, this.serviceManager, this.itemPhotoManager, item, itemPhoto.branch(), photo), processAsync);
		this.scaledProcessingQueue.queue(new ProcessScaledTask(this, this.logger, this.serviceManager, this.itemPhotoManager, item, itemPhoto.branch(), photo), processAsync);
		
		this.clearRenderedItemDisplays(item);
		
		return itemPhoto;
	}

	public void deployPhoto(Entity<Item> item, Entity<ItemPhoto> itemPhoto, List<File> photoSet, List<FieldKey<ItemPhoto.PhotoState>> stateFieldSet, boolean deployAsync) {
		this.deploymentQueue.queue(new DeployPhotoTask(this, this.logger, this.serviceManager, this.itemPhotoManager, this.s3Client, item, itemPhoto, photoSet, stateFieldSet), deployAsync);
	}
	
	public boolean addTag(Entity<Item> item, String tag) {
		Entity<ItemTag> itemTag = this.itemTagManager.create();
		itemTag.set(ItemTag.item, item);
		itemTag.set(ItemTag.tag, tag);
		if (!this.itemTagManager.save(itemTag)) {
			return false;
		}
		
		this.clearRenderedItemDisplays(item);
		
		return true;
	}
	
	public List<Entity<ItemComment>> getComments(Entity<Item> item) {
		return this.itemCommentManager.query(new SQLFieldPredicate<ItemComment, Entity<Item>>(ItemComment.item, item)
				.and(new SQLFieldPredicate<ItemComment, Boolean>(ItemComment.deleted, false))
				.order(ItemComment.rank, SQLOrderPredicate.Order.DESC));
	}
	
	public List<Map<String, Object>> getCommentMaps(Entity<Item> item) {
		List<FieldKey<?>> filtered = new ArrayList<FieldKey<?>>(2);
		filtered.add(ItemComment.item);
		filtered.add(ItemComment.user);
		
		List<EntityHelper.RecursiveFieldKeySet<?>> recursive = new ArrayList<EntityHelper.RecursiveFieldKeySet<?>>(2);
		recursive.add(new EntityHelper.RecursiveFieldKeySet<Entity<Identity>>(ItemComment.twitterIdentity, Identity.secret, Identity.token, Identity.user));
		recursive.add(new EntityHelper.RecursiveFieldKeySet<Entity<Identity>>(ItemComment.facebookIdentity, Identity.secret, Identity.token, Identity.user));

		return this.itemCommentManager.getHelper().getListOfMaps(this.getComments(item), filtered, recursive);
	}
	
	public boolean removeComment(Entity<Item> item, String comment, Entity<User> user) {
		Entity<ItemComment> itemComment = this.itemCommentManager.get(comment);
		if (itemComment == null || itemComment.getKey().length() == 0) {
			return false;
		}
		
		if (!itemComment.get(ItemComment.item).getKey().equals(item.getKey())) {
			return false;
		}
		
		Entity<User> itemOwner = itemComment.get(ItemComment.item).get(Item.user);
		boolean hasItemOwner = itemOwner != null && !itemOwner.getKey().equals("0");
		boolean isItemOwner = itemOwner.getKey().equals(user.getKey());
		
		Entity<User> commentOwner = itemComment.get(ItemComment.user);
		boolean hasCommentOwner = commentOwner != null && !commentOwner.getKey().equals("0");
		boolean isCommentOwner = commentOwner.getKey().equals(user.getKey());
		
		if (!((isCommentOwner && hasCommentOwner) || (!hasCommentOwner && (isItemOwner || !hasItemOwner)))) {
			return false;
		}
		
		itemComment.set(ItemComment.deleted, true);
		if (!this.itemCommentManager.save(itemComment)) {
			return false;
		}
		
		this.clearRenderedItemDisplays(itemComment.get(ItemComment.item));
		
		return true;
	}
	
	public Entity<ItemComment> addComment(Entity<Item> item, Entity<User> user, String comment, boolean useTwitter, String twitter, boolean useFacebook, String facebook) {
		Entity<ItemComment> itemComment = this.itemCommentManager.create();
		itemComment.set(ItemComment.item, item);
		itemComment.set(ItemComment.rank, System.currentTimeMillis() * 1.f);
		itemComment.set(ItemComment.comment, comment);
		
		if (user != null && !user.getKey().equals("0")) {
			itemComment.set(ItemComment.user, user);
			
			if (useFacebook && user.get(User.facebookIdentity) != null && !user.get(User.facebookIdentity).getKey().equals("0")) {
				itemComment.set(ItemComment.facebookIdentity, user.get(User.facebookIdentity));
			}

			if (useTwitter && user.get(User.twitterIdentity) != null && !user.get(User.twitterIdentity).getKey().equals("0")) {
				itemComment.set(ItemComment.twitterIdentity, user.get(User.twitterIdentity));
			}
		}
		
		if (!this.itemCommentManager.save(itemComment)) {
			return null;
		}
		
		this.clearRenderedItemDisplays(item);
		
		return itemComment;
	}
	
	public List<Entity<ItemProperty>> getProperties(Entity<Item> item) {
		return this.itemPropertyManager.query(new SQLFieldPredicate<ItemProperty, Entity<Item>>(ItemProperty.item, item));
	}
	
	public List<Entity<ItemProperty>> getProperties(Entity<Item> item, String namespace) {
		return this.itemPropertyManager.query(new SQLFieldPredicate<ItemProperty, Entity<Item>>(ItemProperty.item, item)
												.and(new SQLFieldPredicate<ItemProperty, String>(ItemProperty.namespace, namespace)));
	}
	
	public Map<String, String> getPropertyMap(Entity<Item> item, String namespace) {
		Map<String, String> map = new HashMap<String, String>();
		for (Entity<ItemProperty> property: this.getProperties(item)) {
			map.put(property.get(ItemProperty.property), property.get(ItemProperty.value));
		}
		
		return map;
	}
	
	public Map<String, Map<String, String>> getPropertyMap(Entity<Item> item) {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (Entity<ItemProperty> property: this.getProperties(item)) {
			Map<String, String> namespace = map.get(property.get(ItemProperty.namespace));
			if (namespace == null) {
				namespace = new HashMap<String, String>();
				map.put(property.get(ItemProperty.namespace), namespace);
			}
			
			namespace.put(property.get(ItemProperty.property), property.get(ItemProperty.value));
		}
		
		return map;
	}

	public Entity<ItemProperty> getProperty(Entity<Item> item, String namespace, String property) {
		List<Entity<ItemProperty>> propList = this.itemPropertyManager.query(new SQLFieldPredicate<ItemProperty, Entity<Item>>(ItemProperty.item, item)
				.and(new SQLFieldPredicate<ItemProperty, String>(ItemProperty.namespace, namespace))
				.and(new SQLFieldPredicate<ItemProperty, String>(ItemProperty.property, property)));
		if (propList.size() == 0) return null;
		return propList.get(0);
	}
	
	public boolean removeProperty(Entity<Item> item, String namespace, String property) {
		Entity<ItemProperty> itemProp = this.getProperty(item, namespace, property);
		if (itemProp == null) return false;
		
		itemProp.delete();
		this.clearRenderedItemDisplays(item);
		return true;
	}
	
	public boolean updateProperty(Entity<Item> item, String namespace, String property, String expect, String update) {
		Entity<ItemProperty> itemProp = this.getProperty(item, namespace, property);
		if (itemProp == null) return false;
		return this.itemPropertyManager.update(new SQLValueList<ItemProperty>(new SQLValue<ItemProperty, String>(ItemProperty.value, update)), 
												new SQLEntityPredicate<ItemProperty>(itemProp)
													.and(new SQLFieldPredicate<ItemProperty, String>(ItemProperty.value, expect))) > 0;
	}
	
	public Entity<ItemProperty> addProperty(Entity<Item> item, String namespace, String property, String value) {
		Entity<ItemProperty> itemProperty = this.itemPropertyManager.create();
		itemProperty.set(ItemProperty.item, item);
		itemProperty.set(ItemProperty.namespace, namespace);
		itemProperty.set(ItemProperty.property, property);
		itemProperty.set(ItemProperty.value, value);
		
		if (!this.itemPropertyManager.save(itemProperty)) {
			return null;
		}
		
		this.clearRenderedItemDisplays(item);
		
		return itemProperty;
	}
	
	public static enum CacheTag {
		RENDERED_PAGE,
		RENDERED_COMMENT;
		public byte[] getTagBytes() { return Serialization.serialize(this.name()); }
	}
	
	public boolean cacheRenderedItemDisplay(Entity<Item> item, CacheTag cacheTag, byte[] value) { return this.cacheConnectionPool.acquire().set(ItemLogic.class.getName(), this.composeCacheKey(item, cacheTag), value); }
	public boolean cacheRenderedItemDisplay(String itemToken, CacheTag cacheTag, byte[] value) { return this.cacheConnectionPool.acquire().set(ItemLogic.class.getName(), this.composeCacheKey(itemToken, cacheTag), value); }
	
	public byte[] getRenderedItemDisplay(Entity<Item> item, CacheTag cacheTag) { return this.cacheConnectionPool.acquire().get(ItemLogic.class.getName(), this.composeCacheKey(item, cacheTag)); }
	public byte[] getRenderedItemDisplay(String itemToken, CacheTag cacheTag) { return this.cacheConnectionPool.acquire().get(ItemLogic.class.getName(), this.composeCacheKey(itemToken, cacheTag)); }
	
	public boolean clearRenderedItemDisplays(Entity<Item> item) {
		boolean ret = true;
		for (CacheTag tag: CacheTag.values()) {
			ret = ret && this.cacheConnectionPool.acquire().clear(ItemLogic.class.getName(), this.composeCacheKey(item, tag));
		}
		return ret;
	}
	
	public boolean clearRenderedItemDisplays(String itemToken) {
		boolean ret = true;
		for (CacheTag tag: CacheTag.values()) {
			ret = ret && this.cacheConnectionPool.acquire().clear(ItemLogic.class.getName(), this.composeCacheKey(itemToken, tag));
		}
		return ret;
	}
	
	private String composeCacheKey(Entity<Item> item, CacheTag cacheTag) { return this.composeCacheKey(item.get(Item.token), cacheTag); }
	private String composeCacheKey(String itemToken, CacheTag cacheTag) {
		return new StringBuilder(cacheTag.name()).append(':').append(itemToken).toString();
	}
	
	public boolean cacheRenderedComment(Entity<ItemComment> comment, CacheTag cacheTag, byte[] value) { return this.cacheConnectionPool.acquire().set(ItemLogic.class.getName(), this.composeCommentCacheKey(comment, cacheTag), value); }
	public boolean cacheRenderedComment(String comment, CacheTag cacheTag, byte[] value){ return this.cacheConnectionPool.acquire().set(ItemLogic.class.getName(), this.composeCommentCacheKey(comment, cacheTag), value); }
	
	public byte[] getRenderedComment(Entity<ItemComment> item, CacheTag cacheTag) { return this.cacheConnectionPool.acquire().get(ItemLogic.class.getName(), this.composeCommentCacheKey(item, cacheTag)); }
	public byte[] getRenderedComment(String itemToken, CacheTag cacheTag) { return this.cacheConnectionPool.acquire().get(ItemLogic.class.getName(), this.composeCommentCacheKey(itemToken, cacheTag)); }
	
	public boolean clearRenderedComment(Entity<ItemComment> comment) {
		boolean ret = true;
		for (CacheTag tag: CacheTag.values()) {
			ret = ret && this.cacheConnectionPool.acquire().clear(ItemLogic.class.getName(), this.composeCommentCacheKey(comment, tag));
		}
		return ret;
	}
	
	public boolean clearRenderedComment(String comment) {
		boolean ret = true;
		for (CacheTag tag: CacheTag.values()) {
			ret = ret && this.cacheConnectionPool.acquire().clear(ItemLogic.class.getName(), this.composeCommentCacheKey(comment, tag));
		}
		return ret;
	}
	
	private String composeCommentCacheKey(Entity<ItemComment> comment, CacheTag cacheTag) { return this.composeCommentCacheKey(comment.getKey(), cacheTag); }
	private String composeCommentCacheKey(String comment, CacheTag cacheTag) {
		return new StringBuilder(cacheTag.name()).append(':').append(comment).toString();
	}

	public List<Entity<Item>> search(GeoPoint geo, String query) {
		return this.searchService.searchItemEntities(geo, query);
	}

	public List<String> suggest(GeoPoint geo, String query) {
		return this.searchService.suggest(query);
	}

	public void addToSearchIndex(Entity<Item> item, boolean async) {
		if (async) {
			this.searchService.addItemAsync(item);
		} else {
			this.searchService.addItem(item);
		}
	}
	
	public void commitSearchIndex() {
		this.searchService.commitChanges();
	}
	
}
