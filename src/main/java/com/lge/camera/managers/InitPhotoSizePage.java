package com.lge.camera.managers;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.device.CameraDeviceUtils;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.RatioCalcUtil;
import com.lge.camera.util.SettingKeyWrapper;
import com.lge.camera.util.Utils;

public class InitPhotoSizePage extends InitHelpPage {
    public static final float INIT_HELP_CONTENT_BOTTOM_MARGIN = 0.0347f;
    public static final float INIT_HELP_CONTENT_START_END_MARGIN = 0.0486f;
    public static final float INIT_HELP_CONTENT_TOP_MARGIN = 0.0278f;
    public static final float INIT_HELP_CONTENT_TOP_MARGIN_LAND = 0.0417f;
    public static final float INIT_HELP_DESC_TOP_MARGIN_LAND = 0.0389f;
    public static final float INIT_HELP_IMAGE_TOP_MARGIN = 0.0069f;
    public static final float INIT_HELP_IMAGE_TOP_MARGIN_LAND = 0.0139f;
    public static final float INIT_HELP_RADIO_START_PADDING = 0.0056f;
    public static final float INIT_HELP_SCROLL_BOTTOM_MARGIN_LANDSCAPE = 0.0885f;
    public static final float INIT_HELP_SCROLL_BOTTOM_MARGIN_PORTRAIT = 0.1413f;
    public static final float INIT_HELP_TITLE_TOP_PADDING = 0.0528f;
    public static final float INIT_HELP_TITLE_TOP_PADDING_LAND = 0.075f;
    private int mFullIndex = 0;
    private int mMaxIndex = 0;
    private OnClickListener mOnClickListener = new C10301();
    private int mSelectedId = 0;

    /* renamed from: com.lge.camera.managers.InitPhotoSizePage$1 */
    class C10301 implements OnClickListener {
        C10301() {
        }

        public void onClick(View view) {
            if (view != null) {
                InitPhotoSizePage.this.onRadioButtonClicked((RadioButton) view);
            }
        }
    }

    public InitPhotoSizePage(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        this.mImageId = C0088R.drawable.camera_initail_image_photo_size_01;
    }

    public void initUI(View v, int degree) {
        this.mView = v;
        this.mDegree = degree;
        initLayout(this.mDegree);
    }

    protected void initLayout(int degree) {
        if (this.mView != null) {
            initPhotoSizeHelp();
            initPhotoSizeHelpLayout(degree);
        }
    }

    protected void initPhotoSizeHelpLayout(int degree) {
        if (this.mSelectedId == 0) {
            this.mSelectedId = C0088R.id.photo_size_radio_1;
        }
        boolean isPortrait = degree == 0 || degree == 180;
        if (isPortrait) {
            initPortraitLayout();
        } else {
            initLandscapeLayout();
        }
        initRadioButton(this.mSelectedId, C0088R.id.photo_size_radio_1, isPortrait);
        initRadioButton(this.mSelectedId, C0088R.id.photo_size_radio_2, isPortrait);
    }

    private void initRadioButton(int selectedId, int radioId, boolean isPortrait) {
        setRadioButtonDrawable(selectedId, radioId);
    }

    private void setRadioButtonDrawable(int selectedId, int radioId) {
        RadioButton radioButton = (RadioButton) this.mView.findViewById(radioId);
        if (radioButton != null) {
            radioButton.setOnClickListener(null);
            radioButton.setOnClickListener(this.mOnClickListener);
            Drawable d = radioButton.getButtonDrawable();
            if (selectedId == radioButton.getId()) {
                radioButton.setChecked(true);
                d.setColorFilter(this.mGet.getAppContext().getColor(C0088R.color.camera_primary_color), Mode.SRC_ATOP);
                return;
            }
            radioButton.setChecked(false);
            d.setColorFilter(this.mGet.getAppContext().getResources().getColor(C0088R.color.setting_2dept_button_unchecked, null), Mode.SRC_ATOP);
        }
    }

