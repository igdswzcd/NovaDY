package com.zxj.novady.utils;

import android.util.Log;

public class TransTime {
    /**
     * @param sec 毫秒数
     * @return  12:34 格式的时间字符串
     */
    public static String transSec(long sec){
//        sec /= 1000;
        long minutes = sec / 60;
        sec -= minutes * 60;
        String str = (minutes>9?"":"0") + minutes + ":" + (sec>9?"":"0") + sec;
        return str;
    }

    /**
     * @param str 12:34格式的时间字符串
     * @return  毫秒数
     */
    public static long transStr(String str){
        return Integer.parseInt(str.split(":")[0])*60+Integer.parseInt(str.split(":")[1]);
    }
}
