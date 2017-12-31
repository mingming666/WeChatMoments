package com.ming.android.wechatmoments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ming.android.wechatmoments.been.Item;
import com.ming.android.wechatmoments.been.LoadingItem;
import com.ming.android.wechatmoments.been.TeetItem;
import com.ming.android.wechatmoments.been.TeetItem.CommentsBean;
import com.ming.android.wechatmoments.been.TeetItem.ImagesBean;
import com.ming.android.wechatmoments.been.TeetItem.SenderBean;
import com.ming.android.wechatmoments.been.UserInfo;
import com.ming.android.wechatmoments.view.CommentsLinearLayout;
import com.ming.android.wechatmoments.view.MultiImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MYNOTEBOOK on 2017/12/26.
 */

class WeChatMomentsFragment extends Fragment {

    private RecyclerView mRecyclerVeiw;
    private List<Item> mItems = new ArrayList<>();
    private List<Item> mItemLab = new ArrayList<>();
    private ThumbnailDownloader<Object> mThumbnailDownloader;
    private SwipeRefreshLayout mRefreshSrl;
    private MomentsAdapter mMomentsAdapter;

    public static Fragment newInstance() {
        return new WeChatMomentsFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wechat_moments, container, false);
        mRecyclerVeiw = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerVeiw.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerVeiw.setOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向最后一个滑动
            boolean isSlidingToLast = false;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当停止滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();//所有条目,数量值
                    int itemViewType = recyclerView.getAdapter().getItemViewType(lastVisibleItem);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast && itemViewType == MomentsAdapter.LOADING_ITEM) {
                        loadMoreItems();//加载更多
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //dx用来判断横向滑动方向，dy用来判断纵向滑动方向。dx>0:向右滑动,dx<0:向左滑动；dy>0:向下滑动,dy<0:向上滑动
                isSlidingToLast = dy > 0 ? true : false;
            }
        });
        mRefreshSrl = (SwipeRefreshLayout) v.findViewById(R.id.srl_refresh);
        mRefreshSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updataItems();
                mRefreshSrl.setRefreshing(false);
            }
        });
        mRefreshSrl.setColorScheme(new int[]{
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light});
        setupAdapter();
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new FetchTask(true).execute();
        new FetchTask(false).execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<BaseHolder>() {
                    @Override
                    public void onThumbnailDownloaded(BaseHolder holder, Bitmap bitmap) {
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                        holder.bindDrawable(drawable);
                    }
                });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mMomentsAdapter = new MomentsAdapter(mItems);
            mRecyclerVeiw.setAdapter(mMomentsAdapter);
        }
    }

    private void updataItems() {
        mItems.clear();
        mItems.addAll(mItemLab.subList(0, 5));
        mItems.add(new LoadingItem(R.string.loading_more));
        mMomentsAdapter.setmItems(mItems);
        mMomentsAdapter.notifyItemRangeChanged(0, mItems.size());
    }

    private void loadMoreItems() {
        mMomentsAdapter.setmItems(mItemLab);
        mMomentsAdapter.notifyItemRangeChanged(mItems.size(), mItemLab.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private class FetchTask extends AsyncTask<Object, Object, Object> {

        private final boolean mIsUserInfo;

        public FetchTask(boolean isUserInfo) {
            mIsUserInfo = isUserInfo;
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (mIsUserInfo) {
                return new DataFetch().fetchUserInfo();
            } else {
                return new DataFetch().fetchTeets();
            }
        }

        @Override
        protected void onPostExecute(Object object) {
            if (mIsUserInfo) {
                mItems.add((Item) object);
                mItems.add(new LoadingItem(R.string.loading_more));
                mItemLab.add((Item) object);
                setupAdapter();
            } else {
                mItemLab.addAll((List<Item>) object);
                updataItems();
            }
        }
    }

    private class MomentsAdapter extends RecyclerView.Adapter<BaseHolder> {
        private static final int CONTENT_ITEM = R.layout.content_item;
        private static final int HEAD_ITEM = R.layout.head_item;
        public static final int LOADING_ITEM = R.layout.loading_more_item;
        private List<Item> mItems;

        public MomentsAdapter(List items) {
            mItems = items;
        }

        public void setmItems(List<Item> mItems) {
            this.mItems = mItems;
        }

        @Override
        public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            switch (viewType) {
                case HEAD_ITEM:
                    return new HeadHolder(inflater, parent);
                case CONTENT_ITEM:
                    return new TeetsHolder(inflater, parent);
                case LOADING_ITEM:
                    return new LoadingHolder(inflater, parent);
                default:
                    return null;
            }


        }

        @Override
        public void onBindViewHolder(BaseHolder holder, int position) {
            Item item = mItems.get(position);
            holder.bind(item);
        }


        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getResId();
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }
    }

    private class TeetsHolder extends BaseHolder {


        private TextView mContentTv;
        private TextView mHeadTv;
        private MultiImageView mMultiImageView;
        private CommentsLinearLayout mContentCll;
        private ImageView mHeadIv;
        private Item mItem;
        private TeetItem mTeetItem;

        public TeetsHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.content_item, parent, false));
            mHeadIv = (ImageView) itemView.findViewById(R.id.iv_head);
            mContentTv = (TextView) itemView.findViewById(R.id.tv_content);
            mHeadTv = (TextView) itemView.findViewById(R.id.tv_name);
            mMultiImageView = (MultiImageView) itemView.findViewById(R.id.images_view);
            mContentCll = (CommentsLinearLayout) itemView.findViewById(R.id.cll_comment);
        }

        @Override
        protected void bind(Item item) {
            mTeetItem = (TeetItem) item;
            SenderBean sender = mTeetItem.getSender();
            if (sender != null) {
                mHeadTv.setText(sender.getUsername());
                mThumbnailDownloader.queueueThumbnail(this, sender.getAvatar());
            }
            mContentTv.setText(mTeetItem.getContent());
            List<ImagesBean> images = mTeetItem.getImages();
            if (images != null && images.size() > 0) {
                mMultiImageView.setVisibility(View.VISIBLE);
                mMultiImageView.setList(images);
            } else {
                mMultiImageView.setVisibility(View.GONE);
            }
            List<CommentsBean> comments = mTeetItem.getComments();
            if (comments == null || comments.size() == 0) {
                mContentCll.setVisibility(View.GONE);
            } else {
                mContentCll.setVisibility(View.VISIBLE);
                mContentCll.setList(comments);
            }

        }

        @Override
        public void bindDrawable(BitmapDrawable drawable) {
            mHeadIv.setImageDrawable(drawable);
        }
    }

    private class HeadHolder extends BaseHolder {

        private ImageView mHeadIv;
        private ImageView mHeadBackgrundIv;
        private TextView mNameTv;

        public HeadHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.head_item, parent, false));
            mHeadIv = (ImageView) itemView.findViewById(R.id.iv_head);
            mHeadBackgrundIv = (ImageView) itemView.findViewById(R.id.iv_head_backgrund);
            mNameTv = (TextView) itemView.findViewById(R.id.tv_name);

        }

        @Override
        protected void bind(Item item) {
            UserInfo userInfo = (UserInfo) item;
            mNameTv.setText(userInfo.getUsername());
            mThumbnailDownloader.queueueThumbnail(this, userInfo.getAvatar());
            Glide.with(getContext())
                    .load(userInfo.getProfileimage())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(R.drawable.no_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mHeadBackgrundIv);
        }

        @Override
        public void bindDrawable(BitmapDrawable drawable) {
            mHeadIv.setImageDrawable(drawable);
        }
    }

    private class LoadingHolder extends BaseHolder {

        private final TextView mLoadingTv;

        public LoadingHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.loading_more_item, parent, false));
            mLoadingTv = (TextView) itemView.findViewById(R.id.tv_loadimg);

        }

        @Override
        protected void bind(Item item) {
            LoadingItem loadingItem = (LoadingItem) item;
            mLoadingTv.setText(loadingItem.getmLoadingTextRes());
        }

    }

    private abstract class BaseHolder extends RecyclerView.ViewHolder {

        public BaseHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(Item item);


        public void bindDrawable(BitmapDrawable drawable) {
        }

        ;
    }
}
