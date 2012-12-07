package com.qorporation.popacross.api;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.RelativePath;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewManager;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

public class APIManager extends ViewManager<APIView, User> {

	private Configuration templateConfig = null;

	public APIManager(EntityService entityService, LogicService logicService, ViewAuthenticator<User> authenticator) {
		super(entityService, logicService, authenticator);
		setupTemplates();
	}
	
	private void setupTemplates() {
		try {
			File templateDir = new File(RelativePath.root().getAbsolutePath()
												.concat(File.separator)
												.concat("templates"));
			
			this.templateConfig = new Configuration();
			this.templateConfig.setLocale(java.util.Locale.CANADA);
			this.templateConfig.setNumberFormat("0.##");  
			
			FileTemplateLoader ftl = new FileTemplateLoader(templateDir);
			this.templateConfig.setTemplateLoader(ftl);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) {
		Transaction transaction = this.entityService.startGlobalTransaction();
		
		APIRequest apiRequest = new APIRequest(this.entityService, this.logicService, this.templateConfig, request, response);
		handle(apiRequest);
		
		transaction.finish();
	}
	
}
