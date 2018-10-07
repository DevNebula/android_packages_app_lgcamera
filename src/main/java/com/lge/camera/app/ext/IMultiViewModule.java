package com.lge.camera.app.ext;

import android.content.Context;
import android.graphics.Bitmap;
import com.lge.camera.components.MultiViewLayout;
import java.util.ArrayList;

public interface IMultiViewModule {
    void afterPreviewCaptured();

    Context getAppContext();

    Bitmap getBitmapForImport();

    int getCurrentPostViewType();

    ArrayList<MultiViewLayout> getMultiviewArrayList();

    int getOrientationDegree();

    boolean getPostviewVisibility();

    int getRecordedCameraIdForReverse();

    boolean getReverseState();

    Bitmap[] getTransformedImage();

    boolean isImportedImage();

    boolean isMultiviewFrameShot();

    boolean isPaused();

    ArrayList<MultiViewLayout> makeMultiviewLayout();

    void multiviewFrameReady();

    void resetStatus();

    void savePostViewContens(int i);

    void setBitmapToPrePostView(boolean z);

    void setReverseState(boolean z);

    void setSwapBitmapReady();
}
