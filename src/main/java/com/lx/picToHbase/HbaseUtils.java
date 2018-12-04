package com.lx.picToHbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

public class HbaseUtils {

	private static Configuration conf = HBaseConfiguration.create();

	public static Connection getConnection() throws IOException{
		conf.set("hbase.zookeeper.quorum", "10.1.220.7");
//		conf.set("hbase.zookeeper.quorum", "10.1.222.240");
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		return ConnectionFactory.createConnection(conf);
	}
}
