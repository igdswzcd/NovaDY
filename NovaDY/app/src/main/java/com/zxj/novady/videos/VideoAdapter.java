package com.zxj.novady.videos;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zxj.novady.R;
import com.zxj.novady.utils.DoubleClickListener;
import com.zxj.novady.utils.MyUtils;
import com.zxj.novady.api.VideoResponse;
import com.zxj.novady.utils.TransTime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private List<VideoResponse> list = new ArrayList<>();
    private Context context;

    private SharedPreferences sharedPreferences;
    // 一些设置关键词
    private static final String KEY_AUTO_PLAY = "自动播放";
    private static final String KEY_AUTO_REPLAY = "重复播放";
    private static final String KEY_FF_COVER = "网络视频第一帧代替封面";
    private static final String KEY_AUTO_DOWN = "自动下载视频";
    private static final String KEY_CUT_VERTICAL = "裁剪竖向视频";

    // seekbar实时更新相关
    boolean hasRun = false;
    Handler handler;
    Runnable run;

    public void setList(List<VideoResponse> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public void setContext(Context context) {
        this.context = context;
        // 读取设置文件
        sharedPreferences = context.getSharedPreferences("novady_settings", Context.MODE_PRIVATE);
    }


    /*  离开Window时对VideoView进行收尾 */
    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        holder.detach();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(list.get(position), position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;                // 视频播放
        private ImageView imageView;                // 封面
        private ConstraintLayout constraintLayout;  // 控件组(整合方便拖动进度条时隐藏)
        private TextView tv_nickname;               // 视频标题
        private TextView tv_description;            // 视频描述
        private TextView tv_likecnt;                // 点赞数
        private SeekBar sb_video;                   // 进度条
        private TextView tv_video_time;             // 进度条时间
        private ImageButton ib_play;                // 暂停图标

        private ImageButton ib_cmt;                         // 评论按钮
        private BottomSheetDialog bottomSheetDialog;        // 使用BottomSheetDialog实现弹出评论界面
        private BottomSheetBehavior bottomSheetBehavior;
        private RecyclerView bsbRV;                         // 评论rv
        private CommentAdapter commentAdapter;
        private EditText editComment;                       // 评论输入
        private ImageButton publishComment;                 // 发布评论
        private ImageView dialog_close;                     // 关闭评论按钮(下滑也可关闭)

        private LottieAnimationView lotv_like;          // Lottie实现点赞(粉色)
        private LottieAnimationView lotv_like_white;    // 未点赞状态(白色)
        private LottieAnimationView lotv_down;          // 下载动画

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.vv);
            imageView = itemView.findViewById(R.id.iv);
            tv_nickname = itemView.findViewById(R.id.tv_nickname);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_likecnt = itemView.findViewById(R.id.tv_likecnt);
            lotv_like = itemView.findViewById(R.id.lotv_like);
            lotv_like.setVisibility(View.INVISIBLE);

            // 设置初始动画进度
            lotv_like_white = itemView.findViewById(R.id.lotv_like_white);
            lotv_like_white.setProgress(1.0f);

            // 设置初始动画进度
            lotv_down = itemView.findViewById(R.id.lotv_down);
            lotv_down.setMaxProgress(0.75f);

            ib_cmt = itemView.findViewById(R.id.btn_cmt);

            sb_video = itemView.findViewById(R.id.sb_video);
            tv_video_time = itemView.findViewById(R.id.tv_video_time);
            ib_play = itemView.findViewById(R.id.ib_play);

            // 控件组移至最顶层
            constraintLayout = itemView.findViewById(R.id.cl);
            constraintLayout.bringToFront();

            // seekbar组(seekbar和时间移至最顶层
            itemView.findViewById(R.id.cl_sb_tv_time).bringToFront();

            // 进度条实时更新,计时
            handler = new Handler();
            run = new Runnable() {
                int curPos, duration;
                @Override
                public void run() {
                    curPos = videoView.getCurrentPosition();
                    duration = videoView.getDuration();
                    int time = curPos * 100 / duration;
                    sb_video.setProgress(time);
                    tv_video_time.setText(TransTime.transSec(curPos/1000)+" / "+TransTime.transSec(duration/1000));
                    handler.postDelayed(run, 1000);
                }
            };
        }

        @SuppressLint("ClickableViewAccessibility")
        void bindData(VideoResponse s, int position){
            // 设置标题(名称),描述,点赞数
            tv_nickname.setText("@" + s.nickname);
            tv_description.setText(s.description);
            tv_likecnt.setText(MyUtils.transCountNumber(s.count));

            // 设置视频url,如果已下载则从本地获取
            String url = s.feedurl.replaceFirst("http", "https");
            if(isDownloadedVideo(url)) {
                videoView.setVideoPath(context.getExternalFilesDir("NovaDY/Videos/Downloads").getAbsolutePath() + "/" +
                        url.split("video/")[1]);
            }else{
                videoView.setVideoURI(Uri.parse(url));
            }
            videoView.requestFocus();
            // 先隐藏视频,显示封面
            videoView.setVisibility(View.INVISIBLE);

            // 初始化评论dialog
            showSheetDialog();

            // 下载监听,动画
            lotv_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(lotv_down.getProgress() == 0) {
                        downloadVideo(url);
                        lotv_down.playAnimation();
                    }
                }
            });

            // 设置第一帧代替封面判定,如果为false则使用Glide从网络获取封面
            if(sharedPreferences.getBoolean(KEY_FF_COVER, false)) {
                // 如果已下载,则从本地读取,否则使用MediaMetadataRetriever网络获取
                if(isDownloadedVideo(url)) {
                    imageView.setImageURI(Uri.parse(context.getExternalFilesDir("NovaDY/Covers/Downloads" +
                            url.split("video")[1].replace(".mp4",".png")).getPath()));
                }
                else {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    media.setDataSource(url, new HashMap<>());
                    Bitmap bitmap = media.getFrameAtTime();
                    imageView.setImageBitmap(bitmap);
                }
            }
            else
                Glide.with(context).load(s.avatar.replaceFirst("http", "https")).into(imageView);


            // 评论按钮监听
            ib_cmt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.show();
                }
            });

            // 自适应屏幕分辨率裁切设置
            if(sharedPreferences.getBoolean(KEY_CUT_VERTICAL, true)){
                autoCut(position, videoView, false);
            }
            else
                autoCut(position, videoView, true);

            // 自动播放选项
            if(sharedPreferences.getBoolean(KEY_AUTO_PLAY, true)){
                imageView.setVisibility(View.INVISIBLE);
                ib_play.setVisibility(View.INVISIBLE);

                videoView.setVisibility(View.VISIBLE);
                videoView.start();
                // 使run唯一
                if(!hasRun) {
                    handler.post(run);
                    hasRun = true;
                }
            }
            else{
                // 不自动播放则点击播放
                imageView.setVisibility(View.VISIBLE);
                ib_play.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.INVISIBLE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageView.setVisibility(View.INVISIBLE);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.start();
                        if(!hasRun) {
                            handler.post(run);
                            hasRun = true;
                        }
                        imageView.setClickable(false);
                    }
                });
            }

            // 播放结束监听
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    handler.removeCallbacks(run);
                    hasRun = false;

                    sb_video.setProgress(0);
                    tv_video_time.setText("00:00 / 00:00");
                    // 重复播放设置
                    if(sharedPreferences.getBoolean(KEY_AUTO_REPLAY, true)) {
                        videoView.start();
                        if (!hasRun) {
                            handler.post(run);
                            hasRun = true;
                        }
                    }
                }
            });

            // seekbar thumb点击显隐动画
            sb_video.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            sb_video.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            sb_video.getThumb().setAlpha(100);
            sb_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int duration = videoView.getDuration();
                    tv_video_time.setText(TransTime.transSec(progress*duration/100000)+" / "+TransTime.transSec(duration/1000));

                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    tv_video_time.setVisibility(View.VISIBLE);

                    videoView.pause();
                    handler.removeCallbacks(run);
                    hasRun = false;

                    // 隐藏主要控件组
                    constraintLayout.setVisibility(View.INVISIBLE);
                    ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(sb_video.getThumb(), "alpha", 150, 255);
                    animatorAlpha.setRepeatCount(0);
                    animatorAlpha.setDuration(200);
                    animatorAlpha.setInterpolator(new LinearInterpolator());
                    animatorAlpha.start();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    constraintLayout.setVisibility(View.VISIBLE);
                    ib_play.setVisibility(View.INVISIBLE);
                    tv_video_time.setVisibility(View.INVISIBLE);
                    videoView.seekTo(seekBar.getProgress()*videoView.getDuration()/100);
                    //Log.d("zxj", "time:"+seekBar.getProgress()*videoView.getDuration()/100);
                    videoView.start();
                    if(!hasRun) {
                        handler.post(run);
                        hasRun = true;
                    }
                    ObjectAnimator animatorAlpha = ObjectAnimator.ofInt(sb_video.getThumb(), "alpha", 255, 150);
                    animatorAlpha.setRepeatCount(0);
                    animatorAlpha.setDuration(1000);
                    animatorAlpha.setInterpolator(new LinearInterpolator());
                    animatorAlpha.start();
                }
            });

            // 视频点击监听,单双击,单击播放/暂停,双击点赞(已点赞则重复动画,取消点赞需要点击点赞图标)
            videoView.setOnTouchListener(new DoubleClickListener(new DoubleClickListener.MyClickCallBack() {
                @Override
                public void singleClick() {
                    if(videoView.isPlaying()) {
                        videoView.pause();
                        handler.removeCallbacks(run);
                        hasRun = false;

                        ib_play.setVisibility(View.VISIBLE);
                        ObjectAnimator sxAnimator = ObjectAnimator.ofFloat(ib_play, "scaleX", 2.5f, 1.5f);
                        sxAnimator.setRepeatCount(0);
                        sxAnimator.setInterpolator(new LinearInterpolator());
                        sxAnimator.setDuration(200);
                        ObjectAnimator syAnimator = ObjectAnimator.ofFloat(ib_play, "scaleY", 2.5f, 1.5f);
                        syAnimator.setRepeatCount(0);
                        syAnimator.setInterpolator(new LinearInterpolator());
                        syAnimator.setDuration(200);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(sxAnimator, syAnimator);
                        animatorSet.start();
                    }
                    else {
                        videoView.start();
                        if(!hasRun) {
                            handler.post(run);
                            hasRun = true;
                            ib_play.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void doubleClick() {
                    if(lotv_like.getVisibility() == View.INVISIBLE){
                        lotv_like_white.setVisibility(View.INVISIBLE);
                        lotv_like.setVisibility(View.VISIBLE);
                        lotv_like.playAnimation();
                    }
                    else {
                        lotv_like.playAnimation();
                    }
                }
            }));

            // 点赞图标监听,单击点赞/取消点赞,两个动画中一个为静态白色♥,表示未点赞,另一个为动态粉色♥,表示点赞
            lotv_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(lotv_like.getVisibility() == View.INVISIBLE){
                        lotv_like_white.setVisibility(View.INVISIBLE);
                        lotv_like.setVisibility(View.VISIBLE);
                        lotv_like.playAnimation();
                    }
                    else {
                        lotv_like_white.setVisibility(View.VISIBLE);
                        lotv_like.setVisibility(View.INVISIBLE);
                    }
                }
            });
            lotv_like_white.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(lotv_like.getVisibility() == View.INVISIBLE){
                        lotv_like_white.setVisibility(View.INVISIBLE);
                        lotv_like.setVisibility(View.VISIBLE);
                        lotv_like.playAnimation();
                    }
                    else {
                        lotv_like_white.setVisibility(View.VISIBLE);
                        lotv_like.setVisibility(View.INVISIBLE);
                    }
                }
            });

            // 检测是否已下载并设置动画进度
            if(isDownloadedVideo(list.get(position).feedurl))
                lotv_down.setProgress(0.75f);
            else {
                lotv_down.setProgress(0);
                if(sharedPreferences.getBoolean(KEY_AUTO_DOWN, false)){
                    downloadVideo(url);
                    lotv_down.setProgress(0.75f);
                }
            }
        }

        // 初始化评论dialog
        private void showSheetDialog(){
            View view = View.inflate(context, R.layout.dialog_bottomsheet, null);
            bsbRV = view.findViewById(R.id.dialog_rv);
            commentAdapter = new CommentAdapter();
            bottomSheetDialog = new BottomSheetDialog(context, R.style.dialog);
            editComment = view.findViewById(R.id.dialog_edit);
            publishComment = view.findViewById(R.id.dialog_publish);
            dialog_close = view.findViewById(R.id.dialog_close);

            // 关闭dialog监听
            dialog_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.cancel();
                }
            });

            // 模拟三条数据
            List<String> comList = new ArrayList<>();
            comList.add("第一条评论");
            comList.add("666啊");
            comList.add("很好看~");
            commentAdapter.setList(comList);
            bsbRV.setHasFixedSize(true);
            bsbRV.setLayoutManager(new LinearLayoutManager(context));
            bsbRV.setAdapter(commentAdapter);

            // 发布按钮监听
            publishComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!editComment.getText().toString().equals("")) {
                        // 发布相当于adapter添加一条数据
                        commentAdapter.addItem(editComment.getText().toString());
                        // 发布后清空editText,强制关闭软键盘,清除焦点(光标)
                        editComment.setText("");
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                        editComment.clearFocus();
                    }
                }
            });


            // 初始化BottomSheetDialog
            bottomSheetDialog.setContentView(view);
            bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
            // 设置评论视图最大高度
            bottomSheetBehavior.setPeekHeight(getPeekHeight());
            // 两种关闭方式——滑动和关闭按钮
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    if(i == BottomSheetBehavior.STATE_HIDDEN){
                        bottomSheetDialog.dismiss();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
                @Override
                public void onSlide(@NonNull View view, float v) {
                }
            });
        }

        // 通过屏幕像素计算评论界面最大高度
        private int getPeekHeight(){
            int peek = context.getResources().getDisplayMetrics().heightPixels;
            return peek - peek / 4;
        }

        // 切换视频时的收尾工作
        private void detach(){
            videoView.pause();
            handler.removeCallbacks(run);
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    // 根据url最后的视频名称判断是否已经将视频下载至本地目录
    private boolean isDownloadedVideo(String url){
        String fileName = url.split("video/")[1];
        File targetDir = context.getExternalFilesDir("NovaDY/Videos/Downloads");
        if(targetDir.exists()){
            String[] files = targetDir.list();
            for(String childName : files){
                if(childName.equals(fileName))
                    return true;
            }
        }
        return false;
    }

    // 使用DownloadManager和BroadcastReceiver下载视频
    private void downloadVideo(String url){
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("downloading video...");
        request.setDescription("NovaDY");

        File storageFile = context.getExternalFilesDir("NovaDY/Videos/Downloads");
        if(!storageFile.exists())
            storageFile.mkdirs();

        // http://jzvd.nathen.cn/video/1137e480-170bac9c523-0007-1823-c86-de200.mp4
        request.setDestinationInExternalFilesDir(context, "NovaDY/Videos/Downloads", url.split("video")[1]);
        long reference = downloadManager.enqueue(request);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // 下载完成的监听
                if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                    File vfile = new File(storageFile, url.split("video")[1]);
                    Log.d("zxjjj", vfile.toString());
                    File cfile = context.getExternalFilesDir("NovaDY/Covers/Downloads");
                    if(!cfile.exists())
                        cfile.mkdirs();
                    // 下载完成时保存封面
                    saveVideoCover(vfile, cfile);
                }
                // 点击通知栏，取消下载任务
                if(action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
                    downloadManager.remove((Long)reference) ;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(broadcastReceiver, filter);
    }

    // 下载完成时保存封面
    private void saveVideoCover(File videoFile, File target){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoFile.getPath());
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();

        File saveFile = new File(target.getPath(), videoFile.getName().replace(".mp4", ".png"));
        try {
            FileOutputStream saveImgOut = new FileOutputStream(saveFile);
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, saveImgOut);
            saveImgOut.flush();
            saveImgOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 根据屏幕分辨率自适应拉伸竖向视频,对宽>高的视频无效
    private void autoCut(int position, VideoView videoView, boolean disable){
        if(disable){
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            videoView.setLayoutParams(layoutParams);
            return;
        }
        List<Integer> hw = null;
        try {
            // 获取视频宽高
            hw = MyUtils.getNetVideoWidthAndHeight(list.get(position).feedurl.replaceFirst("http", "https"));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 自适应
        if (hw.get(0) < hw.get(1)) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            videoView.setLayoutParams(layoutParams);
        }
        else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            videoView.setLayoutParams(layoutParams);
        }
    }
}
