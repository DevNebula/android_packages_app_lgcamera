package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.managers.ext.GridCameraPostviewManagerBase.GridContentsInfo;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;

public class GridCameraPostviewManagerExpand extends GridCameraPostviewManager {
    protected static final int FILM_VIDEO_CAM_0 = 10;
    protected static final int FILM_VIDEO_CAM_180 = 12;
    protected static final int FILM_VIDEO_CAM_270 = 13;
    protected static final int FILM_VIDEO_CAM_90 = 11;
    private AnimationListener mAnimListener = new C12272();

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerExpand$1 */
    class C12261 implements OnTouchListener {
        C12261() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            CamLog.m3d(CameraConstants.TAG, "postview touched");
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerExpand$2 */
    class C12272 implements AnimationListener {
        C12272() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            GridCameraPostviewManagerExpand.this.mOkButton.setEnabled(true);
            GridCameraPostviewManagerExpand.this.mCancelButton.setEnabled(true);
        }
    }

    public GridCameraPostviewManagerExpand(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        initLayout();
    }

    public void showPostviewCover(boolean isShow) {
        if (this.mPostviewCover != null) {
            this.mPostviewCover.setVisibility(isShow ? 0 : 8);
        }
    }

    public boolean getPostviewCoverVisibility() {
        if (this.mPostviewCover == null || this.mPostviewCover.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public void setQuickClipDrawerOpened(boolean isOpen) {
        this.mIsQuickDrawerOpened = isOpen;
    }

    public boolean getQuickClipDrawerOpend() {
        return this.mIsQuickDrawerOpened;
    }

    public void onDestroy() {
        super.onDestroy();
        CamLog.m3d(CameraConstants.TAG, "destroy postview mgr");
        removeViews();
    }

    public void showPostViewLayout(boolean show) {
        if (this.mGridPostviewLayout != null) {
            this.mGridPostviewLayout.setVisibility(show ? 0 : 8);
        }
    }

    public boolean getPostviewVisibility() {
        if (this.mGridPostviewLayout == null || this.mGridPostviewLayout.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    protected void resizeViewSize() {
        super.resizeViewSize();
        setButtonLayoutHeight();
    }

    protected void setButtonLayoutHeight() {
        LayoutParams param = (LayoutParams) this.mPostviewButtonLayout.getLayoutParams();
        param.height = (sSCREEN_HEIGHT * 111) / 1000;
        this.mPostviewButtonLayout.setLayoutParams(param);
    }

    public void setImageViewsTouchClickListener() {
        super.setImageViewsTouchClickListener();
        this.mPostviewCover.setOnTouchListener(new C12261());
    }

    public GridContentsInfo[] getGridContentsInfo() {
        return this.mContentsInfo;
    }

    public boolean isSavingProcess() {
        return this.mIsSavingProcess;
    }

    public int getFilmVideoLocalCamId(int degree) {
        switch (degree) {
            case 0:
                return 10;
            case 90:
                return 11;
            case 180:
                return 12;
            case 270:
                return 13;
            default:
                return 10;
        }
    }

    protected void setBitmapToView(ImageView iv, Bitmap bm, boolean isRearCam, int degree, boolean isImage, RotateTextView rtv) {
        iv.setVisibility(8);
        iv.setImageBitmap(bm);
        iv.setRotation(isImage ? convertDegree(isRearCam, degree) : convertDegreeForVideo(isRearCam, degree));
        AnimationUtil.startShowingAnimation(iv, true, 200, this.mAnimListener, false);
    }

    public void setImageBitmapToGrid(int index, Bitmap bm, boolean isRearCam, int degree, boolean isImage, String filePath, boolean isFilmVideo) {
        if (index <= 3) {
            this.mOkButton.setEnabled(false);
            this.mCancelButton.setEnabled(false);
            if (this.mListener != null && this.mListener.isRetakeMode() && this.mContentsInfo[index].contentsType == 2) {
                releaseMediaPlayer(index);
                removeTextureView(index);
            }
            this.mContentsInfo[index] = new GridContentsInfo(isImage ? 1 : 2, degree, isRearCam, null, filePath, isFilmVideo);
            switch (index) {
                case 0:
                    setBitmapToView(this.mFirstImageView, bm, isRearCam, degree, isImage, this.mFirstTextView);
                    setTalkbackText(index, isImage, this.mFirstImageView);
                    break;
                case 1:
                    setBitmapToView(this.mSecondImageView, bm, isRearCam, degree, isImage, this.mSecondTextView);
                    setTalkbackText(index, isImage, this.mSecondImageView);
                    break;
                case 2:
                    setBitmapToView(this.mThirdImageView, bm, isRearCam, degree, isImage, this.mThirdTextView);
                    setTalkbackText(index, isImage, this.mThirdImageView);
                    break;
                case 3:
                    setBitmapToView(this.mFourthImageView, bm, isRearCam, degree, isImage, this.mFourthTextView);
                    setTalkbackText(index, isImage, this.mFourthImageView);
                    break;
            }
            boolean reverse = (isImage || isRearCam) ? false : true;
            setReverseImageView(index, reverse);
            int captureIndex = 0;
            if (this.mListener != null) {
                if (this.mListener.isRetakeMode()) {
                    captureIndex = getCapturedCount();
                } else {
                    captureIndex = index + 1;
                }
            }
            setHighlightView(captureIndex, false);
        }
    }
}
