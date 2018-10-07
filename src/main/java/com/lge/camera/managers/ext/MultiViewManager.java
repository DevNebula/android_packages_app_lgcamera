package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.MemoryUtils;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;
import java.util.ArrayList;
import java.util.Iterator;

public class MultiViewManager extends ManagerInterfaceImpl {
    private static int sMULTIVIEW_MANAGER_CNT = 0;
    private final String CAPTURED = "captured";
    private final String EMPTY = "empty";
    private final int PREVIEW_DIM_COLOR = -1090519040;
    private ImageView mCapturedPreview1 = null;
    private ImageView mCapturedPreview2 = null;
    private ImageView mCapturedPreview3 = null;
    private ImageView mCapturedPreview4 = null;
    private Point mCenterPoint = null;
    private int mCurViewIndex = 0;
    private ArrayList<Integer> mDrawableArray = null;
    private View mFrameShotGuideView = null;
    private ImageView mGuideBgView = null;
    private ImageView mGuideImageView = null;
    private int[] mGuideLocation = null;
    private RelativeLayout mGuideSelectedLayout = null;
    private ArrayList<RotateTextView> mGuideTextArray = null;
    private ArrayList<int[]> mTextLocationArray = null;

    /* renamed from: com.lge.camera.managers.ext.MultiViewManager$1 */
    class C12311 implements OnTouchListener {
        C12311() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            String tag = (String) MultiViewManager.this.mCapturedPreview1.getTag();
            if (MultiViewManager.this.mGet.getSpliceviewReverseState()) {
                return false;
            }
            return MultiViewManager.this.ignoreTouchEvent(event, tag);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.MultiViewManager$2 */
    class C12322 implements OnTouchListener {
        C12322() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            String tag = (String) MultiViewManager.this.mCapturedPreview2.getTag();
            if (MultiViewManager.this.mGet.getSpliceviewReverseState()) {
                return true;
            }
            return MultiViewManager.this.ignoreTouchEvent(event, tag);
        }
    }

