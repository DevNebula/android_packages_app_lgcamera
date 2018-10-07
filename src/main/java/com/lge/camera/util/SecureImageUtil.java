package com.lge.camera.util;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.net.Uri;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ThumbnailListItem;
import java.util.ArrayList;

public class SecureImageUtil {
    public static final int SNAP_LOCKED_NONE = -1;
    public static KeyguardManager sKeyguardManager = null;
    private static boolean sSecureCamera = false;
    public static SecureImageUtil sSecureImageUtil = null;
    private Object mLock = new Object();
    private ArrayList<Uri> mSecureLockImageUriList = new ArrayList();
    private ArrayList<ThumbnailListItem> mSecureThumbnailList = new ArrayList();
    private int mSnapLockedSize = -1;
    private int mSnapLockedTime = 0;

    public static SecureImageUtil get() {
        if (sSecureImageUtil == null) {
            sSecureImageUtil = new SecureImageUtil();
        }
        return sSecureImageUtil;
    }

    public void setSnapLocked(int size, int time) {
        this.mSnapLockedSize = size;
        this.mSnapLockedTime = time;
    }

    public int getSnapLockedSize() {
        return this.mSnapLockedSize;
    }

    public int getSnapLockedTime() {
        return this.mSnapLockedTime;
    }

    public ArrayList<Uri> getSecureLockUriList() {
        return this.mSecureLockImageUriList;
    }

    public boolean isSecureLockUriListEmpty() {
        return getSecureLockUriListSize() == 0;
    }

    public int getSecureLockUriListSize() {
        ArrayList<Uri> secureLockUriList = this.mSecureLockImageUriList;
        if (secureLockUriList == null) {
            return 0;
        }
        return secureLockUriList.size();
    }

