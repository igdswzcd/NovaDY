package com.zxj.novady.message;

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

import java.util.ArrayList;
import java.util.List;

/*  信息页,参考了作业中的抖音信息界面,使用rv实现列表    */
public class MessageFragment extends Fragment implements MsgAdapter.ListItemClickListener {

    private MsgAdapter msgAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<MyMessage> dataSet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_message, container, false);

        //设置标题加粗
        TextView temp = root.findViewById(R.id.tv_message_title);
        temp.getPaint().setFakeBoldText(true);

        // 初始化rv,adapter
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView = root.findViewById(R.id.msg_rv);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        msgAdapter = new MsgAdapter(this);
        dataSet = setDataSet();
        msgAdapter.setMessages(dataSet);
        recyclerView.setAdapter(msgAdapter);

        return root;
    }

    // 模拟5个联系人
    private List<MyMessage> setDataSet() {
        List<MyMessage> messages = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            MyMessage myMessage = new MyMessage();
            myMessage.setTitle("第"+i+"个联系人");
            myMessage.setDescription("Hello, World!");
            myMessage.setTime("刚刚");
            messages.add(myMessage);
        }
        return messages;
    }

    @Override
    public void onListItemClick(View v, int clickedItemIndex) {
        Intent intent = new Intent(getContext(), ChatroomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("title", dataSet.get(clickedItemIndex).getTitle());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
