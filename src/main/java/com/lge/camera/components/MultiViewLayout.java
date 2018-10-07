package com.lge.camera.components;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public abstract class MultiViewLayout extends MultiViewLayoutBase {
    private boolean isBackupedInfo = false;
    protected ArrayList<int[]> mGuideTextLocationArray = new ArrayList();
    protected ArrayList<int[]> mRecordingUILocationArray = new ArrayList();
    protected final float[] mTextureCoordCollage = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private int[] prevCameraId = new int[]{0, 0};

    public abstract Point getCenterPoint();

    public float[] getOriginalVertices() {
        return this.mInfo.getOriginalVertices();
    }

    public float[] getCurVertex() {
        return this.mInfo.getCurVertices();
    }

    public float[] getTexCoordCollage(int[] inputFileType, ArrayList<Integer> recordingDegrees) {
        return getTexCoordCollage(this.mInfo.getCurTextureCoord(), inputFileType, recordingDegrees, this.mInfo.getCurCameraIdArray());
    }

    public float[] getTexCoordCollage(int index) {
        this.mInfo.setCurTextureCoord(getTexCoordCollage(this.mInfo.getCurTextureCoord(), this.mInfo.getCurCameraIdArray()[index], index));
        return this.mInfo.getCurTextureCoord();
    }

    public float[] getCurTexCoord() {
        return this.mInfo.getCurTextureCoord();
    }

    public int[] getCurCameraIdArray() {
        return this.mInfo.getCurCameraIdArray();
    }

    public float[] getExternalCameraTexCoord(int viewIndex) {
        return this.mInfo.getTextureVariationExtCam(viewIndex);
    }

    protected int[] getCameraIdArray() {
        return this.mInfo.getCameraIdArray();
    }

    protected void setCurVertex(float[] vertices) {
        this.mInfo.setCurVertices(vertices);
    }

    protected void setCurTexCoord(float[] coord) {
        this.mInfo.setCurTextureCoord(coord);
    }

    public ArrayList<Integer> getDrawableArray() {
        return this.mInfo.getDrawableArray();
    }

    public static void setLCDSize(int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "setLCDSize width = " + width + " height = " + height);
        sLcdWidth = width;
        sLcdHeight = height;
    }

    public int getLayoutIndex() {
        return this.mLayoutIndex;
    }

    public void setLayoutIndex(int index) {
        this.mLayoutIndex = index;
    }

    public void setCurPreviewRatio(int ratio) {
        this.mInfo.setCurPreviewRatio(ratio);
    }

    public void setNumOfCameras(int num, boolean removeExtCamId) {
        CamLog.m3d(CameraConstants.TAG, "-ext- setNumOfCameras = " + num + " removeExtCamId = " + removeExtCamId);
        sNumOfCameras = num;
        if (sNumOfCameras == 2 && removeExtCamId) {
            removeNetworkCamId();
        }
    }

    public void setNetworkCamIdWithInitialCamId() {
        this.mInfo.setNetworkCamIdWithInitialCamId();
    }

    public void removeNetworkCamId() {
        this.mInfo.removeNetworkCamId();
    }

    public int getNumOfCameras() {
        return sNumOfCameras;
    }

    public void restoreCurTexture(int degree) {
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        for (int i = 0; i < curCameraArray.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- restoreCurTexture cameraId = " + curCameraArray[i]);
            curTexture = calculateTextureCoord(curTexture, i, curCameraArray[i], degree);
            setCurTexCoord((float[]) curTexture.clone());
        }
    }

    public void initTextureCoord(int degree, int capturedIndex) {
        int[] cameraArray = getCameraIdArray();
        float[] curTexture = getCurTexCoord();
        for (int i = 0; i < cameraArray.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- initTextureCoord cameraId = " + cameraArray[i]);
            curTexture = calculateTextureCoord(curTexture, i, cameraArray[i], degree);
            setCurTexCoord((float[]) curTexture.clone());
        }
    }

    public void changeCamera(int viewIndex, int degree) {
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        int maxNum = sNumOfCameras + 1;
        switch (curCameraArray[viewIndex]) {
            case 0:
                if (!FunctionProperties.multiviewFrontRearWideSupported()) {
                    curCameraArray[viewIndex] = 1;
                    break;
                } else {
                    curCameraArray[viewIndex] = 3;
                    break;
                }
            case 1:
                if (maxNum != 3) {
                    curCameraArray[viewIndex] = 4;
                    break;
                } else {
                    curCameraArray[viewIndex] = 2;
                    break;
                }
            case 2:
                if (!LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_SPLICE_TXT.equals(getLayoutName())) {
                    curCameraArray[viewIndex] = 0;
                    break;
                } else {
                    curCameraArray[viewIndex] = 3;
                    break;
                }
            case 3:
                if (!LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_SPLICE_TXT.equals(getLayoutName())) {
                    curCameraArray[viewIndex] = 1;
                    break;
                } else {
                    curCameraArray[viewIndex] = 2;
                    break;
                }
            case 4:
                curCameraArray[viewIndex] = 2;
                break;
        }
        CamLog.m3d(CameraConstants.TAG, "-ext- camId = " + curCameraArray[viewIndex] + " mNumOfCameras = " + sNumOfCameras);
        setCurTexCoord((float[]) calculateTextureCoord(curTexture, viewIndex, curCameraArray[viewIndex], degree).clone());
    }

    public void changeCamera(int viewIndex, int toCamera, int degree) {
        CamLog.m3d(CameraConstants.TAG, "viewIndex : " + viewIndex + ", toCamera : " + toCamera);
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        curCameraArray[viewIndex] = toCamera;
        setCurTexCoord((float[]) calculateTextureCoord(curTexture, viewIndex, curCameraArray[viewIndex], degree).clone());
    }

    public void setExternalCameraView(int viewIndex) {
        int[] curCameraArray = getCurCameraIdArray();
        curCameraArray[viewIndex] = 4;
        CamLog.m3d(CameraConstants.TAG, "camId = " + curCameraArray[viewIndex]);
    }

    public boolean isSetExternalCamId() {
        int[] curCameraArray = getCurCameraIdArray();
        for (int i : curCameraArray) {
            if (i == 4) {
                return true;
            }
        }
        return false;
    }

    protected float[] flipVertex(float[] vertex, int viewIndex) {
        float[] flippedVertex = (float[]) vertex.clone();
        System.arraycopy(vertex, viewIndex * 8, flippedVertex, (viewIndex * 8) + 4, 4);
        System.arraycopy(vertex, (viewIndex * 8) + 4, flippedVertex, viewIndex * 8, 4);
        return (float[]) flippedVertex.clone();
    }

    protected float[] cropVertex(float[] totalVertex, int viewIndex, int size) {
        float[] viewVertex = new float[8];
        int j = viewIndex * 8;
        if ((viewIndex * 8) + size > totalVertex.length) {
            return null;
        }
        for (int i = 0; i < 8; i++) {
            viewVertex[i] = totalVertex[j];
            j++;
        }
        return (float[]) viewVertex.clone();
    }

    protected float[] getTexCoordCollage(float[] texCoord, int[] inputFileType, ArrayList<Integer> recordingDegrees, int[] cameraId) {
        if (inputFileType.length > cameraId.length) {
            CamLog.m5e(CameraConstants.TAG, "-multiview- Error input file type length should less than cameraId length");
            return (float[]) texCoord.clone();
        }
        float[] textureCoord = (float[]) texCoord.clone();
        int i = 0;
        while (i < inputFileType.length) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- inputFileType [" + i + "] = " + inputFileType[i]);
            if (inputFileType[i] == 1) {
                int curCameraId = cameraId[i];
                CamLog.m3d(CameraConstants.TAG, "-multiview- image tex = " + i + ", cameraId = " + curCameraId);
                System.arraycopy(this.mTextureCoordCollage, curCameraId * 8, textureCoord, i * 8, 8);
            } else {
                CamLog.m3d(CameraConstants.TAG, "-multiview- video tex = " + i + ", cameraId = " + cameraId[i]);
                if (cameraId[i] == 1 || cameraId[i] == 3) {
                    textureCoord = getFrontCollageTextureCoord(textureCoord, i, cameraId[i], ((Integer) recordingDegrees.get(i)).intValue());
                } else {
                    textureCoord = calculateTextureCoord(textureCoord, i, cameraId[i], ((Integer) recordingDegrees.get(i)).intValue());
                }
            }
            i++;
        }
        return (float[]) textureCoord.clone();
    }

    protected float[] getTexCoordCollage(float[] texCoord, int cameraId, int index) {
        CamLog.m3d(CameraConstants.TAG, "-texture- cameraId = " + cameraId + " index = " + index);
        float[] textureCoord = (float[]) texCoord.clone();
        if (cameraId == 5) {
            cameraId = this.mRotateForGuidePhoto + 5;
        }
        System.arraycopy(this.mTextureCoordCollage, cameraId * 8, textureCoord, index * 8, 8);
        return (float[]) textureCoord.clone();
    }

    public float[] getTexCoordCollageForImportImage(int index) {
        int cameraId = this.mInfo.getCurCameraIdArray()[index];
        CamLog.m3d(CameraConstants.TAG, "-texture- cameraId = " + cameraId + " index = " + index);
        float[] textureCoord = this.mInfo.getCurTextureCoord();
        if (cameraId == 5) {
            System.arraycopy(this.mTextureCoordCollage, (this.mRotateForGuidePhoto + 5) * 8, textureCoord, index * 8, 8);
        } else {
            System.arraycopy(this.mInfo.getTextureVariation(0), cameraId * 8, textureCoord, index * 8, 8);
        }
        return (float[]) textureCoord.clone();
    }

    protected void setRecordingUILocation(Context context) {
        int screenWidth = Utils.getLCDsize(context, true)[1];
        int recIconWidth = (int) context.getResources().getDimension(C0088R.dimen.arc_progress_height);
        for (int i = 0; i < getFrameShotMaxCount(); i++) {
            Rect rect = getViewRect(i);
            int start = rect.right - recIconWidth;
            int top = rect.top;
            if (i == 2 && LdbConstants.LDB_MULTIVIEW_LAYOUT_QUAD_TXT.equals(getLayoutName())) {
                start = rect.right - recIconWidth;
                top += (context.getResources().getDrawable(C0088R.drawable.camera_multiview_bg_type_02).getMinimumWidth() * 2) / 3;
            } else if (LdbConstants.LDB_MULTIVIEW_LAYOUT_SPLIT_SPLICE_TXT.equals(getLayoutName())) {
                start = (int) (((float) screenWidth) * 0.86f);
                top += (int) (((float) screenWidth) * 0.05f);
            }
            this.mRecordingUILocationArray.add(new int[]{start, top});
        }
    }

    public int[] getRecordingUILocation(int index, Context context) {
        this.mRecordingUILocationArray.clear();
        setRecordingUILocation(context);
        if (this.mRecordingUILocationArray != null) {
            return (int[]) this.mRecordingUILocationArray.get(index);
        }
        return new int[]{0, 0};
    }

    protected ArrayList<Integer> getCameraIdIndex(int cameraId) {
        ArrayList<Integer> listIndex = new ArrayList();
        for (int i = 0; i < getCurCameraIdArray().length; i++) {
            if (getCurCameraIdArray()[i] == cameraId) {
                listIndex.add(Integer.valueOf(i));
            }
        }
        return listIndex;
    }

    protected int getOrientationIndex(int degree) {
        if (degree == 90) {
            return 1;
        }
        if (degree == 180) {
            return 2;
        }
        if (degree == 270) {
            return 3;
        }
        return 0;
    }

    public void setPrevCameraId() {
        if (!this.isBackupedInfo) {
            int[] curCameraArray = getCurCameraIdArray();
            this.prevCameraId[0] = curCameraArray[0];
            this.prevCameraId[1] = curCameraArray[1];
            this.isBackupedInfo = true;
        }
    }

    public void setGuidePhotoView(int viewIndex, int degree) {
        int[] curCameraArray = getCurCameraIdArray();
        curCameraArray[viewIndex] = 5;
        CamLog.m3d(CameraConstants.TAG, "camId = " + curCameraArray[viewIndex]);
        rotateView(degree);
    }

    public void releaseGuidePhotoView() {
        int i;
        int[] curCameraArray = getCurCameraIdArray();
        boolean checkGuidePhoto = false;
        int index = 0;
        for (i = 0; i < curCameraArray.length; i++) {
            if (curCameraArray[i] > 4) {
                checkGuidePhoto = true;
                index = i;
                break;
            }
        }
        if (checkGuidePhoto) {
            curCameraArray[index] = this.prevCameraId[index];
            float[] origTexture = this.mInfo.getTextureVariation(0);
            float[] curTexture = getCurTexCoord();
            for (i = 0; i < curCameraArray.length; i++) {
                if (curCameraArray[i] > 4) {
                    curCameraArray[i] = 2;
                }
                System.arraycopy(origTexture, curCameraArray[i] * 8, curTexture, i * 8, 8);
            }
            setCurTexCoord((float[]) curTexture.clone());
            this.isBackupedInfo = false;
        }
    }

    public void printfloatArray(float[] array) {
        for (int i = 0; i < array.length; i++) {
            CamLog.m3d(CameraConstants.TAG, "-pr- [" + i + "] = " + array[i]);
        }
    }

    public void swapView(int degree) {
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        int temp = curCameraArray[1];
        curCameraArray[1] = curCameraArray[0];
        curCameraArray[0] = temp;
        curTexture = swapArray(curTexture);
        temp = this.prevCameraId[1];
        this.prevCameraId[1] = this.prevCameraId[0];
        this.prevCameraId[0] = temp;
        setCurTexCoord((float[]) curTexture.clone());
    }

    private float[] swapArray(float[] array) {
        for (int i = 0; i < 8; i++) {
            float temp = array[i + 8];
            array[i + 8] = array[i];
            array[i] = temp;
        }
        return array;
    }

    public int getRotateState() {
        return this.mRotateForGuidePhoto;
    }

    public void setRotateStateIncremental() {
        this.mRotateForGuidePhoto++;
        this.mRotateForGuidePhoto %= 4;
    }

    public void setRotateState(int rotate) {
        this.mRotateForGuidePhoto = rotate;
    }

    public void rotateView(int degree) {
        CamLog.m3d(CameraConstants.TAG, "rotate : " + this.mRotateForGuidePhoto);
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        int curImportedCamera = -1;
        for (int i = 0; i < curCameraArray.length; i++) {
            if (curCameraArray[i] == 5) {
                curImportedCamera = i;
                break;
            }
        }
        int importedBaseIndex = curImportedCamera * 8;
        int texBaseIndex = (this.mRotateForGuidePhoto + 5) * 8;
        for (int j = 0; j < 8; j++) {
            curTexture[importedBaseIndex + j] = this.mTextureCoordCollage[j + texBaseIndex];
        }
        setCurTexCoord((float[]) curTexture.clone());
    }

    public float[] getTexCoordForReverseCollage(int cameraId) {
        boolean isFront;
        float[] targetTexture = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
        if (cameraId == 3) {
            isFront = true;
        } else {
            isFront = false;
        }
        System.arraycopy(isFront ? this.mInfo.getFrontCollageTex(0) : this.mInfo.getTextureVariation(0), isFront ? 8 : cameraId * 8, targetTexture, 0, 8);
        return (float[]) targetTexture.clone();
    }

    public void resetCameraForced() {
        int[] curCameraArray = getCurCameraIdArray();
        float[] curTexture = getCurTexCoord();
        curCameraArray[0] = 1;
        curCameraArray[1] = 0;
        setCurTexCoord((float[]) calculateTextureCoord(calculateTextureCoord(curTexture, 0, curCameraArray[0], 0), 1, curCameraArray[1], 0).clone());
    }

    public int[] getBackupCameraIdArray() {
        return this.prevCameraId;
    }
}
