package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;

public class SpliceViewImageImportManager extends SpliceViewImageImportManagerBase {

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$1 */
    class C13171 implements OnTouchListener {
        C13171() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManager.this.mIsAnimationStarted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$2 */
    class C13182 implements OnTouchListener {
        C13182() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManager.this.mIsAnimationStarted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$3 */
    class C13193 implements OnTouchListener {
        C13193() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManager.this.mIsAnimationStarted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$4 */
    class C13204 implements OnTouchListener {
        C13204() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (SpliceViewImageImportManager.this.mIsAnimationStarted) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$5 */
    class C13215 implements OnClickListener {
        C13215() {
        }

        public void onClick(View arg0) {
            SpliceViewImageImportManager.this.mAngleListener.onSwitchDualCamera(0, SpliceViewImageImportManager.this.isRear(SpliceViewImageImportManager.this.mListener.getCameraArray()[0]) ? 0 : 1);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$6 */
    class C13226 implements OnClickListener {
        C13226() {
        }

        public void onClick(View arg0) {
            SpliceViewImageImportManager.this.mAngleListener.onSwitchDualCamera(0, SpliceViewImageImportManager.this.isRear(SpliceViewImageImportManager.this.mListener.getCameraArray()[0]) ? 2 : 3);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$7 */
    class C13237 implements OnClickListener {
        C13237() {
        }

        public void onClick(View arg0) {
            int i;
            boolean rear = SpliceViewImageImportManager.this.isRear(SpliceViewImageImportManager.this.mListener.getCameraArray()[1]);
            SpliceViewCameraAngleListener spliceViewCameraAngleListener = SpliceViewImageImportManager.this.mAngleListener;
            if (rear) {
                i = 0;
            } else {
                i = 1;
            }
            spliceViewCameraAngleListener.onSwitchDualCamera(1, i);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SpliceViewImageImportManager$8 */
    class C13248 implements OnClickListener {
        C13248() {
        }

        public void onClick(View arg0) {
            SpliceViewImageImportManager.this.mAngleListener.onSwitchDualCamera(1, SpliceViewImageImportManager.this.isRear(SpliceViewImageImportManager.this.mListener.getCameraArray()[1]) ? 2 : 3);
        }
    }

    public SpliceViewImageImportManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setSpliceManagerAngleListener(SpliceViewCameraAngleListener listener) {
        this.mAngleListener = listener;
    }

    public void initLayout() {
        CamLog.m3d(CameraConstants.TAG, "-spliceview- init layout");
        removeViews();
        this.mIsImportedCompleted = false;
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        this.mImportViewLayout = this.mGet.inflateView(C0088R.layout.splice_view_import_image_layout);
        if (!(vg == null || this.mImportViewLayout == null)) {
            vg.addView(this.mImportViewLayout);
            this.mSwapButton = (ImageView) this.mImportViewLayout.findViewById(C0088R.id.splice_swap_button);
            this.mImportButton = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.splice_import_button);
            this.mRotateButton = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.splice_rotate_button);
            this.mSpliceLayout = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.splice_layout);
            this.mSwapBtnLayout = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.splice_swap_button_layout);
            this.mImportButtonLayout = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.splice_import_layout);
            this.mSplicePrePostView = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.splice_pre_post_view);
            this.mSplicePrePostImageFirst = (TouchImageView) this.mImportViewLayout.findViewById(C0088R.id.splice_pre_post_view_first);
            this.mSplicePrePostImageFirst.setTouchImageViewInterface(this);
            this.mSplicePrePostImageSecond = (TouchImageView) this.mImportViewLayout.findViewById(C0088R.id.splice_pre_post_view_second);
            this.mSplicePrePostImageSecond.setTouchImageViewInterface(this);
            this.mSplicePrePostImageFirstBg = (ImageView) this.mImportViewLayout.findViewById(C0088R.id.splice_pre_post_view_first_bg);
            this.mSplicePrePostImageSecondBg = (ImageView) this.mImportViewLayout.findViewById(C0088R.id.splice_pre_post_view_second_bg);
            this.mSpliceGuideCueLayout = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.splice_guide_cue);
            this.mGuideTextTop = (RotateTextView) this.mImportViewLayout.findViewById(C0088R.id.splice_guide_cue_top);
            this.mGuideTextBottom = (RotateTextView) this.mImportViewLayout.findViewById(C0088R.id.splice_guide_cue_bottom);
            setupDualCameraLayout();
            setupCueLayout();
            resizeViewSize();
            setButtonListener();
            if (this.mGet.isSpliceViewImporteImage()) {
                showSwapAndRotateButton(true);
                changeImportButtonLayoutLocation();
                setImageToPrePostView(true);
            }
            if (this.mSpliceInteface != null) {
                if (this.mSpliceInteface.isPostviewVisible()) {
                    showImportLayout(false);
                }
                if (!this.mSpliceInteface.isFrameShot()) {
                    showImportButton(false);
                    showSwapAndRotateButton(false);
                }
            }
        }
        setDegree(this.mGet.getOrientationDegree(), false);
    }

