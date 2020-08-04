package com.zxj.novady.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zxj.novady.R;

import java.util.ArrayList;
import java.util.List;

/*  设置adapter,从SharedPreferences获取数据,按钮使用Switch实现   */
public class SetAdapter extends RecyclerView.Adapter<SetAdapter.MyViewHolder> {

    List<String> list = new ArrayList<>();
    List<Boolean> initBool = new ArrayList<>(); // 初始设置
    Context context;

    public void setList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setInitBool(List<Boolean> initBool) {
        this.initBool = initBool;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(list.get(position), initBool.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        SharedPreferences sharedPreferences;
        TextView tv_sett_name;
        Switch sw_sett;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_sett_name = itemView.findViewById(R.id.tv_sett_name);
            sw_sett = itemView.findViewById(R.id.sw_sett);
        }

        void bindData(String key, boolean initBool){
            // 设置文件为novady_settings
            sharedPreferences = context.getSharedPreferences("novady_settings", Context.MODE_PRIVATE);
            // 设置名
            tv_sett_name.setText(key);
            // 初始设置,如果sharedPreferences.getBoolean(),key没有对应项,则使用initBool(boolean)为初始值并保存
            boolean isOpen = sharedPreferences.getBoolean(key, initBool);
            // 因为初始的Switch开启/关闭渲染左右相差太大,因此在这里做了一些距离修正
            setSwitchTheme(isOpen);
            // 监听改变,写入设置文件
            sw_sett.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(key, b);
                    editor.apply();
                    setSwitchTheme(b);
                }
            });
            // 设置初始开关
            sw_sett.setChecked(isOpen);
        }

        /*  因为Switch默认的thumb和track,在开关两种情况下左右差距较大
            ,因此在不同情况下修改了paddingRight,从而修正距离 */
        private void setSwitchTheme(boolean checked){
            if(checked) {
                sw_sett.setThumbTintList(ColorStateList.valueOf(context.getColor(R.color.colorBDThumb)));
                sw_sett.setTrackTintList(ColorStateList.valueOf(context.getColor(R.color.colorBDThumb)));
                sw_sett.setPadding(sw_sett.getPaddingLeft(), sw_sett.getPaddingTop(),
                        25, sw_sett.getPaddingBottom());
            }
            else {
                sw_sett.setThumbTintList(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
                sw_sett.setTrackTintList(ColorStateList.valueOf(context.getColor(R.color.colorWhite)));
                sw_sett.setPadding(sw_sett.getPaddingLeft(), sw_sett.getPaddingTop(),
                        10, sw_sett.getPaddingBottom());
            }
        }
    }
}
