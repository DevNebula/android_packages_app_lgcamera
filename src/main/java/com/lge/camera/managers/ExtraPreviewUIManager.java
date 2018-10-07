package com.lge.camera.managers;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ColorFilter;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.ValueSeekBar;
import com.lge.camera.components.ValueSeekBar.OnValueSeekBarChangeListener;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.PackageUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;

public class ExtraPreviewUIManager extends ManagerInterfaceImpl {
    private static final int DEFAULT_STRENGTH_VALUE = 100;
    public static final int EXTRA_BTN_TYPE_FILTER = 0;
    public static final int EXTRA_BTN_TYPE_NONE = -1;
    public static final int EXTRA_BTN_TYPE_RELIGHTING = 3;
    public static final int EXTRA_BTN_TYPE_SKIN_TONE = 2;
    public static final int EXTRA_BTN_TYPE_STICKER = 1;
    public static final int FILTER_STRENGTH_BAR = 2;
    private LinearLayout mBottomLayout;
    private int mCurrentFilmStrength;
    private RotateImageButton mDownloadButton;
    private RotateImageButton mEditButton;
    private RelativeLayout mExtraPreviewUIView;
    private RotateImageButton mFilterButton;
    private ValueSeekBar mFilterStrengthBar;
    protected boolean mIsDownloadBtnClicked = false;
    private int mLastSelectedMenu = 0;
    private ExtraPreviewUIInterface mListener;
    private RotateImageButton mRelightingButton;
    private RotateImageButton mSkinToneButton;
    private RotateImageButton mStickerButton;
    private OnValueSeekBarChangeListener mStrengthBarChangeListener = new C08901();
    private LinearLayout mTopLayout;

    public interface ExtraPreviewUIInterface {
        boolean onDownloadClicked();

        boolean onEditClicked();

        boolean onFilterMenuClicked();

        boolean onRelightingMenuClicked();

        boolean onSkinToneMenuClicked();

