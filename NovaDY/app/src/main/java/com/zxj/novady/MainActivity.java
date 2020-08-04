package com.zxj.novady;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zxj.novady.record.RecordActivity;

import java.io.File;
/*  首页,包含BottomNavigationView和Fragment  */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 底部导航分页由BottomNavigationView+Fragment实现
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_videos, R.id.navigation_localvideos, R.id.navigation_empty,
                R.id.navigation_message, R.id.navigation_settings).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }

    // onClick事件,点击底部导航中间图标跳转RecordActivity
    public void openCamera(MenuItem item){
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }
}