package com.lx.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lx.picToHbase.HbaseUtils;
 
/**
 *
 * Description: Excel操作
 * 
 * CreateTime: 2017年12月11日  下午3:08:09
 *
 * Change History:
 *
 *        Date             CR Number              Name              Description of change
 *
 */
public class ExcelUtil {
 
	private static final String EXCEL_XLS = "xls";  
    private static final String EXCEL_XLSX = "xlsx";  
  
    /** 
     * 判断Excel的版本,获取Workbook 
     * @param in 
     * @param filename 
     * @return 
     * @throws IOException 
     */  
    public static Workbook getWorkbok(InputStream in,File file) throws IOException{  
        Workbook wb = null;  
        if(file.getName().endsWith(EXCEL_XLS)){  //Excel 2003  
            wb = new HSSFWorkbook(in);  
        }else if(file.getName().endsWith(EXCEL_XLSX)){  // Excel 2007/2010  
            wb = new XSSFWorkbook(in);  
        }  
        return wb;  
    }  
  
    /** 
     * 判断文件是否是excel 
     * @throws Exception  
     */  
    public static void checkExcelVaild(File file) throws Exception{  
        if(!file.exists()){  
            throw new Exception("文件不存在");  
        }  
        if(!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))){  
            throw new Exception("文件不是Excel");  
        }  
    }  
    
    /** 
     * 读取Excel测试，兼容 Excel 2003/2007/2010 
     * @throws Exception  
     */  
    public static void main(String[] args) throws Exception {  
        CloseableHttpClient client =null;
        String logPath="D:/log.txt";
        String logContent = null;
        Log_Exception log = new Log_Exception();
        try {  
            // 同时支持Excel 2003、2007  
            File excelFile = new File("d:/undownload.xlsx"); // 创建文件对象  
            FileInputStream in = new FileInputStream(excelFile); // 文件流  
            checkExcelVaild(excelFile);  
            Workbook workbook = getWorkbok(in,excelFile);  
            //Workbook workbook = WorkbookFactory.create(is); // 这种方式 Excel2003/2007/2010都是可以处理的  
  
//            int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量  
            /** 
             * 设置当前excel中sheet的下标：0开始 
             */  
            Sheet sheet = workbook.getSheetAt(0);   // 遍历第一个Sheet  
//            Sheet sheet = workbook.getSheetAt(2);   // 遍历第三个Sheet  
            
            //获取总行数
//          System.out.println(sheet.getLastRowNum());
            
            
            //----//
            Connection connection = HbaseUtils.getConnection();
    		Table crawlTable = connection.getTable(TableName.valueOf("crawl_img_bzy"));
    		//----//
    		
            // 为跳过第一行目录设置count  
            int count = 0;
            for (Row row : sheet) {
            	try {
            		// 跳过第一和第二行的目录  
                    if(count < 2 ) {
                        count++;  
                        continue;  
                    }
                    
                    //如果当前行没有数据，跳出循环  
                    if(row.getCell(0).toString().equals("")){  
                    	return;
                    }
                    
                    //获取总列数(空格的不计算)
//                    int columnTotalNum = row.getPhysicalNumberOfCells();
//                    System.out.println("总列数：" + columnTotalNum);
                    
//                    System.out.println("最大列数：" + row.getLastCellNum());
                    
                    //for循环的，不扫描空格的列
//                    for (Cell cell : row) { 
//                    	System.out.println(cell);
//                    }
                    int end = row.getLastCellNum();
                    for (int i = 0; i < end; i++) {
                    	Cell cell = row.getCell(i);
                    	if(cell == null) {
                    		System.out.println("null");
                    		continue;
                    	}
                    	String imgUrl = cell.getStringCellValue();
                    	if (imgUrl.contains("ljcdn.com/")) {
        					int index = imgUrl.indexOf("ljcdn.com/") + 9;
        					String changeUrl = imgUrl.replace(imgUrl.substring(0, index), "D:/lianjia_undownload");
        					
        					if (null == changeUrl || !new File(changeUrl).exists()) {
        						continue;
        					}
        					long start = System.currentTimeMillis();
        					Get get = new Get(Bytes.toBytes(imgUrl));
        			        get.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
        			        Result result = crawlTable.get(get);
        			        byte[] val = result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"));
        			        if (val != null && val.length >0) {
        						continue;
        					}
        			        System.out.println(changeUrl);
        					byte[] bytes = inputStream2ByteArray(changeUrl);
        					Put crawlPut =  new Put(Bytes.toBytes(imgUrl));
        					crawlPut.addColumn(Bytes.toBytes("cfimg"), Bytes.toBytes("content"), bytes);
        					crawlPut.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("fileSaveFlag"), Bytes.toBytes("true"));
        					crawlTable.put(crawlPut);
        					System.out.println("put times:" + (System.currentTimeMillis() - start));
        					
//    						client =   HttpClients.createDefault();
//    							httpGetImg(client,imgUrl,changeUrl);
                    	}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logContent=e.getClass().getName()+"  error Info  "+e.getMessage();
					log.writeEror_to_txt(logPath, logContent);
				}
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{
			if(client!=null){
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
    
    public static void httpGetImg(CloseableHttpClient client,String imgUrl,String savePath) {
		
		 
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
				  
				  FileUtils.copyInputStreamToFile(in, new File(savePath));
				  System.out.println("下载图片成功:"+imgUrl);
				 
			 }
			 
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			request.releaseConnection();
			
		}
	}
 
	public static void mkDir(File file) {
		if (file.getParentFile().exists()) {
			file.mkdir();
		} else {
			mkDir(file.getParentFile());
			file.mkdir();
		}
	}
}
