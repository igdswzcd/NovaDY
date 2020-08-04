package com.zxj.novady.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.util.ArrayList;
import java.util.List;

/*  设置页,使用rv完成  */
public class SetFragment extends Fragment {

    private SetAdapter setAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        //
        //设置标题加粗
        TextView temp = root.findViewById(R.id.tv_sett_title);
        temp.getPaint().setFakeBoldText(true);


        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.rv_settings);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setAdapter = new SetAdapter();
        setAdapter.setContext(getContext());
        setKeys();
        recyclerView.setAdapter(setAdapter);
        //
        return root;
    }

    // 预设了一些设置以及按照index对应的初始值
    private void setKeys(){
        List<String> result = new ArrayList<>();
        result.add("自动播放");
        result.add("重复播放");
        result.add("网络视频第一帧代替封面");
        result.add("自动下载视频");
        result.add("裁剪竖向视频");
        setAdapter.setList(result);
        List<Boolean> initBool = new ArrayList<>();
        initBool.add(true);
        initBool.add(true);
        initBool.add(false);
        initBool.add(false);
        initBool.add(false);
        setAdapter.setInitBool(initBool);
    }
}
