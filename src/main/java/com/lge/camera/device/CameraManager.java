package com.lge.camera.device;

import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.os.Handler;
import android.util.Size;
import android.util.SizeF;
import com.lge.camera.device.CameraManagerBase.CameraProxyBase;
import com.lge.camera.device.ICameraCallback.CameraHistogramDataCallback;
import com.lge.camera.device.ICameraCallback.CameraImageCallback;
import com.lge.camera.device.ICameraCallback.CameraImageMetaCallback;
import com.lge.camera.device.ICameraCallback.CameraLowlightDetectionCallback;
import com.lge.camera.device.ICameraCallback.OutFocusCallback;
import com.lge.hardware.LGCamera.CameraMetaDataCallback;
import com.lge.hardware.LGCamera.CineZoomStateListener;
import com.lge.hardware.LGCamera.EVCallbackListener;
import com.lge.hardware.LGCamera.FingerDetectionDataListener;
import com.lge.hardware.LGCamera.ObtDataListener;
import java.util.List;

public interface CameraManager extends CameraManagerBase {

    public interface CameraProxy extends CameraProxyBase {
        void addRawImageCallbackBuffer(byte[] bArr);

        CameraParameters convertParameter(Parameters parameters);

        float getCameraFnumber(CameraParameters cameraParameters);

        CameraInfomation getCameraInfo();

        float getFocalLength(CameraParameters cameraParameters);

        float getHorizontalViewAngle(CameraParameters cameraParameters);

        List<Area> getMultiWindowFocusArea();

        Size getSensorActiveArrarySize(CameraParameters cameraParameters, boolean z);

        SizeF getSensorPhysicalSize(float f, float f2, float f3);

        Size getSensorPixelArraySize(CameraParameters cameraParameters, boolean z);

        float getVerticalViewAngle(CameraParameters cameraParameters);

        boolean isLowFps();

        boolean isLowLightDetected(CameraParameters cameraParameters);

        void restoreParameters(CameraParameters cameraParameters);

        void setCineZoomListener(CineZoomStateListener cineZoomStateListener);

        void setEVCallbackDataListener(EVCallbackListener eVCallbackListener);

        void setFingerDetectionDataListener(FingerDetectionDataListener fingerDetectionDataListener);

        void setGPSlocation(Location location);

        void setHistogramDataCallback(Handler handler, CameraHistogramDataCallback cameraHistogramDataCallback);

        void setImageDataCallback(int i, CameraImageCallback cameraImageCallback);

        void setImageMetaCallback(CameraImageMetaCallback cameraImageMetaCallback);

        void setLongshot(boolean z);

        void setLowlightDetectionCallback(Handler handler, CameraLowlightDetectionCallback cameraLowlightDetectionCallback);

        void setLuxIndexMetadata(LGCameraMetaDataCallback lGCameraMetaDataCallback);

        void setManualCameraMetadataCb(LGCameraMetaDataCallback lGCameraMetaDataCallback);

        CameraParameters setNightandHDRorAuto(CameraParameters cameraParameters, String str, boolean z);

        void setNightandHDRorAutoSync(CameraParameters cameraParameters, String str, boolean z);

        void setObtDataListener(ObtDataListener obtDataListener);

        void setOpticZoomMetadataCb(CameraMetaDataCallback cameraMetaDataCallback);

        void setOutFocusCallback(OutFocusCallback outFocusCallback);

        CameraParameters setSuperZoom(CameraParameters cameraParameters);

        void setSuperZoomSync(CameraParameters cameraParameters);

        void stopBurstShot();

        void updateRequestCapture(long j, int i);
    }

    boolean isLowDistance();

    void setCameraOpsModuleBridge(CameraOpsModuleBridge cameraOpsModuleBridge);

    void setLGProxyDataListener(boolean z);

    void setParamToBackup(String str, Object obj);
}
