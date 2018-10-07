package com.lge.camera.managers;

import android.location.Location;
import android.location.LocationManager;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.managers.MyLocationListener.MyLocationListenerFunction;
import com.lge.camera.util.CamLog;

public class LocationServiceManager extends ManagerInterfaceImpl implements MyLocationListenerFunction {
    private boolean mGpsAvailable = false;
    private OnLocationListener mListener = null;
    private MyLocationListener[] mLocationListeners = null;
    private LocationManager mLocationManager = null;
    private boolean mRecordLocation = false;

    public interface OnLocationListener {
        boolean isOnGpsSetting();
    }

    public LocationServiceManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        this.mLocationManager = (LocationManager) getActivity().getSystemService("location");
        this.mLocationListeners = new MyLocationListener[]{new MyLocationListener(this, "gps"), new MyLocationListener(this, "network")};
    }

    public void setLocationListener(OnLocationListener listener) {
        this.mListener = listener;
    }

    public void startReceivingLocationUpdates() {
        CamLog.m3d(CameraConstants.TAG, "startReceivingLocationUpdates()");
        if ((this.mListener == null || this.mListener.isOnGpsSetting()) && this.mLocationManager != null) {
            try {
                this.mLocationManager.requestLocationUpdates("network", 1000, 0.0f, this.mLocationListeners[1]);
            } catch (SecurityException ex) {
                CamLog.m8i(CameraConstants.TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex2) {
                CamLog.m3d(CameraConstants.TAG, "provider does not exist " + ex2.getMessage());
            }
            try {
                this.mLocationManager.requestLocationUpdates("gps", 1000, 0.0f, this.mLocationListeners[0]);
            } catch (SecurityException ex3) {
                CamLog.m8i(CameraConstants.TAG, "fail to request location update, ignore", ex3);
            } catch (IllegalArgumentException ex22) {
                CamLog.m3d(CameraConstants.TAG, "provider does not exist " + ex22.getMessage());
            }
        }
    }

    public void stopReceivingLocationUpdates() {
        CamLog.m3d(CameraConstants.TAG, "stopReceivingLocationUpdates");
        if (this.mLocationManager != null && this.mLocationListeners != null) {
            for (int i = 0; i < this.mLocationListeners.length; i++) {
                try {
                    this.mLocationManager.removeUpdates(this.mLocationListeners[i]);
                    this.mLocationListeners[i].resetVaild();
                } catch (Exception ex) {
                    CamLog.m8i(CameraConstants.TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    public Location getCurrentLocation() {
        if ((this.mListener != null && !this.mListener.isOnGpsSetting()) || this.mLocationListeners == null) {
            return null;
        }
        for (MyLocationListener current : this.mLocationListeners) {
            Location location = current.current();
            if (location != null) {
                return location;
            }
        }
        CamLog.m7i(CameraConstants.TAG, "getCurrentLocation return = null");
        return null;
    }

    public boolean setGPSlocation(CameraParameters parameter, Location loc) {
        if (loc != null) {
            Double latitude = Double.valueOf(loc.getLatitude());
            Double longitude = Double.valueOf(loc.getLongitude());
            Double pivot = Double.valueOf(0.0d);
            boolean hasLatLon = (latitude.compareTo(pivot) == 0 && longitude.compareTo(pivot) == 0) ? false : true;
            if (hasLatLon) {
                parameter.setGpsLatitude(latitude.doubleValue());
                parameter.setGpsLongitude(longitude.doubleValue());
                if (loc.hasAltitude()) {
                    Double altitude = Double.valueOf(loc.getAltitude());
                    long altitudeDividend = Double.valueOf(altitude.doubleValue() * 1000.0d).longValue();
                    if (altitudeDividend < 0) {
                        altitudeDividend *= -1;
                    }
                    parameter.setGpsAltitude(altitude.doubleValue());
                } else {
                    parameter.setGpsAltitude(0.0d);
                }
                if (loc.getTime() != 0) {
                    parameter.setGpsTimestamp(loc.getTime() / 1000);
                }
                this.mGpsAvailable = true;
                return true;
            } else if (!this.mGpsAvailable) {
                return false;
            } else {
                this.mGpsAvailable = false;
                parameter.removeGpsData();
                return true;
            }
        } else if (!this.mGpsAvailable) {
            return false;
        } else {
            this.mGpsAvailable = false;
            parameter.removeGpsData();
            return true;
        }
    }

    public boolean isOnGpsSetting() {
        if (this.mListener != null) {
            return this.mListener.isOnGpsSetting();
        }
        return false;
    }

    public boolean getRecordLocation() {
        return this.mRecordLocation;
    }

    public void setRecordLocation(boolean set) {
        this.mRecordLocation = set;
    }

    public void onResumeBefore() {
        if (this.mListener == null || !this.mListener.isOnGpsSetting()) {
            this.mRecordLocation = false;
            return;
        }
        this.mRecordLocation = true;
        startReceivingLocationUpdates();
    }

    public void onPauseAfter() {
        this.mRecordLocation = false;
        stopReceivingLocationUpdates();
    }

    public void onDestroy() {
        this.mListener = null;
        this.mLocationManager = null;
        this.mLocationListeners = null;
        super.onDestroy();
    }

    public void setDegree(int degree, boolean animation) {
    }
}
