package com.lx.picToHbase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewPicToHbase implements Runnable{
	public static Logger logger = LogManager.getLogger(NewPicToHbase.class);

	private static final String IMG_FAMILY = "cfimg";
	private static final String FIELD_IMG_CONTENT = "content";
	public static final String IMG_TABLE = "crawl_img_ajk";

	private Connection connection;
	private Table crawlTable;
	private String token;
	private int offset = 0;
	private int size = 0;
	private String taskId;

	public NewPicToHbase(String token, int offset, int size, String taskId){
		try {
			this.connection = HbaseUtils.getConnection();
			this.crawlTable = this.connection.getTable(TableName.valueOf(IMG_TABLE));
			this.token = token;
			this.offset = offset;
			this.size = size;
			this.taskId = taskId;
		}catch (Exception e){
			logger.error("NewPicToHbase初始化失败:", e);
			throw new RuntimeException("NewPicToHbase初始化失败", e);
		}
	}

	public void housePicImgtoHbase(){
		try {
			long begin = System.currentTimeMillis();
			logger.debug("线程={}, 当前任务ID={}开始抓取",
					Thread.currentThread().getName(), taskId);
			// 2.获取数据
			while (true) {
				long start = System.currentTimeMillis();
				logger.debug("线程={}, 当前任务ID={},获取开始 offset={}, size={}",
						Thread.currentThread().getName(), taskId, offset, size);
				Map<String, Object> map = dataSync(taskId, token, offset, size);
				if(map == null){
					continue;
				}
				JSONArray dataList = (JSONArray) map.get("dataList");
				int restTotal = Integer.parseInt((String) map.get("restTotal"));
				// 3.将数据写入数据库中
				putPicInData(dataList,crawlTable, taskId, offset, size);
				logger.debug("线程={}, 当前任务ID={},获取完成 offset={}, size={}, 耗时={}秒",
						Thread.currentThread().getName(), taskId, offset, size, (System.currentTimeMillis() - start)*0.001);
				if (restTotal > 0) {
					offset = offset + size;
				} else {
					/*crawlTable.close();
					connection.close();*/
					logger.debug("线程={}, 当前任务ID={}抓取完成, 总耗时={}秒",
							Thread.currentThread().getName(), taskId,(System.currentTimeMillis() - begin)*0.001);
					break;
				}
			}
		}catch (Exception e){
			logger.error("housePicImgtoHbase 执行异常:", e);
		}finally {
			try {
				if(crawlTable != null){
					crawlTable.close();
				}
				if(connection != null){
					connection.close();
				}
			}catch (Exception e){
				logger.error("housePicImgtoHbase crawlTable.close()或connection.close()执行异常:", e);
			}
		}
	}

	/**
	 *
	 * @Title: DataSync
	 * @Description: 获取数据方法
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> dataSync(String taskId, String Token, int offset, int size){
		// 发送请求调用八爪鱼数据接口
		// 创建http GET请求
		String url = "http://dataapi.bazhuayu.com/api/alldata/GetDataOfTaskByOffset?taskId="
				+ taskId + "&offset=" + String.valueOf(offset) + "&size=" + String.valueOf(size);
		HttpRequestBase httpGet = new HttpGet(url);
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
				logger.debug("线程={}, 当前任务ID={}, offset={}, size={}, 八爪鱼接口调用返回数据={}",
						Thread.currentThread().getName(), taskId, offset, size, content);
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
					logger.error("线程={}, 当前任务ID={}, offset={}, size={}, url={}, 八爪鱼接口调用返回数据为空",
							Thread.currentThread().getName(), taskId, offset, size, url);
				}
			} else {
				logger.error("线程={}, 当前任务ID={}, offset={}, size={}, url={}, 八爪鱼接口请求失败",
						Thread.currentThread().getName(), taskId, offset, size, url);
			}
		}catch (Exception e){
			logger.error("线程={}, 当前任务ID={}, offset={}, size={}, url={}, 八爪鱼接口请求异常:",
					Thread.currentThread().getName(), taskId, offset, size, url, e);
		}finally {
			try {
				if (response != null) {
					response.close();
				}
				// 相当于关闭浏览器
				httpclient.close();
			}catch (Exception e){
				logger.error("线程={}, 当前任务ID={}, offset={}, size={}, url={}, 八爪鱼接口请求成功, response.close()或httpclient.close()发生异常:",
						Thread.currentThread().getName(), taskId, offset, size, url, e);
			}
		}
		return null;
	}

	private void putPicInData(JSONArray dataList, Table crawlTable, String taskId, int offset, int size){
		//long start = System.currentTimeMillis();
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
				byte[] bytes = httpGetImg(client, imgUrl);
				if(bytes == null){
					logger.error("线程={}, 当前任务ID={}, offset={}, size={}, dataList={}, imgUrl={}httpGetImg失败",
							Thread.currentThread().getName(), taskId, offset, size, dataList, imgUrl);
					continue;
				}
				Put crawlPut =  new Put(Bytes.toBytes(imgUrl));
				crawlPut.addColumn(Bytes.toBytes(IMG_FAMILY), Bytes.toBytes(FIELD_IMG_CONTENT), bytes);
				crawlPut.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"), Bytes.toBytes("true"));
				list.add(crawlPut);
			}
			crawlTable.put(list);
			logger.debug("线程={}, 当前任务ID={}, offset={}, size={}, dataList={}, list={}",
					Thread.currentThread().getName(), taskId, offset, size, dataList, list);
		}catch (Exception e){
			logger.error("线程={}, 当前任务ID={}, offset={}, size={}, dataList={}, 执行异常:",
					Thread.currentThread().getName(), taskId, offset, size, dataList, e);
		}finally {
			if(client!=null){
				try {
					client.close();
				} catch (IOException e) {
					logger.error("线程={}, 当前任务ID={}, offset={}, size={}, dataList={}, putPicInData, client.close()执行异常:",
							Thread.currentThread().getName(), taskId, offset, size, dataList, e);
				}
			}
		}
	}

	public static byte[] httpGetImg(CloseableHttpClient client,String imgUrl) throws Exception{
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			request.releaseConnection();
		}
		return null;
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

	@Override
	public void run() {
		housePicImgtoHbase();
	}
}
