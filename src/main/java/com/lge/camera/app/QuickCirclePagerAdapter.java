package com.lge.camera.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.p000v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class QuickCirclePagerAdapter extends PagerAdapter {
    private final int AKA_PIC_MAX = 5;
    private int mCoverType = 0;
    private int mHeight = 0;
    private LayoutInflater mInflater = null;
    private int mPicCount;
    public ArrayList<BitmapDrawable> mPictureList = null;
    private int mQuickCoverRatio = 0;
    private ImageView mRecentImageView = null;
    private int mWidth = 0;

    public QuickCirclePagerAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mPictureList = new ArrayList();
        this.mPicCount = 0;
    }

    public void addPicture(BitmapDrawable bitmapDrawable) {
        if (this.mPictureList != null && bitmapDrawable != null) {
            BitmapDrawable toRecyle;
            if (this.mCoverType == 4) {
                if (this.mPictureList.size() >= 5) {
                    if (this.mRecentImageView != null) {
                        this.mRecentImageView.setBackground(null);
                    }
                    toRecyle = (BitmapDrawable) this.mPictureList.get(0);
                    this.mPictureList.remove(0);
                    BitmapManagingUtil.recycleBitmapDrawable(toRecyle);
                    this.mPicCount--;
                }
                if (this.mPictureList != null) {
                    this.mPictureList.add(this.mPicCount, checkRecycled(bitmapDrawable));
                }
                this.mPicCount++;
            } else {
                if (this.mPictureList != null && this.mPictureList.size() > 0) {
                    if (this.mRecentImageView != null) {
                        this.mRecentImageView.setBackground(null);
                    }
                    toRecyle = (BitmapDrawable) this.mPictureList.get(0);
                    this.mPictureList.remove(0);
                    BitmapManagingUtil.recycleBitmapDrawable(toRecyle);
                }
                if (this.mPictureList != null) {
                    this.mPictureList.add(0, checkRecycled(bitmapDrawable));
                }
            }
            notifyDataSetChanged();
        }
    }

    private BitmapDrawable checkRecycled(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable == null) {
            return null;
        }
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        return bitmapDrawable;
    }

    public boolean isEmpty() {
        if (this.mPictureList == null || this.mPictureList.size() == 0) {
            return true;
        }
        return false;
    }

    public void setValue(int width, int height, int coverType, int ratio) {
        this.mWidth = width;
        this.mHeight = height;
        this.mCoverType = coverType;
        this.mQuickCoverRatio = ratio;
    }

    public void setCoverRatio(int ratio) {
        this.mQuickCoverRatio = ratio;
    }

    public BitmapDrawable getPicture() {
        if (this.mPictureList != null && this.mPictureList.size() > 0) {
            BitmapDrawable ret = (BitmapDrawable) this.mPictureList.get(0);
            if (ret != null) {
                if (ret.getBitmap().isRecycled()) {
                    return null;
                }
                return ret;
            }
        }
        return null;
    }

    public BitmapDrawable getPicture(int count) {
        if (this.mPictureList == null || this.mPictureList.size() <= count) {
            return null;
        }
        return (BitmapDrawable) this.mPictureList.get(count);
    }

    public int getCount() {
        if (this.mPictureList != null && this.mPictureList.size() < 2) {
            return this.mPictureList.size() + 1;
        }
        return 2;
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        if (container == null) {
            CamLog.m3d(CameraConstants.TAG, "instantiateItem : PagerAdapter container is null.");
            return null;
        }
        Object postViewByTag = null;
        if (position == 0) {
            postViewByTag = container.findViewWithTag("preview");
            if (postViewByTag == null) {
                postViewByTag = this.mInflater.inflate(C0088R.layout.quick_window_camera_view, null);
                postViewByTag.setTag("preview");
            }
        } else if (position == 1) {
            postViewByTag = container.findViewWithTag("postview");
            if (this.mCoverType == 4) {
                if (postViewByTag == null) {
                    postViewByTag = this.mInflater.inflate(C0088R.layout.postview_aka, null);
                    postViewByTag.setTag("postview");
                }
                setAKAPostImageView(postViewByTag);
            } else {
                if (postViewByTag == null) {
                    postViewByTag = this.mInflater.inflate(C0088R.layout.postview_quickcircle, null);
                    postViewByTag.setTag("postview");
                }
                setCoverPostImageView(postViewByTag);
            }
        }
        if (postViewByTag == null) {
            return null;
        }
        postViewByTag.setVisibility(0);
        container.addView(postViewByTag);
        return postViewByTag;
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (container != null) {
            container.removeView((View) object);
        }
    }

    private void setCoverPostImageView(View postViewByTag) {
        if (postViewByTag != null) {
            this.mRecentImageView = (ImageView) postViewByTag.findViewById(C0088R.id.captured_quickcircle_image);
            if (this.mRecentImageView != null) {
                LayoutParams postviewParams = (LayoutParams) this.mRecentImageView.getLayoutParams();
                if (this.mQuickCoverRatio == 2) {
                    postviewParams.topMargin = 0;
                }
                if (this.mQuickCoverRatio != 3) {
                    this.mRecentImageView.setPaddingRelative((this.mHeight - this.mWidth) / 2, 0, 0, 0);
                } else {
                    this.mRecentImageView.setPaddingRelative(0, 0, 0, 0);
                }
                this.mRecentImageView.setLayoutParams(postviewParams);
                this.mRecentImageView.setBackground(getPicture());
                this.mRecentImageView.setVisibility(0);
            }
        }
    }

    private void setAKAPostImageView(View postViewByTag) {
        if (postViewByTag != null) {
            LinearLayout postViewById = (LinearLayout) postViewByTag.findViewById(C0088R.id.aka_postview_linear);
            LayoutParams postviewParams = (LayoutParams) postViewById.getLayoutParams();
            postviewParams.height = this.mHeight;
            postViewById.setLayoutParams(postviewParams);
            int picSize = this.mPictureList.size();
            int picWidth = (int) (((long) this.mWidth) / ((long) picSize));
            int picHeight = this.mHeight;
            int picWidth_max = getMaxWidth(picHeight);
            if (picWidth > picWidth_max) {
                picWidth = picWidth_max;
            }
            SparseArray<ImageView> sArray = new SparseArray();
            sArray.put(0, (ImageView) postViewByTag.findViewById(C0088R.id.captured_aka_image4));
            sArray.put(1, (ImageView) postViewByTag.findViewById(C0088R.id.captured_aka_image3));
            sArray.put(2, (ImageView) postViewByTag.findViewById(C0088R.id.captured_aka_image2));
            sArray.put(3, (ImageView) postViewByTag.findViewById(C0088R.id.captured_aka_image1));
            sArray.put(4, (ImageView) postViewByTag.findViewById(C0088R.id.captured_aka_image));
            int count = picSize;
            while (count > 0) {
                count--;
                if (count != 4 || this.mRecentImageView == null) {
                    ImageView cImage = (ImageView) sArray.get(count);
                    if (cImage != null) {
                        cImage.setVisibility(0);
                        cImage.setImageDrawable(getPicture(count));
                        setAKAPostviewParams(cImage, picWidth, picHeight, picSize);
                    }
                } else {
                    this.mRecentImageView = (ImageView) sArray.get(count);
                    this.mRecentImageView.setVisibility(0);
                    this.mRecentImageView.setImageDrawable(getPicture(count));
                    setAKAPostviewParams(this.mRecentImageView, picWidth, picHeight, picSize);
                }
            }
        }
    }

    private int getMaxWidth(int picHeight) {
        switch (this.mQuickCoverRatio) {
            case 2:
                return (int) (((float) picHeight) / 1.7777778f);
            case 3:
                return picHeight;
            default:
                return (int) (((float) picHeight) / 1.3333334f);
        }
    }

    private void setAKAPostviewParams(ImageView imgView, int width, int height, int picSize) {
        if (picSize < 5) {
            LinearLayout.LayoutParams lParam = (LinearLayout.LayoutParams) imgView.getLayoutParams();
            if (lParam != null) {
                lParam.width = width;
                lParam.height = height;
                imgView.setLayoutParams(lParam);
            }
        }
    }

    public void unbind() {
        if (this.mRecentImageView != null) {
            this.mRecentImageView = null;
        }
        if (this.mPictureList != null) {
            for (int i = 0; i < this.mPictureList.size(); i++) {
                BitmapDrawable temp = (BitmapDrawable) this.mPictureList.get(i);
                if (temp != null) {
                    this.mPictureList.remove(i);
                    BitmapManagingUtil.recycleBitmapDrawable(temp);
                }
            }
            this.mPictureList = null;
        }
    }
}
