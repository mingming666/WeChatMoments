package com.ming.android.wechatmoments.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ming.android.wechatmoments.DensityUtil;
import com.ming.android.wechatmoments.R;
import com.ming.android.wechatmoments.been.TeetItem.ImagesBean;

import java.util.List;

/**
 * Created by MYNOTEBOOK on 2017/12/30.
 */

public class MultiImageView extends LinearLayout {

    public static final String TAG = "MultiImageView";
    public static int sMaxWidth = 0;

    // 照片的列表
    private List<ImagesBean> mImageList;

    /**
     * 长度 单位为Pixel
     **/
    private int mSinglePictureMaxHigh;  // 单张图最大允许宽高
    private int mMorePictureHigh = 0;// 多张图的宽高
    private int mPicturePadding;// 图片间的间距;
    private int mMaxColumnCount = 3;// 每行显示最大数

    public MultiImageView(Context context) {
        this(context, null);

    }

    public MultiImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MultiImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        getAttributes(context, attrs, defStyleAttr);
    }

    private void getAttributes(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiImageView);
        mPicturePadding = a.getDimensionPixelSize(R.styleable.MultiImageView_imagePadding, 0);
        Log.d(TAG, "getAttributes:" + mPicturePadding);
        a.recycle();
    }


    public void setList(List<ImagesBean> lists) throws IllegalArgumentException {
        if (lists == null) {
            throw new IllegalArgumentException("imageList is null...");
        }
        mImageList = lists;
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (sMaxWidth == 0) {
            int width = measureWidth(widthMeasureSpec);
            if (width > 0) {
                sMaxWidth = width;
                if (mImageList != null && mImageList.size() > 0) {
                    setList(mImageList);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
        }
        return result;
    }


    // 根据imageView的数量初始化不同的View布局
    private void initView() {
        if (mImageList == null || mImageList.size() == 0) {
            return;
        }

        setOrientation(VERTICAL);
        removeAllViews();
        if (sMaxWidth == 0) {
            //为了触发onMeasure()来测量MultiImageView的最大宽度，MultiImageView的宽设置为match_parent
            addView(new View(getContext()));
            return;
        } else {
            mMorePictureHigh = (sMaxWidth - mPicturePadding * 2) / 3;
            mSinglePictureMaxHigh = sMaxWidth * 2 / 3;
        }

        int size = mImageList.size();
        if (size == 1) {
            addView(createImageView(0, false));
        } else {
            mMaxColumnCount = size == 4 ? 2 : 3;

            int lastRowCount = size % mMaxColumnCount;//最后一行数目
            int rowCount = size / mMaxColumnCount + (lastRowCount > 0 ? 1 : 0);// 行数
            int columnCount = lastRowCount > 0 ? lastRowCount : mMaxColumnCount;//每行的列数
            for (int i = 0; i < rowCount; i++) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                if (i > 0) {
                    rowLayout.setPadding(0, mPicturePadding, 0, 0);
                }
                //最后一行之前都是最大数目
                columnCount = i < rowCount - 1 ? mMaxColumnCount : columnCount;

                int rowOffset = i * mMaxColumnCount;// 行偏移
                for (int j = 0; j < columnCount; j++) {
                    int position = j + rowOffset;
                    rowLayout.addView(createImageView(position, true));
                }

                addView(rowLayout);
            }
        }
    }

    private ImageView createImageView(int position, boolean isMultiImage) {
        ImagesBean image = mImageList.get(position);
        ImageView imageView = new ImageView(getContext());
        if (isMultiImage) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LayoutParams moreParams = new LayoutParams(mMorePictureHigh, mMorePictureHigh);
            if (position % mMaxColumnCount != 0) {
                moreParams.setMargins(mPicturePadding, 0, 0, 0);
            }
            imageView.setLayoutParams(moreParams);
        } else {
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            //模拟图片大小200dp
            int expectW = DensityUtil.dip2px(getContext(), 200);
            int expectH = DensityUtil.dip2px(getContext(), 200);

            if (expectW != 0 && expectH != 0) {
                int actualW = 0;
                int actualH = 0;
                float scale = expectH / expectW;
                if (expectW > mSinglePictureMaxHigh) {
                    actualW = mSinglePictureMaxHigh;
                    actualH = (int) (actualW * scale);
                } else if (expectW < mMorePictureHigh) {
                    actualW = mMorePictureHigh;
                    actualH = (int) (actualW * scale);
                } else {
                    actualW = expectW;
                    actualH = expectH;
                }
                imageView.setLayoutParams(new LayoutParams(actualW, actualH));
            } else {
                imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
        }

        imageView.setId(image.getUrl().hashCode());
        imageView.setBackgroundColor(getResources().getColor(R.color.grey));
        Glide.with(getContext())
                .load(image.getUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(R.drawable.no_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        return imageView;
    }
}