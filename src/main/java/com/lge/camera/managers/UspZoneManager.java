package com.lge.camera.managers;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.google.lens.sdk.LensApi;
import com.google.lens.sdk.LensApi.LensAvailabilityCallback;
import com.lge.camera.C0088R;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AppControlUtilBase;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.camera.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;

public class UspZoneManager {
    protected final float USP_BOTTOM_MARGIN = 0.224f;
    protected final float USP_BOTTOM_MARGIN_FULLVISION = 0.227f;
    protected final float USP_BOTTOM_MARGIN_FULLVISION_LONG = 0.253f;
    protected final float USP_START_MARGIN_FOR_THREE_ITEM = 0.0528f;
    protected final float USP_START_MARGIN_FOR_TWO_ITEM = 0.233f;
    protected final float USP_TEXT_SIZE = 0.0361f;
    private String mBackupUspMode = "";
    private UspZoneInterface mGet;
    private int mGoogleLensStatus = -1;
    private boolean mIsPuasedOnSecureCamera = false;
    private boolean mIsReadyToShowUI = true;
    private boolean mIsRearCamera;
    private LensApi mLensApi;
    private int mSelectedIndex = -1;
    private int mStartMarginDp = 0;
    private ArrayList<UspItem> mUspArrayList;
    private View mUspBaseView;
    private RelativeLayout mUspRelativeLayout;
    private HashMap<String, Boolean> mUspSupportedMap = new HashMap();
    private int mUspTextSize = 0;

    /* renamed from: com.lge.camera.managers.UspZoneManager$1 */
    class C11851 implements LensAvailabilityCallback {
        C11851() {
        }

        public void onAvailabilityStatusFetched(int status) {
            CamLog.m3d(CameraConstants.TAG, "Google lens availability has changed to status " + status);
            UspZoneManager.this.mGoogleLensStatus = status;
            UspZoneManager.this.refreshUspZone(false);
        }
    }

    /* renamed from: com.lge.camera.managers.UspZoneManager$2 */
    class C11862 implements OnClickListener {
        C11862() {
        }

        public void onClick(View view) {
            if (UspZoneManager.this.mGet == null || UspZoneManager.this.mGet.isModuleChanging() || UspZoneManager.this.mGet.isPaused() || !UspZoneManager.this.mGet.checkModuleValidate(207) || !UspZoneManager.this.mGet.checkUSPZoneAvailable()) {
                CamLog.m3d(CameraConstants.TAG, "[usp] click return");
                return;
            }
            int position = ((Integer) view.getTag()).intValue();
            String newUspValue = ((UspItem) UspZoneManager.this.mUspArrayList.get(position)).getValue();
            if (UspZoneManager.this.mGet.isScreenPinningState() && (CameraConstantsEx.USP_LG_LENS.equals(newUspValue) || CameraConstantsEx.USP_GOOGLE_LENS.equals(newUspValue))) {
                UspZoneManager.this.mGet.showToast(UspZoneManager.this.mGet.getAppContext().getResources().getString(C0088R.string.lock_to_app_toast), CameraConstants.TOAST_LENGTH_LONG);
            } else {
                UspZoneManager.this.onUspItemClick(newUspValue, position, true);
            }
        }
    }

    public UspZoneManager(UspZoneInterface listener) {
        this.mGet = listener;
    }

    public void init() {
        initItemList();
        if (!SecureImageUtil.isSecureCamera()) {
            SharedPreferenceUtilBase.saveUspSetting(this.mGet.getAppContext(), CameraConstantsEx.USP_NORMAL);
        }
        this.mUspSupportedMap.put("mode_normal", Boolean.valueOf(true));
        this.mUspSupportedMap.put(CameraConstants.MODE_BEAUTY, Boolean.valueOf(true));
        this.mUspSupportedMap.put(CameraConstants.MODE_FRONT_OUTFOCUS, Boolean.valueOf(true));
        this.mUspSupportedMap.put(CameraConstants.MODE_REAR_OUTFOCUS, Boolean.valueOf(true));
        this.mUspSupportedMap.put(CameraConstants.MODE_SMART_CAM, Boolean.valueOf(true));
        this.mUspSupportedMap.put(CameraConstants.MODE_SMART_CAM_FRONT, Boolean.valueOf(true));
        this.mIsPuasedOnSecureCamera = false;
        this.mSelectedIndex = -1;
    }

