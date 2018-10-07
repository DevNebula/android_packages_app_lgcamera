package com.lge.camera.managers;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import com.lge.graphy.data.GraphyItem;

public class GraphyControlManager extends ManagerInterfaceImpl {
    private static final float EV_MAX = 1.0f;
    private static final float EV_MIN = -1.0f;
    HandlerRunnable mCheckEVRunnable = new HandlerRunnable(this) {
        public void handleRun() {
            GraphyControlManager.this.checkEVValue();
        }
    };
    private GraphyInterface mGraphyGet = null;
    private boolean mIsGraphyAppInstalled = false;

    /* renamed from: com.lge.camera.managers.GraphyControlManager$1 */
    class C09731 implements Runnable {
        C09731() {
        }

        public void run() {
            GraphyControlManager.this.mIsGraphyAppInstalled = GraphyControlManager.this.checkGraphyAppInstalled();
        }
    }

    public GraphyControlManager(GraphyInterface moduleInterface) {
        super(moduleInterface);
        this.mGraphyGet = moduleInterface;
        CamLog.m3d(CameraConstants.TAG, "[Graphy] GraphyControlManager created");
    }

    public void OnItemSelected(GraphyItem item, boolean init) {
        if (item.getType() == 1) {
            OnGraphyImageItemSelected(item, init);
        } else if (item.getType() == 0) {
            OnCategoryItemSelected(item);
        } else if (item.getType() == 3) {
            OnNoneItemSelected();
        } else if (item.getType() == 2) {
            gotoGraphyApp();
        }
    }

    public void onResumeAfter() {
        super.onResumeAfter();
        new Thread(new C09731()).start();
    }

