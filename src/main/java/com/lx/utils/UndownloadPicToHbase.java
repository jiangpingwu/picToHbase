package com.lx.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UndownloadPicToHbase {
    public static void main(String[] args) throws Exception{
        String a = String.valueOf('\u0000'); //控制台输出的是空格，\u0000 表示的是Unicode值
        System.out.println("a的Unicode值：" + Integer.toHexString(a.charAt(0)));// \u0000
        String b = " "; // 空格字符串
        String c = ""; //空字符串
        String d = null; //没有任何指向的字符串引用
        String e = "null"; //null字符串，这个null是常量池里的
        System.out.println(a + ";" + b + ";" +c + ";" + d + ";" + e + ";");
        System.out.println("a.equals(c):" + a.equals(c)); // false
        System.out.println("a.equals(b):" + a.equals(b)); // false
        System.out.println("a == c:" + (a == c)); // false
        System.out.println("a == d:" + (d == a)); // false
        System.out.println("a.equals(e):" + a.equals(e)); // false
    }
}
