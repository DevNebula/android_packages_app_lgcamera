package com.lge.camera.managers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.p000v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.app.ThumbnailAdapter;
import com.lge.camera.app.ThumbnailHelper;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.components.TouchImageViewInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.FileUtil;
import com.lge.camera.util.Utils;
import java.lang.ref.WeakReference;

public class ThumbnailListPagerAdapter extends PagerAdapter {
    protected WeakReference<Activity> mActivity = null;
    protected ThumbnailAdapter mAdapter;
    protected String mBucketIdSelection = null;
    protected WeakReference<Context> mContext = null;
    protected boolean mIsRTL = false;
    protected ThumbnailListPagerListener mListener;
    protected String mSecureTime = null;
    protected final ThumbnailHelper mThumbnailHelper;
    protected SparseArray<TouchImageView> mViewArray = new SparseArray();

    public interface ThumbnailListPagerListener {
        void onClicked();
    }

    /* renamed from: com.lge.camera.managers.ThumbnailListPagerAdapter$2 */
    class C03472 implements TouchImageViewInterface {
        C03472() {
        }

        public boolean isSystemUIVisible() {
            return false;
        }

        public void onClicked() {
            ThumbnailListPagerAdapter.this.mListener.onClicked();
        }

        public void onTouchStateChanged(boolean isTouchDown) {
        }

        public void onZoomScaleStart() {
        }
    }

    public ThumbnailListPagerAdapter(Context context, Activity activity, ThumbnailListPagerListener listener, ThumbnailAdapter adapter, ThumbnailHelper mThumbnailHelper) {
        this.mContext = new WeakReference(context);
        this.mActivity = new WeakReference(activity);
        this.mListener = listener;
        this.mAdapter = adapter;
        this.mThumbnailHelper = mThumbnailHelper;
        this.mIsRTL = Utils.isRTLLanguage();
    }

    public int getCount() {
        if (this.mAdapter == null || this.mAdapter.getCursor() == null || this.mAdapter.getCursor().isClosed()) {
            return 0;
        }
        return this.mAdapter.getCursor().getCount();
    }

    protected ThumbnailListItem getItem(int position) {
        return ThumbnailListItem.fromCursor((Cursor) this.mAdapter.getItem(position));
    }

    protected boolean checkAdapter(int position) {
        if (this.mAdapter == null || this.mAdapter.getCursor() == null || this.mAdapter.getCursor().getCount() < position + 1) {
            return false;
        }
        return true;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from((Context) this.mContext.get()).inflate(C0088R.layout.thumbnail_pager_item, null);
        if (view == null) {
            return null;
        }
        int data_index = position;
        if (this.mIsRTL) {
            data_index = (getCount() - position) - 1;
        }
        CamLog.m3d(CameraConstants.TAG, "[Tile] data_index : " + data_index);
        TouchImageView imageView = (TouchImageView) view.findViewById(C0088R.id.thumbnail_pager_image_view);
        ImageView playIcon = (ImageView) view.findViewById(C0088R.id.thumbnail_pager_play_icon);
        final ThumbnailListItem item = getItem(data_index);
        if (imageView == null || playIcon == null || item == null || !checkAdapter(data_index)) {
            return view;
        }
        int visibility;
        if (item.mIsImage) {
            visibility = 8;
        } else {
            visibility = 0;
        }
        if (!item.mIsImage) {
            imageView.setOnTouchListener(null);
            imageView.setOnClickListener(null);
            imageView.setOnTouchImageViewListener(null);
            imageView.setOnDoubleTapListener(null);
        }
        playIcon.setVisibility(visibility);
        playIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ThumbnailListPagerAdapter.this.exitQslideVideo();
                ThumbnailListPagerAdapter.this.playVideo(item.mUri);
            }
        });
        this.mThumbnailHelper.stopLoading(imageView);
        this.mThumbnailHelper.loadThumbnail(item, imageView, 70);
        if (item.mIsImage) {
            imageView.setContentDescription(String.format(((Context) this.mContext.get()).getString(C0088R.string.talkback_cell_image_selected), new Object[]{Integer.valueOf(position + 1)}));
        } else {
            imageView.setContentDescription(String.format(((Context) this.mContext.get()).getString(C0088R.string.talkback_cell_video_selected), new Object[]{Integer.valueOf(position + 1)}));
        }
        setTouchImageViewInterface(imageView);
        container.addView(view);
        this.mViewArray.put(position, imageView);
        return view;
    }

    private void setTouchImageViewInterface(TouchImageView imageView) {
        imageView.setTouchImageViewInterface(new C03472());
    }

    public void setDegree(int degree) {
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (container != null) {
            ImageView imageView = (ImageView) container.findViewById(C0088R.id.thumbnail_pager_image_view);
            if (imageView != null) {
                Bitmap bmp = imageView.getDrawingCache();
                if (bmp != null) {
                    bmp.recycle();
                }
            }
            this.mViewArray.remove(position);
            container.removeView((View) object);
        }
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public TouchImageView getSpecficView(int position) {
        if (this.mViewArray == null) {
            return null;
        }
        return (TouchImageView) this.mViewArray.get(position);
    }

    public void tempUnbind() {
    }

    protected void exitQslideVideo() {
        CamLog.m3d(CameraConstants.TAG, "request exitQslideVideo");
        Intent intent = new Intent("com.lge.intent.action.REQUEST_QSLIDE_EXIT");
        intent.addFlags(16777216);
        ((Activity) this.mActivity.get()).sendBroadcast(intent);
    }

    public void setBucketId(String bucketId) {
        this.mBucketIdSelection = bucketId;
    }

    protected void playVideo(Uri uri) {
        CamLog.m3d(CameraConstants.TAG, "mPlayButtonListener clicked.");
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, MultimediaProperties.VIDEO_MIME_TYPE);
        intent.putExtra("mimeType", MultimediaProperties.VIDEO_MIME_TYPE);
        intent.putExtra("full_screen_only", true);
        intent.putExtra("QslideSupported", false);
        if (!(this.mBucketIdSelection == null || "".equals(this.mBucketIdSelection.trim()))) {
            intent.putExtra("bucket_ids", this.mBucketIdSelection);
            intent.putExtra("sort", "datetaken DESC,_id DESC");
        }
        if (this.mContext != null && FileUtil.isCNasContents(uri, (Context) this.mContext.get())) {
            intent.setPackage(CameraConstants.PACKAGE_VIDEOPLAYER);
        }
        try {
            AppControlUtilBase.setLaunchingVideo(true);
            ((Activity) this.mActivity.get()).startActivity(intent);
        } catch (ActivityNotFoundException e) {
            CamLog.m11w(CameraConstants.TAG, "ActivityNotFoundException");
        }
    }

    public void setSecureTime(String secureTime) {
        this.mSecureTime = secureTime;
    }
}
