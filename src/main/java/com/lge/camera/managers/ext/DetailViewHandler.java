package com.lge.camera.managers.ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.file.ExifInterface.GpsSpeedRef;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;
import com.lge.graphy.data.GraphyItem;

public class DetailViewHandler {
    private ViewGroup mBase;
    private TextView mCineHashTags;
    private Context mContext;
    private ViewGroup mDetailInfoWrapper;
    private RotateLayout mDetailRotateLayout;
    private ViewGroup mDetailView;
    private LinearLayout mGraphyData;
    private TextView mGraphyDataAperture;
    private TextView mGraphyDataISO;
    private TextView mGraphyDataSS;
    private TextView mGraphyDataWB;
    private int mGraphyImageHeight = 0;
    private int mGraphyImageWidth = 0;
    private TextView mGraphyTitle;
    private View mGraphyViewGap;
    private AnimationListener mHideListener = new C11961();
    private ImageView mImageView;
    private int mMaxHeight = 0;
    private int mMaxWidth = 0;

    /* renamed from: com.lge.camera.managers.ext.DetailViewHandler$1 */
    class C11961 implements AnimationListener {
        C11961() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (DetailViewHandler.this.mDetailView != null) {
                DetailViewHandler.this.mDetailView.setVisibility(8);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    public DetailViewHandler(Context context, ViewGroup baseParent) {
        if (context == null || baseParent == null) {
            CamLog.m5e(CameraConstants.TAG, "context or baseParent is null! return");
            return;
        }
        this.mContext = context;
        this.mBase = baseParent;
        LayoutInflater.from(context).inflate(C0088R.layout.detail_view, baseParent);
        this.mDetailView = (ViewGroup) this.mBase.findViewById(C0088R.id.detail_view_base);
        this.mDetailRotateLayout = (RotateLayout) this.mBase.findViewById(C0088R.id.detail_rotate_layout);
        this.mImageView = (ImageView) this.mDetailView.findViewById(C0088R.id.detail_image);
        this.mDetailInfoWrapper = (ViewGroup) this.mDetailView.findViewById(C0088R.id.detail_info_wrapper);
        this.mMaxWidth = RatioCalcUtil.getSizeCalculatedByPercentage(context, false, 0.895f);
        if (ModelProperties.getLCDType() == 2) {
            this.mDetailView.setPadding(0, RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.09f), 0, RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.295f));
            this.mMaxHeight = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.562f);
            return;
        }
        this.mDetailView.setPadding(0, 0, 0, RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.336f));
        this.mMaxHeight = RatioCalcUtil.getSizeCalculatedByPercentage(context, true, 0.608f);
    }

    public boolean isDetailViewVisible() {
        return this.mDetailView != null && this.mDetailView.isShown();
    }

    private void show() {
        if (this.mContext != null && this.mDetailRotateLayout != null && this.mDetailView != null && !this.mDetailView.isShown()) {
            AnimationUtil.startDetailViewShowAnimation(this.mDetailRotateLayout, 300, null);
            this.mDetailView.setVisibility(0);
            if (!FunctionProperties.isSupportedHDR10()) {
                int[] lcd_size = Utils.getLCDsize(this.mContext, true);
                Bitmap screenCapture = Utils.getScreenShot(lcd_size[1], lcd_size[0], false, null);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(screenCapture, (int) (((float) (lcd_size[1] * 600)) / ((float) lcd_size[0])), 600, false);
                Bitmap blurredBitmap = ColorUtil.getBlurImage(this.mContext, resizedBitmap, 13);
                if (!(screenCapture == null || screenCapture.isRecycled())) {
                    screenCapture.recycle();
                }
                if (!(resizedBitmap == null || resizedBitmap.isRecycled())) {
                    resizedBitmap.recycle();
                }
                this.mDetailView.setBackground(new BitmapDrawable(this.mContext.getResources(), blurredBitmap));
            }
        }
    }

    public void hide() {
        if (this.mDetailView != null && this.mDetailRotateLayout != null && this.mDetailView.isShown()) {
            AnimationUtil.startDetailViewHideAnimation(this.mDetailRotateLayout, 300, this.mHideListener);
        }
    }

    public void onDestroy() {
        this.mContext = null;
        if (!(this.mBase == null || this.mDetailView == null)) {
            this.mBase.removeView(this.mDetailView);
        }
        this.mBase = null;
        this.mDetailView = null;
        this.mImageView = null;
        this.mCineHashTags = null;
        this.mGraphyData = null;
        this.mGraphyDataWB = null;
        this.mGraphyDataAperture = null;
        this.mGraphyDataISO = null;
        this.mGraphyDataSS = null;
    }

    public void rotateLayout(int degree) {
        if (degree == 0 || degree == 180) {
            rotateGraphyDetailView(this.mGraphyImageWidth, this.mGraphyImageHeight);
        } else {
            rotateGraphyDetailView(this.mGraphyImageHeight, this.mGraphyImageWidth);
        }
        if (this.mDetailRotateLayout != null) {
            this.mDetailRotateLayout.rotateLayout(degree);
        }
        CamLog.m3d(CameraConstants.TAG, "[detail] mDetailInfoWrapper.getLayoutParams().height : " + this.mDetailInfoWrapper.getLayoutParams().height);
    }

    public void clearAnimation() {
        if (this.mDetailRotateLayout != null) {
            this.mDetailRotateLayout.clearAnimation();
        }
    }

    public void initCineDetailView() {
        if (this.mContext == null || this.mBase == null || this.mDetailInfoWrapper == null || this.mDetailRotateLayout == null) {
            CamLog.m5e(CameraConstants.TAG, "init Cine detail view failed");
            return;
        }
        LayoutInflater.from(this.mContext).inflate(C0088R.layout.cine_detail_view_info, this.mDetailInfoWrapper);
        this.mCineHashTags = (TextView) this.mBase.findViewById(C0088R.id.cine_detail_view_hash_tags);
        Options mDimensions = new Options();
        mDimensions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(this.mContext.getResources(), C0088R.drawable.camera_filter_long_press_sample_01, mDimensions);
        int minWidthHeight = Math.min(mDimensions.outWidth, this.mMaxWidth);
        this.mDetailRotateLayout.getLayoutParams().width = minWidthHeight;
        this.mDetailRotateLayout.getLayoutParams().height = minWidthHeight;
        CamLog.m3d(CameraConstants.TAG, "[detail] mDetailRotateLayout.width : " + this.mDetailRotateLayout.getLayoutParams().width + ", mDetailRotateLayout.height : " + this.mDetailRotateLayout.getLayoutParams().height);
    }

    public void initGraphyDetailView() {
        if (this.mContext == null || this.mBase == null || this.mDetailInfoWrapper == null || this.mDetailRotateLayout == null) {
            CamLog.m5e(CameraConstants.TAG, "init Cine detail view failed");
            return;
        }
        LayoutInflater.from(this.mContext).inflate(C0088R.layout.graphy_detail_view_info, this.mDetailInfoWrapper);
        this.mGraphyTitle = (TextView) this.mBase.findViewById(C0088R.id.graphy_image_info_title);
        this.mGraphyData = (LinearLayout) this.mBase.findViewById(C0088R.id.graphy_image_contents_info);
        this.mGraphyDataWB = (TextView) this.mBase.findViewById(C0088R.id.graphy_contents_info_wb);
        this.mGraphyDataAperture = (TextView) this.mBase.findViewById(C0088R.id.graphy_contents_info_aperture);
        this.mGraphyDataISO = (TextView) this.mBase.findViewById(C0088R.id.graphy_contents_info_iso);
        this.mGraphyDataSS = (TextView) this.mBase.findViewById(C0088R.id.graphy_contents_info_ss);
        this.mGraphyViewGap = this.mBase.findViewById(C0088R.id.graphy_iso_ss_gap);
    }

    private Bitmap getResizedBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        if (!(bitmap.getWidth() == this.mMaxWidth && bitmap.getHeight() == this.mMaxHeight)) {
            float s1;
            float s1x = ((float) this.mMaxWidth) / ((float) bitmap.getWidth());
            float s1y = ((float) this.mMaxHeight) / ((float) bitmap.getHeight());
            if (s1x < s1y) {
                s1 = s1x;
            } else {
                s1 = s1y;
            }
            matrix.postScale(s1, s1);
        }
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap == null) {
            return null;
        }
        if (resizedBitmap != bitmap) {
            bitmap.recycle();
        }
        return matrix != null ? resizedBitmap : resizedBitmap;
    }

    public void showGraphyDetailView(int degree, GraphyItem item, String title, String WB, String Aperture, String ISO, String SS) {
        Bitmap bitmap;
        int resId = item.getIntValue(GraphyItem.KEY_RESOURCE_ID_INT);
        String imagePath = item.getStringValue(GraphyItem.KEY_IMAGE_PATH_STR);
        if (resId > 0) {
            bitmap = BitmapManagingUtil.getBitmap(this.mContext, resId);
        } else if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
        } else {
            bitmap = getResizedBitmap(item.getBitmap());
        }
        if (bitmap == null) {
            CamLog.m5e(CameraConstants.TAG, "[detail] bitmap NULL");
            return;
        }
        this.mGraphyImageWidth = bitmap.getWidth();
        this.mGraphyImageHeight = bitmap.getHeight();
        rotateLayout(degree);
        CamLog.m11w(CameraConstants.TAG, "[detail] mMaxWidth : " + this.mMaxWidth + ", mMaxHeight : " + this.mMaxHeight);
        this.mImageView.setImageBitmap(bitmap);
        if (ModelProperties.isJoanRenewal()) {
            this.mGraphyTitle.setVisibility(8);
        } else {
            this.mGraphyTitle.setText(title);
        }
        this.mGraphyDataWB.setText("WB " + WB + GpsSpeedRef.KILOMETERS);
        this.mGraphyDataAperture.setText("F " + Aperture);
        this.mGraphyDataISO.setText("ISO " + ISO);
        this.mGraphyDataSS.setText("S " + SS);
        CamLog.m3d(CameraConstants.TAG, "WB " + WB + "K\tF " + Aperture + "\tISO " + ISO + "/tS " + SS);
        show();
    }

    private void rotateGraphyDetailView(int imageWidth, int imageHeight) {
        if (imageWidth != 0 && imageHeight != 0 && this.mGraphyImageWidth != 0 && this.mGraphyImageHeight != 0 && this.mDetailInfoWrapper != null && this.mGraphyData != null) {
            this.mGraphyDataWB.measure(0, 0);
            this.mGraphyDataISO.measure(0, 0);
            LayoutParams layoutParams;
            int px;
            if (this.mGraphyImageWidth <= this.mGraphyImageHeight) {
                layoutParams = this.mDetailInfoWrapper.getLayoutParams();
                if (ModelProperties.isJoanRenewal()) {
                    px = Utils.getPx(this.mContext, C0088R.dimen.detail_view_info_height_port) - Utils.getPx(this.mContext, C0088R.dimen.detail_view_graphy_content_title);
                } else {
                    px = Utils.getPx(this.mContext, C0088R.dimen.detail_view_info_height_port);
                }
                layoutParams.height = px;
                this.mGraphyViewGap.getLayoutParams().width = (this.mGraphyDataWB.getMeasuredWidth() - this.mGraphyDataISO.getMeasuredWidth()) + Utils.getPx(this.mContext, C0088R.dimen.detail_view_graphy_item_info_padding);
                if (this.mGraphyData.getOrientation() == 0) {
                    this.mGraphyData.setOrientation(1);
                }
            } else {
                layoutParams = this.mDetailInfoWrapper.getLayoutParams();
                if (ModelProperties.isJoanRenewal()) {
                    px = Utils.getPx(this.mContext, C0088R.dimen.detail_view_info_height_land) - Utils.getPx(this.mContext, C0088R.dimen.detail_view_graphy_content_title);
                } else {
                    px = Utils.getPx(this.mContext, C0088R.dimen.detail_view_info_height_land);
                }
                layoutParams.height = px;
                this.mGraphyViewGap.getLayoutParams().width = Utils.getPx(this.mContext, C0088R.dimen.detail_view_graphy_item_info_padding);
                if (this.mGraphyData.getOrientation() == 1) {
                    this.mGraphyData.setOrientation(0);
                }
            }
            CamLog.m11w(CameraConstants.TAG, "[detail] bitmap.getWidth : " + this.mGraphyImageWidth + ", bitmap.getHeight : " + this.mGraphyImageHeight + ", imageWidth : " + imageWidth + ", imageHeight : " + imageHeight);
            if (imageWidth <= imageHeight) {
                if (this.mMaxHeight >= imageHeight) {
                    this.mDetailRotateLayout.getLayoutParams().height = this.mMaxHeight;
                    this.mDetailRotateLayout.getLayoutParams().width = (this.mMaxHeight * imageWidth) / imageHeight;
                }
            } else if (this.mMaxWidth >= imageWidth) {
                this.mDetailRotateLayout.getLayoutParams().width = this.mMaxWidth;
                this.mDetailRotateLayout.getLayoutParams().height = (this.mMaxWidth * imageHeight) / imageWidth;
            }
        }
    }

    public void showCineDetailView(int resId, String hashTags) {
        if (this.mImageView == null || this.mCineHashTags == null) {
            CamLog.m5e(CameraConstants.TAG, "show Cine Detail failed");
            return;
        }
        this.mImageView.setImageResource(resId);
        this.mCineHashTags.setText(hashTags);
        show();
    }
}
