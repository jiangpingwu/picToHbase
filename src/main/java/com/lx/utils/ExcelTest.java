package com.lx.utils;

import com.lx.picToHbase.HbaseUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @ClassName: ExcelTest
 * @description: TODO
 * @author: alan
 * @date: 2018-12-06 10:40
 * @version: V1.0
 **/
public class ExcelTest {

    public static void main(String[] args) throws Exception{
        try {
            // 同时支持Excel 2003、2007
            File excelFile = new File("d:/undownload.xls"); // 创建文件对象
            FileInputStream in = new FileInputStream(excelFile); // 文件流
            ExcelUtil.checkExcelVaild(excelFile);
            Workbook workbook = ExcelUtil.getWorkbok(in,excelFile);
            //Workbook workbook = WorkbookFactory.create(is); // 这种方式 Excel2003/2007/2010都是可以处理的

//            int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量
            /**
             * 设置当前excel中sheet的下标：0开始
             */
            Sheet sheet = workbook.getSheetAt(0);   // 遍历第一个Sheet
            //Sheet sheet = workbook.getSheetAt(2);   // 遍历第三个Sheet

            //获取总行数
          System.out.println(sheet.getLastRowNum());
          HSSFCell cell1 = (HSSFCell) sheet.getRow(0).getCell(0);
            //System.out.println("左上端单元是： " + cell1.getStringCellValue());
            // 为跳过第一行目录设置count
            int count = 0;
            for (Row row : sheet) {
                try {
                    // 跳过第一和第二行的目录
                    /*if(count < 2 ) {
                        count++;
                        continue;
                    }*/
                    //如果当前行没有数据，跳出循环
                    /*if(row.getCell(0).toString().equals("")){
                        return;
                    }*/

                    //获取总列数(空格的不计算)
                    int columnTotalNum = row.getPhysicalNumberOfCells();
                    System.out.println("总列数：" + columnTotalNum);

                    System.out.println("最大列数：" + row.getLastCellNum());

                    //for循环的，不扫描空格的列
                    for (Cell cell : row) {
                    	System.out.println("cell="+cell);
                    }
                    /*int end = row.getLastCellNum();
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
//    						client =   HttpClients.createDefault();
//    							httpGetImg(client,imgUrl,changeUrl);
                        }
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
