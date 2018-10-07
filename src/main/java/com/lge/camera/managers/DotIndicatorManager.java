package com.lge.camera.managers;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.Utils;

public class DotIndicatorManager extends ManagerInterfaceImpl {
    private RotateLayout mDotIndicatorBaseView;
    private boolean mFeatureEnable = false;
    private ImageButton mFirstIndicator;
    private boolean mIsNotUpdateDotIndicator = false;
    private DotIndicatorInterface mListener;
    private ImageButton mSecondIndicator;
    private ImageButton mThirdIndicator;

    public interface DotIndicatorInterface {
        boolean filterMenuAvailable();
    }

    public DotIndicatorManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setDotIndicatorListener(DotIndicatorInterface listener) {
        this.mListener = listener;
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (this.mFeatureEnable) {
            this.mDotIndicatorBaseView = (RotateLayout) this.mGet.findViewById(C0088R.id.dot_page_indicator_layout);
            this.mFirstIndicator = (ImageButton) this.mDotIndicatorBaseView.findViewById(C0088R.id.first_indicator);
            setFilterMenuVisibility();
            this.mSecondIndicator = (ImageButton) this.mDotIndicatorBaseView.findViewById(C0088R.id.second_indicator);
            this.mThirdIndicator = (ImageButton) this.mDotIndicatorBaseView.findViewById(C0088R.id.third_indicator);
            updateIndicatorPosition(2);
            LinearLayout dotIndicatorLayout = (LinearLayout) this.mDotIndicatorBaseView.findViewById(C0088R.id.dot_page_indicator_view);
            LayoutParams rlp = (LayoutParams) dotIndicatorLayout.getLayoutParams();
            if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                rlp.setMarginsRelative(0, 0, 0, (Utils.getLCDsize(getAppContext(), true)[0] / 2) + ((int) Utils.dpToPx(getAppContext(), 6.0f)));
            } else {
                rlp.setMarginsRelative(0, 0, 0, (int) Utils.dpToPx(getAppContext(), 43.0f));
            }
            dotIndicatorLayout.setLayoutParams(rlp);
            setDegree(getOrientationDegree(), false);
        }
    }

    public void setFilterMenuVisibility() {
        if (this.mListener != null && this.mFirstIndicator != null) {
            if (this.mListener.filterMenuAvailable()) {
                this.mFirstIndicator.setVisibility(0);
            } else {
                this.mFirstIndicator.setVisibility(8);
            }
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        this.mDotIndicatorBaseView = null;
    }

    public void setTriggerBlockUpdateDotIndicator() {
        this.mIsNotUpdateDotIndicator = true;
    }

    public void updateIndicatorPosition(int position) {
        if (this.mFirstIndicator != null && this.mSecondIndicator != null && this.mThirdIndicator != null) {
            if (this.mIsNotUpdateDotIndicator) {
                this.mIsNotUpdateDotIndicator = false;
                return;
            }
            switch (position) {
                case 1:
                    this.mFirstIndicator.setSelected(true);
                    this.mSecondIndicator.setSelected(false);
                    this.mThirdIndicator.setSelected(false);
                    return;
                case 2:
                    this.mFirstIndicator.setSelected(false);
                    this.mSecondIndicator.setSelected(true);
                    this.mThirdIndicator.setSelected(false);
                    return;
                case 3:
                    this.mFirstIndicator.setSelected(false);
                    this.mSecondIndicator.setSelected(false);
                    this.mThirdIndicator.setSelected(true);
                    return;
                default:
                    return;
            }
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mDotIndicatorBaseView != null) {
            this.mDotIndicatorBaseView.rotateLayout(degree);
            LinearLayout dotIndicatorLayout = (LinearLayout) this.mDotIndicatorBaseView.findViewById(C0088R.id.dot_page_indicator_view);
            LayoutParams rlp = (LayoutParams) dotIndicatorLayout.getLayoutParams();
            if (degree == 0 || degree == 180) {
                dotIndicatorLayout.setVisibility(0);
            } else {
                dotIndicatorLayout.setVisibility(4);
            }
        }
    }

    public void show() {
        if (this.mDotIndicatorBaseView != null) {
            this.mDotIndicatorBaseView.setVisibility(0);
        }
    }

    public void hide() {
        if (this.mDotIndicatorBaseView != null) {
            this.mDotIndicatorBaseView.setVisibility(4);
        }
    }
}
