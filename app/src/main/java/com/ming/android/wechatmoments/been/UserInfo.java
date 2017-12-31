package com.ming.android.wechatmoments.been;

import com.ming.android.wechatmoments.R;

/**
 * Created by MYNOTEBOOK on 2017/12/29.
 */

public class UserInfo implements Item {
    private String profileimage;
    private String avatar;
    private String nick;
    private String username;

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public int getResId() {
        return R.layout.head_item;
    }
}
