package com.qorporation.qluster.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.view.ViewService;

public class View extends HttpServlet {
	private static final long serialVersionUID = -89077422071834551L;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ViewService viewService = null;
	
	public View(ViewService viewService) {
		this.viewService = viewService;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		long startTime = System.currentTimeMillis();
		
		this.viewService.handle(request, response);
		
		long delta = System.currentTimeMillis() - startTime;
		
		this.logger.info(String.format("%s %s (%sms)", 
				request.getMethod(), 
				request.getRequestURI(), 
				delta));
	}

}