package com.lge.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import com.lge.camera.util.Utils;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BarViewBase extends RelativeLayout implements OnRemoveHandler {
    public static final int CAMERA_PREVIEW_INTERACTION_DURATION = 120000;
    public static final int CURSOR_ONE_STEP_MINUS_BUTTON = -1;
    public static final int CURSOR_ONE_STEP_PLUS_BUTTON = 1;
    public static final int DEFAULT_INTERACTION_DURATION = 3000;
    protected static final long LONG_PRESS_EVENT_DELAY = 200;
    protected static final long LONG_PRESS_JUDGE_TIMEOUT = 1000;
    public static final int MANUAL_FOCUS_INTERACTION_DURATION = 5000;
    public static final String NONE_SETTING_KEY = "none";
    public static final int TYPE_ALL = -2;
    public static final int TYPE_BEAUTYSHOT_BAR = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_RELIGHTING_BAR = 4;
    public static final int TYPE_ZOOM_BAR = 2;
    public static int sCURSOR_MIN_STEP = 0;
    public static int sCURSOR_ONE_STEP_MINUS = -1;
    public static int sCURSOR_ONE_STEP_PLUS = 1;
    protected int ADJUST_BEAUTY_CURSOR;
    protected float CURSOR_HEIGHT;
    protected float CURSOR_HEIGHT_PORT;
    protected int CURSOR_POS_HEIGHT;
    protected int CURSOR_POS_HEIGHT_PORT;
    protected int CURSOR_POS_WIDTH;
    protected float CURSOR_WIDTH;
    protected int MAX_CURSOR_POS;
    protected int MAX_CURSOR_POS_PORT;
    private final int MAX_DIFF_TIME = 50;
    protected int MIN_CURSOR_POS;
    protected int RELEASE_EXPAND_BOTTOM = 0;
    protected int RELEASE_EXPAND_LEFT = 0;
    protected int RELEASE_EXPAND_RIGHT = 0;
    protected int RELEASE_EXPAND_TOP = 0;
    protected int cursorResId = C0088R.id.face_cursor;
    protected int cursorTextId = C0088R.id.beauty_text_layout;
    protected BarAction mBarAction = null;
    protected String mBarSettingKey = Setting.KEY_BEAUTYSHOT;
    protected boolean mBarTouchState = false;
    public int mBarType = 0;
    protected Timer mButtonCheckTimer;
    protected int mCursorBGResId = C0088R.id.face_cursor_bg_view;
    protected int mCursorMaxStep = 12;
    protected boolean mInitial = false;
    private long mLastMoveTime = 0;
    private long mMoveTimeDiff = 0;
    public OnTouchListener mOnButtonTouchListener = new C05282();
    public OnTouchListener mOnLineTouchListener = new C05271();
    private long mTimeDiff = 0;
    private long mTouchDownTime = 0;
    protected int mValue;
    protected int minusButtonResId = C0088R.id.face_minus_button;
    protected int minusButtonViewResId = C0088R.id.face_minus_button_view;
    protected int plusButtonResId = C0088R.id.face_plus_button;
    protected int plusButtonViewResId = C0088R.id.face_plus_button_view;

    /* renamed from: com.lge.camera.components.BarViewBase$1 */
    class C05271 implements OnTouchListener {
        C05271() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (BarViewBase.this.mInitial && BarViewBase.this.getVisibility() == 0) {
                int value = getValueForLineTouchListener(event.getX(), event.getY());
                switch (event.getAction()) {
                    case 0:
                        BarViewBase.this.doBarActionDown(v, event, value);
                        break;
                    case 1:
                    case 3:
                        BarViewBase.this.doBarActionUp(value);
                        break;
                    case 2:
                        BarViewBase.this.doBarActionMove(event, value);
                        break;
                }
                BarViewBase.this.resetDisplayTimeout();
                return true;
            }
            BarViewBase.this.mBarTouchState = false;
            return false;
        }

        private int getValueForLineTouchListener(float x, float y) {
            try {
                switch (BarViewBase.this.mBarType) {
                    case 1:
                    case 4:
                        float curLineLevel;
                        int marginDown;
                        if (Utils.isConfigureLandscape(BarViewBase.this.getResources())) {
                            curLineLevel = ((float) BarViewBase.this.CURSOR_POS_HEIGHT) / ((float) BarViewBase.this.mCursorMaxStep);
                            marginDown = (int) (((float) (BarViewBase.this.MAX_CURSOR_POS - (BarViewBase.this.MIN_CURSOR_POS * 2))) - (curLineLevel / 2.0f));
                        } else {
                            curLineLevel = ((float) BarViewBase.this.CURSOR_POS_HEIGHT_PORT) / ((float) BarViewBase.this.mCursorMaxStep);
                            marginDown = (BarViewBase.this.MAX_CURSOR_POS_PORT - (BarViewBase.this.MIN_CURSOR_POS * 2)) - BarViewBase.this.ADJUST_BEAUTY_CURSOR;
                        }
                        return (int) ((((float) marginDown) - y) / curLineLevel);
                    case 2:
                        int tmpCursorPosHeight;
                        int tmpCursorHeight = (int) (Utils.isConfigureLandscape(BarViewBase.this.getResources()) ? BarViewBase.this.CURSOR_HEIGHT : BarViewBase.this.CURSOR_HEIGHT_PORT);
                        if (Utils.isConfigureLandscape(BarViewBase.this.getResources())) {
                            tmpCursorPosHeight = BarViewBase.this.CURSOR_POS_HEIGHT;
                        } else {
                            tmpCursorPosHeight = BarViewBase.this.CURSOR_POS_HEIGHT_PORT;
                        }
                        return (int) ((((float) ((tmpCursorHeight + tmpCursorPosHeight) - tmpCursorHeight)) - (((float) (tmpCursorHeight / 2)) + y)) / (((float) (tmpCursorPosHeight - tmpCursorHeight)) / ((float) BarViewBase.this.mCursorMaxStep)));
                    default:
                        return 0;
                }
            } catch (ArithmeticException e) {
                CamLog.m3d(CameraConstants.TAG, "getCursorMaxStep() = " + BarViewBase.this.mCursorMaxStep);
                CamLog.m6e(CameraConstants.TAG, "ArithmeticException:", e);
                return 0;
            }
        }
    }

    /* renamed from: com.lge.camera.components.BarViewBase$2 */
    class C05282 implements OnTouchListener {
        C05282() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return onButtonTouch(v, event);
        }

        private boolean onButtonTouch(View v, MotionEvent event) {
            if (!BarViewBase.this.mInitial || BarViewBase.this.getVisibility() != 0) {
                return false;
            }
            switch (event.getAction()) {
                case 0:
                    BarViewBase.this.disallowTouchInParentView(v);
                    v.setPressed(true);
                    if (!BarViewBase.this.isTouchUpAreaOfButton(v, event)) {
                        v.setPressed(false);
                        BarViewBase.this.stopTimerTask();
                        break;
                    }
                    v.sendAccessibilityEvent(8);
                    if (!BarViewBase.this.isPlusButton(v)) {
                        BarViewBase.this.updateBarWithTimer(-1, false, false, false);
                        break;
                    }
                    BarViewBase.this.updateBarWithTimer(1, false, false, false);
                    break;
                case 1:
                case 3:
                    v.playSoundEffect(0);
                    v.setPressed(false);
                    BarViewBase.this.stopTimerTask();
                    BarViewBase.this.updateBar(0, false, true, true);
                    break;
                case 2:
                    if (!BarViewBase.this.isTouchUpAreaOfButton(v, event)) {
                        v.setPressed(false);
                        BarViewBase.this.stopTimerTask();
                        break;
                    }
                    break;
            }
            BarViewBase.this.resetDisplayTimeout();
            return true;
        }
    }

    protected abstract void disallowTouchInParentView(View view);

    protected abstract RotateLayout getBarLayout();

    protected abstract View getBarParentLayout();

    protected abstract View getBarView();

    public abstract void setLayoutDimension();

    public abstract void setProgress(int i);

    private void doBarActionDown(View v, MotionEvent event, int value) {
        this.mTimeDiff = 0;
        this.mTouchDownTime = System.currentTimeMillis();
        this.mMoveTimeDiff = 0;
        this.mLastMoveTime = 0;
        this.mBarTouchState = true;
        disallowTouchInParentView(v);
        findViewById(this.cursorResId).sendAccessibilityEvent(8);
        findViewById(this.cursorResId).setPressed(true);
        updateBarWithValue(value, false);
        if (this.mBarAction != null) {
            this.mBarAction.pauseShutterless();
        }
    }

    private void doBarActionMove(MotionEvent event, int value) {
        if (!isMovableArea(getBarView(), event)) {
            if (this.mTimeDiff == 0) {
                this.mTimeDiff = System.currentTimeMillis() - this.mTouchDownTime;
            }
            if (this.mTimeDiff <= 50) {
                return;
            }
        }
        if (!isNeedToSkipZoom()) {
            this.mBarTouchState = true;
            findViewById(this.cursorResId).sendAccessibilityEvent(4096);
            findViewById(this.cursorResId).setPressed(true);
            updateBarWithValue(value, false);
        }
    }

    private void doBarActionUp(int value) {
        this.mBarTouchState = false;
        if (0 >= this.mTimeDiff || this.mTimeDiff > 50 || this.mBarAction == null) {
            this.mTimeDiff = 0;
            if (this.mBarAction != null) {
                this.mBarAction.resumeShutterless();
            }
            findViewById(this.cursorResId).setPressed(false);
            updateBarWithValue(value, true);
            sendLDBIntentOnTouchUp();
            return;
        }
        CamLog.m7i(CameraConstants.TAG, "switch camera because the time difference between touch down and move is less than 50ms");
        this.mBarAction.switchCamera();
    }

    private boolean isNeedToSkipZoom() {
        if (ModelProperties.getAppTier() >= 5) {
            return false;
        }
        this.mMoveTimeDiff += System.currentTimeMillis() - this.mLastMoveTime;
        if (this.mMoveTimeDiff < 30) {
            return true;
        }
        this.mMoveTimeDiff = 0;
        this.mLastMoveTime = System.currentTimeMillis();
        return false;
    }

    protected void sendLDBIntentOnTouchUp() {
    }

    public BarViewBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BarViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarViewBase(Context context) {
        super(context);
    }

    public void initBar(BarAction barAction, int barType) {
        this.mBarAction = barAction;
        setLayoutDimension();
        findViewById(this.cursorResId).setFocusable(false);
        this.mBarType = barType;
        this.mInitial = true;
    }

    public void unbind() {
        this.mOnButtonTouchListener = null;
        this.mOnLineTouchListener = null;
        setListener(false);
        this.mBarAction = null;
        this.mButtonCheckTimer = null;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        disallowTouchInParentView(this);
        return super.dispatchTouchEvent(ev);
    }

    public void setListener(boolean set) {
        if (!this.mInitial) {
            return;
        }
        if (set) {
            findViewById(this.mCursorBGResId).setOnTouchListener(this.mOnLineTouchListener);
        } else {
            findViewById(this.mCursorBGResId).setOnTouchListener(null);
        }
    }

    public void updateAllBars() {
        if (this.mInitial && this.mBarAction != null) {
            this.mBarAction.updateAllBars(this.mBarType, this.mValue);
        }
    }

    public void updateBar(int step, boolean others, boolean isLongTouch, boolean actionEnd) {
        int lValue = this.mValue;
        if (this.mInitial && !actionEnd && this.mBarAction != null) {
            int updatedValue = lValue + step;
            if (updatedValue > this.mCursorMaxStep) {
                updatedValue = this.mCursorMaxStep;
            }
            if (updatedValue < 0) {
                updatedValue = 0;
            }
            if (updatedValue != lValue) {
                this.mValue = updatedValue;
                setCursor(this.mValue);
                setBarSettingValue(this.mBarSettingKey, this.mValue, isLongTouch);
                if (this.mBarType != 2) {
                    updateAllBars();
                }
            }
            resetDisplayTimeout();
        }
    }

    public void updateBarWithValue(int value, boolean actionEnd) {
        int lValue = this.mValue;
        if (!this.mInitial) {
            return;
        }
        if (actionEnd) {
            setBarSettingValue(this.mBarSettingKey, lValue, actionEnd);
            setProgress(lValue);
            return;
        }
        lValue = value;
        if (lValue > this.mCursorMaxStep) {
            lValue = this.mCursorMaxStep;
        }
        if (lValue < sCURSOR_MIN_STEP) {
            lValue = sCURSOR_MIN_STEP;
        }
        setCursor(lValue);
        this.mValue = lValue;
        setBarSettingValue(this.mBarSettingKey, lValue, false);
        if (this.mBarType != 2) {
            updateAllBars();
        }
        resetDisplayTimeout();
    }

    public void updateBarWithTimer(final int step, final boolean others, boolean isLongTouch, final boolean actionEnd) {
        if (this.mInitial) {
            updateBar(step, others, isLongTouch, actionEnd);
            if (!isLongTouch && !actionEnd) {
                startTimerTask(new TimerTask() {
                    public void run() {
                        BarViewBase.this.updateBarWithTimer(step, others, true, actionEnd);
                    }
                }, 1000, (long) ((int) (ModelProperties.isKeyPadSupported(getContext()) ? 100 : 200)));
            }
        }
    }

    public void startTimerTask(TimerTask t, long judge, long interval) {
        if (this.mInitial && this.mButtonCheckTimer == null) {
            this.mButtonCheckTimer = new Timer("timer_long_press_check");
            this.mButtonCheckTimer.scheduleAtFixedRate(t, judge, interval);
        }
    }

    public void stopTimerTask() {
        if (this.mButtonCheckTimer != null) {
            this.mButtonCheckTimer.cancel();
            this.mButtonCheckTimer.purge();
            this.mButtonCheckTimer = null;
        }
    }

    public void setDisplayTimeout() {
        if (!this.mInitial) {
        }
    }

    public void resetDisplayTimeout() {
        if (this.mBarAction == null) {
            setDisplayTimeout();
            return;
        }
        if (!this.mBarAction.isPaused()) {
            setDisplayTimeout();
        }
        if (this.mBarType == 2) {
            this.mBarAction.resetBarDisappearTimer(this.mBarType, 3000);
        }
    }

    public void setCursor(final int value) {
        if (this.mInitial && this.mBarAction != null) {
            this.mBarAction.runOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    try {
                        ImageView cursor = (ImageView) BarViewBase.this.findViewById(BarViewBase.this.cursorResId);
                        RelativeLayout cursorText = (RelativeLayout) BarViewBase.this.mBarAction.findViewById(BarViewBase.this.cursorTextId);
                        if (cursor != null && cursorText != null) {
                            LayoutParams param = (LayoutParams) cursor.getLayoutParams();
                            LayoutParams paramForText = (LayoutParams) cursorText.getLayoutParams();
                            if (param != null && paramForText != null) {
                                int marginDown;
                                int position;
                                switch (BarViewBase.this.mBarType) {
                                    case 1:
                                    case 4:
                                        int curLevel;
                                        if (Utils.isConfigureLandscape(BarViewBase.this.getResources())) {
                                            marginDown = (int) (((float) (BarViewBase.this.MAX_CURSOR_POS - BarViewBase.this.MIN_CURSOR_POS)) - BarViewBase.this.CURSOR_HEIGHT);
                                            curLevel = (int) (((float) value) * (((float) BarViewBase.this.CURSOR_POS_HEIGHT) / ((float) BarViewBase.this.mCursorMaxStep)));
                                        } else {
                                            marginDown = (int) (((float) (BarViewBase.this.MAX_CURSOR_POS_PORT - BarViewBase.this.MIN_CURSOR_POS)) - BarViewBase.this.CURSOR_HEIGHT_PORT);
                                            curLevel = (int) (((float) value) * (((float) BarViewBase.this.CURSOR_POS_HEIGHT_PORT) / ((float) BarViewBase.this.mCursorMaxStep)));
                                        }
                                        position = Math.max(Math.min(marginDown - curLevel, Utils.isConfigureLandscape(BarViewBase.this.getResources()) ? BarViewBase.this.MAX_CURSOR_POS : BarViewBase.this.MAX_CURSOR_POS_PORT), BarViewBase.this.MIN_CURSOR_POS);
                                        param.topMargin = position;
                                        paramForText.topMargin = position - ((cursorText.getHeight() - cursor.getHeight()) / 2);
                                        break;
                                    case 2:
                                        int tmpCursorPosHeight;
                                        int tmpCursorHeight = (int) (Utils.isConfigureLandscape(BarViewBase.this.getResources()) ? BarViewBase.this.CURSOR_HEIGHT : BarViewBase.this.CURSOR_HEIGHT_PORT);
                                        if (Utils.isConfigureLandscape(BarViewBase.this.getResources())) {
                                            tmpCursorPosHeight = BarViewBase.this.CURSOR_POS_HEIGHT;
                                        } else {
                                            tmpCursorPosHeight = BarViewBase.this.CURSOR_POS_HEIGHT_PORT;
                                        }
                                        marginDown = (tmpCursorHeight + tmpCursorPosHeight) - tmpCursorHeight;
                                        position = Math.max(Math.min(marginDown - ((int) (((float) value) * (((float) (tmpCursorPosHeight - tmpCursorHeight)) / ((float) BarViewBase.this.mCursorMaxStep)))), marginDown), BarViewBase.this.MIN_CURSOR_POS);
                                        param.topMargin = position;
                                        paramForText.topMargin = position - ((cursorText.getHeight() - cursor.getHeight()) / 2);
                                        break;
                                }
                                cursor.setLayoutParams(param);
                                cursorText.setLayoutParams(paramForText);
                                BarViewBase.this.setProgress(BarViewBase.this.mValue);
                            }
                        }
                    } catch (ArithmeticException e) {
                        CamLog.m3d(CameraConstants.TAG, "getCursorMaxStep() = " + BarViewBase.this.mCursorMaxStep);
                        CamLog.m6e(CameraConstants.TAG, "ArithmeticException:", e);
                    }
                }
            });
        }
    }

    protected boolean isPlusButton(View v) {
        return v.getId() == this.plusButtonViewResId;
    }

    public boolean isTouchUpAreaOfButton(View v, MotionEvent event) {
        if (event.getX() <= ((float) (-this.RELEASE_EXPAND_LEFT)) || event.getX() >= ((float) (v.getWidth() + this.RELEASE_EXPAND_RIGHT)) || event.getY() <= ((float) (-this.RELEASE_EXPAND_TOP)) || event.getY() >= ((float) (v.getWidth() + this.RELEASE_EXPAND_BOTTOM))) {
            return false;
        }
        return true;
    }

    private boolean isMovableArea(View view, MotionEvent event) {
        if (view == null || event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        if (0.0f >= x || x >= ((float) view.getWidth()) || 0.0f >= y || y >= ((float) view.getHeight())) {
            return false;
        }
        return true;
    }

    public void startRotation(int degree, boolean animation) {
        RotateLayout rl = getBarLayout();
        if (rl != null) {
            rl.rotateLayout(degree);
            rl.requestLayout();
            rl.invalidate();
            View parent = getBarParentLayout();
            if (parent != null) {
                parent.setLayoutParams((LayoutParams) parent.getLayoutParams());
            }
        }
    }

    public void setBarSettingValue(String key, int value, boolean save) {
        if (!"none".equals(key) && this.mBarAction != null) {
            this.mBarAction.setBarSetting(key, Integer.toString(value), save);
        }
    }

    public int getCursorValue() {
        return this.mValue;
    }

    public void setCursorValue(int value) {
        this.mValue = value;
    }

    public void setMaxValue(int maxValue) {
        this.mCursorMaxStep = maxValue;
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mBarAction != null) {
            this.mBarAction.removePostRunnable(runnable);
        }
    }

    public boolean isBarTouched() {
        return this.mBarTouchState;
    }

    public void setBarType(int barType) {
        this.mBarType = barType;
    }

    public void setBarSettingKey(String barKey) {
        CamLog.m11w(CameraConstants.TAG, "[relighting] setBarSettingKey " + barKey);
        this.mBarSettingKey = barKey;
    }
}
