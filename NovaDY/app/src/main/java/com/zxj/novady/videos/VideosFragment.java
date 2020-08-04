package com.zxj.novady.videos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.zxj.novady.R;
import com.zxj.novady.api.ApiService;
import com.zxj.novady.api.VideoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*  网络视频页(主页),主要使用ViewPager2和Retrofit   */
public class VideosFragment extends Fragment {

    VideoAdapter videoAdapter;
    private ViewPager2 viewPager2;
    private List<VideoResponse> mVideos;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        // ViewPage2
        viewPager2 = root.findViewById(R.id.vp2);
        videoAdapter = new VideoAdapter();
        viewPager2.setAdapter(videoAdapter);
        // 通过Retrofit获取数据
        getData();
        videoAdapter.setContext(getContext());
        // 换页监听
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPager2.post(new Runnable() {
                    @Override
                    public void run() {
                        videoAdapter.notifyItemChanged(position);
                    }
                });
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        return root;
    }

    // 通过Retrofit获取数据
    private void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://beiyou.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getVideos().enqueue(new Callback<List<VideoResponse>>() {
            @Override
            public void onResponse(Call<List<VideoResponse>> call, Response<List<VideoResponse>> response) {
                if(response.body() != null) {
                    mVideos = response.body();
                    Log.d("retrofit", mVideos.size()+"");
                    if(mVideos.size() != 0) {
                        videoAdapter.setList(mVideos);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<VideoResponse>> call, Throwable t) {
                Log.d("retrofit", t.getMessage());
            }
        });
    }
}
