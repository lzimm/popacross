package com.qorporation.popacross.page;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

import com.qorporation.popacross.Server;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.RelativePath;
import com.qorporation.qluster.view.ViewAuthenticator;
import com.qorporation.qluster.view.ViewManager;

public class PageManager extends ViewManager<PageView, User> {
	private static String SHORT_REDIRECT_TEMPLATE = "http://%s/v/%s";
	
	private Configuration templateConfig;
	
	public PageManager(EntityService entityService, LogicService logicService, ViewAuthenticator<User> authenticator) {
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
		
		String domain = request.getServerName();
		if (domain.contains(Server.SHORT_DOMAIN)) {
			try {
				String redirectTo = String.format(SHORT_REDIRECT_TEMPLATE, Server.ROOT_DOMAIN, request.getRequestURI());
				response.sendRedirect(redirectTo);
				response.flushBuffer();			
			} catch (Exception e) {
				ErrorControl.logException(e);
			}
		} else {
			PageRequest pageRequest = new PageRequest(this.entityService, this.logicService, this.templateConfig, request, response);
			handle(pageRequest);
		}
		
		transaction.finish();
	}
	
}
