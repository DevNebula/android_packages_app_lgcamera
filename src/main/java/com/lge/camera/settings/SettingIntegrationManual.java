package com.lge.camera.settings;

import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.HashMap;

public class SettingIntegrationManual extends SettingIntegration {
    private static final int IDX_BITRATE_120_FPS = 6;
    private static final int IDX_BITRATE_240_FPS = 7;
    private static final int IDX_BITRATE_60_FPS = 5;
    private static final int IDX_BITRATE_NORMAL = 4;
    private static final int IDX_BITRATE_TIME_LAPSE = 3;
    private static final int IDX_FRAME_RATE = 2;
    private static final int IDX_PREVIEW_SIZE = 0;
    private static final int IDX_VIDEO_SIZE = 1;
    private static final String NOT_SUPPORT = "NotSupport";
    private static ArrayList<String> sSupportedList = null;
    private static ArrayList<String> sSupportedPreviewSizeList = null;
    private static ArrayList<String> sSupportedVideoSizeList = null;
    private HashMap<String, String> mDescMap = new HashMap();
    private HashMap<String, String> mFrameRateEntriesMap = new HashMap();
    private final int[] mQualityList = new int[]{8, 6, 5};
    private ArrayList<Float> mRatioList = null;
    private HashMap<String, Integer> mVideoSizeEntriesMap = new HashMap();

