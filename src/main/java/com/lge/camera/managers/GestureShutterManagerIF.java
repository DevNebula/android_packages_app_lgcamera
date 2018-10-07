package com.lge.camera.managers;

import android.media.Image;
import android.util.Size;
import com.lge.camera.device.CameraManager.CameraProxy;

public class GestureShutterManagerIF extends ManagerInterfaceImpl {

    public interface OnGestureRecogListener {
        void doTimershotByGestureRecog(int i);

        void onHideQuickView();

        void onShowQuickView();
    }

    public interface onGestureUIListener {
        void onHideGestureGuide();

        void onShowGestureGuide();
    }

    public GestureShutterManagerIF(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setGestureRecogEngineListener(OnGestureRecogListener listener) {
    }

    public void setGestureUIListener(onGestureUIListener listener) {
    }

    public void runGestureEngine(boolean useOnlyMotionQuickview) {
    }

    public void initLayout() {
    }

    public void releaseLayout() {
    }

    public void initGestureEngine() {
    }

    public void initMotionEngine() {
    }

    public void showGestureGuide() {
    }

    public void hideGestureGuide() {
    }

    public boolean getGesutreGuideVisibility() {
        return false;
    }

    public void startGestureEngine() {
    }

    public void stopGestureEngine() {
    }

    public void resetGuideRectArea() {
    }

    public void releaseGestureEngine() {
    }

    public void setPreviewBuffer(byte[] previewBuf) {
    }

    public int getGestureEngineStatus() {
        return 0;
    }

    public void onPreviewFrame(byte[] data, CameraProxy camera) {
    }

    public void onImageData(Image img) {
    }

    public void startMotionEngine() {
    }

    public void resumePushMotionEngine() {
    }

    public void stopMotionEngine() {
    }

    public boolean isAvailableGestureShutterStarted() {
        return false;
    }

    public boolean isAvailableMotionQuickView() {
        return false;
    }

    public boolean isAvailableIntervalShot() {
        return false;
    }

    public int getGestureCaptureType() {
        return 0;
    }

    public void showGestureEnteringGuide() {
    }

    public void hideGestureEnteringGuide() {
    }

    public void resetGestureCaptureType() {
    }

    public void setPreviewSize(Size previewSize) {
    }
}
