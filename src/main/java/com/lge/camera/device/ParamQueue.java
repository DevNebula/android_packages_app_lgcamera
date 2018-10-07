package com.lge.camera.device;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParamQueue {
    private Queue<ParamItem> mQueue = new ConcurrentLinkedQueue();

    public boolean add(String key, int value) {
        if (this.mQueue == null) {
            return false;
        }
        return this.mQueue.add(new ParamItem(key, Integer.valueOf(value)));
    }

    public boolean add(String key, String value) {
        if (this.mQueue == null) {
            return false;
        }
        return this.mQueue.add(new ParamItem(key, value));
    }

    public ParamItem poll() {
        if (this.mQueue == null) {
            return null;
        }
        return (ParamItem) this.mQueue.poll();
    }

    public int size() {
        if (this.mQueue == null) {
            return 0;
        }
        return this.mQueue.size();
    }

    public boolean isParamQueueEmpty() {
        if (this.mQueue == null) {
            return true;
        }
        return this.mQueue.isEmpty();
    }

    public void setParameters(CameraParameters params) {
        if (this.mQueue != null && params != null) {
            while (!this.mQueue.isEmpty()) {
                ParamItem item = (ParamItem) this.mQueue.poll();
                if (item != null) {
                    String key = item.getKey();
                    Object value = item.getValue();
                    if (!setParamByValue(params, key, value) && setParamByKey(params, key, value)) {
                    }
                }
            }
        }
    }

    private boolean setParamByKey(CameraParameters params, String key, Object value) {
        if (ParamConstants.KEY_METERING_AREAS.equals(key)) {
            params.setMeteringAreas((List) value);
            return true;
        } else if (!ParamConstants.KEY_FOCUS_AREAS.equals(key)) {
            return false;
        } else {
            params.setFocusAreas((List) value);
            return true;
        }
    }

    private boolean setParamByValue(CameraParameters params, String key, Object value) {
        if (value instanceof Integer) {
            params.set(key, ((Integer) value).intValue());
            return true;
        } else if (!(value instanceof String)) {
            return false;
        } else {
            params.set(key, (String) value);
            return true;
        }
    }
}
