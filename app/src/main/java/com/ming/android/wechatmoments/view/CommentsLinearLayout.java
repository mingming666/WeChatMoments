package com.ming.android.wechatmoments.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ming.android.wechatmoments.R;
import com.ming.android.wechatmoments.been.TeetItem.CommentsBean;
import com.ming.android.wechatmoments.been.TeetItem.SenderBean;

import java.util.List;

/**
 * Created by MYNOTEBOOK on 2017/12/30.
 */

public class CommentsLinearLayout extends LinearLayout {

    private List<CommentsBean> mImageList;

    public CommentsLinearLayout(Context context) {
        super(context);
    }

    public CommentsLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void setList(List<CommentsBean> lists) throws IllegalArgumentException {
        if (lists == null || lists.size() == 0) {
            throw new IllegalArgumentException("imageList is null...");
        }
        mImageList = lists;
        initView();
    }

    private void initView() {
        removeAllViews();
        for (int i = 0; i < mImageList.size(); i++) {
            CommentsBean comment = mImageList.get(i);
            SenderBean sender = comment.getSender();
            if (sender == null) {
                continue;
            }
            LayoutInflater inflater = LayoutInflater.from(getContext());
            LinearLayout v = (LinearLayout) inflater.inflate(R.layout.comment_item, this, false);
            TextView name = (TextView) v.findViewById(R.id.tv_name);
            TextView content = (TextView) v.findViewById(R.id.tv_content);
            name.setText(sender.getUsername()+":");
            content.setText(comment.getContent());
            addView(v);
        }


    }
}
