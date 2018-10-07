package com.lge.camera.components;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class GridViewLayout {
    protected final int FIRST_INDEX = 0;
    protected final int FIRST_VIEW = 0;
    protected final int FOURTH_VIEW = 3;
    protected final int SECOND_VIEW = 1;
    protected final int THIRD_VIEW = 2;
    protected GridViewLayoutInfo mInfo;
    protected final float[] mTextureCoordCollage = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f};

    public GridViewLayout(int degree, GridViewLayoutInfo info) {
        this.mInfo = info;
        initTextureCoord(degree);
    }

    public float[] getOriginalVertices() {
        return this.mInfo.getOriginalVertices();
    }

    public float[] getCurVertex() {
        return this.mInfo.getCurVertices();
    }

    public float[] getTexCoordCollage(int[] inputFileType, ArrayList<Integer> recordingDegrees) {
        return getTexCoordCollage(this.mInfo.getCurTextureCoord(), inputFileType, recordingDegrees, this.mInfo.getCurCameraIdArray());
    }

    public float[] getCurTexCoord() {
        return this.mInfo.getCurTextureCoord();
    }

    protected float[] calculateTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getTextureVariation(0), cameraId * 8, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    protected float[] getFrontCollageTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getFrontCollageTex(0), 0, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    protected float[] getFrontFilmCollageTextureCoord(float[] texture, int viewIndex, int cameraId, int degree) {
        System.arraycopy(this.mInfo.getFrontFilmCollageTexture(0), (cameraId - 10) * 8, texture, viewIndex * 8, 8);
        return (float[]) texture.clone();
    }

    protected void setCurVertex(float[] vertices) {
        this.mInfo.setCurVertices(vertices);
    }

    protected void setCurTexCoord(float[] coord) {
        this.mInfo.setCurTextureCoord(coord);
    }

    public void restoreCurTexture(int degree) {
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        for (int i = 0; i < curCameraArray.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-gridview- restoreCurTexture cameraId = " + curCameraArray[i]);
            curTexture = calculateTextureCoord(curTexture, i, curCameraArray[i], degree);
            setCurTexCoord((float[]) curTexture.clone());
        }
    }

    public void initTextureCoord(int degree) {
        int[] cameraArray = getCameraIdArray();
        float[] curTexture = getCurTexCoord();
        for (int i = 0; i < cameraArray.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-gridview- initTextureCoord cameraId = " + cameraArray[i]);
            curTexture = calculateTextureCoord(curTexture, i, cameraArray[i], degree);
            setCurTexCoord((float[]) curTexture.clone());
        }
    }

    protected float[] getTexCoordCollage(float[] texCoord, int[] inputFileType, ArrayList<Integer> recordingDegrees, int[] cameraId) {
        if (inputFileType.length > cameraId.length) {
            CamLog.m5e(CameraConstants.TAG, "-gridview- Error input file type length should less than cameraId length");
            return (float[]) texCoord.clone();
        }
        float[] textureCoord = (float[]) texCoord.clone();
        for (int i = 0; i < inputFileType.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-gridview- inputFileType [" + i + "] = " + inputFileType[i]);
            if (inputFileType[i] == 1) {
                int curCameraId = cameraId[i];
                CamLog.m3d(CameraConstants.TAG, "-gridview- image tex = " + i + ", cameraId = " + curCameraId);
                System.arraycopy(this.mTextureCoordCollage, curCameraId * 8, textureCoord, i * 8, 8);
            } else {
                CamLog.m3d(CameraConstants.TAG, "-gridview- video tex = " + i + ", cameraId = " + cameraId[i]);
                if (cameraId[i] == 1) {
                    textureCoord = getFrontCollageTextureCoord(textureCoord, i, cameraId[i], ((Integer) recordingDegrees.get(i)).intValue());
                } else if (cameraId[i] == 0) {
                    textureCoord = calculateTextureCoord(textureCoord, i, cameraId[i], ((Integer) recordingDegrees.get(i)).intValue());
                } else {
                    textureCoord = getFrontFilmCollageTextureCoord(textureCoord, i, cameraId[i], ((Integer) recordingDegrees.get(i)).intValue());
                }
            }
        }
        return (float[]) textureCoord.clone();
    }

    protected int[] getCameraIdArray() {
        return this.mInfo.getCameraIdArray();
    }

    public int[] getCurCameraIdArray() {
        return this.mInfo.getCurCameraIdArray();
    }

    public void setCameraIdArray(int[] isRearCam) {
        this.mInfo.setCameraIdArray(isRearCam);
    }
}
