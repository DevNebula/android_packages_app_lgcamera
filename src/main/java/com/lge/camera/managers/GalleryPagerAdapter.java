package com.lge.camera.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.p000v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lge.camera.C0088R;
import com.lge.camera.components.TouchImageView;
import com.lge.camera.components.TouchImageViewInterface;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GalleryPagerAdapter extends PagerAdapter {
    WeakReference<Context> mContext;
    private ArrayList<SquareSnapGalleryItem> mGalleryItemList;
    private LayoutInflater mInflater;
    private boolean mIsRTL = false;
    private int mLastCount;
    private GalleryPagerListener mListener;
    private SparseArray<TouchImageView> mViewArray = new SparseArray();

    public interface GalleryPagerListener {
        Bitmap getTempBitmap(Uri uri, int i);

        void onClicked();

        void onInstantiateItem(int i);

        void onTouchStateChanged(boolean z);

        void onZoomScaleStart();
    }

    /* renamed from: com.lge.camera.managers.GalleryPagerAdapter$1 */
    class C09411 implements TouchImageViewInterface {
        C09411() {
        }

        public boolean isSystemUIVisible() {
            return false;
        }

        public void onClicked() {
            GalleryPagerAdapter.this.mListener.onClicked();
        }

        public void onTouchStateChanged(boolean isTouchDown) {
            GalleryPagerAdapter.this.mListener.onTouchStateChanged(isTouchDown);
        }

        public void onZoomScaleStart() {
            GalleryPagerAdapter.this.mListener.onZoomScaleStart();
        }
    }

    public GalleryPagerAdapter(Context context, ArrayList<SquareSnapGalleryItem> itemList, GalleryPagerListener listener) {
        this.mContext = new WeakReference(context);
        this.mInflater = LayoutInflater.from(context);
        this.mGalleryItemList = itemList;
        this.mListener = listener;
        this.mIsRTL = Utils.isRTLLanguage();
    }

    public int getCount() {
        return this.mGalleryItemList.size();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        CamLog.m7i(CameraConstants.TAG, "[Cell] instantiateItem : " + position);
        int changePosition = reverse(position);
        View view = this.mInflater.inflate(C0088R.layout.snap_gallery_item_view, null);
        SquareSnapGalleryItem item = (SquareSnapGalleryItem) this.mGalleryItemList.get(changePosition);
        Bitmap bitmap = item.mThumbBitmap;
        int type = item.mType;
        if (type == -1) {
            view.findViewById(C0088R.id.snap_gallery_dummy_view).setVisibility(0);
            container.addView(view);
        } else {
            TouchImageView img = (TouchImageView) view.findViewById(C0088R.id.snap_gallery_imageview);
            img.setTouchImageViewInterface(new C09411());
            if (type == 1 && item.mPauseScreenBitmap != null) {
                img.setImageBitmap(item.mPauseScreenBitmap);
                CamLog.m7i(CameraConstants.TAG, "[Cell] pause screen bitmap is set");
            } else if (bitmap != null) {
                img.setImageBitmap(bitmap);
                CamLog.m7i(CameraConstants.TAG, "[Cell] normal bitmap is set");
            } else {
                img.setImageBitmap(this.mListener.getTempBitmap(((SquareSnapGalleryItem) this.mGalleryItemList.get(changePosition)).mUri, type));
                CamLog.m7i(CameraConstants.TAG, "[Cell] thumbnail bitmap is set");
            }
            if (type == 1) {
                img.setContentDescription(String.format(((Context) this.mContext.get()).getString(C0088R.string.talkback_cell_video_selected), new Object[]{Integer.valueOf(changePosition + 1)}));
            } else {
                img.setContentDescription(String.format(((Context) this.mContext.get()).getString(C0088R.string.talkback_cell_image_selected), new Object[]{Integer.valueOf(changePosition + 1)}));
            }
            container.addView(view);
            this.mViewArray.put(changePosition, img);
            this.mListener.onInstantiateItem(changePosition);
        }
        return view;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        CamLog.m3d(CameraConstants.TAG, "[Cell] destroyItem : " + position);
        int changePosition = reverse(position);
        if (container != null) {
            ImageView imageView = (ImageView) container.findViewById(C0088R.id.snap_gallery_imageview);
            if (imageView != null) {
                Bitmap bmp = imageView.getDrawingCache();
                if (!(bmp == null || bmp.isRecycled())) {
                    bmp.recycle();
                }
            }
            this.mViewArray.remove(changePosition);
            container.removeView((View) object);
        }
    }

    public TouchImageView getSpecficView(int position) {
        return (TouchImageView) this.mViewArray.get(position);
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    private int reverse(int position) {
        if (!this.mIsRTL) {
            return position;
        }
        int convertPosition = (getCount() - position) - 1;
        if (convertPosition < 0) {
            return 0;
        }
        return convertPosition;
    }

    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(reverse(position));
    }

    public float getPageWidth(int position) {
        return super.getPageWidth(reverse(position));
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (this.mIsRTL) {
            super.setPrimaryItem(container, (this.mLastCount - position) - 1, object);
        } else {
            super.setPrimaryItem(container, position, object);
        }
    }

    public int getLastCount() {
        return this.mLastCount;
    }

    public void setLastCount(int newCount) {
        this.mLastCount = newCount;
    }
}