    public void onDestroy() {
        super.onDestroy();
        removeViews();
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mListener != null && this.mImportButton != null && this.mRotateButton != null && this.mGuideTextTop != null && this.mGuideTextBottom != null && this.mDualNormalTop != null && this.mDualNormalBottom != null && this.mDualWideTop != null && this.mDualWideBottom != null) {
            super.setDegree(degree, animation);
            this.mListener.onRotateDegree(degree);
            this.mImportButton.setDegree(degree, animation);
            this.mRotateButton.setDegree(degree, animation);
            this.mGuideTextTop.setDegree(degree, animation);
            this.mGuideTextBottom.setDegree(degree, animation);
            this.mDualNormalTop.setDegree(degree, animation);
            this.mDualNormalBottom.setDegree(degree, animation);
            this.mDualWideTop.setDegree(degree, animation);
            this.mDualWideBottom.setDegree(degree, animation);
        }
    }

    public void hidePrePostLayout() {
        if (this.mSplicePrePostView == null) {
            CamLog.m5e(CameraConstants.TAG, "null check");
        } else {
            this.mSplicePrePostView.setVisibility(8);
        }
    }

    public void resetPrePostLayout() {
        hidePrePostView(this.mSplicePrePostImageFirst, this.mSplicePrePostImageFirstBg);
        hidePrePostView(this.mSplicePrePostImageSecond, this.mSplicePrePostImageSecondBg);
        hidePrePostLayout();
    }

    public void resetPrePostViewDegree(float angle) {
        if (this.mSplicePrePostImageFirst != null && this.mSplicePrePostImageSecond != null) {
            this.mSplicePrePostImageFirst.setRotation(angle);
            this.mSplicePrePostImageSecond.setRotation(angle);
        }
    }

    public void processForTimerShotCancel() {
        showDualCamLayoutAll(true);
        if (this.mSpliceInteface.getFrameshotCount() == 0) {
            showImportButton(true);
        } else if (this.mGet.isSpliceViewImporteImage()) {
            showImportButton(true);
            showSwapAndRotateButton(true);
        }
    }

    public void setCurrentImportState() {
        if (this.mImportButton == null || this.mSwapButton == null || this.mRotateButton == null) {
            this.mCurrentImportState = sIMPORT_STATE_IMPORT_LAYOUT_NONE;
            return;
        }
        showDualCamLayoutAll(false);
        int state = sIMPORT_STATE_IMPORT_LAYOUT_NONE;
        if (this.mImportButton.getVisibility() == 0) {
            state = sIMPORT_STATE_IMPORT_IDLE;
        } else if (this.mSwapButton.getVisibility() == 0 && this.mRotateButton.getVisibility() == 0) {
            state = sIMPORT_STATE_IMPORT_ALREADY;
        }
        this.mCurrentImportState = state;
    }

    public boolean getGalleryButtonClicked() {
        return this.mIsGalleryButtonClicked;
    }

    public void resetGalleryButtonClicked() {
        this.mIsGalleryButtonClicked = false;
    }

