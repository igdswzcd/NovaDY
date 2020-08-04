package com.zxj.novady.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/*  api接口，从网络获取视频链接、封面链接等信息 */
public interface ApiService {
    // https://beiyou.bytedance.com/api/invoke/video/invoke/video
    @GET("api/invoke/video/invoke/video")
    Call<List<VideoResponse>> getVideos();


}