    public SettingIntegrationManual(SettingInterface setting) {
        super(setting);
        CamLog.m3d(CameraConstants.TAG, "SettingIntegrationManual [START]");
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
            makeVideoRatioList();
            makeSupportedList();
            makeVideoSizeEntriesMap();
            makeFrameRateEntriesMap();
            makeResolutionHashMap();
        }
        CamLog.m3d(CameraConstants.TAG, "SettingIntegrationManual [END]");
    }

    private void makeVideoRatioList() {
        if (this.mRatioList == null) {
            this.mRatioList = new ArrayList();
        }
        this.mRatioList.add(Float.valueOf(1.7777778f));
        this.mRatioList.add(Float.valueOf(2.3333333f));
        if (!ModelProperties.isLongLCDModel()) {
            return;
        }
        if (ModelProperties.getLCDType() == 2) {
            this.mRatioList.add(Float.valueOf(2.1f));
        } else {
            this.mRatioList.add(Float.valueOf(2.0f));
        }
    }

    private void makeVideoSizeEntriesMap() {
        if (this.mVideoSizeEntriesMap == null) {
            this.mVideoSizeEntriesMap = new HashMap();
        }
        this.mVideoSizeEntriesMap.put(ParamConstants.VIDEO_3840_BY_2160, Integer.valueOf(C0088R.string.video_manual_size_uhd_16_9));
        this.mVideoSizeEntriesMap.put("1920x1080", Integer.valueOf(C0088R.string.video_manual_size_fhd_16_9));
        this.mVideoSizeEntriesMap.put(CameraConstants.VIDEO_CINEMA_FHD, Integer.valueOf(C0088R.string.video_manual_size_fhd_21_9));
        this.mVideoSizeEntriesMap.put("2268x1080", Integer.valueOf(C0088R.string.video_manual_size_fhd_18_9_9));
        this.mVideoSizeEntriesMap.put("2160x1080", Integer.valueOf(C0088R.string.video_manual_size_fhd_wide));
        this.mVideoSizeEntriesMap.put("1280x720", Integer.valueOf(C0088R.string.video_manual_size_hd_16_9));
        this.mVideoSizeEntriesMap.put("1680x720", Integer.valueOf(C0088R.string.video_manual_size_hd_21_9));
        this.mVideoSizeEntriesMap.put("1512x720", Integer.valueOf(C0088R.string.video_manual_size_hd_18_9_9));
        this.mVideoSizeEntriesMap.put(ParamConstants.VIDEO_1440_BY_720, Integer.valueOf(C0088R.string.video_manual_size_hd_wide));
    }

    private void makeFrameRateEntriesMap() {
        if (this.mFrameRateEntriesMap == null) {
            this.mFrameRateEntriesMap = new HashMap();
        }
        String fpsString = " " + this.mGet.getActivity().getString(C0088R.string.camera_manual_info_title_frame_rate);
        this.mFrameRateEntriesMap.put("1", "1" + fpsString);
        this.mFrameRateEntriesMap.put("2", "2" + fpsString);
        this.mFrameRateEntriesMap.put(CameraConstants.FPS_24, CameraConstants.FPS_24 + fpsString);
        this.mFrameRateEntriesMap.put(CameraConstants.FPS_25, CameraConstants.FPS_25 + fpsString);
        this.mFrameRateEntriesMap.put("30", "30" + fpsString);
        this.mFrameRateEntriesMap.put(CameraConstants.FPS_50, CameraConstants.FPS_50 + fpsString);
        this.mFrameRateEntriesMap.put(CameraConstants.FPS_60, CameraConstants.FPS_60 + fpsString);
        this.mFrameRateEntriesMap.put(CameraConstants.FPS_100, CameraConstants.FPS_100 + fpsString);
        this.mFrameRateEntriesMap.put("120", "120" + fpsString);
        this.mFrameRateEntriesMap.put("240", "240" + fpsString);
    }

    private void makeResolutionHashMap() {
        if (this.mDescMap == null) {
            this.mDescMap = new HashMap();
        }
        String uhd = "U H D";
        String fhd = "F H D";
        String hd = "H D";
        if (uhd != null && fhd != null && hd != null) {
            this.mDescMap.put(ParamConstants.VIDEO_3840_BY_2160, uhd);
            this.mDescMap.put("2268x1080", fhd);
            this.mDescMap.put("1920x1080", fhd);
            this.mDescMap.put(CameraConstants.VIDEO_CINEMA_FHD, fhd);
            this.mDescMap.put("2160x1080", fhd);
            this.mDescMap.put("1680x720", hd);
            this.mDescMap.put("1280x720", hd);
            this.mDescMap.put("1512x720", hd);
            this.mDescMap.put(ParamConstants.VIDEO_1440_BY_720, hd);
        }
    }

    protected void initManualOnlySetingOrder() {
        if (CameraConstants.MODE_MANUAL_CAMERA.equals(this.mGet.getShotMode())) {
            this.mOrderBackCamera.add(Setting.SETTING_SECTION_MANUAL_CAMERA);
            if (FunctionProperties.isSupportedGraphy()) {
                this.mOrderBackCamera.add(Setting.KEY_GRAPHY);
            }
            if (FunctionProperties.isSupportedRAWPictureSaving()) {
                this.mOrderBackCamera.add(Setting.KEY_RAW_PICTURE);
            }
            if (FunctionProperties.isSupportedManualNoiseReduction()) {
                this.mOrderBackCamera.add(Setting.KEY_MANUAL_NOISE_REDUCTION);
            }
        } else if (CameraConstants.MODE_MANUAL_VIDEO.equals(this.mGet.getShotMode())) {
            this.mOrderBackCamera.add(Setting.SETTING_SECTION_MANUAL_VIDEO);
            this.mOrderBackCamera.add(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
            this.mOrderBackCamera.add(Setting.KEY_MANUAL_VIDEO_BITRATE);
            this.mOrderBackCamera.add(Setting.KEY_MANUAL_VIDEO_AUDIO);
            if (FunctionProperties.isSupportedLogProfile()) {
                this.mOrderBackCamera.add(Setting.KEY_MANUAL_VIDEO_LOG);
            } else if (FunctionProperties.isSupportedHDR10() && !ModelProperties.isFakeMode()) {
                this.mOrderBackCamera.add(Setting.KEY_HDR10);
            }
        }
    }

    public void changeSettingMenu(SettingMenuItem item, ListPreference listPref) {
        if (item != null) {
            SettingMenuItem mBackUpChildItem = new SettingMenuItem(item.getSettingIndex(), item.getName());
            for (int i = 0; i < item.getChildCount(); i++) {
                mBackUpChildItem.addChild(item.getChild(i));
            }
            item.close();
            if (listPref != null) {
                CharSequence[] entries = listPref.getEntries();
                CharSequence[] entryValues = listPref.getEntryValues();
                int j = 0;
                while (j < entries.length) {
                    if (!(entries[j] == null || entryValues[j] == null)) {
                        SettingMenuItem childMenu = new SettingMenuItem(j, entries[j].toString());
                        childMenu.setValue(entryValues[j].toString());
                        childMenu.setCommand(listPref.getCommand());
                        int mIndexFromBackUpItem = mBackUpChildItem.getChildIndex(childMenu.getValue());
                        if (mIndexFromBackUpItem != -1) {
                            childMenu.setEnable(mBackUpChildItem.getChild(mIndexFromBackUpItem).isEnable());
                        }
                        item.addChild(childMenu);
                    }
                    j++;
                }
                mBackUpChildItem.close();
            }
        }
    }

    public void closeSetting() {
        super.closeSetting();
        if (sSupportedList != null) {
            sSupportedList.clear();
            sSupportedList = null;
        }
        if (sSupportedVideoSizeList != null) {
            sSupportedVideoSizeList.clear();
            sSupportedVideoSizeList = null;
        }
        if (sSupportedPreviewSizeList != null) {
            sSupportedPreviewSizeList.clear();
            sSupportedPreviewSizeList = null;
        }
        if (this.mVideoSizeEntriesMap != null) {
            this.mVideoSizeEntriesMap.clear();
            this.mVideoSizeEntriesMap = null;
        }
        if (this.mFrameRateEntriesMap != null) {
            this.mFrameRateEntriesMap.clear();
            this.mFrameRateEntriesMap = null;
        }
        if (this.mDescMap != null) {
            this.mDescMap.clear();
            this.mDescMap = null;
        }
    }

    protected void addSetting() {
        super.addSetting();
        if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
            Setting rearSetting = getSpecificSetting(true);
            if (rearSetting != null) {
                makeVideoSizePreference(rearSetting);
                makeFrameRatePreference(rearSetting);
                makeBitratePreference(rearSetting);
            }
        }
    }

    public void refreshSettingByCameraId() {
        if (sSupportedList != null) {
            sSupportedList.clear();
            sSupportedList = null;
        }
        if (sSupportedVideoSizeList != null) {
            sSupportedVideoSizeList.clear();
            sSupportedVideoSizeList = null;
        }
        if (sSupportedPreviewSizeList != null) {
            sSupportedPreviewSizeList.clear();
            sSupportedPreviewSizeList = null;
        }
        makeSupportedList();
        makeVideoSizeEntriesMap();
        makeFrameRateEntriesMap();
        makeResolutionHashMap();
        Setting rearSetting = getSpecificSetting(true);
        if (rearSetting != null) {
            makeVideoSizePreference(rearSetting);
            makeFrameRatePreference(rearSetting);
            makeBitratePreference(rearSetting);
        }
    }

    private void makeSupportedList() {
        if (sSupportedList == null || sSupportedVideoSizeList == null || sSupportedPreviewSizeList == null) {
            sSupportedList = new ArrayList();
            sSupportedVideoSizeList = new ArrayList();
            sSupportedPreviewSizeList = new ArrayList();
            for (int manualVideoSupportedListStr : this.mQualityList) {
                for (int j = 0; j < this.mRatioList.size(); j++) {
                    String temp = MultimediaProperties.getManualVideoSupportedListStr(this.mGet.getCurrentCameraId(), manualVideoSupportedListStr, ((Float) this.mRatioList.get(j)).floatValue());
                    if (!(temp == null || "NotSupport".equalsIgnoreCase(temp))) {
                        String[] tempSplittedString = temp.split(":");
                        sSupportedList.add(temp);
                        if (tempSplittedString != null && tempSplittedString.length >= 3) {
                            sSupportedVideoSizeList.add(tempSplittedString[1]);
                            sSupportedPreviewSizeList.add(tempSplittedString[0]);
                        }
                    }
                }
            }
        }
    }

    private void makeVideoSizePreference(Setting rearSetting) {
        if (sSupportedList == null || sSupportedVideoSizeList == null || sSupportedPreviewSizeList == null) {
            makeSupportedList();
        }
        ListPreference sizePref = rearSetting.getListPreference(Setting.KEY_MANUAL_VIDEO_SIZE);
        String title = "Video size";
        if (sizePref != null) {
            title = this.mGet.getAppContext().getString(C0088R.string.video_size);
            String[] entryValue = new String[sSupportedVideoSizeList.size()];
            String[] entries = new String[sSupportedVideoSizeList.size()];
            CharSequence[] previewSize = new String[sSupportedVideoSizeList.size()];
            String[] contentDesc = new String[sSupportedVideoSizeList.size()];
            for (int i = 0; i < sSupportedVideoSizeList.size(); i++) {
                String value = (String) sSupportedVideoSizeList.get(i);
                String entry = this.mGet.getAppContext().getString(((Integer) this.mVideoSizeEntriesMap.get(value)).intValue());
                entryValue[i] = value;
                entries[i] = entry + " " + value;
                previewSize[i] = (String) sSupportedPreviewSizeList.get(i);
                contentDesc[i] = title + " " + ((String) this.mDescMap.get(value)) + " " + entries[i];
            }
            if (sizePref != null) {
                sizePref.setEntries(entries);
                sizePref.setEntryValues(entryValue);
                sizePref.setExtraInfos(previewSize, 1);
                sizePref.setMenuIconResources(null);
                sizePref.setSettingMenuIconResources(null);
                sizePref.setDescription(contentDesc);
            }
        }
    }

    private void makeFrameRatePreference(Setting rearSetting) {
        String[] frameRateList = getFrameRateList();
        if (frameRateList != null) {
            String[] entries = new String[frameRateList.length];
            String[] descList = new String[frameRateList.length];
            ListPreference frameRatePref = rearSetting.getListPreference(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
            if (frameRatePref != null) {
                String previousValue = getFpsDefaultValue(frameRatePref.getValue());
                int currentIdx = -1;
                String title = frameRatePref.getTitle();
                for (int i = 0; i < frameRateList.length; i++) {
                    String value = frameRateList[i];
                    entries[i] = (String) this.mFrameRateEntriesMap.get(value);
                    descList[i] = title + " " + value + " frame";
                    if (previousValue.equals(value)) {
                        currentIdx = i;
                    }
                }
                if (currentIdx == -1) {
                    currentIdx = frameRateList.length - 1;
                    setSelectedChild(Setting.KEY_MANUAL_VIDEO_FRAME_RATE, currentIdx, true);
                }
                String curValue = frameRateList[currentIdx];
                frameRatePref.setEntries(entries);
                frameRatePref.setEntryValues(frameRateList);
                frameRatePref.setMenuIconResources(null);
                frameRatePref.setSettingMenuIconResources(null);
                frameRatePref.setValue(curValue);
                frameRatePref.setDescription(descList);
            }
        }
    }

    private String getFpsDefaultValue(String curValue) {
        String newValue = curValue;
        if (CameraConstants.FPS_25.equals(curValue)) {
            return "30";
        }
        if (CameraConstants.FPS_50.equals(curValue)) {
            return CameraConstants.FPS_60;
        }
        return newValue;
    }

    private void makeBitratePreference(Setting rearSetting) {
        if (sSupportedList == null || sSupportedVideoSizeList == null) {
            makeSupportedList();
        }
        ListPreference bitRatePref = rearSetting.getListPreference(Setting.KEY_MANUAL_VIDEO_BITRATE);
        String currentVideoSize = rearSetting.getSettingValue(CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode()) ? Setting.KEY_VIDEO_RECORDSIZE : Setting.KEY_MANUAL_VIDEO_SIZE);
        CamLog.m3d(CameraConstants.TAG, "currentVideoSize = " + currentVideoSize);
        int supportedListidx = sSupportedVideoSizeList.indexOf(currentVideoSize);
        if (supportedListidx == -1 && CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode())) {
            ListPreference pref = rearSetting.getListPreference(Setting.KEY_VIDEO_RECORDSIZE);
            if (pref != null) {
                supportedListidx = sSupportedVideoSizeList.indexOf(pref.getDefaultValue());
                CamLog.m3d(CameraConstants.TAG, "do not support currentVideoSize. use default size : " + pref.getDefaultValue());
            }
        }
        String[] tempSplittedArray = ((String) sSupportedList.get(supportedListidx)).split(":");
        int convertedFrameRate = Integer.parseInt(rearSetting.getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE));
        int idx = 4;
        if (convertedFrameRate == 1 || convertedFrameRate == 2) {
            idx = 3;
        } else if (convertedFrameRate >= 50 && convertedFrameRate < 100) {
            idx = 5;
        } else if (convertedFrameRate >= 100 && convertedFrameRate < 200) {
            idx = 6;
        } else if (convertedFrameRate >= 200 && convertedFrameRate < 300) {
            idx = 7;
        }
        if (tempSplittedArray != null && tempSplittedArray.length > 0) {
            if (idx >= tempSplittedArray.length) {
                idx = tempSplittedArray.length - 1;
            }
            String[] bitrateList = tempSplittedArray[idx].split(",");
            if (bitRatePref != null) {
                int currentBitrateValueIdx;
                if (bitRatePref.getEntryValues().length == 0) {
                    bitRatePref.setEntryValues(bitrateList);
                    if ("Medium".equals(rearSetting.getSettingValue(Setting.KEY_MANUAL_VIDEO_BITRATE))) {
                        currentBitrateValueIdx = 1;
                    } else {
                        currentBitrateValueIdx = bitRatePref.findIndexOfValue(rearSetting.getSettingValue(Setting.KEY_MANUAL_VIDEO_BITRATE));
                    }
                } else {
                    currentBitrateValueIdx = bitRatePref.findIndexOfValue(rearSetting.getSettingValue(Setting.KEY_MANUAL_VIDEO_BITRATE));
                    bitRatePref.setEntryValues(bitrateList);
                }
                String refresedValue = bitrateList[currentBitrateValueIdx];
                bitRatePref.setDefaultValue(bitrateList[1]);
                bitRatePref.setValue(refresedValue);
            }
        }
    }

    public String getSettingDesc(String key) {
        if (this.mDescMap != null) {
            return (String) this.mDescMap.get(key);
        }
        return null;
    }

    public boolean setSetting(String key, String value, boolean save) {
        String prevFpsValue = getSpecificSetting(true).getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
        boolean result = super.setSetting(key, value, save);
        Setting rearSetting = getSpecificSetting(true);
        if (Setting.KEY_MANUAL_VIDEO_SIZE.equals(key) || Setting.KEY_MANUAL_VIDEO_FRAME_RATE.equals(key) || (CameraConstants.MODE_CINEMA.equals(this.mGet.getShotMode()) && Setting.KEY_VIDEO_RECORDSIZE.equals(key))) {
            makeFrameRatePreference(rearSetting);
            makeBitratePreference(rearSetting);
            String curFpsValue = getSpecificSetting(true).getSettingValue(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
            if (!curFpsValue.equals(prevFpsValue)) {
                this.mGet.onFrameRateListRefreshed(prevFpsValue, curFpsValue);
            }
        }
        return result;
    }

    protected void makeChildSettingMenuItemList(String key, ArrayList<SettingMenuItem> menuItemList) {
        SettingMenuItem settingMenuItem = getCameraSettingMenu().getMenuItem(key);
        if (settingMenuItem != null && menuItemList != null) {
            int i;
            SettingMenuItem childItem;
            if (Setting.KEY_MANUAL_VIDEO_FRAME_RATE.equals(key)) {
                ListPreference frameRatePref = getSpecificSetting(true).getListPreference(Setting.KEY_MANUAL_VIDEO_FRAME_RATE);
                String[] frameRateList = getFrameRateList();
                if (frameRatePref != null) {
                    for (i = 0; i < frameRateList.length; i++) {
                        childItem = new SettingMenuItem(i, (String) this.mFrameRateEntriesMap.get(frameRateList[i]));
                        childItem.setKey(key);
                        childItem.setValue(frameRatePref.getEntryValues()[i].toString());
                        childItem.setCommand(frameRatePref.getCommand());
                        menuItemList.add(childItem);
                    }
                }
            } else if (Setting.KEY_MANUAL_VIDEO_BITRATE.equals(key)) {
                ListPreference bitratePref = this.mCurPrefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_BITRATE);
                if (bitratePref != null) {
                    for (i = 0; i < settingMenuItem.getChildCount(); i++) {
                        childItem = settingMenuItem.getChild(i);
                        childItem.setKey(key);
                        childItem.setValue(bitratePref.getEntryValues()[i].toString());
                        menuItemList.add(childItem);
                    }
                }
            } else if (Setting.KEY_MANUAL_VIDEO_SIZE.equals(key)) {
                ListPreference manualVideoSizePref = this.mCurPrefGroup.findPreference(Setting.KEY_MANUAL_VIDEO_SIZE);
                if (manualVideoSizePref != null) {
                    for (i = 0; i < manualVideoSizePref.getEntryValues().length; i++) {
                        childItem = new SettingMenuItem(i, manualVideoSizePref.getEntries()[i].toString());
                        childItem.setKey(key);
                        childItem.setValue(manualVideoSizePref.getEntryValues()[i].toString());
                        menuItemList.add(childItem);
                    }
                }
            } else {
                super.makeChildSettingMenuItemList(key, menuItemList);
            }
        }
    }

    private String[] getFrameRateList() {
        if (sSupportedList == null || sSupportedList.size() == 0 || sSupportedVideoSizeList == null || sSupportedVideoSizeList.size() == 0) {
            makeSupportedList();
        }
        int supportedListidx = sSupportedVideoSizeList.indexOf(getSpecificSetting(true).getSettingValue(Setting.KEY_MANUAL_VIDEO_SIZE));
        if (supportedListidx < 0) {
            supportedListidx = 0;
        }
        return ((String) sSupportedList.get(supportedListidx)).split(":")[2].split(",");
    }
}
