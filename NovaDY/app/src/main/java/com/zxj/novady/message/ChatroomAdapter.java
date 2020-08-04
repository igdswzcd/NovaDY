package com.zxj.novady.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomAdapter.MyViewHolder> {
    private List<MyChat> chats;

    public void setChats(List<MyChat> chats) {
        this.chats = chats;
    }

    // 添加数据并通知数据刷新
    public void addChats(MyChat chat) {
        chats.add(chat);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(chats.get(position), shouldHideTime(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView chat_item_avatar; // 头像
        private TextView chat_msg_tv;       // 对话
        private TextView chat_item_time;    // 时间
        private RelativeLayout rl_top;      // 时间父布局

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
//            chat_msg_tv = itemView.findViewById(R.id.ctr_item_msg_tv);
            //chat_item_avatar = itemView.findViewById(R.id.chat_item_avatar);
            chat_item_time = itemView.findViewById(R.id.chat_item_time);
            rl_top = itemView.findViewById(R.id.chat_item_top);
        }
        /*  实现对话信息在同一个itemView中设置了两套隐藏的头像-对话布局
        *   如果isSendByUser为true,则显示右布局,否则显示左布局
        *   如果hideTime为true(两条数据时间间隔<2min),则隐藏时间父布局
        * */
        public void bindData(MyChat chat, boolean hideTime) {
            // 分配头像-对话并显示
            if(chat.isSendByUser()) {
                chat_item_avatar = itemView.findViewById(R.id.chat_item_avatar_right);
                chat_msg_tv = itemView.findViewById(R.id.ctr_item_msg_tv_right);
            }
            else {
                chat_item_avatar = itemView.findViewById(R.id.chat_item_avatar_left);
                chat_msg_tv = itemView.findViewById(R.id.ctr_item_msg_tv_left);
            }
            chat_msg_tv.setText(chat.getMsg());
            chat_msg_tv.setVisibility(View.VISIBLE);
            chat_item_avatar.setVisibility(View.VISIBLE);
            // 是否显示时间
            if(!hideTime) {
                rl_top.setVisibility(View.VISIBLE);
                SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");
                Date date = new Date(chat.getDate());
                chat_item_time.setText((formatter.format(date)));
            }
            else {
                rl_top.setVisibility(View.INVISIBLE);
            }
        }
    }

    /** 如果position为0,第一条信息,则不隐藏
     *  如果不为0,则与position-1比较
     * @param position 当前位置
     * @return  隐藏时间?
     */
    private boolean shouldHideTime(int position){
        if(position == 0)
            return false;
        // 最大间隔为两分钟
        long maxDuration = 2 * 60 * 1000;
        if(chats.get(position).getDate() - chats.get(position-1).getDate() < maxDuration)
            return true;
        return false;
    }
}
