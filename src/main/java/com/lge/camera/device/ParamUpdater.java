package com.lge.camera.device;

import android.content.Context;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.HandlerRunnable.OnRemoveHandler;
import java.util.HashMap;

public class ParamUpdater implements OnRemoveHandler {
    private OnParamsListener mGet = null;
    public HashMap<String, ParamRequester> mKeyMap = new HashMap();

    public interface OnParamsListener {
        Context getAppContext();

        String getSettingValue(String str);

        void removePostRunnable(Object obj);

        void runOnUiThread(Object obj);

        void setSettingMenuEnable(String str, boolean z);

        void updateUi(ParamRequester paramRequester);
    }

    public ParamUpdater(OnParamsListener listener) {
        this.mGet = listener;
    }

    public void addRequester(String keyString, String fixedValue, boolean updateUi, boolean enable) {
        if (this.mKeyMap != null && !this.mKeyMap.containsKey(keyString)) {
            this.mKeyMap.put(keyString, new ParamRequester(keyString, fixedValue, updateUi, enable));
        }
    }

    public void removeRequester(String keyString) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyString)) {
            this.mKeyMap.remove(keyString);
        }
    }

    public void releaseAllRequester() {
        if (this.mKeyMap != null) {
            this.mKeyMap.clear();
        }
    }

    public void setParameters(CameraParameters params, String keyValue, String paramValue) {
        if (params == null) {
            CamLog.m11w(CameraConstants.TAG, "Parameters is empty.");
        }
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                if (paramValue != null) {
                    requester.setFixedValue(paramValue);
                }
                executeRequest(requester, params, true);
            }
        }
    }

    public void setParameters(CameraParameters params, String keyValue, String paramValue, boolean updateUi) {
        if (params == null) {
            CamLog.m11w(CameraConstants.TAG, "Parameters is empty.");
        }
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                if (paramValue != null) {
                    requester.setFixedValue(paramValue);
                }
                executeRequest(requester, params, updateUi);
            }
        }
    }

    public String getParamValue(String keyValue) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                return requester.getFixedValue();
            }
        }
        return "not found";
    }

    public void setParamValue(String keyValue, String paramValue) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                requester.setFixedValue(paramValue);
            }
        }
    }

    public void setParamValue(String keyValue, boolean updateUi) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                requester.setUpdateUi(updateUi);
            }
        }
    }

    public void setParamValue(String keyValue, String paramValue, boolean enable) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                requester.setFixedValue(paramValue);
                requester.setEnable(enable);
            }
        }
    }

    public void setParamValue(String keyValue, String paramValue, boolean updateUi, boolean enable) {
        if (this.mKeyMap != null && this.mKeyMap.containsKey(keyValue)) {
            ParamRequester requester = (ParamRequester) this.mKeyMap.get(keyValue);
            if (requester != null) {
                requester.setFixedValue(paramValue);
                requester.setUpdateUi(updateUi);
                requester.setEnable(enable);
            }
        }
    }

    public void updateAllParameters(CameraParameters params) {
        updateAllParameters(params, true);
    }

    public void updateAllParameters(CameraParameters params, boolean isUpdateUi) {
        if (this.mKeyMap != null && this.mKeyMap.keySet() != null) {
            for (Object obj : this.mKeyMap.keySet()) {
                executeRequest((ParamRequester) this.mKeyMap.get(obj), params, isUpdateUi);
            }
        }
    }

    public void updateParameters(CameraParameters params, String key) {
        executeRequest((ParamRequester) this.mKeyMap.get(key), params, false);
    }

    private void executeRequest(final ParamRequester requester, CameraParameters params, boolean isUpdateUi) {
        if (requester != null) {
            String key = requester.getKey();
            String value = requester.getFixedValue();
            if (ParamConstants.USE_CUR_VALUE.equals(value)) {
                value = this.mGet.getSettingValue(key);
            }
            if (value == null) {
                CamLog.m3d(CameraConstants.TAG, "Exit executeRequest : key = " + key + ", value is null.");
                return;
            }
            if (!(params == null || "not found".equals(value))) {
                params.set(key, value);
            }
            if (isUpdateUi) {
                this.mGet.runOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (requester.isUpdateUi()) {
                            ParamUpdater.this.mGet.updateUi(requester);
                        }
                    }
                });
            }
        }
    }

    public void onRemoveRunnable(HandlerRunnable runnable) {
        if (this.mGet != null) {
            this.mGet.removePostRunnable(runnable);
        }
    }
}
