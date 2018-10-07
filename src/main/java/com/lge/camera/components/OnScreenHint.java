package com.lge.camera.components;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.Utils;

public class OnScreenHint {
    public static final int TOASTMESSAGE_END = 2;
    public static final int TOASTMESSAGE_NORMAL = 0;
    public static final int TOASTMESSAGE_STORAGE = 1;
    private static int sGravityLandscape = 81;
    private static int sGravityLandscapeReverse = 49;
    private static int sGravityPortrait = 8388629;
    private static int sGravityPortraitReverse = 8388627;
    private static String[] sLastMessage = new String[2];
    private static boolean sToastLocation = false;
    final Context mContext;
    int mGravity = sGravityLandscape;
    private final Handler mHandler = new Handler();
    private final Runnable mHide = new C05452();
    float mHorizontalMargin = 0.0f;
    int mLcdSizeHeight;
    int mLcdSizeWidth;
    View mNextView;
    private final LayoutParams mParams = new LayoutParams();
    private final Runnable mShow = new C05441();
    float mVerticalMargin = 0.0f;
    View mView;
    private final WindowManager mWM;
    /* renamed from: mX */
    int f15mX;
    /* renamed from: mY */
    int f16mY;

    /* renamed from: com.lge.camera.components.OnScreenHint$1 */
    class C05441 implements Runnable {
        C05441() {
        }

        public void run() {
            OnScreenHint.this.handleShow();
        }
    }

    /* renamed from: com.lge.camera.components.OnScreenHint$2 */
    class C05452 implements Runnable {
        C05452() {
        }

        public void run() {
            OnScreenHint.this.handleHide();
        }
    }

    public OnScreenHint(Context context) {
        this.mContext = context;
        DisplayMetrics outMetrics = Utils.getWindowRealMatics(context);
        this.mWM = (WindowManager) context.getSystemService("window");
        if (outMetrics.heightPixels > outMetrics.widthPixels) {
            this.mLcdSizeWidth = outMetrics.heightPixels;
            this.mLcdSizeHeight = outMetrics.widthPixels;
        } else {
            this.mLcdSizeWidth = outMetrics.widthPixels;
            this.mLcdSizeHeight = outMetrics.heightPixels;
        }
        this.mParams.height = -2;
        this.mParams.width = -2;
        this.mParams.flags = 24;
        this.mParams.format = -3;
        this.mParams.windowAnimations = C0088R.style.Animation.OnScreenHint;
        this.mParams.type = 1000;
        this.mParams.setTitle("OnScreenHint");
        if (this.mContext == null || isConfigureLandscape(this.mContext.getResources())) {
            sGravityLandscape = 81;
            sGravityPortrait = 8388629;
            sGravityLandscapeReverse = 49;
            sGravityPortraitReverse = 8388627;
            return;
        }
        sGravityLandscape = 8388627;
        sGravityPortrait = 81;
        sGravityLandscapeReverse = 8388629;
        sGravityPortraitReverse = 49;
    }