    private void OnGraphyImageItemSelected(GraphyItem item, boolean init) {
        String iso = trimValue(item.getStringValue(GraphyItem.KEY_ISO_STR));
        String shutterSpeed = trimValue(item.getStringValue(GraphyItem.KEY_SHUTTER_SPEED_STR));
        String wbType = trimValue(item.getStringValue(GraphyItem.KEY_WB_TYPE_STR));
        String wb = trimValue(item.getStringValue(GraphyItem.KEY_WB_STR));
        String angle = trimValue(item.getStringValue(GraphyItem.KEY_ANGLE));
        CamLog.m3d(CameraConstants.TAG, "[Graphy] id : " + item.getIntValue("_id"));
        CamLog.m3d(CameraConstants.TAG, "[Graphy] iso : " + iso);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] shutter speed : " + shutterSpeed);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] white balance type : " + wbType);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] white balance : " + wb);
        CamLog.m3d(CameraConstants.TAG, "[Graphy] angle : " + angle);
        if (!this.mGraphyGet.getAELock()) {
            this.mGraphyGet.setAELock(Boolean.valueOf(true));
            this.mGraphyGet.setButtonLocked(true, 2);
        }
        this.mGraphyGet.setGraphyData(iso, shutterSpeed, wb);
        if ("0".equals(angle)) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] normal angle");
            this.mGraphyGet.changeCameraAngle(false);
        } else if ("2".equals(angle)) {
            CamLog.m3d(CameraConstants.TAG, "[Graphy] wide angle");
            this.mGraphyGet.changeCameraAngle(true);
        }
        this.mGraphyGet.setButtonLocked(init, 13);
        this.mGraphyGet.setEVGuideLayoutVisibility(false, false);
        this.mGet.removePostRunnable(this.mCheckEVRunnable);
        if (!init) {
            this.mGet.postOnUiThread(this.mCheckEVRunnable, 900);
        }
    }

    private String trimValue(String s) {
        String tmp = s;
        if (tmp != null) {
            return tmp.trim();
        }
        return tmp;
    }

    private void OnCategoryItemSelected(GraphyItem item) {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] category name : " + item.getStringValue(GraphyItem.KEY_CATEGORY_NAME_STR));
        int categoryId = item.getIntValue(GraphyItem.KEY_CATEGORY_ID_INT);
        if (categoryId == 1) {
            if (this.mGraphyGet.isFoldedBestItem()) {
                this.mGraphyGet.attachBestImageItems();
            } else {
                this.mGraphyGet.removeBestImageItems();
            }
        } else if (categoryId != 2) {
        } else {
            if (this.mGraphyGet.isFoldedMyFilterItem()) {
                this.mGraphyGet.attachMyFilterImageItems();
            } else {
                this.mGraphyGet.removeMyFilterImageItems();
            }
        }
    }

    private void OnNoneItemSelected() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy] OnNoneItemSelected");
        this.mGraphyGet.setEVGuideLayoutVisibility(false, false);
        this.mGraphyGet.setAllAuto();
    }

    private void setGraphyData(String iso, String shutterSpeed, String wb, String wbType) {
        if (iso != null) {
            ManualData isoData = this.mGraphyGet.getManualData(8);
            isoData.setValue(iso);
            if (!"auto".equals(iso.trim())) {
                isoData.setValue(Float.valueOf(iso).floatValue());
            }
            CamLog.m3d(CameraConstants.TAG, "[Graphy] isoData.matchValue() : " + isoData.matchValue());
            this.mGraphyGet.setManualDataByGraphy(8, ParamConstants.KEY_MANUAL_ISO, isoData.matchValue(), false);
        }
        if (shutterSpeed != null) {
            this.mGraphyGet.setManualDataByGraphy(1, "shutter-speed", shutterSpeed, false);
        }
        if (wbType == null) {
            return;
        }
        if ("auto".equals(wbType)) {
            if (wb != null) {
                this.mGraphyGet.setManualDataByGraphy(4, "lg-wb", wb, false);
            }
        } else if (wb != null) {
            this.mGraphyGet.setManualDataByGraphy(4, "lg-wb", wb, false);
        }
    }

    private void checkEVValue() {
        ManualData evData = this.mGraphyGet.getManualData(2);
        if (evData != null) {
            String evValue = evData.getUserInfoValue();
            CamLog.m3d(CameraConstants.TAG, "[Graphy] evValue : " + evValue);
            try {
                float ev = Float.parseFloat(evValue);
                CamLog.m3d(CameraConstants.TAG, "[Graphy] ev : " + ev);
                if (ev > 1.0f) {
                    this.mGraphyGet.setEVGuideLayoutVisibility(true, true);
                } else if (ev < EV_MIN) {
                    this.mGraphyGet.setEVGuideLayoutVisibility(true, false);
                }
            } catch (Exception e) {
                CamLog.m5e(CameraConstants.TAG, e.getMessage());
            }
        }
    }

    private void gotoGraphyApp() {
        if (isGrahyAppInstalled()) {
            Intent intent = new Intent(CameraConstantsEx.GRAPHY_LENS_INTENT_ACTION);
            intent.setClassName(GraphyDataManager.GRAPHY_PACKAGE_NAME, GraphyDataManager.GRAPHY_CLASS_NAME);
            if (intent != null) {
                try {
                    getActivity().startActivityForResult(intent, 1000);
                } catch (Exception e) {
                    intent = getAppContext().getPackageManager().getLaunchIntentForPackage(GraphyDataManager.GRAPHY_PACKAGE_NAME);
                    if (intent != null) {
                        intent.putExtra("graphy_show_filter", true);
                        getActivity().startActivity(intent);
                    }
                }
            } else {
                CamLog.m5e(CameraConstants.TAG, "intent is null, can not launch graphy app.");
            }
        } else {
            this.mGet.showDialog(14, false);
        }
        LdbUtil.sendLDBIntent(getAppContext(), LdbConstants.LDB_FEATURE_NAME_GRAPHY_GOTOGRAPHY);
    }

    private boolean checkGraphyAppInstalled() {
        CamLog.m3d(CameraConstants.TAG, "[Graphy ]checkGraphyAppInstalled");
        try {
            if (getActivity().getPackageManager().getPackageInfo(GraphyDataManager.GRAPHY_PACKAGE_NAME, 1) == null) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public boolean isGrahyAppInstalled() {
        return this.mIsGraphyAppInstalled;
    }

    public int getLastGraphyIndex() {
        return getAppContext().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).getInt("graphy_idx", -1);
    }

    public void saveLastGraphyIndex(int index) {
        Editor editor = getAppContext().getSharedPreferences(SharedPreferenceUtilBase.SETTING_PRIMARY, 0).edit();
        editor.putInt("graphy_idx", index);
        editor.apply();
    }
}
