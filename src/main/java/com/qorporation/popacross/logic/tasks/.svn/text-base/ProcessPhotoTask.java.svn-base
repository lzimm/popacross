package com.qorporation.popacross.logic.tasks;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;

import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.entity.manager.ItemPhotoManager;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.qluster.async.AsyncTask;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.transaction.Transaction;

public class ProcessPhotoTask extends AsyncTask<Void> {
	private ItemLogic itemLogic = null;
	
	private ServiceManager serviceManager = null;
	private ItemPhotoManager itemPhotoManager = null;
	
	private Entity<Item> item = null;
	private Entity<ItemPhoto> itemPhoto = null;
	private File photo = null;
	public ProcessPhotoTask(ItemLogic itemLogic, Logger logger, ServiceManager serviceManager, ItemPhotoManager itemPhotoManager, Entity<Item> item, Entity<ItemPhoto> itemPhoto, File photo) {
		this.itemLogic = itemLogic;
		this.serviceManager = serviceManager;
		this.itemPhotoManager = itemPhotoManager;
		
		this.item = item;
		this.itemPhoto = itemPhoto;
		this.photo = photo;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Void call() throws Exception {
		Transaction t = this.serviceManager.getService(EntityService.class).startGlobalTransaction();
		
		this.itemPhoto.set(ItemPhoto.photoState, ItemPhoto.PhotoState.PROCESSED);
		this.itemPhotoManager.save(itemPhoto);
		
		this.itemLogic.deployPhoto(item, itemPhoto, Arrays.asList(photo), Arrays.asList(ItemPhoto.photoState), true);
		this.itemLogic.clearRenderedItemDisplays(item);
		
		t.finish();
		
		return null;
	}
}