    /* JADX WARNING: Missing block: B:31:?, code:
            return;
     */
    public void checkSecureLockUriList(android.app.Activity r12) {
        /*
        r11 = this;
        r2 = -1;
        r7 = r11.mLock;
        monitor-enter(r7);
        r6 = r11.mSecureLockImageUriList;	 Catch:{ all -> 0x0062 }
        if (r6 != 0) goto L_0x0012;
    L_0x0009:
        r6 = "CameraApp";
        r8 = "mSecureLockImageUriList is null, so return";
        com.lge.camera.util.CamLog.m3d(r6, r8);	 Catch:{ all -> 0x0062 }
        monitor-exit(r7);	 Catch:{ all -> 0x0062 }
    L_0x0011:
        return;
    L_0x0012:
        if (r12 == 0) goto L_0x00a0;
    L_0x0014:
        r6 = r11.isSecureLockUriListEmpty();	 Catch:{ all -> 0x0062 }
        if (r6 != 0) goto L_0x00a0;
    L_0x001a:
        r6 = "CameraApp";
        r8 = "checkSecureLockUriList start = ";
        com.lge.camera.util.CamLog.m3d(r6, r8);	 Catch:{ all -> 0x0062 }
        r1 = new java.util.ArrayList;	 Catch:{ all -> 0x0062 }
        r1.<init>();	 Catch:{ all -> 0x0062 }
        r5 = r11.mSecureLockImageUriList;	 Catch:{ all -> 0x0062 }
        r6 = r5.iterator();	 Catch:{ all -> 0x0062 }
    L_0x002c:
        r8 = r6.hasNext();	 Catch:{ all -> 0x0062 }
        if (r8 == 0) goto L_0x0065;
    L_0x0032:
        r4 = r6.next();	 Catch:{ all -> 0x0062 }
        r4 = (android.net.Uri) r4;	 Catch:{ all -> 0x0062 }
        r8 = "CameraApp";
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0062 }
        r9.<init>();	 Catch:{ all -> 0x0062 }
        r10 = "secureLockUriList = ";
        r9 = r9.append(r10);	 Catch:{ all -> 0x0062 }
        r10 = r5.size();	 Catch:{ all -> 0x0062 }
        r9 = r9.append(r10);	 Catch:{ all -> 0x0062 }
        r9 = r9.toString();	 Catch:{ all -> 0x0062 }
        com.lge.camera.util.CamLog.m3d(r8, r9);	 Catch:{ all -> 0x0062 }
        r2 = com.lge.camera.util.FileUtil.getIdFromUri(r12, r4);	 Catch:{ all -> 0x0062 }
        r8 = -1;
        r8 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1));
        if (r8 != 0) goto L_0x002c;
    L_0x005e:
        r1.add(r4);	 Catch:{ all -> 0x0062 }
        goto L_0x002c;
    L_0x0062:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0062 }
        throw r6;
    L_0x0065:
        r6 = r1.iterator();	 Catch:{ all -> 0x0062 }
    L_0x0069:
        r8 = r6.hasNext();	 Catch:{ all -> 0x0062 }
        if (r8 == 0) goto L_0x0095;
    L_0x006f:
        r0 = r6.next();	 Catch:{ all -> 0x0062 }
        r0 = (android.net.Uri) r0;	 Catch:{ all -> 0x0062 }
        r11.removeSecureLockUri(r0);	 Catch:{ all -> 0x0062 }
        r8 = "CameraApp";
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0062 }
        r9.<init>();	 Catch:{ all -> 0x0062 }
        r10 = "deleteUri = ";
        r9 = r9.append(r10);	 Catch:{ all -> 0x0062 }
        r10 = r0.toString();	 Catch:{ all -> 0x0062 }
        r9 = r9.append(r10);	 Catch:{ all -> 0x0062 }
        r9 = r9.toString();	 Catch:{ all -> 0x0062 }
        com.lge.camera.util.CamLog.m3d(r8, r9);	 Catch:{ all -> 0x0062 }
        goto L_0x0069;
    L_0x0095:
        r1.clear();	 Catch:{ all -> 0x0062 }
        r1 = 0;
        r6 = "CameraApp";
        r8 = "checkSecureLockUriList end = ";
        com.lge.camera.util.CamLog.m3d(r6, r8);	 Catch:{ all -> 0x0062 }
    L_0x00a0:
        monitor-exit(r7);	 Catch:{ all -> 0x0062 }
        goto L_0x0011;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SecureImageUtil.checkSecureLockUriList(android.app.Activity):void");
    }

    /* JADX WARNING: Missing block: B:25:?, code:
            return;
     */
    public void updateSecureLockRecentUri(android.app.Activity r10) {
        /*
        r9 = this;
        r0 = -1;
        r5 = r9.mLock;
        monitor-enter(r5);
        r4 = r9.mSecureLockImageUriList;	 Catch:{ all -> 0x004e }
        if (r4 != 0) goto L_0x0012;
    L_0x0009:
        r4 = "CameraApp";
        r6 = "mSecureLockImageUriList is null, so return";
        com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x004e }
        monitor-exit(r5);	 Catch:{ all -> 0x004e }
    L_0x0011:
        return;
    L_0x0012:
        if (r10 == 0) goto L_0x007c;
    L_0x0014:
        r4 = r9.isSecureLockUriListEmpty();	 Catch:{ all -> 0x004e }
        if (r4 != 0) goto L_0x007c;
    L_0x001a:
        r4 = "CameraApp";
        r6 = "checkSecureLockUriList start = ";
        com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x004e }
        r3 = r9.mSecureLockImageUriList;	 Catch:{ all -> 0x004e }
    L_0x0023:
        r4 = r9.isSecureLockUriListEmpty();	 Catch:{ all -> 0x004e }
        if (r4 != 0) goto L_0x0075;
    L_0x0029:
        r4 = r9.mSecureLockImageUriList;	 Catch:{ all -> 0x004e }
        r6 = r3.size();	 Catch:{ all -> 0x004e }
        r6 = r6 + -1;
        r2 = r4.get(r6);	 Catch:{ all -> 0x004e }
        r2 = (android.net.Uri) r2;	 Catch:{ all -> 0x004e }
        r0 = com.lge.camera.util.FileUtil.getIdFromUri(r10, r2);	 Catch:{ all -> 0x004e }
        r6 = -1;
        r4 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1));
        if (r4 != 0) goto L_0x0051;
    L_0x0041:
        r4 = "CameraApp";
        r6 = "uri is not valid. find most recent uri";
        com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x004e }
        r4 = r9.mSecureLockImageUriList;	 Catch:{ all -> 0x004e }
        r4.remove(r2);	 Catch:{ all -> 0x004e }
        goto L_0x0023;
    L_0x004e:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x004e }
        throw r4;
    L_0x0051:
        r4 = "CameraApp";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x004e }
        r6.<init>();	 Catch:{ all -> 0x004e }
        r7 = "most recent uri = ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x004e }
        r7 = r9.mSecureLockImageUriList;	 Catch:{ all -> 0x004e }
        r8 = r3.size();	 Catch:{ all -> 0x004e }
        r8 = r8 + -1;
        r7 = r7.get(r8);	 Catch:{ all -> 0x004e }
        r6 = r6.append(r7);	 Catch:{ all -> 0x004e }
        r6 = r6.toString();	 Catch:{ all -> 0x004e }
        com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x004e }
    L_0x0075:
        r4 = "CameraApp";
        r6 = "checkSecureLockUriList end = ";
        com.lge.camera.util.CamLog.m3d(r4, r6);	 Catch:{ all -> 0x004e }
    L_0x007c:
        monitor-exit(r5);	 Catch:{ all -> 0x004e }
        goto L_0x0011;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SecureImageUtil.updateSecureLockRecentUri(android.app.Activity):void");
    }

    public void addSecureLockImageUri(Uri addUri) {
        synchronized (this.mLock) {
            if (this.mSecureLockImageUriList != null) {
                CamLog.m3d(CameraConstants.TAG, "addSecureLockImageUri end = ");
                this.mSecureLockImageUriList.add(addUri);
            }
        }
    }

    public void addSecureLockImageUri(Uri addUri, int position) {
        synchronized (this.mLock) {
            if (this.mSecureLockImageUriList != null) {
                CamLog.m3d(CameraConstants.TAG, "addSecureLockImageUri end = ");
                this.mSecureLockImageUriList.add(position, addUri);
            }
        }
    }

    public void removeSecureLockUri(Uri removeUri) {
        synchronized (this.mLock) {
            ArrayList<Uri> secureLockUriList = this.mSecureLockImageUriList;
            if (secureLockUriList != null) {
                int index = secureLockUriList.indexOf(removeUri);
                if (index > -1) {
                    CamLog.m3d(CameraConstants.TAG, "removeSecureLockUri end = ");
                    secureLockUriList.remove(index);
                }
            }
        }
    }

    public void removeSecureLockIndex(int index) {
        synchronized (this.mLock) {
            ArrayList<Uri> secureLockUriList = this.mSecureLockImageUriList;
            if (secureLockUriList != null && index < secureLockUriList.size() && index > -1) {
                CamLog.m3d(CameraConstants.TAG, "removeSecureLockUri end = ");
                secureLockUriList.remove(index);
            }
        }
    }

    public void setSecureThumbnailList(ArrayList<ThumbnailListItem> items) {
        this.mSecureThumbnailList = (ArrayList) items.clone();
    }

    public ArrayList<ThumbnailListItem> getSecureThumbnailList() {
        return this.mSecureThumbnailList;
    }

    public void addSecureThumbnailItem(ThumbnailListItem item) {
        synchronized (this.mLock) {
            if (this.mSecureThumbnailList != null) {
                CamLog.m3d(CameraConstants.TAG, "mSecureThumbnailList end = ");
                this.mSecureThumbnailList.add(0, item);
                if (this.mSecureThumbnailList.size() > 200) {
                    this.mSecureThumbnailList.remove(200);
                }
            }
        }
    }

    public void addSecureThumbnailItem(ThumbnailListItem item, int position) {
        synchronized (this.mLock) {
            if (this.mSecureThumbnailList != null) {
                CamLog.m3d(CameraConstants.TAG, "mSecureThumbnailList end = ");
                this.mSecureThumbnailList.add(position, item);
                if (this.mSecureThumbnailList.size() > 200) {
                    this.mSecureThumbnailList.remove(200);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:23:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            return;
     */
    public void removeSecureThumbnailItem(long r8) {
        /*
        r7 = this;
        r3 = r7.mLock;
        monitor-enter(r3);
        r2 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0037 }
        if (r2 == 0) goto L_0x0015;
    L_0x0007:
        r2 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0037 }
        r2 = r2.size();	 Catch:{ all -> 0x0037 }
        if (r2 == 0) goto L_0x0015;
    L_0x000f:
        r4 = 0;
        r2 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r2 != 0) goto L_0x0017;
    L_0x0015:
        monitor-exit(r3);	 Catch:{ all -> 0x0037 }
    L_0x0016:
        return;
    L_0x0017:
        r0 = 0;
        r2 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0037 }
        r2 = r2.iterator();	 Catch:{ all -> 0x0037 }
    L_0x001e:
        r4 = r2.hasNext();	 Catch:{ all -> 0x0037 }
        if (r4 == 0) goto L_0x0035;
    L_0x0024:
        r1 = r2.next();	 Catch:{ all -> 0x0037 }
        r1 = (com.lge.camera.managers.ThumbnailListItem) r1;	 Catch:{ all -> 0x0037 }
        r4 = r1.f33id;	 Catch:{ all -> 0x0037 }
        r4 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r4 != 0) goto L_0x003a;
    L_0x0030:
        r2 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0037 }
        r2.remove(r0);	 Catch:{ all -> 0x0037 }
    L_0x0035:
        monitor-exit(r3);	 Catch:{ all -> 0x0037 }
        goto L_0x0016;
    L_0x0037:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0037 }
        throw r2;
    L_0x003a:
        r0 = r0 + 1;
        goto L_0x001e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SecureImageUtil.removeSecureThumbnailItem(long):void");
    }

    /* JADX WARNING: Missing block: B:13:?, code:
            return;
     */
    public void removeSecureThumbnailItem(int r3) {
        /*
        r2 = this;
        r1 = r2.mLock;
        monitor-enter(r1);
        r0 = r2.mSecureThumbnailList;	 Catch:{ all -> 0x0018 }
        if (r0 == 0) goto L_0x000f;
    L_0x0007:
        r0 = r2.mSecureThumbnailList;	 Catch:{ all -> 0x0018 }
        r0 = r0.size();	 Catch:{ all -> 0x0018 }
        if (r0 != 0) goto L_0x0011;
    L_0x000f:
        monitor-exit(r1);	 Catch:{ all -> 0x0018 }
    L_0x0010:
        return;
    L_0x0011:
        r0 = r2.mSecureThumbnailList;	 Catch:{ all -> 0x0018 }
        r0.remove(r3);	 Catch:{ all -> 0x0018 }
        monitor-exit(r1);	 Catch:{ all -> 0x0018 }
        goto L_0x0010;
    L_0x0018:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0018 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SecureImageUtil.removeSecureThumbnailItem(int):void");
    }

    /* JADX WARNING: Missing block: B:24:?, code:
            return null;
     */
    public com.lge.camera.managers.ThumbnailListItem getSecureThumbnailItem(long r8) {
        /*
        r7 = this;
        r1 = 0;
        r2 = r7.mLock;
        monitor-enter(r2);
        r3 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0033 }
        if (r3 == 0) goto L_0x0016;
    L_0x0008:
        r3 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0033 }
        r3 = r3.size();	 Catch:{ all -> 0x0033 }
        if (r3 == 0) goto L_0x0016;
    L_0x0010:
        r4 = 0;
        r3 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r3 != 0) goto L_0x0019;
    L_0x0016:
        monitor-exit(r2);	 Catch:{ all -> 0x0033 }
        r0 = r1;
    L_0x0018:
        return r0;
    L_0x0019:
        r3 = r7.mSecureThumbnailList;	 Catch:{ all -> 0x0033 }
        r3 = r3.iterator();	 Catch:{ all -> 0x0033 }
    L_0x001f:
        r4 = r3.hasNext();	 Catch:{ all -> 0x0033 }
        if (r4 == 0) goto L_0x0036;
    L_0x0025:
        r0 = r3.next();	 Catch:{ all -> 0x0033 }
        r0 = (com.lge.camera.managers.ThumbnailListItem) r0;	 Catch:{ all -> 0x0033 }
        r4 = r0.f33id;	 Catch:{ all -> 0x0033 }
        r4 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1));
        if (r4 != 0) goto L_0x001f;
    L_0x0031:
        monitor-exit(r2);	 Catch:{ all -> 0x0033 }
        goto L_0x0018;
    L_0x0033:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0033 }
        throw r1;
    L_0x0036:
        monitor-exit(r2);	 Catch:{ all -> 0x0033 }
        r0 = r1;
        goto L_0x0018;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.util.SecureImageUtil.getSecureThumbnailItem(long):com.lge.camera.managers.ThumbnailListItem");
    }

    public void release() {
        synchronized (this.mLock) {
            CamLog.m3d(CameraConstants.TAG, "SecureImageUtil release.");
            if (this.mSecureLockImageUriList != null) {
                this.mSecureLockImageUriList.clear();
                this.mSecureLockImageUriList = null;
            }
            if (this.mSecureThumbnailList != null) {
                this.mSecureThumbnailList.clear();
                this.mSecureThumbnailList = null;
            }
            this.mSnapLockedSize = -1;
            this.mSnapLockedTime = 0;
            sKeyguardManager = null;
            sSecureImageUtil = null;
        }
    }

    public static boolean isScreenLocked() {
        return sSecureCamera ? false : getScreenLock();
    }

    public static boolean getScreenLock() {
        if (sKeyguardManager != null) {
            return sKeyguardManager.isKeyguardLocked();
        }
        CamLog.m3d(CameraConstants.TAG, "keyguard manager service is null");
        return false;
    }

    public static boolean useSecureLockImage() {
        boolean z = true;
        if (sKeyguardManager == null) {
            CamLog.m3d(CameraConstants.TAG, "keyguard manager service is null");
            return false;
        }
        boolean checkSecureLock;
        if (sKeyguardManager.isKeyguardLocked() && sKeyguardManager.isKeyguardSecure()) {
            checkSecureLock = true;
        } else {
            checkSecureLock = false;
        }
        if (!(isSecureCamera() && checkSecureLock)) {
            z = false;
        }
        return z;
    }

    public static boolean isSecureCamera() {
        return sSecureCamera;
    }

    public static void setSecureCamera(Activity activity) {
        boolean isSecure;
        if (!activity.isFinishing() && isSecureCameraIntent(activity.getIntent())) {
            isSecure = true;
        } else if (AppControlUtil.checkGalleryEnabledOnGuestMode(activity.getContentResolver())) {
            isSecure = false;
        } else {
            isSecure = false;
            get().release();
        }
        sKeyguardManager = (KeyguardManager) activity.getSystemService("keyguard");
        CamLog.m3d(CameraConstants.TAG, "setSecureCamera = " + isSecure);
        sSecureCamera = isSecure;
    }

    public static boolean isSecureCameraIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        if (intent.getAction() != null) {
            if (CameraConstants.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(intent.getAction())) {
                return true;
            }
            if (CameraConstants.ACTION_IMAGE_CAPTURE_SECURE.equals(intent.getAction())) {
                return true;
            }
            if (intent.getBooleanExtra(CameraConstants.SECURE_CAMERA, false)) {
                return true;
            }
            if (CameraConstants.INTENT_ACTION_CAMERA_START_FROM_COVER.equals(intent.getAction())) {
                return true;
            }
            if (CameraConstants.INTENT_ACTION_VIDEO_CAMERA_SECURE.equals(intent.getAction())) {
                return true;
            }
            return intent.getBooleanExtra(CameraConstants.SECURE_CAMERA_EXTRA, false);
        } else if (intent.getBooleanExtra(CameraConstants.SECURE_CAMERA, false)) {
            return true;
        } else {
            return false;
        }
    }
}