        boolean onStickerMenuClicked();
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$10 */
    class C088810 implements AnimationListener {
        C088810() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (ExtraPreviewUIManager.this.mTopLayout != null) {
                ExtraPreviewUIManager.this.mTopLayout.setVisibility(8);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$11 */
    class C088911 implements AnimationListener {
        C088911() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (ExtraPreviewUIManager.this.mBottomLayout != null) {
                ExtraPreviewUIManager.this.mBottomLayout.setVisibility(8);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$1 */
    class C08901 implements OnValueSeekBarChangeListener {
        C08901() {
        }

        public void onProgressChanged(ValueSeekBar valueSeekBar, int progress, boolean fromUser, boolean valueChanged) {
            if (valueSeekBar != null && ExtraPreviewUIManager.this.mFilterStrengthBar != null) {
                valueSeekBar.setTextVisibility(0);
                valueSeekBar.removeTextDelayed(1500);
                if (valueSeekBar.getType() == 2 && valueChanged && ExtraPreviewUIManager.this.mGet != null) {
                    ExtraPreviewUIManager.this.mCurrentFilmStrength = progress;
                    ExtraPreviewUIManager.this.mGet.setFilmStrength(((float) progress) / 100.0f);
                }
            }
        }

        public void onStartTrackingTouch(ValueSeekBar valueSeekBar) {
        }

        public void onStopTrackingTouch(ValueSeekBar valueSeekBar) {
            if (ExtraPreviewUIManager.this.mGet != null) {
                CamLog.m3d(CameraConstants.TAG, "[Strength] onStopTrackingTouch. isRearCamera ? " + ExtraPreviewUIManager.this.mGet.isRearCamera() + ", mCurrentFilmStrength : " + ExtraPreviewUIManager.this.mCurrentFilmStrength);
                if (ExtraPreviewUIManager.this.mGet.isRearCamera()) {
                    SharedPreferenceUtilBase.saveRearFilterStrength(ExtraPreviewUIManager.this.getAppContext(), ExtraPreviewUIManager.this.mCurrentFilmStrength);
                } else {
                    SharedPreferenceUtilBase.saveFrontFilterStrength(ExtraPreviewUIManager.this.getAppContext(), ExtraPreviewUIManager.this.mCurrentFilmStrength);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$2 */
    class C08912 implements OnClickListener {
        C08912() {
        }

        public void onClick(View button) {
            boolean z = true;
            if (ExtraPreviewUIManager.this.checkExtraPreviewButton() && ExtraPreviewUIManager.this.mListener.onFilterMenuClicked()) {
                ExtraPreviewUIManager.this.mLastSelectedMenu = 0;
                if (!CameraConstants.FILM_NONE.equals(ExtraPreviewUIManager.this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                    ExtraPreviewUIManager.this.setStrengthBarVisibility(true, true);
                }
                ExtraPreviewUIManager extraPreviewUIManager = ExtraPreviewUIManager.this;
                if (CameraConstants.FILM_NONE.equals(ExtraPreviewUIManager.this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) {
                    z = false;
                }
                extraPreviewUIManager.changeButtonState(0, z);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$3 */
    class C08923 implements OnClickListener {
        C08923() {
        }

        public void onClick(View button) {
            if (ExtraPreviewUIManager.this.checkExtraPreviewButton() && ExtraPreviewUIManager.this.mListener.onStickerMenuClicked()) {
                boolean z;
                ExtraPreviewUIManager.this.mLastSelectedMenu = 1;
                ExtraPreviewUIManager extraPreviewUIManager = ExtraPreviewUIManager.this;
                if (ExtraPreviewUIManager.this.mGet.isStickerSelected() || ExtraPreviewUIManager.this.mGet.isMenuShowing(16)) {
                    z = true;
                } else {
                    z = false;
                }
                extraPreviewUIManager.changeButtonState(1, z);
                ExtraPreviewUIManager.this.setStrengthBarVisibility(false, true);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$4 */
    class C08934 implements OnClickListener {
        C08934() {
        }

        public void onClick(View button) {
            if (ExtraPreviewUIManager.this.checkExtraPreviewButton() && ExtraPreviewUIManager.this.mListener.onSkinToneMenuClicked()) {
                ExtraPreviewUIManager.this.hide(false, true, false);
                ExtraPreviewUIManager.this.setStrengthBarVisibility(false, true);
                ExtraPreviewUIManager.this.changeButtonState(2, ExtraPreviewUIManager.this.mGet.isBarVisible(1));
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$5 */
    class C08945 implements OnClickListener {
        C08945() {
        }

        public void onClick(View button) {
            if (ExtraPreviewUIManager.this.checkExtraPreviewButton() && ExtraPreviewUIManager.this.mListener.onRelightingMenuClicked()) {
                ExtraPreviewUIManager.this.hide(false, true, false);
                ExtraPreviewUIManager.this.setStrengthBarVisibility(false, true);
                ExtraPreviewUIManager.this.changeButtonState(3, ExtraPreviewUIManager.this.mGet.isBarVisible(4));
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$6 */
    class C08956 implements OnClickListener {
        C08956() {
        }

        public void onClick(View arg0) {
            if (!ExtraPreviewUIManager.this.checkExtraPreviewButton() || ExtraPreviewUIManager.this.mListener.onEditClicked()) {
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$7 */
    class C08967 implements OnClickListener {
        C08967() {
        }

        public void onClick(View arg0) {
            if (!PackageUtil.isLGSmartWorldInstalled(ExtraPreviewUIManager.this.mGet.getAppContext())) {
                new SmartWorldCheckTask(ExtraPreviewUIManager.this, null).execute(new Void[0]);
            } else if (!PackageUtil.isLGSmartWorldEnabled(ExtraPreviewUIManager.this.mGet.getAppContext())) {
                ExtraPreviewUIManager.this.mGet.showToast(ExtraPreviewUIManager.this.mGet.getAppContext().getString(C0088R.string.error_not_exist_app), CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } else if (!ExtraPreviewUIManager.this.mIsDownloadBtnClicked) {
                ExtraPreviewUIManager.this.mIsDownloadBtnClicked = true;
                if (ExtraPreviewUIManager.this.checkExtraPreviewButton() && ExtraPreviewUIManager.this.mListener.onDownloadClicked()) {
                    ExtraPreviewUIManager.this.setStrengthBarVisibility(false, true);
                }
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$8 */
    class C08978 implements AnimationListener {
        C08978() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (ExtraPreviewUIManager.this.mTopLayout != null) {
                ExtraPreviewUIManager.this.mTopLayout.setVisibility(0);
            }
        }
    }

    /* renamed from: com.lge.camera.managers.ExtraPreviewUIManager$9 */
    class C08989 implements AnimationListener {
        C08989() {
        }

        public void onAnimationStart(Animation arg0) {
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            if (ExtraPreviewUIManager.this.mBottomLayout != null) {
                ExtraPreviewUIManager.this.mBottomLayout.setVisibility(0);
            }
        }
    }

    private class SmartWorldCheckTask extends AsyncTask<Void, Void, Void> {
        private SmartWorldCheckTask() {
        }

        /* synthetic */ SmartWorldCheckTask(ExtraPreviewUIManager x0, C08901 x1) {
            this();
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        protected Void doInBackground(Void... params) {
            PackageUtil.checkLGSmartWorldUpdated(ExtraPreviewUIManager.this.mGet.getAppContext());
            return null;
        }
    }

    public void setExtraPreviewUIListener(ExtraPreviewUIInterface listener) {
        CamLog.m3d(CameraConstants.TAG, "setExtraPreviewUIListener");
        this.mListener = listener;
    }

    public ExtraPreviewUIManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        releaseViews();
        setupViews();
    }

    public void init() {
        if (this.mGet != null) {
            int strength = this.mGet.isRearCamera() ? SharedPreferenceUtilBase.getRearFilterStrength(getAppContext()) : SharedPreferenceUtilBase.getFrontFilterStrength(getAppContext());
            CamLog.m3d(CameraConstants.TAG, "[Strength] init. isRearCamera ? : " + this.mGet.isRearCamera() + ", rear : " + SharedPreferenceUtilBase.getRearFilterStrength(getAppContext()) + ", front : " + SharedPreferenceUtilBase.getFrontFilterStrength(getAppContext()) + ", (float)(strength) / 100 ? " + (((float) strength) / 100.0f));
            this.mGet.setFilmStrength(((float) strength) / 100.0f);
        }
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        setupViews();
        this.mIsDownloadBtnClicked = false;
    }

    private boolean isFilterDownloadSupported() {
        return ModelProperties.getCarrierCode() != 6 && ModelProperties.getRawAppTier() >= 5 && !ModelProperties.isJoanRenewal() && PackageUtil.isLGSmartWorldPreloaded();
    }

    private boolean isStickerDownloadSupported() {
        return (ModelProperties.getCarrierCode() == 6 || ModelProperties.isJoanRenewal() || !PackageUtil.isLGSmartWorldInstalled(getAppContext())) ? false : true;
    }

    private boolean checkNeedExtrePreviewUI() {
        if (this.mGet.isRearCamera() || (!CameraConstants.MODE_SNAP.equals(this.mGet.getShotMode()) && !CameraConstants.MODE_FRONT_OUTFOCUS.equals(this.mGet.getShotMode()))) {
            return true;
        }
        return false;
    }

    private boolean setupViews() {
        if (!checkNeedExtrePreviewUI() || this.mExtraPreviewUIView != null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "[extra] setupView");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mExtraPreviewUIView = (RelativeLayout) this.mGet.inflateView(C0088R.layout.extra_preview_ui);
        if (vg == null || this.mExtraPreviewUIView == null) {
            return false;
        }
        vg.addView(this.mExtraPreviewUIView, 0);
        this.mEditButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.edit_button);
        this.mDownloadButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.download_button);
        if (!isFilterDownloadSupported()) {
            this.mDownloadButton.setVisibility(8);
            this.mEditButton.setVisibility(8);
        }
        this.mFilterButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.filter_menu_button);
        this.mStickerButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.sticker_menu_button);
        if (!FunctionProperties.isSupportedSticker() || (!"mode_normal".equals(this.mGet.getShotMode()) && !this.mGet.getShotMode().contains(CameraConstants.MODE_BEAUTY))) {
            this.mExtraPreviewUIView.findViewById(C0088R.id.sticker_menu_button_view).setVisibility(8);
            if (this.mGet.isRearCamera() || CameraConstants.MODE_SMART_CAM_FRONT.equals(this.mGet.getShotMode()) || !((FunctionProperties.isSupportedFilmEmulator() || this.mGet.isColorEffectSupported()) && (FunctionProperties.isSupportedFilmRecording() || this.mGet.isRearCamera() || this.mGet.getSettingMenuEnable(Setting.KEY_FILM_EMULATOR)))) {
                if (this.mGet.isRearCamera() || !((FunctionProperties.isSupportedFilmEmulator() || this.mGet.isColorEffectSupported()) && this.mGet.isAttachIntent())) {
                    this.mExtraPreviewUIView.findViewById(C0088R.id.filter_menu_button_view).setVisibility(8);
                } else {
                    CamLog.m3d(CameraConstants.TAG, "set Film button visible at attach mode. because film or colorEffect is supported.");
                }
            }
        } else if (FunctionProperties.isSupportedSticker()) {
            this.mExtraPreviewUIView.findViewById(C0088R.id.sticker_menu_button_view).setVisibility(0);
            if (this.mGet.isStickerSelected()) {
                if (isStickerDownloadSupported()) {
                    this.mDownloadButton.setVisibility(0);
                    this.mEditButton.setVisibility(0);
                }
                this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_selected);
            } else {
                this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_button);
            }
        }
        this.mSkinToneButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.skin_tone_button);
        if (!FunctionProperties.isSupportedBeautyShot() || this.mGet.isRearCamera()) {
            this.mExtraPreviewUIView.findViewById(C0088R.id.skin_tone_button_view).setVisibility(8);
        }
        this.mRelightingButton = (RotateImageButton) this.mExtraPreviewUIView.findViewById(C0088R.id.relighting_menu_button);
        if (!FunctionProperties.isSupportedRelighting() || this.mGet.isRearCamera()) {
            this.mExtraPreviewUIView.findViewById(C0088R.id.relighting_menu_button_view).setVisibility(8);
        }
        setButtonListener();
        if (!FunctionProperties.isSupportedFilmEmulator()) {
            changeButtonState(0, false);
        }
        this.mBottomLayout = (LinearLayout) this.mExtraPreviewUIView.findViewById(C0088R.id.extra_preview_ui_bottom_menu_layout);
        this.mTopLayout = (LinearLayout) this.mExtraPreviewUIView.findViewById(C0088R.id.extra_preview_ui_top_menu_layout);
        int degree = getOrientationDegree();
        if (FunctionProperties.isFilmStrengthSupported()) {
            initStrengthBarUI();
            setStrengthBarLayout(degree);
        }
        setStrengthBarVisibility(false, false);
        if (this.mGet.isRearCamera()) {
            this.mTopLayout.setVisibility(8);
            this.mBottomLayout.setVisibility(8);
        } else {
            int i;
            this.mTopLayout.setVisibility(8);
            LinearLayout linearLayout = this.mBottomLayout;
            if (this.mGet.isModuleChanging()) {
                i = 8;
            } else {
                i = 0;
            }
            linearLayout.setVisibility(i);
        }
        this.mExtraPreviewUIView.setVisibility(0);
        setDegree(degree, false);
        if (!(this.mGet.isRearCamera() || this.mExtraPreviewUIView.findViewById(C0088R.id.filter_menu_button_view).getVisibility() == 0)) {
            LayoutParams skinViewParam;
            if (this.mExtraPreviewUIView.findViewById(C0088R.id.relighting_menu_button_view).getVisibility() != 0) {
                RelativeLayout skinView = (RelativeLayout) this.mExtraPreviewUIView.findViewById(C0088R.id.skin_tone_button_view);
                skinViewParam = (LayoutParams) skinView.getLayoutParams();
                skinViewParam.setMarginEnd(0);
                skinView.setLayoutParams(skinViewParam);
            } else {
                RelativeLayout relightingView = (RelativeLayout) this.mExtraPreviewUIView.findViewById(C0088R.id.relighting_menu_button_view);
                skinViewParam = (LayoutParams) relightingView.getLayoutParams();
                skinViewParam.setMarginEnd(0);
                relightingView.setLayoutParams(skinViewParam);
            }
        }
        return true;
    }

    private void initStrengthBarUI() {
        if (this.mExtraPreviewUIView != null) {
            this.mFilterStrengthBar = new ValueSeekBar(getAppContext(), 2, this.mExtraPreviewUIView);
            this.mFilterStrengthBar.setProgress(100);
            this.mFilterStrengthBar.setSeekBarDescription(getAppContext().getString(C0088R.string.strength));
            this.mFilterStrengthBar.setLabel(getAppContext().getString(C0088R.string.strength));
            this.mFilterStrengthBar.setOnValueSeekBarChangeListener(this.mStrengthBarChangeListener);
            this.mFilterStrengthBar.setRotation(getOrientationDegree());
        }
    }

    public void onPauseBefore() {
        super.onPauseBefore();
        releaseViews();
    }

    private void releaseViews() {
        CamLog.m3d(CameraConstants.TAG, "[extra] releaseView");
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        if (!(vg == null || this.mExtraPreviewUIView == null)) {
            this.mExtraPreviewUIView.setVisibility(8);
            this.mExtraPreviewUIView.removeAllViews();
            vg.removeView(this.mExtraPreviewUIView);
        }
        if (this.mFilterButton != null) {
            this.mFilterButton.setOnClickListener(null);
            this.mFilterButton = null;
        }
        if (this.mStickerButton != null) {
            this.mStickerButton.setOnClickListener(null);
            this.mStickerButton = null;
        }
        if (this.mSkinToneButton != null) {
            this.mSkinToneButton.setOnClickListener(null);
        }
        if (this.mRelightingButton != null) {
            this.mRelightingButton.setOnClickListener(null);
        }
        if (this.mDownloadButton != null) {
            this.mDownloadButton.setOnClickListener(null);
            this.mDownloadButton = null;
        }
        if (this.mEditButton != null) {
            this.mEditButton.setOnClickListener(null);
            this.mEditButton = null;
        }
        if (this.mFilterStrengthBar != null) {
            this.mFilterStrengthBar.setOnValueSeekBarChangeListener(null);
            this.mFilterStrengthBar = null;
        }
        this.mBottomLayout = null;
        this.mExtraPreviewUIView = null;
    }

    public void setButtonListener() {
        this.mFilterButton.setOnClickListener(new C08912());
        this.mStickerButton.setOnClickListener(new C08923());
        this.mSkinToneButton.setOnClickListener(new C08934());
        this.mRelightingButton.setOnClickListener(new C08945());
        this.mEditButton.setOnClickListener(new C08956());
        this.mDownloadButton.setOnClickListener(new C08967());
    }

    private boolean checkExtraPreviewButton() {
        if (this.mListener == null || this.mGet.isTimerShotCountdown()) {
            return false;
        }
        return true;
    }

    public void setDegree(int degree, boolean animation) {
        super.setDegree(degree, animation);
        if (this.mFilterButton != null && this.mStickerButton != null && this.mEditButton != null && this.mDownloadButton != null && this.mSkinToneButton != null) {
            boolean z;
            this.mFilterButton.setDegree(degree, animation);
            this.mStickerButton.setDegree(degree, animation);
            this.mEditButton.setDegree(degree, animation);
            this.mDownloadButton.setDegree(degree, animation);
            this.mSkinToneButton.setDegree(degree, animation);
            this.mRelightingButton.setDegree(degree, animation);
            if (this.mGet.isRearCamera() || this.mTopLayout.getVisibility() == 0) {
                z = false;
            } else {
                z = true;
            }
            setBottomMenuLayout(0, z);
            setTopMenuLayout(degree);
            setStrengthBarLayout(degree);
            if (this.mFilterStrengthBar != null) {
                this.mFilterStrengthBar.setRotation(degree);
            }
        }
    }

    private void setTopMenuLayout(int degree) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mTopLayout.getLayoutParams();
        Utils.resetLayoutParameter(params);
        if (null == null) {
            this.mTopLayout.setLayoutDirection(1);
            this.mTopLayout.setGravity(8388659);
            int topMargin = RatioCalcUtil.getQuickButtonWidth(getAppContext());
            if ("on".equals(this.mGet.getSettingValue(Setting.KEY_TILE_PREVIEW))) {
                topMargin += RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.11112f);
            }
            this.mTopLayout.setPaddingRelative(0, topMargin, 0, 0);
            params.addRule(10);
        } else if (0 == 90) {
            this.mTopLayout.setLayoutDirection(1);
            this.mTopLayout.setGravity(8388661);
            this.mTopLayout.setPaddingRelative(0, 0, 0, 0);
            params.addRule(10);
        } else if (0 == 180) {
            this.mTopLayout.setLayoutDirection(0);
            this.mTopLayout.setGravity(8388691);
            this.mTopLayout.setPaddingRelative(0, 0, 0, (int) Utils.dpToPx(getAppContext(), 62.0f));
            params.addRule(12);
        } else {
            this.mTopLayout.setLayoutDirection(0);
            this.mTopLayout.setGravity(8388693);
            this.mTopLayout.setPaddingRelative(0, 0, 0, (int) Utils.dpToPx(getAppContext(), 62.0f));
            params.addRule(12);
        }
        this.mTopLayout.setLayoutParams(params);
    }

    private void setBottomMenuLayout(int degree, boolean isPreview) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.mBottomLayout.getLayoutParams();
        Utils.resetLayoutParameter(params);
        if (degree == 0 || degree == 90) {
            int bottomMargin;
            if (isPreview) {
                if (ModelProperties.getLCDType() == 2) {
                    bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.313f);
                } else if (ModelProperties.getLCDType() == 1) {
                    bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.289f);
                } else {
                    bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.286f);
                }
                if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    bottomMargin = Utils.getLCDsize(getAppContext(), true)[1] + ((int) Utils.dpToPx(getAppContext(), 6.0f));
                }
                params.bottomMargin = bottomMargin;
            } else {
                if (ModelProperties.getLCDType() == 2) {
                    bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.361f);
                } else {
                    bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.344f);
                }
                if (this.mGet.getShotMode().contains(CameraConstants.MODE_SQUARE)) {
                    if (ModelProperties.getLCDType() == 2) {
                        bottomMargin = (Utils.getLCDsize(getAppContext(), true)[1] + ((int) Utils.dpToPx(getAppContext(), 6.0f))) + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.123f);
                    } else {
                        bottomMargin = (Utils.getLCDsize(getAppContext(), true)[1] + ((int) Utils.dpToPx(getAppContext(), 6.0f))) + RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.133f);
                    }
                }
                params.bottomMargin = bottomMargin;
            }
            params.addRule(12);
        } else {
            params.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(getAppContext(), true, 0.265f);
            params.addRule(10);
        }
        this.mBottomLayout.setLayoutParams(params);
    }

    private void setStrengthBarLayout(int degree) {
        if (this.mFilterStrengthBar != null) {
            ValueSeekBar valueSeekBar = this.mFilterStrengthBar;
            Context appContext = getAppContext();
            String shotMode = this.mGet.getShotMode();
            boolean z = !this.mGet.isRearCamera() || FunctionProperties.isSupportedSticker();
            valueSeekBar.setFilterStrengthBarLayout(degree, appContext, shotMode, z);
        }
    }

    public void setStrengthBarVisibility(boolean isNeedVisible, boolean useAnim) {
        if (this.mFilterStrengthBar != null) {
            this.mFilterStrengthBar.setProgress(this.mGet == null ? 100 : this.mGet.getFilmStrengthValue());
            this.mFilterStrengthBar.setVisibility(isNeedVisible ? 0 : 4);
        }
    }

    public void show(boolean useAnim, boolean top, boolean bottom) {
        if (this.mExtraPreviewUIView != null && this.mTopLayout != null && this.mBottomLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "Show extraPreviewButtons, useAnim : " + useAnim + ", top : " + top + ", bottom : " + bottom);
            setEditMode(false);
            this.mExtraPreviewUIView.setVisibility(0);
            setTopMenuLayout(0);
            boolean z = (top || !bottom || this.mGet.isRearCamera()) ? false : true;
            setBottomMenuLayout(0, z);
            if (top) {
                if (useAnim) {
                    AnimationUtil.startAlphaAnimation(this.mTopLayout, 0.0f, 1.0f, 300, new C08978());
                } else {
                    this.mTopLayout.setVisibility(0);
                }
            }
            if (!bottom) {
                return;
            }
            if (useAnim) {
                AnimationUtil.startAlphaAnimation(this.mBottomLayout, 0.0f, 1.0f, 300, new C08989());
            } else {
                this.mBottomLayout.setVisibility(0);
            }
        }
    }

    public void hide(boolean useAnim, boolean top, boolean bottom) {
        if (this.mExtraPreviewUIView != null && this.mTopLayout != null && this.mBottomLayout != null) {
            CamLog.m3d(CameraConstants.TAG, "Hide extraPreviewButtons, useAnim : " + useAnim + ", top : " + top + ", bottom : " + bottom);
            if (!bottom) {
                boolean z = (this.mGet.isRearCamera() || !top || bottom) ? false : true;
                setBottomMenuLayout(0, z);
            }
            if (top) {
                if (useAnim) {
                    AnimationUtil.startAlphaAnimation(this.mTopLayout, 1.0f, 0.0f, 300, new C088810());
                } else if (this.mTopLayout != null) {
                    this.mTopLayout.setVisibility(8);
                }
            }
            if (!bottom || this.mBottomLayout.getVisibility() != 0) {
                return;
            }
            if (useAnim) {
                AnimationUtil.startAlphaAnimation(this.mBottomLayout, 1.0f, 0.0f, 300, new C088911());
            } else if (this.mBottomLayout != null) {
                this.mBottomLayout.setVisibility(8);
            }
        }
    }

    public int getLastSelectedMenu() {
        return this.mLastSelectedMenu;
    }

    public void setLastSelectedMenu(int menuValue) {
        this.mLastSelectedMenu = menuValue;
        CamLog.m3d(CameraConstants.TAG, "mLastSelectedMenu : " + this.mLastSelectedMenu);
    }

    public void changeButtonState(int menuType, boolean isOn) {
        if (this.mFilterButton != null && this.mStickerButton != null) {
            if (menuType == 1 && isStickerDownloadSupported()) {
                this.mDownloadButton.setVisibility(0);
                this.mEditButton.setVisibility(0);
            } else if (!isFilterDownloadSupported()) {
                this.mDownloadButton.setVisibility(8);
                this.mEditButton.setVisibility(8);
            }
            if (menuType == 0) {
                if (isOn) {
                    this.mFilterButton.setBackgroundResource(C0088R.drawable.btn_filter_filter_pressed);
                    this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_button);
                    this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_skin_tone_button);
                    this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_lighting_button);
                } else {
                    changeFilterButtonState();
                    this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_button);
                    if (!this.mGet.isBarVisible(1)) {
                        this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_skin_tone_button);
                    }
                    if (!this.mGet.isBarVisible(4)) {
                        this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_lighting_button);
                    }
                }
            } else if (menuType == 1) {
                if (isOn) {
                    this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_selected);
                    changeFilterButtonState();
                    this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_skin_tone_button);
                    this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_lighting_button);
                } else {
                    this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_button);
                }
            } else if (menuType == 2) {
                if (isOn) {
                    this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_selfie_tone_pressed);
                    changeFilterButtonState();
                    this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_lighting_button);
                } else {
                    this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_skin_tone_button);
                }
            } else if (menuType == 3) {
                if (isOn) {
                    this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_selfie_relight_pressed);
                    changeFilterButtonState();
                    this.mStickerButton.setBackgroundResource(C0088R.drawable.btn_filter_sticker_button);
                    this.mSkinToneButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_skin_tone_button);
                } else {
                    this.mRelightingButton.setBackgroundResource(C0088R.drawable.btn_advanced_selfie_lighting_button);
                }
            }
            if (this.mGet.isVideoCaptureMode() && (this.mGet.isMMSIntent() || !FunctionProperties.isSupportedBeautyRecording())) {
                setMenuButtonEnable(2, false);
                setMenuButtonEnable(3, false);
            }
            if (!this.mGet.isRearCamera() && this.mGet.isVideoCaptureMode() && !FunctionProperties.isSupportedFilmRecording()) {
                setMenuButtonEnable(0, false);
            }
        }
    }

    private void changeFilterButtonState() {
        if (!(FunctionProperties.isSupportedFilmEmulator() && CameraConstants.FILM_NONE.equals(this.mGet.getSettingValue(Setting.KEY_FILM_EMULATOR))) && (FunctionProperties.isSupportedFilmEmulator() || !"none".equals(this.mGet.getSettingValue(Setting.KEY_COLOR_EFFECT)))) {
            this.mFilterButton.setBackgroundResource(C0088R.drawable.btn_filter_filter_on);
        } else {
            this.mFilterButton.setBackgroundResource(C0088R.drawable.btn_filter_filter_off);
        }
    }

    public void setMenuButtonEnable(int type, boolean enable) {
        if (this.mExtraPreviewUIView != null) {
            if (this.mGet.isStickerIconDisableCondition() && type == 1) {
                enable = false;
            }
            RotateImageButton menuButton = getMenuButton(type);
            ColorFilter cf = enable ? ColorUtil.getNormalColorByAlpha() : ColorUtil.getDimColorByAlpha();
            if (menuButton != null) {
                menuButton.setEnabled(enable);
                menuButton.setColorFilter(cf);
                menuButton.getBackground().setColorFilter(cf);
            }
        }
    }

    private RotateImageButton getMenuButton(int type) {
        switch (type) {
            case 0:
                return this.mFilterButton;
            case 1:
                return this.mStickerButton;
            case 2:
                return this.mSkinToneButton;
            case 3:
                return this.mRelightingButton;
            default:
                return null;
        }
    }

    public void setAllButtonsEnable(boolean enable) {
        boolean z;
        boolean z2 = true;
        if (enable && (this.mGet.isRearCamera() || !this.mGet.isVideoCaptureMode() || FunctionProperties.isSupportedFilmRecording())) {
            z = true;
        } else {
            z = false;
        }
        setMenuButtonEnable(0, z);
        setMenuButtonEnable(1, enable);
        if (!enable || (this.mGet.isVideoCaptureMode() && (!FunctionProperties.isSupportedBeautyRecording() || this.mGet.isMMSIntent()))) {
            z = false;
        } else {
            z = true;
        }
        setMenuButtonEnable(2, z);
        if (!enable || (this.mGet.isVideoCaptureMode() && (!FunctionProperties.isSupportedBeautyRecording() || this.mGet.isMMSIntent()))) {
            z2 = false;
        }
        setMenuButtonEnable(3, z2);
    }

    public void setEditMode(boolean mIsEditMode) {
        if (this.mEditButton != null) {
            if (mIsEditMode) {
                this.mEditButton.setImageResource(C0088R.drawable.btn_quickbutton_edit_pressed);
            } else {
                this.mEditButton.setImageResource(C0088R.drawable.btn_quickbutton_edit);
            }
        }
    }

    public void setEditDim(boolean dim) {
        if (this.mEditButton != null) {
            if (dim) {
                this.mEditButton.setEnabled(false);
                this.mEditButton.setAlpha(0.35f);
                return;
            }
            this.mEditButton.setEnabled(true);
            this.mEditButton.setAlpha(1.0f);
        }
    }

    public void setEnable(int type, boolean enable) {
        CamLog.m3d(CameraConstants.TAG, "setEnable, type = " + type + ", enable = " + enable);
        View button = null;
        switch (type) {
            case 0:
                button = this.mFilterButton;
                break;
            case 1:
                button = this.mStickerButton;
                break;
            case 2:
                button = this.mSkinToneButton;
                break;
            case 3:
                button = this.mRelightingButton;
                break;
        }
        if (button != null) {
            button.setEnabled(enable);
        }
    }
}
