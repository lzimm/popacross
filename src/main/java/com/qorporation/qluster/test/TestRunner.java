package com.qorporation.qluster.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;

public class TestRunner {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected EntityService entityService = null;
	protected LogicService logicService = null;
	
	public void setup(EntityService entityService, LogicService logicService) {
		this.logger.info("setting up runner");
		this.entityService = entityService;
		this.logicService = logicService;
	}
	
}
