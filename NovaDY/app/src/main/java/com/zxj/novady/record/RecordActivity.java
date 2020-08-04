package com.zxj.novady.record;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.CameraView;
import androidx.core.app.ActivityCompat;

import com.zxj.novady.R;
import com.zxj.novady.utils.TransTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// 录制活动,主要使用androidx的CameraView完成
public class RecordActivity extends AppCompatActivity {
    CameraView cameraView;
    ImageButton ib_lens;    // 前后摄像头切换按钮
    ImageButton ib_shutter; // 快门按钮
    ImageButton ib_torch;   // 闪光灯按钮
    TextView tv_timer;      // 拍摄计时器
    File myRecordsDir;      // 视频保存目录
    File myCoversDir;       // 封面保存目录

    // 点击动画集
    AnimatorSet animatorSet;

    // 计时
    private Handler handler;
    private Runnable run;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_record);

        // 创建/获取目录,视频位于NovaDY/Videos/Records
        // 缩略图(取第一帧)位于NovaDY/Covers/Records
        File storageFile = this.getExternalFilesDir("NovaDY");
        Log.d("zxjjj", storageFile.toString());
        if(!storageFile.exists())
            storageFile.mkdir();
        myRecordsDir = new File(storageFile,"Videos/Records");
        Log.d("zxjjj", myRecordsDir.toString());
        if (!myRecordsDir.exists())
            myRecordsDir.mkdirs();
        myCoversDir = new File(storageFile, "Covers/Records");
        if(!myCoversDir.exists())
            myCoversDir.mkdirs();

        cameraView = findViewById(R.id.view_camera);
        ib_lens = findViewById(R.id.change_lens);
        ib_shutter = findViewById(R.id.shutter);
        ib_torch = findViewById(R.id.torch);
        tv_timer = findViewById(R.id.tv_timer);

        // 拍摄计时器,为了减少处理时间误差,缩短了每次响应的时间
        handler = new Handler();
        run = new Runnable() {
            long curTime;
            @Override
            public void run() {
                curTime = TransTime.transStr((String) tv_timer.getText());
                curTime++;
                tv_timer.setText(TransTime.transSec(curTime));
                handler.postDelayed(run, 970);
            }
        };

        // 在拍摄前检查CAMERA权限,实际上在启动时就已经赋予,因此只是为了避免warning和error
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 将摄像头与该活动的生命周期绑定
        cameraView.bindToLifecycle(this);
        // 设置模式为拍摄
        cameraView.setCaptureMode(CameraView.CaptureMode.VIDEO);
        // 允许手指缩放
        cameraView.setPinchToZoomEnabled(true);;

    }

    // onTouch方法,切换前后(前置无闪光灯,隐藏闪光灯按钮)
    public void changeLens(View view) {
        cameraView.toggleCamera();
        if(cameraView.getCameraLensFacing() == CameraX.LensFacing.BACK)
            ib_torch.setVisibility(View.VISIBLE);
        else {
            cameraView.enableTorch(false);
            ib_torch.setVisibility(View.INVISIBLE);
        }
    }

    // onTouch方法,开启闪光灯
    public void ocTorch(View view) {
        if(cameraView.isTorchOn())
            cameraView.enableTorch(false);
        else
            cameraView.enableTorch(true);
    }

    // onTouch方法,点击录制/结束录制视频
    public void recordVideos(View view) throws IOException {
        // 如果点击时正在录制,则结束录制,否则开始录制
        if(!cameraView.isRecording()) {
            // 点击动画
            recordStartAnim();
            // 开始后禁止切换前后镜头
            ib_lens.setVisibility(View.INVISIBLE);
            // 初始化计时器
            tv_timer.setText("00:00");
            tv_timer.setVisibility(View.VISIBLE);
            tv_timer.bringToFront();
            handler.post(run);

            // 开始录制
            cameraView.startRecording(startRecordingFilePath(), new VideoCapture.OnVideoSavedListener(){
                @Override
                public void onVideoSaved(@NonNull File file) {
                    // 视频保存后保存封面
                    saveVideoCover(file, myCoversDir);
                }

                @Override
                public void onError(@NonNull VideoCapture.VideoCaptureError videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                    Log.d("RecordError", message);
                }
            });
        }
        else {
            // 停止动画
            recordStopAnim();
            // 停止并隐藏计时器,显示切换镜头按钮
            ib_lens.setVisibility(View.VISIBLE);
            tv_timer.setVisibility(View.INVISIBLE);
            handler.removeCallbacks(run);
            // 停止录制
            cameraView.stopRecording();
        }
    }

    /*  使用系统时间作为文件名称    */
    private File startRecordingFilePath() throws IOException {
        String path = myRecordsDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp4";
        File file = new File(path);
        if(!file.exists())
            file.createNewFile();
        return file;
    }

    /*  开始动画    */
    private void recordStartAnim(){
        ib_shutter.setImageResource(R.mipmap.ic_center_focus_strong_white_48dp);
        ObjectAnimator sxAnimator = ObjectAnimator.ofFloat(ib_shutter, "scaleX", 1.1f, 0.9f);
        sxAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sxAnimator.setInterpolator(new LinearInterpolator());
        sxAnimator.setDuration(2000);
        sxAnimator.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator syAnimator = ObjectAnimator.ofFloat(ib_shutter, "scaleY", 1.1f, 0.9f);
        syAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syAnimator.setInterpolator(new LinearInterpolator());
        syAnimator.setDuration(2000);
        syAnimator.setRepeatMode(ValueAnimator.REVERSE);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(sxAnimator, syAnimator);
        animatorSet.start();
    }
    /*  停止动画    */
    private void recordStopAnim(){
        animatorSet.cancel();
        animatorSet = null;
        ib_shutter.setImageResource(R.mipmap.ic_movie_white_48dp);
        ObjectAnimator sxAnimator = ObjectAnimator.ofFloat(ib_shutter, "scaleX", 0, 1.0f);
        sxAnimator.setRepeatCount(0);
        sxAnimator.setInterpolator(new LinearInterpolator());
        sxAnimator.setDuration(600);
        ObjectAnimator syAnimator = ObjectAnimator.ofFloat(ib_shutter, "scaleY", 0, 1.0f);
        syAnimator.setRepeatCount(0);
        syAnimator.setInterpolator(new LinearInterpolator());
        syAnimator.setDuration(600);
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(sxAnimator, syAnimator);
        animatorSet.start();
    }

    /*  使用MediaMetadataRetriever和Bitmap获取视频第一帧,并将bitmap压缩保存为png */
    private void saveVideoCover(File videoFile, File target){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoFile.getPath());
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();

        File saveFile = new File(target.getPath(), videoFile.getName().replace(".mp4", ".png"));
        try {
            FileOutputStream saveImgOut = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, saveImgOut);
            saveImgOut.flush();
            saveImgOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
