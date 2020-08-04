package com.zxj.novady.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MyViewHolder> {

    List<MyMessage> messages;
    private final ListItemClickListener listItemClickListener;
    public MsgAdapter(ListItemClickListener listItemClickListener){
        this.listItemClickListener = listItemClickListener;
    }

    public void setMessages(List<MyMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(messages.get(position), position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView avatar;       // 头像
        private TextView title;         // 标题/名称
        private TextView description;   // 描述
        private TextView time;          // 时间

        private int pos;                // 当前位置

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            avatar = itemView.findViewById(R.id.msg_item_iv_avatar);
            title = itemView.findViewById(R.id.msg_item_tv_title);
            description = itemView.findViewById(R.id.msg_item_tv_description);
            time = itemView.findViewById(R.id.msg_item_tv_time);
        }

        public void bindData(MyMessage message, int position) {
            title.setText(message.getTitle());
            description.setText(message.getDescription());
            time.setText(message.getTime());
            pos = position;
        }

        @Override
        public void onClick(View view) {
            if(listItemClickListener != null){
                listItemClickListener.onListItemClick(view, pos);
            }
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(View v, int clickedItemIndex);
    }
}
