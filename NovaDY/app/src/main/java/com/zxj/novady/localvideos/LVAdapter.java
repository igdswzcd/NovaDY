package com.zxj.novady.localvideos;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;
import com.zxj.novady.utils.TransTime;

import java.util.ArrayList;
import java.util.List;

public class LVAdapter extends RecyclerView.Adapter<LVAdapter.MyViewHolder> {

    private List<String> list = new ArrayList<>();
    private final ListItemClickListener listItemClickListener;

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public LVAdapter(ListItemClickListener listener){
        listItemClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_video_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(list.get(position), position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView cover;    // 视频缩略图
        private TextView title;     // 视频标题
        private TextView duration;  // 视频时长
        private ImageView type;     // 视频类型(网络下载/本地拍摄)
        private int pos;            // 当前位置

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.iv_cover);
            title = itemView.findViewById(R.id.tv_title);
            duration = itemView.findViewById(R.id.tv_duration);
            type = itemView.findViewById(R.id.iv_type_of_lv);

            itemView.setOnClickListener(this);
        }

        void bindData(String url, int position){
            // 根据.../Videos目录下的子目录名称——Downloads和Records,区分视频
            try {
                String splitRule = "com.zxj.novady/files/NovaDY/Videos/";
                String titleRule = "NovaDY/Videos/";
                String coverRule = "Covers/";
                if(url.split(splitRule)[1].startsWith("Downloads")) {
                    type.setImageResource(R.mipmap.ic_cloud_download_white_48dp);
                    titleRule += "Downloads/";
                    coverRule += "Downloads"+url.split(splitRule+"Downloads")[1].replace(".mp4", ".png");
                }
                else {
                    type.setImageResource(R.mipmap.ic_local_see_white_48dp);
                    titleRule += "Records/";
                    coverRule += "Records"+url.split(splitRule+"Records")[1].replace(".mp4", ".png");;
                }

                // 使用MediaPlayer获取视频长度,并通过utils中TransTime类中 毫秒-分钟:时间 换算方法进行换算
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                duration.setText(TransTime.transSec(mediaPlayer.getDuration()/1000));
                mediaPlayer.release();

                // 因为能够出现在本地视频列表中的视频必定是下载好或者拍摄好的,
                // 而下载完成/拍摄完成时已经完成了封面的保存,所以只需url就能在NovaDY/Covers/xxxxxx中获得封面
                cover.setImageURI(Uri.parse(url.split("Videos/")[0]+coverRule));
                title.setText(url.split(titleRule)[1]);

                pos = position;
            }
            catch (Exception e){
                e.printStackTrace();
            }
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