    public boolean isUspZoneSupportedMode(String shotMode) {
        if (this.mUspSupportedMap == null) {
            return false;
        }
        String[] mode = shotMode.split("=");
        if (this.mUspSupportedMap.get(mode[0]) != null) {
            return ((Boolean) this.mUspSupportedMap.get(mode[0])).booleanValue();
        }
        return false;
    }

    public boolean checkUsingUSPBackground() {
        if (FunctionProperties.isSupportedGoogleLens() || FunctionProperties.isSupportedLGLens(this.mGet.getAppContext()) || FunctionProperties.isSupportedSmartCam(this.mGet.getAppContext())) {
            return false;
        }
        return true;
    }

    private void initItemList() {
        if (this.mUspArrayList == null) {
            this.mUspArrayList = new ArrayList();
            setLensItem();
            if (this.mLensApi == null && FunctionProperties.isSupportedGoogleLens()) {
                this.mLensApi = new LensApi(this.mGet.getAppContext());
                CamLog.m7i(CameraConstants.TAG, "Register LensAvailabilityCallback");
                this.mLensApi.checkLensAvailability(new C11851());
            }
            if (!ModelProperties.isFakeMode()) {
                if (FunctionProperties.isSupportedRearOutfocus() && this.mGet.isRearCamera()) {
                    this.mUspArrayList.add(new UspItem(C0088R.string.portrait_outfocus, CameraConstantsEx.USP_OUTFOCUS, C0088R.id.usp_zone_outfocus));
                } else if (FunctionProperties.isSupportedFrontOutfocus() && !this.mGet.isRearCamera()) {
                    this.mUspArrayList.add(new UspItem(C0088R.string.portrait_outfocus, CameraConstantsEx.USP_OUTFOCUS, C0088R.id.usp_zone_outfocus));
                }
            }
            if (FunctionProperties.isSupportedSmartCam(this.mGet.getAppContext())) {
                this.mUspArrayList.add(new UspItem(C0088R.string.ai_cam2, CameraConstantsEx.USP_SMART_CAM, C0088R.id.usp_zone_smart_cam));
            }
        }
    }

    private void setLensItem() {
        if (!FunctionProperties.isSupportedGoogleLens() || this.mGoogleLensStatus != -1) {
            if (FunctionProperties.isSupportedLGLens(this.mGet.getAppContext()) && FunctionProperties.isSupportedGoogleLens()) {
                CamLog.m3d(CameraConstants.TAG, "Supported Google and Q lens");
                this.mGet.setSettingChildMenuEnable(Setting.KEY_LENS_SELECTION, CameraConstants.GOOGLELENS, true);
                boolean isPrevSelectedGoogle = CameraConstants.GOOGLELENS.equals(CheckStatusManager.getSystemSettingLensType(this.mGet.getActivity().getContentResolver()));
                if (isPrevSelectedGoogle && isGoogleLensAvailable()) {
                    CamLog.m3d(CameraConstants.TAG, "Google lens added");
                    this.mGet.setSetting(Setting.KEY_LENS_SELECTION, CameraConstants.GOOGLELENS, true);
                    CheckStatusManager.setSystemSettingLensType(this.mGet.getActivity().getContentResolver(), CameraConstants.GOOGLELENS);
                    this.mUspArrayList.add(new UspItem(C0088R.string.google_lens, CameraConstantsEx.USP_GOOGLE_LENS, C0088R.id.usp_zone_google_lens));
                    return;
                }
                CamLog.m3d(CameraConstants.TAG, "QLens added");
                this.mUspArrayList.add(new UspItem(C0088R.string.lg_lens, false, CameraConstantsEx.USP_LG_LENS, C0088R.id.usp_zone_lg_lens));
                if (!isGoogleLensAvailable()) {
                    this.mGet.setSettingChildMenuEnable(Setting.KEY_LENS_SELECTION, CameraConstants.GOOGLELENS, false);
                    if (isPrevSelectedGoogle && SharedPreferenceUtil.getInitialHelpShown(this.mGet.getAppContext())) {
                        this.mGet.showToast(getGoogleLensErrorCode(), CameraConstants.TOAST_LENGTH_LONG);
                    }
                }
                CheckStatusManager.setSystemSettingLensType(this.mGet.getActivity().getContentResolver(), CameraConstants.QLENS);
                this.mGet.setSetting(Setting.KEY_LENS_SELECTION, CameraConstants.QLENS, true);
            } else if (FunctionProperties.isSupportedLGLens(this.mGet.getAppContext())) {
                CamLog.m3d(CameraConstants.TAG, "Supported Q lens only");
                this.mUspArrayList.add(new UspItem(C0088R.string.lg_lens, false, CameraConstantsEx.USP_LG_LENS, C0088R.id.usp_zone_lg_lens));
            } else if (FunctionProperties.isSupportedGoogleLens() && isGoogleLensAvailable()) {
                CamLog.m3d(CameraConstants.TAG, "Supported Google lens only");
                this.mUspArrayList.add(new UspItem(C0088R.string.google_lens, CameraConstantsEx.USP_GOOGLE_LENS, C0088R.id.usp_zone_google_lens));
            }
        }
    }