    protected void setButtonListener() {
        super.setButtonListener();
        this.mDualNormalTop.setOnTouchListener(new C13171());
        this.mDualWideTop.setOnTouchListener(new C13182());
        this.mDualNormalBottom.setOnTouchListener(new C13193());
        this.mDualWideBottom.setOnTouchListener(new C13204());
        setDualCamIconListener();
        updateDualCameraIcon();
    }

    protected void setDualCamIconListener() {
        this.mDualNormalTop.setOnClickListener(new C13215());
        this.mDualWideTop.setOnClickListener(new C13226());
        this.mDualNormalBottom.setOnClickListener(new C13237());
        this.mDualWideBottom.setOnClickListener(new C13248());
    }

    public void updateDualCameraIcon() {
        int[] camIdArray = this.mListener == null ? null : this.mListener.getCameraArray();
        if (camIdArray != null && this.mDualNormalTop != null && this.mDualNormalBottom != null && this.mDualWideTop != null && this.mDualWideBottom != null) {
            boolean isTopShowing = true;
            if (!isRear(camIdArray[0]) && FunctionProperties.getCameraTypeFront() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Top FrontCamera only normal");
                isTopShowing = false;
            }
            if (isRear(camIdArray[0]) && FunctionProperties.getCameraTypeRear() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Top RearCamera only normal");
                isTopShowing = false;
            }
            if (isTopShowing && camIdArray[0] < 4) {
                setDualCamIconSelected(this.mDualNormalTop, this.mDualWideTop, isWideAngle(camIdArray[0]));
                setDualCamIconImage(this.mDualNormalTop, this.mDualWideTop, isRear(camIdArray[0]));
                setDescriptionText(this.mDualNormalTop, this.mDualWideTop, camIdArray[0]);
            }
            boolean isBottomShowing = true;
            if (!isRear(camIdArray[1]) && FunctionProperties.getCameraTypeFront() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Bottom FrontCamera only normal");
                isBottomShowing = false;
            }
            if (isRear(camIdArray[1]) && FunctionProperties.getCameraTypeRear() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Bottom RearCamera only normal");
                isBottomShowing = false;
            }
            if (isBottomShowing && camIdArray[1] < 4) {
                setDualCamIconSelected(this.mDualNormalBottom, this.mDualWideBottom, isWideAngle(camIdArray[1]));
                setDualCamIconImage(this.mDualNormalBottom, this.mDualWideBottom, isRear(camIdArray[1]));
                setDescriptionText(this.mDualNormalBottom, this.mDualWideBottom, camIdArray[1]);
            }
        }
    }

    private void setDescriptionText(RotateImageButton normalButton, RotateImageButton wideButton, int camId) {
        if (isRear(camId)) {
            normalButton.setContentDescription(this.mGet.getActivity().getString(C0088R.string.normal_angle_lens));
            wideButton.setContentDescription(this.mGet.getActivity().getString(C0088R.string.wide_angle_lens));
            return;
        }
        normalButton.setContentDescription(this.mGet.getActivity().getString(C0088R.string.selfie_camera));
        wideButton.setContentDescription(this.mGet.getActivity().getString(C0088R.string.groupfie_camera));
    }

    public boolean isWideAngle(int camId) {
        return camId == 3 || camId == 2;
    }

    private boolean isRear(int camId) {
        return camId == 0 || camId == 2;
    }

    private void setDualCamIconSelected(RotateImageButton normal, RotateImageButton wide, boolean isWideAngle) {
        normal.setSelected(!isWideAngle);
        wide.setSelected(isWideAngle);
    }

    private void setDualCamIconImage(RotateImageButton normal, RotateImageButton wide, boolean isRear) {
        normal.setImageResource(isRear ? C0088R.drawable.btn_dualview_angle : C0088R.drawable.btn_dualview_angle_front);
        wide.setImageResource(isRear ? C0088R.drawable.btn_dualview_wide_angle : C0088R.drawable.btn_dualview_wide_angle_front);
    }

