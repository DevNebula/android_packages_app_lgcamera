package com.lge.camera.managers;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class MyLocationListener implements LocationListener {
    private MyLocationListenerFunction mGet = null;
    private Location mLastLocation;
    private String mProvider;
    private boolean mValid = false;

    public interface MyLocationListenerFunction {
        boolean isOnGpsSetting();
    }

    public MyLocationListener(MyLocationListenerFunction function, String provider) {
        this.mGet = function;
        this.mProvider = provider;
        this.mLastLocation = new Location(this.mProvider);
    }

    public void onLocationChanged(Location newLocation) {
        Double location = Double.valueOf(0.0d);
        this.mLastLocation.set(newLocation);
        this.mValid = true;
        try {
            if ((this.mGet == null || this.mGet.isOnGpsSetting()) && location.compareTo(Double.valueOf(newLocation.getLatitude())) == 0 && location.compareTo(Double.valueOf(newLocation.getLongitude())) == 0) {
            }
        } catch (NullPointerException e) {
            CamLog.m3d(CameraConstants.TAG, "LocationListener onLocationChanged" + e);
        }
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
        this.mValid = false;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        CamLog.m7i(CameraConstants.TAG, "LocationListener onStatusChanged status:" + status);
        if (this.mGet == null || this.mGet.isOnGpsSetting()) {
            switch (status) {
                case 0:
                case 1:
                    if (!this.mValid || status != 1) {
                        this.mValid = false;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public Location current() {
        return this.mValid ? this.mLastLocation : null;
    }

    public void resetVaild() {
        this.mValid = false;
    }
}
