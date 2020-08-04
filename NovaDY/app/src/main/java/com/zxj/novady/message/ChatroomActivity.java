package com.zxj.novady.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*  聊天界面,嵌套了一个rv    */
public class ChatroomActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ChatroomAdapter chatroomAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // 设置聊天标题（对方名称）
        TextView tvhead = findViewById(R.id.ctr_tv_name);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString("title");
        tvhead.setText(title);
        tvhead.getPaint().setFakeBoldText(true);

        // 返回按钮
        ImageView iv_close = findViewById(R.id.ctr_iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 配置聊天信息rv
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = findViewById(R.id.ctr_msg_rv);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        chatroomAdapter = new ChatroomAdapter();
        chatroomAdapter.setChats(setDataSet());
        recyclerView.setAdapter(chatroomAdapter);

        // 输入框和发送按钮
        EditText editText = findViewById(R.id.ctr_ed_say);;
        ImageView imageView = findViewById(R.id.ctr_btn_send_info);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().equals("")) {
                    // 发送即往adapter中新增一条数据
                    MyChat temp = new MyChat();
                    temp.setDate(System.currentTimeMillis());
                    temp.setMsg(editText.getText().toString());
                    temp.setSendByUser(true);
                    chatroomAdapter.addChats(temp);
                    // 发送后清除输入框,强制关闭软键盘,清除焦点(光标)
                    editText.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    editText.clearFocus();

                    // 在发送后模拟对方模拟回复
                    chatroomAdapter.addChats(simReply());
                }
            }
        });

    }
    // 设置初始对话
    private List<MyChat> setDataSet(){
        List<MyChat> chats = new ArrayList<>();
        MyChat myChat = new MyChat();
        myChat.setDate(new Date().getTime());
        myChat.setMsg("你好~");
        myChat.setSendByUser(false);
        chats.add(myChat);
        return chats;
    }


    /** 模拟随机回复
     * @return 5条随机对话选一
     */
    private MyChat simReply() {
        int i = (int) (Math.random() * 5);
        MyChat myChat = new MyChat();
        myChat.setDate(System.currentTimeMillis());
        myChat.setSendByUser(false);
        String str = "";
        switch (i) {
            case 0 : str = "666"; break;
            case 1 : str = "太对了"; break;
            case 2 : str = "我洗澡了"; break;
            case 3 : str = "晚安"; break;
            case 4 : str = " xxx是怎么回事呢？" +
                    "xxx相信大家都很熟悉，但是xxx是怎么回事呢，" +
                    "下面就让小编带大家一起了解吧。"; break;
            default: str = "error";
        }
        myChat.setMsg(str);
        return myChat;
    }
}
