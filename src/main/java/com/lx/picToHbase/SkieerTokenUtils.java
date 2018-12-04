package com.lx.picToHbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
* @ClassName: SkieerTokenUtils 
* @Description:获取八爪鱼接口token工具
* @author 张新磊  
* @date 2018年5月21日
 */
public class SkieerTokenUtils {

	
	public static String getToken() throws Exception {
		//获取token令牌
		String token = null;
		// 创建Httpclient对象  
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        // 创建http POST请求  
        HttpPost httpPost = new HttpPost("http://dataapi.bazhuayu.com/token");  
        // 设置3个post参数
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();  
        parameters.add(new BasicNameValuePair("username", "zxl0016"));
        parameters.add(new BasicNameValuePair("password", "zxl1024"));
        parameters.add(new BasicNameValuePair("grant_type", "password"));
        // 构造一个form表单式的实体  
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);  
        // 将请求实体设置到httpPost对象中  
        httpPost.setEntity(formEntity);  
        // 伪装浏览器请求  
        httpPost.setHeader(  
                "User-Agent",  
                "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");  
        CloseableHttpResponse response = null;  
            // 执行请求  
            response = httpclient.execute(httpPost);  
            // 判断返回状态是否为200  
            if (response.getStatusLine().getStatusCode() == 200) {  
                // 获取服务端响应的数据  
                String content = EntityUtils.toString(response.getEntity(),  
                        "UTF-8");
                JSONObject jsonObj=JSON.parseObject(content);
                token = jsonObj.getString("access_token");
//		                System.out.println(token);  
            }else {
				System.out.println("链接失败1");
			}  
		return token;
	}
}
