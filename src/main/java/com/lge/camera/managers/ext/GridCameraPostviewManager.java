package com.lge.camera.managers.ext;

import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.R;
import com.lge.camera.C0088R;
import com.lge.camera.components.ReverseRelativeLayout;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class GridCameraPostviewManager extends GridCameraPostviewManagerBase {
    protected static final int VIEW_TYPE_HALF = 0;
    protected static final int VIEW_TYPE_HALF_QUARTER = 1;
    protected static int sHALF_SIZE = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected static int sQUARTER_SIZE = CameraConstantsEx.HD_SCREEN_RESOLUTION;
    protected boolean mIsQuickDrawerOpened = false;
    protected boolean mIsSaveButtonClicked = false;
    protected boolean mIsSavingProcess = false;

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$10 */
    class C120510 implements OnTouchListener {
        C120510() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (GridCameraPostviewManager.this.isSystemBarVisible() || GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$11 */
    class C120611 implements OnTouchListener {
        C120611() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (GridCameraPostviewManager.this.isSystemBarVisible() || GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$12 */
    class C120712 implements OnTouchListener {
        C120712() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (GridCameraPostviewManager.this.isSystemBarVisible() || GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$1 */
    class C12081 implements OnClickListener {
        C12081() {
        }

        public void onClick(View arg0) {
            if (!GridCameraPostviewManager.this.isSystemBarVisible()) {
                if ((GridCameraPostviewManager.this.mListener == null || !GridCameraPostviewManager.this.mListener.isCountDown()) && !GridCameraPostviewManager.this.checkReocrdingState() && !GridCameraPostviewManager.this.mIsQuickDrawerOpened && !GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                    if (GridCameraPostviewManager.this.mListener != null && GridCameraPostviewManager.this.mListener.isRetakeMode() && GridCameraPostviewManager.this.mListener.getRetakeCurrentIndex() == 0) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(0, false);
                    } else if (GridCameraPostviewManager.this.getCapturedCount() >= 1 && GridCameraPostviewManager.this.mListener != null) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(0, true);
                        GridCameraPostviewManager.this.setHighlightView(0, true);
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$2 */
    class C12092 implements OnClickListener {
        C12092() {
        }

        public void onClick(View arg0) {
            if (!GridCameraPostviewManager.this.isSystemBarVisible()) {
                if ((GridCameraPostviewManager.this.mListener == null || !GridCameraPostviewManager.this.mListener.isCountDown()) && !GridCameraPostviewManager.this.checkReocrdingState() && !GridCameraPostviewManager.this.mIsQuickDrawerOpened && !GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                    if (GridCameraPostviewManager.this.mListener != null && GridCameraPostviewManager.this.mListener.isRetakeMode() && GridCameraPostviewManager.this.mListener.getRetakeCurrentIndex() == 1) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(1, false);
                    } else if (GridCameraPostviewManager.this.getCapturedCount() >= 2 && GridCameraPostviewManager.this.mListener != null) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(1, true);
                        GridCameraPostviewManager.this.setHighlightView(1, true);
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$3 */
    class C12103 implements OnClickListener {
        C12103() {
        }

        public void onClick(View arg0) {
            if (!GridCameraPostviewManager.this.isSystemBarVisible()) {
                if ((GridCameraPostviewManager.this.mListener == null || !GridCameraPostviewManager.this.mListener.isCountDown()) && !GridCameraPostviewManager.this.checkReocrdingState() && !GridCameraPostviewManager.this.mIsQuickDrawerOpened && !GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                    if (GridCameraPostviewManager.this.mListener != null && GridCameraPostviewManager.this.mListener.isRetakeMode() && GridCameraPostviewManager.this.mListener.getRetakeCurrentIndex() == 2) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(2, false);
                    } else if (GridCameraPostviewManager.this.getCapturedCount() >= 3 && GridCameraPostviewManager.this.mListener != null) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(2, true);
                        GridCameraPostviewManager.this.setHighlightView(2, true);
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$4 */
    class C12114 implements OnClickListener {
        C12114() {
        }

        public void onClick(View arg0) {
            if (!GridCameraPostviewManager.this.isSystemBarVisible()) {
                if ((GridCameraPostviewManager.this.mListener == null || !GridCameraPostviewManager.this.mListener.isCountDown()) && !GridCameraPostviewManager.this.checkReocrdingState() && !GridCameraPostviewManager.this.mIsQuickDrawerOpened && !GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                    if (GridCameraPostviewManager.this.mListener != null && GridCameraPostviewManager.this.mListener.isRetakeMode() && GridCameraPostviewManager.this.mListener.getRetakeCurrentIndex() == 3) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(3, false);
                    } else if (GridCameraPostviewManager.this.getCapturedCount() >= 4 && GridCameraPostviewManager.this.mListener != null) {
                        GridCameraPostviewManager.this.mListener.setRetakeMode(3, true);
                        GridCameraPostviewManager.this.setHighlightView(3, true);
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$5 */
    class C12125 implements OnTouchListener {
        C12125() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$6 */
    class C12136 implements OnTouchListener {
        C12136() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$7 */
    class C12147 implements OnClickListener {
        C12147() {
        }

        public void onClick(View arg0) {
            if (GridCameraPostviewManager.this.mIsSaveButtonClicked || GridCameraPostviewManager.this.isSystemBarVisible() || !GridCameraPostviewManager.this.checkContentsAvailable() || GridCameraPostviewManager.this.getCapturedCount() == 0 || GridCameraPostviewManager.this.mIsSavingProcess || (GridCameraPostviewManager.this.mListener != null && GridCameraPostviewManager.this.mListener.isRetakeMode())) {
                CamLog.m3d(CameraConstants.TAG, "blocking duplicated save button clicked");
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "save button clicked");
            GridCameraPostviewManager.this.mIsSaveButtonClicked = true;
            GridCameraPostviewManager.this.hideIndexTextview();
            GridCameraPostviewManager.this.saveContents(true);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$8 */
    class C12158 implements OnClickListener {
        C12158() {
        }

        public void onClick(View arg0) {
            if (!GridCameraPostviewManager.this.mIsSaveButtonClicked && !GridCameraPostviewManager.this.isSystemBarVisible() && !GridCameraPostviewManager.this.checkReocrdingState() && GridCameraPostviewManager.this.mListener != null) {
                GridCameraPostviewManager.this.mListener.onCancel();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManager$9 */
    class C12169 implements OnTouchListener {
        C12169() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            if (GridCameraPostviewManager.this.isSystemBarVisible() || GridCameraPostviewManager.this.mIsSaveButtonClicked) {
                return true;
            }
            return false;
        }
    }

    public GridCameraPostviewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initLayout() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        this.mPostViewLayout = this.mGet.inflateView(C0088R.layout.grid_post_view_layout);
        if (vg != null && this.mPostViewLayout != null) {
            View preview = this.mGet.findViewById(C0088R.id.preview_layout);
            int previewIndex = 1;
            if (preview != null) {
                previewIndex = ((RelativeLayout) preview.getParent()).indexOfChild(preview);
            }
            vg.addView(this.mPostViewLayout, previewIndex + 3);
            this.mGridPostviewLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_postview_layout);
            this.mPostviewGridContentsFirst = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_contents_first);
            this.mPostviewGridContentsSecond = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_contents_second);
            this.mPostviewGridContentsThird = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_contents_third);
            this.mPostviewGridContentsFourth = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_contents_fourth);
            this.mFirstImageView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_image_first);
            this.mSecondImageView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_image_second);
            this.mThirdImageView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_image_third);
            this.mFourthImageView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_image_fourth);
            this.mFirstImageHighrightView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_highright_first);
            this.mSecondImageHighrightView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_highright_second);
            this.mThirdImageHighrightView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_highright_third);
            this.mFourthImageHighrightView = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.grid_highright_fourth);
            this.mFirstTextView = (RotateTextView) this.mPostViewLayout.findViewById(C0088R.id.grid_text_first);
            this.mSecondTextView = (RotateTextView) this.mPostViewLayout.findViewById(C0088R.id.grid_text_second);
            this.mThirdTextView = (RotateTextView) this.mPostViewLayout.findViewById(C0088R.id.grid_text_third);
            this.mFourthTextView = (RotateTextView) this.mPostViewLayout.findViewById(C0088R.id.grid_text_fourth);
            this.mFirstImageViewWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_image_first_wrap);
            this.mSecondImageViewWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_image_second_wrap);
            this.mThirdImageViewWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_image_third_wrap);
            this.mFourthImageViewWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_image_fourth_wrap);
            this.mFirstTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_video_first_wrap);
            this.mSecondTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_video_second_wrap);
            this.mThirdTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_video_third_wrap);
            this.mFourthTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_video_fourth_wrap);
            this.mOkButton = (RotateImageButton) this.mPostViewLayout.findViewById(C0088R.id.grid_button_ok);
            this.mCancelButton = (RotateImageButton) this.mPostViewLayout.findViewById(C0088R.id.grid_button_cancel);
            this.mPostviewContentsLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_postview_contents_layout);
            this.mPreviewDummyLayout = (RotateLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_preview_area_dummy);
            this.mPostviewButtonLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_post_view_button_layout);
            this.mPostviewCover = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.grid_post_view_cover);
            this.mGridGuideText = (TextView) this.mPostViewLayout.findViewById(C0088R.id.retake_guide_text);
            LayoutParams rlpPreviewDummy = (LayoutParams) this.mPreviewDummyLayout.getLayoutParams();
            LayoutParams rlpCover = (LayoutParams) this.mPostviewCover.getLayoutParams();
            if (ModelProperties.getLCDType() == 2) {
                rlpPreviewDummy.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
                rlpCover.topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
            } else {
                rlpPreviewDummy.topMargin = 0;
                rlpCover.topMargin = 0;
            }
            this.mPreviewDummyLayout.setLayoutParams(rlpPreviewDummy);
            this.mPostviewCover.setLayoutParams(rlpCover);
            resizeViewSize();
            setTextView();
            setButtonListener();
            setTopImageViewsClickListener();
            setBottomImageViewsClickListener();
            setImageViewsTouchClickListener();
            setToFirstIndexHighlightView();
            setIndexTextViewPosition();
            setPostviewButtonLayoutPosition();
        }
    }

    private void setIndexTextViewPosition() {
        int topmargin = (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.056f) - Utils.getPx(getAppContext(), R.dimen.type_c01_dp)) / 2;
        int startEndMargin = Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin);
        LayoutParams firstlpm = (LayoutParams) this.mFirstTextView.getLayoutParams();
        firstlpm.topMargin = topmargin;
        firstlpm.setMarginStart(startEndMargin);
        this.mFirstTextView.setLayoutParams(firstlpm);
        LayoutParams secondlpm = (LayoutParams) this.mSecondTextView.getLayoutParams();
        secondlpm.topMargin = topmargin;
        secondlpm.setMarginEnd(startEndMargin);
        this.mSecondTextView.setLayoutParams(secondlpm);
        LayoutParams thirdlpm = (LayoutParams) this.mThirdTextView.getLayoutParams();
        thirdlpm.topMargin = topmargin;
        thirdlpm.setMarginStart(startEndMargin);
        this.mThirdTextView.setLayoutParams(thirdlpm);
        LayoutParams fourthlpm = (LayoutParams) this.mFourthTextView.getLayoutParams();
        fourthlpm.topMargin = topmargin;
        fourthlpm.setMarginEnd(startEndMargin);
        this.mFourthTextView.setLayoutParams(fourthlpm);
    }

    private void setPostviewButtonLayoutPosition() {
        LayoutParams lp = (LayoutParams) this.mPostviewButtonLayout.getLayoutParams();
        lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.81f);
        this.mPostviewButtonLayout.setLayoutParams(lp);
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        if (!this.mListener.isSavingOnPause()) {
            releaseCollageTexture();
        }
        if (this.mGet.isPaused()) {
            for (int i = 0; i < 4; i++) {
                releaseMediaPlayer(i);
                removeTextureView(i);
            }
        }
    }

    public void releaseCollageTexture() {
        if (this.mGridPostviewLayout != null) {
            if (this.mCollageTextureView != null) {
                this.mCollageTextureView.setVisibility(8);
                this.mCollageTextureView.setSurfaceTextureListener(null);
            }
            this.mGridPostviewLayout.removeView(this.mCollageTextureView);
        }
        this.mCollageTextureView = null;
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        if (getPostviewButtonsVisibility() && getCapturedCount() == 4) {
            for (int i = 0; i < 4; i++) {
                if (this.mContentsInfo[i].contentsType == 2) {
                    addTextureView(i);
                }
            }
        }
    }

    public void setPostviewButtonsVisibility(boolean visible) {
        if (this.mPostviewButtonLayout != null) {
            this.mPostviewButtonLayout.setVisibility(visible ? 0 : 8);
        }
    }

    public boolean getPostviewButtonsVisibility() {
        if (this.mPostviewButtonLayout == null || this.mPostviewButtonLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void setPostviewPreviewDummyVisibility(boolean visible) {
        if (this.mPreviewDummyLayout != null) {
            this.mPreviewDummyLayout.setVisibility(visible ? 0 : 8);
            if (visible) {
                setRotateDegree(this.mGet.getOrientationDegree(), false);
            }
        }
    }

    public boolean getPostviewPreviewDummyVisibility() {
        if (this.mPreviewDummyLayout == null || this.mPreviewDummyLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void setVideoContents() {
        if (!isImageOnly()) {
            for (int i = 0; i < this.mContentsInfo.length; i++) {
                releaseMediaPlayer(i);
                removeTextureView(i);
                if (this.mContentsInfo[i].contentsType == 2) {
                    addTextureView(i);
                }
            }
        }
    }

    public void addTextureView(int index) {
        if (!this.mIsSavingProcess) {
            switch (index) {
                case 0:
                    this.mFirstTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.grid_texture_view_comp_first);
                    this.mFirstTextureWrap.addView(this.mFirstTextureView);
                    initTextureView(this.mContentsInfo[index].isRearCam, this.mFirstTextureView, this.mFirstTextureWrap, this.mContentsInfo[index].degree);
                    setFirstTextureViewListener();
                    this.mFirstTextureView.setVisibility(0);
                    return;
                case 1:
                    this.mSecondTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.grid_texture_view_comp_second);
                    this.mSecondTextureWrap.addView(this.mSecondTextureView);
                    initTextureView(this.mContentsInfo[index].isRearCam, this.mSecondTextureView, this.mSecondTextureWrap, this.mContentsInfo[index].degree);
                    setSecondTextureViewListener();
                    this.mSecondTextureView.setVisibility(0);
                    return;
                case 2:
                    this.mThirdTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.grid_texture_view_comp_third);
                    this.mThirdTextureWrap.addView(this.mThirdTextureView);
                    initTextureView(this.mContentsInfo[index].isRearCam, this.mThirdTextureView, this.mThirdTextureWrap, this.mContentsInfo[index].degree);
                    setThirdTextureViewListener();
                    this.mThirdTextureView.setVisibility(0);
                    return;
                case 3:
                    this.mFourthTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.grid_texture_view_comp_fourth);
                    this.mFourthTextureWrap.addView(this.mFourthTextureView);
                    initTextureView(this.mContentsInfo[index].isRearCam, this.mFourthTextureView, this.mFourthTextureWrap, this.mContentsInfo[index].degree);
                    setFourthTextureViewListener();
                    this.mFourthTextureView.setVisibility(0);
                    return;
                default:
                    return;
            }
        }
    }

    public void releaseMediaPlayer(int index) {
        switch (index) {
            case 0:
                if (this.mFirstPlayer != null) {
                    this.mFirstPlayer.stop();
                    this.mFirstPlayer.release();
                    this.mFirstPlayer = null;
                    return;
                }
                return;
            case 1:
                if (this.mSecondPlayer != null) {
                    this.mSecondPlayer.stop();
                    this.mSecondPlayer.release();
                    this.mSecondPlayer = null;
                    return;
                }
                return;
            case 2:
                if (this.mThirdPlayer != null) {
                    this.mThirdPlayer.stop();
                    this.mThirdPlayer.release();
                    this.mThirdPlayer = null;
                    return;
                }
                return;
            case 3:
                if (this.mFourthPlayer != null) {
                    this.mFourthPlayer.stop();
                    this.mFourthPlayer.release();
                    this.mFourthPlayer = null;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void removeTextureView(int index) {
        switch (index) {
            case 0:
                if (this.mFirstTextureView != null) {
                    this.mFirstTextureView.setSurfaceTextureListener(null);
                    this.mFirstTextureView.setVisibility(8);
                    this.mFirstTextureWrap.removeView(this.mFirstTextureView);
                    this.mFirstTextureView = null;
                    return;
                }
                return;
            case 1:
                if (this.mSecondTextureView != null) {
                    this.mSecondTextureView.setSurfaceTextureListener(null);
                    this.mSecondTextureView.setVisibility(8);
                    this.mSecondTextureWrap.removeView(this.mSecondTextureView);
                    this.mSecondTextureView = null;
                    return;
                }
                return;
            case 2:
                if (this.mThirdTextureView != null) {
                    this.mThirdTextureView.setSurfaceTextureListener(null);
                    this.mThirdTextureView.setVisibility(8);
                    this.mThirdTextureWrap.removeView(this.mThirdTextureView);
                    this.mThirdTextureView = null;
                    return;
                }
                return;
            case 3:
                if (this.mFourthTextureView != null) {
                    this.mFourthTextureView.setSurfaceTextureListener(null);
                    this.mFourthTextureView.setVisibility(8);
                    this.mFourthTextureWrap.removeView(this.mFourthTextureView);
                    this.mFourthTextureView = null;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mOkButton != null && this.mCancelButton != null && this.mFirstTextView != null && this.mSecondTextView != null && this.mThirdTextView != null && this.mFourthTextView != null) {
            this.mOkButton.setDegree(degree, animation);
            this.mCancelButton.setDegree(degree, animation);
            this.mFirstTextView.setDegree(degree, animation);
            this.mSecondTextView.setDegree(degree, animation);
            this.mThirdTextView.setDegree(degree, animation);
            this.mFourthTextView.setDegree(degree, animation);
        }
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mPreviewDummyLayout != null && this.mPreviewDummyLayout.getVisibility() == 0) {
            this.mPreviewDummyLayout.rotateLayout(degree);
        }
    }

    protected void setTalkbackText(int index, boolean isImage, ImageView iv) {
        String msg = "";
        switch (index) {
            case 0:
                msg = msg + getActivity().getString(C0088R.string.sp_odinary_lable_first);
                break;
            case 1:
                msg = msg + getActivity().getString(C0088R.string.sp_odinary_lable_second);
                break;
            case 2:
                msg = msg + getActivity().getString(C0088R.string.sp_odinary_lable_third);
                break;
            case 3:
                msg = msg + getActivity().getString(C0088R.string.sp_odinary_lable_fourth);
                break;
        }
        if (isImage) {
            msg = msg + " " + getActivity().getString(C0088R.string.sp_image_SHORT);
        } else {
            msg = msg + " " + getActivity().getString(C0088R.string.sp_video_SHORT);
        }
        iv.setContentDescription(msg);
    }

    protected void setReverseImageView(int index, boolean isReverse) {
        switch (index) {
            case 0:
                this.mFirstImageViewWrap.setReverse(isReverse);
                return;
            case 1:
                this.mSecondImageViewWrap.setReverse(isReverse);
                return;
            case 2:
                this.mThirdImageViewWrap.setReverse(isReverse);
                return;
            case 3:
                this.mFourthImageViewWrap.setReverse(isReverse);
                return;
            default:
                return;
        }
    }

    public void setToFirstIndexHighlightView() {
        setHighlightView(0, false);
    }

    public void removeViews() {
        CamLog.m3d(CameraConstants.TAG, "removeViews");
        this.mPostViewLayout.setVisibility(8);
        this.mFirstImageViewWrap.setVisibility(8);
        this.mSecondImageViewWrap.setVisibility(8);
        this.mThirdImageViewWrap.setVisibility(8);
        this.mFourthImageViewWrap.setVisibility(8);
        resetImageViews();
        this.mPostviewContentsLayout.setVisibility(8);
        this.mPostViewLayout.setVisibility(8);
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_base);
        if (!(vg == null || this.mPostViewLayout == null)) {
            vg.removeView(this.mPostViewLayout);
        }
        this.mPostViewLayout = null;
    }

    public void hideIndexTextview() {
        CamLog.m3d(CameraConstants.TAG, "hideIndexTextview");
        this.mFirstTextView.setVisibility(8);
        this.mSecondTextView.setVisibility(8);
        this.mThirdTextView.setVisibility(8);
        this.mFourthTextView.setVisibility(8);
    }

    public void resetImageViews() {
        CamLog.m3d(CameraConstants.TAG, "resetImageViews");
        this.mFirstImageViewWrap.setReverse(false);
        this.mSecondImageViewWrap.setReverse(false);
        this.mThirdImageViewWrap.setReverse(false);
        this.mFourthImageViewWrap.setReverse(false);
        this.mFirstTextView.setVisibility(0);
        this.mSecondTextView.setVisibility(0);
        this.mThirdTextView.setVisibility(0);
        this.mFourthTextView.setVisibility(0);
        this.mFirstImageView.setImageBitmap(null);
        this.mSecondImageView.setImageBitmap(null);
        this.mThirdImageView.setImageBitmap(null);
        this.mFourthImageView.setImageBitmap(null);
        this.mFirstImageView.setVisibility(8);
        this.mSecondImageView.setVisibility(8);
        this.mThirdImageView.setVisibility(8);
        this.mFourthImageView.setVisibility(8);
        hideAllHighrightViews();
        for (int i = 0; i < this.mContentsInfo.length; i++) {
            releaseMediaPlayer(i);
            removeTextureView(i);
        }
    }

    protected void setTopImageViewsClickListener() {
        this.mFirstImageView.setOnClickListener(new C12081());
        this.mSecondImageView.setOnClickListener(new C12092());
    }

    protected void setBottomImageViewsClickListener() {
        this.mThirdImageView.setOnClickListener(new C12103());
        this.mFourthImageView.setOnClickListener(new C12114());
    }

    protected void setButtonListener() {
        this.mPreviewDummyLayout.setOnTouchListener(new C12125());
        this.mPostviewContentsLayout.setOnTouchListener(new C12136());
        this.mOkButton.setOnClickListener(new C12147());
        this.mCancelButton.setOnClickListener(new C12158());
    }

    protected int getCapturedCount() {
        if (this.mListener != null) {
            return this.mListener.getCurrentShotCount();
        }
        return 0;
    }

    public void saveContents(boolean isSaveBtnClicked) {
        if (!isSystemBarVisible() && checkContentsAvailable() && !this.mIsSavingProcess && getCapturedCount() != 0) {
            this.mIsSavingProcess = true;
            if (this.mListener == null) {
                return;
            }
            if (isImageOnly()) {
                CamLog.m3d(CameraConstants.TAG, "image save contents");
                if (!isSaveBtnClicked) {
                    hideIndexTextview();
                }
                this.mListener.onSaveContents(getImageInView(this.mPostviewContentsLayout), isSaveBtnClicked);
                setToFirstIndexHighlightView();
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "collage save contents");
            for (int i = 0; i < this.mContentsInfo.length; i++) {
                if (this.mContentsInfo[i].contentsType == 2) {
                    releaseMediaPlayer(i);
                    removeTextureView(i);
                }
            }
            setBitmapForCollage();
            this.mListener.onSaveContents(this.mContentsInfo, isSaveBtnClicked);
        }
    }

    protected void setTextView() {
        int size = this.mGet.getActivity().getResources().getDimensionPixelSize(R.dimen.type_c01_dp);
        String num = "";
        this.mFirstTextView.setText(String.format("%d", new Object[]{Integer.valueOf(1)}));
        this.mFirstTextView.setTextSize(size);
        this.mSecondTextView.setText(String.format("%d", new Object[]{Integer.valueOf(2)}));
        this.mSecondTextView.setTextSize(size);
        this.mThirdTextView.setText(String.format("%d", new Object[]{Integer.valueOf(3)}));
        this.mThirdTextView.setTextSize(size);
        this.mFourthTextView.setText(String.format("%d", new Object[]{Integer.valueOf(4)}));
        this.mFourthTextView.setTextSize(size);
        if (!FunctionProperties.isSupportedCollageRecording()) {
            this.mGridGuideText.setText(C0088R.string.cell_square_grid_post_retake_guide_pic_only);
        }
    }

    protected void resizeViewSize() {
        int[] lcdSize = Utils.getLCDsize(getAppContext(), true);
        sSCREEN_HEIGHT = lcdSize[1] * 2;
        sHALF_SIZE = lcdSize[1];
        sQUARTER_SIZE = sSCREEN_HEIGHT / 4;
        setRelativeLayoutParam(this.mPostviewContentsLayout, 0);
        setRelativeLayoutParam(this.mPreviewDummyLayout, 0);
        setRelativeLayoutParam(this.mPostviewGridContentsFirst, 1);
        setRelativeLayoutParam(this.mPostviewGridContentsSecond, 1);
        setRelativeLayoutParam(this.mPostviewGridContentsThird, 1);
        setRelativeLayoutParam(this.mPostviewGridContentsFourth, 1);
        setRelativeLayoutParam(this.mFirstTextureWrap, 1);
        setRelativeLayoutParam(this.mSecondTextureWrap, 1);
        setRelativeLayoutParam(this.mThirdTextureWrap, 1);
        setRelativeLayoutParam(this.mFourthTextureWrap, 1);
        setImageViewParam(this.mFirstImageHighrightView, 1);
        setImageViewParam(this.mSecondImageHighrightView, 1);
        setImageViewParam(this.mThirdImageHighrightView, 1);
        setImageViewParam(this.mFourthImageHighrightView, 1);
    }

    protected void setRelativeLayoutParam(RelativeLayout rl, int viewType) {
        LayoutParams param = (LayoutParams) rl.getLayoutParams();
        if (viewType == 0) {
            param.width = sHALF_SIZE;
            param.height = sHALF_SIZE;
        } else {
            param.width = sQUARTER_SIZE;
            param.height = sQUARTER_SIZE;
        }
        rl.setLayoutParams(param);
    }

    protected void setImageViewParam(ImageView iv, int viewType) {
        LayoutParams param = (LayoutParams) iv.getLayoutParams();
        if (viewType == 0) {
            param.width = sHALF_SIZE;
            param.height = sHALF_SIZE;
        } else {
            param.width = sQUARTER_SIZE;
            param.height = sQUARTER_SIZE;
        }
        iv.setLayoutParams(param);
    }

    public boolean checkReocrdingState() {
        return !this.mGet.checkModuleValidate(240);
    }

    protected boolean checkContentsAvailable() {
        return (this.mContentsInfo == null || this.mContentsInfo[0] == null || this.mContentsInfo[1] == null || this.mContentsInfo[2] == null || this.mContentsInfo[3] == null) ? false : true;
    }

    public void resetSaveButtonClickedFlag() {
        this.mIsSaveButtonClicked = false;
        this.mIsSavingProcess = false;
    }

    public void setImageViewsTouchClickListener() {
        this.mFirstImageView.setOnTouchListener(new C12169());
        this.mSecondImageView.setOnTouchListener(new C120510());
        this.mThirdImageView.setOnTouchListener(new C120611());
        this.mFourthImageView.setOnTouchListener(new C120712());
    }
}
