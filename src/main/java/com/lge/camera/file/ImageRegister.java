package com.lge.camera.file;

import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;

public class ImageRegister extends Thread {
    private ImageRegisterCallback mCb;
    private boolean mDeleteRequests;
    private ArrayList<ImageRegisterRequest> mRequestQueue;
    private boolean mStop;

    public interface ImageRegisterCallback {
        void doAfterSaveImageSavers(Uri uri, boolean z);

        Uri insertImageContent(ImageRegisterRequest imageRegisterRequest);

        void onQueueStatus();
    }

    public ImageRegister(ImageRegisterCallback callback) {
        this.mRequestQueue = null;
        this.mStop = false;
        this.mCb = null;
        this.mDeleteRequests = false;
        this.mRequestQueue = new ArrayList();
        this.mCb = callback;
        start();
    }

    public int getQueueCount() {
        return this.mRequestQueue.size();
    }

    public void insertContent(ImageRegisterRequest irr) {
        if (this.mCb != null) {
            CamLog.m3d(CameraConstants.TAG, "ImageRegister addQueue : " + getQueueCount());
            synchronized (this) {
                this.mRequestQueue.add(irr);
                notifyAll();
            }
        }
    }

    public synchronized boolean deleteAllRequests() {
        boolean z = true;
        synchronized (this) {
            if (!(this.mRequestQueue.isEmpty() || this.mStop)) {
                try {
                    CamLog.m3d(CameraConstants.TAG, "mRequestQueue size : " + this.mRequestQueue.size());
                    this.mDeleteRequests = true;
                    wait();
                    CamLog.m3d(CameraConstants.TAG, "complete deleting queue");
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                    z = false;
                }
            }
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:26:0x0050, code:
            if (r6.mCb == null) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:27:0x0052, code:
            if (r1 == null) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:28:0x0054, code:
            r2 = r6.mCb.insertImageContent(r1);
     */
    /* JADX WARNING: Missing block: B:29:0x005a, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r6.mRequestQueue.remove(0);
            notifyAll();
     */
    /* JADX WARNING: Missing block: B:32:0x0066, code:
            if (r6.mCb == null) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:33:0x0068, code:
            if (r1 == null) goto L_0x0079;
     */
    /* JADX WARNING: Missing block: B:34:0x006a, code:
            r6.mCb.doAfterSaveImageSavers(r2, r1.mUpdateThumbnail);
            r6.mCb.onQueueStatus();
            r1.unbind();
     */
    /* JADX WARNING: Missing block: B:35:0x0079, code:
            monitor-exit(r6);
     */
    public void run() {
        /*
        r6 = this;
        r2 = 0;
    L_0x0001:
        monitor-enter(r6);
        r3 = r6.mDeleteRequests;	 Catch:{ all -> 0x0027 }
        if (r3 == 0) goto L_0x0011;
    L_0x0006:
        r3 = r6.mRequestQueue;	 Catch:{ all -> 0x0027 }
        r3.clear();	 Catch:{ all -> 0x0027 }
        r6.notifyAll();	 Catch:{ all -> 0x0027 }
        r3 = 0;
        r6.mDeleteRequests = r3;	 Catch:{ all -> 0x0027 }
    L_0x0011:
        r3 = r6.mRequestQueue;	 Catch:{ all -> 0x0027 }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x0027 }
        if (r3 == 0) goto L_0x0044;
    L_0x0019:
        r6.notifyAll();	 Catch:{ all -> 0x0027 }
        r3 = r6.mStop;	 Catch:{ all -> 0x0027 }
        if (r3 == 0) goto L_0x0022;
    L_0x0020:
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        return;
    L_0x0022:
        r6.wait();	 Catch:{ InterruptedException -> 0x002a }
    L_0x0025:
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        goto L_0x0001;
    L_0x0027:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        throw r3;
    L_0x002a:
        r0 = move-exception;
        r3 = "CameraApp";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0027 }
        r4.<init>();	 Catch:{ all -> 0x0027 }
        r5 = "InterruptedException : ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0027 }
        r4 = r4.append(r0);	 Catch:{ all -> 0x0027 }
        r4 = r4.toString();	 Catch:{ all -> 0x0027 }
        com.lge.camera.util.CamLog.m5e(r3, r4);	 Catch:{ all -> 0x0027 }
        goto L_0x0025;
    L_0x0044:
        r3 = r6.mRequestQueue;	 Catch:{ all -> 0x0027 }
        r4 = 0;
        r1 = r3.get(r4);	 Catch:{ all -> 0x0027 }
        r1 = (com.lge.camera.file.ImageRegisterRequest) r1;	 Catch:{ all -> 0x0027 }
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        r3 = r6.mCb;
        if (r3 == 0) goto L_0x005a;
    L_0x0052:
        if (r1 == 0) goto L_0x005a;
    L_0x0054:
        r3 = r6.mCb;
        r2 = r3.insertImageContent(r1);
    L_0x005a:
        monitor-enter(r6);
        r3 = r6.mRequestQueue;	 Catch:{ all -> 0x007b }
        r4 = 0;
        r3.remove(r4);	 Catch:{ all -> 0x007b }
        r6.notifyAll();	 Catch:{ all -> 0x007b }
        r3 = r6.mCb;	 Catch:{ all -> 0x007b }
        if (r3 == 0) goto L_0x0079;
    L_0x0068:
        if (r1 == 0) goto L_0x0079;
    L_0x006a:
        r3 = r6.mCb;	 Catch:{ all -> 0x007b }
        r4 = r1.mUpdateThumbnail;	 Catch:{ all -> 0x007b }
        r3.doAfterSaveImageSavers(r2, r4);	 Catch:{ all -> 0x007b }
        r3 = r6.mCb;	 Catch:{ all -> 0x007b }
        r3.onQueueStatus();	 Catch:{ all -> 0x007b }
        r1.unbind();	 Catch:{ all -> 0x007b }
    L_0x0079:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        goto L_0x0001;
    L_0x007b:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.ImageRegister.run():void");
    }

    public void waitDone() {
        CamLog.m3d(CameraConstants.TAG, "ImageREgister waitDone start : Queue size = " + this.mRequestQueue.size());
        synchronized (this) {
            while (!this.mRequestQueue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
                }
            }
        }
    }

    public void finish() {
        CamLog.m3d(CameraConstants.TAG, "ImageRegister finish : Queue size = " + getQueueCount());
        waitDone();
        synchronized (this) {
            this.mStop = true;
            notifyAll();
        }
        try {
            join();
        } catch (InterruptedException ex) {
            CamLog.m5e(CameraConstants.TAG, "InterruptedException : " + ex);
        }
        this.mCb = null;
        if (this.mRequestQueue != null) {
            this.mRequestQueue.clear();
            this.mRequestQueue = null;
        }
    }
}
