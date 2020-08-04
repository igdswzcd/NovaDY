package com.zxj.novady;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/*  启动界面,获取权限,如果不给予权限则无法进入app*/
public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGHT = 1000; // 延迟
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        handler = new Handler();

        // 权限组
        String[] PERMISSIONS_RECORD_ACTIVITY = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        // 获取权限
        myRequestPermission(PERMISSIONS_RECORD_ACTIVITY);


    }

    private void myRequestPermission(String[] PERMISSIONS_RECORD_ACTIVITY){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_RECORD_ACTIVITY, 1);
        }
        else {
            // 如果已经获取权限(二次进入),则延迟2秒后进入主页
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                }
            }, SPLASH_DISPLAY_LENGHT * 2);
        }
        return;
    }

    // 请求权限监听
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            for(int i = 0; i < permissions.length; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
                    // 成功则1秒后跳转主页
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SplashActivity.this,
                                    MainActivity.class);
                            startActivity(intent);
                            SplashActivity.this.finish();
                        }
                    }, SPLASH_DISPLAY_LENGHT);
                }
                else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("警告！")
                            .setMessage("请前往设置->应用->权限中打开相关权限，否则功能无法正常运行！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 用户不授权,退出
                                    finish();
                                }
                            }).show();
                }
                return;
            }
        }
    }
}