    private boolean isGoogleLensAvailable() {
        return this.mGoogleLensStatus == 0;
    }

    private String getGoogleLensErrorCode() {
        int resId;
        if (this.mGoogleLensStatus == 2) {
            resId = C0088R.string.setting_visual_search_error_code_language;
        } else {
            resId = C0088R.string.setting_visual_search_error_code_unknown;
        }
        return this.mGet.getActivity().getString(resId);
    }

    public void onResume() {
        if (this.mUspBaseView == null) {
            this.mUspBaseView = this.mGet.getActivity().getLayoutInflater().inflate(C0088R.layout.usp_zone_layout, null);
            this.mGet.inflateStub(C0088R.id.stub_usp_view);
        }
        ViewGroup vg = (ViewGroup) this.mGet.findViewById(C0088R.id.usp_view);
        if (vg != null && this.mUspBaseView != null) {
            CamLog.m3d(CameraConstants.TAG, "[usp] init layout, from voice assistantce : " + CameraConstantsEx.FLAG_VALUE_MODE_SMARTCAM.equals(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null)));
            vg.addView(this.mUspBaseView, 0, new LayoutParams(-1, -2));
            if (!this.mIsPuasedOnSecureCamera) {
                this.mSelectedIndex = -1;
            }
            this.mIsRearCamera = this.mGet.isRearCamera();
            setUspZoneLayout();
            this.mUspRelativeLayout = (RelativeLayout) this.mUspBaseView.findViewById(C0088R.id.usp_zone_layout);
            if (this.mUspArrayList.size() != 0) {
                addUspItemViews();
                this.mIsReadyToShowUI = true;
                if (!SecureImageUtil.isSecureCamera() || this.mIsPuasedOnSecureCamera) {
                    String lastUsp = SharedPreferenceUtilBase.getUspSetting(this.mGet.getAppContext());
                    String curUsp = getCurValue();
                    if (this.mIsPuasedOnSecureCamera) {
                        lastUsp = curUsp;
                    }
                    refreshUspZone(false);
                    CamLog.m3d(CameraConstants.TAG, "[usp] last usp : " + lastUsp + ", cur usp : " + curUsp);
                    if (lastUsp.equals(curUsp)) {
                        UspItem newUspItem = getItem(lastUsp);
                        if (newUspItem != null) {
                            newUspItem.setSelected(true);
                            setUspItemVisibility(newUspItem);
                        }
                        if (CameraConstantsEx.FLAG_VALUE_MODE_SMARTCAM.equals(this.mGet.getAssistantStringFlag(CameraConstantsEx.FLAG_CAMERA_MODE, null))) {
                            setVisibility(4);
                            this.mIsReadyToShowUI = false;
                        }
                    } else if (!(lastUsp == null || CameraConstantsEx.USP_NORMAL.equals(lastUsp))) {
                        onUspItemClick(lastUsp, getIndex(lastUsp), false);
                    }
                }
                if (this.mLensApi != null) {
                    try {
                        this.mLensApi.onResume();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setUspZoneLayout() {
        int startMargin = 0;
        if (this.mUspArrayList.size() == 3) {
            startMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0528f);
        } else if (this.mUspArrayList.size() == 2) {
            startMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.233f);
        }
        this.mStartMarginDp = (int) (((float) startMargin) / this.mGet.getAppContext().getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.mUspBaseView.getLayoutParams();
        if (params != null) {
            params.bottomMargin = getUspBottomMargin();
            params.setMarginStart((int) Utils.dpToPx(this.mGet.getAppContext(), (float) this.mStartMarginDp));
            params.setMarginEnd((int) Utils.dpToPx(this.mGet.getAppContext(), (float) this.mStartMarginDp));
            this.mUspBaseView.setLayoutParams(params);
        }
        setVisibility(0);
    }

    public int getUspBottomMargin() {
        if (ModelProperties.getLCDType() == 1) {
            return RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.227f);
        }
        if (ModelProperties.getLCDType() == 2) {
            return RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.253f);
        }
        return RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.224f);
    }

