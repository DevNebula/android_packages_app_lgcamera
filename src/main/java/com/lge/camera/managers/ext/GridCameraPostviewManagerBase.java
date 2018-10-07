package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.UserHandle;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.ReverseRelativeLayout;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SystemBarUtil;
import java.io.IOException;

public class GridCameraPostviewManagerBase extends ManagerInterfaceImpl {
    protected static final int AUDIO_VOL_SCALE = 1;
    protected static final int FIRST_IMG_VIEW = 0;
    public static final int FIRST_INDEX = 0;
    public static final String FIRST_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesgridview_0.mp4");
    protected static final int FOURTH_IMG_VIEW = 3;
    public static final int FOURTH_INDEX = 3;
    public static final String FOURTH_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesgridview_3.mp4");
    protected static final String GRID_BG_OPACITY_70 = "#bf000000";
    protected static final String GRID_BG_OPACITY_TRANSPARENT = "#00000000";
    public static final int IMAGE = 1;
    protected static final float LOCAL_ANGLE_0 = 0.0f;
    protected static final float LOCAL_ANGLE_180 = 180.0f;
    protected static final float LOCAL_ANGLE_270 = 270.0f;
    protected static final float LOCAL_ANGLE_90 = 90.0f;
    protected static final int MAX_CAPTURED_COUNT = 4;
    protected static final int OUT_OF_SCREEN_LOCATION = 3000;
    protected static final int POSTVIEW_BUTTON_DIVIDER = 1000;
    protected static final int POSTVIEW_BUTTON_RATIO = 111;
    protected static final int SECOND_IMG_VIEW = 1;
    public static final int SECOND_INDEX = 1;
    public static final String SECOND_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesgridview_1.mp4");
    protected static final int THIRD_IMG_VIEW = 2;
    public static final int THIRD_INDEX = 2;
    public static final String THIRD_VIDEO_LOCATION = ("/data/user/" + USER_ID + "/com.lge.camera/filesgridview_2.mp4");
    public static final int USER_ID = UserHandle.myUserId();
    public static final int VIDEO = 2;
    protected static int sSCREEN_HEIGHT = 2880;
    protected RotateImageButton mCancelButton = null;
    protected TextureView mCollageTextureView;
    protected GridContentsInfo[] mContentsInfo = new GridContentsInfo[4];
    protected ImageView mFirstImageHighrightView = null;
    protected ImageView mFirstImageView = null;
    protected ReverseRelativeLayout mFirstImageViewWrap = null;
    protected MediaPlayer mFirstPlayer = null;
    protected OnPreparedListener mFirstPreparedListener = new C12226();
    protected SurfaceTexture mFirstSurfaceTexture = null;
    protected RotateTextView mFirstTextView = null;
    protected TextureView mFirstTextureView = null;
    protected ReverseRelativeLayout mFirstTextureWrap = null;
    protected ImageView mFourthImageHighrightView = null;
    protected ImageView mFourthImageView = null;
    protected ReverseRelativeLayout mFourthImageViewWrap = null;
    protected MediaPlayer mFourthPlayer = null;
    protected OnPreparedListener mFourthPreparedListener = new C12259();
    protected SurfaceTexture mFourthSurfaceTexture = null;
    protected RotateTextView mFourthTextView = null;
    protected TextureView mFourthTextureView = null;
    protected ReverseRelativeLayout mFourthTextureWrap = null;
    protected TextView mGridGuideText = null;
    protected RelativeLayout mGridPostviewLayout = null;
    protected GridPostViewListener mListener = null;
    protected RotateImageButton mOkButton = null;
    protected View mPostViewLayout = null;
    protected RelativeLayout mPostviewButtonLayout = null;
    protected RelativeLayout mPostviewContentsLayout = null;
    protected RelativeLayout mPostviewCover = null;
    protected RelativeLayout mPostviewGridContentsFirst = null;
    protected RelativeLayout mPostviewGridContentsFourth = null;
    protected RelativeLayout mPostviewGridContentsSecond = null;
    protected RelativeLayout mPostviewGridContentsThird = null;
    protected RotateLayout mPreviewDummyLayout = null;
    protected ImageView mSecondImageHighrightView = null;
    protected ImageView mSecondImageView = null;
    protected ReverseRelativeLayout mSecondImageViewWrap = null;
    protected MediaPlayer mSecondPlayer = null;
    protected OnPreparedListener mSecondPreparedListener = new C12237();
    protected SurfaceTexture mSecondSurfaceTexture = null;
    protected RotateTextView mSecondTextView = null;
    protected TextureView mSecondTextureView = null;
    protected ReverseRelativeLayout mSecondTextureWrap = null;
    protected SurfaceTexture mSurfaceTextureCollage = null;
    protected SurfaceTextureListener mSurfaceTextureListenerMV;
    protected ImageView mThirdImageHighrightView = null;
    protected ImageView mThirdImageView = null;
    protected ReverseRelativeLayout mThirdImageViewWrap = null;
    protected MediaPlayer mThirdPlayer = null;
    protected OnPreparedListener mThirdPreparedListener = new C12248();
    protected SurfaceTexture mThirdSurfaceTexture = null;
    protected RotateTextView mThirdTextView = null;
    protected TextureView mThirdTextureView = null;
    protected ReverseRelativeLayout mThirdTextureWrap = null;

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$1 */
    class C12171 implements SurfaceTextureListener {
        C12171() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "collage onSurfaceTextureDestroyed");
            if (GridCameraPostviewManagerBase.this.mSurfaceTextureCollage != null) {
                GridCameraPostviewManagerBase.this.mSurfaceTextureCollage.release();
                GridCameraPostviewManagerBase.this.mSurfaceTextureCollage = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "collage onSurfaceTextureAvailable");
            GridCameraPostviewManagerBase.this.mSurfaceTextureCollage = surface;
            if (GridCameraPostviewManagerBase.this.mListener != null) {
                GridCameraPostviewManagerBase.this.mListener.startRenderer(GridCameraPostviewManagerBase.this.mCollageTextureView, GridCameraPostviewManagerBase.this.mSurfaceTextureCollage);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$2 */
    class C12182 implements SurfaceTextureListener {
        C12182() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView Destroyed");
            if (GridCameraPostviewManagerBase.this.mFirstSurfaceTexture != null) {
                GridCameraPostviewManagerBase.this.mFirstSurfaceTexture.release();
                GridCameraPostviewManagerBase.this.mFirstSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "1st textureView onSurfaceTextureAvailable");
            if (GridCameraPostviewManagerBase.this.mFirstPlayer == null || !GridCameraPostviewManagerBase.this.mFirstPlayer.isPlaying()) {
                GridCameraPostviewManagerBase.this.mFirstSurfaceTexture = surface;
                GridCameraPostviewManagerBase.this.playFirstVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$3 */
    class C12193 implements SurfaceTextureListener {
        C12193() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView Destroyed");
            if (GridCameraPostviewManagerBase.this.mSecondSurfaceTexture != null) {
                GridCameraPostviewManagerBase.this.mSecondSurfaceTexture.release();
                GridCameraPostviewManagerBase.this.mSecondSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "2nd textureView onSurfaceTextureAvailable");
            if (GridCameraPostviewManagerBase.this.mSecondPlayer == null || !GridCameraPostviewManagerBase.this.mSecondPlayer.isPlaying()) {
                GridCameraPostviewManagerBase.this.mSecondSurfaceTexture = surface;
                GridCameraPostviewManagerBase.this.playSecondVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$4 */
    class C12204 implements SurfaceTextureListener {
        C12204() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "3nd textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "3nd textureView Destroyed");
            if (GridCameraPostviewManagerBase.this.mThirdSurfaceTexture != null) {
                GridCameraPostviewManagerBase.this.mThirdSurfaceTexture.release();
                GridCameraPostviewManagerBase.this.mThirdSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "3nd textureView onSurfaceTextureAvailable");
            if (GridCameraPostviewManagerBase.this.mThirdPlayer == null || !GridCameraPostviewManagerBase.this.mThirdPlayer.isPlaying()) {
                GridCameraPostviewManagerBase.this.mThirdSurfaceTexture = surface;
                GridCameraPostviewManagerBase.this.playThirdVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$5 */
    class C12215 implements SurfaceTextureListener {
        C12215() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "4nd textureView SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "4nd textureView Destroyed");
            if (GridCameraPostviewManagerBase.this.mFourthSurfaceTexture != null) {
                GridCameraPostviewManagerBase.this.mFourthSurfaceTexture.release();
                GridCameraPostviewManagerBase.this.mFourthSurfaceTexture = null;
            }
            return true;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "nd textureView onSurfaceTextureAvailable");
            if (GridCameraPostviewManagerBase.this.mFourthPlayer == null || !GridCameraPostviewManagerBase.this.mFourthPlayer.isPlaying()) {
                GridCameraPostviewManagerBase.this.mFourthSurfaceTexture = surface;
                GridCameraPostviewManagerBase.this.playFourthVideo();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$6 */
    class C12226 implements OnPreparedListener {
        C12226() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$7 */
    class C12237 implements OnPreparedListener {
        C12237() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$8 */
    class C12248 implements OnPreparedListener {
        C12248() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.GridCameraPostviewManagerBase$9 */
    class C12259 implements OnPreparedListener {
        C12259() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(1.0f);
            }
        }
    }

    public class GridContentsInfo {
        /* renamed from: bm */
        public Bitmap f34bm;
        public int contentsType;
        public int degree;
        public String filePath;
        public boolean isFilmVideo;
        public boolean isRearCam;

        GridContentsInfo(int contentsType, int degree, boolean isRearCam, Bitmap bm, String filePath, boolean isFilmVideo) {
            this.contentsType = contentsType;
            this.degree = degree;
            this.isRearCam = isRearCam;
            this.f34bm = bm;
            this.filePath = filePath;
            this.isFilmVideo = isFilmVideo;
        }
    }

    public GridCameraPostviewManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setPostviewListener(GridPostViewListener listener) {
        this.mListener = listener;
    }

    protected void setBitmapForCollage() {
        for (int i = 0; i < this.mContentsInfo.length; i++) {
            if (this.mContentsInfo[i].contentsType == 1) {
                this.mContentsInfo[i].f34bm = getImageInView(getViewFromIndex(i));
            }
        }
    }

    protected ReverseRelativeLayout getViewFromIndex(int index) {
        switch (index) {
            case 0:
                return this.mFirstImageViewWrap;
            case 1:
                return this.mSecondImageViewWrap;
            case 2:
                return this.mThirdImageViewWrap;
            case 3:
                return this.mFourthImageViewWrap;
            default:
                return null;
        }
    }

    protected float convertDegree(boolean isRearCam, int degree) {
        if (isRearCam) {
            degree = 90;
        } else if (getOrientationDegree() == 270 || getOrientationDegree() == 90) {
            degree = 90;
        } else {
            degree = 270;
        }
        switch (degree) {
            case 90:
                return 90.0f;
            case 270:
                return 270.0f;
            default:
                return 0.0f;
        }
    }

    protected float convertDegreeForVideo(boolean isRearCam, int degree) {
        if (!isRearCam) {
            switch (degree) {
                case 90:
                    degree = 90;
                    break;
                case 270:
                    degree = 270;
                    break;
            }
        } else if (degree == 270) {
            degree = 90;
        } else if (degree == 90) {
            degree = 270;
        }
        switch (degree) {
            case 0:
                return 0.0f;
            case 90:
                return 90.0f;
            case 180:
                return LOCAL_ANGLE_180;
            case 270:
                return 270.0f;
            default:
                return 0.0f;
        }
    }

    public boolean isImageOnly() {
        for (GridContentsInfo gridContentsInfo : this.mContentsInfo) {
            if (gridContentsInfo.contentsType == 2) {
                return false;
            }
        }
        return true;
    }

    protected void addBitmapForCollage() {
        for (int i = 0; i < this.mContentsInfo.length; i++) {
            if (this.mContentsInfo[i].contentsType == 1) {
                if (i == 0) {
                    this.mContentsInfo[i].f34bm = getImageInView(this.mFirstImageView);
                } else if (i == 1) {
                    this.mContentsInfo[i].f34bm = getImageInView(this.mSecondImageView);
                } else if (i == 2) {
                    this.mContentsInfo[i].f34bm = getImageInView(this.mThirdImageView);
                } else if (i == 3) {
                    this.mContentsInfo[i].f34bm = getImageInView(this.mFourthImageView);
                }
            }
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

    public void setHighlightView(int index, boolean isRetakeMode) {
        hideAllHighrightViews();
        switch (index) {
            case 0:
                this.mFirstImageHighrightView.setVisibility(0);
                setHighrightViewBackGroundColor(this.mFirstImageHighrightView, isRetakeMode);
                return;
            case 1:
                this.mSecondImageHighrightView.setVisibility(0);
                setHighrightViewBackGroundColor(this.mSecondImageHighrightView, isRetakeMode);
                return;
            case 2:
                this.mThirdImageHighrightView.setVisibility(0);
                setHighrightViewBackGroundColor(this.mThirdImageHighrightView, isRetakeMode);
                return;
            case 3:
                this.mFourthImageHighrightView.setVisibility(0);
                setHighrightViewBackGroundColor(this.mFourthImageHighrightView, isRetakeMode);
                return;
            default:
                hideAllHighrightViews();
                return;
        }
    }

    private void setHighrightViewBackGroundColor(ImageView iv, boolean isRetakeMode) {
        String color = isRetakeMode ? GRID_BG_OPACITY_70 : GRID_BG_OPACITY_TRANSPARENT;
        if (iv != null) {
            iv.setBackgroundColor(Color.parseColor(color));
        }
    }

    public void hideAllHighrightViews() {
        this.mFirstImageHighrightView.setVisibility(8);
        this.mSecondImageHighrightView.setVisibility(8);
        this.mThirdImageHighrightView.setVisibility(8);
        this.mFourthImageHighrightView.setVisibility(8);
    }

    public void setupCollageTextureView() {
        if (this.mCollageTextureView == null) {
            this.mCollageTextureView = (TextureView) this.mGet.inflateView(C0088R.layout.grid_collage_textureview);
            this.mGridPostviewLayout.addView(this.mCollageTextureView);
            this.mCollageTextureView.setSurfaceTextureListener(getCollageTexture());
        }
        this.mCollageTextureView.setTranslationY(3000.0f);
        this.mCollageTextureView.setVisibility(0);
    }

    private SurfaceTextureListener getCollageTexture() {
        this.mSurfaceTextureListenerMV = new C12171();
        return this.mSurfaceTextureListenerMV;
    }

    protected void initTextureView(boolean isRearCam, TextureView textureView, ReverseRelativeLayout layout, int vidDegree) {
        textureView.setRotation(convertDegreeForVideo(isRearCam, vidDegree));
        if (isRearCam) {
            layout.setReverse(false);
        } else {
            layout.setReverse(true);
        }
    }

    protected void setFirstTextureViewListener() {
        this.mFirstTextureView.setSurfaceTextureListener(new C12182());
    }

    protected void setSecondTextureViewListener() {
        this.mSecondTextureView.setSurfaceTextureListener(new C12193());
    }

    protected void setThirdTextureViewListener() {
        this.mThirdTextureView.setSurfaceTextureListener(new C12204());
    }

    protected void setFourthTextureViewListener() {
        this.mFourthTextureView.setSurfaceTextureListener(new C12215());
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
            this.mFirstPlayer.setDataSource(FIRST_VIDEO_LOCATION);
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

    protected void playThirdVideo() {
        try {
            if (this.mThirdSurfaceTexture == null) {
                if (this.mThirdTextureView != null) {
                    this.mThirdSurfaceTexture = this.mThirdTextureView.getSurfaceTexture();
                } else {
                    return;
                }
            }
            Surface surface = new Surface(this.mThirdSurfaceTexture);
            if (this.mThirdPlayer == null) {
                this.mThirdPlayer = new MediaPlayer();
            }
            this.mThirdPlayer.setDataSource(THIRD_VIDEO_LOCATION);
            this.mThirdPlayer.setSurface(surface);
            this.mThirdPlayer.setLooping(true);
            this.mThirdPlayer.prepareAsync();
            this.mThirdPlayer.setOnPreparedListener(this.mThirdPreparedListener);
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

    protected void playFourthVideo() {
        try {
            if (this.mFourthSurfaceTexture == null) {
                if (this.mFourthTextureView != null) {
                    this.mFourthSurfaceTexture = this.mFourthTextureView.getSurfaceTexture();
                } else {
                    return;
                }
            }
            Surface surface = new Surface(this.mFourthSurfaceTexture);
            if (this.mFourthPlayer == null) {
                this.mFourthPlayer = new MediaPlayer();
            }
            this.mFourthPlayer.setDataSource(FOURTH_VIDEO_LOCATION);
            this.mFourthPlayer.setSurface(surface);
            this.mFourthPlayer.setLooping(true);
            this.mFourthPlayer.prepareAsync();
            this.mFourthPlayer.setOnPreparedListener(this.mFourthPreparedListener);
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

    protected boolean isSystemBarVisible() {
        return SystemBarUtil.isSystemUIVisible(getActivity());
    }
}
