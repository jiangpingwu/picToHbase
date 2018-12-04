package com.lx.picToHbase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;


/**
 * @author kaca
 *
 */
public class WriterCrawlImageTest {
	
	
	public static final String FIELD_IMG_CONTENT = "content";
	public static final String IMG_FAMILY = "cfimg";

	public static void write(String sourceUrl,String filename, Table crawlTable) throws Exception {
		
		Get get = new Get(Bytes.toBytes(sourceUrl));
        get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
        Result result = crawlTable.get(get);
       
        byte[] val = result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
        if (val != null && val.length >0) {
			return;
		}
		
		byte[] bytes = inputStream2ByteArray(filename);
		Put crawlPut =  new Put(Bytes.toBytes(sourceUrl));
		crawlPut.addColumn(Bytes.toBytes(IMG_FAMILY), Bytes.toBytes(FIELD_IMG_CONTENT), bytes);
		crawlPut.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"), Bytes.toBytes("true"));
		crawlTable.put(crawlPut);
		
	}

	private static byte[] inputStream2ByteArray(String filePath) throws IOException {
		 
	    InputStream in = new FileInputStream(filePath);
	    byte[] data = toByteArray(in);
	    in.close();
	 
	    return data;
	}
	 
	private static byte[] toByteArray(InputStream in) throws IOException {
	 
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024 * 4];
	    int n = 0;
	    while ((n = in.read(buffer)) != -1) {
	        out.write(buffer, 0, n);
	    }
	    byte[] byteArray = out.toByteArray();
	    out.close();
		return byteArray;
	}
}
