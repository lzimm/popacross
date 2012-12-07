package com.qorporation.popacross.logic.tasks;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;

import com.mortennobel.imagescaling.ThumbnailRescaleOp;
import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.ItemPhoto;
import com.qorporation.popacross.entity.manager.ItemPhotoManager;
import com.qorporation.popacross.logic.ItemLogic;
import com.qorporation.qluster.async.AsyncTask;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.SimpleProfiler;

public class ProcessScaledTask extends AsyncTask<Void> {
	private ItemLogic itemLogic = null;
	
	private Logger logger = null;
	private ServiceManager serviceManager = null;
	private ItemPhotoManager itemPhotoManager = null;
	
	private Entity<Item> item = null;
	private Entity<ItemPhoto> itemPhoto = null;
	private File photo = null;
	public ProcessScaledTask(ItemLogic itemLogic, Logger logger, ServiceManager serviceManager, ItemPhotoManager itemPhotoManager, Entity<Item> item, Entity<ItemPhoto> itemPhoto, File photo) {
		this.itemLogic = itemLogic;
		this.logger = logger;
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

		File scaled = this.createScaledImage(this.item, this.itemPhoto, this.photo, ItemLogic.SCALED_SIZE, true, false);
		
		this.itemPhoto.set(ItemPhoto.scaledState, ItemPhoto.PhotoState.PROCESSED);
		this.itemPhotoManager.save(this.itemPhoto);
		
		this.itemLogic.deployPhoto(this.item, this.itemPhoto, Arrays.asList(scaled), Arrays.asList(ItemPhoto.scaledState), true);
		this.itemLogic.clearRenderedItemDisplays(this.item);
		
		t.finish();
		
		return null;
	}
	
	private File createScaledImage(Entity<Item> item, Entity<ItemPhoto> itemPhoto, File photo, Point size, boolean crop, boolean processNatively) {
        File dstPhoto = new File(new StringBuilder(photo.getParentFile().getAbsolutePath()).append(File.separator).append("proc.scaled.").append(size.x).append(".").append(size.y).append(".").append(photo.getName()).toString());
        
		SimpleProfiler profiler = new SimpleProfiler(this.logger, "createScaledImage");
		
		try {
			if (processNatively) {
				BufferedImage originalImage = ImageIO.read(photo);
				
				int width = size.x;
				int height = size.y;
				int imgWidth = originalImage.getWidth();
				int imgHeight = originalImage.getHeight();
				
				float scale = 1.0f;
				if (width == 0 || (imgWidth > imgHeight && height > 0)) {
					scale = ((float) imgHeight) / ((float) height);
					width = (int) ((float) imgWidth / scale);
				} else {
					scale = ((float) imgWidth) / ((float) width);
					height = (int) ((float) imgHeight / scale);
				}
	
				ThumbnailRescaleOp rescaleOp = new ThumbnailRescaleOp(width, height);
	            rescaleOp.setSampling(ThumbnailRescaleOp.Sampling.S_2X2_RGSS);
	            BufferedImage img = crop ? new BufferedImage(size.x, size.y, originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType()) : rescaleOp.filter(originalImage, null);
	            
	            if (crop) {
	    			int imgX = (width - size.x) / 2;
	    			int imgY = (height - size.y) / 2;
	    			imgWidth = imgX + size.x;
	    			imgHeight = imgY + size.y;
	
	    			Graphics2D g = img.createGraphics();
	    			g.drawImage(rescaleOp.filter(originalImage, null), 0, 0, size.x, size.y, imgX, imgY, imgWidth, imgHeight, null);
	    			g.dispose();
	    			g.setComposite(AlphaComposite.Src);
	    			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            }
				
		        if (ImageIO.write(img, FilenameUtils.getExtension(dstPhoto.getName()), dstPhoto)) {
		        	profiler.profile("done");
		        	return dstPhoto;
		        }
			} else {
				int imgWidth = itemPhoto.get(ItemPhoto.width);
				int imgHeight = itemPhoto.get(ItemPhoto.height);
				
				IMOperation op = new IMOperation();
				op.size(imgWidth, imgHeight);
				op.addImage(photo.getPath());	
				op.autoOrient();
				op.thumbnail(size.x, size.y, '^');
				op.gravity("center");
				op.extent(size.x, size.y);
				op.addImage(dstPhoto.getPath());
				ConvertCmd cmd = new ConvertCmd();
				cmd.run(op);
				
	        	profiler.profile("done");
	        	return dstPhoto;
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		return null;
	}
}