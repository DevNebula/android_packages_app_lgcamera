package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.ReverseRelativeLayout;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class SplicePostViewManager extends SplicePostViewManagerBase {
    protected static final int BOTTOM_GUIDE_TEXT_BOTTOM_MARGIN_LAND = 550;
    protected static final int BOTTOM_GUIDE_TEXT_TOP_MARGIN_DEGREE_0 = 96;
    protected static final int BOTTOM_GUIDE_TEXT_TOP_MARGIN_DEGREE_180 = 500;
    protected static final int POSTVIEW_BUTTON_DIVIDER = 1000;
    protected static final int POSTVIEW_BUTTON_RATIO = 111;
    protected static int sHALF_SIZE = CameraConstantsEx.QHD_SCREEN_RESOLUTION;
    protected static int sSCREEN_HEIGHT = 2880;

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManager$1 */
    class C13081 implements OnClickListener {
        C13081() {
        }

        public void onClick(View arg0) {
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManager$2 */
    class C13092 implements OnClickListener {
        C13092() {
        }

        public void onClick(View arg0) {
        }
    }

    public SplicePostViewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void initLayout() {
        if (!this.mIsInit) {
            ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
            this.mPostViewLayout = this.mGet.inflateView(C0088R.layout.splice_post_view_layout);
            if (vg != null && this.mPostViewLayout != null) {
                vg.addView(this.mPostViewLayout);
                this.mFirstImageView = (TouchImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_image_first);
                this.mFirstImageView.setTouchImageViewInterface(this);
                this.mSecondImageView = (TouchImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_image_second);
                this.mSecondImageView.setTouchImageViewInterface(this);
                this.mFirstTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.texture_view_component_top);
                this.mSecondTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.texture_view_component_bottom);
                this.mFirstVideoSplash = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_video_first_splash);
                this.mSecondVideoSplash = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_video_second_splash);
                this.mFirstImageViewBg = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_image_first_bg);
                this.mSecondImageViewBg = (ImageView) this.mPostViewLayout.findViewById(C0088R.id.splice_image_second_bg);
                this.mFirstTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_video_first_wrap);
                this.mFirstTextureWrap.addView(this.mFirstTextureView);
                this.mSecondTextureWrap = (ReverseRelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_video_second_wrap);
                this.mSecondTextureWrap.addView(this.mSecondTextureView);
                this.mOkButton = (RotateImageButton) this.mPostViewLayout.findViewById(C0088R.id.splice_button_ok);
                this.mCancelButton = (RotateImageButton) this.mPostViewLayout.findViewById(C0088R.id.splice_button_cancel);
                this.mPostviewContentsLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_postview_contents_layout);
                this.mPostViewDummy = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_dummy_view);
                this.mReadyToSaveLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_postview_layout);
                this.mPostViewButtonLayout = (RelativeLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_post_view_button_layout);
                this.mGuideTextAreaTop = (RotateLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_guide_text_area_top);
                this.mGuideTextAreaBottom = (RotateLayout) this.mPostViewLayout.findViewById(C0088R.id.splice_guide_text_area_bottom);
                this.mTextGuideBottom = (TextView) this.mPostViewLayout.findViewById(C0088R.id.guide_text_bottom);
                resizeViewSize();
                setButtonListener();
                setPostViewContentsVisibility();
                this.mIsInit = true;
                setPostviewButtonLayoutPosition();
            }
        }
    }

    private void setPostviewButtonLayoutPosition() {
        LayoutParams lp = (LayoutParams) this.mPostViewButtonLayout.getLayoutParams();
        lp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.81f);
        this.mPostViewButtonLayout.setLayoutParams(lp);
    }

    public void setPostViewContentsVisibility() {
        CamLog.m3d(CameraConstants.TAG, "mCurrentContentType : " + this.mCurrentContentType);
        this.mPostviewContentsLayout.setVisibility(0);
        switch (this.mCurrentContentType) {
            case 0:
                this.mFirstImageView.setVisibility(0);
                this.mSecondImageView.setVisibility(0);
                this.mFirstImageViewBg.setVisibility(0);
                this.mSecondImageViewBg.setVisibility(0);
                this.mFirstTextureView.setVisibility(8);
                this.mSecondTextureView.setVisibility(8);
                this.mFirstVideoSplash.setVisibility(8);
                this.mSecondVideoSplash.setVisibility(8);
                return;
            case 1:
                this.mFirstImageView.setVisibility(0);
                this.mFirstImageViewBg.setVisibility(0);
                this.mSecondImageView.setVisibility(8);
                this.mFirstVideoSplash.setVisibility(8);
                this.mSecondVideoSplash.setVisibility(0);
                this.mFirstTextureView.setVisibility(8);
                if (this.mSecondTextureView.getVisibility() != 0) {
                    this.mSecondTextureView.setVisibility(0);
                    return;
                }
                return;
            case 2:
                this.mFirstImageView.setVisibility(8);
                this.mSecondImageView.setVisibility(0);
                this.mSecondImageViewBg.setVisibility(0);
                this.mFirstVideoSplash.setVisibility(0);
                this.mSecondVideoSplash.setVisibility(8);
                if (this.mSecondTextureView.getVisibility() != 0) {
                    this.mFirstTextureView.setVisibility(0);
                }
                this.mSecondTextureView.setVisibility(8);
                return;
            case 3:
                this.mFirstImageView.setVisibility(8);
                this.mSecondImageView.setVisibility(8);
                this.mFirstVideoSplash.setVisibility(0);
                this.mSecondVideoSplash.setVisibility(0);
                this.mFirstTextureView.setVisibility(0);
                this.mSecondTextureView.setVisibility(0);
                return;
            default:
                return;
        }
    }

    public void setPostViewContents(Bitmap[] bm) {
        this.mFirstImageView.setImageBitmap(bm[0]);
        this.mSecondImageView.setImageBitmap(bm[1]);
        showGuideText(0);
    }

    public void setPostViewContents(Bitmap[] bm, int[] cameraId, int[] fileType, boolean isReverse, int degree, ArrayList<Integer> vidDegree) {
        this.mInfo = new ContentsInfo(isReverse, cameraId, fileType, degree, vidDegree);
        this.mIsReverse = isReverse;
        if (this.mGet.isSpliceViewImporteImage()) {
            if (isReverse) {
                setPostviewType(2);
                this.mSecondImageView.setImageBitmap(bm[0]);
                this.mFirstVideoSplash.setImageBitmap(bm[1]);
                this.mFirstVideoSplash.setVisibility(0);
                initTextureView(cameraId[0], this.mFirstTextureView, this.mFirstTextureWrap, 0);
                setFirstTextureViewListener();
                showGuideText(2);
            } else {
                setPostviewType(1);
                this.mSecondVideoSplash.setImageBitmap(bm[1]);
                this.mSecondVideoSplash.setVisibility(0);
                this.mFirstImageView.setImageBitmap(bm[0]);
                initTextureView(cameraId[1], this.mSecondTextureView, this.mSecondTextureWrap, 1);
                setSecondTextureViewListener();
                showGuideText(1);
            }
        } else if (fileType[0] == 2) {
            setPostviewType(2);
            this.mFirstVideoSplash.setImageBitmap(bm[0]);
            this.mFirstVideoSplash.setVisibility(0);
            this.mSecondImageView.setImageBitmap(bm[1]);
            initTextureView(cameraId[0], this.mFirstTextureView, this.mFirstTextureWrap, 0);
            setFirstTextureViewListener();
            showGuideText(2);
        } else {
            setPostviewType(1);
            this.mSecondVideoSplash.setImageBitmap(bm[1]);
            this.mSecondVideoSplash.setVisibility(0);
            this.mFirstImageView.setImageBitmap(bm[0]);
            initTextureView(cameraId[1], this.mSecondTextureView, this.mSecondTextureWrap, 1);
            setSecondTextureViewListener();
            showGuideText(1);
        }
        setPostViewContentsVisibility();
    }

    public void setPostViewContents(Bitmap[] bm, int[] cameraId, ArrayList<Integer> vidDegree) {
        this.mInfo = new ContentsInfo(false, cameraId, null, 0, vidDegree);
        setPostviewType(3);
        this.mFirstVideoSplash.setImageBitmap(bm[0]);
        this.mFirstVideoSplash.setVisibility(0);
        this.mSecondVideoSplash.setImageBitmap(bm[1]);
        this.mSecondVideoSplash.setVisibility(0);
        initTextureView(cameraId[0], this.mFirstTextureView, this.mFirstTextureWrap, 0);
        setFirstTextureViewListener();
        initTextureView(cameraId[1], this.mSecondTextureView, this.mSecondTextureWrap, 1);
        setSecondTextureViewListener();
        setPostViewContentsVisibility();
    }

    public Bitmap getRotatedImage(Bitmap bmp, int degree) {
        if (bmp == null) {
            return null;
        }
        if (degree == 0) {
            return bmp;
        }
        Matrix matrix = new Matrix();
        if (degree != 0) {
            matrix.postRotate((float) degree);
        }
        Bitmap rotated = null;
        try {
            rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } catch (OutOfMemoryError err) {
            CamLog.m6e(CameraConstants.TAG, "error occurred rotating image because of OutOfMemory", err);
        }
        if (matrix != null) {
        }
        return rotated;
    }

    public void setDegree(int degree, boolean isAnim) {
        if (this.mOkButton != null && this.mCancelButton != null && this.mGuideTextAreaTop != null && this.mGuideTextAreaBottom != null) {
            this.mOkButton.setDegree(degree, isAnim);
            this.mCancelButton.setDegree(degree, isAnim);
            this.mGuideTextAreaTop.rotateLayout(degree);
            this.mGuideTextAreaBottom.rotateLayout(degree);
            setTextViewParamForBottomTextArea(this.mTextGuideBottom);
        }
    }

    public void onPauseAfter() {
        releaseMediaPlayer();
        if (this.mFirstTextureView != null) {
            this.mFirstTextureView.setSurfaceTextureListener(null);
            this.mFirstTextureWrap.removeView(this.mFirstTextureView);
            this.mFirstTextureView = null;
        }
        if (this.mSecondTextureView != null) {
            this.mSecondTextureView.setSurfaceTextureListener(null);
            this.mSecondTextureWrap.removeView(this.mSecondTextureView);
            this.mSecondTextureView = null;
        }
    }

    public void onResumeAfter() {
        this.mFirstTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.texture_view_component_top);
        this.mSecondTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.texture_view_component_bottom);
        if (this.mCurrentContentType == 2) {
            this.mFirstTextureWrap.addView(this.mFirstTextureView);
            initTextureView(this.mInfo.cameraId[0], this.mFirstTextureView, this.mFirstTextureWrap, 0);
            this.mFirstTextureView.setVisibility(0);
            setFirstTextureViewListener();
        } else if (this.mCurrentContentType == 1) {
            this.mSecondTextureWrap.addView(this.mSecondTextureView);
            initTextureView(this.mInfo.cameraId[1], this.mSecondTextureView, this.mSecondTextureWrap, 1);
            this.mSecondTextureView.setVisibility(0);
            setSecondTextureViewListener();
        } else if (this.mCurrentContentType == 3) {
            this.mFirstTextureWrap.addView(this.mFirstTextureView);
            this.mSecondTextureWrap.addView(this.mSecondTextureView);
            initTextureView(this.mInfo.cameraId[0], this.mFirstTextureView, this.mFirstTextureWrap, 0);
            initTextureView(this.mInfo.cameraId[1], this.mSecondTextureView, this.mSecondTextureWrap, 1);
            this.mFirstTextureView.setVisibility(0);
            this.mSecondTextureView.setVisibility(0);
            setFirstTextureViewListener();
            setSecondTextureViewListener();
        } else if (this.mCurrentContentType == 0) {
        }
        showGuideText(this.mCurrentContentType);
    }

    protected void resizeViewSize() {
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        sSCREEN_HEIGHT = lcdSize[1] * 2;
        sHALF_SIZE = lcdSize[1];
        setRelativeLayoutParam(this.mFirstTextureWrap);
        setRelativeLayoutParam(this.mSecondTextureWrap);
        setRelativeLayoutParam(this.mPostViewDummy);
        setRelativeLayoutParam(this.mGuideTextAreaTop);
        setRelativeLayoutParam(this.mGuideTextAreaBottom);
        setImageViewParam(this.mFirstImageViewBg);
        setImageViewParam(this.mSecondImageViewBg);
        setImageViewParam(this.mFirstImageView);
        setImageViewParam(this.mSecondImageView);
        setImageViewParam(this.mFirstVideoSplash);
        setImageViewParam(this.mSecondVideoSplash);
        setButtonLayoutHeight();
    }

    protected void setRelativeLayoutParam(RelativeLayout rl) {
        LayoutParams param = (LayoutParams) rl.getLayoutParams();
        param.width = sHALF_SIZE;
        param.height = sHALF_SIZE;
        rl.setLayoutParams(param);
    }

    protected void setRelativeLayoutParamForBottomTextArea(RelativeLayout rl) {
        LayoutParams param = (LayoutParams) rl.getLayoutParams();
        param.width = sHALF_SIZE;
        param.height = (int) (((float) sHALF_SIZE) * 0.82f);
        param.topMargin = sHALF_SIZE;
        rl.setLayoutParams(param);
    }

    protected void setImageViewParam(ImageView iv) {
        LayoutParams param = (LayoutParams) iv.getLayoutParams();
        param.width = sHALF_SIZE;
        param.height = sHALF_SIZE;
        iv.setLayoutParams(param);
    }

    protected void setTextViewParamForBottomTextArea(TextView tv) {
        LayoutParams param = (LayoutParams) tv.getLayoutParams();
        int degree = this.mGet.getOrientationDegree();
        switch (degree) {
            case 0:
            case 180:
                param.removeRule(12);
                param.removeRule(10);
                param.addRule(10);
                if (degree != 0) {
                    param.topMargin = 500;
                    break;
                } else {
                    param.topMargin = 96;
                    break;
                }
            case 90:
            case 270:
                param.removeRule(12);
                param.removeRule(10);
                param.addRule(12);
                param.bottomMargin = BOTTOM_GUIDE_TEXT_BOTTOM_MARGIN_LAND;
                break;
        }
        tv.setLayoutParams(param);
    }

    public boolean isPostViewButtonVisibility() {
        if (this.mPostViewButtonLayout == null || this.mPostViewButtonLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public int getPostviewType() {
        return this.mCurrentContentType;
    }

    public void setSplicePostViewListener(SplicePostViewListener listener) {
        this.mListener = listener;
    }

    public void setPostviewType(int type) {
        this.mCurrentContentType = type;
    }

    protected void setButtonListener() {
        super.setButtonListener();
        this.mFirstVideoSplash.setOnClickListener(new C13081());
        this.mSecondVideoSplash.setOnClickListener(new C13092());
        String msg = this.mGet.getActivity().getString(C0088R.string.sp_image_SHORT);
        this.mFirstImageView.setContentDescription(msg);
        this.mSecondImageView.setContentDescription(msg);
        msg = this.mGet.getActivity().getString(C0088R.string.sp_video_SHORT);
        this.mFirstVideoSplash.setContentDescription(msg);
        this.mSecondVideoSplash.setContentDescription(msg);
    }

    protected void setButtonLayoutHeight() {
        LayoutParams param = (LayoutParams) this.mPostViewButtonLayout.getLayoutParams();
        param.height = (sSCREEN_HEIGHT * 111) / 1000;
        this.mPostViewButtonLayout.setLayoutParams(param);
    }
}
