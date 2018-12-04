package com.lx.picToHbase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lx.utils.DownloadUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("unused")
public class PicToHbase {
	
	// 户型图片任务ID
	private static String[] housetypePictaskIds = {""};
	// 相册图片任务ID
	private static String[] housePictaskIds = {"f81d06bd-3e4f-4ec9-90ca-7f4ef9927388"};
		
	//public static final String IMG_TABLE = "crawl_img_bzy";
	public static final String IMG_TABLE = "crawl_img_ajk";
	public static final String IMG_FAMILY = "cfimg";
	public static final String FIELD_IMG_CONTENT = "content";

	public static void main(String[] args) throws Exception {
		System.out.println("================ 2.0链家数据整理任务 开始 ================");
		Connection connection = HbaseUtils.getConnection();
		Table crawlTable = connection.getTable(TableName.valueOf(IMG_TABLE));
		// 1.先获取Token
		String token = SkieerTokenUtils.getToken();
		int offset = 0;
		int size = 500;
		for (int i = 0; i < housePictaskIds.length; i++) {
//			houseTypeImgToHbase(crawlTable, token, offset, size,i);
			housePicImgtoHbase(crawlTable, token, offset, size,i);
		}
		crawlTable.close();
		connection.close();
		System.out.println("================ 2.0链家数据整理任务 结束 ================");
	}

	private static void housePicImgtoHbase(Table crawlTable, String token, int offset, int size, int i) throws Exception{
		// 2.获取数据
		while (true) {
			Map<String, Object> map = dataSync(housePictaskIds[i], token, offset, size);
			JSONArray dataList = (JSONArray) map.get("dataList");
			int restTotal = Integer.parseInt((String) map.get("restTotal"));
			// 3.将数据写入数据库中
			putPicInData(dataList,crawlTable);
			if (restTotal > 0) {
				offset = offset + size;
			} else {
				break;
			}
		}
	}

	private static void putPicInData(JSONArray dataList, Table crawlTable) throws Exception {
		long start = System.currentTimeMillis();
		CloseableHttpClient client =null;
		try {
			client =   HttpClients.createDefault();
			ArrayList<Put> list = new ArrayList<Put>();
			for (int i = 0; i < dataList.size(); i++) {
				JSONObject housedata = dataList.getJSONObject(i);
				String imgUrl = housedata.getString("图片url");
				if (null == imgUrl) {
					continue;
				}
				Get get = new Get(Bytes.toBytes(imgUrl));
				get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
				Result result = crawlTable.get(get);
				byte[] val = result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
				if (val != null && val.length >0) {
					continue;
				}
				//InputStream in = DownloadUtils.httpGetImg(client, imgUrl);
				//InputStream in = httpGetImg(client, imgUrl);
				//if(in == null)continue;
				//byte[] bytes = toByteArray(in);
				byte[] bytes = httpGetImg(client, imgUrl);
				if(bytes == null)continue;
				Put crawlPut =  new Put(Bytes.toBytes(imgUrl));
				crawlPut.addColumn(Bytes.toBytes(IMG_FAMILY), Bytes.toBytes(FIELD_IMG_CONTENT), bytes);
				crawlPut.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"), Bytes.toBytes("true"));
				list.add(crawlPut);
			}
			crawlTable.put(list);
			System.out.println("put times:" + (System.currentTimeMillis() - start));
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(client!=null){
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*private static void houseTypeImgToHbase(Table crawlTable, String token, int i, int size, int offset) throws Exception {
		// 2.获取数据
		while (true) {
			Map<String, Object> map = dataSync(housetypePictaskIds[i], token, offset, size);
			JSONArray dataList = (JSONArray) map.get("dataList");
			int restTotal = Integer.parseInt((String) map.get("restTotal"));
			// 3.将数据写入数据库中
			putInData(dataList,crawlTable);
			if (restTotal > 0) {
				offset = offset + size;
			} else {
				break;
			}
		}
	}*/
	
	/*private static void putInData(JSONArray dataList, Table crawlTable) throws Exception {
		long start = System.currentTimeMillis();
		ArrayList<Put> list = new ArrayList<Put>();
		for (int i = 0; i < dataList.size(); i++) {
			JSONObject housedata = dataList.getJSONObject(i);
			String imgUrl = housedata.getString("户型图");
			if (null == imgUrl) {
				continue;
			}
			*//*String changeUrl = null;
			if (imgUrl.contains("ljcdn.com/")) {
				int index = imgUrl.indexOf("ljcdn.com/") + 9;
				changeUrl = imgUrl.replace(imgUrl.substring(0, index), "D:/lianjia/");
			}
			if (null == changeUrl || !new File(changeUrl).exists()) {
				continue;
			}*//*
			Get get = new Get(Bytes.toBytes(imgUrl));
	        get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
	        Result result = crawlTable.get(get);
	        byte[] val = result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
	        if (val != null && val.length >0) {
				continue;
			}
			byte[] bytes = inputStream2ByteArray(changeUrl);
			Put crawlPut =  new Put(Bytes.toBytes(imgUrl));
			crawlPut.addColumn(Bytes.toBytes(IMG_FAMILY), Bytes.toBytes(FIELD_IMG_CONTENT), bytes);
			crawlPut.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"), Bytes.toBytes("true"));
			list.add(crawlPut);
		}
		crawlTable.put(list);
		System.out.println("put times:" + (System.currentTimeMillis() - start));
	}*/

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
	
	/**
	 * 
	 * @Title: DataSync
	 * @Description: 获取数据方法
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> dataSync(String taskId, String Token, int offset, int size) throws Exception {
		// 发送请求调用八爪鱼数据接口
		// 创建http GET请求
		HttpRequestBase httpGet = new HttpGet("http://dataapi.bazhuayu.com/api/alldata/GetDataOfTaskByOffset?taskId="
				+ taskId + "&offset=" + String.valueOf(offset) + "&size=" + String.valueOf(size));
		httpGet.setHeader("Authorization", "bearer " + Token);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 执行请求
			response = httpclient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				// 获取服务端返回的数据
				String content = EntityUtils.toString(response.getEntity(), "UTF-8");
				// System.out.println(content);
				JSONObject jsonObj = JSON.parseObject(content);
				JSONObject data = (JSONObject) jsonObj.get("data");
				if (data != null) {
					JSONArray dataList = data.getJSONArray("dataList");
					String restTotal = data.getString("restTotal");
					map.put("dataList", dataList);
					map.put("restTotal", restTotal);
					return map;
				} else {
					System.out.println("八爪鱼接口调取异常");
				}
			} else {
				System.out.println("链接失败2");
			}
		} finally {
			if (response != null) {
				response.close();
			}
			// 相当于关闭浏览器
			httpclient.close();
		}
		return null;

	}

	public static byte[] httpGetImg(CloseableHttpClient client,String imgUrl) {
		// 发送get请求
		HttpGet request = new HttpGet(imgUrl);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(50000).setConnectTimeout(50000).build();
		//设置请求头
		request.setHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1" );
		request.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = client.execute(request);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();

				InputStream in = entity.getContent();
				byte[] bytes = toByteArray(in);
				in.close();
				return bytes;
				  /*FileUtils.copyInputStreamToFile(in, new File(savePath));
				  System.out.println("下载图片成功:"+imgUrl);*/
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			request.releaseConnection();
		}
		return null;
	}

}