    public void updateDualCamLayoutShowing(int viewIdx, boolean isShow) {
        int i = 0;
        CamLog.m3d(CameraConstants.TAG, "viewIdx : " + viewIdx + ", isShow : " + isShow);
        RelativeLayout relativeLayout;
        if (this.mListener == null || this.mListener.getCameraArray() == null) {
            CamLog.m3d(CameraConstants.TAG, CameraConstants.NULL);
        } else if (!isShow) {
            RelativeLayout targetLayout = viewIdx == 0 ? this.mDoubleCamLayoutTop : this.mDoubleCamLayoutBottom;
            if (targetLayout != null) {
                CamLog.m3d(CameraConstants.TAG, "INVISIBLE");
                targetLayout.setVisibility(4);
            }
        } else if (viewIdx == 0) {
            boolean isTopShowing = true;
            if (!isRear(this.mListener.getCameraArray()[0]) && FunctionProperties.getCameraTypeFront() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Top Front one normal");
                isTopShowing = false;
            }
            if (isRear(this.mListener.getCameraArray()[0]) && FunctionProperties.getCameraTypeRear() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Top Rear one normal");
                isTopShowing = false;
            }
            if (this.mDoubleCamLayoutTop != null) {
                relativeLayout = this.mDoubleCamLayoutTop;
                if (!isTopShowing) {
                    i = 4;
                }
                relativeLayout.setVisibility(i);
            }
        } else {
            boolean isBottomShowing = true;
            if (!isRear(this.mListener.getCameraArray()[1]) && FunctionProperties.getCameraTypeFront() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Bottom Front one normal");
                isBottomShowing = false;
            }
            if (isRear(this.mListener.getCameraArray()[1]) && FunctionProperties.getCameraTypeRear() == 0) {
                CamLog.m3d(CameraConstants.TAG, "Bottom Rear one normal");
                isBottomShowing = false;
            }
            if (this.mDoubleCamLayoutBottom != null) {
                relativeLayout = this.mDoubleCamLayoutBottom;
                if (!isBottomShowing) {
                    i = 4;
                }
                relativeLayout.setVisibility(i);
            }
        }
    }

    public void showDualCamLayoutAll(boolean isShow) {
        if (this.mDoubleCamLayoutAll != null) {
            this.mDoubleCamLayoutAll.setVisibility(isShow ? 0 : 8);
        }
    }

    protected void setupDualCameraLayout() {
        boolean z;
        boolean z2 = false;
        this.mDoubleCamLayoutAll = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.dualview_button_all);
        this.mDoubleCamLayoutTop = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.dualview_button_layout_top);
        this.mDoubleCamLayoutBottom = (RelativeLayout) this.mImportViewLayout.findViewById(C0088R.id.dualview_button_layout_bottom);
        this.mDualNormalTop = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.btn_dualview_normal_range_top);
        this.mDualWideTop = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.btn_dualview_wide_range_top);
        this.mDualNormalBottom = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.btn_dualview_normal_range_bottom);
        this.mDualWideBottom = (RotateImageButton) this.mImportViewLayout.findViewById(C0088R.id.btn_dualview_wide_range_bottom);
        if (isRear(this.mListener.getCameraArray()[0])) {
            z = false;
        } else {
            z = true;
        }
        updateDualCamLayoutShowing(0, z);
        if (!isRear(this.mListener.getCameraArray()[1])) {
            z2 = true;
        }
        updateDualCamLayoutShowing(1, z2);
    }

    public void setReverseState(boolean isReverse) {
        this.mIsReverse = isReverse;
        CamLog.m3d(CameraConstants.TAG, "mIsReverse : " + this.mIsReverse);
    }

    public boolean getReverseState() {
        return this.mIsReverse;
    }

    public void setPrePostBitmap(Bitmap bm) {
        if (!(bm != null || this.mPrePostviewBitmap == null || this.mPrePostviewBitmap.isRecycled())) {
            this.mPrePostviewBitmap.recycle();
        }
        this.mPrePostviewBitmap = bm;
    }

    public Bitmap getPrePostBitmap() {
        return this.mPrePostviewBitmap;
    }

    public boolean getSpliceLayoutVisibility() {
        if (this.mSpliceLayout == null || this.mSpliceLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }
}
