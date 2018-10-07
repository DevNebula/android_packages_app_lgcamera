package com.lge.camera.device.api2;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.OutfocusCaptureResult;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OutFocusImageManager extends MultiFrameBufferManager {
    private static final int IMG_TYPE_BLURED = 0;
    private static final int IMG_TYPE_ORIGINAL = 1;
    private static final int IMG_WAIT_TIMEOUT = 30000;
    private Handler mHandler = null;
    private OnOutFocusImageListener mImageListener;
    private boolean mIsEnabled;
    private boolean mIsFrontCamera = false;
    private Thread mWaitThread = null;

    public interface OnOutFocusImageListener {
        void onOutFocusImage(OutfocusCaptureResult outfocusCaptureResult);
    }

    static OutFocusImageManager getInstance(int cameraId, Parameters2 parameters, Handler handler) {
        if (parameters == null) {
            return null;
        }
        String shotMode = parameters.get(ParamConstants.KEY_APP_SHOT_MODE);
        if (shotMode == null || !shotMode.contains(ParamConstants.KEY_OUTFOCUS)) {
            return null;
        }
        OutFocusImageManager manager = new OutFocusImageManager(cameraId, handler);
        manager.configureBuffer(parameters.getPictureSize());
        return manager;
    }

    private OutFocusImageManager(int cameraId, Handler handler) {
        boolean z = false;
        this.mHandler = handler;
        if (cameraId == 1 || (cameraId == 2 && FunctionProperties.getCameraTypeFront() == 1)) {
            z = true;
        }
        this.mIsFrontCamera = z;
    }

    public void setEnable(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public boolean waitAllImage(final ResultChecker checker) {
        if (this.mWaitThread == null || !this.mWaitThread.isAlive()) {
            this.mWaitThread = new Thread(new Runnable() {
                public void run() {
                    OutFocusImageManager.this.gatherImage(2, checker, 30000);
                    OutFocusImageManager.this.setImage(OutFocusImageManager.this.getImageItems(2));
                    OutFocusImageManager.this.clearBuffer();
                }
            });
            this.mWaitThread.start();
            return true;
        }
        CamLog.m7i(CameraConstants.TAG, " Wait Thread already run");
        return false;
    }

    private void setImage(ArrayList<ImageItem> imageItems) {
        if (imageItems == null || imageItems.size() == 0) {
            sendNotify(null);
        } else if (this.mImageListener == null) {
            Iterator it = imageItems.iterator();
            while (it.hasNext()) {
                ((ImageItem) it.next()).close();
            }
        } else {
            CamLog.m7i(CameraConstants.TAG, " Get Image " + imageItems.size());
            OutfocusCaptureResult result = new OutfocusCaptureResult();
            int errorType = imageItems.size() == 2 ? 0 : 1;
            byte[][] imageArrary = new byte[2][];
            byte[] metaData = null;
            int extraSize = 0;
            Iterator it2 = imageItems.iterator();
            while (it2.hasNext()) {
                byte[] jpegData;
                ImageItem item = (ImageItem) it2.next();
                TotalCaptureResult meta = item.getMetadata();
                int index = 1;
                if (errorType == 0) {
                    try {
                        errorType = meta.get(ParamConstants.KEY_OUTFOCUS_RESULT) == null ? 0 : ((Integer) meta.get(ParamConstants.KEY_OUTFOCUS_RESULT)).intValue();
                    } catch (IllegalArgumentException e) {
                        if (CamLog.isTagExceptionLogOn()) {
                            e.printStackTrace();
                        }
                    }
                }
                boolean isOriginalImg = ((Boolean) meta.getRequest().get(ParamConstants.KEY_OUTFOCUS_ORIGINAL_IMAGE)).booleanValue();
                CamLog.m7i(CameraConstants.TAG, " result " + meta.get(ParamConstants.KEY_OUTFOCUS_RESULT) + " isOriginalImg " + isOriginalImg);
                if (!isOriginalImg) {
                    if (errorType != 0) {
                        CamLog.m7i(CameraConstants.TAG, " outfocus failed  : " + errorType);
                        item.close();
                    } else {
                        index = 0;
                        result.setSolutionType(meta.get(ParamConstants.KEY_OUTFOCUS_SOLUTION_TYPE) == null ? 0 : ((Byte) meta.get(ParamConstants.KEY_OUTFOCUS_SOLUTION_TYPE)).byteValue());
                        if (meta.get(ParamConstants.KEY_OUTFOCUS_EXTRA_SIZE) == null) {
                            extraSize = 0;
                        } else {
                            extraSize = ((Integer) meta.get(ParamConstants.KEY_OUTFOCUS_EXTRA_SIZE)).intValue();
                        }
                        CamLog.m7i(CameraConstants.TAG, " extraSize " + extraSize + " solutionType " + result.getSolutionType());
                        result.setFocusRegions((MeteringRectangle[]) meta.get(CaptureResult.CONTROL_AF_REGIONS));
                    }
                }
                ByteBuffer jpegBuffer = item.getImage().getPlanes()[0].getBuffer();
                int extraOffset = 0;
                CamLog.m7i(CameraConstants.TAG, " type " + (index == 0 ? "blured " : " original") + "jpeg size " + jpegBuffer.capacity());
                if (index == 0) {
                    if (this.mIsFrontCamera) {
                        extraOffset = 4;
                        jpegBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        extraSize = jpegBuffer.getInt(jpegBuffer.capacity() - 4);
                        CamLog.m7i(CameraConstants.TAG, " get extra data from bluredImage " + extraSize + " byte order " + jpegBuffer.order().toString());
                    }
                    if (extraSize <= 0 || extraSize > jpegBuffer.capacity()) {
                        item.close();
                        errorType = 1;
                    } else {
                        jpegData = new byte[((jpegBuffer.capacity() - extraSize) - extraOffset)];
                        jpegBuffer.get(jpegData, 0, jpegData.length);
                        if (extraSize > 0) {
                            metaData = new byte[extraSize];
                            int i = jpegData.length;
                            int i2 = 0;
                            while (i2 < extraSize) {
                                int cnt = i2 + 1;
                                metaData[i2] = jpegBuffer.get(i);
                                i++;
                                i2 = cnt;
                            }
                        }
                    }
                } else {
                    jpegData = new byte[jpegBuffer.capacity()];
                    jpegBuffer.get(jpegData);
                }
                imageArrary[index] = jpegData;
                item.close();
            }
            result.setOriginImage(imageArrary[1]);
            result.setBluredImage(imageArrary[0]);
            result.setMeta(metaData);
            result.setErrorType(errorType);
            sendNotify(result);
        }
    }

    public void captureStart(OnOutFocusImageListener listener, List<CaptureRequest> requests) {
        if (listener != null) {
            this.mImageListener = listener;
            ResultChecker checker = new ResultChecker();
            for (CaptureRequest cr : requests) {
                checker.addCheckCondition(cr.hashCode());
            }
            startBuffering();
            waitAllImage(checker);
        }
    }

    public void sendNotify(final OutfocusCaptureResult outfocusResult) {
        if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (OutFocusImageManager.this.mImageListener != null) {
                        OutFocusImageManager.this.mImageListener.onOutFocusImage(outfocusResult);
                    }
                }
            });
        }
    }

    public boolean isSecondShot(CaptureRequest request) {
        if (!isEnabled()) {
            return false;
        }
        try {
            Boolean isOriginalImg = (Boolean) request.get(ParamConstants.KEY_OUTFOCUS_ORIGINAL_IMAGE);
            if (isOriginalImg != null) {
                return isOriginalImg.booleanValue();
            }
            return false;
        } catch (IllegalArgumentException e) {
            if (!CamLog.isTagExceptionLogOn()) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }
}
