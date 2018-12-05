package com.lx.picToHbase;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class PicToHbaseMain {
	public static Logger logger = LogManager.getLogger(PicToHbaseMain.class.getName());
	//20个城市*3个任务ID*每个任务ID10000条数据
	//任务组1
	private static String[] taskGroup1 = {"f81d06bd-3e4f-4ec9-90ca-7f4ef9927388"};
	//任务组2
	private static String[] taskGroup2 = {"f81d06bd-3e4f-4ec9-90ca-7f4ef9927388"};
	//任务组3
	private static String[] taskGroup3 = {"f81d06bd-3e4f-4ec9-90ca-7f4ef9927388"};

	public static void main(String[] args) throws Exception {
		logger.info("================ 2.0链家数据整理任务 开始 ================");
		// 1.先获取Token
		String token = SkieerTokenUtils.getToken();
		logger.info("初始化参数, taskGroup1={}, taskGroup2={}, taskGroup3={}, token={}",
				JSON.toJSONString(taskGroup1), JSON.toJSONString(taskGroup2), JSON.toJSONString(taskGroup3), token);
		int offset = 0;
		int size = 500;
		for (int i = 0; i < taskGroup1.length; i++) {
			NewPicToHbase newPicToHbase = new NewPicToHbase(token, offset, size, taskGroup1[i]);
			new Thread(newPicToHbase).start();
		}
		for (int i = 0; i < taskGroup2.length; i++) {
			NewPicToHbase newPicToHbase = new NewPicToHbase(token, offset, size, taskGroup2[i]);
			new Thread(newPicToHbase).start();
		}
		for (int i = 0; i < taskGroup3.length; i++) {
			NewPicToHbase newPicToHbase = new NewPicToHbase(token, offset, size, taskGroup3[i]);
			new Thread(newPicToHbase).start();
		}
		logger.info("================ 2.0链家数据整理任务 结束 ================");
	}
}
