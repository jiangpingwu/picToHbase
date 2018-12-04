package com.lx.picToHbase;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class PicToHbaseMain {
	public static Logger logger = LogManager.getLogger(PicToHbaseMain.class.getName());

	// 户型图片任务ID
	private static String[] housetypePictaskIds = {""};
	// 相册图片任务ID
	private static String[] housePictaskIds = {"f81d06bd-3e4f-4ec9-90ca-7f4ef9927388"};

	public static void main(String[] args) throws Exception {
		logger.info("================ 2.0链家数据整理任务 开始 ================");
		// 1.先获取Token
		String token = SkieerTokenUtils.getToken();
		logger.info("初始化参数, housetypePictaskIds={}, housePictaskIds={}, token={}",
				JSON.toJSONString(housetypePictaskIds), JSON.toJSONString(housePictaskIds), token);
		int offset = 0;
		int size = 500;
		//相册图
		for (int i = 0; i < housePictaskIds.length; i++) {
			NewPicToHbase newPicToHbase = new NewPicToHbase(token, offset, size, housePictaskIds[i]);
			new Thread(newPicToHbase).start();
		}
		//户型图
		/*for (int i = 0; i < housetypePictaskIds.length; i++) {
			NewPicToHbase newPicToHbase = new NewPicToHbase(token, offset, size, housetypePictaskIds[i]);
			new Thread(newPicToHbase).start();
		}*/
		logger.info("================ 2.0链家数据整理任务 结束 ================");
	}
}