    private void initPortraitLayout() {
        if (this.mView != null) {
            int padding = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.083f);
            this.mView.findViewById(C0088R.id.photo_size_layout).setPadding(padding, 0, padding, 0);
            this.mView.findViewById(C0088R.id.photo_size_title_layout).setPadding(0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0528f), 0, 0);
            View view = this.mView.findViewById(C0088R.id.photo_size_help_scroll);
            LayoutParams rlp = (LayoutParams) view.getLayoutParams();
            rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0278f);
            rlp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.1413f);
            view.setLayoutParams(rlp);
            view = this.mView.findViewById(C0088R.id.photo_size_1_layout);
            rlp = (LayoutParams) view.getLayoutParams();
            rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0278f);
            rlp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0347f);
            view.setLayoutParams(rlp);
            initPhotoSizeContentLayout(C0088R.id.photo_size_radio_1_layout, C0088R.id.photo_size_image_1, C0088R.id.photo_size_desc_1);
            rlp = (LayoutParams) this.mView.findViewById(C0088R.id.photo_size_2_layout).getLayoutParams();
            int sizeCalculatedByPercentage = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0347f);
            rlp.bottomMargin = sizeCalculatedByPercentage;
            rlp.topMargin = sizeCalculatedByPercentage;
            initPhotoSizeContentLayout(C0088R.id.photo_size_radio_2_layout, C0088R.id.photo_size_image_2, C0088R.id.photo_size_desc_2);
            if (ModelProperties.getCarrierCode() == 4) {
                Context context = this.mGet.getAppContext();
                ViewGroup layout = (ViewGroup) this.mView.findViewById(C0088R.id.recording_preview_layout);
                layout.setVisibility(0);
                Utils.addTabToNumberedDescription(layout, (context.getString(C0088R.string.help_tips) + "\n") + context.getString(C0088R.string.help_recording_preview_desc), context, false, C0088R.style.help_desc);
            }
        }
    }

    private void initLandscapeLayout() {
        if (this.mView != null) {
            int width;
            View view = this.mView.findViewById(C0088R.id.photo_size_help_layout);
            int padding = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.069f);
            view.setPadding(padding, 0, padding, 0);
            this.mView.findViewById(C0088R.id.photo_size_title_layout).setPadding(0, RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.075f), 0, 0);
            view = this.mView.findViewById(C0088R.id.photo_size_help_scroll);
            LayoutParams rlp = (LayoutParams) view.getLayoutParams();
            rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0417f);
            rlp.bottomMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0885f);
            view.setLayoutParams(rlp);
            view = this.mView.findViewById(C0088R.id.photo_size_content_layout);
            rlp = (LayoutParams) view.getLayoutParams();
            rlp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0417f);
            view.setLayoutParams(rlp);
            int[] lcdSize = Utils.getLCDsize(this.mGet.getAppContext(), true);
            padding = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.083f);
            int margin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0486f);
            if (ModelProperties.getLCDType() == 2) {
                width = (((lcdSize[0] - RatioCalcUtil.getNotchDisplayHeight(this.mGet.getAppContext())) / 2) - padding) - margin;
            } else {
                width = ((lcdSize[0] / 2) - padding) - margin;
            }
            view = this.mView.findViewById(C0088R.id.photo_size_1_layout);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.width = width;
            llp.setMarginEnd(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0486f));
            view.setLayoutParams(llp);
            initPhotoSizeContentLandLayout(C0088R.id.photo_size_radio_1_layout, C0088R.id.photo_size_image_1, C0088R.id.photo_size_desc_1, width);
            view = this.mView.findViewById(C0088R.id.photo_size_2_layout);
            llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.width = width;
            llp.setMarginStart(RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0486f));
            view.setLayoutParams(llp);
            initPhotoSizeContentLandLayout(C0088R.id.photo_size_radio_2_layout, C0088R.id.photo_size_image_2, C0088R.id.photo_size_desc_2, width);
            if (ModelProperties.getCarrierCode() == 4) {
                Context context = this.mGet.getAppContext();
                ViewGroup layout = (ViewGroup) this.mView.findViewById(C0088R.id.recording_preview_layout);
                layout.setVisibility(0);
                Utils.addTabToNumberedDescription(layout, (context.getString(C0088R.string.help_tips) + "\n") + context.getString(C0088R.string.help_recording_preview_desc), context, false, C0088R.style.help_desc);
            }
        }
    }

    private void initPhotoSizeContentLayout(int checkBoxId, int imageViewId, int textViewId) {
        if (this.mView != null) {
            View view = this.mView.findViewById(imageViewId);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0069f);
            view.setLayoutParams(llp);
            view = this.mView.findViewById(textViewId);
            llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), true, 0.0167f);
            view.setLayoutParams(llp);
        }
    }

    private void initPhotoSizeContentLandLayout(int checkBoxId, int imageViewId, int textViewId, int contentLayoutWidth) {
        if (this.mView != null) {
            View view = this.mView.findViewById(checkBoxId);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.width = contentLayoutWidth;
            view.setLayoutParams(llp);
            view = this.mView.findViewById(imageViewId);
            llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0139f);
            view.setLayoutParams(llp);
            view = this.mView.findViewById(textViewId);
            llp = (LinearLayout.LayoutParams) view.getLayoutParams();
            llp.topMargin = RatioCalcUtil.getSizeCalculatedByPercentage(this.mGet.getAppContext(), false, 0.0389f);
            view.setLayoutParams(llp);
        }
    }

    private void onRadioButtonClicked(RadioButton button) {
        if (button != null && this.mView != null && button.getId() != this.mSelectedId) {
            this.mSelectedId = button.getId();
            setRadioButtonDrawable(this.mSelectedId, C0088R.id.photo_size_radio_1);
            setRadioButtonDrawable(this.mSelectedId, C0088R.id.photo_size_radio_2);
            setRadioButtonEnable(C0088R.id.photo_size_radio_1, false);
            setRadioButtonEnable(C0088R.id.photo_size_radio_2, false);
            changePictureSize(button);
        }
    }

    private void changePictureSize(RadioButton button) {
        if (button != null) {
            ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
            if (listPref != null && this.mGet.checkModuleValidate(208)) {
                CharSequence[] entryValues = listPref.getEntryValues();
                if (entryValues != null) {
                    boolean toFullVision = button.getId() == C0088R.id.photo_size_radio_2;
                    int currentIndex = toFullVision ? this.mFullIndex : this.mMaxIndex;
                    int[] pictureSize = Utils.sizeStringToArray(entryValues[currentIndex].toString());
                    if (pictureSize != null && pictureSize[0] != 0) {
                        CamLog.m3d(CameraConstants.TAG, String.format("-Photo size help- changePictureSize, key = %s, value = %s", new Object[]{listPref.getKey(), entryValues[currentIndex].toString()}));
                        this.mGet.childSettingMenuClicked(key, value);
                        float ratio = ((float) pictureSize[1]) / ((float) pictureSize[0]);
                        if (CameraDeviceUtils.isRearCamera(this.mGet.getCameraId())) {
                            setPictureSizePref(1, ratio);
                            ListPreference fullVisionListPref = (ListPreference) this.mGet.getListPreference(Setting.KEY_FULLVISION, false);
                            if (fullVisionListPref != null) {
                                String str;
                                if (toFullVision) {
                                    str = "on";
                                } else {
                                    str = "off";
                                }
                                fullVisionListPref.setValue(str);
                                return;
                            }
                            return;
                        }
                        setPictureSizePref(0, ratio);
                        if (FunctionProperties.getCameraTypeRear() == 1) {
                            setPictureSizePref(2, ratio);
                        }
                    }
                }
            }
        }
    }

    private void setPictureSizePref(int cameraId, float ratio) {
        ListPreference listPref;
        if (cameraId == 0 || cameraId == 2) {
            listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), cameraId), true);
        } else {
            listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), cameraId), false);
        }
        if (listPref != null) {
            CharSequence[] entryValues = listPref.getEntryValues();
            if (entryValues != null) {
                int i = 0;
                while (i < entryValues.length) {
                    int[] size = Utils.sizeStringToArray(entryValues[i].toString());
                    if (size == null || size[0] == 0 || Float.compare(ratio, ((float) size[1]) / ((float) size[0])) != 0) {
                        i++;
                    } else {
                        String pictureSize = entryValues[i].toString();
                        CamLog.m3d(CameraConstants.TAG, String.format("-Photo size help- set picture size, cameraId = %d, size = %s", new Object[]{Integer.valueOf(cameraId), pictureSize}));
                        listPref.setValue(entryValues[i].toString());
                        return;
                    }
                }
            }
        }
    }

    private void setRadioButtonEnable(int buttonId, boolean enable) {
        if (this.mView != null) {
            RadioButton button = (RadioButton) this.mView.findViewById(buttonId);
            if (button != null) {
                CamLog.m3d(CameraConstants.TAG, "-Photo size help- setRadioButtonEnable, enable = " + enable);
                button.setEnabled(enable);
                Drawable d = button.getButtonDrawable();
                if (d != null) {
                    d.setAlpha(enable ? 255 : 89);
                }
            }
        }
    }

    protected void initPhotoSizeHelp() {
        ListPreference listPref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        if (listPref != null) {
            CharSequence[] entries = listPref.getEntries();
            CharSequence[] entryValues = listPref.getEntryValues();
            this.mMaxIndex = 0;
            String maxString = entries[this.mMaxIndex].toString();
            for (int i = 0; i < entryValues.length; i++) {
                int[] size = Utils.sizeStringToArray(entryValues[i].toString());
                if (size[0] >= size[1] * 2) {
                    this.mFullIndex = i;
                    break;
                }
            }
            String fullString = entries[this.mFullIndex].toString();
            String ratio = maxString.substring(0, maxString.indexOf("("));
            ((RadioButton) this.mView.findViewById(C0088R.id.photo_size_radio_1)).setText(ratio + " (" + this.mGet.getAppContext().getString(C0088R.string.photo_size_radio_text_1) + ")");
            ((TextView) this.mView.findViewById(C0088R.id.photo_size_desc_1)).setText(this.mGet.getAppContext().getString(C0088R.string.photo_size_help_desc_1).replace("#01#", maxString.substring(0, maxString.indexOf(")") + 1)));
            ratio = fullString.substring(0, fullString.indexOf("("));
            ((RadioButton) this.mView.findViewById(C0088R.id.photo_size_radio_2)).setText(ratio + " (" + this.mGet.getAppContext().getString(C0088R.string.photo_size_radio_text_2) + ")");
            ((TextView) this.mView.findViewById(C0088R.id.photo_size_desc_2)).setText(this.mGet.getAppContext().getString(C0088R.string.photo_size_help_desc_2_1).replace("#02#", fullString.substring(0, fullString.indexOf(")") + 1)));
        }
    }

    public void oneShotPreviewCallbackDone() {
        super.oneShotPreviewCallbackDone();
        if (this.mView != null) {
            setRadioButtonEnable(C0088R.id.photo_size_radio_1, true);
            setRadioButtonEnable(C0088R.id.photo_size_radio_2, true);
        }
    }

    public void onPause() {
        super.onPause();
        setRadioButtonEnable(C0088R.id.photo_size_radio_1, false);
        setRadioButtonEnable(C0088R.id.photo_size_radio_2, false);
    }
}
