package com.lge.camera.file;

import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.file.ImageRegister.ImageRegisterCallback;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class ImageSavers extends Thread implements ImageRegisterCallback {
    private ImageSaverCallback mCb = null;
    private int mCount = 0;
    private boolean mDeleteRequests = false;
    private ImageRegister mImgRegister = null;
    private ArrayList<SaveRequest> mQueue;
    private int mQueueLimit = 99;
    private boolean mStop = false;

    public interface ImageSaverCallback {
        void doAfterSaveImageSavers(Uri uri, boolean z);

        Uri insertImageContent(ImageRegisterRequest imageRegisterRequest);

        void onImageSaverQueueStatus(int i);

        void onQueueStatus(int i);

        ImageRegisterRequest saveImageDataForImageRegister(SaveRequest saveRequest);

        void setSaveRequest(SaveRequest saveRequest, byte[] bArr, int i, boolean z, int i2);
    }

    public ImageSavers(ImageSaverCallback callback, int queueCount) {
        this.mQueueLimit = queueCount;
        this.mQueue = new ArrayList();
        this.mCb = callback;
        start();
        this.mImgRegister = new ImageRegister(this);
    }

    public int getQueueCount() {
        return this.mQueue.size();
    }

    public int getCount() {
        return this.mCount;
    }

    private void addQueue(SaveRequest sr) {
        synchronized (this) {
            while (this.mQueue.size() >= this.mQueueLimit) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                }
            }
            this.mQueue.add(sr);
            notifyAll();
        }
    }

    public boolean addImage(byte[] data, int imageRotationDegree, boolean updateThumbnail, int countBurstshot) {
        if (this.mCb == null) {
            return false;
        }
        this.mCount++;
        SaveRequest sr = new SaveRequest();
        sr.queueCount = this.mCount;
        this.mCb.setSaveRequest(sr, data, imageRotationDegree, updateThumbnail, countBurstshot);
        addQueue(sr);
        CamLog.m3d(CameraConstants.TAG, "ImageSaver addImageBurst - mCount : " + this.mCount);
        return true;
    }

    public synchronized boolean deleteAllRequests() {
        boolean z = true;
        synchronized (this) {
            if (!(this.mQueue.isEmpty() || this.mStop)) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "mQueue size : " + this.mQueue.size());
                    this.mDeleteRequests = true;
                    wait();
                    CamLog.m3d(CameraConstants.TAG, "complete deleting queue");
                    z = this.mImgRegister.deleteAllRequests();
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                    z = false;
                }
            }
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:26:0x0053, code:
            if (r6.mCb == null) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:27:0x0055, code:
            if (r2 == null) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:28:0x0057, code:
            r1 = r6.mCb.saveImageDataForImageRegister(r2);
     */
    /* JADX WARNING: Missing block: B:29:0x005d, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r6.mCount--;
            r6.mQueue.remove(0);
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:32:0x006f, code:
            if (r6.mImgRegister == null) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:33:0x0071, code:
            if (r2 == null) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:34:0x0073, code:
            r6.mImgRegister.insertContent(r1);
            r2.unbind();
     */
    /* JADX WARNING: Missing block: B:36:0x007d, code:
            if (r6.mCb == null) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:37:0x007f, code:
            r6.mCb.onImageSaverQueueStatus(r6.mCount);
     */
    /* JADX WARNING: Missing block: B:38:0x0086, code:
            monitor-exit(r6);
     */
    public void run() {
        /*
        r6 = this;
        r1 = 0;
    L_0x0001:
        monitor-enter(r6);
        r3 = r6.mDeleteRequests;	 Catch:{ all -> 0x002a }
        if (r3 == 0) goto L_0x0014;
    L_0x0006:
        r3 = 0;
        r6.mCount = r3;	 Catch:{ all -> 0x002a }
        r3 = r6.mQueue;	 Catch:{ all -> 0x002a }
        r3.clear();	 Catch:{ all -> 0x002a }
        r6.notifyAll();	 Catch:{ all -> 0x002a }
        r3 = 0;
        r6.mDeleteRequests = r3;	 Catch:{ all -> 0x002a }
    L_0x0014:
        r3 = r6.mQueue;	 Catch:{ all -> 0x002a }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x002a }
        if (r3 == 0) goto L_0x0047;
    L_0x001c:
        r6.notifyAll();	 Catch:{ all -> 0x002a }
        r3 = r6.mStop;	 Catch:{ all -> 0x002a }
        if (r3 == 0) goto L_0x0025;
    L_0x0023:
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        return;
    L_0x0025:
        r6.wait();	 Catch:{ InterruptedException -> 0x002d }
    L_0x0028:
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        goto L_0x0001;
    L_0x002a:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        throw r3;
    L_0x002d:
        r0 = move-exception;
        r3 = "CameraApp";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x002a }
        r4.<init>();	 Catch:{ all -> 0x002a }
        r5 = "InterruptedException : ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x002a }
        r4 = r4.append(r0);	 Catch:{ all -> 0x002a }
        r4 = r4.toString();	 Catch:{ all -> 0x002a }
        com.lge.camera.util.CamLog.m5e(r3, r4);	 Catch:{ all -> 0x002a }
        goto L_0x0028;
    L_0x0047:
        r3 = r6.mQueue;	 Catch:{ all -> 0x002a }
        r4 = 0;
        r2 = r3.get(r4);	 Catch:{ all -> 0x002a }
        r2 = (com.lge.camera.file.SaveRequest) r2;	 Catch:{ all -> 0x002a }
        monitor-exit(r6);	 Catch:{ all -> 0x002a }
        r3 = r6.mCb;
        if (r3 == 0) goto L_0x005d;
    L_0x0055:
        if (r2 == 0) goto L_0x005d;
    L_0x0057:
        r3 = r6.mCb;
        r1 = r3.saveImageDataForImageRegister(r2);
    L_0x005d:
        monitor-enter(r6);
        r3 = r6.mCount;	 Catch:{ all -> 0x0089 }
        r3 = r3 + -1;
        r6.mCount = r3;	 Catch:{ all -> 0x0089 }
        r3 = r6.mQueue;	 Catch:{ all -> 0x0089 }
        r4 = 0;
        r3.remove(r4);	 Catch:{ all -> 0x0089 }
        r6.notifyAll();	 Catch:{ all -> 0x0089 }
        r3 = r6.mImgRegister;	 Catch:{ all -> 0x0089 }
        if (r3 == 0) goto L_0x007b;
    L_0x0071:
        if (r2 == 0) goto L_0x007b;
    L_0x0073:
        r3 = r6.mImgRegister;	 Catch:{ all -> 0x0089 }
        r3.insertContent(r1);	 Catch:{ all -> 0x0089 }
        r2.unbind();	 Catch:{ all -> 0x0089 }
    L_0x007b:
        r3 = r6.mCb;	 Catch:{ all -> 0x0089 }
        if (r3 == 0) goto L_0x0086;
    L_0x007f:
        r3 = r6.mCb;	 Catch:{ all -> 0x0089 }
        r4 = r6.mCount;	 Catch:{ all -> 0x0089 }
        r3.onImageSaverQueueStatus(r4);	 Catch:{ all -> 0x0089 }
    L_0x0086:
        monitor-exit(r6);	 Catch:{ all -> 0x0089 }
        goto L_0x0001;
    L_0x0089:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0089 }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.ImageSavers.run():void");
    }

    public void waitDone() {
        CamLog.m3d(CameraConstants.TAG, "ImageSaver waitDone start : Qsize" + this.mQueue.size());
        synchronized (this) {
            while (!this.mQueue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                }
            }
        }
    }

    public void waitAvailableQueueCount(int availableCount) {
        synchronized (this) {
            if (availableCount > this.mQueueLimit) {
                CamLog.m5e(CameraConstants.TAG, "Error! availableCount must be less than Limit!");
                return;
            }
            while (this.mQueueLimit - this.mQueue.size() < availableCount) {
                CamLog.m7i(CameraConstants.TAG, "Imagesaver available Que Count is " + (this.mQueueLimit - this.mQueue.size()) + ", Wait...");
                try {
                    wait();
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                }
            }
        }
    }

    public void finish() {
        CamLog.m3d(CameraConstants.TAG, "ImageSaver finish : Queue size = " + getQueueCount());
        waitDone();
        synchronized (this) {
            this.mStop = true;
            notifyAll();
        }
        if (this.mImgRegister != null) {
            this.mImgRegister.finish();
            this.mImgRegister = null;
        }
        try {
            join();
        } catch (InterruptedException ex) {
            CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
        }
        this.mCb = null;
        if (this.mQueue != null) {
            this.mQueue.clear();
            this.mQueue = null;
        }
    }

    public Uri insertImageContent(ImageRegisterRequest irr) {
        if (this.mCb != null) {
            return this.mCb.insertImageContent(irr);
        }
        return null;
    }

    public void onQueueStatus() {
        if (this.mCb != null) {
            int qCount = this.mCount;
            if (this.mImgRegister != null) {
                int imgRegQueCnt = this.mImgRegister.getQueueCount();
                if (imgRegQueCnt > this.mCount) {
                    qCount = imgRegQueCnt;
                }
            }
            this.mCb.onQueueStatus(qCount);
        }
    }

    public void doAfterSaveImageSavers(Uri uri, boolean updateThumbnail) {
        if (this.mCb != null) {
            this.mCb.doAfterSaveImageSavers(uri, updateThumbnail);
        }
    }
}
