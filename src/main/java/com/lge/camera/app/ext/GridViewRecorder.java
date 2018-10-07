package com.lge.camera.app.ext;

import com.lge.camera.app.ext.MultiViewRecorder.EncoderConfig;

public class GridViewRecorder extends MultiViewRecorder {
    private int[] mCameraId;
    private GridViewFrame mGridViewFrameRecord;

    public GridViewRecorder(GridViewFrame frameLayout, String shotMode) {
        super(null, shotMode);
        this.mGridViewFrameRecord = frameLayout;
    }

    public void setCameraId(int[] cameraId) {
        this.mCameraId = cameraId;
    }

    public synchronized void frameAvailable(int[] texture, float[][] texMatrix) {
    }

    protected void drawCollageFrame(int[] texture) {
        this.mGridViewFrameRecord.drawFrameCollage(texture);
    }

    protected void setupRecorder(EncoderConfig config, int degree) {
        if (this.mGridViewFrameRecord != null && config.mOutputFile != null) {
            this.mGridViewFrameRecord.init(config.mInputFileType, config.mRecordingDegrees, degree, this.mCameraId);
        }
    }
}
