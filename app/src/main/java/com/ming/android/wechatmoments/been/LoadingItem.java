package com.ming.android.wechatmoments.been;

import com.ming.android.wechatmoments.R;

/**
 * Created by MYNOTEBOOK on 2017/12/29.
 */

public class LoadingItem implements Item {

    private int mLoadingTextRes;

    public LoadingItem(int text) {
        mLoadingTextRes = text;
    }

    public int getmLoadingTextRes() {
        return mLoadingTextRes;
    }

    public void setmLoadingTextRes(int mLoadingTextRes) {
        this.mLoadingTextRes = mLoadingTextRes;
    }

    @Override
    public int getResId() {
        return R.layout.loading_more_item;
    }

}
