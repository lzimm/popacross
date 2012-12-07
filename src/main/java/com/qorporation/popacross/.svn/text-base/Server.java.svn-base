package com.qorporation.popacross;

import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.Qluster;
import com.qorporation.qluster.config.Config;

public class Server {
	public static final String ROOT_DOMAIN = "popacross.com";
	public static final String SHORT_DOMAIN = "pops.io";
	
	public static void main(String[] args) {
		Qluster.start(new Config() {{
			this.rootPackage = "com.qorporation.popacross";
			this.cassandraKeySpace = "Popacross";
			this.mysqlDB = "popacross";
			this.userDefinition = User.class;
		}});
	}
}
