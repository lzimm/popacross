package com.qorporation.qluster.config;

import org.apache.hadoop.hbase.HBaseConfiguration;

import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.view.undefined.UndefinedUser;

public class Config {

	public String rootPackage = "com.qorporation";
	
	public String cassandraKeySpace = "VURS";
	public HBaseConfiguration hbaseConfiguration = new HBaseConfiguration();
	public String mysqlURL = "jdbc:mysql://localhost";
	public String mysqlDB = "vurs";
	public String mysqlUser = "root";
	public String mysqlPass = "";
	
	public String templateDir = "templates";
	public String mediaDir = "media";
	
	public Class<? extends Definition<? extends Connection>> userDefinition = UndefinedUser.class;
	
}
