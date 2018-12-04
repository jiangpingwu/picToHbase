/**
 * 
 */
package com.lx.picToHbase;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author kaca
 *
 */
public class ReadHbaseImagesTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		
        long start = System.currentTimeMillis();
        Connection connection = HbaseUtils.getConnection();
	        System.out.println("coonection times:" + (System.currentTimeMillis() - start));
	        start = System.currentTimeMillis();
	        
	        System.out.println("coonection times2:" + (System.currentTimeMillis() - start));
	        start = System.currentTimeMillis();
	        //建立表的连接
	        Table table = connection.getTable(TableName.valueOf("crawl_img"));
	        Get get = new Get(Bytes.toBytes("https://image1.ljcdn.com/hdic-frame/17c83b7a-0a9a-4c1f-8b3e-b02c4935df9c.jpg.1000x.jpg"));
	        get.addColumn(Bytes.toBytes("cfimg"), Bytes.toBytes("content"));
	        get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("type"));
	        System.out.println(System.currentTimeMillis() - start);
	        
	        start = System.currentTimeMillis();
	        Result result = table.get(get);
	        System.out.println(Bytes.toString( result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("type"))));
	       
	        byte[] val = result.getValue(Bytes.toBytes("cfimg"),
                             Bytes.toBytes("content"));
	        FileOutputStream os = new FileOutputStream("d://crawlpng.jpg");
	        os.write(val);
	        os.close();
	        System.out.println(System.currentTimeMillis() - start);
//	        ByteArrayInputStream in=new ByteArrayInputStream(val);
//	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        
	        //创建一个空的Scan实例
//	        Scan scan1 = new Scan();
//	        
//	        //在行上获取遍历器
//	        ResultScanner scanner1 = table.getScanner(scan1);
//
//	        //打印行的值
//	        for (Result res : scanner1) {
//	            System.out.println(res);
//	        }
//	        //关闭释放资源
//	        scanner1.close();

	}

}
