package com.lge.camera.device.api2;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult.Key;
import android.hardware.camera2.TotalCaptureResult;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ResultChecker {
    private Set<Integer> mHashCodeSet;
    private ArrayList<ResultEntry> mResultSet;

    class ResultEntry {
        private boolean mIgnoreMetaAferValidImage = false;
        private Key<?> mKey;
        private Object mValue;

        public ResultEntry(Key<?> key, Object value, boolean ignoreMeta) {
            this.mKey = key;
            this.mValue = value;
            this.mIgnoreMetaAferValidImage = ignoreMeta;
        }

        public boolean isValid(TotalCaptureResult result) {
            if (result == null) {
                CamLog.m7i(CameraConstants.TAG, "result null");
                return false;
            }
            CamLog.m7i(CameraConstants.TAG, "checked item " + this.mKey + " vaule " + this.mValue + " result " + result.get(this.mKey));
            return this.mValue.equals(result.get(this.mKey));
        }

        public boolean hasIgnoreMetaAfterValidImage() {
            return this.mIgnoreMetaAferValidImage;
        }
    }

    public void addCheckCondition(int requestHashcode) {
        if (this.mHashCodeSet == null) {
            this.mHashCodeSet = Collections.synchronizedSet(new HashSet());
        }
        this.mHashCodeSet.add(Integer.valueOf(requestHashcode));
    }

    public void addCheckCondition(Key<?> key, Object value, boolean ignoreMeta) {
        if (this.mResultSet == null) {
            this.mResultSet = new ArrayList();
        }
        this.mResultSet.add(new ResultEntry(key, value, ignoreMeta));
    }

    public void removeCheckCondition(int requestHashcode) {
        if (this.mHashCodeSet != null) {
            CamLog.m7i(CameraConstants.TAG, " isSucess ? " + this.mHashCodeSet.remove(Integer.valueOf(requestHashcode)));
        }
    }

    public boolean isValid(TotalCaptureResult result) {
        if (checkHashCode(result) && checkCaptureResult(result)) {
            return true;
        }
        return false;
    }

    public boolean isValid(CaptureRequest request) {
        if (this.mHashCodeSet == null) {
            return true;
        }
        return this.mHashCodeSet.contains(Integer.valueOf(request.hashCode()));
    }

    private boolean checkCaptureResult(TotalCaptureResult result) {
        if (this.mResultSet == null) {
            return true;
        }
        Iterator it = this.mResultSet.iterator();
        while (it.hasNext()) {
            if (!((ResultEntry) it.next()).isValid(result)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkHashCode(TotalCaptureResult result) {
        if (this.mHashCodeSet == null) {
            return true;
        }
        return this.mHashCodeSet.contains(Integer.valueOf(result.getRequest().hashCode()));
    }

    public boolean hasIgnoreMetaAfterValidImage() {
        if (this.mResultSet == null) {
            return false;
        }
        Iterator it = this.mResultSet.iterator();
        while (it.hasNext()) {
            if (((ResultEntry) it.next()).hasIgnoreMetaAfterValidImage()) {
                return true;
            }
        }
        return false;
    }
}
