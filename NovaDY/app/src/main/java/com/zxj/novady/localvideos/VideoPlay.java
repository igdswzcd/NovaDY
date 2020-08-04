package com.zxj.novady.localvideos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zxj.novady.R;
/*  播放本地视频,因为相关功能在首页视频播放中已经完成,所以除了VideoView没有添加其余控件 */
public class VideoPlay extends AppCompatActivity {
    private VideoView videoView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_video_item_play);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        videoView = findViewById(R.id.local_video_item_play_vv);
        videoView.setVideoPath(url);
        videoView.start();
    }
}