    /* renamed from: com.lge.camera.managers.ext.MultiViewManager$3 */
    class C12333 implements OnTouchListener {
        C12333() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            return MultiViewManager.this.ignoreTouchEvent(event, (String) MultiViewManager.this.mCapturedPreview3.getTag());
        }
    }

    /* renamed from: com.lge.camera.managers.ext.MultiViewManager$4 */
    class C12344 implements OnTouchListener {
        C12344() {
        }

        public boolean onTouch(View arg0, MotionEvent event) {
            return MultiViewManager.this.ignoreTouchEvent(event, (String) MultiViewManager.this.mCapturedPreview4.getTag());
        }
    }

    public MultiViewManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
        sMULTIVIEW_MANAGER_CNT++;
        CamLog.m3d(CameraConstants.TAG, "MultiviewManager sMULTIVIEW_MANAGER_CNT = " + sMULTIVIEW_MANAGER_CNT);
    }

    public void init() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- init");
        super.init();
    }

    public void onPauseAfter() {
        super.onPauseAfter();
        setFrameShotGuideVisibility(false);
    }

    private void releaseViews() {
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (vg != null && this.mFrameShotGuideView != null && this.mGuideTextArray != null) {
            vg.removeView(this.mFrameShotGuideView);
            Iterator it = this.mGuideTextArray.iterator();
            while (it.hasNext()) {
                RotateTextView tv = (RotateTextView) it.next();
                if (tv != null) {
                    vg.removeView(tv);
                    MemoryUtils.releaseViews(tv);
                }
            }
        }
    }

    public void onDestroy() {
        CamLog.m3d(CameraConstants.TAG, "-mutliview- onDestroy");
        releaseViews();
        super.onDestroy();
        this.mGuideSelectedLayout = null;
        this.mGuideImageView = null;
        this.mGuideLocation = null;
        if (this.mDrawableArray != null) {
            this.mDrawableArray.clear();
            this.mDrawableArray = null;
        }
        if (this.mTextLocationArray != null) {
            this.mTextLocationArray.clear();
            this.mTextLocationArray = null;
        }
        if (this.mGuideTextArray != null) {
            this.mGuideTextArray.clear();
            this.mGuideTextArray = null;
        }
        clearCapturedImageView();
        sMULTIVIEW_MANAGER_CNT--;
        CamLog.m3d(CameraConstants.TAG, "onDestroy sMULTIVIEW_MANAGER_CNT = " + sMULTIVIEW_MANAGER_CNT);
    }

    public void clearCapturedImageView() {
        if (this.mCapturedPreview1 != null) {
            this.mCapturedPreview1.setVisibility(8);
            this.mCapturedPreview1 = null;
        }
        if (this.mCapturedPreview2 != null) {
            this.mCapturedPreview2.setVisibility(8);
            this.mCapturedPreview2 = null;
        }
        if (this.mCapturedPreview3 != null) {
            this.mCapturedPreview3.setVisibility(8);
            this.mCapturedPreview3 = null;
        }
        if (this.mCapturedPreview4 != null) {
            this.mCapturedPreview4.setVisibility(8);
            this.mCapturedPreview4 = null;
        }
    }

    public void initGuideLayout() {
        releaseViews();
        this.mDrawableArray = new ArrayList();
        this.mTextLocationArray = new ArrayList();
        this.mGuideTextArray = new ArrayList();
        CamLog.m3d(CameraConstants.TAG, "-multiview- initGuideLayout");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        this.mFrameShotGuideView = this.mGet.inflateView(C0088R.layout.multiview_frame_shot_guide);
        if (vg != null && this.mFrameShotGuideView != null) {
            vg.addView(this.mFrameShotGuideView);
            this.mCapturedPreview1 = (ImageView) this.mFrameShotGuideView.findViewById(C0088R.id.multiview_captured01);
            this.mCapturedPreview2 = (ImageView) this.mFrameShotGuideView.findViewById(C0088R.id.multiview_captured02);
            this.mCapturedPreview3 = (ImageView) this.mFrameShotGuideView.findViewById(C0088R.id.multiview_captured03);
            this.mCapturedPreview4 = (ImageView) this.mFrameShotGuideView.findViewById(C0088R.id.multiview_captured04);
            setOnTouchListenerForCapturedView();
            this.mGuideSelectedLayout = (RelativeLayout) this.mFrameShotGuideView.findViewById(C0088R.id.multiview_guide_layout);
            this.mGuideImageView = (ImageView) this.mGuideSelectedLayout.findViewById(C0088R.id.multiview_guide_image);
            this.mGuideBgView = (ImageView) this.mGuideSelectedLayout.findViewById(C0088R.id.multi_frame_shot_guide_bg);
        }
    }

    private boolean ignoreTouchEvent(MotionEvent event, String tag) {
        if (!"captured".equals(tag)) {
            return false;
        }
        switch (event.getAction()) {
            case 1:
                if (!this.mGet.isModeMenuVisible() && !this.mGet.isSettingMenuVisible() && !this.mGet.isHelpListVisible()) {
                    return false;
                }
                this.mGet.hideModeMenu(false, false);
                this.mGet.removeSettingMenu(true, false);
                return false;
            default:
                return true;
        }
    }

    private void setOnTouchListenerForCapturedView() {
        this.mCapturedPreview1.setOnTouchListener(new C12311());
        this.mCapturedPreview2.setOnTouchListener(new C12322());
        this.mCapturedPreview3.setOnTouchListener(new C12333());
        this.mCapturedPreview4.setOnTouchListener(new C12344());
    }

    public void activateFirstGuideLayout() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- activateFirstGuideLayout");
        String isFrameShot = this.mGet.getSettingValue(Setting.KEY_MULTIVIEW_FRAMESHOT);
        CamLog.m3d(CameraConstants.TAG, "isFrameShot = " + isFrameShot);
        if (this.mFrameShotGuideView != null) {
            if ("on".equals(isFrameShot)) {
                this.mFrameShotGuideView.setVisibility(0);
                setFrameShotGuideVisibility(true);
            } else {
                this.mFrameShotGuideView.setVisibility(8);
                setFrameShotGuideVisibility(false);
            }
        }
        setGuideLocation();
        resetFreezedPreview();
        this.mGuideImageView.setBackground(this.mGet.getAppContext().getDrawable(((Integer) this.mDrawableArray.get(0)).intValue()));
        this.mCurViewIndex = 0;
        clearCurrentView(this.mCurViewIndex);
    }

    public void setMultiviewGuideBg(int layoutIndex) {
        if (this.mGuideBgView != null) {
            this.mGuideBgView.setImageLevel(layoutIndex);
        }
    }

    public void restorePreCondition() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- restorePreCondition");
        setGuideLocation();
        resetFreezedPreview();
        clearCurrentView(this.mCurViewIndex);
        initGuideText();
        if (this.mFrameShotGuideView != null) {
            this.mFrameShotGuideView.setVisibility(0);
        }
        setFrameShotGuideVisibility(true);
    }

    public void updateGuide() {
        CamLog.m3d(CameraConstants.TAG, "updateGuide mCurViewIndex = " + this.mCurViewIndex);
        if (!this.mGet.isSpliceViewImporteImage()) {
            this.mCurViewIndex++;
        }
        clearCurrentView(this.mCurViewIndex);
        updateDrawable();
    }

    private void clearCurrentView(int index) {
        if (this.mCapturedPreview1 != null && this.mCapturedPreview2 != null && this.mCapturedPreview3 != null && this.mCapturedPreview4 != null) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- clearCurrentView index = " + index);
            this.mCapturedPreview1.setVisibility(0);
            this.mCapturedPreview2.setVisibility(0);
            this.mCapturedPreview3.setVisibility(0);
            this.mCapturedPreview4.setVisibility(0);
            if (index == 0) {
                this.mCapturedPreview1.setBackgroundColor(0);
            } else if (index == 1) {
                this.mCapturedPreview1.setBackgroundColor(0);
                setCapturedTag(0);
                this.mCapturedPreview2.setBackgroundColor(0);
            } else if (index == 2) {
                this.mCapturedPreview1.setBackgroundColor(0);
                setCapturedTag(0);
                this.mCapturedPreview2.setBackgroundColor(0);
                setCapturedTag(1);
                this.mCapturedPreview3.setBackgroundColor(0);
            } else {
                this.mCapturedPreview1.setBackgroundColor(0);
                setCapturedTag(0);
                this.mCapturedPreview2.setBackgroundColor(0);
                setCapturedTag(1);
                this.mCapturedPreview3.setBackgroundColor(0);
                setCapturedTag(2);
                this.mCapturedPreview4.setBackgroundColor(0);
            }
            if (this.mGet.isSpliceViewImporteImage()) {
                this.mCapturedPreview1.setBackgroundColor(0);
                this.mCapturedPreview2.setBackgroundColor(0);
            }
        }
    }

    private void setPreviewBitmap(ImageView preview, Bitmap bitmap) {
        preview.setVisibility(0);
        if (bitmap == null) {
            preview.setImageBitmap(null);
            preview.setTag("empty");
            preview.setBackgroundColor(-1090519040);
            return;
        }
        preview.setImageBitmap(bitmap);
        preview.setTag("captured");
    }

    private void updateDrawable() {
        ViewUtil.clearImageViewDrawableOnly(this.mGuideImageView);
        if (this.mCurViewIndex < this.mDrawableArray.size()) {
            this.mGuideImageView.setBackground(this.mGet.getAppContext().getDrawable(((Integer) this.mDrawableArray.get(this.mCurViewIndex)).intValue()));
            this.mGuideImageView.setVisibility(0);
        }
    }

    public void setFrameShotGuideVisibility(boolean visibility) {
        int i = 0;
        if (CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode())) {
            visibility = false;
        }
        CamLog.m3d(CameraConstants.TAG, "-interval- setFrameShotGuideVisibility visibility = " + visibility);
        if (this.mGuideSelectedLayout != null) {
            int i2;
            RelativeLayout relativeLayout = this.mGuideSelectedLayout;
            if (visibility) {
                i2 = 0;
            } else {
                i2 = 4;
            }
            relativeLayout.setVisibility(i2);
            ImageView imageView = this.mGuideBgView;
            if (!visibility) {
                i = 4;
            }
            imageView.setVisibility(i);
        }
        setMultiGuideTextVisibility(visibility);
    }

    public void setDrawableArray(ArrayList<Integer> array) {
        if (this.mDrawableArray != null) {
            this.mDrawableArray.clear();
        }
        this.mDrawableArray = (ArrayList) array.clone();
    }

    public void showCapturedImg(Bitmap bitmap, Rect rect, int index) {
        final int i = index;
        final Bitmap bitmap2 = bitmap;
        final Rect rect2 = rect;
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (i == 0) {
                    CamLog.m3d(CameraConstants.TAG, "mCapturedPreview1 = " + MultiViewManager.this.mCapturedPreview1);
                    MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview1, bitmap2);
                    MultiViewManager.this.setViewPositionOnRelativeLayoutParam(MultiViewManager.this.mCapturedPreview1, rect2);
                } else if (i == 1) {
                    MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview2, bitmap2);
                    MultiViewManager.this.setViewPositionOnRelativeLayoutParam(MultiViewManager.this.mCapturedPreview2, rect2);
                } else if (i == 2) {
                    MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview3, bitmap2);
                    MultiViewManager.this.setViewPositionOnRelativeLayoutParam(MultiViewManager.this.mCapturedPreview3, rect2);
                } else {
                    MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview4, bitmap2);
                    MultiViewManager.this.setViewPositionOnRelativeLayoutParam(MultiViewManager.this.mCapturedPreview4, rect2);
                }
            }
        });
    }

    public void setCapturedTag(final int index) {
        if (this.mCapturedPreview1 != null && this.mCapturedPreview2 != null && this.mCapturedPreview3 != null && this.mCapturedPreview4 != null) {
            this.mGet.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (index == 0) {
                        MultiViewManager.this.mCapturedPreview1.setTag("captured");
                    } else if (index == 1) {
                        MultiViewManager.this.mCapturedPreview2.setTag("captured");
                    } else if (index == 2) {
                        MultiViewManager.this.mCapturedPreview3.setTag("captured");
                    } else {
                        MultiViewManager.this.mCapturedPreview4.setTag("captured");
                    }
                }
            });
        }
    }

    public void setViewPositionOnRelativeLayoutParam(View v, Rect rect) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = rect.left;
        lp.topMargin = rect.top;
        lp.width = rect.width();
        lp.height = rect.height();
        v.setLayoutParams(lp);
    }

    public void setViewPositionOnRelativeLayoutParam(View v, int left, int top) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = left;
        lp.topMargin = top;
        v.setLayoutParams(lp);
    }

    public void setCaptureImgPosition(int index, Rect rect) {
        CamLog.m3d(CameraConstants.TAG, "-multiview- setCaptureImgPosition index = " + index + " rect.left = " + rect.left + " rect.top = " + rect.top);
        if (index == 0) {
            setViewPositionOnRelativeLayoutParam(this.mCapturedPreview1, rect);
        } else if (index == 1) {
            setViewPositionOnRelativeLayoutParam(this.mCapturedPreview2, rect);
        } else if (index == 2) {
            setViewPositionOnRelativeLayoutParam(this.mCapturedPreview3, rect);
        } else {
            setViewPositionOnRelativeLayoutParam(this.mCapturedPreview4, rect);
        }
    }

    public void resetFreezedPreview() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- resetFreezedPreview");
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview1, null);
                MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview2, null);
                MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview3, null);
                MultiViewManager.this.setPreviewBitmap(MultiViewManager.this.mCapturedPreview4, null);
            }
        });
    }

    public void setCenterPoint(Point center) {
        this.mCenterPoint = center;
    }

    private void setGuideLocation() {
        if (this.mCenterPoint == null) {
            CamLog.m3d(CameraConstants.TAG, "setGuideLocation mCenterPoint is null");
            return;
        }
        int half_height = this.mGet.getAppContext().getResources().getDrawable(C0088R.drawable.camera_multiview_bg_type_02).getIntrinsicHeight() / 2;
        int half_width = this.mGet.getAppContext().getResources().getDrawable(C0088R.drawable.camera_multiview_bg_type_02).getIntrinsicWidth() / 2;
        CamLog.m3d(CameraConstants.TAG, "height = " + half_height + " width = " + half_width);
        int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
        int left = (lcdSize[1] / 2) - half_width;
        int top = (lcdSize[0] / 2) - half_height;
        if ((this.mCenterPoint.x > 0 && this.mCenterPoint.x < lcdSize[1]) || (this.mCenterPoint.y > 0 && this.mCenterPoint.y < lcdSize[0])) {
            left = this.mCenterPoint.x - half_width;
            top = this.mCenterPoint.y - half_height;
        }
        CamLog.m3d(CameraConstants.TAG, "center x = " + this.mCenterPoint.x + " mCenterPoint y = " + this.mCenterPoint.y);
        setViewPositionOnRelativeLayoutParam(this.mGuideSelectedLayout, left, top);
        this.mGuideLocation = new int[]{left + half_width, top + half_height};
    }

    public int[] getGuideLocation() {
        return this.mGuideLocation;
    }

    public void onPauseBefore() {
        if (this.mFrameShotGuideView != null) {
            this.mFrameShotGuideView.setVisibility(4);
        }
    }

    public void setRecordingUILocation(int[] location, boolean isFrameShot) {
        RelativeLayout recTime = (RelativeLayout) this.mGet.findViewById(C0088R.id.rec_time_indicator);
        RotateTextView tv = (RotateTextView) this.mGet.findViewById(C0088R.id.arc_progress_text);
        RotateLayout v = (RotateLayout) this.mGet.findViewById(C0088R.id.arc_progress_rotate_layout);
        CamLog.m7i(CameraConstants.TAG, "location[0] : " + location[0] + ", location[1] : " + location[1]);
        CamLog.m7i(CameraConstants.TAG, "recTime : " + recTime + ", tv : " + tv + ", v : " + v);
        if (recTime == null || tv == null || v == null) {
            CamLog.m5e(CameraConstants.TAG, "recording UI is null");
            return;
        }
        LayoutParams param = (LayoutParams) v.getLayoutParams();
        if (isFrameShot) {
            recTime.setVisibility(4);
            tv.setVisibility(0);
            v.setVisibility(0);
        } else {
            recTime.setVisibility(0);
            tv.setVisibility(8);
            v.setVisibility(8);
        }
        Utils.resetLayoutParameter(param);
        param.addRule(10);
        param.addRule(20);
        param.setMarginEnd(0);
        param.setMargins(location[0], location[1], 0, 0);
        param.setMarginStart(location[0]);
        v.setLayoutParams(param);
    }

    public void initGuideText() {
        if (this.mGuideTextArray == null) {
            CamLog.m5e(CameraConstants.TAG, "mGuideTextArray is null");
            return;
        }
        int i;
        setMultiGuideTextVisibility(false);
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.contents_base);
        if (this.mGuideTextArray.size() != this.mTextLocationArray.size()) {
            Iterator it = this.mGuideTextArray.iterator();
            while (it.hasNext()) {
                RotateTextView tv = (RotateTextView) it.next();
                if (tv != null) {
                    vg.removeView(tv);
                    MemoryUtils.releaseViews(tv);
                }
            }
            this.mGuideTextArray.clear();
            for (i = 0; i < this.mTextLocationArray.size(); i++) {
                if (this.mGuideTextArray != null) {
                    RotateTextView textView = new RotateTextView(this.mGet.getAppContext());
                    if (!(vg == null || textView == null)) {
                        vg.addView(textView);
                        this.mGuideTextArray.add(textView);
                    }
                }
            }
        }
        int[] location = new int[0];
        for (i = 0; i < this.mTextLocationArray.size(); i++) {
            location = (int[]) this.mTextLocationArray.get(i);
            FrameLayout.LayoutParams param = new FrameLayout.LayoutParams((int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.multi_guide_text_width), (int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.multi_guide_text_height));
            param.leftMargin = location[0];
            param.topMargin = location[1];
            ((RotateTextView) this.mGuideTextArray.get(i)).setText(String.valueOf(i + 1));
            ((RotateTextView) this.mGuideTextArray.get(i)).setTextSize((int) this.mGet.getAppContext().getResources().getDimension(C0088R.dimen.multi_guide_text_size));
            ((RotateTextView) this.mGuideTextArray.get(i)).setLayoutParams(param);
            ((RotateTextView) this.mGuideTextArray.get(i)).setVisibility(8);
            ((RotateTextView) this.mGuideTextArray.get(i)).setTextTypeface(1);
            ((RotateTextView) this.mGuideTextArray.get(i)).setTextColor(-1);
            ((RotateTextView) this.mGuideTextArray.get(i)).setTextShadowColor(-1);
            ((RotateTextView) this.mGuideTextArray.get(i)).setTextShadowRadius(2.0f);
        }
        setDegree(this.mGet.getOrientationDegree(), false);
    }

    public void setDegree(int degree, boolean animation) {
        if (this.mGuideTextArray != null) {
            Iterator it = this.mGuideTextArray.iterator();
            while (it.hasNext()) {
                RotateTextView tv = (RotateTextView) it.next();
                if (tv != null) {
                    tv.setDegree(degree, animation);
                }
            }
            super.setDegree(degree, animation);
        }
    }

    public void setGuideTextLocation(ArrayList<int[]> array) {
        if (this.mTextLocationArray != null) {
            this.mTextLocationArray.clear();
        }
        this.mTextLocationArray = (ArrayList) array.clone();
    }

    public void setMultiGuideTextVisibility(boolean visibility) {
        if (this.mGuideTextArray != null) {
            if (CameraConstants.MODE_SQUARE_SPLICE.equals(this.mGet.getShotMode())) {
                visibility = false;
            }
            CamLog.m3d(CameraConstants.TAG, "-interval- setMultiGuideTextVisibility visibility = " + visibility);
            Iterator it = this.mGuideTextArray.iterator();
            while (it.hasNext()) {
                RotateTextView tv = (RotateTextView) it.next();
                if (tv != null) {
                    tv.setVisibility(visibility ? 0 : 8);
                }
            }
        }
    }
}
