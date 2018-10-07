package com.lge.camera.systeminput;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.CheckStatusManager;
import com.lge.camera.util.QuickWindowUtils;

public class BatteryReceiver extends CameraBroadCastReceiver {
    public static final int BATTERY_CHARGING_CURRENT_INCOMPATIBLE_CHARGING = 2;
    public static final int BATTERY_CHARGING_CURRENT_NORMAL_CHARGING = 1;
    public static final int BATTERY_CHARGING_CURRENT_USB_DRIVER_UNINSTALLED = 4;
    public static final String BATTERY_EXTRA_CHARGING_CURRENT = "charging_current";
    public static final int UNCHARGE_LEVEL = 2;
    private Toast mToast = null;

    public BatteryReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                checkLowBattery(intent);
                actionBatteryChanged(intent);
            } else if (action.equals("android.intent.action.BATTERY_LOW")) {
                checkLowBattery(intent);
            }
            heatWarningByPowerConnection(action);
        }
    }

    private void checkLowBattery(Intent intent) {
        int level = intent.getIntExtra("level", -1);
        if (level <= 5) {
            CamLog.m3d(CameraConstants.TAG, "[indicator] battery changed to less than 15. SHOW! low battery indicator");
            this.mGet.setBatteryIndicatorVisibility(true);
        } else {
            this.mGet.setBatteryIndicatorVisibility(false);
        }
        if (level != -1 && level <= -1 && !this.mGet.getActivity().isFinishing() && !this.mGet.isActivityPaused() && CheckStatusManager.getCheckEnterOutSecure() == 0) {
            CamLog.m3d(CameraConstants.TAG, "battery level is too low!! go to finish!");
            if (QuickWindowUtils.isQuickWindowCaseClosed()) {
                if (this.mToast != null) {
                    this.mToast.cancel();
                }
                this.mToast = Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.sp_lowbattery_MLINE), 0);
                this.mToast.setGravity(49, 0, 500);
                this.mToast.show();
            } else {
                Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.sp_lowbattery_MLINE), 0).show();
            }
            this.mGet.getActivity().finish();
        }
    }

    private void actionBatteryChanged(Intent intent) {
        int chargedLevel = intent.getIntExtra("level", -1);
        int levelMax = intent.getIntExtra("scale", -1);
        if (!(chargedLevel == -1 || chargedLevel > -1 || this.mGet.getActivity().isFinishing() || this.mGet.isActivityPaused() || CheckStatusManager.getCheckEnterOutSecure() != 0)) {
            Toast.makeText(this.mGet.getAppContext(), this.mGet.getAppContext().getString(C0088R.string.sp_lowbattery_MLINE), 0).show();
            this.mGet.getActivity().finish();
        }
        if (chargedLevel == -1 || levelMax == -1) {
            CamLog.m3d(CameraConstants.TAG, "Fail to receive battery level!");
            return;
        }
        int level = calculateBatteryLevel(chargedLevel);
        int status = intent.getIntExtra("status", -1);
        int pluged = intent.getIntExtra("plugged", 0);
        float voltage = ((float) intent.getIntExtra("voltage", 0)) / 1000.0f;
        CamLog.m3d(CameraConstants.TAG, "voltage =" + voltage);
        if (ModelProperties.getCarrierCode() == 6) {
            level = setChargingState(21, level, pluged, status, intent.getIntExtra(BATTERY_EXTRA_CHARGING_CURRENT, 1));
        } else {
            level = setChargingState(21, level, pluged, status);
        }
        this.mGet.onBatteryLevelChanged(chargedLevel, level, voltage);
    }

    private int setChargingState(int tempTotalBatteryLevel, int level, int pluged, int status) {
        if (status == 2) {
            level += tempTotalBatteryLevel;
            this.mGet.setCharging(true);
            return level;
        } else if (status != 5) {
            return level;
        } else {
            if (pluged != 1 && pluged != 2) {
                return level;
            }
            level += tempTotalBatteryLevel;
            this.mGet.setCharging(true);
            return level;
        }
    }

    private int setChargingState(int tempTotalBatteryLevel, int level, int pluged, int status, int currentChargeStatus) {
        if (currentChargeStatus == 2 || (currentChargeStatus == 4 && pluged == 2)) {
            return level + (tempTotalBatteryLevel * 2);
        }
        if (status == 2) {
            level += tempTotalBatteryLevel;
            this.mGet.setCharging(true);
            return level;
        } else if (status != 5) {
            return level;
        } else {
            if (pluged != 1 && pluged != 2) {
                return level;
            }
            level += tempTotalBatteryLevel;
            this.mGet.setCharging(true);
            return level;
        }
    }

    private void heatWarningByPowerConnection(String action) {
        if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
            this.mGet.setCharging(true);
            CamLog.m3d(CameraConstants.TAG, "ACTION_POWER_CONNECTED");
            this.mGet.onPowerConnected();
        } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
            this.mGet.setCharging(false);
            CamLog.m3d(CameraConstants.TAG, "ACTION_POWER_DISCONNECTED");
            this.mGet.onPowerDisconnected();
        }
    }

    private int calculateBatteryLevel(int charged) {
        int currentCarrierCode = ModelProperties.getCarrierCode();
        if (charged < 0) {
            charged = 0;
        } else if (charged > 100) {
            charged = 100;
        }
        if (currentCarrierCode == 6) {
            if ((charged >= 21 && charged <= 22) || (charged >= 16 && charged <= 17)) {
                return (charged + 4) / 5;
            }
            if ((charged < 8 || charged > 10) && (charged < 3 || charged > 5)) {
                return (charged + 2) / 5;
            }
            return (charged - 1) / 5;
        } else if (currentCarrierCode == 5) {
            if ((charged < 21 || charged > 22) && ((charged < 16 || charged > 17) && (charged < 11 || charged > 12))) {
                return (charged + 2) / 5;
            }
            return (charged + 4) / 5;
        } else if (charged < 16 || charged > 17) {
            return (charged + 2) / 5;
        } else {
            return (charged + 4) / 5;
        }
    }
}
