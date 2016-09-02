package com.chaincloud.chaincloudv.ui.base;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaincloud.chaincloudv.R;
import com.chaincloud.chaincloudv.util.ThreadUtil;
import com.rey.material.widget.ProgressView;

public abstract class EndlessRecyclerViewAdapter<DVH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface EndlessRecyclerViewLoadNextPageListener {
        void onPageAutoLoad();
    }

    private EndlessRecyclerViewLoadNextPageListener autoLoadListener;

    private static final int TYPE_FOOTER = Integer.MAX_VALUE;
    private static final int TYPE_EMPTY = Integer.MAX_VALUE - 1;

    private int lastVisibleItem;

    private int autoLoadThreshold = 4;
    private boolean noMore;
    private boolean loading;
    private boolean firstLoaded = false;
    private int emptyText = 0;
    private int emptyTextColor = Color.BLACK;

    private FooterViewHolder loadingFooterHolder;
    private TextView tvEmpty;

    private LinearLayoutManager layoutManager;

    private Handler handler = new Handler();

    public abstract DVH onCreateDataViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindDataViewHolder(DVH holder, int position);

    public abstract int getDataItemCount();

    abstract public int getDataItemViewType(int position);

    public void setLoadNextPageListener(EndlessRecyclerViewLoadNextPageListener l) {
        this.autoLoadListener = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .layout_page_auto_load_footer, parent, false);
            loadingFooterHolder = new FooterViewHolder(view);
            return loadingFooterHolder;
        } else if (viewType == TYPE_EMPTY) {
            tvEmpty = new TextView(parent.getContext());
            tvEmpty.setText(emptyText);
            tvEmpty.setTextColor(emptyTextColor);
            return new EmptyViewHolder(tvEmpty);
        } else {
            return onCreateDataViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < getDataItemCount()) {
            this.onBindDataViewHolder((DVH) holder, position);
        }
    }

    public void setLoading(final boolean loading) {
        if (this.loading && !loading) {
            firstLoaded = true;
        }
        handler.removeCallbacks(stopRunnable);
        ThreadUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                EndlessRecyclerViewAdapter.this.loading = loading;
                if (!loading) {
                    handler.postDelayed(stopRunnable, 100);
                    maybeLoad();
                }
                if (needEmptyView() && getDataItemCount() == 0) {
                    notifyDataSetChanged();
                }
            }
        });
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public int getItemCount() {
        if (getDataItemCount() == 0 && needEmptyView()) {
            return 1;
        }
        return getDataItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (getDataItemCount() == 0 && needEmptyView()) {
            return TYPE_EMPTY;
        }
        if (position == getDataItemCount()) {
            return TYPE_FOOTER;
        }
        int type = getDataItemViewType(position);
        return type;
    }

    public void maybeLoad() {
        if (layoutManager == null) {
            return;
        }
        if (layoutManager.findLastCompletelyVisibleItemPosition() >= getDataItemCount() -
                getAutoLoadThreshold() && !isNoMore() && !isLoading() && getDataItemCount() > 0) {
            ThreadUtil.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("RVPageAutoLoad", "MaybeLoad true");
                    loadPage();
                }
            });
        } else {
            Log.i("RVPageAutoLoad", "MaybeLoad false");
        }
    }

    private void loadPage() {
        handler.removeCallbacks(stopRunnable);
        if (autoLoadListener != null) {
            setLoading(true);
            if (!isAnimating()) {
                if (loadingFooterHolder != null) {
                    ProgressView pbLoadingFooter = loadingFooterHolder.pbLoadingFooter;
                    if (pbLoadingFooter.getVisibility() != View.VISIBLE) {
                        pbLoadingFooter.setVisibility(View.VISIBLE);
                    } else {
                        pbLoadingFooter.start();
                    }
                }
            }
            autoLoadListener.onPageAutoLoad();
        }
    }


    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            if (loadingFooterHolder != null) {
                loadingFooterHolder.pbLoadingFooter.stop();
            }
        }
    };

    private boolean isAnimating() {
        if (loadingFooterHolder == null) {
            return false;
        }
        if (loadingFooterHolder.pbLoadingFooter.getBackground() == null) {
            return false;
        }
        if (!(loadingFooterHolder.pbLoadingFooter.getBackground() instanceof Animatable)) {
            return false;
        }
        return ((Animatable) loadingFooterHolder.pbLoadingFooter.getBackground()).isRunning();
    }


    public int getAutoLoadThreshold() {
        return autoLoadThreshold;
    }

    public void setAutoLoadThreshold(int autoLoadThreshold) {
        this.autoLoadThreshold = autoLoadThreshold;
    }

    public boolean isNoMore() {
        return noMore;
    }

    public void setNoMore(boolean noMore) {
        this.noMore = noMore;
        if (!noMore) {
            maybeLoad();
        }
        if (needEmptyView() && getDataItemCount() == 0) {
            notifyDataSetChanged();
        }
    }

    private boolean needEmptyView() {
        return firstLoaded && isNoMore() && emptyText > 0;
    }

    public void setEmptyText(int res) {
        emptyText = res;
        if (res > 0 && tvEmpty != null) {
            tvEmpty.setText(res);
        }
    }

    public void setEmptyTextColor(int color) {
        this.emptyTextColor = color;
        if (tvEmpty != null) {
            tvEmpty.setTextColor(color);
        }
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (layoutManager == null) {
                layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            }
            int currentLastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            if (lastVisibleItem < currentLastVisibleItem) {
                maybeLoad();
            }
            lastVisibleItem = currentLastVisibleItem;
        }
    };


    private static final class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmpty;

        public EmptyViewHolder(TextView v) {
            super(v);
            tvEmpty = v;
            tvEmpty.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                    .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvEmpty.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.font_large));
            tvEmpty.setPadding(0, tvEmpty.getContext().getResources().getDimensionPixelOffset(R
                    .dimen.spacing_huge), 0, 0);
            tvEmpty.setGravity(Gravity.CENTER);
        }
    }

    private static final class FooterViewHolder extends RecyclerView.ViewHolder {
        public View loadingFooter;
        public ProgressView pbLoadingFooter;

        public FooterViewHolder(View v) {
            super(v);
            this.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                    .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.loadingFooter = v;
            this.pbLoadingFooter = (ProgressView) v.findViewById(R.id.pb_loading_footer);
            pbLoadingFooter.setVisibility(View.GONE);
        }
    }
}
