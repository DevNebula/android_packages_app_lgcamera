package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.arcsoft.stickerlibrary.utils.Constant;
import com.lge.camera.app.ProjectionMatrix;
import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.components.MultiViewLayoutBase;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class MultiViewFrame extends MultiViewFrameBase {
    private static final int NUM_TEXTURE = 4;
    private static int[] sCapturedTexture = new int[4];
    private static boolean sGuidePhotoLoaded = false;
    private static boolean sIsSaveReady = false;
    private static int sNumTextureCollage = 0;
    public static int sOriX = 0;
    public static int sOriY = 0;
    private static int sPostviewType = -1;
    public static Bitmap[] sPreviewBitmap = new Bitmap[4];
    public static boolean sPreviewCap = false;
    public static Bitmap[] sPreviewCapture = new Bitmap[1];
    public static int sSize = 0;
    public static boolean sSwap = false;
    private static boolean sTransformedImage = false;
    private static int[] sTypeCollage;
    protected final int ANIMATION_MULTI_CAM_CHANGE_DURATION;
    private final int TEXTURE_INIT;
    private final int TRIPLE_VIEW_VERTEX_SIZE;
    protected String mCaptureFilePath;
    protected boolean mFreezePreview;
    protected int mFreezePreviewIndex;
    private int[] mInputFileType;
    protected boolean mIsSingleViewScreenRecording;
    protected ArrayList<MultiViewLayout> mMultiViewLayoutList;
    private ArrayList<Integer> mRecordingDegrees;

    interface CollagePictureTakenListener {
        void onPictureTaken(byte[] bArr);
    }

    public MultiViewFrame(IMultiViewModule multiviewModule) {
        this.ANIMATION_MULTI_CAM_CHANGE_DURATION = 10;
        this.mMultiViewLayoutList = null;
        this.TRIPLE_VIEW_VERTEX_SIZE = 24;
        this.mFreezePreview = false;
        this.mCaptureFilePath = null;
        this.mFreezePreviewIndex = -1;
        this.TEXTURE_INIT = -1;
        this.mCurLayout = 2;
        this.mGet = multiviewModule;
    }

    public MultiViewFrame(int type, IMultiViewModule multiviewModule) {
        super(type);
        this.ANIMATION_MULTI_CAM_CHANGE_DURATION = 10;
        this.mMultiViewLayoutList = null;
        this.TRIPLE_VIEW_VERTEX_SIZE = 24;
        this.mFreezePreview = false;
        this.mCaptureFilePath = null;
        this.mFreezePreviewIndex = -1;
        this.TEXTURE_INIT = -1;
        this.mCurLayout = 2;
        this.mGet = multiviewModule;
    }

    public MultiViewFrame(int type, int layoutIndex, IMultiViewModule multiviewModule) {
        super(type);
        this.ANIMATION_MULTI_CAM_CHANGE_DURATION = 10;
        this.mMultiViewLayoutList = null;
        this.TRIPLE_VIEW_VERTEX_SIZE = 24;
        this.mFreezePreview = false;
        this.mCaptureFilePath = null;
        this.mFreezePreviewIndex = -1;
        this.TEXTURE_INIT = -1;
        this.mCurLayout = layoutIndex;
        this.mGet = multiviewModule;
    }

    public void init(int layoutType, int[] inputFileType, ArrayList<Integer> recordingDegrees, int degree, boolean singleViewRecording) {
        CamLog.m3d(CameraConstants.TAG, "-single- init singleViewRecording = " + singleViewRecording);
        this.mCurLayout = layoutType;
        this.mInputFileType = inputFileType;
        this.mRecordingDegrees = recordingDegrees;
        this.mIsSingleViewScreenRecording = singleViewRecording;
        init(degree);
    }

    public void init(int layoutType, int degree, boolean singleViewRecording) {
        CamLog.m3d(CameraConstants.TAG, "-single- init singleViewRecording = " + singleViewRecording);
        this.mIsSingleViewScreenRecording = singleViewRecording;
        this.mCurLayout = layoutType;
        init(degree);
    }

    protected void initVertices(int degree) {
        if (this.mGet != null) {
            this.mMultiViewLayoutList = this.mGet.getMultiviewArrayList();
            if (this.mMultiViewLayoutList == null || this.mMultiViewLayoutList.size() == 0) {
                this.mMultiViewLayoutList = this.mGet.makeMultiviewLayout();
            }
            CamLog.m3d(CameraConstants.TAG, "-multiview- initVertices mCurPreviewMode = " + this.mCurLayout + ", layout list size : " + this.mMultiViewLayoutList.size());
            if (this.mMultiViewLayoutList != null && this.mMultiViewLayoutList.size() <= this.mCurLayout) {
                this.mMultiViewLayoutList = this.mGet.makeMultiviewLayout();
            }
            if (this.mIsSingleViewScreenRecording) {
                initVerticeForSingleViewRecording();
                return;
            }
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).restoreCurTexture(degree);
            CamLog.m3d(CameraConstants.TAG, "getLayoutName = " + ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getLayoutName());
            this.mVerticesCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurVertex().clone();
            if ((this.mFrameType & 6) != 0) {
                processCollageInit();
            } else {
                processCaptureInit();
            }
            this.mVerticesPrev = (float[]) this.mVerticesCoord.clone();
            this.mVerticesTransition = (float[]) this.mVerticesCoord.clone();
            this.mTexturePrev = (float[]) this.mTextureCoord.clone();
            this.mTextureTransition = (float[]) this.mTextureCoord.clone();
        }
    }

    private void processCollageInit() {
        if (!this.mGet.isMultiviewFrameShot()) {
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
        } else if (this.mInputFileType != null) {
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(this.mInputFileType, this.mRecordingDegrees).clone();
            if (this.mGet.getReverseState()) {
                System.arraycopy(((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordForReverseCollage(this.mGet.getRecordedCameraIdForReverse()), 0, this.mTextureCoord, 0, 8);
            }
        } else {
            CamLog.m5e(CameraConstants.TAG, "-rec- input file type should be specified");
        }
    }

    private void processCaptureInit() {
        this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
        if (sCapturedIndex <= -1) {
            return;
        }
        int i;
        if (this.mGet.isImportedImage()) {
            getImportedBitmap();
            for (i = 0; i < 2; i++) {
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollageForImportImage(i).clone();
            }
            return;
        }
        for (i = 0; i <= sCapturedIndex; i++) {
            sCapturedTexture = makeCapturedTexture(sPreviewBitmap[i], i, -1);
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(i).clone();
        }
    }

    public void restoreVertex(int degree) {
        if (this.mMultiViewLayoutList == null || this.mMultiViewLayoutList.size() <= this.mCurLayout) {
            CamLog.m5e(CameraConstants.TAG, "mMultiViewLayoutList : " + this.mMultiViewLayoutList + ", mCurLayout : " + this.mCurLayout);
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "-c1- restoreVertex");
        ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).restoreCurTexture(degree);
        this.mVerticesCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurVertex().clone();
        this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
        if (sCapturedIndex > -1) {
            int i;
            if (this.mGet.getReverseState()) {
                for (i = 0; i < 2; i++) {
                    this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollageForImportImage(i).clone();
                }
            } else {
                for (i = 0; i <= sCapturedIndex; i++) {
                    this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(i).clone();
                }
            }
        }
        this.mVerticesPrev = (float[]) this.mVerticesCoord.clone();
        this.mVerticesTransition = (float[]) this.mVerticesCoord.clone();
        this.mTexturePrev = (float[]) this.mTextureCoord.clone();
        this.mTextureTransition = (float[]) this.mTextureCoord.clone();
        setVertices();
    }

    private void initVerticeForSingleViewRecording() {
        CamLog.m3d(CameraConstants.TAG, "-single- initVerticeForSingleViewRecording");
        this.mVerticesCoord = MultiViewLayoutBase.getVertexSingleViewRecording();
        this.mTextureCoord = MultiViewLayoutBase.getTextureSingleViewRecording();
        this.mVerticesPrev = (float[]) this.mVerticesCoord.clone();
        this.mVerticesTransition = (float[]) this.mVerticesCoord.clone();
        this.mTexturePrev = (float[]) this.mTextureCoord.clone();
        this.mTextureTransition = (float[]) this.mTextureCoord.clone();
    }

    public void changeLayout() {
        this.mCurLayout++;
        this.mCurLayout %= 3;
        changeLayout(this.mCurLayout, true);
    }

    public void changeLayout(int toViewType, boolean animationOn) {
        CamLog.m3d(CameraConstants.TAG, "MultiViewFrame - changeLayout");
        setLayoutIndex(toViewType);
        if (this.mVerticesPrev != null) {
            this.mVerticesTransition = (float[]) this.mVerticesPrev.clone();
        } else {
            initVertices(this.mGet.getOrientationDegree());
        }
        switch (toViewType) {
            case 1:
            case 2:
            case 3:
            case 4:
                updateLayoutVertex(toViewType);
                break;
        }
        this.mIsShowingAnimation = animationOn;
        if (this.mDuration == 0) {
            this.mDuration = 10;
        }
    }

    protected void setLayoutIndex(int index) {
        this.mCurLayout = index;
        CamLog.m3d(CameraConstants.TAG, "setLayoutIndex : mCurLayout = " + this.mCurLayout);
    }

    public void updateLayoutVertex(int toViewType) {
        if (this.mMultiViewLayoutList == null || this.mMultiViewLayoutList.size() <= toViewType) {
            CamLog.m5e(CameraConstants.TAG, "mMultiViewLayoutList : " + this.mMultiViewLayoutList + ", toViewType : " + toViewType);
            return;
        }
        this.mVerticesTransition = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(toViewType)).getCurVertex().clone();
        if ((this.mFrameType & 6) != 0) {
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(this.mInputFileType, this.mRecordingDegrees).clone();
        } else {
            this.mTextureTransition = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(toViewType)).getCurTexCoord().clone();
        }
    }

    public void showAnimation() {
        CamLog.m3d(CameraConstants.TAG, "mDuration = " + this.mDuration + " mIsShowingAnimation = " + this.mIsShowingAnimation);
        if (this.mVerticesTransition.length > 24) {
            this.mVerticesCoord = enlargeVertex(this.mVerticesCoord, this.mVerticesTransition.length);
            this.mVerticesPrev = enlargeVertex(this.mVerticesPrev, this.mVerticesTransition.length);
            this.mTextureCoord = enlargeVertex(this.mTextureCoord, this.mTextureTransition.length);
            this.mTexturePrev = enlargeVertex(this.mTexturePrev, this.mTextureTransition.length);
        }
        if (!this.mIsShowingAnimation || this.mDuration <= 0) {
            this.mVerticesCoord = (float[]) this.mVerticesTransition.clone();
            this.mVerticesPrev = (float[]) this.mVerticesTransition.clone();
            this.mTextureCoord = (float[]) this.mTextureTransition.clone();
            this.mTexturePrev = (float[]) this.mTextureTransition.clone();
            this.mIsShowingAnimation = false;
            this.mDuration = 0;
        } else {
            for (int i = 0; i < this.mVerticesTransition.length; i++) {
                float[] fArr = this.mVerticesCoord;
                fArr[i] = fArr[i] + ((this.mVerticesTransition[i] - this.mVerticesPrev[i]) / ((float) 10));
                fArr = this.mTextureCoord;
                fArr[i] = fArr[i] + ((this.mTextureTransition[i] - this.mTexturePrev[i]) / ((float) 10));
            }
        }
        setVertices();
    }

    protected float[] enlargeVertex(float[] vertex, int targetLen) {
        int i;
        float[] tmpVertex = new float[targetLen];
        for (i = 0; i < vertex.length; i++) {
            tmpVertex[i] = vertex[i];
        }
        for (i = vertex.length; i < targetLen; i++) {
            tmpVertex[i] = 0.0f;
        }
        return (float[]) tmpVertex.clone();
    }

    public void drawFrame(int[] previewTexture, float[][] texMatrix) {
        if (this.mMultiViewLayoutList != null) {
            if (this.mDuration > 0) {
                this.mDuration--;
                showAnimation();
            }
            this.pVertex.position(0);
            this.pTexCoord.position(0);
            GLES20.glVertexAttribPointer(this.maPositionLoc, 2, 5126, false, 0, this.pVertex);
            GLES20.glVertexAttribPointer(this.maTexCoordsLoc, 2, 5126, false, 0, this.pTexCoord);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            MultiViewFrameBase.checkGlError("glClearColor");
            GLES20.glClear(16384);
            MultiViewFrameBase.checkGlError("glClear");
            if (texMatrix != null) {
                this.mTexMatrix = texMatrix;
            }
            drawLayout(previewTexture);
        }
    }

    protected void drawSingleViewRecording(int cameraId, int[] previewTexture) {
        setPreviewTexture(cameraId, previewTexture);
        GLES20.glDrawArrays(5, 0, 4);
        MultiViewFrameBase.checkGlError("glDrawArrays");
    }

    protected void drawLayout(int[] previewTexture) {
        if (this.mMultiViewLayoutList != null) {
            int[] cameraId = ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurCameraIdArray();
            if (sCapturedIndex == -1) {
                GLES20.glUniform1i(this.muTextureImageLoc, 3);
                MultiViewFrameBase.checkGlError("glUniform1i");
            }
            int i = 0;
            while (i < cameraId.length) {
                if (i <= sCapturedIndex && !this.mGet.getReverseState()) {
                    GLES20.glActiveTexture(33991 + i);
                    GLES20.glBindTexture(3553, sCapturedTexture[i]);
                    GLES20.glUniform1f(this.muTextureTypeLoc, 1.0f);
                    MultiViewFrameBase.checkGlError("glUniform1f");
                    GLES20.glUniform1f(this.muCameraIdLoc, 0.0f);
                    MultiViewFrameBase.checkGlError("glUniform1f");
                    GLES20.glUniform1i(this.muTextureImageLoc, i + 7);
                    MultiViewFrameBase.checkGlError("glUniform1i");
                } else if (this.mGet.getReverseState() && cameraId[i] > 4) {
                    GLES20.glActiveTexture(33991 + i);
                    GLES20.glBindTexture(3553, sCapturedTexture[0]);
                    GLES20.glUniform1f(this.muTextureTypeLoc, 1.0f);
                    MultiViewFrameBase.checkGlError("glUniform1f");
                    GLES20.glUniform1f(this.muCameraIdLoc, 0.0f);
                    MultiViewFrameBase.checkGlError("glUniform1f");
                    GLES20.glUniform1i(this.muTextureImageLoc, i + 7);
                    MultiViewFrameBase.checkGlError("glUniform1i");
                } else if (this.mIsSingleViewScreenRecording) {
                    int recordingView = sCapturedIndex + 1;
                    if (recordingView < cameraId.length) {
                        setPreviewTexture(cameraId[recordingView], previewTexture);
                        GLES20.glDrawArrays(5, 0, 4);
                        MultiViewFrameBase.checkGlError("glDrawArrays");
                        return;
                    }
                } else {
                    setPreviewTexture(cameraId[i], previewTexture);
                }
                GLES20.glDrawArrays(5, i * 4, 4);
                MultiViewFrameBase.checkGlError("glDrawArrays");
                i++;
            }
            processScreenCapture();
        }
    }

    private void processScreenCapture() {
        boolean z = false;
        if (sIsSaveReady) {
            this.mGet.savePostViewContens(sPostviewType);
            sPostviewType = -1;
            sIsSaveReady = false;
        }
        if (this.mFreezePreview) {
            Rect rect = ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getViewRect(this.mGet.getReverseState() ? 0 : this.mFreezePreviewIndex);
            CamLog.m3d(CameraConstants.TAG, "-cp- mFreezePreviewIndex = " + this.mFreezePreviewIndex);
            CamLog.m3d(CameraConstants.TAG, "-cp- rect top = " + rect.top + " left = " + rect.left + " right = " + rect.right + " bottom = " + rect.bottom);
            if (sPreviewBitmap == null || this.mFreezePreviewIndex >= 4) {
                CamLog.m3d(CameraConstants.TAG, "-cp- sPreviewBitmap is null");
            } else {
                sPreviewBitmap[this.mFreezePreviewIndex] = captureTexture(rect.left, MultiViewLayout.sLcdHeight - rect.bottom, rect.width(), rect.height());
                if (sPreviewBitmap[this.mFreezePreviewIndex] == null) {
                    CamLog.m3d(CameraConstants.TAG, "-cp- sPreviewBitmap[ " + this.mFreezePreviewIndex + " ] is null");
                    return;
                } else {
                    sCapturedTexture = makeCapturedTexture(sPreviewBitmap[this.mFreezePreviewIndex], this.mFreezePreviewIndex, -1);
                    sCapturedIndex = this.mFreezePreviewIndex;
                }
            }
            if (this.mGet.getReverseState()) {
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
            } else {
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(sCapturedIndex).clone();
            }
            setVertices();
            this.mGet.afterPreviewCaptured();
            this.mFreezePreview = false;
            if (this.mFreezePreviewIndex == 0) {
                IMultiViewModule iMultiViewModule = this.mGet;
                if (this.mCaptureFilePath != null) {
                    z = true;
                }
                iMultiViewModule.setBitmapToPrePostView(z);
            }
        }
        processGuidePhoto();
    }

    private void processGuidePhoto() {
        if (sGuidePhotoLoaded && this.mGet.isImportedImage()) {
            getImportedBitmap();
            setVertices();
            this.mGet.afterPreviewCaptured();
            sGuidePhotoLoaded = false;
        }
        if (!this.mGet.isImportedImage()) {
            sGuidePhotoLoaded = false;
        }
        if (sTransformedImage) {
            this.mGet.setReverseState(false);
            loadTransformedImage();
            setVertices();
            sTransformedImage = false;
            sIsSaveReady = true;
        }
        if (sPreviewCap) {
            sPreviewCapture[0] = captureTexture(sOriX, sOriY, sSize, sSize);
            if (sSwap) {
                this.mGet.setSwapBitmapReady();
                sSwap = false;
            }
            sPreviewCap = false;
        }
    }

    private void loadTransformedImage() {
        Bitmap[] bm = this.mGet.getTransformedImage();
        if (sPostviewType == 0) {
            int count = bm.length;
            for (int i = 0; i < count; i++) {
                if (bm[i] != null) {
                    sPreviewBitmap[i] = bm[i];
                    sCapturedTexture = makeCapturedTexture(sPreviewBitmap[i], i, -1);
                    sCapturedIndex = i;
                    this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(sCapturedIndex).clone();
                }
            }
        } else if (sPostviewType == 1) {
            if (bm[0] != null) {
                sPreviewBitmap[0] = bm[0];
                sCapturedTexture = makeCapturedTexture(sPreviewBitmap[0], 0, -1);
                sCapturedIndex = 0;
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(sCapturedIndex).clone();
            }
        } else if (bm[0] != null) {
            sPreviewBitmap[1] = bm[0];
            sCapturedTexture = makeCapturedTexture(sPreviewBitmap[1], 1, -1);
            sCapturedIndex = 1;
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(sCapturedIndex).clone();
        }
    }

    private void getImportedBitmap() {
        Bitmap bm = this.mGet.getBitmapForImport();
        if (bm == null || !bm.isRecycled()) {
            sPreviewBitmap[0] = bm;
            sCapturedTexture = makeCapturedTexture(sPreviewBitmap[0], 0, sCapturedIndex == 0 ? sCapturedTexture[0] : -1);
            sCapturedIndex = 0;
            if (this.mGet.getReverseState()) {
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
            } else {
                this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTexCoordCollage(sCapturedIndex).clone();
            }
            this.mGet.setBitmapToPrePostView(true);
            return;
        }
        this.mGet.resetStatus();
    }

    public boolean isCapturingPreview() {
        return this.mFreezePreview;
    }

    private void setPreviewTexture(int cameraId, int[] previewTexture) {
        GLES20.glUniform1f(this.muTextureTypeLoc, 0.0f);
        MultiViewFrameBase.checkGlError("glUniform1f");
        if (cameraId == 1 || cameraId == 3) {
            GLES20.glUniform1i(this.muTextureCameraLoc, 0);
            MultiViewFrameBase.checkGlError("glUniform1i");
            GLES20.glUniform1f(this.muCameraIdLoc, 1.0f);
            MultiViewFrameBase.checkGlError("glUniform1f");
            GLES20.glActiveTexture(33985);
            GLES20.glBindTexture(36197, previewTexture[1]);
            GLES20.glUniform1i(this.muTextureCameraLoc, 1);
            MultiViewFrameBase.checkGlError("glUniform1i");
        } else if (cameraId == 0 || cameraId == 2) {
            GLES20.glUniform1i(this.muTextureCameraLoc, 1);
            MultiViewFrameBase.checkGlError("glUniform1i");
            GLES20.glUniform1f(this.muCameraIdLoc, 0.0f);
            MultiViewFrameBase.checkGlError("glUniform1f");
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(36197, previewTexture[0]);
            GLES20.glUniform1i(this.muTextureCameraLoc, 0);
            MultiViewFrameBase.checkGlError("glUniform1i");
        } else if (cameraId == 4) {
            GLES20.glUniform1i(this.muTextureCameraLoc, 0);
            MultiViewFrameBase.checkGlError("glUniform1i");
            GLES20.glUniform1f(this.muCameraIdLoc, 3.0f);
            MultiViewFrameBase.checkGlError("glUniform1f");
            GLES20.glActiveTexture(33986);
            GLES20.glBindTexture(36197, previewTexture[2]);
            GLES20.glUniform1i(this.muTextureCameraLoc, 2);
            MultiViewFrameBase.checkGlError("glUniform1i");
        }
        if (!(this.mTexMatrix == null || this.mTexMatrix[1] == null)) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixFrontLoc, 1, false, this.mTexMatrix[1], 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        }
        if (!(this.mTexMatrix == null || this.mTexMatrix[0] == null)) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixRearLoc, 1, false, this.mTexMatrix[0], 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        }
        if (this.mTmpMatrixImg != null) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixImgLoc, 1, false, this.mTmpMatrixImg, 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        }
    }

    public static void resetCapturedTexture() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- resetCapturedTexture");
        if (sPreviewBitmap != null) {
            int i = 0;
            while (i < sPreviewBitmap.length) {
                if (!(sPreviewBitmap[i] == null || sPreviewBitmap[i].isRecycled())) {
                    sPreviewBitmap[i].recycle();
                    sPreviewBitmap[i] = null;
                }
                i++;
            }
            sPreviewBitmap = null;
            sPreviewBitmap = new Bitmap[4];
        }
        if (sPreviewCapture != null) {
            if (!(sPreviewCapture[0] == null || sPreviewCapture[0].isRecycled())) {
                sPreviewCapture[0].recycle();
                sPreviewCapture[0] = null;
            }
            sPreviewCapture = null;
            sPreviewCapture = new Bitmap[1];
        }
        deleteCapturedTexture();
        sCapturedTexture = new int[4];
        sCapturedIndex = -1;
    }

    protected void freezePreview(int viewIndex, String filePath) {
        CamLog.m3d(CameraConstants.TAG, "-cp- freezePreview viewIndex = " + viewIndex + " filepath = " + filePath);
        this.mFreezePreview = true;
        this.mFreezePreviewIndex = viewIndex;
        this.mCaptureFilePath = filePath;
    }

    public static void setGuidePhotoLoad() {
        sGuidePhotoLoaded = true;
    }

    public static void setTransformedImage(int type) {
        sTransformedImage = true;
        sPostviewType = type;
    }

    public static void getPreviewBitmap(int x, int y, int size, boolean swap) {
        sPreviewCap = true;
        sSwap = swap;
        sOriX = x;
        sOriY = y;
        sSize = size;
    }

    public Bitmap captureTexture(int x, int y, int width, int height) {
        CamLog.m3d(CameraConstants.TAG, "-cp- saveTexture start x = " + x + " y = " + y + " w = " + width + " h = " + height);
        int screenshotSize = width * height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(x, y, width, height, 6408, 5121, bb);
        int[] pixelsBuffer = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0, 0, width, height);
        short[] sBuffer = new short[screenshotSize];
        Buffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);
        for (int i = 0; i < screenshotSize; i++) {
            short v = sBuffer[i];
            sBuffer[i] = (short) ((((v & 31) << 11) | (v & 2016)) | ((Constant.PHOTO_EFFECT_ID_GLITCH01 & v) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        CamLog.m3d(CameraConstants.TAG, "-cp- saveTexture end");
        return bitmap;
    }

    public int[] makeCapturedTexture(Bitmap bitmap, int index, int textureId) {
        int[] texture = new int[1];
        if (textureId != -1) {
            texture[0] = textureId;
        } else if (!sIsGenTex) {
            CamLog.m3d(CameraConstants.TAG, "-gl - glGenTexture");
            GLES20.glGenTextures(1, texture, 0);
            sIsGenTex = true;
        }
        GLES20.glActiveTexture(33991 + index);
        if (bitmap != null) {
            CamLog.m3d(CameraConstants.TAG, "-multiview- bitmap size width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
            GLES20.glBindTexture(3553, texture[0]);
            GLES20.glTexParameterf(3553, 10241, 9728.0f);
            GLES20.glTexParameterf(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
            GLUtils.texImage2D(3553, 0, bitmap, 0);
        } else {
            CamLog.m5e(CameraConstants.TAG, "-multiview- bitmap is null; please check the existence of the bitmap : " + bitmap);
        }
        MultiViewFrameBase.checkGlError("glBindTexture: " + index);
        sCapturedTexture[index] = texture[0];
        CamLog.m3d(CameraConstants.TAG, "-multiview- sCapturedTexture[" + index + "] = " + sCapturedTexture[index]);
        return sCapturedTexture;
    }

    protected void changeCamera(int viewIndex, int degree) {
        if (this.mMultiViewLayoutList != null && (this.mFrameType & 6) == 0) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).changeCamera(viewIndex, degree);
        }
    }

    public static int[] initTextureCollage(int numTexture, int[] type, ArrayList<String> fileNameArray) {
        int i;
        sNumTextureCollage = numTexture;
        sTypeCollage = type;
        int[] texture = new int[numTexture];
        GLES20.glGenTextures(numTexture, texture, 0);
        StringBuilder builder = new StringBuilder();
        for (i = 0; i < texture.length; i++) {
            builder.append("texture[" + i + "] = " + texture[i] + ", ");
        }
        CamLog.m3d(CameraConstants.TAG, "texture collage = " + builder.toString());
        for (i = 0; i < numTexture; i++) {
            GLES20.glActiveTexture(33987 + i);
            if (type[i] == 2) {
                GLES20.glBindTexture(36197, texture[i]);
                GLES20.glTexParameteri(36197, 10242, 33071);
                GLES20.glTexParameteri(36197, 10243, 33071);
                GLES20.glTexParameteri(36197, 10241, 9729);
                GLES20.glTexParameteri(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
            } else {
                String fileName = (String) fileNameArray.get(i);
                CamLog.m3d(CameraConstants.TAG, "filename = " + fileName);
                new Options().inSampleSize = 2;
                Bitmap bitmap = sPreviewBitmap[i];
                if (bitmap != null) {
                    CamLog.m3d(CameraConstants.TAG, "bitmap size width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
                    GLES20.glBindTexture(3553, texture[i]);
                    GLES20.glTexParameterf(3553, 10241, 9729.0f);
                    GLES20.glTexParameterf(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
                    GLUtils.texImage2D(3553, 0, bitmap, 0);
                } else {
                    CamLog.m5e(CameraConstants.TAG, "bitmap is null; please check the existence of the file : " + fileName);
                }
            }
            MultiViewFrameBase.checkGlError("glBindTexture: " + i);
        }
        GLES20.glBindTexture(36197, 0);
        return texture;
    }

    public void drawFrameCollage(int[] texture) {
        drawFrameCollage(texture, null);
    }

    private void initCollageTexture(int[] texture) {
        GLES20.glUniform1f(this.muCameraIdLoc, 0.0f);
        MultiViewFrameBase.checkGlError("glUniform1f");
        if (this.mColTexMatrix != null) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixRearLoc, 1, false, this.mColTexMatrix, 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
            GLES20.glUniformMatrix4fv(this.muTexMatrixFrontLoc, 1, false, this.mColTexMatrix, 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        }
        if (this.mTmpMatrixImg != null) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixImgLoc, 1, false, this.mTmpMatrixImg, 0);
            MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        }
        int i = 0;
        while (i < sNumTextureCollage) {
            GLES20.glActiveTexture(33987 + i);
            if (sTypeCollage == null || sTypeCollage[i] != 2) {
                GLES20.glBindTexture(3553, this.mGet.getReverseState() ? sCapturedTexture[0] : texture[i]);
                GLES20.glUniform1f(this.muTextureTypeLoc, 1.0f);
                MultiViewFrameBase.checkGlError("glUniform1f");
                GLES20.glUniform1i(this.muTextureImageLoc, i + 3);
                MultiViewFrameBase.checkGlError("glUniform1i");
            } else {
                GLES20.glBindTexture(36197, texture[i]);
                GLES20.glUniform1f(this.muTextureTypeLoc, 0.0f);
                MultiViewFrameBase.checkGlError("glUniform1f");
                GLES20.glUniform1i(this.muTextureCameraLoc, i + 3);
                MultiViewFrameBase.checkGlError("glUniform1i");
            }
            GLES20.glDrawArrays(5, i * 4, 4);
            MultiViewFrameBase.checkGlError("glDrawArrays");
            i++;
        }
    }

    public void drawFrameCollage(int[] texture, CollagePictureTakenListener listener) {
        if (this.mDuration > 0) {
            this.mDuration--;
            showAnimation();
        }
        GLES20.glUseProgram(this.mProgram);
        MultiViewFrameBase.checkGlError("glUseProgram");
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        MultiViewFrameBase.checkGlError("glEnableVertexAttribArray - attribPosition");
        GLES20.glEnableVertexAttribArray(this.maTexCoordsLoc);
        MultiViewFrameBase.checkGlError("glEnableVertexAttribArray - attribTexCoords");
        this.pVertex.position(0);
        this.pTexCoord.position(0);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, 5126, false, 0, this.pVertex);
        GLES20.glVertexAttribPointer(this.maTexCoordsLoc, 2, 5126, false, 0, this.pTexCoord);
        if ((this.mFrameType & 2) == 0 || listener != null) {
            ProjectionMatrix.setRotation(0.0f);
        } else {
            ProjectionMatrix.setRotation(90.0f);
        }
        this.mMVPMatrix = ProjectionMatrix.getModelViewMatrix();
        GLES20.glUniformMatrix4fv(this.muMVPmatrixLoc, 1, false, this.mMVPMatrix, 0);
        MultiViewFrameBase.checkGlError("glUniformMatrix4fv");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        MultiViewFrameBase.checkGlError("glClearColor");
        GLES20.glClear(16384);
        MultiViewFrameBase.checkGlError("glClear");
        initCollageTexture(texture);
        if (listener != null) {
            CamLog.m3d(CameraConstants.TAG, "collage picture taken");
            Bitmap bitmap = BitmapManagingUtil.getRotatedImage(captureTexture(0, 0, MultiViewLayout.sLcdWidth, MultiViewLayout.sLcdHeight), 270, false);
            ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.compress(CompressFormat.JPEG, 100, jpegStream);
                bitmap.recycle();
                CamLog.m3d(CameraConstants.TAG, "jpeg compression - DONE");
            }
            listener.onPictureTaken(jpegStream.toByteArray());
        }
    }

    protected void processGesture(float x, float y, int degree) {
        int viewIndex = 0;
        if (this.mMultiViewLayoutList != null) {
            viewIndex = ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getTouchedViewIndex(x, y);
            changeCamera(viewIndex, degree);
            changeLayout(this.mCurLayout, false);
        }
        CamLog.m3d(CameraConstants.TAG, "viewIndex = " + viewIndex);
    }

    public void swapView(int degree) {
        boolean z;
        if (this.mMultiViewLayoutList != null) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).swapView(degree);
        }
        changeLayout(this.mCurLayout, false);
        IMultiViewModule iMultiViewModule = this.mGet;
        if (this.mGet.getReverseState()) {
            z = false;
        } else {
            z = true;
        }
        iMultiViewModule.setReverseState(z);
    }

    public void setRotateStateIncremental() {
        if (this.mMultiViewLayoutList != null) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).setRotateStateIncremental();
        }
    }

    public void setRotateState(int rotate) {
        if (this.mMultiViewLayoutList != null && this.mMultiViewLayoutList.size() > 0) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).setRotateState(rotate);
        }
    }

    public void rotateView(int degree) {
        if (this.mMultiViewLayoutList != null) {
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).rotateView(degree);
        }
        changeLayout(this.mCurLayout, false);
    }

    public int getMultiIntervalMaxCount() {
        return ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getFrameShotMaxCount();
    }

    public int[] getCurCameraIdArray() {
        return ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurCameraIdArray();
    }

    public void setInterface(IMultiViewModule multiviewModule) {
        this.mGet = multiviewModule;
    }

    public static boolean initializeStaticVariables(boolean forceInit) {
        if (!forceInit && sCapturedTexture != null && sPreviewBitmap != null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "-multiview- initializeStaticVariables forceInit = " + forceInit);
        sCapturedIndex = -1;
        sCapturedTexture = new int[4];
        sPreviewBitmap = new Bitmap[4];
        sTypeCollage = null;
        return true;
    }

    public static void releaseFrame() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- releaseFrame");
        sCapturedIndex = -1;
        sCapturedTexture = null;
        if (sPreviewBitmap != null) {
            int i = 0;
            while (i < sPreviewBitmap.length) {
                if (!(sPreviewBitmap[i] == null || sPreviewBitmap[i].isRecycled())) {
                    sPreviewBitmap[i].recycle();
                    sPreviewBitmap[i] = null;
                }
                i++;
            }
            sPreviewBitmap = null;
        }
    }

    public static void deleteCapturedTexture() {
        if (sCapturedTexture != null) {
            CamLog.m3d(CameraConstants.TAG, "-null- deleteCapturedTexture");
            MultiViewFrameBase.deleteTexture(sCapturedTexture, sCapturedIndex + 1);
        }
    }

    public int getLayoutListCountMVFrame() {
        if (this.mMultiViewLayoutList != null) {
            return this.mMultiViewLayoutList.size();
        }
        return 0;
    }
}