    private void addUspItemViews() {
        if (this.mUspArrayList.size() != 0) {
            this.mUspTextSize = (int) (((float) RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0361f)) / this.mGet.getAppContext().getResources().getDisplayMetrics().density);
            UspItem uspItem;
            RotateTextView itemView;
            LayoutParams itemViewLp;
            if (this.mUspArrayList.size() == 1) {
                uspItem = (UspItem) this.mUspArrayList.get(0);
                itemView = new RotateTextView(this.mGet.getAppContext());
                itemView.setId(uspItem.getLayoutId());
                itemView.setStyle((int) C0088R.style.usp_item_text_stroke);
                itemView.setText(this.mGet.getAppContext().getString(uspItem.getTitleId()));
                itemView.setTextSize((int) Utils.dpToPx(this.mGet.getAppContext(), (float) this.mUspTextSize));
                itemView.setContentDescription(this.mGet.getAppContext().getString(uspItem.getTitleId()));
                itemView.setLayoutParams(new LayoutParams(-2, -2));
                itemView.setTag(Integer.valueOf(0));
                itemView.setOnClickListener(getUspClickListener());
                itemViewLp = (LayoutParams) itemView.getLayoutParams();
                itemViewLp.addRule(13);
                itemView.setLayoutParams(itemViewLp);
                if (checkUsingUSPBackground()) {
                    itemView.setBackgroundResource(C0088R.drawable.bg_usp_button);
                } else {
                    itemView.setBackground(null);
                }
                this.mUspRelativeLayout.addView(itemView);
                return;
            }
            for (int i = 0; i < this.mUspArrayList.size(); i++) {
                uspItem = (UspItem) this.mUspArrayList.get(i);
                itemView = new RotateTextView(this.mGet.getAppContext());
                itemView.setId(uspItem.getLayoutId());
                itemView.setStyle((int) C0088R.style.usp_item_text_stroke);
                itemView.setText(this.mGet.getAppContext().getString(uspItem.getTitleId()));
                itemView.setTextSize((int) Utils.dpToPx(this.mGet.getAppContext(), (float) this.mUspTextSize));
                itemView.setContentDescription(this.mGet.getAppContext().getString(uspItem.getTitleId()));
                itemView.setLayoutParams(new LayoutParams(-2, -2));
                itemView.setTag(Integer.valueOf(i));
                itemView.setOnClickListener(getUspClickListener());
                itemViewLp = (LayoutParams) itemView.getLayoutParams();
                if (i == 0) {
                    itemViewLp.addRule(20);
                } else if (i == this.mUspArrayList.size() - 1) {
                    itemViewLp.addRule(21);
                } else {
                    itemViewLp.addRule(13);
                }
                itemView.setLayoutParams(itemViewLp);
                itemView.setBackground(null);
                this.mUspRelativeLayout.addView(itemView);
            }
        }
    }

    private OnClickListener getUspClickListener() {
        return new C11862();
    }

    public void onUspItemClick(String newUspValue, int position, boolean doClick) {
        UspItem newUspItem = getItem(newUspValue);
        if (newUspItem != null) {
            CamLog.m3d(CameraConstants.TAG, "[usp] newUspValue : " + newUspValue + ", position : " + position);
            String curUspValue = getCurValue();
            if (curUspValue == null) {
                return;
            }
            if (curUspValue.equals(newUspValue)) {
                this.mSelectedIndex = -1;
                this.mBackupUspMode = "";
                newUspItem.setSelected(false);
                setUspItemVisibility(newUspItem);
                if (doClick) {
                    doUspItemClick(newUspValue, false);
                    return;
                }
                return;
            }
            if (!CameraConstantsEx.USP_NORMAL.equals(curUspValue)) {
                UspItem curUspItem = getItem(curUspValue);
                if (curUspItem != null) {
                    curUspItem.setSelected(false);
                    ((RotateTextView) this.mGet.findViewById(curUspItem.getLayoutId())).setSelected(curUspItem.isSelected());
                }
            }
            this.mSelectedIndex = position;
            newUspItem.setSelected(true);
            setUspItemVisibility(newUspItem);
            if (doClick) {
                doUspItemClick(newUspValue, true);
            }
        }
    }

    private void setUspItemVisibility(UspItem newUspItem) {
        CamLog.m3d(CameraConstants.TAG, "[usp] mSelectedIndex : " + this.mSelectedIndex);
        setVisibility(0);
        ((RotateTextView) this.mGet.findViewById(newUspItem.getLayoutId())).setSelected(newUspItem.isSelected());
        for (int i = 0; i < this.mUspArrayList.size(); i++) {
            View itemView = this.mUspRelativeLayout.getChildAt(i);
            int visibility = this.mSelectedIndex == -1 ? 0 : ((UspItem) this.mUspArrayList.get(i)).isSelected() ? 0 : 4;
            itemView.setVisibility(visibility);
        }
    }

    public void updateUspIndex() {
        this.mIsPuasedOnSecureCamera = false;
        String lastUsp = SharedPreferenceUtilBase.getUspSetting(this.mGet.getAppContext());
        this.mSelectedIndex = getIndex(lastUsp);
        CamLog.m3d(CameraConstants.TAG, "[usp] update : " + lastUsp);
    }

    public void updateLayout(String newUsp, boolean selected) {
        this.mIsReadyToShowUI = true;
        onUspItemClick(newUsp, getIndex(newUsp), false);
    }

    protected UspItem getItem(String usp) {
        if (this.mUspArrayList == null) {
            return null;
        }
        for (int i = 0; i < this.mUspArrayList.size(); i++) {
            UspItem item = (UspItem) this.mUspArrayList.get(i);
            if (item != null && usp.equals(item.getValue())) {
                return item;
            }
        }
        return null;
    }

    protected int getIndex(String uspValue) {
        if (this.mUspArrayList == null) {
            return -1;
        }
        for (int i = 0; i < this.mUspArrayList.size(); i++) {
            UspItem item = (UspItem) this.mUspArrayList.get(i);
            if (item != null && uspValue.equals(item.getValue())) {
                return i;
            }
        }
        return -1;
    }

    public String getCurValue() {
        if (this.mUspArrayList == null) {
            return CameraConstantsEx.USP_NORMAL;
        }
        int index = this.mSelectedIndex;
        if (index == -1) {
            return CameraConstantsEx.USP_NORMAL;
        }
        return ((UspItem) this.mUspArrayList.get(index)).getValue();
    }

    public String getPrevValue() {
        return this.mBackupUspMode;
    }

    public void onPause() {
        if (SecureImageUtil.isSecureCamera()) {
            this.mIsPuasedOnSecureCamera = true;
            if (CameraConstantsEx.USP_GOOGLE_LENS.equals(getCurValue()) || CameraConstantsEx.USP_LG_LENS.equals(getCurValue())) {
                this.mSelectedIndex = -1;
            }
        } else {
            String uspSetting;
            if (CameraConstantsEx.USP_LG_LENS.equals(getCurValue()) || CameraConstantsEx.USP_GOOGLE_LENS.equals(getCurValue())) {
                uspSetting = CameraConstantsEx.USP_NORMAL;
            } else {
                uspSetting = getCurValue();
            }
            CamLog.m3d(CameraConstants.TAG, "[usp] save last usp : " + uspSetting);
            SharedPreferenceUtilBase.saveUspSetting(this.mGet.getAppContext(), uspSetting);
        }
        for (int i = 0; i < this.mUspArrayList.size(); i++) {
            ((UspItem) this.mUspArrayList.get(i)).setSelected(false);
        }
        if (this.mUspRelativeLayout != null) {
            this.mUspRelativeLayout.removeAllViews();
            this.mUspRelativeLayout = null;
        }
        if (this.mUspBaseView != null) {
            this.mUspBaseView.setVisibility(8);
            ViewGroup vg = (ViewGroup) this.mGet.getActivity().findViewById(C0088R.id.usp_view);
            if (!(vg == null || this.mUspBaseView == null)) {
                vg.removeView(this.mUspBaseView);
            }
            this.mUspBaseView = null;
        }
        if (this.mLensApi != null) {
            try {
                this.mLensApi.onPause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy() {
        if (this.mUspArrayList != null) {
            this.mUspArrayList.clear();
            this.mUspArrayList = null;
        }
        this.mIsPuasedOnSecureCamera = false;
        this.mSelectedIndex = -1;
        this.mLensApi = null;
    }

    public void setVisibility(int visibility) {
        CamLog.m3d(CameraConstants.TAG, "[usp] setVisibility : " + visibility + ", mIsReadyToShowUI : " + this.mIsReadyToShowUI);
        if (this.mIsReadyToShowUI) {
            if (!"mode_normal".equals(this.mGet.getCurSettingValue(Setting.KEY_MODE))) {
                visibility = 8;
            }
            if (this.mUspBaseView != null) {
                this.mUspBaseView.setVisibility(visibility);
            }
        }
    }

    public boolean isVisible() {
        if (this.mUspBaseView == null || this.mUspBaseView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public boolean doBackKey() {
        if (!isVisible()) {
            return false;
        }
        String curUsp = getCurValue();
        if (CameraConstantsEx.USP_NORMAL.equals(curUsp)) {
            return false;
        }
        onUspItemClick(curUsp, this.mSelectedIndex, true);
        CamLog.m3d(CameraConstants.TAG, "[usp] do back key : " + curUsp);
        return true;
    }

    protected boolean doUspItemClick(String value, boolean selected) {
        if (this.mGet == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "[usp] setItemSelected : " + value + ", isOn : " + selected);
        LdbUtil.sendLDBIntent(this.mGet.getAppContext(), LdbConstants.LDB_FEAT_NAME_USPZONE, -1, value);
        if (CameraConstantsEx.USP_OUTFOCUS.equals(value)) {
            return this.mGet.onOutfocusModeClicked(selected);
        }
        if (CameraConstantsEx.USP_SMART_CAM.equals(value)) {
            return this.mGet.onSmartCamModeClicked(selected);
        }
        if (CameraConstantsEx.USP_LG_LENS.equals(value)) {
            if (selected) {
                this.mGet.stopPreview();
                AppControlUtilBase.setLaunchingLGLens(true);
                Intent intent = new Intent("com.lge.ellievision.action.VISION_SEARCH_CAMERA");
                intent.setFlags(268468224);
                intent.putExtra("startMargin", this.mStartMarginDp);
                if (ModelProperties.getLCDType() == 1) {
                    intent.putExtra("bottomMargin", 0.227f);
                } else if (ModelProperties.getLCDType() == 2) {
                    intent.putExtra("bottomMargin", 0.253f);
                } else {
                    intent.putExtra("bottomMargin", 0.224f);
                }
                intent.putExtra("itemCnt", this.mUspArrayList.size());
                intent.putExtra("textSize", this.mUspTextSize);
                this.mGet.getActivity().startActivity(intent);
            }
            return true;
        } else if (!CameraConstantsEx.USP_GOOGLE_LENS.equals(value)) {
            return false;
        } else {
            if (this.mLensApi != null) {
                this.mLensApi.launchLensActivity(this.mGet.getActivity());
                return true;
            }
            CamLog.m5e(CameraConstants.TAG, "mLensApi is null");
            return false;
        }
    }

    public void refreshUspZone(boolean isCameraSwitching) {
        if (!isCameraSwitching || this.mIsRearCamera != this.mGet.isRearCamera()) {
            String curValue = getCurValue();
            if (!CameraConstantsEx.USP_SMART_CAM.equals(curValue) && this.mUspArrayList != null && this.mUspRelativeLayout != null) {
                if (!CameraConstantsEx.USP_OUTFOCUS.equals(curValue) || !FunctionProperties.isSupportedRearOutfocus() || !FunctionProperties.isSupportedFrontOutfocus()) {
                    int beforeSize = this.mUspArrayList.size();
                    this.mIsRearCamera = this.mGet.isRearCamera();
                    CamLog.m3d(CameraConstants.TAG, "[usp] refreshUspZone , isCameraSwitching : " + isCameraSwitching);
                    this.mUspArrayList.clear();
                    this.mSelectedIndex = -1;
                    this.mUspArrayList = null;
                    initItemList();
                    int afterSize = this.mUspArrayList.size();
                    this.mUspRelativeLayout.removeAllViews();
                    setUspZoneLayout();
                    addUspItemViews();
                    if (beforeSize == afterSize) {
                        this.mBackupUspMode = "";
                    } else if (CameraConstantsEx.USP_OUTFOCUS.equals(this.mBackupUspMode)) {
                        onUspItemClick(CameraConstantsEx.USP_OUTFOCUS, getIndex(CameraConstantsEx.USP_OUTFOCUS), false);
                        this.mBackupUspMode = "";
                    } else if (CameraConstantsEx.USP_OUTFOCUS.equals(curValue)) {
                        this.mBackupUspMode = curValue;
                    } else {
                        this.mBackupUspMode = "";
                    }
                    CamLog.m3d(CameraConstants.TAG, "[usp] mPreviousUspMode : " + this.mBackupUspMode);
                }
            }
        }
    }
}
