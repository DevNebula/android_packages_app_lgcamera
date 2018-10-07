package com.lge.camera.managers.ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.text.SpannableString;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.components.ShiftImageSpan;
import com.lge.camera.components.SnapMovieSeekBar;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.TalkBackUtil;
import com.lge.camera.util.Utils;
import java.io.IOException;
import java.util.Locale;

public class SnapMovieManagerBase extends SnapMovieInterfaceImpl {
    private final int MAX_RETRY_PLAY_VIDEO = 10;
    protected SnapMovieSeekBar mBar = null;
    private OnTouchListener mBarInnerLayoutTouchListener = new C12995();
    private OnTouchListener mBarLayoutTouchListener = new C12984();
    protected RelativeLayout mBarRotateInnerLayout = null;
    protected RotateLayout mBarRotateLayout = null;
    protected int mBarStartOnScreen = 0;
    protected RelativeLayout mBarWarpLayout = null;
    protected RelativeLayout mBaseLayout = null;
    protected RelativeLayout mButtonLayout = null;
    private OnTouchListener mDeleteAllTouchListener = new C13017();
    private OnClickListener mDeleteClickListener = new C13006();
    protected int mFixedDegree = -1;
    protected TextView mGuideText = null;
    protected RelativeLayout mGuideTextLayout = null;
    private OnInfoListener mInfoListener = new C129416();
    protected boolean mIsRecording = false;
    private MediaPlayer mMediaPlayer = null;
    protected boolean mOrientationFixed = false;
    protected ImageButton mPlayButton = null;
    protected RelativeLayout mPlayButtonLayout = null;
    private OnClickListener mPlayClickListener = new C13039();
    protected int mPreSelectedIndex = -1;
    private OnPreparedListener mPreparedListener = new C129014();
    protected RotateTextView mResetButton = null;
    private int mRetryCountPlayVideo = 0;
    protected RotateTextView mSaveButton = null;
    private OnTouchListener mSaveTouchListener = new C13028();
    protected RelativeLayout mSeekBarLayout = null;
    private OnSeekCompleteListener mSeekCompleteListener = new C129115();
    protected ShotItemList mShotList = new ShotItemList();
    protected ImageView mThumbHandler = null;
    protected RelativeLayout mThumbHandlerLayout = null;
    private OnTouchListener mThumbHandlerLayoutTouchListener = new C12973();
    private View mThumbInnerFrame = null;
    protected ImageView mThumbnail = null;
    protected ImageView mThumbnailDelete = null;
    protected TextView mThumbnailDuration = null;
    protected RelativeLayout mThumbnailFrameLayout = null;
    protected RelativeLayout mThumbnailInnerLayout = null;
    protected RelativeLayout mThumbnailLayout = null;
    protected RotateTextView mTimeText = null;
    private SurfaceTexture mVideoSurfaceTexture = null;
    private TextureView mVideoTextureView = null;

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$14 */
    class C129014 implements OnPreparedListener {
        C129014() {
        }

