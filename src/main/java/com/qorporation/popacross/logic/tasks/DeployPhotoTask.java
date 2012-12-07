package com.qorporation.popacross.logic.tasks;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.entity.manager.ItemPhotoManager;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.popacross.logic.ItemLogic.ItemPhotoType;
import com.qorporation.qluster.async.AsyncTask;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.transaction.Transaction;

public class DeployPhotoTask extends AsyncTask<Void> {
	private ItemLogic itemLogic = null;

	private ServiceManager serviceManager = null;
	private ItemPhotoManager itemPhotoManager = null;
	private AmazonS3Client s3Client = null;
	
	private Entity<Item> item = null;
	private Entity<ItemPhoto> itemPhoto = null;
	private List<File> photoSet = null;
	private List<FieldKey<ItemPhoto.PhotoState>> stateFieldSet;
	public DeployPhotoTask(ItemLogic itemLogic, Logger logger, ServiceManager serviceManager, ItemPhotoManager itemPhotoManager, AmazonS3Client s3Client, Entity<Item> item, Entity<ItemPhoto> itemPhoto, List<File> photoSet, List<FieldKey<ItemPhoto.PhotoState>> stateFieldSet) {
		this.itemLogic = itemLogic;
		
		this.serviceManager = serviceManager;
		this.itemPhotoManager = itemPhotoManager;
		this.s3Client = s3Client;
		
		this.item = item;
		this.itemPhoto = itemPhoto;
		this.photoSet = photoSet;
		this.stateFieldSet = stateFieldSet;
	}
	
	@Override
	public Void call() throws Exception {
		Transaction t = this.serviceManager.getService(EntityService.class).startGlobalTransaction();
		
		String bucket = ItemPhotoType.getBucket(this.item);
		
		if (!this.s3Client.doesBucketExist(bucket)) {
			try {
				this.s3Client.createBucket(bucket);
			} catch (Exception e) {
				throw e;
			}
		}

		for (File photo: this.photoSet) {
			this.s3Client.putObject(new PutObjectRequest(bucket, new StringBuilder(this.item.get(Item.token)).append(".").append(photo.getName()).toString(), photo).withCannedAcl(CannedAccessControlList.PublicRead));
		}
		
		for (FieldKey<ItemPhoto.PhotoState> stateField: stateFieldSet) {
			itemPhoto.set(stateField, ItemPhoto.PhotoState.DEPLOYED);
		}
		
		if (this.itemPhotoManager.save(itemPhoto)) {
			this.itemLogic.clearRenderedItemDisplays(item);
			
			for (File photo: this.photoSet) {
				photo.delete();
				File parent = photo.getParentFile();
				if (parent != null && parent.isDirectory()) {
					if (parent.list().length == 0) {
						parent.delete();
					}
				}
			}
		}
		
		t.finish();
		
		return null;
	}
}