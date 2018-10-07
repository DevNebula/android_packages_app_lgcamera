package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.UserHandle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.ReverseRelativeLayout;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.components.TouchImageViewInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.SystemBarUtil;
import com.lge.camera.util.Utils;
import java.io.IOException;
import java.util.ArrayList;

public class SplicePostViewManagerBase implements TouchImageViewInterface {
    protected static final int AUDIO_VOL_SCALE = 1;
    protected static final int BITMAP_INDEX_BOTTOM = 1;
    protected static final int BITMAP_INDEX_TOP = 0;
    protected static final float CAMERA_ORIGINAL_SCALE_FACTOR = 1.0f;
    protected static final int CAM_INDEX_BOTTOM = 1;
    protected static final int CAM_INDEX_TOP = 0;
    protected static final int FILE_TYPE_FIRST_INDEX = 0;
    protected static final String FIRST_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesmultiview_0.mp4");
    protected static final float FRONT_NORMAL_CAMERA_X_SCALE_FACTOR = 1.36f;
    protected static final float FRONT_NORMAL_CAMERA_Y_SCALE_FACTOR = 1.79f;
    protected static final float FRONT_WIDE_CAMERA_SCALE_FACTOR = 1.35f;
    protected static final float LOCAL_ANGLE_0 = 0.0f;
    protected static final float LOCAL_ANGLE_180 = 180.0f;
    protected static final float LOCAL_ANGLE_270 = 270.0f;
    protected static final float LOCAL_ANGLE_90 = 90.0f;
    protected static final int LOCAL_DEGREE_0 = 0;
    protected static final int LOCAL_DEGREE_180 = 2;
    protected static final int LOCAL_DEGREE_270 = 1;
    protected static final int LOCAL_DEGREE_90 = 3;
    protected static final float REAR_NORMAL_CAMERA_X_SCALE_FACTOR = 1.65f;
    protected static final float REAR_NORMAL_CAMERA_Y_SCALE_FACTOR = 2.96f;
    protected static final float REAR_WIDE_CAMERA_SCALE_FACTOR = 1.7f;
    protected static final String SECOND_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesmultiview_1.mp4");
    protected static final int TEXTURE_VIEW_INDEX_BOTTOM = 1;
    protected static final int TEXTURE_VIEW_INDEX_TOP = 0;
    public static final int USER_ID = UserHandle.myUserId();
    protected static float sVIEW_CENTER_X = 720.0f;
    protected static float sVIEW_CENTER_Y = 720.0f;
    protected RotateImageButton mCancelButton = null;
    protected int mCurrentContentType = -1;
    protected TouchImageView mFirstImageView = null;
    protected ImageView mFirstImageViewBg = null;
    protected MediaPlayer mFirstPlayer = null;
    protected OnPreparedListener mFirstPreparedListener = new C13123();
    protected SurfaceTexture mFirstSurfaceTexture = null;
    protected TextureView mFirstTextureView = null;
    protected ReverseRelativeLayout mFirstTextureWrap = null;
    protected ImageView mFirstVideoSplash = null;
    protected ModuleInterface mGet;
    protected RotateLayout mGuideTextAreaBottom = null;
    protected RotateLayout mGuideTextAreaTop = null;
    protected ContentsInfo mInfo = null;
    protected boolean mIsInit = false;
    protected boolean mIsReverse = false;
    protected boolean mIsSaveClicked = false;
    protected SplicePostViewListener mListener = null;
    protected RotateImageButton mOkButton = null;
    protected RelativeLayout mPostViewButtonLayout = null;
    protected RelativeLayout mPostViewDummy = null;
    protected View mPostViewLayout = null;
    protected RelativeLayout mPostviewContentsLayout = null;
    protected RelativeLayout mReadyToSaveLayout = null;
    protected TouchImageView mSecondImageView = null;
    protected ImageView mSecondImageViewBg = null;
    protected MediaPlayer mSecondPlayer = null;
    protected OnPreparedListener mSecondPreparedListener = new C13134();
    protected SurfaceTexture mSecondSurfaceTexture = null;
    protected TextureView mSecondTextureView = null;
    protected ReverseRelativeLayout mSecondTextureWrap = null;
    protected ImageView mSecondVideoSplash = null;
    protected TextView mTextGuideBottom = null;
    protected OnTouchListener mTouchImgViewListener = new C13145();
    protected Bitmap[] mTransformedBitmap = null;

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$1 */
    class C13101 implements SurfaceTextureListener {
        C13101() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView Destroyed");
            if (SplicePostViewManagerBase.this.mFirstSurfaceTexture != null) {
                SplicePostViewManagerBase.this.mFirstSurfaceTexture.release();
                SplicePostViewManagerBase.this.mFirstSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView onSurfaceTextureAvailable");
            if (SplicePostViewManagerBase.this.mFirstPlayer == null || !SplicePostViewManagerBase.this.mFirstPlayer.isPlaying()) {
                SplicePostViewManagerBase.this.mFirstSurfaceTexture = surface;
                SplicePostViewManagerBase.this.playFirstVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$2 */
    class C13112 implements SurfaceTextureListener {
        C13112() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView Destroyed");
            if (SplicePostViewManagerBase.this.mSecondSurfaceTexture != null) {
                SplicePostViewManagerBase.this.mSecondSurfaceTexture.release();
                SplicePostViewManagerBase.this.mSecondSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView onSurfaceTextureAvailable");
            if (SplicePostViewManagerBase.this.mSecondPlayer == null || !SplicePostViewManagerBase.this.mSecondPlayer.isPlaying()) {
                SplicePostViewManagerBase.this.mSecondSurfaceTexture = surface;
                SplicePostViewManagerBase.this.playSecondVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$3 */
    class C13123 implements OnPreparedListener {
        C13123() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$4 */
    class C13134 implements OnPreparedListener {
        C13134() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$5 */
    class C13145 implements OnTouchListener {
        C13145() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            SplicePostViewManagerBase.this.hideGuideText();
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$6 */
    class C13156 implements OnClickListener {
        C13156() {
        }

        public void onClick(View arg0) {
            if (SplicePostViewManagerBase.this.mIsSaveClicked || SplicePostViewManagerBase.this.isSystemBarVisible()) {
                CamLog.m3d(CameraConstants.TAG, "mIsSaveClicked : " + SplicePostViewManagerBase.this.mIsSaveClicked);
                return;
            }
            SplicePostViewManagerBase.this.mIsSaveClicked = true;
            SplicePostViewManagerBase.this.hideGuideText();
            SplicePostViewManagerBase.this.saveContents(true);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SplicePostViewManagerBase$7 */
    class C13167 implements OnClickListener {
        C13167() {
        }

        public void onClick(View arg0) {
            if (SplicePostViewManagerBase.this.mIsSaveClicked || SplicePostViewManagerBase.this.isSystemBarVisible()) {
                CamLog.m3d(CameraConstants.TAG, "mIsSaveClicked : " + SplicePostViewManagerBase.this.mIsSaveClicked);
                return;
            }
            if (SplicePostViewManagerBase.this.mListener != null) {
                SplicePostViewManagerBase.this.mListener.onCancel();
                SplicePostViewManagerBase.this.mListener.removePostView();
            }
            SplicePostViewManagerBase.this.releaseMediaPlayer();
        }
    }

    protected class ContentsInfo {
        int[] cameraId;
        int degree;
        int[] fileType;
        boolean isReverse;
        ArrayList<Integer> vidDegree;

        ContentsInfo(boolean isReverse, int[] cameraId, int[] fileType, int degree, ArrayList<Integer> vidDegree) {
            this.isReverse = isReverse;
            this.cameraId = cameraId;
            this.fileType = fileType;
            this.degree = degree;
            this.vidDegree = vidDegree;
        }
    }

    public SplicePostViewManagerBase(ModuleInterface moduleInterface) {
        this.mGet = moduleInterface;
    }

    public void removeViews() {
        if (this.mFirstImageView != null) {
            this.mFirstImageView.setImageBitmap(null);
            this.mFirstImageView.setVisibility(8);
        }
        if (this.mSecondImageView != null) {
            this.mSecondImageView.setImageBitmap(null);
            this.mSecondImageView.setVisibility(8);
        }
        if (this.mFirstVideoSplash != null) {
            this.mFirstVideoSplash.setVisibility(8);
        }
        if (this.mSecondVideoSplash != null) {
            this.mSecondVideoSplash.setVisibility(8);
        }
        if (this.mPostviewContentsLayout != null) {
            this.mPostviewContentsLayout.setVisibility(8);
        }
        if (this.mPostViewLayout != null) {
            this.mPostViewLayout.setVisibility(8);
        }
        if (this.mReadyToSaveLayout != null) {
            this.mReadyToSaveLayout.setVisibility(8);
        }
        releaseBitmap();
        releaseListener();
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mPostViewLayout == null || this.mReadyToSaveLayout == null)) {
            MemoryUtils.releaseViews(this.mReadyToSaveLayout);
            vg.removeView(this.mPostViewLayout);
        }
        this.mCurrentContentType = -1;
        this.mIsReverse = false;
        this.mIsInit = false;
    }

    protected void releaseListener() {
        if (this.mFirstImageView != null && this.mSecondImageView != null && this.mFirstVideoSplash != null && this.mSecondVideoSplash != null && this.mOkButton != null && this.mCancelButton != null) {
            this.mFirstImageView.setOnTouchListener(null);
            this.mSecondImageView.setOnTouchListener(null);
            this.mFirstVideoSplash.setOnClickListener(null);
            this.mSecondVideoSplash.setOnClickListener(null);
            this.mOkButton.setOnClickListener(null);
            this.mCancelButton.setOnClickListener(null);
        }
    }

    protected void initTextureView(int cameraId, TextureView textureView, ReverseRelativeLayout layout, int viewIndex) {
        int screenWidth = Utils.getLCDsize(this.mGet.getAppContext(), true)[1];
        sVIEW_CENTER_X = (float) (screenWidth / 2);
        sVIEW_CENTER_Y = (float) (screenWidth / 2);
        int currentVideoDegree = 0;
        try {
            currentVideoDegree = ((Integer) this.mInfo.vidDegree.get(viewIndex)).intValue();
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, e.toString());
        }
        if (cameraId == 3 || cameraId == 1) {
            if (currentVideoDegree == 90) {
                textureView.setRotation(getDegree(3));
            } else if (currentVideoDegree == 270) {
                textureView.setRotation(getDegree(1));
            } else if (currentVideoDegree == 0) {
                textureView.setRotation(getDegree(2));
            }
            textureView.setTransform(getScaledMatrix(cameraId, currentVideoDegree));
            layout.setReverse(true);
            return;
        }
        if (currentVideoDegree == 90) {
            textureView.setRotation(getDegree(1));
        } else if (currentVideoDegree == 270) {
            textureView.setRotation(getDegree(3));
        } else if (currentVideoDegree == 180) {
            textureView.setRotation(getDegree(2));
        }
        textureView.setTransform(getScaledMatrix(cameraId, currentVideoDegree));
    }

    protected Matrix getScaledMatrix(int cameraId, int degree) {
        Matrix matrix = new Matrix();
        if (cameraId == 3 || cameraId == 2) {
            if (degree == 90 || degree == 270) {
                matrix.setScale(getVideoScaleFactor(cameraId), 1.0f, sVIEW_CENTER_X, sVIEW_CENTER_Y);
            } else {
                matrix.setScale(1.0f, getVideoScaleFactor(cameraId), sVIEW_CENTER_X, sVIEW_CENTER_Y);
            }
        } else if (degree == 90 || degree == 270) {
            matrix.setScale(getVideoYScaleFactor(cameraId), getVideoXScaleFactor(cameraId), sVIEW_CENTER_X, sVIEW_CENTER_Y);
        } else {
            matrix.setScale(getVideoXScaleFactor(cameraId), getVideoYScaleFactor(cameraId), sVIEW_CENTER_X, sVIEW_CENTER_Y);
        }
        return matrix;
    }

    protected void setFirstTextureViewListener() {
        this.mFirstTextureView.setSurfaceTextureListener(new C13101());
    }

    protected void setSecondTextureViewListener() {
        this.mSecondTextureView.setSurfaceTextureListener(new C13112());
    }

    protected void playFirstVideo() {
        try {
            if (this.mFirstSurfaceTexture == null) {
                if (this.mFirstTextureView != null) {
                    this.mFirstSurfaceTexture = this.mFirstTextureView.getSurfaceTexture();
                } else {
                    return;
                }
            }
            Surface surface = new Surface(this.mFirstSurfaceTexture);
            if (this.mFirstPlayer == null) {
                this.mFirstPlayer = new MediaPlayer();
            }
            this.mFirstPlayer.setDataSource(this.mIsReverse ? SECOND_VIDEO_LOCATION : FIRST_VIDEO_LOCATION);
            this.mFirstPlayer.setSurface(surface);
            this.mFirstPlayer.setLooping(true);
            this.mFirstPlayer.prepareAsync();
            this.mFirstPlayer.setOnPreparedListener(this.mFirstPreparedListener);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
    }

    protected void playSecondVideo() {
        try {
            if (this.mSecondSurfaceTexture == null) {
                if (this.mSecondTextureView != null) {
                    this.mSecondSurfaceTexture = this.mSecondTextureView.getSurfaceTexture();
                } else {
                    return;
                }
            }
            Surface surface = new Surface(this.mSecondSurfaceTexture);
            if (this.mSecondPlayer == null) {
                this.mSecondPlayer = new MediaPlayer();
            }
            this.mSecondPlayer.setDataSource(SECOND_VIDEO_LOCATION);
            this.mSecondPlayer.setSurface(surface);
            this.mSecondPlayer.setLooping(true);
            this.mSecondPlayer.prepareAsync();
            this.mSecondPlayer.setOnPreparedListener(this.mSecondPreparedListener);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        }
    }

    protected void setButtonListener() {
        this.mOkButton.setOnClickListener(new C13156());
        this.mCancelButton.setOnClickListener(new C13167());
        this.mFirstImageView.setOnTouchListener(this.mTouchImgViewListener);
        this.mSecondImageView.setOnTouchListener(this.mTouchImgViewListener);
    }

    public void saveContents(boolean isButtonClicked) {
        CamLog.m3d(CameraConstants.TAG, "in");
        if (!isSystemBarVisible()) {
            if (this.mListener != null) {
                if (this.mCurrentContentType == 0) {
                    this.mListener.onSaveContents(getImageInView(this.mPostviewContentsLayout), isButtonClicked);
                } else {
                    setPostViewBitmapToGLView(this.mCurrentContentType);
                    this.mListener.onSaveContents(isButtonClicked);
                }
            }
            if (1 == this.mCurrentContentType) {
                this.mFirstVideoSplash.setImageBitmap(this.mTransformedBitmap[0]);
                this.mFirstVideoSplash.setVisibility(0);
            } else if (2 == this.mCurrentContentType) {
                this.mSecondVideoSplash.setImageBitmap(this.mTransformedBitmap[0]);
                this.mSecondVideoSplash.setVisibility(0);
            }
            releaseMediaPlayer();
            CamLog.m3d(CameraConstants.TAG, "out");
        }
    }

    protected float getDegree(int degree) {
        switch (degree) {
            case 1:
                return 270.0f;
            case 2:
                return LOCAL_ANGLE_180;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    public int getIntDegree(int degree) {
        switch (degree) {
            case 1:
                return 270;
            case 2:
                return 180;
            case 3:
                return 90;
            default:
                return 0;
        }
    }

    protected float getVideoScaleFactor(int cameraId) {
        if (cameraId == 3) {
            return FRONT_WIDE_CAMERA_SCALE_FACTOR;
        }
        return REAR_WIDE_CAMERA_SCALE_FACTOR;
    }

    protected float getVideoXScaleFactor(int cameraId) {
        if (cameraId == 1) {
            return FRONT_NORMAL_CAMERA_X_SCALE_FACTOR;
        }
        return REAR_NORMAL_CAMERA_X_SCALE_FACTOR;
    }

    protected float getVideoYScaleFactor(int cameraId) {
        if (cameraId == 1) {
            return FRONT_NORMAL_CAMERA_Y_SCALE_FACTOR;
        }
        return REAR_NORMAL_CAMERA_Y_SCALE_FACTOR;
    }

    protected void setPostViewBitmapToGLView(int type) {
        switch (type) {
            case 0:
                this.mTransformedBitmap = new Bitmap[2];
                this.mTransformedBitmap[0] = getImageInView(this.mFirstImageView);
                this.mTransformedBitmap[1] = getImageInView(this.mSecondImageView);
                break;
            case 1:
                this.mTransformedBitmap = new Bitmap[1];
                this.mTransformedBitmap[0] = getImageInView(this.mFirstImageView);
                break;
            case 2:
                this.mTransformedBitmap = new Bitmap[1];
                this.mTransformedBitmap[0] = getImageInView(this.mSecondImageView);
                break;
        }
        if (this.mListener != null) {
            this.mListener.onImageTransformed(type);
        }
    }

    protected Bitmap getImageInView(View view) {
        if (view == null) {
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Config.ARGB_8888);
        view.draw(new Canvas(bm));
        return bm;
    }

    public Bitmap[] getTransformedImage() {
        return this.mTransformedBitmap;
    }

    protected void releaseBitmap() {
        if (this.mTransformedBitmap != null) {
            int i = 0;
            while (i < this.mTransformedBitmap.length) {
                if (!(this.mTransformedBitmap[i] == null || this.mTransformedBitmap[i].isRecycled())) {
                    this.mTransformedBitmap[i].recycle();
                    this.mTransformedBitmap[i] = null;
                }
                i++;
            }
        }
        this.mTransformedBitmap = null;
    }

    public void releaseMediaPlayer() {
        if (this.mFirstPlayer != null) {
            this.mFirstPlayer.stop();
            this.mFirstPlayer.release();
            this.mFirstPlayer = null;
        }
        if (this.mSecondPlayer != null) {
            this.mSecondPlayer.stop();
            this.mSecondPlayer.release();
            this.mSecondPlayer = null;
        }
    }

    protected boolean isSystemBarVisible() {
        if (this.mGet != null) {
            return SystemBarUtil.isSystemUIVisible(this.mGet.getActivity());
        }
        return false;
    }

    public boolean isSystemUIVisible() {
        return isSystemBarVisible() || (isPostviewVisible() && this.mListener != null && this.mListener.getFrameshotCountForPostView() < 2);
    }

    public void onClicked() {
    }

    public boolean isPostviewVisible() {
        if (this.mReadyToSaveLayout != null) {
            return this.mReadyToSaveLayout.getVisibility() == 0;
        } else {
            return false;
        }
    }

    protected void showGuideText(int contentType) {
        if (contentType != 3) {
            if (contentType == 2) {
                this.mGuideTextAreaTop.setVisibility(8);
                this.mGuideTextAreaBottom.setVisibility(0);
                return;
            }
            this.mGuideTextAreaTop.setVisibility(0);
            this.mGuideTextAreaBottom.setVisibility(8);
        }
    }

    protected void hideGuideText() {
        if (this.mGuideTextAreaTop != null && this.mGuideTextAreaBottom != null) {
            this.mGuideTextAreaTop.setVisibility(8);
            this.mGuideTextAreaBottom.setVisibility(8);
        }
    }

    protected void resizeViewSize() {
    }

    public void setSaveButtonClickedFlag(boolean setValue) {
        CamLog.m3d(CameraConstants.TAG, "setValue : " + setValue);
        this.mIsSaveClicked = setValue;
    }

    public void resetSaveButtonClickedFlag() {
        this.mIsSaveClicked = false;
    }

    public void onTouchStateChanged(boolean isTouchDown) {
    }

    public void onZoomScaleStart() {
    }

    public void setQuickClipDrawerOpend(boolean isOpen) {
        if (isOpen) {
            if (this.mCancelButton != null) {
                this.mCancelButton.setVisibility(4);
            }
            if (this.mOkButton != null) {
                this.mOkButton.setVisibility(4);
                return;
            }
            return;
        }
        if (this.mCancelButton != null) {
            this.mCancelButton.setVisibility(0);
        }
        if (this.mOkButton != null) {
            this.mOkButton.setVisibility(0);
        }
    }
}
