package com.qorporation.popacross.logic.tasks;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
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
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.SimpleProfiler;

public class ProcessBackgroundTask extends AsyncTask<Void> {
	private static final boolean BG_USE_AVERAGE = true;
	private static final boolean BG_FORCE_GREY = false;
	private static final boolean BG_DARKEN = false;
	
	private ItemLogic itemLogic = null;
	
	private Logger logger = null;
	private ServiceManager serviceManager = null;
	private ItemPhotoManager itemPhotoManager = null;
	
	private Entity<Item> item = null;
	private Entity<ItemPhoto> itemPhoto = null;
	private File photo = null;

	public ProcessBackgroundTask(ItemLogic itemLogic, Logger logger, ServiceManager serviceManager, ItemPhotoManager itemPhotoManager, Entity<Item> item, Entity<ItemPhoto> itemPhoto, File photo) {
		this.itemLogic = itemLogic;
		this.logger = logger;
		this.serviceManager = serviceManager;
		this.itemPhotoManager = itemPhotoManager;
		
		this.item = item;
		this.itemPhoto = itemPhoto;
		this.photo = photo;
	}
	
	public long getCost() { return photo.length(); }

	@SuppressWarnings("unchecked")
	@Override
	public Void call() throws Exception {
		Transaction t = this.serviceManager.getService(EntityService.class).startGlobalTransaction();
		
		File bgImage = this.createBackgroundImage(this.item, this.itemPhoto, this.photo, false);
		
		this.itemPhoto.set(ItemPhoto.bgState, ItemPhoto.PhotoState.PROCESSED);
		this.itemPhotoManager.save(this.itemPhoto);
		
		this.itemLogic.deployPhoto(this.item, this.itemPhoto, Arrays.asList(bgImage), Arrays.asList(ItemPhoto.bgState), true);
		this.itemLogic.clearRenderedItemDisplays(this.item);
		
		t.finish();
		
		return null;
	}
	
    private File createBackgroundImage(Entity<Item> item, Entity<ItemPhoto> itemPhoto, File photo, boolean processNatively) {    	
        File dstPhoto = new File(new StringBuilder(photo.getParentFile().getAbsolutePath()).append(File.separator).append("proc.bg.").append(itemPhoto.get(ItemPhoto.photoName)).append(".jpg").toString());
        
    	SimpleProfiler profiler = new SimpleProfiler(this.logger, "createBackgroundImage");
    	
		try {
			if (processNatively) {
				BufferedImage originalImage = ImageIO.read(photo);
				
				int width = ItemLogic.BACKGROUND_SIZE.x;
				int height = ItemLogic.BACKGROUND_SIZE.y;
				
				if (originalImage.getWidth() > originalImage.getHeight()) {
					float scale = ((float) height) / ((float) originalImage.getHeight());
					if (scale > 1.f) {
						width *= scale;
					} else {
						width = originalImage.getWidth();
						height = originalImage.getHeight();
					}
				} else {
					float scale = ((float) width) / ((float) originalImage.getWidth());
					if (scale > 1.f) {
						height *= scale;
					} else {
						width = originalImage.getWidth();
						height = originalImage.getHeight();
					}
				}
				
				BufferedImage img = new BufferedImage(width, height, originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
				Graphics2D g = img.createGraphics();
				g.drawImage(originalImage, 0, 0, width, height, null);
				g.dispose();
				g.setComposite(AlphaComposite.Src);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				if (ProcessBackgroundTask.BG_FORCE_GREY) {
					BufferedImageOp opGrey = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null); 
					img = opGrey.filter(img, null);
				
					BufferedImageOp opRGB = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null); 
					img = opRGB.filter(img, null);
				}
		
				img = this.getGaussianBlurFilter(25, true).filter(img, null);
				img = this.getGaussianBlurFilter(25, false).filter(img, null);
				
				BufferedImage bugWorkaround = new BufferedImage(width, height, originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
				Graphics2D wg = bugWorkaround.createGraphics();
				wg.drawImage(img, 0, 0, null);
				wg.dispose();
				
				BufferedImageOp darken = new RescaleOp(0.25f, 0, null);
				img = darken.filter(bugWorkaround, null);
		        
		        if (ImageIO.write(img, FilenameUtils.getExtension(dstPhoto.getName()), dstPhoto)) {
		        	profiler.profile("full");
		        	return dstPhoto;
		        }
			} else {
				int width = ItemLogic.BACKGROUND_SIZE.x;
				int height = ItemLogic.BACKGROUND_SIZE.y;
				
				int imgWidth = itemPhoto.get(ItemPhoto.width);
				int imgHeight = itemPhoto.get(ItemPhoto.height);
				
				IMOperation op = new IMOperation();				
				op.size(imgWidth, imgHeight);				
				op.addImage(photo.getPath());	
				op.autoOrient();
				op.resize(width, height, '^');
				op.gravity("center");
				op.extent(width, height);
				
				if (ProcessBackgroundTask.BG_FORCE_GREY) {
					op.colorspace("gray");
				}
				
				if (ProcessBackgroundTask.BG_DARKEN) {
					if (ProcessBackgroundTask.BG_USE_AVERAGE) {					
						int rgb = this.itemPhoto.get(ItemPhoto.rgb);
						int r = rgb >> 16 & 0xFF;
						int g = rgb >> 8 & 0xFF;
						int b = rgb >> 0 & 0xFF;
						String rgba = String.format("rgba(%s,%s,%s,0.85)", r * 0.125, g * 0.125, b * 0.125);
						op.fill(rgba);
						op.draw(String.format("rectangle 0,0 %s,%s", width, height));
					} else {
						op.evaluate("multiply", "0.5");
					}
				} else if (ProcessBackgroundTask.BG_USE_AVERAGE) {					
					int rgb = this.itemPhoto.get(ItemPhoto.rgb);
					int r = rgb >> 16 & 0xFF;
					int g = rgb >> 8 & 0xFF;
					int b = rgb >> 0 & 0xFF;
					String rgba = String.format("rgba(%s,%s,%s,0.5)", r * 0.75, g * 0.75, b * 0.75);
					op.fill(rgba);
					op.draw(String.format("rectangle 0,0 %s,%s", width, height));
				}
				
				op.blur(Double.valueOf(0), Double.valueOf(15));
				op.type("optimize");
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
    
	private ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        int size = radius * 2 + 1;
        float[] data = new float[size];
        
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }        
        
        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
}