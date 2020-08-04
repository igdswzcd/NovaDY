package com.zxj.novady.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/*  双击监听,在onTouchListener的基础上加上延时监听实现   */
public class DoubleClickListener implements View.OnTouchListener {
    private static final int timeout = 400;
    private int clickCount = 0;
    private Handler handler;
    private MyClickCallBack myClickCallBack;

    public interface MyClickCallBack{
        void singleClick();
        void doubleClick();
    }

    public DoubleClickListener(MyClickCallBack myClickCallBack) {
        this.myClickCallBack = myClickCallBack;
        handler = new Handler();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //  这里使用ACTION_UP而非ACTION_DOWN,使得在拖动时不会判断为单击导致视频停止
        if(motionEvent.getAction() == MotionEvent.ACTION_UP){
            clickCount++;
            // 在timeout(400ms)内如果点击-抬起了一次,则为单击,两次为双击
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(clickCount == 1){
                        myClickCallBack.singleClick();
                    }
                    else if(clickCount == 2){
                        myClickCallBack.doubleClick();
                    }
                    handler.removeCallbacksAndMessages(null);
                    clickCount = 0;
                }
            }, timeout);
        }
        return true;
    }
}

