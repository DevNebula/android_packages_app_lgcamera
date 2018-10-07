package com.lge.camera.managers;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.graphy.data.GraphyItem;
import java.util.ArrayList;

public class GraphyViewManager extends GraphyViewManagerExt {
    public GraphyViewManager(GraphyInterface moduleInterface) {
        super(moduleInterface);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyViewManager created");
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        this.mSelectedPosition = this.mGraphyGet.getLastGraphyIndex();
        this.mBaseView = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        initGraphyFilterLayout();
        initGraphyEVGuideLayout();
        initGraphyButtonLayout();
        initGraphyDetailView();
        if (!SharedPreferenceUtil.getGraphyInitGuideShown(this.mGet.getAppContext())) {
            initGraphyInitialGuideLayout();
        }
        changeLayoutZlevel();
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        setGraphyListVisibility(false, false);
        this.mGraphyButton.setOnClickListener(null);
        this.mGraphyButtonLayout.setVisibility(8);
        this.mGet.removePostRunnable(this.mHideEVGuideRunnable);
        this.mGraphyEVGuideLayout.setVisibility(8);
        hideDetailView();
        this.mGraphyAdapter = null;
        setIsFold(false, -1);
        this.mGraphyGet.saveLastGraphyIndex(this.mSelectedPosition);
        removeGraphyView();
    }

    public void notifyManualDataByUser(boolean changed) {
        if (this.mGraphyAdapter != null) {
            this.mGraphyAdapter.setIsChangedManualDataByUser(changed);
            int position = this.mSelectedPosition;
            this.mSelectedPosition = -1;
            this.mGraphyAdapter.notifyItemChanged(position);
        }
    }

    public void spreadImageItems(int offset, int count, int category) {
        if (this.mGraphyAdapter != null) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] offset : " + offset);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] count : " + count);
            this.mGraphyAdapter.notifyItemRangeInserted(offset, count);
            setIsFold(false, category);
        }
    }

    public void foldImageItems(int offset, int count, int category) {
        if (this.mGraphyAdapter != null) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] offset : " + offset);
            CamLog.m3d(CameraConstants.TAG, "[Graphy] count : " + count);
            this.mGraphyAdapter.notifyItemRangeRemoved(offset, count);
            setIsFold(true, category);
        }
    }

    public boolean isFoldedBestItem() {
        return this.mIsFoldedBestItem;
    }

    public boolean isFoldedMyFilterItem() {
        return this.mIsFoldedMyFilterItem;
    }

    protected void changeLayoutZlevel() {
        if (this.mBaseView != null) {
            View quickviewLayout = this.mBaseView.findViewById(C0088R.id.quick_view_layout);
            if (quickviewLayout != null) {
                float zLimit = quickviewLayout.getZ();
                if (this.mGraphyView != null) {
                    this.mGraphyView.setZ(zLimit - 1.0f);
                }
                if (this.mGraphyButtonLayout != null) {
                    this.mGraphyButtonLayout.setZ(zLimit - 1.0f);
                }
                if (this.mGraphyEVGuideLayout != null) {
                    this.mGraphyEVGuideLayout.setZ(zLimit - 1.0f);
                }
            }
        }
    }

    protected void removeGraphyView() {
        if (this.mGraphyView != null) {
            this.mBaseView.removeView(this.mGraphyView);
        }
        if (this.mDetailViewHandler != null) {
            this.mDetailViewHandler.onDestroy();
            this.mDetailViewHandler = null;
        }
        if (this.mGraphyButtonLayout != null) {
            this.mBaseView.removeView(this.mGraphyButtonLayout);
        }
        if (this.mGraphyEVGuideLayout != null) {
            this.mBaseView.removeView(this.mGraphyEVGuideLayout);
        }
        removeInitialGuideLayout();
    }

    public void setGraphyEnable(final boolean enable) {
        if (this.mGraphyButton != null && this.mGraphyListDimView != null) {
            this.mGraphyButton.setColorFilter(enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha());
            this.mGraphyButton.setEnabled(enable);
            int visibility = enable ? 8 : 0;
            if (this.mGraphyRecyclerViewWrapper != null && this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
                this.mGraphyListDimView.setVisibility(visibility);
                this.mGraphyListDimView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        return !enable;
                    }
                });
            }
        }
    }

    public void hideGraphyViewTransiently() {
        if (this.mGraphyRecyclerViewWrapper != null && this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
            this.mkeepVisibleGraphyView = true;
            this.mGraphyRecyclerViewWrapper.setVisibility(8);
        }
        setGraphyButtonVisiblity(false);
        setEVGuideLayoutVisibility(false, false);
        this.mTransientHiddenState = true;
    }

    public void restoreGraphyView() {
        if (this.mGraphyRecyclerViewWrapper != null && this.mkeepVisibleGraphyView) {
            this.mkeepVisibleGraphyView = false;
            this.mGraphyRecyclerViewWrapper.setVisibility(0);
        }
        setGraphyButtonVisiblity(true);
        this.mTransientHiddenState = false;
    }

    public void setGraphyItems(ArrayList<GraphyItem> itemList) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] itemList : " + itemList);
        if (this.mGraphyAdapter != null) {
            this.mGraphyAdapter.setItemList(itemList);
            this.mGraphyAdapter.notifyDataSetChanged();
            if (this.mGraphyRecyclerView != null) {
                this.mGraphyRecyclerView.scrollToPosition(this.mSelectedPosition);
            }
        }
    }

    public void selectItem() {
        if (this.mGraphyAdapter != null && this.mGraphyRecyclerView != null && this.mSelectedPosition > -1) {
            ArrayList<GraphyItem> items = this.mGraphyAdapter.getItemList();
            if (items != null) {
                if (this.mSelectedPosition >= items.size()) {
                    this.mSelectedPosition = -1;
                }
                if (this.mSelectedPosition > -1) {
                    this.mGraphyGet.sendSelectedItemToControlManager((GraphyItem) items.get(this.mSelectedPosition), true);
                }
            }
        }
    }

    public void selectItem(int position) {
        if (this.mGraphyAdapter != null && this.mGraphyRecyclerView != null) {
            this.mSelectedPosition = position;
            this.mGraphyRecyclerView.scrollToPosition(position);
            ArrayList<GraphyItem> items = this.mGraphyAdapter.getItemList();
            if (items != null) {
                if (this.mSelectedPosition >= items.size()) {
                    this.mSelectedPosition = -1;
                }
                if (this.mSelectedPosition > -1) {
                    this.mGraphyGet.sendSelectedItemToControlManager((GraphyItem) items.get(this.mSelectedPosition), false);
                }
            }
        }
    }
}
