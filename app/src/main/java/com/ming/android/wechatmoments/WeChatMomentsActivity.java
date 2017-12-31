package com.ming.android.wechatmoments;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WeChatMomentsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment creatFragment() {
        return WeChatMomentsFragment.newInstance();
    }
}
