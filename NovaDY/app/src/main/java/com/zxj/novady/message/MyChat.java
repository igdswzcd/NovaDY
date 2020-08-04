package com.zxj.novady.message;

import java.util.Date;
/*  对话类,包括时间、对话、发送者 */
public class MyChat {
    private long date;
    private String msg;
    private boolean sendByUser;     // true = send by me     false = send by you

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSendByUser() {
        return sendByUser;
    }

    public void setSendByUser(boolean sendByUser) {
        this.sendByUser = sendByUser;
    }
}
