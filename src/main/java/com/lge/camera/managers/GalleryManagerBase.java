package com.lge.camera.managers;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.p000v4.view.ViewCompat;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.GalleryViewPager;
import com.lge.camera.components.GalleryViewPager.OnGalleryViewPagerListener;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateImageView;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.database.OverlapProjectDbAdapter;
import com.lge.camera.file.Exif;
import com.lge.camera.file.ExifInterface;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.GalleryPagerAdapter.GalleryPagerListener;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SquareUtil;
import com.lge.camera.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class GalleryManagerBase extends ManagerInterfaceImpl implements OnPageChangeListener, OnGalleryViewPagerListener {
    protected static final float MAIN_ACCESS_GUIDE_ICON_TOP_MARGIN_LAND = 0.10139f;
    protected static final float MAIN_ACCESS_GUIDE_ICON_TOP_MARGIN_PORT = 0.10139f;
    protected static final float MAIN_ACCESS_GUIDE_TEXT_TOP_MARGIN = 0.01111f;
    private static final float MAIN_ACCESS_VIEW_ICON_SIZE = 0.1528f;
    private static final float MAIN_ACCESS_VIEW_TEXT_SIZE = 0.0333f;
    private static final float MAIN_ACCESS_VIEW_TEXT_WIDTH = 0.5556f;
    protected static final int SQUARE_MSG_ORIGINAL_IMAGE_MAKING = 1;
    public static final int STATE_SQUARE_SNAP_IDLE = 0;
    public static final int STATE_SQUARE_SNAP_VIDEO_PALYING = 1;
    public static final int STATE_SQUARE_SNAP_VIDEO_PAUSE = 3;
    public static final int STATE_SQUARE_SNAP_VIDEO_PAUSE_BEFORE = 2;
    protected static final int VIEW_PAGER_ALIVE_BITMAP_CNT = 3;
    protected static final int VIEW_PAGER_OFFSCREEN_CNT = 1;
    private OnClickListener mBurstPlayButtonClilckListener = new C092912();
    protected RotateImageView mBurstShotMark;
    protected LinkedBlockingQueue<SquareSnapGalleryItem> mCaptureDataQueue;
    private OnCompletionListener mCompletionListener = new C092811();
    protected View mControlButtonLayout;
    protected int mCurrentIndex;
    protected RotateImageButton mDeleteButton;
    private OnClickListener mDeleteButtonClickListener = new C09367();
    protected int mDeleteIndex;
    protected ArrayList<SquareSnapGalleryItem> mGalleryItemList;
    protected FrameLayout mGalleryLayout;
    protected GalleryPagerAdapter mGalleryPagerAdapter;
    private GalleryPagerListener mGalleryPagerListenr = new C09301();
    protected RotateLayout mGalleryPlayButtonLayout;
    protected RotateLayout mGalleryRotateLayout;
    protected GalleryViewPager mGalleryViewPager;
    protected HandlerThread mImageHandlerThread;
    private OnInfoListener mInfoListener = new C09389();
    protected boolean mIsDelAnimDirectionRtoL = true;
    protected boolean mIsDeleting = false;
    protected boolean mIsMuteVideo = false;
    protected boolean mIsNewItemAdding = false;
    protected boolean mIsUndoing = false;
    protected GalleryManagerInterface mListener;
    protected RotateLayout mMainAccessView;
    protected RotateImageView mMainAccessViewImageCue;
    protected MediaPlayer mMediaPlayer;
    protected RotateImageButton mMuteButton;
    private OnClickListener mMuteButtonClickListener = new C09356();
    protected ImageButton mPlayButton;
    protected OnClickListener mPlayButtonClickListener = new C09323();
    private OnPreparedListener mPreparedListener = new C092710();
    protected int mPreviousIndex;
    protected SquareSnapGalleryItem mReadyToDeleteItem;
    protected int mSquareSnapState = 0;
    protected ImageView mSquareSnapshotAnimationFrontView;
    protected ImageView mSquareSnapshotAnimationRearView;
    protected Handler mThreadHandler = null;
    protected View mTouchBlockCoverView;
    protected TextureView mVideoPlayerTextureView;
    protected SurfaceTexture mVideoSurfaceTexture;

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$10 */
    class C092710 implements OnPreparedListener {
        C092710() {
        }

        public void onPrepared(MediaPlayer mp) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] onPrepared");
            if (mp != null) {
                AudioUtil.setAudioFocus(GalleryManagerBase.this.getAppContext(), true);
                mp.start();
                if (GalleryManagerBase.this.mIsMuteVideo) {
                    mp.setVolume(0.0f);
                } else {
                    mp.setVolume(1.0f);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$11 */
    class C092811 implements OnCompletionListener {
        C092811() {
        }

        public void onCompletion(MediaPlayer arg0) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] onCompletion");
            GalleryManagerBase.this.stopVideoPlay();
            GalleryManagerBase.this.mVideoPlayerTextureView.setVisibility(4);
            GalleryManagerBase.this.mPlayButton.setImageResource(C0088R.drawable.selector_snapshot_video_play);
            GalleryManagerBase.this.showGalleryControlUI(true, 1, false);
            if (!"on".equals(GalleryManagerBase.this.mGet.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(GalleryManagerBase.this.getAppContext(), false);
            }
            GalleryManagerBase.this.mGet.getHandler().removeMessages(94);
            GalleryManagerBase.this.mGet.getHandler().sendEmptyMessageDelayed(93, CameraConstants.TOAST_LENGTH_SHORT);
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$12 */
    class C092912 implements OnClickListener {
        C092912() {
        }

        public void onClick(View arg0) {
            if (GalleryManagerBase.this.mListener != null) {
                GalleryManagerBase.this.mListener.setStartGalleryLocation(new float[]{0.15f, 0.52f});
                GalleryManagerBase.this.mListener.startGallery(((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mUri, 2);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$1 */
    class C09301 implements GalleryPagerListener {
        C09301() {
        }

        public void onInstantiateItem(int position) {
            if (position == 0 && GalleryManagerBase.this.mCurrentIndex == 0 && GalleryManagerBase.this.mGalleryItemList != null && GalleryManagerBase.this.mGalleryItemList.size() > 0) {
                GalleryManagerBase.this.showGalleryControlUI(true, ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mType, false);
                if (GalleryManagerBase.this.mSquareSnapState == 3 || GalleryManagerBase.this.mSquareSnapState == 1) {
                    GalleryManagerBase.this.stopVideoPlay();
                }
            }
            if (GalleryManagerBase.this.mSquareSnapState == 2) {
                GalleryManagerBase.this.setSquareSnapshotState(3);
            }
        }

        public void onClicked() {
            if (GalleryManagerBase.this.mListener != null) {
                GalleryManagerBase.this.mListener.onGalleryImageViewClicked();
            }
        }

        public Bitmap getTempBitmap(Uri uri, int type) {
            return GalleryManagerBase.this.getThumbnailBitmap(uri, type, false);
        }

        public void onTouchStateChanged(boolean isTouchDown) {
            if (GalleryManagerBase.this.mListener != null) {
                GalleryManagerBase.this.mListener.onGalleryViewTouched(isTouchDown);
            }
        }

        public void onZoomScaleStart() {
            CamLog.m3d(CameraConstants.TAG, "[Cell], onZoomScaleStart!!");
            if (GalleryManagerBase.this.mGalleryItemList != null && !GalleryManagerBase.this.mIsDeleting && ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(0)).mType != -1 && ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mType != 1) {
                if (((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mBitmapState == 2) {
                    CamLog.m11w(CameraConstants.TAG, "[Cell], already original image, so return");
                    return;
                }
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = GalleryManagerBase.this.mCurrentIndex;
                msg.arg2 = GalleryManagerBase.this.mGalleryItemList.size();
                msg.obj = ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mUri;
                if (GalleryManagerBase.this.mThreadHandler != null) {
                    GalleryManagerBase.this.mThreadHandler.removeMessages(1);
                    GalleryManagerBase.this.mThreadHandler.sendMessage(msg);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$3 */
    class C09323 implements OnClickListener {
        C09323() {
        }

        public void onClick(View arg0) {
            if (!GalleryManagerBase.this.mIsDeleting && !GalleryManagerBase.this.mGet.getBurstProgress()) {
                if (GalleryManagerBase.this.mIsNewItemAdding || !GalleryManagerBase.this.mGet.checkModuleValidate(48)) {
                    GalleryManagerBase.this.showGalleryControlUI(true, 0, false);
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "[Cell] play button clicked");
                GalleryManagerBase.this.mGet.getHandler().removeMessages(93);
                GalleryManagerBase.this.mGet.getHandler().sendEmptyMessage(94);
                if (GalleryManagerBase.this.mSquareSnapState == 0) {
                    GalleryManagerBase.this.setSquareSnapshotState(1);
                    GalleryManagerBase.this.showGalleryControlUI(false, 1, true);
                    Surface surface = new Surface(GalleryManagerBase.this.mVideoSurfaceTexture);
                    if (GalleryManagerBase.this.mMediaPlayer == null) {
                        CamLog.m3d(CameraConstants.TAG, "[Cell] create new media player");
                        GalleryManagerBase.this.mMediaPlayer = new MediaPlayer();
                    }
                    try {
                        GalleryManagerBase.this.mMediaPlayer.reset();
                        GalleryManagerBase.this.mMediaPlayer.setDataSource(GalleryManagerBase.this.getAppContext(), ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mUri);
                        GalleryManagerBase.this.mMediaPlayer.prepareAsync();
                        GalleryManagerBase.this.mMediaPlayer.setSurface(surface);
                        GalleryManagerBase.this.mMediaPlayer.setLooping(false);
                        GalleryManagerBase.this.mMediaPlayer.setOnInfoListener(GalleryManagerBase.this.mInfoListener);
                        GalleryManagerBase.this.mMediaPlayer.setOnPreparedListener(GalleryManagerBase.this.mPreparedListener);
                        GalleryManagerBase.this.mMediaPlayer.setOnCompletionListener(GalleryManagerBase.this.mCompletionListener);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e2) {
                        e2.printStackTrace();
                    } catch (IllegalStateException e3) {
                        e3.printStackTrace();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                } else if (GalleryManagerBase.this.mSquareSnapState == 3) {
                    GalleryManagerBase.this.setSquareSnapshotState(1);
                    GalleryManagerBase.this.pauseAndResumeVideoPlay(false);
                    GalleryManagerBase.this.showGalleryControlUI(false, 1, true);
                    GalleryManagerBase.this.mVideoPlayerTextureView.setVisibility(0);
                } else {
                    if (GalleryManagerBase.this.mSquareSnapState == 2) {
                    }
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$4 */
    class C09334 implements SurfaceTextureListener {
        C09334() {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] textureVuew SizeChanged");
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] textureview Destroyed");
            return false;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] onSurfaceTextureAvailable");
            GalleryManagerBase.this.mVideoSurfaceTexture = surface;
            GalleryManagerBase.this.mVideoPlayerTextureView.setVisibility(4);
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$5 */
    class C09345 implements OnClickListener {
        C09345() {
        }

        public void onClick(View arg0) {
            if (GalleryManagerBase.this.mSquareSnapState != 0) {
                CamLog.m3d(CameraConstants.TAG, "[Cell] Video player textureview clicked");
                if (GalleryManagerBase.this.mSquareSnapState == 1) {
                    GalleryManagerBase.this.mGet.getHandler().removeMessages(94);
                    GalleryManagerBase.this.mGet.getHandler().sendEmptyMessageDelayed(93, CameraConstants.TOAST_LENGTH_SHORT);
                    GalleryManagerBase.this.setSquareSnapshotState(2);
                    GalleryManagerBase.this.mPlayButton.setImageResource(C0088R.drawable.selector_snapshot_video_play);
                    GalleryManagerBase.this.pauseAndResumeVideoPlay(true);
                    GalleryManagerBase.this.showGalleryControlUI(true, 1, false);
                    ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).setPauseBitmap(GalleryManagerBase.this.mVideoPlayerTextureView.getBitmap());
                    GalleryManagerBase.this.mGalleryPagerAdapter.notifyDataSetChanged();
                    GalleryManagerBase.this.mVideoPlayerTextureView.setVisibility(4);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$6 */
    class C09356 implements OnClickListener {
        C09356() {
        }

        public void onClick(View arg0) {
            boolean isPlaying;
            if (GalleryManagerBase.this.mSquareSnapState == 1 || GalleryManagerBase.this.mSquareSnapState == 3) {
                isPlaying = true;
            } else {
                isPlaying = false;
            }
            if (GalleryManagerBase.this.mIsMuteVideo) {
                if (isPlaying && GalleryManagerBase.this.mMediaPlayer != null) {
                    GalleryManagerBase.this.mMediaPlayer.setVolume(1.0f);
                }
                GalleryManagerBase.this.mMuteButton.setImageResource(C0088R.drawable.btn_square_snap_sound_normal);
                GalleryManagerBase.this.mMuteButton.setContentDescription(GalleryManagerBase.this.getAppContext().getString(C0088R.string.switch_to_volume_off));
                GalleryManagerBase.this.mIsMuteVideo = false;
                return;
            }
            if (isPlaying && GalleryManagerBase.this.mMediaPlayer != null) {
                GalleryManagerBase.this.mMediaPlayer.setVolume(0.0f);
            }
            GalleryManagerBase.this.mMuteButton.setImageResource(C0088R.drawable.btn_square_snap_sound_mute);
            GalleryManagerBase.this.mMuteButton.setContentDescription(GalleryManagerBase.this.getAppContext().getString(C0088R.string.switch_to_volume_on));
            GalleryManagerBase.this.mIsMuteVideo = true;
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$7 */
    class C09367 implements OnClickListener {
        C09367() {
        }

        public void onClick(View arg0) {
            if (!GalleryManagerBase.this.mGet.checkInterval(5) || !GalleryManagerBase.this.mGet.checkModuleValidate(48) || GalleryManagerBase.this.mSquareSnapshotAnimationFrontView.getVisibility() == 0 || GalleryManagerBase.this.mIsNewItemAdding || GalleryManagerBase.this.mIsDeleting || GalleryManagerBase.this.mIsUndoing || GalleryManagerBase.this.mListener == null || !GalleryManagerBase.this.mListener.isDeleteAvailable() || GalleryManagerBase.this.mCaptureDataQueue.size() != 0) {
                CamLog.m7i(CameraConstants.TAG, "[Cell] mSquareSnapshotAnimationFrontView.getVisibility() : " + GalleryManagerBase.this.mSquareSnapshotAnimationFrontView.getVisibility() + ", mIsNewItemAdding : " + GalleryManagerBase.this.mIsNewItemAdding);
                CamLog.m7i(CameraConstants.TAG, "[Cell] mIsDeleting : " + GalleryManagerBase.this.mIsDeleting + ", mIsUndoing : " + GalleryManagerBase.this.mIsUndoing + ", mCaptureDataQueue.size() : " + GalleryManagerBase.this.mCaptureDataQueue.size());
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "[Cell] onDeleteButton Clicked - mCurrentIndex : " + GalleryManagerBase.this.mCurrentIndex);
            if (!GalleryManagerBase.this.isOverlapSampleUri()) {
                if (GalleryManagerBase.this.mGalleryItemList == null || GalleryManagerBase.this.mGalleryItemList.size() <= 0 || !GalleryManagerBase.this.mGet.checkModuleValidate(192) || GalleryManagerBase.this.mSquareSnapState == 1 || GalleryManagerBase.this.mSquareSnapState == 2) {
                    CamLog.m3d(CameraConstants.TAG, "[Cell] Can not delete item");
                    return;
                }
                GalleryManagerBase.this.clearUndoItem(true);
                GalleryManagerBase.this.mReadyToDeleteItem = new SquareSnapGalleryItem((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex));
                if (GalleryManagerBase.this.mReadyToDeleteItem == null) {
                    CamLog.m5e(CameraConstants.TAG, "[Cell] mReadyToDeleteItem is null");
                    return;
                }
                if (GalleryManagerBase.this.mSquareSnapState == 3 || GalleryManagerBase.this.mSquareSnapState == 1) {
                    GalleryManagerBase.this.stopVideoPlay();
                }
                boolean isCnasImage = FileUtil.isCNasContents(GalleryManagerBase.this.mReadyToDeleteItem.mUri, GalleryManagerBase.this.getAppContext());
                GalleryManagerBase.this.mListener.onDeleteButtonClicked();
                GalleryManagerBase.this.mDeleteIndex = GalleryManagerBase.this.mCurrentIndex;
                int nextPosition = GalleryManagerBase.this.mDeleteIndex + 1;
                if (nextPosition >= GalleryManagerBase.this.mGalleryItemList.size()) {
                    nextPosition = GalleryManagerBase.this.mGalleryItemList.size() - 1;
                }
                if (nextPosition < 0) {
                    nextPosition = 0;
                }
                GalleryManagerBase.this.makeDeleteAnimationBitmap(nextPosition);
                if (!GalleryManagerBase.this.deleteGalleryItem(isCnasImage, null)) {
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$8 */
    class C09378 implements AnimationListener {
        C09378() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (GalleryManagerBase.this.mGalleryItemList != null && GalleryManagerBase.this.mGalleryViewPager != null && GalleryManagerBase.this.mListener != null) {
                if (GalleryManagerBase.this.mGalleryItemList.size() == 0 || GalleryManagerBase.this.isOverlapSampleUri()) {
                    GalleryManagerBase.this.showGalleryControlUI(false, 0, false);
                    if (GalleryManagerBase.this.mMainAccessView != null && CameraConstants.MODE_SQUARE_SNAPSHOT.equals(GalleryManagerBase.this.mGet.getShotMode())) {
                        GalleryManagerBase.this.showMainAccesView(true, false);
                        GalleryManagerBase.this.mMainAccessView.rotateLayout(GalleryManagerBase.this.getOrientationDegree());
                        GalleryManagerBase.this.setMainAccessViewLayout(GalleryManagerBase.this.getOrientationDegree());
                    }
                    GalleryManagerBase.this.mListener.onGalleryPageChanged(null, false, true);
                } else {
                    GalleryManagerBase.this.showGalleryControlUI(true, ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mType, false);
                    GalleryManagerBase.this.mListener.onGalleryPageChanged(((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mUri, false, true);
                }
                GalleryManagerBase.this.mGalleryViewPager.setEnableScroll(true);
                GalleryManagerBase.this.mIsDeleting = false;
                if (!(GalleryManagerBase.this.mThreadHandler == null || GalleryManagerBase.this.mGalleryItemList.size() == 0 || ((SquareSnapGalleryItem) GalleryManagerBase.this.mGalleryItemList.get(GalleryManagerBase.this.mCurrentIndex)).mType == 1)) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = GalleryManagerBase.this.mCurrentIndex;
                    msg.arg2 = GalleryManagerBase.this.mGalleryItemList.size();
                    GalleryManagerBase.this.mThreadHandler.removeMessages(1);
                    GalleryManagerBase.this.mThreadHandler.sendMessage(msg);
                }
                GalleryManagerBase.this.removeAnimationViews();
            }
        }
    }

    /* renamed from: com.lge.camera.managers.GalleryManagerBase$9 */
    class C09389 implements OnInfoListener {
        C09389() {
        }

        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] onInfo, what : " + what + ", extra : " + extra);
            if (mp != null && what == 3) {
                GalleryManagerBase.this.mGet.postOnUiThread(new HandlerRunnable(GalleryManagerBase.this) {
                    public void handleRun() {
                        GalleryManagerBase.this.mVideoPlayerTextureView.setVisibility(0);
                    }
                }, 50);
            }
            return false;
        }
    }

    public GalleryManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onPageScrollStateChanged(int state) {
        if (this.mCurrentIndex != this.mGalleryItemList.size() - 1 && this.mCurrentIndex != 0) {
            if ((state == 1 || state == 2) && this.mThreadHandler != null) {
                this.mThreadHandler.removeMessages(1);
            }
        }
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int curPosition) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] onPageSelected : " + curPosition + ", mIsNewItemAdding : " + this.mIsNewItemAdding + ", mIsDeleting : " + this.mIsDeleting);
        if (this.mGalleryItemList != null && this.mListener != null) {
            if (this.mSquareSnapState == 3 || this.mSquareSnapState == 1) {
                stopVideoPlay();
            }
            this.mPreviousIndex = this.mCurrentIndex;
            this.mCurrentIndex = curPosition;
            showGalleryControlUI(true, ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mType, false);
            this.mListener.onGalleryPageChanged(((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri, this.mIsNewItemAdding, this.mIsDeleting);
            if (!(this.mIsDeleting || ((SquareSnapGalleryItem) this.mGalleryItemList.get(0)).mType == -1 || ((SquareSnapGalleryItem) this.mGalleryItemList.get(curPosition)).mType == 1)) {
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = curPosition;
                msg.arg2 = this.mGalleryItemList.size();
                msg.obj = ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri;
                ((SquareSnapGalleryItem) this.mGalleryItemList.get(curPosition)).mBitmapState = 0;
                if (this.mThreadHandler != null) {
                    this.mThreadHandler.removeMessages(1);
                    this.mThreadHandler.sendMessage(msg);
                }
            }
            if (this.mGalleryItemList.size() != 0 && ((SquareSnapGalleryItem) this.mGalleryItemList.get(0)).mType == -1) {
                this.mGalleryViewPager.setEnableScroll(false);
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (GalleryManagerBase.this.mGalleryItemList != null && GalleryManagerBase.this.mGalleryPagerAdapter != null && GalleryManagerBase.this.mGalleryViewPager != null && GalleryManagerBase.this.mGalleryItemList.size() != 0) {
                            CamLog.m3d(CameraConstants.TAG, "[Cell] Remove main access guide");
                            GalleryManagerBase.this.mGalleryItemList.remove(0);
                            GalleryManagerBase.this.mGalleryPagerAdapter.notifyDataSetChanged();
                            GalleryManagerBase.this.mGalleryViewPager.setCurrentItem(0, false);
                            GalleryManagerBase.this.mGalleryViewPager.setPageMarginDrawable((int) C0088R.color.black);
                            GalleryManagerBase.this.mGalleryViewPager.setEnableScroll(true);
                            GalleryManagerBase.this.mPreviousIndex = GalleryManagerBase.this.mCurrentIndex;
                            GalleryManagerBase.this.mCurrentIndex = 0;
                            GalleryManagerBase.this.showMainAccesView(false, false);
                        }
                    }
                }, 300);
            }
        }
    }

    protected void setupGalleryViewPager() {
        this.mGalleryViewPager = (GalleryViewPager) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_view_pager);
        this.mGalleryItemList = new ArrayList();
        this.mGalleryPagerAdapter = new GalleryPagerAdapter(getAppContext(), this.mGalleryItemList, this.mGalleryPagerListenr);
        this.mGalleryViewPager.setPageMargin(50);
        this.mGalleryViewPager.setOffscreenPageLimit(1);
        if (CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode())) {
            this.mGalleryViewPager.setBackgroundColor(0);
            this.mGalleryViewPager.setPageMarginDrawable(17170445);
        } else {
            this.mGalleryViewPager.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
            this.mGalleryViewPager.setPageMarginDrawable((int) C0088R.color.black);
        }
        this.mGalleryViewPager.setAdapter(this.mGalleryPagerAdapter);
        this.mGalleryViewPager.setOnPageChangeListener(this);
        this.mGalleryViewPager.setGalleryViewPagerListener(this);
    }

    protected void setupGalleryPlayerTextureView(int height) {
        this.mGalleryPlayButtonLayout = (RotateLayout) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_play_rotate_layout);
        this.mVideoPlayerTextureView = (TextureView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_video_player);
        this.mVideoPlayerTextureView.setSurfaceTextureListener(new C09334());
        this.mVideoPlayerTextureView.setVisibility(!CameraConstants.MODE_SQUARE_OVERLAP.equals(this.mGet.getShotMode()) ? 0 : 8);
        this.mVideoPlayerTextureView.setOnClickListener(new C09345());
        this.mVideoPlayerTextureView.setSoundEffectsEnabled(false);
    }

    protected void setupGalleryControlButtons(int height) {
        this.mMuteButton = (RotateImageButton) this.mGalleryLayout.findViewById(C0088R.id.video_control_mute_button);
        this.mMuteButton.setOnClickListener(this.mMuteButtonClickListener);
        this.mDeleteButton = (RotateImageButton) this.mGalleryLayout.findViewById(C0088R.id.video_control_delete_button);
        this.mDeleteButton.setOnClickListener(this.mDeleteButtonClickListener);
        int endMargin = Utils.getPx(getAppContext(), C0088R.dimen.quick_button_margin);
        LayoutParams rlp = (LayoutParams) this.mDeleteButton.getLayoutParams();
        rlp.setMarginEnd(endMargin);
        this.mDeleteButton.setLayoutParams(rlp);
        this.mDeleteButton.setVisibility(0);
        this.mBurstShotMark = (RotateImageView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_control_burst_play);
        rlp = (LayoutParams) this.mBurstShotMark.getLayoutParams();
        rlp.setMarginStart(endMargin);
        this.mBurstShotMark.setLayoutParams(rlp);
    }

    private boolean deleteGalleryItem(boolean isCnasImage, String burstId) {
        if (this.mReadyToDeleteItem != null) {
            if (this.mReadyToDeleteItem.mType != 2) {
                if (isCnasImage) {
                    this.mGet.deleteImmediatelyNotUndo();
                    int resId = FileManager.deleteFile(getAppContext(), this.mReadyToDeleteItem.mUri);
                    if (resId != 0) {
                        this.mGet.showToast(this.mGet.getAppContext().getString(resId), CameraConstants.TOAST_LENGTH_SHORT);
                        clearUndoItem(true);
                        return false;
                    }
                    this.mGet.showToast(this.mGet.getAppContext().getString(C0088R.string.popup_delete_done), CameraConstants.TOAST_LENGTH_SHORT);
                }
                doDeleteItemOnList(this.mReadyToDeleteItem);
                if (!isCnasImage) {
                    deleteOrUndo(this.mReadyToDeleteItem.mUri, burstId);
                }
            } else {
                String fileName = FileUtil.getFileNameFromURI(getAppContext(), this.mReadyToDeleteItem.mUri);
                if (fileName == null) {
                    return false;
                }
                deleteOrUndo(this.mReadyToDeleteItem.mUri, fileName.split("_B")[0]);
            }
        }
        return true;
    }

    private void makeDeleteAnimationBitmap(int nextPosition) {
        if (this.mSquareSnapshotAnimationFrontView != null && this.mSquareSnapshotAnimationRearView != null) {
            Bitmap deleteImgBmp = getThumbnailBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mDeleteIndex)).mUri, ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mDeleteIndex)).mType, true);
            if (!(deleteImgBmp == null || deleteImgBmp.isRecycled())) {
                this.mSquareSnapshotAnimationFrontView.setImageBitmap(deleteImgBmp);
            }
            Bitmap nextImgBmp;
            if (this.mGalleryItemList.size() == 1) {
                captureMainAccessView(this.mSquareSnapshotAnimationRearView);
            } else if (this.mDeleteIndex == this.mGalleryItemList.size() - 1) {
                nextImgBmp = getThumbnailBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mDeleteIndex - 1)).mUri, ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mDeleteIndex - 1)).mType, true);
                if (!(nextImgBmp == null || nextImgBmp.isRecycled())) {
                    this.mSquareSnapshotAnimationRearView.setImageBitmap(nextImgBmp);
                }
                this.mIsDelAnimDirectionRtoL = false;
            } else {
                nextImgBmp = getThumbnailBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(nextPosition)).mUri, ((SquareSnapGalleryItem) this.mGalleryItemList.get(nextPosition)).mType, true);
                if (!(nextImgBmp == null || nextImgBmp.isRecycled())) {
                    this.mSquareSnapshotAnimationRearView.setImageBitmap(nextImgBmp);
                }
                this.mIsDelAnimDirectionRtoL = true;
            }
        }
    }

    protected void doDeleteItemOnList(SquareSnapGalleryItem deleteItem) {
        if (deleteItem != null) {
            boolean isBurstType;
            if (deleteItem.mType == 30) {
                isBurstType = true;
            } else {
                isBurstType = false;
            }
            if (this.mDeleteIndex < this.mGalleryItemList.size()) {
                this.mIsDeleting = true;
                this.mGalleryViewPager.setEnableScroll(false);
                this.mGalleryItemList.remove(this.mDeleteIndex);
                if (isBurstType) {
                    clearUndoItem(true);
                }
                CamLog.m3d(CameraConstants.TAG, "[Cell] Delete item : " + this.mGalleryItemList.size());
                this.mCurrentIndex = SquareUtil.checkIndexBoundary(this.mDeleteIndex, this.mGalleryItemList.size());
                this.mGalleryPagerAdapter.notifyDataSetChanged();
                this.mGalleryViewPager.setCurrentItem(this.mDeleteIndex, false);
                startDeleteAnimation(new C09378());
            }
        }
    }

    private void startDeleteAnimation(AnimationListener animListener) {
        if (this.mIsDelAnimDirectionRtoL) {
            AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationRearView, getAppContext(), getOrientationDegree(), false, 1.0f, null);
            AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationFrontView, getAppContext(), getOrientationDegree(), true, 1.0f, animListener);
            return;
        }
        AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationRearView, getAppContext(), getOrientationDegree(), false, -1.0f, null);
        AnimationUtil.startSnapshotDeleteOrUndoAnimation(this.mSquareSnapshotAnimationFrontView, getAppContext(), getOrientationDegree(), true, -1.0f, animListener);
    }

    protected void setupGalleryExtraViews(int height) {
        this.mControlButtonLayout = this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_control_layout);
        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) this.mControlButtonLayout.getLayoutParams();
        flp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.056f);
        this.mControlButtonLayout.setLayoutParams(flp);
        this.mPlayButton = (ImageButton) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_play_button);
        this.mPlayButton.setVisibility(4);
        this.mPlayButton.setOnClickListener(this.mPlayButtonClickListener);
        setRotatePlayButton(getOrientationDegree());
        this.mTouchBlockCoverView = this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_video_black_cover);
        this.mSquareSnapshotAnimationFrontView = (ImageView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_capture_animation_view);
        this.mSquareSnapshotAnimationFrontView.setVisibility(4);
        this.mSquareSnapshotAnimationRearView = (ImageView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_capture_animation_previous_view);
        this.mSquareSnapshotAnimationRearView.setVisibility(4);
    }

    protected void setRotatePlayButton(int degree) {
        LayoutParams rl = (LayoutParams) this.mPlayButton.getLayoutParams();
        int topMargin = (int) Utils.dpToPx(getAppContext(), 115.0f);
        Utils.resetLayoutParameter(rl);
        if (degree == 0) {
            rl.addRule(14);
            rl.topMargin = topMargin;
        } else if (degree == 90) {
            rl.addRule(15);
            rl.addRule(21);
            rl.setMarginEnd(topMargin);
        } else if (degree == 180) {
            rl.addRule(14);
            rl.addRule(12);
            rl.bottomMargin = topMargin;
        } else {
            rl.addRule(15);
            rl.setMarginStart(topMargin);
        }
    }

    public void stopVideoPlay() {
        if (this.mMediaPlayer != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] stopVideoPlay");
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
            this.mMediaPlayer.setOnPreparedListener(null);
            this.mMediaPlayer.setOnCompletionListener(null);
            this.mMediaPlayer.setOnInfoListener(null);
            this.mSquareSnapState = 0;
            setSquareSnapshotState(0);
            this.mVideoPlayerTextureView.setVisibility(4);
            if (!(this.mGalleryItemList == null || this.mGalleryItemList.size() == 0 || this.mCurrentIndex >= this.mGalleryItemList.size())) {
                ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).unsetPauseBitmap();
            }
            this.mGalleryPagerAdapter.notifyDataSetChanged();
        }
    }

    protected void pauseAndResumeVideoPlay(boolean pause) {
        if (this.mMediaPlayer == null) {
            return;
        }
        if (pause) {
            this.mMediaPlayer.pause();
            if (!"on".equals(this.mGet.getSettingValue(Setting.KEY_VOICESHUTTER))) {
                AudioUtil.setAudioFocus(getAppContext(), false);
                return;
            }
            return;
        }
        AudioUtil.setAudioFocus(getAppContext(), true);
        this.mMediaPlayer.start();
    }

    public void showGalleryControlUI(boolean show, int type, boolean isPlaying) {
        int i = 4;
        if (this.mControlButtonLayout != null && this.mPlayButton != null && this.mDeleteButton != null && this.mMuteButton != null && this.mBurstShotMark != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] showGalleryControlUI, show : " + show + ", type : " + type + ", mIsPlaying : " + isPlaying);
            if (show) {
                this.mControlButtonLayout.setVisibility(0);
                if (type == 1) {
                    this.mPlayButton.setVisibility(0);
                    if (isPlaying) {
                        this.mDeleteButton.setVisibility(4);
                    } else {
                        this.mDeleteButton.setVisibility(0);
                    }
                    this.mMuteButton.setVisibility(0);
                    this.mBurstShotMark.setVisibility(8);
                    return;
                }
                this.mPlayButton.setVisibility(8);
                RotateImageButton rotateImageButton = this.mDeleteButton;
                if (!isOverlapSampleUri()) {
                    i = 0;
                }
                rotateImageButton.setVisibility(i);
                this.mMuteButton.setVisibility(8);
                if (type == 2) {
                    this.mBurstShotMark.setImageResource(C0088R.drawable.btn_square_snap_burst_play);
                    this.mBurstShotMark.setVisibility(0);
                    this.mBurstShotMark.setClickable(true);
                    this.mBurstShotMark.setOnClickListener(null);
                    this.mBurstShotMark.setOnClickListener(this.mBurstPlayButtonClilckListener);
                } else if (type == 3) {
                    this.mBurstShotMark.setImageResource(C0088R.drawable.camera_quick_clip_badge_gif);
                    this.mBurstShotMark.setVisibility(0);
                    this.mBurstShotMark.setClickable(false);
                    this.mBurstShotMark.setOnClickListener(null);
                } else {
                    this.mBurstShotMark.setVisibility(8);
                    this.mBurstShotMark.setClickable(false);
                    this.mBurstShotMark.setOnClickListener(null);
                }
            } else if (type == 1 && !isPlaying) {
                this.mPlayButton.setVisibility(0);
                this.mControlButtonLayout.setVisibility(0);
                this.mDeleteButton.setVisibility(8);
                this.mMuteButton.setVisibility(8);
                this.mBurstShotMark.setVisibility(8);
            } else if (type == 2) {
                this.mPlayButton.setVisibility(8);
                this.mControlButtonLayout.setVisibility(0);
                this.mDeleteButton.setVisibility(4);
                this.mMuteButton.setVisibility(4);
                this.mBurstShotMark.setVisibility(0);
            } else {
                this.mPlayButton.setVisibility(8);
                this.mControlButtonLayout.setVisibility(8);
            }
        }
    }

    public boolean isOverlapSampleUri() {
        boolean result = false;
        if (this.mGalleryItemList.size() > 0) {
            Uri uri = ((SquareSnapGalleryItem) this.mGalleryItemList.get(this.mCurrentIndex)).mUri;
            if (uri == null) {
                return false;
            }
            result = uri.toString().contains(OverlapProjectDbAdapter.URI_OVERLAP);
        }
        return result;
    }

    protected void setMainAccessViewLayout(int degree) {
        ImageView icon = (ImageView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_main_access_icon);
        TextView text = (TextView) this.mGalleryLayout.findViewById(C0088R.id.snap_gallery_main_access_text);
        text.setTextSize(0, (float) RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, 0.0333f));
        LayoutParams iconRlp = (LayoutParams) icon.getLayoutParams();
        LayoutParams textRlp = (LayoutParams) text.getLayoutParams();
        LayoutParams cueRlp = (LayoutParams) this.mMainAccessViewImageCue.getLayoutParams();
        int iconTopMarginPort = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.10139f);
        int iconTopMarginLand = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.10139f);
        int textTopMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, MAIN_ACCESS_GUIDE_TEXT_TOP_MARGIN);
        Utils.resetLayoutParameter(cueRlp);
        Utils.resetLayoutParameter(iconRlp);
        Utils.resetLayoutParameter(textRlp);
        cueRlp.addRule(15);
        if (Utils.isRTLLanguage()) {
            this.mMainAccessViewImageCue.setDegree(180, false);
        } else {
            this.mMainAccessViewImageCue.setDegree(0, false);
        }
        setMainAccessViewLayoutForVertical(degree, icon, iconRlp, text, textRlp, iconTopMarginPort, textTopMargin, cueRlp);
        setMainAccessViewLayoutForHorizontal(degree, icon, iconRlp, text, textRlp, iconTopMarginLand, textTopMargin, cueRlp);
        textRlp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_TEXT_WIDTH);
        iconRlp.width = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_ICON_SIZE);
        iconRlp.height = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_ICON_SIZE);
        icon.setLayoutParams(iconRlp);
        text.setLayoutParams(textRlp);
    }

    private void setMainAccessViewLayoutForVertical(int degree, ImageView icon, LayoutParams iconRlp, TextView text, LayoutParams textRlp, int iconMarginTop, int textMarginTop, LayoutParams cueRlp) {
        if (Utils.isRTLLanguage()) {
            cueRlp.addRule(20);
            cueRlp.setMarginStart(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_cue));
        } else {
            cueRlp.addRule(21);
            cueRlp.setMarginEnd(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_cue));
        }
        if (degree == 0) {
            iconRlp.addRule(14);
            iconRlp.setMarginsRelative(0, iconMarginTop, 0, 0);
            textRlp.addRule(14);
            textRlp.addRule(3, C0088R.id.snap_gallery_main_access_icon);
            textRlp.setMarginsRelative(0, textMarginTop, 0, 0);
        } else if (degree == 180) {
            iconRlp.addRule(14);
            iconRlp.addRule(12);
            iconRlp.setMarginsRelative(0, 0, 0, iconMarginTop);
            textRlp.addRule(14);
            textRlp.addRule(12);
            if (text.getMeasuredHeight() == 0) {
                text.measure(0, 0);
            }
            textRlp.setMarginsRelative(0, 0, 0, (iconMarginTop - textMarginTop) - text.getMeasuredHeight());
        }
    }

    private void setMainAccessViewLayoutForHorizontal(int degree, ImageView icon, LayoutParams iconRlp, TextView text, LayoutParams textRlp, int iconMarginTop, int textMarginTop, LayoutParams cueRlp) {
        if (Utils.isRTLLanguage()) {
            cueRlp.addRule(20);
        } else {
            cueRlp.addRule(21);
        }
        if (degree == 270) {
            if (Utils.isRTLLanguage()) {
                cueRlp.setMarginStart(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_cue));
            } else {
                cueRlp.setMarginEnd(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_que_land));
            }
            iconRlp.addRule(15);
            iconRlp.topMargin = 0;
            iconRlp.setMarginsRelative(iconMarginTop, 0, 0, 0);
            textRlp.addRule(15);
            textRlp.addRule(3, C0088R.id.snap_gallery_main_access_icon);
            textRlp.setMarginsRelative(((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_ICON_SIZE) / 2) + iconMarginTop) - (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_TEXT_WIDTH) / 2), textMarginTop, 0, 0);
        } else if (degree == 90) {
            if (Utils.isRTLLanguage()) {
                cueRlp.setMarginStart(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_que_land));
            } else {
                cueRlp.setMarginEnd(Utils.getPx(getAppContext(), C0088R.dimen.square_snap_shot_main_access_cue));
            }
            iconRlp.addRule(15);
            iconRlp.addRule(21);
            iconRlp.topMargin = 0;
            iconRlp.setMarginEnd(iconMarginTop);
            iconRlp.setMarginsRelative(0, 0, iconMarginTop, 0);
            textRlp.addRule(15);
            textRlp.addRule(21);
            textRlp.addRule(3, C0088R.id.snap_gallery_main_access_icon);
            textRlp.setMarginsRelative(0, textMarginTop, ((RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_ICON_SIZE) / 2) + iconMarginTop) - (RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), false, MAIN_ACCESS_VIEW_TEXT_WIDTH) / 2), 0);
        }
    }

    public Bitmap getThumbnailBitmap(Uri uri, int type, boolean forAniView) {
        if (uri == null) {
            return null;
        }
        if (uri.toString().contains(OverlapProjectDbAdapter.URI_OVERLAP)) {
            int height = SquareUtil.getHeight(getAppContext());
            Bitmap sampleBitmap = SquareUtil.getOverlapSampleBitmap(this.mGet.getAppContext(), uri, height, height);
            return forAniView ? BitmapManagingUtil.getRotatedImage(sampleBitmap, SquareUtil.getThumbnailDegree(0, getOrientationDegree(), forAniView), false) : sampleBitmap;
        } else {
            String filePath = FileUtil.getRealPathFromURI(this.mGet.getAppContext(), uri);
            Bitmap bmp;
            if (filePath == null) {
                CamLog.m3d(CameraConstants.TAG, "[Cell] filePath is null");
                return null;
            } else if (type == 0 || type == 2) {
                ExifInterface exif = Exif.readExif(filePath);
                int degree = SquareUtil.getThumbnailDegree(Exif.getOrientation(exif), getOrientationDegree(), forAniView);
                if (exif.getThumbnailBitmap() != null) {
                    return BitmapManagingUtil.getRotatedImage(exif.getThumbnailBitmap(), degree, false);
                }
                bmp = BitmapManagingUtil.getThumbnailFromUri(getActivity(), uri, 1);
                if (forAniView) {
                    return BitmapManagingUtil.getRotatedImage(bmp, degree, false);
                }
                return bmp;
            } else {
                bmp = BitmapManagingUtil.getThumbnailFromUri(getActivity(), uri, 1);
                if (forAniView) {
                    return BitmapManagingUtil.getRotatedImage(bmp, SquareUtil.getThumbnailDegree(0, getOrientationDegree(), forAniView), false);
                }
                return bmp;
            }
        }
    }

    public void setSquareSnapshotState(int state) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] Square snap shot state : " + state);
        this.mSquareSnapState = state;
    }

    public void showMainAccesView(boolean show, boolean showCue) {
        if (this.mMainAccessView != null && this.mMainAccessViewImageCue != null && CameraConstants.MODE_SQUARE_SNAPSHOT.equals(this.mGet.getShotMode())) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] showMainAccesView : " + show + ", showQue : " + showCue);
            if (show) {
                this.mMainAccessView.setVisibility(0);
                if (showCue) {
                    this.mMainAccessViewImageCue.setVisibility(0);
                } else {
                    this.mMainAccessViewImageCue.setVisibility(4);
                }
            } else if (this.mMainAccessView.getVisibility() == 0) {
                this.mMainAccessView.setVisibility(4);
            }
        }
    }

    protected void clearUndoItem(boolean forced) {
        if ((forced || !this.mGet.checkUndoCurrentState(2)) && this.mReadyToDeleteItem != null) {
            CamLog.m3d(CameraConstants.TAG, "[Cell] clearUndoItem");
            this.mReadyToDeleteItem.unsetPauseBitmap();
            this.mReadyToDeleteItem.unbind();
            this.mReadyToDeleteItem = null;
        }
    }

    public void onChangeViewPagerTouchState(boolean isTouched) {
        if (this.mListener != null) {
            this.mListener.onGalleryViewTouched(isTouched);
        }
    }

    public void captureMainAccessView(ImageView animationView) {
        if (this.mMainAccessView != null) {
            this.mMainAccessView.setDrawingCacheEnabled(true);
            Bitmap bitmap = this.mMainAccessView.getDrawingCache();
            if (!(bitmap == null || bitmap.isRecycled())) {
                animationView.setImageBitmap(Bitmap.createBitmap(bitmap));
            }
            this.mMainAccessView.setDrawingCacheEnabled(false);
        }
    }

    public void deleteOrUndo(Uri uri, String burstId) {
    }

    public void removeAnimationViews() {
        if (this.mSquareSnapshotAnimationRearView != null) {
            this.mSquareSnapshotAnimationRearView.setVisibility(4);
        }
        if (this.mSquareSnapshotAnimationFrontView != null) {
            this.mSquareSnapshotAnimationFrontView.setVisibility(4);
        }
    }
}
