package com.zxj.novady.localvideos;

import android.content.Intent;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/*  本地视频页,收录NovaDY/Videos文件夹下的所有视频文件 */
public class LocalvideosFragment extends Fragment implements LVAdapter.ListItemClickListener {

    private LVAdapter lvAdapter;                // 本地视频adapter
    private RecyclerView recyclerView;          // 本地视频rv
    private LinearLayoutManager layoutManager;
    private List<String> dataSet;               // 本地视频数据集

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_localvideos, container, false);

        // 设置标题加粗
        TextView temp = root.findViewById(R.id.tv_local_title);
        temp.getPaint().setFakeBoldText(true);

        // 初始化rv,adpter
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.rv_local_video);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        lvAdapter = new LVAdapter(this);

        // 读取并设置数据集,rv设置adapter
        dataSet = readVideosFromDiffDir(getContext().getExternalFilesDir("NovaDY/Videos/Downloads"),
                getContext().getExternalFilesDir("NovaDY/Videos/Records"));
        lvAdapter.setList(dataSet);
        recyclerView.setAdapter(lvAdapter);


        return root;
    }

    /**
     * @param dirs 多个文件夹
     * @return  收集好的视频url列表
     */
    private List<String> readVideosFromDiffDir(File... dirs){
        List<String> dataSet = new ArrayList<>();
        for(File itemDir : dirs) {
            if (itemDir.exists()) {
                File[] files = itemDir.listFiles();
                for (File item : files) {
                    dataSet.add(item.getAbsolutePath());
                }
            }
        }
        return dataSet;
    }

    /*  当点击本地视频列表的子元素,即列表中某一视频时,跳转播放    */
    @Override
    public void onListItemClick(View v, int clickedItemIndex) {
        Intent intent = new Intent(getContext(), VideoPlay.class);
        Bundle bundle = new Bundle();
        // 传递url信息
        bundle.putString("url", dataSet.get(clickedItemIndex));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        lvAdapter.notifyDataSetChanged();
    }
}