        public void onPrepared(MediaPlayer mp) {
            if (mp != null) {
                mp.start();
                mp.setVolume(0.0f);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$15 */
    class C129115 implements OnSeekCompleteListener {
        C129115() {
        }

        public void onSeekComplete(final MediaPlayer mp) {
            SnapMovieManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(SnapMovieManagerBase.this) {
                public void handleRun() {
                    if (SnapMovieManagerBase.this.mGet != null && mp != null) {
                        SnapMovieManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(SnapMovieManagerBase.this) {
                            public void handleRun() {
                                if (SnapMovieManagerBase.this.mMediaPlayer != null) {
                                    SnapMovieManagerBase.this.mMediaPlayer.start();
                                }
                                if (SnapMovieManagerBase.this.mThumbnail != null) {
                                    SnapMovieManagerBase.this.mThumbnail.setVisibility(4);
                                }
                            }
                        }, 0);
                    }
                }
            }, 0);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$16 */
    class C129416 implements OnInfoListener {
        C129416() {
        }

        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == 3 && mp != null) {
                mp.pause();
                mp.seekTo(1);
                mp.setVolume(1.0f);
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$1 */
    class C12951 implements OnTouchListener {
        C12951() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$2 */
    class C12962 implements SurfaceTextureListener {
        C12962() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "textureVuew SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "textureVuew Destroyed");
            return false;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            SnapMovieManagerBase.this.mVideoSurfaceTexture = surface;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$3 */
    class C12973 implements OnTouchListener {
        C12973() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "thumb handler layout DOWN");
                    if (SnapMovieManagerBase.this.mThumbHandlerLayout != null && SnapMovieManagerBase.this.mThumbHandlerLayout.getVisibility() == 0) {
                        SnapMovieManagerBase.this.mThumbHandlerLayout.setPressed(true);
                    }
                    SnapMovieManagerBase.this.setThumbHandlersPressed(true);
                    break;
                case 1:
                    CamLog.m3d(CameraConstants.TAG, "thumb handler layout UP");
                    if (SnapMovieManagerBase.this.mThumbHandlerLayout != null) {
                        SnapMovieManagerBase.this.mThumbHandlerLayout.setPressed(false);
                        break;
                    }
                    break;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$4 */
    class C12984 implements OnTouchListener {
        C12984() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "setThumbHandlersPressed! : " + v.getId());
                    SnapMovieManagerBase.this.setThumbHandlersPressed(true);
                    break;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$5 */
    class C12995 implements OnTouchListener {
        C12995() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int index;
            switch (event.getAction()) {
                case 0:
                    if (SnapMovieManagerBase.this.isThumbHandlersPressed()) {
                        int x = (int) event.getX();
                        boolean isAvailablePosX = SnapMovieManagerBase.this.isAvailablePosX(x);
                        if (SnapMovieManagerBase.this.mBar != null && SnapMovieManagerBase.this.mBar.getMode() == 1) {
                            index = SnapMovieManagerBase.this.mBar.getIndexByPos(x - SnapMovieManagerBase.this.mBarStartOnScreen);
                            if (!isAvailablePosX || SnapMovieManagerBase.this.mPreSelectedIndex == index) {
                                SnapMovieManagerBase.this.setThumbHandlersPressed(false);
                                SnapMovieManagerBase.this.mPreSelectedIndex = -1;
                                SnapMovieManagerBase.this.mBar.setSelectedIndex(-1);
                                SnapMovieManagerBase.this.setVisibleThumb(false);
                                return true;
                            }
                        }
                        if (SnapMovieManagerBase.this.mThumbHandlerLayout == null || SnapMovieManagerBase.this.mThumbHandlerLayout.isPressed() || isAvailablePosX) {
                            index = SnapMovieManagerBase.this.setThumbLayoutX(x);
                            if (SnapMovieManagerBase.this.mPreSelectedIndex == index) {
                                return true;
                            }
                            SnapMovieManagerBase.this.mPreSelectedIndex = index;
                            SnapMovieManagerBase.this.stopThumbVideoPlaying();
                            SnapMovieManagerBase.this.updateThumb(false);
                            return true;
                        }
                        SnapMovieManagerBase.this.setThumbHandlersPressed(false);
                        return true;
                    } else if (SnapMovieManagerBase.this.mBar != null && SnapMovieManagerBase.this.mBar.getMode() == 1) {
                        SnapMovieManagerBase.this.mPreSelectedIndex = -1;
                        SnapMovieManagerBase.this.setVisibleThumb(false);
                        SnapMovieManagerBase.this.setThumbHandlersPressed(false);
                        return true;
                    }
                    break;
                case 1:
                case 3:
                    return doTouchUp((int) event.getX());
                case 2:
                    if (SnapMovieManagerBase.this.isThumbHandlersPressed()) {
                        index = SnapMovieManagerBase.this.setThumbLayoutX((int) event.getX());
                        if (SnapMovieManagerBase.this.mPreSelectedIndex == index) {
                            return true;
                        }
                        SnapMovieManagerBase.this.mPreSelectedIndex = index;
                        SnapMovieManagerBase.this.updateThumb(false);
                        return true;
                    }
                    break;
            }
            return false;
        }

        private boolean doTouchUp(int x) {
            if (!SnapMovieManagerBase.this.isThumbHandlersPressed()) {
                return false;
            }
            SnapMovieManagerBase.this.setThumbHandlersPressed(false);
            SnapMovieManagerBase.this.mPreSelectedIndex = SnapMovieManagerBase.this.setThumbLayoutX(x);
            if (SnapMovieManagerBase.this.mPreSelectedIndex == -1) {
                return true;
            }
            SnapMovieManagerBase.this.setThumbLayoutXByIndex(SnapMovieManagerBase.this.mPreSelectedIndex);
            SnapMovieManagerBase.this.updateThumb(true);
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$6 */
    class C13006 implements OnClickListener {
        C13006() {
        }

        public void onClick(View arg0) {
            SnapMovieManagerBase.this.setVisibleThumbLayout(false);
            int selectedIndex = -1;
            if (SnapMovieManagerBase.this.mBar != null) {
                selectedIndex = SnapMovieManagerBase.this.mBar.getSelectedIndex();
            }
            if (SnapMovieManagerBase.this.isAvailableShotListIndex(selectedIndex)) {
                SnapMovieManagerBase.this.deleteShot(selectedIndex);
                if (SnapMovieManagerBase.this.mShotList != null && SnapMovieManagerBase.this.mShotList.size() > 0) {
                    int nextIndex;
                    int lastIndex = SnapMovieManagerBase.this.mShotList.getLastIndex();
                    if (selectedIndex >= lastIndex) {
                        nextIndex = lastIndex;
                    } else {
                        nextIndex = selectedIndex;
                    }
                    SnapMovieManagerBase.this.setThumbLayoutXByIndex(nextIndex);
                    SnapMovieManagerBase.this.setVisibleThumbLayout(true);
                    SnapMovieManagerBase.this.setVisibleThumb(false);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$7 */
    class C13017 implements OnTouchListener {
        boolean outOfBounds = false;

        C13017() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    ((RotateTextView) view).setColorFilter(ColorUtil.getSeletedColor(SnapMovieManagerBase.this.mGet.getAppContext().getResources().getColor(C0088R.color.camera_accent_txt)));
                    this.outOfBounds = false;
                    break;
                case 1:
                    ((RotateTextView) view).setColorFilter(null);
                    if (SnapMovieManagerBase.this.isTouchOnView(view, event.getRawX(), event.getRawY()) && !this.outOfBounds) {
                        view.playSoundEffect(0);
                        CamLog.m3d(CameraConstants.TAG, "delete all action");
                        if (SnapMovieManagerBase.this.mGet.checkModuleValidate(223) && SnapMovieManagerBase.this.mStatus < 3) {
                            SnapMovieManagerBase.this.setVisibleThumb(false);
                            SnapMovieManagerBase.this.mGet.showDialog(131);
                            break;
                        }
                        CamLog.m3d(CameraConstants.TAG, "EXIT delete");
                        break;
                    }
                    break;
                case 2:
                    if (!SnapMovieManagerBase.this.isTouchOnView(view, event.getRawX(), event.getRawY())) {
                        ((RotateTextView) view).setColorFilter(null);
                        this.outOfBounds = true;
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$8 */
    class C13028 implements OnTouchListener {
        boolean outOfBounds = false;

        C13028() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    this.outOfBounds = false;
                    ((RotateTextView) view).setColorFilter(ColorUtil.getSeletedColor(SnapMovieManagerBase.this.mGet.getAppContext().getResources().getColor(C0088R.color.camera_accent_txt)));
                    break;
                case 1:
                    ((RotateTextView) view).setColorFilter(null);
                    if (SnapMovieManagerBase.this.isTouchOnView(view, event.getRawX(), event.getRawY()) && !this.outOfBounds) {
                        view.playSoundEffect(0);
                        CamLog.m3d(CameraConstants.TAG, "save action");
                        if (SnapMovieManagerBase.this.mGet.checkModuleValidate(223) && SnapMovieManagerBase.this.mStatus < 3) {
                            SnapMovieManagerBase.this.mGet.setSavePath(null);
                            SnapMovieManagerBase.this.saveVideoClips();
                            break;
                        }
                        CamLog.m3d(CameraConstants.TAG, "EXIT save");
                        break;
                    }
                    break;
                case 2:
                    if (!SnapMovieManagerBase.this.isTouchOnView(view, event.getRawX(), event.getRawY())) {
                        ((RotateTextView) view).setColorFilter(null);
                        this.outOfBounds = true;
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.SnapMovieManagerBase$9 */
    class C13039 implements OnClickListener {
        C13039() {
        }

        public void onClick(View view) {
            CamLog.m3d(CameraConstants.TAG, "click play");
            if (SnapMovieManagerBase.this.mGet != null && SnapMovieManagerBase.this.mGet.checkModuleValidate(223) && SnapMovieManagerBase.this.mStatus < 3) {
                SnapMovieManagerBase.this.setVisibleThumb(false);
                LdbUtil.sendLDBIntent(SnapMovieManagerBase.this.getAppContext(), LdbConstants.LDB_FEATURE_NAME_SNAP_MOVIE, -1, LdbConstants.LDB_PLAY);
                SnapMovieManagerBase.this.launchVideoStudio("preview_only");
            }
        }
    }

    public SnapMovieManagerBase(SnapMovieInterface snapMovieInterface) {
        super(snapMovieInterface);
    }

    public void createViews(View baseView) {
        if (baseView == null || this.mGet == null) {
            CamLog.m3d(CameraConstants.TAG, "baseView is null, return.");
            return;
        }
        baseView.setVisibility(0);
        this.mBaseLayout = (RelativeLayout) baseView.findViewById(C0088R.id.snap_movie_view_layout);
        this.mBaseLayout.setVisibility(4);
        this.mBarRotateLayout = (RotateLayout) this.mBaseLayout.findViewById(C0088R.id.snap_movie_view_rotate_layout);
        this.mBarRotateLayout.setVisibility(4);
        this.mBarRotateInnerLayout = (RelativeLayout) this.mBarRotateLayout.findViewById(C0088R.id.snap_movie_view_rotate_inner_layout);
        this.mBarRotateInnerLayout.setOnTouchListener(this.mBarInnerLayoutTouchListener);
        this.mSeekBarLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_seek_bar_layout);
        this.mTimeText = (RotateTextView) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_duration_text);
        this.mTimeText.setVisibility(4);
        this.mGuideTextLayout = (RelativeLayout) this.mBaseLayout.findViewById(C0088R.id.snap_movie_guide_layout);
        this.mGuideText = (TextView) this.mBaseLayout.findViewById(C0088R.id.snap_movie_guide_text);
        String guideText = this.mGet.getAppContext().getString(C0088R.string.snap_init_guide2);
        this.mGuideText.setText(makeSpannableGuideString(this.mGet.getAppContext(), guideText));
        this.mGuideText.append("\n ");
        this.mGuideText.setContentDescription(guideText.replace("#02#", this.mGet.getAppContext().getString(C0088R.string.start_recording_button)));
        this.mBarRotateLayout.rotateLayout(this.mGet.getOrientationDegree());
        this.mPlayButtonLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_play_button_layout);
        this.mPlayButton = (ImageButton) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_play_button);
        this.mPlayButton.setVisibility(4);
        this.mPlayButton.setOnClickListener(this.mPlayClickListener);
        this.mButtonLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_button_layout);
        this.mSaveButton = (RotateTextView) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_save_button);
        this.mSaveButton.setText(getAppContext().getString(C0088R.string.sp_save_SHORT));
        this.mSaveButton.setContentDescription(getAppContext().getString(C0088R.string.sp_save_SHORT));
        this.mSaveButton.setVisibility(4);
        this.mSaveButton.setClickable(true);
        this.mSaveButton.setOnTouchListener(this.mSaveTouchListener);
        this.mResetButton = (RotateTextView) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_reset_button);
        this.mResetButton.setText(getAppContext().getString(C0088R.string.snap_reset));
        this.mResetButton.setContentDescription(getAppContext().getString(C0088R.string.snap_reset));
        this.mResetButton.setVisibility(4);
        this.mResetButton.setClickable(true);
        this.mResetButton.setOnTouchListener(this.mDeleteAllTouchListener);
        this.mBar = (SnapMovieSeekBar) this.mSeekBarLayout.findViewById(C0088R.id.snap_movie_seek_bar);
        this.mBar.init(getActivity());
        this.mBar.setMaxTime(SnapMovieInterfaceImpl.SHOT_TIME_MAX);
        this.mBarWarpLayout = (RelativeLayout) this.mSeekBarLayout.findViewById(C0088R.id.snap_movie_seek_bar_wrap_layout);
        this.mBarWarpLayout.setOnTouchListener(this.mBarLayoutTouchListener);
        this.mThumbnailLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_seek_bar_thumbnail_layout);
        this.mThumbnailInnerLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail_inner_layout);
        this.mThumbnailInnerLayout.setVisibility(4);
        this.mThumbnailInnerLayout.setOnTouchListener(new C12951());
        this.mThumbnailFrameLayout = (RelativeLayout) this.mBarRotateInnerLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail_frame_layout);
        this.mThumbnail = (ImageView) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail);
        this.mThumbnail.setVisibility(0);
        this.mThumbnailDuration = (TextView) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail_duration);
        this.mThumbnailDelete = (ImageView) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail_delete);
        this.mThumbnailDelete.setVisibility(4);
        this.mThumbnailDelete.setOnClickListener(this.mDeleteClickListener);
        this.mThumbHandlerLayout = (RelativeLayout) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_handler_layout);
        this.mThumbHandlerLayout.setOnTouchListener(this.mThumbHandlerLayoutTouchListener);
        this.mThumbHandler = (ImageView) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_handler);
        this.mVideoTextureView = (TextureView) this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_thumbnail_video_texture_view);
        this.mVideoTextureView.setSurfaceTextureListener(new C12962());
        this.mVideoTextureView.setVisibility(0);
        this.mThumbInnerFrame = this.mThumbnailLayout.findViewById(C0088R.id.snap_movie_seek_thumbnail_inner_frame);
    }

    protected void setVisibleDuration(boolean isVisible) {
        if (this.mTimeText != null) {
            this.mTimeText.setVisibility(isVisible ? 0 : 4);
        }
    }

    protected void setVisibleButton(boolean isVisible) {
        if (this.mPlayButton != null && this.mSaveButton != null && this.mResetButton != null) {
            int visibility = isVisible ? 0 : 4;
            this.mPlayButton.setVisibility(visibility);
            this.mSaveButton.setVisibility(visibility);
            this.mResetButton.setVisibility(visibility);
        }
    }

    public void setVisibleGuideText(boolean isVisible) {
        if (this.mGuideTextLayout == null) {
            return;
        }
        if (!isVisible || this.mShotList == null || this.mShotList.size() <= 0) {
            this.mGuideTextLayout.setVisibility(isVisible ? 0 : 4);
        }
    }

    public void setVisibleThumbLayout(boolean isVisible) {
        if (this.mThumbnailLayout != null) {
            this.mThumbnailLayout.setVisibility(isVisible ? 0 : 4);
        }
        if (!isVisible) {
            stopThumbVideoPlaying();
        }
    }

    public void setVisibleThumb(boolean isVisible) {
        int i = 0;
        if (this.mThumbnailInnerLayout != null && this.mThumbnailDelete != null && this.mBar != null) {
            int visibility = isVisible ? 0 : 4;
            this.mThumbnailInnerLayout.setVisibility(visibility);
            this.mThumbnailDelete.setVisibility(visibility);
            SnapMovieSeekBar snapMovieSeekBar = this.mBar;
            if (isVisible) {
                i = 1;
            }
            snapMovieSeekBar.setMode(i);
            if (!isVisible) {
                stopThumbVideoPlaying();
            }
        }
    }

    public void setVisibleHandler(boolean isVisible) {
        if (this.mThumbHandlerLayout != null && this.mThumbHandler != null) {
            int visibility = isVisible ? 0 : 4;
            this.mThumbHandlerLayout.setVisibility(visibility);
            this.mThumbHandler.setVisibility(visibility);
        }
    }

    public void setVisibleBar(boolean isVisible) {
        if (isVisible && this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL)) {
            CamLog.m3d(CameraConstants.TAG, "some menu is showing. return");
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "setVisibleBar - " + isVisible);
        setVisibleBar(isVisible, false);
        if (!isVisible) {
            setVisibleThumb(isVisible);
            stopThumbVideoPlaying();
        }
    }

    protected void setVisibleBar(boolean isVisible, boolean isEnforce) {
        if (this.mBaseLayout != null && this.mBarRotateLayout != null) {
            if (isEnforce || this.mStatus != 0) {
                CamLog.m3d(CameraConstants.TAG, "setVisibleBar - " + isVisible + " , isEnforce - " + isEnforce);
                int visibility = isVisible ? 0 : 4;
                this.mBaseLayout.setVisibility(visibility);
                if (this.mShotList == null || this.mShotList.size() > 0 || !isVisible) {
                    this.mBarRotateLayout.setVisibility(visibility);
                } else {
                    CamLog.m3d(CameraConstants.TAG, "mShotList.size() is zero");
                }
            }
        }
    }

    protected boolean isThumbHandlersPressed() {
        if (this.mThumbHandler == null) {
            return false;
        }
        return this.mThumbHandler.isPressed();
    }

    protected void setThumbHandlersPressed(boolean isPressed) {
        if (this.mThumbHandler != null) {
            if (!isPressed) {
                this.mThumbHandler.setPressed(false);
            } else if (this.mThumbHandler.getVisibility() == 0) {
                this.mThumbHandler.setPressed(true);
            }
        }
    }

    private boolean isTouchOnView(View btnView, float x, float y) {
        if (btnView == null) {
            return false;
        }
        int[] btnLoc = new int[2];
        btnView.getLocationOnScreen(btnLoc);
        int btnWidth = btnView.getWidth();
        int btnHeight = btnView.getHeight();
        switch (this.mFixedDegree) {
            case 90:
                btnWidth = btnView.getHeight();
                btnHeight = btnView.getWidth();
                btnLoc[1] = btnLoc[1] - btnHeight;
                break;
            case 180:
                btnLoc[0] = btnLoc[0] - btnWidth;
                btnLoc[1] = btnLoc[1] - btnHeight;
                break;
            case 270:
                btnWidth = btnView.getHeight();
                btnHeight = btnView.getWidth();
                btnLoc[0] = btnLoc[0] - btnWidth;
                break;
        }
        if (x < ((float) btnLoc[0]) || x > ((float) (btnLoc[0] + btnWidth)) || y < ((float) btnLoc[1]) || y > ((float) (btnLoc[1] + btnHeight))) {
            return false;
        }
        return true;
    }

    public void launchVideoStudio(String type) {
    }

    protected void saveVideoClips() {
    }

    public SpannableString makeSpannableGuideString(Context mContext, String mTextString) {
        SpannableString ss = null;
        if (!(mContext == null || mTextString == null)) {
            String mChangeString = "#02#";
            int spanStartIndex = mTextString.indexOf(mChangeString);
            if (spanStartIndex >= 0) {
                int spanEndIndex = spanStartIndex + mChangeString.length();
                ss = new SpannableString(mTextString);
                Drawable d = mContext.getDrawable(C0088R.drawable.camera_guide_spannable_video);
                if (d != null) {
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    ss.setSpan(new ShiftImageSpan(d, 1, (float) Utils.getPx(mContext, C0088R.dimen.snapvideo_image_span_shift_up)), spanStartIndex, spanEndIndex, 17);
                }
            }
        }
        return ss;
    }

    private boolean isAvailablePosX(int x) {
        if (this.mBar == null) {
            return false;
        }
        int startPosXonAvailableArea;
        int startPosXonBar = this.mBarStartOnScreen + this.mBar.getOutlineWidth();
        if (SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage()) {
            startPosXonAvailableArea = startPosXonBar + this.mBar.getDisabledWidth();
        } else {
            startPosXonAvailableArea = startPosXonBar;
        }
        int endPosXonAvailableArea = ((this.mBarStartOnScreen + this.mBar.getCurrentWidth()) - this.mBar.getOutlineWidth()) - ((int) this.mBar.getSeparatorWidth());
        if (x < startPosXonAvailableArea || x > endPosXonAvailableArea) {
            return false;
        }
        return true;
    }

    private int[] getAvailablePosX(int x) {
        if (this.mThumbnailLayout == null || this.mThumbnail == null || this.mBar == null) {
            return new int[]{0, 0};
        }
        int startPosXonAvailableArea;
        int[] result = new int[]{x, (this.mThumbnailLayout.getWidth() / 2) - (this.mThumbnail.getWidth() / 2)};
        int startPosXonBar = this.mBarStartOnScreen + this.mBar.getOutlineWidth();
        int endPosXonBar = (this.mBarStartOnScreen + this.mBar.getWidth()) - this.mBar.getOutlineWidth();
        boolean isSecure = SecureImageUtil.isSecureCamera() && SecureImageUtil.useSecureLockImage();
        if (isSecure) {
            startPosXonAvailableArea = startPosXonBar + this.mBar.getDisabledWidth();
        } else {
            startPosXonAvailableArea = startPosXonBar;
        }
        int endPosXonAvailableArea = (this.mBarStartOnScreen + this.mBar.getCurrentWidth()) - this.mBar.getOutlineWidth();
        int thumbLengthHor = this.mThumbnail.getLeft() + this.mThumbnail.getWidth();
        int startPosXforThumb = startPosXonBar + thumbLengthHor;
        int endPosXforThumb = endPosXonBar - thumbLengthHor;
        if (x < startPosXonAvailableArea) {
            result[0] = startPosXonAvailableArea;
        }
        if (x > endPosXonAvailableArea) {
            result[0] = endPosXonAvailableArea;
        }
        if (result[0] < startPosXforThumb) {
            int xOnBar;
            if (result[0] < startPosXonBar) {
                xOnBar = startPosXonBar;
            } else {
                xOnBar = result[0];
            }
            result[1] = result[1] + ((startPosXforThumb - xOnBar) / 2);
        }
        if (result[0] <= endPosXforThumb) {
            return result;
        }
        result[1] = result[1] - (((result[0] - endPosXonBar) / 2) + (thumbLengthHor / 2));
        return result;
    }

    private int setThumbLayoutX(int x) {
        if (this.mThumbnailLayout == null || this.mThumbnailInnerLayout == null || this.mBar == null) {
            return 0;
        }
        int[] avaiablePosX = getAvailablePosX(x);
        this.mThumbnailLayout.setX((float) (avaiablePosX[0] - (this.mThumbnailLayout.getWidth() / 2)));
        LayoutParams lpThumbInner = (LayoutParams) this.mThumbnailInnerLayout.getLayoutParams();
        lpThumbInner.setMarginStart(avaiablePosX[1]);
        this.mThumbnailInnerLayout.setLayoutParams(lpThumbInner);
        return this.mBar.setSelectedIndexByPos(avaiablePosX[0] - this.mBarStartOnScreen);
    }

    protected void setThumbLayoutXByIndex(int index) {
        if (this.mBar != null && this.mShotList != null && index >= 0) {
            if (this.mShotList == null || this.mShotList.size() >= 1) {
                int curTime = this.mShotList.getCurrentTime(index);
                int curPos = this.mBar.convertTimeToPos(curTime);
                int curStart = 0;
                ShotItem item = (ShotItem) this.mShotList.valueAt(index);
                if (item != null) {
                    curTime += item.getDuration();
                    curStart = curPos - this.mBar.convertTimeToPos(item.getDuration());
                }
                setThumbLayoutX((this.mBarStartOnScreen + curStart) + ((curPos - curStart) / 2));
            }
        }
    }

    protected void updateThumb(final boolean isPlayVideo) {
        if (this.mGet != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    SnapMovieManagerBase.this.updateThumbJob(isPlayVideo);
                }
            });
        }
    }

    private void updateThumbJob(boolean isPlayVideo) {
        if (this.mBar != null && this.mShotList != null) {
            int selectedIndex = this.mBar.getSelectedIndex();
            if (isAvailableShotListIndex(selectedIndex)) {
                ShotItem item = (ShotItem) this.mShotList.valueAt(selectedIndex);
                if (item != null) {
                    Bitmap bitmap = item.getThumbnail();
                    if (bitmap != null && this.mGet != null && this.mThumbnail != null && this.mVideoTextureView != null && this.mThumbInnerFrame != null && this.mThumbnailDuration != null) {
                        LayoutParams lpThumb = (LayoutParams) this.mThumbnail.getLayoutParams();
                        LayoutParams lpVideo = (LayoutParams) this.mVideoTextureView.getLayoutParams();
                        LayoutParams lpInnerFrame = (LayoutParams) this.mThumbInnerFrame.getLayoutParams();
                        int width = Utils.getPx(getAppContext(), C0088R.dimen.snap_movie_seek_thumbnail_width);
                        int height = Utils.getPx(getAppContext(), C0088R.dimen.snap_movie_seek_thumbnail_height);
                        if (bitmap.getWidth() > bitmap.getHeight()) {
                            lpThumb.width = width;
                            lpThumb.height = height;
                        } else {
                            lpThumb.width = height;
                            lpThumb.height = width;
                        }
                        this.mThumbnail.setLayoutParams(lpThumb);
                        lpVideo.width = lpThumb.width;
                        lpVideo.height = lpThumb.height;
                        lpInnerFrame.width = lpThumb.width;
                        lpInnerFrame.height = lpThumb.height;
                        this.mVideoTextureView.setLayoutParams(lpVideo);
                        this.mThumbInnerFrame.setLayoutParams(lpInnerFrame);
                        this.mThumbnail.setImageBitmap(bitmap);
                        int durTime = (int) Math.floor((double) (((float) item.getDuration()) / 1000.0f));
                        if (durTime > 60) {
                            durTime = 60;
                        }
                        this.mThumbnailDuration.setText(String.format("%d" + getAppContext().getResources().getString(C0088R.string.snap_time_sec), new Object[]{Integer.valueOf(durTime)}));
                        setVisibleThumb(true);
                        if (isPlayVideo) {
                            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                                public void handleRun() {
                                    SnapMovieManagerBase.this.mRetryCountPlayVideo = 0;
                                    SnapMovieManagerBase.this.startThumbVideoPlaying();
                                }
                            }, 0);
                        }
                    }
                }
            }
        }
    }

    public void deleteShot(int index) {
        if (this.mGet != null && this.mShotList != null && this.mBar != null) {
            ShotItem item = (ShotItem) this.mShotList.valueAt(index);
            if (item != null && FileManager.deleteFile(item.getFilePath())) {
                this.mShotList.removeAt(index);
                if (this.mShotList.getCurrentTime() < 61000) {
                    setBarMaxTime(SnapMovieInterfaceImpl.SHOT_TIME_MAX);
                }
                updateSnapDurationTime(this.mShotList.getCurrentTime());
                this.mBar.removeSeparator(index);
                if (this.mShotList.size() < 1) {
                    setVisibleThumb(false);
                    setVisibleThumbLayout(false);
                    setVisibleButton(false);
                    setVisibleGuideText(true);
                    setVisibleDuration(false);
                    setOrientationFixed(false);
                }
                if (this.mBar.getDisabledSize() == this.mShotList.size()) {
                    setVisibleHandler(false);
                }
                this.mGet.showToast(getAppContext().getString(C0088R.string.popup_delete_done), CameraConstants.TOAST_LENGTH_SHORT);
                rotateView(this.mGet.getOrientationDegree(), false);
            }
        }
    }

    public void deleteLastShot() {
        if (this.mGet != null && this.mShotList.size() >= 1) {
            int lastIndex = this.mShotList.getLastIndex();
            if (lastIndex == 0) {
                this.mFixedDegree = -1;
                SharedPreferenceUtil.saveSnapMovieOrientation(getAppContext(), -1);
                setOrientationFixed(false);
                rotateView(this.mGet.getOrientationDegree(), false);
            }
            deleteShot(lastIndex);
        }
    }

    public void updateSnapDurationTime(int time) {
        int durTime = 61;
        if (this.mGet != null) {
            int floorTime = (int) Math.floor((double) (((float) time) / 1000.0f));
            if (floorTime <= 61) {
                durTime = floorTime;
            }
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    int minutes = durTime / 60;
                    int seconds = durTime - (minutes * 60);
                    int secondsTotal = 0;
                    if (durTime > 60) {
                        secondsTotal = seconds;
                    }
                    String totalTime = String.format(Locale.getDefault(), "%d:%02d", new Object[]{Integer.valueOf(1), Integer.valueOf(secondsTotal)});
                    String finalText = String.format(Locale.getDefault(), "%d:%02d / " + totalTime, new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)});
                    String talkBackText = TalkBackUtil.getMinutes(SnapMovieManagerBase.this.getAppContext(), minutes) + TalkBackUtil.getSeconds(SnapMovieManagerBase.this.getAppContext(), seconds, true) + " / " + TalkBackUtil.getMinutes(SnapMovieManagerBase.this.getAppContext(), 1) + TalkBackUtil.getSeconds(SnapMovieManagerBase.this.getAppContext(), secondsTotal, false);
                    if (SnapMovieManagerBase.this.mTimeText != null) {
                        SnapMovieManagerBase.this.mTimeText.setText(finalText);
                        SnapMovieManagerBase.this.mTimeText.setContentDescription(talkBackText);
                    }
                }
            }, 0);
        }
    }

    public void setOrientationFixed(boolean set) {
        CamLog.m3d(CameraConstants.TAG, "setOrientationFixed - " + set);
        this.mOrientationFixed = set;
    }

    public boolean isOrentationFixed() {
        return this.mOrientationFixed;
    }

    public void rotateView(int degree, final boolean isFirst) {
        CamLog.m3d(CameraConstants.TAG, "degree = " + degree + ", first = " + isFirst + ", fixed = " + this.mOrientationFixed + ", paused = " + this.mGet.isPaused());
        if (!this.mOrientationFixed && !this.mGet.isPaused() && this.mBarRotateLayout != null && this.mBarRotateInnerLayout != null && this.mSeekBarLayout != null) {
            int seekBarBottomLand;
            boolean isLand;
            rotateGuideText(this.mGuideTextLayout, degree);
            if (!isFirst) {
                setVisibleBar(false);
            }
            this.mBarRotateLayout.rotateLayout(degree);
            LayoutParams lpRotateInnerLayout = (LayoutParams) this.mBarRotateInnerLayout.getLayoutParams();
            LayoutParams lpBarLayout = (LayoutParams) this.mSeekBarLayout.getLayoutParams();
            int distanceOfBarAndPlayButtonBottom = Utils.getPx(getAppContext(), C0088R.dimen.snap_movie_distance_seek_bar_play_btn_bottom);
            int seekBarBottomPort = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.282f);
            if (degree == 270) {
                seekBarBottomLand = Utils.getPx(getAppContext(), C0088R.dimen.snap_movie_seek_bar_bottom_margin_270_degree);
            } else {
                seekBarBottomLand = Utils.getPx(getAppContext(), C0088R.dimen.snap_movie_seek_bar_bottom_margin_90_degree);
            }
            if (degree == 90 || degree == 270) {
                isLand = true;
            } else {
                isLand = false;
            }
            if (isLand) {
                lpRotateInnerLayout.bottomMargin = seekBarBottomLand - distanceOfBarAndPlayButtonBottom;
                lpBarLayout.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.474f);
            } else {
                lpRotateInnerLayout.bottomMargin = seekBarBottomPort - distanceOfBarAndPlayButtonBottom;
                lpBarLayout.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.642f);
            }
            this.mBarRotateInnerLayout.setLayoutParams(lpRotateInnerLayout);
            this.mSeekBarLayout.setLayoutParams(lpBarLayout);
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (SnapMovieManagerBase.this.mGet != null && SnapMovieManagerBase.this.mBarRotateInnerLayout != null && SnapMovieManagerBase.this.mSeekBarLayout != null && SnapMovieManagerBase.this.mBarWarpLayout != null && SnapMovieManagerBase.this.mBar != null) {
                        SnapMovieManagerBase.this.mBarStartOnScreen = ((SnapMovieManagerBase.this.mBarRotateInnerLayout.getLeft() + SnapMovieManagerBase.this.mSeekBarLayout.getLeft()) + SnapMovieManagerBase.this.mBarWarpLayout.getLeft()) + SnapMovieManagerBase.this.mBar.getLeft();
                        SnapMovieManagerBase.this.updateBar();
                        CamLog.m3d(CameraConstants.TAG, "set visible bar on rotateView, first=" + isFirst + " setting=" + SnapMovieManagerBase.this.mGet.isSettingMenuVisible() + " QClip=" + SnapMovieManagerBase.this.mGet.getQuickClipManager().isOpened());
                        if (!isFirst && !SnapMovieManagerBase.this.mGet.isSettingMenuVisible() && !SnapMovieManagerBase.this.mGet.getQuickClipManager().isOpened() && !SnapMovieManagerBase.this.mGet.isModeMenuVisible() && !SnapMovieManagerBase.this.mGet.isHelpListVisible()) {
                            SnapMovieManagerBase.this.setVisibleBar(true);
                        }
                    }
                }
            }, 0);
        }
    }

    private int getMeasuredWidth(RotateTextView view) {
        if (view.getWidth() > 0) {
            return view.getWidth();
        }
        view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        return view.getMeasuredWidth();
    }

    private int getMeasureWidth(View view) {
        if (view.getWidth() > 0) {
            return view.getWidth();
        }
        view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        return view.getMeasuredWidth();
    }

    protected void updateBar() {
        if (this.mBar != null) {
            int selectedIndex = this.mBar.getSelectedIndex();
            if (isAvailableShotListIndex(selectedIndex)) {
                setThumbLayoutXByIndex(selectedIndex);
            }
        }
    }

    protected void rotateGuideText(View layout, int degree) {
        if (this.mGet != null && layout != null && this.mGuideText != null) {
            TextView textGuide = (TextView) layout.findViewById(C0088R.id.snap_movie_guide_text);
            if (textGuide != null) {
                LayoutParams lpTextGuide = (LayoutParams) textGuide.getLayoutParams();
                RelativeLayout textInnerLayout = (RelativeLayout) layout.findViewById(C0088R.id.snap_movie_guide_text_inner_layout);
                LayoutParams lpInnerLayout = (LayoutParams) textInnerLayout.getLayoutParams();
                Utils.resetLayoutParameter(lpInnerLayout);
                textInnerLayout.setLayoutDirection(0);
                lpTextGuide.topMargin = 0;
                lpTextGuide.bottomMargin = 0;
                lpTextGuide.setMarginStart(0);
                lpTextGuide.setMarginEnd(0);
                lpTextGuide.width = Utils.getLCDsize(this.mGet.getAppContext(), true)[1] - (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.snap_movie_guide_text_port_margin) * 2);
                int mTextViewBottomMargin = getTextGuideBottomMargin(this.mGet.getAppContext(), textGuide, lpTextGuide.width, true);
                lpTextGuide.addRule(14, 1);
                lpTextGuide.addRule(12);
                if (degree == 0 || degree == 180) {
                    lpTextGuide.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.22f);
                } else if (degree == 90) {
                    lpTextGuide.width = Utils.getLCDsize(this.mGet.getAppContext(), true)[0] - (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.snap_movie_guide_text_land_margin) * 2);
                    lpTextGuide.bottomMargin = getTextGuideBottomMargin(this.mGet.getAppContext(), textGuide, lpTextGuide.width, false);
                } else if (degree == 270) {
                    lpTextGuide.width = Utils.getLCDsize(this.mGet.getAppContext(), true)[0] - (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.snap_movie_guide_text_land_margin) * 2);
                    lpTextGuide.bottomMargin = getTextGuideBottomMargin(this.mGet.getAppContext(), textGuide, lpTextGuide.width, false);
                }
                textInnerLayout.setLayoutParams(lpInnerLayout);
                textGuide.setLayoutParams(lpTextGuide);
                RotateLayout textRotate = (RotateLayout) layout.findViewById(C0088R.id.snap_movie_guide_text_rotate_layout);
                if (textRotate != null) {
                    textRotate.rotateLayout(degree);
                }
            }
        }
    }

    protected boolean isAvailableShotListIndex(int index) {
        return index > -1 && index < this.mShotList.size();
    }

    public String[] getInputSize(ListPreference listPref) {
        String inputSize = "1920x1080";
        String previewSize = "1920x1080";
        if (ModelProperties.getAppTier() < 3) {
            inputSize = "1280x720";
            previewSize = "1280x720";
        }
        String tmpWideSize = inputSize;
        if (listPref != null) {
            boolean isFound = false;
            CharSequence[] entryValues = listPref.getEntryValues();
            for (int i = 0; i < entryValues.length; i++) {
                String itemValue = entryValues[i];
                if (itemValue != null && itemValue.equals(inputSize)) {
                    isFound = true;
                    inputSize = itemValue;
                    previewSize = listPref.getExtraInfo(1, i);
                    break;
                }
                if (Utils.isWidePictureSize(Utils.sizeStringToArray(itemValue))) {
                    tmpWideSize = itemValue;
                }
            }
            if (!isFound) {
                inputSize = listPref.getDefaultValue();
                if (!Utils.isWidePictureSize(Utils.sizeStringToArray(inputSize))) {
                    inputSize = tmpWideSize;
                }
                previewSize = listPref.getExtraInfo(1, listPref.findIndexOfValue(inputSize));
            }
        }
        return new String[]{inputSize, previewSize};
    }

    private void reStartThumbVideoPlaying() {
        if (this.mVideoTextureView != null) {
            SurfaceTexture surfaceTexture = this.mVideoTextureView.getSurfaceTexture();
            if (surfaceTexture != null) {
                this.mVideoSurfaceTexture = surfaceTexture;
            } else if (this.mRetryCountPlayVideo > 10) {
                this.mRetryCountPlayVideo = 0;
            } else {
                this.mRetryCountPlayVideo++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startThumbVideoPlaying();
            }
        }
    }

    private void startThumbVideoPlaying() {
        CamLog.m3d(CameraConstants.TAG, "retry = " + this.mRetryCountPlayVideo);
        if (this.mBar != null) {
            int selectedIndex = this.mBar.getSelectedIndex();
            if (isAvailableShotListIndex(selectedIndex)) {
                ShotItem item = (ShotItem) this.mShotList.valueAt(selectedIndex);
                CamLog.m3d(CameraConstants.TAG, "item=" + item + " mVideoTextureView=" + this.mVideoTextureView);
                if (item != null && this.mVideoTextureView != null) {
                    this.mVideoTextureView.setVisibility(0);
                    try {
                        if (this.mVideoSurfaceTexture == null) {
                            reStartThumbVideoPlaying();
                        }
                        Surface surface = new Surface(this.mVideoSurfaceTexture);
                        if (this.mMediaPlayer == null) {
                            this.mMediaPlayer = new MediaPlayer();
                        }
                        this.mMediaPlayer.setDataSource(item.getFilePath());
                        this.mMediaPlayer.setSurface(surface);
                        this.mMediaPlayer.setLooping(true);
                        this.mMediaPlayer.prepareAsync();
                        this.mMediaPlayer.setOnSeekCompleteListener(this.mSeekCompleteListener);
                        this.mMediaPlayer.setOnInfoListener(this.mInfoListener);
                        this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                        return;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        return;
                    } catch (SecurityException e2) {
                        e2.printStackTrace();
                        return;
                    } catch (IllegalStateException e3) {
                        e3.printStackTrace();
                        return;
                    } catch (IOException e4) {
                        e4.printStackTrace();
                        return;
                    }
                }
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "exit there no available index");
        }
    }

    protected void stopThumbVideoPlaying() {
        if (this.mThumbnail != null) {
            this.mThumbnail.setVisibility(0);
        }
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
        }
    }

    public int getFixedDegree() {
        return this.mFixedDegree;
    }

    public void onPauseAfter() {
        setVisibleBar(false);
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        super.onPauseAfter();
    }

    protected int getTextGuideBottomMargin(Context mContext, TextView mTextView, int maxWidth, boolean portrait) {
        return 0;
    }

    protected void setBarMaxTime(int time) {
        if (this.mBar != null) {
            this.mBar.setMaxTime(time);
        }
    }

    public void showGuideTextByList() {
        if (this.mShotList == null || this.mShotList.size() <= 0) {
            setVisibleGuideText(true);
        }
    }
}
