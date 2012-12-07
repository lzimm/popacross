package com.qorporation.qluster;

import java.io.File;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.service.ServiceManager;
import com.qorporation.qluster.servlet.View;
import com.qorporation.qluster.test.TestService;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.RelativePath;
import com.qorporation.qluster.view.ViewService;

public class Qluster {
	private static final Logger logger = LoggerFactory.getLogger(Qluster.class);
	
	public static void main(String[] args) {		
		start(new Config());
	}

	public static void start(Config config) {
		try {
			Qluster.logger.info("Loading Qluster");

			ClassWalker.addRootPackage(config.rootPackage);
			
	        ServiceManager serviceManager = new ServiceManager(config);
			runTests(serviceManager);
			
			HandlerList handlers = new HandlerList();
	        setupStaticResourceHandler(handlers, config, serviceManager);
	        setupViewServletHandler(handlers, config, serviceManager);
	        setupRequestLogHandler(handlers, config, serviceManager);
			
			Server server = new Server();
			setupServer(server, handlers);
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setupStaticResourceHandler(HandlerList handlers, Config config, ServiceManager serviceManager) {
		Qluster.logger.info("Setting up static resources");
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(RelativePath.root().getAbsolutePath()
				.concat(File.separator)
				.concat("media"));
		
		ContextHandler handler = new ContextHandler("/media");
		handler.setHandler(resourceHandler);
		
		handlers.addHandler(handler);
	}
	
	private static void setupViewServletHandler(HandlerList handlers, Config config, ServiceManager serviceManager) {
		Qluster.logger.info("Setting up view servlet");
		
		
		ServletHolder holder = new ServletHolder(new View(serviceManager.getService(ViewService.class)));
		
		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		handler.addServlet(holder, "/");
		
		handlers.addHandler(handler);
	}
	
	private static void setupRequestLogHandler(HandlerList handlers, Config config, ServiceManager serviceManager) {
		Qluster.logger.info("Setting up request log");

		NCSARequestLog log = new NCSARequestLog(RelativePath.root().getAbsolutePath()
				.concat(File.separator)
				.concat("logs")
				.concat(File.separator)
				.concat("jetty-yyyy_mm_dd.log"));
		log.setExtended(false);
		
		RequestLogHandler handler = new RequestLogHandler();
		handler.setRequestLog(log);
		
		handlers.addHandler(handler);
	}
	
	private static void setupServer(Server server, HandlerList handlers) {
		Qluster.logger.info("Setting up server");
		
		server.setThreadPool(new QueuedThreadPool());
		
		SelectChannelConnector selectConnector = new SelectChannelConnector();
		selectConnector.setPort(8080);
		server.addConnector(selectConnector);

        server.setHandler(handlers);		
	}

	private static void runTests(ServiceManager serviceManager) {
		Qluster.logger.info("Running tests");
		
		LogicService logicService = serviceManager.getService(LogicService.class);
		EntityService entityService = serviceManager.getService(EntityService.class);
		TestService testService = new TestService(entityService, logicService);
		testService.run();
	}
	
}