    public void show() {
        if (this.mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        this.mHandler.post(this.mShow);
    }

    public void cancel() {
        this.mHandler.post(this.mHide);
    }

    public void showImmediately() {
        if (this.mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        handleShow();
    }

    public void cancelImmediately() {
        handleHide();
    }

    public static OnScreenHint makeText(Context context, CharSequence text) {
        return makeText(context, text, 0);
    }

    public static OnScreenHint makeText(Context context, CharSequence text, int degree) {
        return makeText(context, text, degree, 0);
    }

    public static OnScreenHint makeText(Context context, CharSequence text, int degree, int selectedToastMessage) {
        OnScreenHint result = new OnScreenHint(context);
        View v = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(C0088R.layout.on_screen_hint, null);
        TextView tv = (TextView) v.findViewById(C0088R.id.message);
        if (selectedToastMessage < 0 || selectedToastMessage >= 2) {
            selectedToastMessage = 0;
        }
        sLastMessage[selectedToastMessage] = text.toString();
        tv.setText(sLastMessage[selectedToastMessage]);
        setGravityAndRotate(context, result, v, degree);
        result.mNextView = v;
        return result;
    }

    public static OnScreenHint changeOrientation(Context context, int degree) {
        return changeOrientation(context, degree, 0);
    }

    public static OnScreenHint changeOrientation(Context context, int degree, int selectedToastMessage) {
        if (selectedToastMessage < 0 || selectedToastMessage >= 2) {
            selectedToastMessage = 0;
        }
        OnScreenHint hint = makeText(context, sLastMessage[selectedToastMessage], degree);
        hint.show();
        return hint;
    }

    public void setText(CharSequence s) {
        if (this.mNextView == null) {
            throw new RuntimeException("This OnScreenHint was not created with OnScreenHint.makeText()");
        }
        TextView tv = (TextView) this.mNextView.findViewById(C0088R.id.message);
        if (tv == null) {
            throw new RuntimeException("This OnScreenHint was not created with OnScreenHint.makeText()");
        }
        tv.setText(s);
    }

    public String getText() {
        TextView tv = (TextView) this.mNextView.findViewById(C0088R.id.message);
        return tv == null ? null : (String) tv.getText();
    }

    private synchronized void handleShow() {
        if (this.mView != this.mNextView) {
            handleHide();
            this.mView = this.mNextView;
            int gravity = this.mGravity;
            boolean windowLand = isConfigureLandscape(this.mContext.getResources());
            CamLog.m3d(CameraConstants.TAG, "windowLand = " + windowLand);
            this.mParams.gravity = gravity;
            if ((gravity & 7) == 7) {
                if (windowLand) {
                    this.mParams.horizontalWeight = 1.0f;
                } else {
                    this.mParams.verticalWeight = 1.0f;
                }
            }
            if ((gravity & 112) == 112) {
                if (windowLand) {
                    this.mParams.verticalWeight = 1.0f;
                } else {
                    this.mParams.horizontalWeight = 1.0f;
                }
            }
            if (this.mGravity == sGravityLandscape || this.mGravity == sGravityLandscapeReverse) {
                if (windowLand) {
                    this.mParams.y = Utils.getPx(this.mContext, C0088R.dimen.screen_hint_bottom_margin);
                } else {
                    this.mParams.x = Utils.getPx(this.mContext, C0088R.dimen.screen_hint_bottom_margin_land);
                }
            } else if (this.mGravity == sGravityPortrait || this.mGravity == sGravityPortraitReverse) {
                if (windowLand) {
                    this.mParams.x = Utils.getPx(this.mContext, C0088R.dimen.screen_hint_bottom_margin);
                } else if (sToastLocation) {
                    this.mParams.y = Utils.getPx(this.mContext, C0088R.dimen.screen_hint_bottom_margin_center);
                } else {
                    this.mParams.y = Utils.getPx(this.mContext, C0088R.dimen.screen_hint_bottom_margin);
                    if (ModelProperties.getLCDType() == 2) {
                        LayoutParams layoutParams = this.mParams;
                        layoutParams.y += RatioCalcUtil.getSizeCalculatedByPercentage(this.mContext, true, 0.05601f);
                    }
                }
            }
            try {
                this.mParams.verticalMargin = this.mVerticalMargin;
                this.mParams.horizontalMargin = this.mHorizontalMargin;
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mWM.addView(this.mView, this.mParams);
            } catch (Exception e) {
                CamLog.m12w(CameraConstants.TAG, String.format("OnScreenHint display failed.", new Object[0]), e);
            }
        }
    }

    private synchronized void handleHide() {
        if (this.mView != null) {
            if (this.mView.getParent() != null) {
                this.mWM.removeView(this.mView);
            }
            this.mView = null;
        }
    }

    private static boolean isConfigureLandscape(Resources resource) {
        if (resource == null || resource.getConfiguration().orientation != 2) {
            return false;
        }
        return true;
    }

    private static void setGravityAndRotate(Context context, OnScreenHint result, View toast, int degree) {
        ((RotateLayout) toast.findViewById(C0088R.id.rotate_toast)).rotateLayout(degree);
        switch (degree) {
            case 90:
                result.mGravity = sGravityLandscape;
                return;
            case 180:
                result.mGravity = sGravityPortrait;
                return;
            case 270:
                result.mGravity = sGravityLandscape;
                return;
            default:
                result.mGravity = sGravityPortrait;
                return;
        }
    }

    public static void setToastLocation(boolean isCenter) {
        sToastLocation = isCenter;
    }
}
