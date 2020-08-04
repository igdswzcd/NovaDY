package com.zxj.novady.utils;

import android.media.MediaMetadataRetriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*  一些工具方法  */
public class MyUtils {

    /**
     * @param count 点赞数
     * @return  以123,1.2k,12.3m,123.4b为样例格式的点赞数缩略字符串
     */
    public static String transCountNumber(long count) {
        String tmp = String.valueOf(count);
        String result = tmp;
        int length = tmp.length();
        if(length > 9)
            result = tmp.substring(0, length-9) + "." + tmp.charAt(length-9) + "b";
        else if(length > 6)
            result = tmp.substring(0, length-6) + "." + tmp.charAt(length-6) + "m";
        else if(length > 3)
            result = tmp.substring(0, length-3) + "." + tmp.charAt(length-3) + "k";
        return result;
    }

    /*  通过url获取网络视频长宽,主要使用了MediaMetadataRetriever,返回包含长宽信息的列表   */
    private static MediaMetadataRetriever mediaMetadataRetriever;
    public static List<Integer> getNetVideoWidthAndHeight(String url) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
        Callable myCallable = () -> {
            float videoWidth = 0;
            float videoHeight = 0;
            try {
                videoWidth = Float.parseFloat(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                videoHeight = Float.parseFloat(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            } catch (Exception e) {
                videoWidth = 0;
                videoHeight = 0;
            } finally {
                mediaMetadataRetriever.release();
                List<Integer> temp = new ArrayList<Integer>();
                temp.add((int)videoWidth);
                temp.add((int)videoHeight);
                return temp;
            }
        };

        return (List<Integer>) executor.submit(myCallable).get();
    }

}
