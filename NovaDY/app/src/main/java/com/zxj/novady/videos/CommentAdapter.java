package com.zxj.novady.videos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.util.ArrayList;
import java.util.List;

/*  评论adapter   */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private List<String> list = new ArrayList<>();

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void addItem(String str) {
        list.add(str);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyViewHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView dialog_item_tv;        // 评论用户名
        private ImageView dialog_thumb;         // 点赞图片
        private TextView dialog_thumb_count;    // 点赞数0/1
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            dialog_item_tv = itemView.findViewById(R.id.dialog_item_comment);
            dialog_thumb = itemView.findViewById(R.id.dialog_item_thumb_like);
            dialog_thumb_count = itemView.findViewById(R.id.dialog_item_thumb_count);
        }

        void bindData(String str){
            dialog_item_tv.setText(str);
            // 点击点赞图片改变图源和点赞数量,再次点击取消
            dialog_thumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(dialog_thumb_count.getText().equals("0")) {
                        dialog_thumb.setImageResource(R.mipmap.icon_topic_post_item_like_blue);
                        dialog_thumb_count.setText("1");
                    }
                    else {
                        dialog_thumb.setImageResource(R.mipmap.icon_topic_post_item_like);
                        dialog_thumb_count.setText("0");
                    }
                }
            });
        }
    }
}
