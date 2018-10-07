package com.lge.camera.managers.ext;

import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.Utils;

class CinemaFilterManagerBase extends ManagerInterfaceImpl {
    protected final float CINE_LIST_TOP_POSITION = 0.336f;
    protected boolean isRTL = Utils.isRTLLanguage();
    protected View mBubbleBox;
    protected View mBubbleTextLayout;
    protected CinemaFilterInterface mCineFilterGet = null;
    protected int mCurrentLUTIndex = 0;
    protected View mLUTControllerLayout;
    protected ListView mLUTListView;
    protected int mListItemWidth = 0;

    /* renamed from: com.lge.camera.managers.ext.CinemaFilterManagerBase$1 */
    class C11941 implements OnLayoutChangeListener {
        C11941() {
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (CinemaFilterManagerBase.this.mLUTListView == null) {
                CamLog.m5e(CameraConstants.TAG, "mLUTListView is null");
                return;
            }
            CinemaFilterManagerBase.this.mLUTListView.removeOnLayoutChangeListener(this);
            if (CinemaFilterManagerBase.this.mBubbleBox == null || CinemaFilterManagerBase.this.mBubbleTextLayout == null) {
                CamLog.m3d(CameraConstants.TAG, "mBubbleBox or mBubbleTextLayout is null return");
                return;
            }
            int[] location = new int[2];
            View item = CinemaFilterManagerBase.this.mLUTListView.getChildAt((int) Math.floor((double) (((float) (CinemaFilterManagerBase.this.mLUTListView.getLastVisiblePosition() - CinemaFilterManagerBase.this.mLUTListView.getFirstVisiblePosition())) / 2.0f)));
            item.getLocationOnScreen(location);
            int itemStart = CinemaFilterManagerBase.this.isRTL ? location[0] - item.getWidth() : location[0];
            LayoutParams lp = (LayoutParams) CinemaFilterManagerBase.this.mBubbleBox.getLayoutParams();
            lp.setMarginStart(itemStart);
            CinemaFilterManagerBase.this.mBubbleBox.setLayoutParams(lp);
            lp = (LayoutParams) CinemaFilterManagerBase.this.mBubbleTextLayout.getLayoutParams();
            lp.setMarginStart(itemStart - ((CinemaFilterManagerBase.this.mBubbleTextLayout.getMeasuredWidth() - item.getWidth()) / 2));
            CinemaFilterManagerBase.this.mBubbleTextLayout.setLayoutParams(lp);
            CinemaFilterManagerBase.this.mBubbleBox.setVisibility(0);
            CinemaFilterManagerBase.this.mBubbleTextLayout.setVisibility(0);
            CamLog.m3d(CameraConstants.TAG, "show Cine Effect Bubble Help");
            SharedPreferenceUtil.setCineEffectBubbleShown(CinemaFilterManagerBase.this.getAppContext(), true);
            CinemaFilterManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(CinemaFilterManagerBase.this) {
                public void handleRun() {
                    CinemaFilterManagerBase.this.hideBubbleHelp();
                }
            }, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
        }
    }

    public CinemaFilterManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initBubbleHelp();
    }

    private void initBubbleHelp() {
        if (!SharedPreferenceUtil.getCineEffectBubbleShown(getAppContext())) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                this.mGet.inflateView(C0088R.layout.cine_bubble_help_layout, vg);
                this.mBubbleTextLayout = vg.findViewById(C0088R.id.cine_bubble_text_layout);
                this.mBubbleBox = vg.findViewById(C0088R.id.cine_bubble_box);
                if (this.mBubbleTextLayout != null && this.mBubbleBox != null) {
                    setBubbleTextLayout();
                    LayoutParams lp = (LayoutParams) this.mBubbleBox.getLayoutParams();
                    lp.width = this.mListItemWidth;
                    lp.height = this.mListItemWidth;
                    lp.addRule(20, 1);
                    lp.addRule(12, 1);
                    lp.bottomMargin = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.336f) - Utils.getPx(getAppContext(), C0088R.dimen.cinema_listView_padding)) - this.mListItemWidth;
                    this.mBubbleBox.setLayoutParams(lp);
                }
            }
        }
    }

    protected void setBubbleTextLayout() {
        if (this.mBubbleTextLayout != null) {
            TextView bubbleTextView = (TextView) this.mBubbleTextLayout.findViewById(C0088R.id.graphy_init_guide_txt);
            if (bubbleTextView != null) {
                bubbleTextView.setMaxWidth(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.4222f));
                LayoutParams lp = (LayoutParams) this.mBubbleTextLayout.getLayoutParams();
                lp.addRule(20, 1);
                lp.addRule(12, 1);
                int bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.336f) + Utils.getPx(getAppContext(), C0088R.dimen.cinema_bubble_text_marginBottom);
                if (this.mManagerDegree % 180 == 0) {
                    this.mBubbleTextLayout.setBackgroundResource(C0088R.drawable.camera_cine_help_bubble_ver);
                    this.mBubbleTextLayout.setRotation(0.0f);
                    bubbleTextView.setRotation((float) this.mManagerDegree);
                    this.mBubbleTextLayout.measure(0, 0);
                } else {
                    this.mBubbleTextLayout.setBackgroundResource(C0088R.drawable.camera_cine_help_bubble);
                    this.mBubbleTextLayout.setRotation(90.0f);
                    bubbleTextView.setRotation((float) (this.mManagerDegree + 90));
                    this.mBubbleTextLayout.measure(0, 0);
                    bottomMargin += (this.mBubbleTextLayout.getMeasuredWidth() - this.mBubbleTextLayout.getMeasuredHeight()) / 2;
                }
                lp.bottomMargin = bottomMargin;
                this.mBubbleTextLayout.setLayoutParams(lp);
            }
        }
    }

    protected void showBubbleHelp() {
        if (this.mLUTListView == null || this.mBubbleBox == null) {
            CamLog.m3d(CameraConstants.TAG, "mLUTListView = " + this.mLUTListView);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "mCurrentLUTIndex = " + this.mCurrentLUTIndex);
        this.mLUTListView.addOnLayoutChangeListener(new C11941());
    }

    protected void hideBubbleHelp() {
        if (this.mBubbleBox != null && this.mBubbleBox.getVisibility() == 0 && this.mBubbleTextLayout != null) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
            if (vg != null) {
                this.mBubbleBox.setVisibility(8);
                this.mBubbleTextLayout.setVisibility(8);
                vg.removeView(this.mBubbleBox);
                vg.removeView(this.mBubbleTextLayout);
                this.mBubbleBox = null;
                this.mBubbleTextLayout = null;
                CamLog.m3d(CameraConstants.TAG, "hide Cine Effect Bubble Help");
            }
        }
    }

    public boolean isCinemaLUTVisible() {
        return this.mLUTControllerLayout != null && this.mLUTControllerLayout.getVisibility() == 0;
    }
}
