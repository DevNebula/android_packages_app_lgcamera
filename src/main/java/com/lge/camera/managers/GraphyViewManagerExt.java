package com.lge.camera.managers;

import android.graphics.Color;
import android.support.p001v7.widget.DefaultItemAnimator;
import android.support.p001v7.widget.LinearLayoutManager;
import android.support.p001v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.GraphyViewManagerBase.GraphyAdapter.ViewHolder;
import com.lge.camera.managers.ext.DetailViewHandler;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;
import com.lge.graphy.data.GraphyItem;

public class GraphyViewManagerExt extends GraphyViewManagerBase {
    protected ViewGroup mBaseView = null;
    AnimationListener mButtonAniListener = new C09854();
    private final float mButtonBottomListInvisibleRatio = 0.187f;
    private final float mButtonBottomListInvisibleRatioLong = 0.217f;
    private final float mButtonBottomListVisibleRatio = 0.3f;
    private final float mButtonBottomListVisibleRatioLong = 0.33f;
    private final float mEVBoxBottom = 0.369f;
    private final float mEVBoxBottomLong = 0.3926f;
    private final float mEVBoxHeight = 0.1528f;
    private final float mEVBoxHeightLong = 0.139f;
    protected RotateImageButton mGraphyButton = null;
    protected RelativeLayout mGraphyButtonLayout = null;
    protected RelativeLayout mGraphyEVBox = null;
    protected RelativeLayout mGraphyEVGuideLayout = null;
    protected TextView mGraphyEVText = null;
    protected RelativeLayout mGraphyEVTextLayout = null;
    protected RelativeLayout mGraphyInitialGuideBox = null;
    protected RelativeLayout mGraphyInitialGuideLayout = null;
    protected View mGraphyListDimView = null;
    protected RelativeLayout mGraphyView = null;
    HandlerRunnable mHideEVGuideRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (GraphyViewManagerExt.this.mGraphyEVGuideLayout != null) {
                GraphyViewManagerExt.this.mGraphyEVGuideLayout.setVisibility(8);
            }
        }
    };
    protected boolean mIsFoldedBestItem = false;
    protected boolean mIsFoldedMyFilterItem = false;
    private boolean mIsGraphyListAnimationShowing = false;
    protected LinearLayoutManager mLayoutManager = null;
    private final float mListBottomMarginAdjust = 0.0292f;
    private final float mListBottomMarginRatio = 0.185f;
    private final float mListBottomMarginRatioLong = 0.214f;
    OnTouchListener mOnRecyclerViewTouchListener = new C09908();
    HandlerRunnable mShowEVGuideRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            if (GraphyViewManagerExt.this.mGraphyEVGuideLayout != null) {
                if (GraphyViewManagerExt.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) || !GraphyViewManagerExt.this.isGraphyListVisible()) {
                    GraphyViewManagerExt.this.mGraphyEVGuideLayout.setVisibility(8);
                    return;
                }
                GraphyViewManagerExt.this.setParamOfEVTextLayout();
                GraphyViewManagerExt.this.mGraphyEVGuideLayout.setAlpha(1.0f);
            }
        }
    };
    protected boolean mTransientHiddenState = false;
    protected boolean mkeepVisibleGraphyView = false;

    /* renamed from: com.lge.camera.managers.GraphyViewManagerExt$3 */
    class C09843 implements OnClickListener {
        C09843() {
        }

        public void onClick(View arg0) {
            if (GraphyViewManagerExt.this.mGet.checkModuleValidate(48) && GraphyViewManagerExt.this.mGraphyRecyclerView != null && !GraphyViewManagerExt.this.isButtonAnimating()) {
                if (GraphyViewManagerExt.this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
                    GraphyViewManagerExt.this.setGraphyListVisibility(false, true);
                    return;
                }
                GraphyViewManagerExt.this.mComparativeIlluminance = GraphyViewManagerExt.this.mGraphyGet.getIlluminance();
                GraphyViewManagerExt.this.setGraphyListVisibility(true, true);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GraphyViewManagerExt$4 */
    class C09854 implements AnimationListener {
        C09854() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            GraphyViewManagerExt.this.mGet.postOnUiThread(new HandlerRunnable(GraphyViewManagerExt.this) {
                public void handleRun() {
                    if (GraphyViewManagerExt.this.mGraphyRecyclerView != null) {
                        if (GraphyViewManagerExt.this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
                            GraphyViewManagerExt.this.hideGraphyList();
                        } else {
                            GraphyViewManagerExt.this.showGraphyList();
                        }
                        GraphyViewManagerExt.this.setButtonLayoutParam();
                    }
                }
            }, 0);
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* renamed from: com.lge.camera.managers.GraphyViewManagerExt$5 */
    class C09875 implements AnimationListener {
        C09875() {
        }

        public void onAnimationStart(Animation arg0) {
            GraphyViewManagerExt.this.mIsGraphyListAnimationShowing = true;
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            GraphyViewManagerExt.this.mIsGraphyListAnimationShowing = false;
        }
    }

    /* renamed from: com.lge.camera.managers.GraphyViewManagerExt$8 */
    class C09908 implements OnTouchListener {
        C09908() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == 1 && GraphyViewManagerExt.this.mGraphyAdapter != null && GraphyViewManagerExt.this.mGraphyAdapter.isLongClick()) {
                GraphyViewManagerExt.this.hideDetailView();
            }
            return false;
        }
    }

    public GraphyViewManagerExt(GraphyInterface moduleInterface) {
        super(moduleInterface);
    }

    protected void initGraphyFilterLayout() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyViewManager initGraphyFilterLayout");
        this.mGraphyView = (RelativeLayout) this.mGet.inflateView(C0088R.layout.graphy_list);
        if (this.mBaseView != null && this.mGraphyView != null) {
            this.mBaseView.addView(this.mGraphyView);
            int listHeight = this.mListViewItemWidth + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            this.mGraphyRecyclerView = (RecyclerView) this.mGraphyView.findViewById(C0088R.id.graphy_recyclerview);
            LayoutParams lp = (LayoutParams) this.mGraphyRecyclerView.getLayoutParams();
            this.mGraphyRecyclerViewWrapper = (RelativeLayout) this.mGraphyView.findViewById(C0088R.id.graphy_recyclerview_wrapper);
            LayoutParams wrapperLp = (LayoutParams) this.mGraphyRecyclerViewWrapper.getLayoutParams();
            wrapperLp.removeRule(10);
            wrapperLp.addRule(12, 1);
            lp.width = -1;
            lp.height = listHeight;
            int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
            int bottom = (lcdSize[0] - ((this.mListViewItemWidth + (Utils.getPx(getAppContext(), C0088R.dimen.cinema_listView_padding) * 2)) + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.2432f : 0.185f))) + RatioCalcUtil.getNotchDisplayHeight(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[graphy] lcdSize[0] : " + lcdSize[0] + ", bottom : " + bottom);
            wrapperLp.setMarginsRelative(0, 0, 0, bottom);
            this.mGraphyRecyclerView.setLayoutParams(lp);
            this.mGraphyRecyclerViewWrapper.setLayoutParams(wrapperLp);
            this.mGraphyRecyclerViewWrapper.setVisibility(4);
            this.mGraphyListDimView = this.mGraphyView.findViewById(C0088R.id.graphy_list_disable);
            this.mGraphyListDimView.setLayoutParams(lp);
            this.mGraphyListDimView.setVisibility(8);
            this.mLayoutManager = new LinearLayoutManager(getAppContext(), 0, false);
            this.mGraphyRecyclerView.setLayoutManager(this.mLayoutManager);
            this.mGraphyRecyclerView.setItemAnimator(new DefaultItemAnimator());
            this.mGraphyRecyclerView.addItemDecoration(new GraphyDividerItemDecoration(getAppContext()));
            this.mGraphyAdapter = new GraphyAdapter();
            this.mGraphyAdapter.setItemList(this.mGraphyGet.getGraphyItems());
            this.mGraphyRecyclerView.setAdapter(this.mGraphyAdapter);
            if (Utils.isRTLLanguage()) {
                this.mLayoutManager.setReverseLayout(true);
            }
        }
    }

    protected void initGraphyDetailView() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyViewManager initGraphyDetailView");
        this.mDetailViewHandler = new DetailViewHandler(getAppContext(), this.mBaseView);
        this.mDetailViewHandler.initGraphyDetailView();
    }

    protected void initGraphyButtonLayout() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyViewManager initGraphyButtonLayout");
        this.mGraphyButtonLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.graphy_button);
        if (this.mBaseView != null && this.mGraphyButtonLayout != null) {
            this.mBaseView.addView(this.mGraphyButtonLayout);
            this.mGraphyButton = (RotateImageButton) this.mGraphyButtonLayout.findViewById(C0088R.id.graphy_btn);
            if (this.mGraphyButton != null) {
                this.mGraphyButton.setText(this.mGet.getActivity().getResources().getString(C0088R.string.graphy));
            }
            setButtonLayoutParam();
            setGraphyButtonVisiblity(false);
            setOnClickListenerGraphyButton();
        }
    }

    protected void initGraphyEVGuideLayout() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyViewManager initGraphyEVGuideLayout");
        this.mGraphyEVGuideLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.graphy_ev_guide);
        if (this.mBaseView != null && this.mGraphyEVGuideLayout != null) {
            this.mBaseView.addView(this.mGraphyEVGuideLayout);
            this.mGraphyEVBox = (RelativeLayout) this.mGraphyEVGuideLayout.findViewById(C0088R.id.graphy_ev_box);
            this.mGraphyEVTextLayout = (RelativeLayout) this.mGraphyEVGuideLayout.findViewById(C0088R.id.graphy_ev_text_layout);
            this.mGraphyEVText = (TextView) this.mGraphyEVGuideLayout.findViewById(C0088R.id.graphy_ev_text);
            setParamOfEVGuideLayout();
            setParamOfEVTextLayout();
            setParamOfEVBox();
            this.mGraphyEVGuideLayout.setVisibility(8);
        }
    }

    protected void initGraphyInitialGuideLayout() {
        this.mGraphyInitialGuideLayout = (RelativeLayout) this.mGet.inflateView(C0088R.layout.graphy_initial_guide);
        if (!(this.mBaseView == null || this.mGraphyInitialGuideLayout == null)) {
            this.mBaseView.addView(this.mGraphyInitialGuideLayout);
            setInitialGuideLayoutParam();
        }
        this.mGraphyInitialGuideBox = (RelativeLayout) this.mGet.inflateView(C0088R.layout.graphy_init_guide_box);
        if (this.mBaseView != null && this.mGraphyInitialGuideBox != null) {
            int marginEnd;
            this.mBaseView.addView(this.mGraphyInitialGuideBox);
            LayoutParams lp = (LayoutParams) this.mGraphyInitialGuideBox.getLayoutParams();
            lp.width = this.mListViewItemWidth + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            lp.height = this.mListViewItemWidth + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            lp.addRule(this.mIsRTL ? 21 : 20, 1);
            lp.addRule(12, 1);
            int marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.214f : 0.185f) + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            int marginStart = this.mIsRTL ? 0 : (this.mListViewItemWidth * 2) + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            if (this.mIsRTL) {
                marginEnd = (this.mListViewItemWidth * 2) + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
            } else {
                marginEnd = 0;
            }
            lp.setMarginsRelative(marginStart, 0, marginEnd, marginBottom);
            this.mGraphyInitialGuideBox.setLayoutParams(lp);
        }
    }

    protected void setInitialGuideLayoutParam() {
        if (this.mGraphyInitialGuideLayout != null) {
            TextView initGuideTextView = (TextView) this.mGraphyInitialGuideLayout.findViewById(C0088R.id.graphy_init_guide_txt);
            if (initGuideTextView != null) {
                initGuideTextView.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.4222f));
            }
            LayoutParams lp = (LayoutParams) this.mGraphyInitialGuideLayout.getLayoutParams();
            int marginStart;
            int marginBottom;
            int marginEnd;
            if (this.mManagerDegree % 180 == 0) {
                this.mGraphyInitialGuideLayout.setBackgroundResource(C0088R.drawable.camera_cine_help_bubble_ver);
                this.mGraphyInitialGuideLayout.setRotation(0.0f);
                if (initGuideTextView != null) {
                    initGuideTextView.setRotation((float) this.mManagerDegree);
                }
                marginStart = (int) (((double) this.mListViewItemWidth) * 1.5d);
                marginBottom = (RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.214f : 0.185f) + this.mListViewItemWidth) + Utils.getPx(getAppContext(), C0088R.dimen.graphy_item_padding);
                if (this.mIsRTL) {
                    lp.addRule(21, 1);
                    marginStart = 0;
                    marginEnd = (int) (((double) this.mListViewItemWidth) * 1.5d);
                } else {
                    lp.addRule(20, 1);
                    marginStart = (int) (((double) this.mListViewItemWidth) * 1.5d);
                    marginEnd = 0;
                }
                lp.addRule(12, 1);
                lp.setMarginsRelative(marginStart, 0, marginEnd, marginBottom);
            } else {
                this.mGraphyInitialGuideLayout.setBackgroundResource(C0088R.drawable.camera_cine_help_bubble);
                this.mGraphyInitialGuideLayout.setRotation(90.0f);
                if (initGuideTextView != null) {
                    initGuideTextView.setRotation((float) (this.mManagerDegree + 90));
                }
                marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.348f);
                marginStart = (int) (((double) this.mListViewItemWidth) * 1.5d);
                if (this.mIsRTL) {
                    lp.addRule(21, 1);
                    marginStart = 0;
                    marginEnd = (int) (((double) this.mListViewItemWidth) * 1.5d);
                } else {
                    lp.addRule(20, 1);
                    marginStart = (int) (((double) this.mListViewItemWidth) * 1.5d);
                    marginEnd = 0;
                }
                lp.addRule(12, 1);
                lp.setMarginsRelative(marginStart, 0, marginEnd, marginBottom);
            }
            this.mGraphyInitialGuideLayout.setLayoutParams(lp);
        }
    }

    protected void removeInitialGuideLayout() {
        if (this.mGraphyInitialGuideLayout != null && this.mGraphyInitialGuideLayout.getVisibility() == 0) {
            this.mGraphyInitialGuideLayout.setVisibility(8);
            this.mBaseView.removeView(this.mGraphyInitialGuideLayout);
            this.mGraphyInitialGuideLayout = null;
        }
        if (this.mGraphyInitialGuideBox != null && this.mGraphyInitialGuideBox.getVisibility() == 0) {
            this.mGraphyInitialGuideBox.setVisibility(8);
            this.mGraphyInitialGuideBox = null;
        }
    }

    protected void setButtonLayoutParam() {
        if (this.mGraphyButtonLayout != null) {
            int marginBottom;
            LayoutParams lp = (LayoutParams) this.mGraphyButtonLayout.getLayoutParams();
            lp.width = -2;
            lp.height = -2;
            lp.addRule(21, 1);
            lp.addRule(12, 1);
            if (this.mGraphyRecyclerViewWrapper.getVisibility() == 0) {
                marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.33f : 0.3f);
            } else {
                marginBottom = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.217f : 0.187f);
            }
            lp.setMarginsRelative(0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0076f), marginBottom);
            this.mGraphyButtonLayout.setLayoutParams(lp);
        }
    }

    protected void setParamOfEVGuideLayout() {
        if (this.mGraphyEVGuideLayout != null) {
            LayoutParams lp = (LayoutParams) this.mGraphyEVGuideLayout.getLayoutParams();
            lp.width = -1;
            lp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.4555f);
            lp.addRule(20, 1);
            lp.addRule(12, 1);
            lp.setMarginsRelative(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0263f), 0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, ModelProperties.getLCDType() == 2 ? 0.3926f : 0.369f));
            this.mGraphyEVGuideLayout.setLayoutParams(lp);
        }
    }

    protected void setParamOfEVTextLayout() {
        if (this.mGraphyEVTextLayout != null && this.mGraphyEVText != null) {
            this.mGraphyEVTextLayout.measure(0, 0);
            this.mGraphyEVTextLayout.setPivotX((float) this.mGraphyEVTextLayout.getMeasuredWidth());
            this.mGraphyEVTextLayout.setPivotY((float) this.mGraphyEVTextLayout.getMeasuredHeight());
            LayoutParams lp = (LayoutParams) this.mGraphyEVTextLayout.getLayoutParams();
            if (this.mManagerDegree % 180 == 90) {
                this.mGraphyEVTextLayout.setBackgroundResource(C0088R.drawable.camera_graphy_help_bubble);
                this.mGraphyEVTextLayout.setRotation(90.0f);
                if (this.mManagerDegree == 90) {
                    this.mGraphyEVText.setRotation(180.0f);
                } else {
                    this.mGraphyEVText.setRotation(0.0f);
                }
                this.mGraphyEVText.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.4555f) - (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0194f) * 2));
                lp.width = -2;
                lp.height = -2;
                lp.addRule(17, C0088R.id.graphy_ev_box);
                lp.addRule(12, 1);
                lp.setMarginsRelative(-this.mGraphyEVTextLayout.getMeasuredWidth(), 0, 0, 0);
            } else {
                this.mGraphyEVTextLayout.setBackgroundResource(C0088R.drawable.camera_graphy_help_bubble_ver);
                this.mGraphyEVTextLayout.setRotation(0.0f);
                if (this.mManagerDegree == 180) {
                    this.mGraphyEVText.setRotation(180.0f);
                } else {
                    this.mGraphyEVText.setRotation(0.0f);
                }
                this.mGraphyEVText.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.3958f) - (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.0194f) * 2));
                lp.width = -2;
                lp.height = -2;
                lp.addRule(17, C0088R.id.graphy_ev_box);
                lp.addRule(12, 1);
                lp.setMarginsRelative(0, 0, 0, (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.1833f) / 2) - (this.mGraphyEVTextLayout.getMeasuredHeight() / 2));
            }
            this.mGraphyEVTextLayout.setLayoutParams(lp);
        }
    }

    protected void setParamOfEVBox() {
        if (this.mGraphyEVBox != null) {
            LayoutParams lp = (LayoutParams) this.mGraphyEVBox.getLayoutParams();
            int previewWidth = (int) (((float) Utils.getLCDsize(getAppContext(), true)[1]) * 1.3333334f);
            int manualPanelbuttonWidth = ((previewWidth - RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.1166f)) - RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.09375f)) / 6;
            lp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.125f);
            lp.height = manualPanelbuttonWidth * 2;
            lp.addRule(12, 1);
            lp.addRule(20, 1);
        }
    }

    public void setEVGuideLayoutVisibility(boolean show, boolean over) {
        if (this.mGraphyEVGuideLayout != null && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            if (show && !this.mTransientHiddenState && isGraphyListVisible()) {
                if (over) {
                    this.mGraphyEVText.setText(C0088R.string.graphy_label_ev_over_guide);
                } else {
                    this.mGraphyEVText.setText(C0088R.string.graphy_label_ev_under_guide);
                }
                this.mGraphyEVGuideLayout.setVisibility(0);
                this.mGraphyEVGuideLayout.setAlpha(0.0f);
                setParamOfEVTextLayout();
                this.mGet.postOnUiThread(this.mShowEVGuideRunnable, 50);
                this.mGet.removePostRunnable(this.mHideEVGuideRunnable);
                this.mGet.postOnUiThread(this.mHideEVGuideRunnable, 3050);
                return;
            }
            this.mGraphyEVGuideLayout.setVisibility(8);
        }
    }

    protected void setOnClickListenerGraphyButton() {
        if (this.mGraphyButton != null) {
            this.mGraphyButton.setOnClickListener(new C09843());
        }
    }

    public void setGraphyButtonVisiblity(boolean visible) {
        if (this.mGraphyButtonLayout != null && this.mGraphyButton != null) {
            if (!visible) {
                this.mGraphyButton.setOnClickListener(null);
                this.mGraphyButtonLayout.setVisibility(8);
                if (this.mGraphyEVGuideLayout != null) {
                    this.mGraphyEVGuideLayout.setVisibility(8);
                }
            } else if (!"on".equals(this.mGraphyGet.getSettingValue(Setting.KEY_GRAPHY)) || this.mGet.isJogZoomMoving()) {
                this.mGraphyButton.setOnClickListener(null);
                this.mGraphyButtonLayout.setVisibility(8);
                if (this.mGraphyEVGuideLayout != null) {
                    this.mGraphyEVGuideLayout.setVisibility(8);
                }
            } else {
                this.mGraphyButtonLayout.setVisibility(0);
                this.mGraphyButton.setDegree(this.mManagerDegree, false);
                setOnClickListenerGraphyButton();
            }
        }
    }

    public void setGraphyListVisibility(boolean visible, boolean animate) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] setGraphyListVisibility visibility : " + visible);
        if (this.mGraphyRecyclerViewWrapper != null && this.mGraphyAdapter != null) {
            if (!visible) {
                if (animate) {
                    AnimationUtil.startGraphyButtonTransAnimation(this.mGet.getAppContext(), this.mGraphyButtonLayout, 200, this.mButtonAniListener, true);
                    AnimationUtil.startGraphyListAlphaAnimation(this.mGraphyRecyclerViewWrapper, false, 200, new C09875());
                } else {
                    hideGraphyList();
                    setButtonLayoutParam();
                }
                removeInitialGuideLayout();
            } else if (!"on".equals(this.mGraphyGet.getSettingValue(Setting.KEY_GRAPHY))) {
                hideGraphyList();
                setButtonLayoutParam();
                removeInitialGuideLayout();
            } else if (animate) {
                AnimationUtil.startGraphyButtonTransAnimation(this.mGet.getAppContext(), this.mGraphyButtonLayout, 200, this.mButtonAniListener, false);
                AnimationUtil.startGraphyListAlphaAnimation(this.mGraphyRecyclerViewWrapper, true, 200, null);
            } else {
                showGraphyList();
                setButtonLayoutParam();
            }
        }
    }

    protected void showInitialGuideLayout() {
        if (!SharedPreferenceUtil.getGraphyInitGuideShown(this.mGet.getAppContext()) && this.mGraphyInitialGuideLayout != null && this.mGraphyInitialGuideBox != null) {
            setInitialGuideLayoutParam();
            this.mGraphyInitialGuideLayout.setVisibility(0);
            this.mGraphyInitialGuideBox.setVisibility(0);
            SharedPreferenceUtil.setGraphyInitGuideShown(this.mGet.getAppContext(), true);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    GraphyViewManagerExt.this.removeInitialGuideLayout();
                }
            }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        }
    }

    protected void showGraphyList() {
        showInitialGuideLayout();
        if (this.mGraphyAdapter != null) {
            this.mGraphyAdapter.notifyDataSetChanged();
        }
        if (!(this.mGraphyRecyclerView == null || this.mGraphyRecyclerViewWrapper == null)) {
            this.mGraphyRecyclerViewWrapper.setVisibility(0);
            this.mGraphyRecyclerView.setOnTouchListener(this.mOnRecyclerViewTouchListener);
        }
        if (this.mGraphyButton != null) {
            this.mGraphyButton.setSelected(true);
        }
        this.mGraphyGet.onShowGraphyList();
    }

    protected void hideGraphyList() {
        if (this.mGraphyRecyclerViewWrapper != null) {
            this.mGraphyRecyclerView.setOnTouchListener(null);
            this.mGraphyRecyclerViewWrapper.setVisibility(8);
        }
        if (this.mGraphyButton != null) {
            this.mGraphyButton.setSelected(false);
        }
        if (this.mGraphyEVGuideLayout != null) {
            this.mGraphyEVGuideLayout.setVisibility(8);
        }
        this.mGraphyGet.onHideGraphyList();
    }

    public boolean isGraphyListVisible() {
        if (this.mGraphyRecyclerViewWrapper == null || this.mGraphyRecyclerViewWrapper.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public boolean isGraphyListAnimaitonShowing() {
        return this.mIsGraphyListAnimationShowing;
    }

    protected void setIsFold(boolean isFold, int category) {
        switch (category) {
            case -1:
                this.mIsFoldedBestItem = isFold;
                this.mIsFoldedMyFilterItem = isFold;
                return;
            case 1:
                this.mIsFoldedBestItem = isFold;
                return;
            case 2:
                this.mIsFoldedMyFilterItem = isFold;
                return;
            default:
                return;
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mGraphyButton != null) {
            this.mGraphyButton.setDegree(degree, true);
        }
        if (this.mGraphyRecyclerViewWrapper != null && this.mGraphyRecyclerViewWrapper.getVisibility() == 0 && this.mGraphyAdapter != null) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] degree : " + degree + " animation : " + animation);
            int startPosition = this.mLayoutManager.findFirstVisibleItemPosition();
            if (startPosition < 0) {
                startPosition = 0;
            }
            int endPosition = this.mLayoutManager.findLastVisibleItemPosition();
            if (endPosition < 0) {
                endPosition = this.mGraphyAdapter.getItemCount() - 1;
            }
            rotateGraphyItems(startPosition, endPosition);
            if (this.mDetailViewHandler != null) {
                this.mDetailViewHandler.rotateLayout(degree);
                this.mDetailViewHandler.clearAnimation();
            }
            if (this.mGraphyEVGuideLayout != null && this.mGraphyEVGuideLayout.getVisibility() == 0) {
                setParamOfEVTextLayout();
                this.mGraphyEVGuideLayout.setAlpha(0.0f);
                this.mGet.postOnUiThread(this.mShowEVGuideRunnable, 50);
            }
            if (this.mGraphyInitialGuideLayout != null && this.mGraphyInitialGuideLayout.getVisibility() == 0) {
                setInitialGuideLayoutParam();
            }
        }
    }

    protected void rotateGraphyItems(int startPosition, int endPosition) {
        for (int i = startPosition; i <= endPosition; i++) {
            ViewHolder holder = (ViewHolder) this.mGraphyRecyclerView.findViewHolderForAdapterPosition(i);
            GraphyItem item = (GraphyItem) this.mGraphyAdapter.getItemList().get(i);
            if (item != null && item.getType() == 0) {
                setCategoryItemView(holder, item);
            }
            if (!(holder == null || holder.itemView == null || holder.imageView == null || holder.image_text_View == null)) {
                holder.image_text_View.setRotation((float) (-this.mManagerDegree));
                holder.categoryView.setRotation((float) (-this.mManagerDegree));
            }
        }
        if (startPosition > 0) {
            this.mGraphyAdapter.notifyItemRangeChanged(0, startPosition);
        }
        if (endPosition < this.mGraphyAdapter.getItemCount() - 1) {
            this.mGraphyAdapter.notifyItemRangeChanged(endPosition + 1, this.mGraphyAdapter.getItemCount() - endPosition);
        }
    }

    protected void setCategoryItemView(ViewHolder holder, GraphyItem item) {
        int type = item.getIntValue(GraphyItem.KEY_CATEGORY_ID_INT);
        String title = item.getStringValue(GraphyItem.KEY_CATEGORY_NAME_STR);
        holder.categoryView.setVisibility(0);
        holder.categoryViewBG.setVisibility(0);
        CamLog.m3d(CameraConstants.TAG, "[graphy][category] type : " + type);
        if (type == 1) {
            setBestCategoryItemView(holder);
        } else if (type == 2) {
            setMyFilterCategoryItemView(holder);
        } else if (type == 3) {
            holder.categoryViewBG.setBackgroundColor(Color.argb(192, 38, 38, 38));
            holder.arrowView.setVisibility(8);
            holder.textView.setVisibility(8);
            holder.moreView.setVisibility(0);
            holder.category_text_View.setVisibility(8);
        } else if (type == 0) {
            title = getAppContext().getResources().getString(C0088R.string.film_emulator_film_none);
            holder.arrowView.setImageDrawable(null);
            if (this.mSelectedPosition == 0) {
                holder.textView.setTextColor(this.mGet.getAppContext().getColor(C0088R.color.camera_pressed_txt));
            } else {
                holder.textView.setTextColor(this.mGet.getAppContext().getColor(C0088R.color.camera_white_txt));
            }
            holder.categoryViewBG.setBackgroundColor(Color.argb(192, 38, 38, 38));
            holder.arrowView.setVisibility(0);
            holder.textView.setVisibility(0);
            holder.moreView.setVisibility(8);
            holder.category_text_View.setVisibility(8);
        }
        holder.textView.setText(title);
    }

    protected void setBestCategoryItemView(ViewHolder holder) {
        holder.arrowView.setImageResource(C0088R.drawable.ic_graphy_filter_arrow_close);
        rotateBestArrow(holder);
        holder.textView.setTextColor(Color.argb(255, 0, 0, 0));
        holder.categoryViewBG.setBackgroundColor(Color.argb(178, 255, 255, 255));
        holder.arrowView.setVisibility(0);
        holder.textView.setVisibility(0);
        holder.moreView.setVisibility(8);
    }

    protected void setMyFilterCategoryItemView(ViewHolder holder) {
        holder.arrowView.setImageResource(C0088R.drawable.ic_graphy_filter_arrow_open);
        rotateMyFilterArrow(holder);
        holder.textView.setTextColor(this.mGet.getAppContext().getColor(C0088R.color.camera_white_txt));
        holder.categoryViewBG.setBackgroundColor(Color.argb(178, 75, 219, 190));
        holder.arrowView.setVisibility(0);
        holder.textView.setVisibility(0);
        holder.moreView.setVisibility(8);
    }

    protected void rotateBestArrow(ViewHolder holder) {
        if (holder != null && holder.arrowView != null) {
            int rotate;
            if (this.mGraphyGet.isFoldedBestItem()) {
                rotate = this.mManagerDegree - 90;
            } else {
                rotate = this.mManagerDegree + 90;
            }
            holder.arrowView.setRotation((float) rotate);
        }
    }

    protected void rotateMyFilterArrow(ViewHolder holder) {
        if (holder != null && holder.arrowView != null) {
            int rotate;
            if (this.mGraphyGet.isFoldedMyFilterItem()) {
                rotate = this.mManagerDegree + 90;
            } else {
                rotate = this.mManagerDegree - 90;
            }
            holder.arrowView.setRotation((float) rotate);
        }
    }

    protected void smoothScrollToPosition(final ViewHolder holder) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (holder != null && holder.itemView != null && GraphyViewManagerExt.this.mGraphyRecyclerView != null) {
                    GraphyViewManagerExt.this.mGraphyRecyclerView.smoothScrollBy(((int) holder.itemView.getX()) - ((RatioCalcUtil.getSizeCalculatedByPercentage(GraphyViewManagerExt.this.getAppContext(), false, 1.0f) - GraphyViewManagerExt.this.mListViewItemWidth) / 2), 0);
                }
            }
        });
    }

    public void hideDetailView() {
        if (this.mDetailViewHandler != null) {
            this.mDetailViewHandler.hide();
        }
        if (this.mGraphyAdapter != null) {
            this.mGraphyAdapter.setIsLongClick(false);
        }
    }

    public boolean isDetailviewVisible() {
        if (this.mDetailViewHandler != null) {
            return this.mDetailViewHandler.isDetailViewVisible();
        }
        return false;
    }

    protected boolean isListAnimating() {
        if (this.mGraphyRecyclerViewWrapper != null) {
            Animation animation = this.mGraphyRecyclerViewWrapper.getAnimation();
            if (animation != null) {
                return animation.hasStarted();
            }
        }
        return false;
    }

    protected boolean isButtonAnimating() {
        if (this.mGraphyButtonLayout != null) {
            Animation animation = this.mGraphyButtonLayout.getAnimation();
            if (animation != null) {
                return animation.hasStarted();
            }
        }
        return false;
    }
}
