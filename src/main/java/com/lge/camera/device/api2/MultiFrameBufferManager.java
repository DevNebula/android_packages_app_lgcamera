package com.lge.camera.device.api2;

import android.graphics.Rect;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.util.Size;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MultiFrameBufferManager {
    private static final int BUFFER_SIZE_DEFAULT = 2;
    public static final int BUFFER_STATE_CONFIGURE = 1;
    public static final int BUFFER_STATE_NONE = 0;
    public static final int BUFFER_STATE_READY = 2;
    public static final int BUFFER_STATE_STARTED = 3;
    public static final int BUFFER_STATE_STOPPED = 4;
    public static final int DROP_IMAGE = -2;
    public static final int DROP_META = -1;
    public static final int WAIT_FOCUSED_IMAGE = 1;
    public static final int WAIT_IMAGE = 2;
    public static final int WAIT_NONE = 0;
    private final int MAX_BUFFER_NUMBER = 10;
    private final int MAX_INCOMPLETE_BUFFER_NUMBER = 3;
    private ImageItem[] mBuffer = new ImageItem[20];
    private volatile int mCurrentQueueCnt = 0;
    private volatile int mFocusedAge = 0;
    private int mFrameHeight;
    private int mFrameWidth;
    private volatile boolean mIgnoreMeta = false;
    private volatile int mImageHead;
    private ImageItem[] mIncompleteBuffer = new ImageItem[3];
    private volatile int mIncompleteImageCnt = 0;
    private volatile int mIncompleteMetaCnt = 0;
    private ImageItem mLastFocusedImage = null;
    private CountDownLatch mLatch = null;
    private final Object mLock = new Object();
    private ResultChecker mMetaChecker = null;
    private volatile int mMetaHead;
    private int mNumBuffer;
    private volatile int mQueueFront = -1;
    private volatile int mQueueRear = -1;
    private int mState = 0;
    private volatile int mWaitCondition = 0;

    public void configureBuffer(Size size) {
        if (size != null) {
            setState(1);
            if (!(size.getWidth() == this.mFrameWidth && size.getHeight() == this.mFrameHeight)) {
                clearBuffer();
            }
            setBufferSize(2);
            this.mFrameWidth = size.getWidth();
            this.mFrameHeight = size.getHeight();
            CamLog.m3d(CameraConstants.TAG, "Buffer configure : numBuffer = 2, mBufferWidth = " + this.mFrameWidth + ", mBufferHeight = " + this.mFrameHeight);
            setState(2);
        }
    }

    public void clearBuffer() {
        synchronized (this.mLock) {
            int i;
            for (i = 0; i < this.mBuffer.length; i++) {
                if (this.mBuffer[i] != null) {
                    this.mBuffer[i].close();
                    this.mBuffer[i] = null;
                }
            }
            for (i = 0; i < this.mIncompleteBuffer.length; i++) {
                if (this.mIncompleteBuffer[i] != null) {
                    this.mIncompleteBuffer[i].close();
                    this.mIncompleteBuffer[i] = null;
                }
            }
            this.mQueueFront = -1;
            this.mQueueRear = -1;
            this.mCurrentQueueCnt = 0;
            this.mImageHead = 0;
            this.mMetaHead = 0;
            this.mIncompleteMetaCnt = 0;
            this.mIncompleteImageCnt = 0;
            this.mMetaChecker = null;
        }
        CamLog.m3d(CameraConstants.TAG, "clearBuffer");
    }

    public Image[] getAllImages() {
        Image[] array;
        synchronized (this.mLock) {
            int cnt = this.mCurrentQueueCnt;
            if (cnt == 0) {
                array = null;
            } else {
                array = new Image[cnt];
                for (int i = 0; i < cnt; i++) {
                    ImageItem image = poll();
                    if (image != null) {
                        array[i] = image.getImage();
                    }
                }
            }
        }
        return array;
    }

    public ArrayList<ImageItem> getAllImageItems() {
        ArrayList<ImageItem> items;
        synchronized (this.mLock) {
            int cnt = this.mCurrentQueueCnt;
            if (cnt == 0) {
                items = null;
            } else {
                items = new ArrayList(cnt);
                for (int i = 0; i < cnt; i++) {
                    ImageItem image = poll();
                    if (image != null) {
                        items.add(image);
                    }
                }
            }
        }
        return items;
    }

    public ArrayList<ImageItem> getImageItems(int count) {
        ArrayList<ImageItem> items;
        synchronized (this.mLock) {
            int cnt = count < this.mCurrentQueueCnt ? count : this.mCurrentQueueCnt;
            if (cnt == 0) {
                items = null;
            } else {
                items = new ArrayList(cnt);
                for (int i = 0; i < cnt; i++) {
                    ImageItem image = poll();
                    if (image != null) {
                        items.add(image);
                    }
                }
            }
        }
        return items;
    }

    public void setBufferSize(int bufferSize) {
        synchronized (this.mLock) {
            if (bufferSize < 2) {
                bufferSize = 2;
            } else if (bufferSize > 10) {
                bufferSize = 10;
            }
            if (this.mNumBuffer == bufferSize) {
                return;
            }
            CamLog.m3d(CameraConstants.TAG, "Image buffering size new : " + bufferSize + " Old : " + this.mNumBuffer + " mCurrentQueueCnt " + this.mCurrentQueueCnt);
            if (this.mQueueFront < 0) {
                this.mNumBuffer = bufferSize;
                return;
            }
            arrangeBuffer(bufferSize);
        }
    }

    private void arrangeBuffer(int bufferSize) {
        int i;
        if (this.mNumBuffer < bufferSize) {
            if (this.mQueueFront > this.mQueueRear) {
                int lastIndex = bufferSize - 1;
                for (i = 0; i < this.mCurrentQueueCnt; i++) {
                    this.mBuffer[lastIndex - i] = this.mBuffer[this.mQueueRear];
                    this.mBuffer[this.mQueueRear] = null;
                    this.mQueueRear = ((this.mNumBuffer + this.mQueueRear) - 1) % this.mNumBuffer;
                }
                this.mQueueRear = lastIndex;
                this.mQueueFront = bufferSize - this.mCurrentQueueCnt;
            }
            this.mNumBuffer = bufferSize;
            printQueue();
            return;
        }
        int cnt = this.mCurrentQueueCnt;
        while (cnt >= bufferSize) {
            remove();
            cnt--;
        }
        if (this.mQueueFront > this.mQueueRear) {
            int firstIndex = this.mQueueRear;
            this.mQueueRear = firstIndex + 1;
            for (i = firstIndex; i < this.mNumBuffer - firstIndex; i++) {
                this.mBuffer[i] = this.mBuffer[this.mQueueFront];
                ImageItem[] imageItemArr = this.mBuffer;
                int i2 = this.mQueueFront;
                this.mQueueFront = i2 + 1;
                imageItemArr[i2] = null;
            }
            this.mQueueFront = firstIndex;
        } else if (this.mQueueFront > 0) {
            for (i = 0; i < cnt; i++) {
                this.mBuffer[i] = this.mBuffer[this.mQueueFront + i];
                this.mBuffer[this.mQueueFront + i] = null;
            }
            this.mQueueFront = 0;
            this.mQueueRear = cnt - 1;
        }
        this.mNumBuffer = bufferSize;
    }

    private void printQueue() {
        CamLog.m7i(CameraConstants.TAG, "Complete Queue:  Max Count " + this.mNumBuffer + " mCurrentQueueCnt " + this.mCurrentQueueCnt + " mQueueFront " + this.mQueueFront + " mQueueRear " + this.mQueueRear);
        for (int i = 0; i < this.mNumBuffer; i++) {
            CamLog.m7i(CameraConstants.TAG, " " + i + (this.mBuffer[i] == null ? " empty " : " " + this.mBuffer[i].getImage().getTimestamp()));
        }
        CamLog.m7i(CameraConstants.TAG, "InComplete Queue:  Max Count " + this.mIncompleteBuffer.length + " mIncompleteImageCnt " + this.mIncompleteImageCnt + " mIncompleteMetaCnt " + this.mIncompleteMetaCnt);
    }

    private int findMeta(long timestamp, int index) {
        int startIndex = index;
        boolean isValidImage = false;
        int emptyIndex = -1;
        int oldestIndex = index;
        long oldestTimeStamp = timestamp;
        do {
            if (this.mIncompleteBuffer[index] == null) {
                emptyIndex = index;
            } else {
                ImageItem item = this.mIncompleteBuffer[index];
                if (item.getMetadata() != null) {
                    long metaTimeStamp = ((Long) this.mIncompleteBuffer[index].getMetadata().get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                    if (metaTimeStamp == timestamp) {
                        return index;
                    }
                    if (metaTimeStamp < oldestTimeStamp) {
                        oldestIndex = index;
                        oldestTimeStamp = metaTimeStamp;
                        isValidImage = true;
                    }
                } else if (item.getImage() != null) {
                    long queueImageTimeStamp = item.getImage().getTimestamp();
                    if (queueImageTimeStamp < oldestTimeStamp) {
                        oldestIndex = index;
                        oldestTimeStamp = queueImageTimeStamp;
                        isValidImage = true;
                    }
                }
            }
            index = (index + 1) % this.mIncompleteBuffer.length;
        } while (index != startIndex);
        if (emptyIndex >= 0) {
            return emptyIndex;
        }
        return isValidImage ? removeIncompelteQueue(oldestIndex) : -2;
    }

    private int findImage(long timestamp, int index) {
        int startIndex = index;
        boolean isValidMeta = false;
        int emptyIndex = -1;
        int oldestIndex = index;
        long oldestTimeStamp = timestamp;
        do {
            if (this.mIncompleteBuffer[index] == null) {
                emptyIndex = index;
            } else {
                ImageItem item = this.mIncompleteBuffer[index];
                if (item.getImage() != null) {
                    long imageTimeStamp = item.getImage().getTimestamp();
                    if (imageTimeStamp == timestamp) {
                        return index;
                    }
                    if (imageTimeStamp < oldestTimeStamp) {
                        oldestIndex = index;
                        oldestTimeStamp = imageTimeStamp;
                        isValidMeta = true;
                    }
                } else if (item.getMetadata() != null) {
                    long queueMetaTimeStamp = ((Long) item.getMetadata().get(CaptureResult.SENSOR_TIMESTAMP)).longValue();
                    if (queueMetaTimeStamp < oldestTimeStamp) {
                        oldestTimeStamp = queueMetaTimeStamp;
                        oldestIndex = index;
                        isValidMeta = true;
                    }
                }
            }
            index = (index + 1) % this.mIncompleteBuffer.length;
        } while (index != startIndex);
        if (emptyIndex >= 0) {
            return emptyIndex;
        }
        return isValidMeta ? removeIncompelteQueue(oldestIndex) : -1;
    }

    private int removeIncompelteQueue(int index) {
        if (this.mIncompleteBuffer[index] != null) {
            if (this.mIncompleteBuffer[index].getImage() != null) {
                this.mIncompleteBuffer[index].closeImage();
                this.mIncompleteImageCnt--;
                CamLog.m3d(CameraConstants.TAG, "drop old image " + index);
            }
            if (this.mIncompleteBuffer[index].getMetadata() != null) {
                this.mIncompleteBuffer[index].closeMeta();
                this.mIncompleteMetaCnt--;
                CamLog.m3d(CameraConstants.TAG, "drop old meta " + index);
            }
        }
        return index;
    }

    private boolean checkState() {
        return getState() == 3;
    }

    /* JADX WARNING: Missing block: B:17:0x0027, code:
            if (r0 != null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return false;
     */
    public boolean add(android.media.Image r6) {
        /*
        r5 = this;
        r1 = 1;
        r2 = 0;
        r3 = r5.mLock;
        monitor-enter(r3);
        r4 = r5.checkState();	 Catch:{ all -> 0x001f }
        if (r4 != 0) goto L_0x000e;
    L_0x000b:
        monitor-exit(r3);	 Catch:{ all -> 0x001f }
        r1 = r2;
    L_0x000d:
        return r1;
    L_0x000e:
        r4 = r5.mIgnoreMeta;	 Catch:{ all -> 0x001f }
        if (r4 == 0) goto L_0x0022;
    L_0x0012:
        r0 = new com.lge.camera.device.api2.ImageItem;	 Catch:{ all -> 0x001f }
        r0.<init>();	 Catch:{ all -> 0x001f }
        r0.setImage(r6);	 Catch:{ all -> 0x001f }
        r5.addQueue(r0);	 Catch:{ all -> 0x001f }
        monitor-exit(r3);	 Catch:{ all -> 0x001f }
        goto L_0x000d;
    L_0x001f:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x001f }
        throw r1;
    L_0x0022:
        r0 = r5.matchMeta(r6);	 Catch:{ all -> 0x001f }
        monitor-exit(r3);	 Catch:{ all -> 0x001f }
        if (r0 != 0) goto L_0x000d;
    L_0x0029:
        r1 = r2;
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.MultiFrameBufferManager.add(android.media.Image):boolean");
    }

    protected ImageItem matchMeta(Image image) {
        int i = findMeta(image.getTimestamp(), this.mImageHead);
        if (i == -2) {
            CamLog.m5e(CameraConstants.TAG, "Invalid Image");
            return null;
        }
        this.mImageHead = i;
        if (this.mIncompleteBuffer[this.mImageHead] == null) {
            this.mIncompleteBuffer[this.mImageHead] = new ImageItem();
        }
        this.mIncompleteBuffer[this.mImageHead].setImage(image);
        ImageItem item = this.mIncompleteBuffer[this.mImageHead];
        if (item.getMetadata() != null) {
            addQueue(item);
            this.mIncompleteBuffer[this.mImageHead] = null;
            this.mIncompleteMetaCnt--;
        } else {
            this.mIncompleteImageCnt++;
            checkImageCount();
        }
        this.mImageHead = (this.mImageHead + 1) % this.mIncompleteBuffer.length;
        return item;
    }

    public boolean add(TotalCaptureResult metadata, boolean isFocused) {
        synchronized (this.mLock) {
            if (!checkState()) {
                return false;
            } else if (!isAddableMeta(metadata)) {
                return false;
            } else if (matchImage(metadata, isFocused, ((Long) metadata.get(CaptureResult.SENSOR_TIMESTAMP)).longValue()) != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    private ImageItem matchImage(TotalCaptureResult metadata, boolean isFocused, long timestamp) {
        int i = findImage(timestamp, this.mMetaHead);
        if (i == -1) {
            CamLog.m5e(CameraConstants.TAG, "Invalid TotalCaptureResult");
            return null;
        }
        this.mMetaHead = i;
        if (this.mIncompleteBuffer[this.mMetaHead] == null) {
            this.mIncompleteBuffer[this.mMetaHead] = new ImageItem();
        }
        this.mIncompleteBuffer[this.mMetaHead].setMetadata(metadata, isFocused);
        ImageItem item = this.mIncompleteBuffer[this.mMetaHead];
        if (checkImage(item)) {
            addQueue(this.mIncompleteBuffer[this.mMetaHead]);
            this.mIncompleteBuffer[this.mMetaHead] = null;
            this.mIncompleteImageCnt--;
        } else {
            this.mIncompleteMetaCnt++;
        }
        this.mMetaHead = (this.mMetaHead + 1) % this.mIncompleteBuffer.length;
        return item;
    }

    protected boolean checkImage(ImageItem item) {
        return (item == null || item.getImage() == null) ? false : true;
    }

    public void addQueue(ImageItem item) {
        synchronized (this.mLock) {
            if (this.mQueueFront < 0) {
                this.mQueueRear = 0;
                this.mQueueFront = 0;
            } else {
                this.mQueueRear = (this.mQueueRear + 1) % this.mNumBuffer;
            }
            if (this.mBuffer[this.mQueueRear] != null) {
                this.mBuffer[this.mQueueRear].close();
                this.mQueueFront = (this.mQueueFront + 1) % this.mNumBuffer;
            } else {
                this.mCurrentQueueCnt++;
            }
            this.mBuffer[this.mQueueRear] = item;
            if (item.getFocusState()) {
                this.mFocusedAge = 1;
                this.mLastFocusedImage = item;
            } else if (this.mFocusedAge > 0) {
                this.mFocusedAge++;
            }
            notifyImageItem(item);
        }
    }

    private void notifyImageItem(ImageItem item) {
        if (this.mLatch != null) {
            if (this.mMetaChecker != null && this.mMetaChecker.hasIgnoreMetaAfterValidImage()) {
                CamLog.m7i(CameraConstants.TAG, "mIgnoreMeta true");
                this.mIgnoreMeta = true;
            }
            if (this.mWaitCondition == 1) {
                if (item.getFocusState()) {
                    CamLog.m7i(CameraConstants.TAG, "Get Focused Image");
                    this.mLatch.countDown();
                }
            } else if (this.mWaitCondition == 2) {
                CamLog.m7i(CameraConstants.TAG, "Get Image");
                this.mLatch.countDown();
            }
            if (this.mLatch.getCount() == 0) {
                stopBuffering();
            }
        }
    }

    private void remove() {
        ImageItem image = poll();
        if (image != null) {
            image.close();
        }
    }

    public ImageItem poll() {
        ImageItem image = null;
        synchronized (this.mLock) {
            if (this.mCurrentQueueCnt <= 0) {
            } else {
                image = this.mBuffer[this.mQueueFront];
                if (this.mLastFocusedImage == image) {
                    this.mFocusedAge = 0;
                    this.mLastFocusedImage = null;
                }
                this.mBuffer[this.mQueueFront] = null;
                if (this.mQueueFront == this.mQueueRear) {
                    this.mQueueFront = -1;
                    this.mQueueRear = -1;
                    this.mCurrentQueueCnt = 0;
                } else {
                    this.mQueueFront = (this.mQueueFront + 1) % this.mNumBuffer;
                    this.mCurrentQueueCnt--;
                }
            }
        }
        return image;
    }

    public ImageItem peak() {
        ImageItem imageItem;
        synchronized (this.mLock) {
            if (this.mQueueFront < 0) {
                imageItem = null;
            } else {
                stopBuffering();
                imageItem = this.mBuffer[this.mQueueFront];
            }
        }
        return imageItem;
    }

    /* JADX WARNING: Missing block: B:32:0x00ac, code:
            if (waitImage(500) != false) goto L_0x00b1;
     */
    /* JADX WARNING: Missing block: B:34:0x00b1, code:
            r2 = r6.mLock;
     */
    /* JADX WARNING: Missing block: B:35:0x00b3, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            com.lge.camera.util.CamLog.m7i(com.lge.camera.constants.CameraConstants.TAG, "Find Focused Image");
            r0 = poll();
     */
    /* JADX WARNING: Missing block: B:38:0x00bf, code:
            if (r0 == null) goto L_0x00cf;
     */
    /* JADX WARNING: Missing block: B:40:0x00c5, code:
            if (r0.getFocusState() != false) goto L_0x00cf;
     */
    /* JADX WARNING: Missing block: B:41:0x00c7, code:
            r0.close();
            r0 = poll();
     */
    /* JADX WARNING: Missing block: B:42:0x00cf, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:53:?, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            return r0;
     */
    private com.lge.camera.device.api2.ImageItem getFocusedImage(boolean r7) {
        /*
        r6 = this;
        r4 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r2 = 1;
        r1 = r6.mCurrentQueueCnt;
        if (r1 >= r2) goto L_0x0017;
    L_0x0007:
        r1 = new java.util.concurrent.CountDownLatch;
        r1.<init>(r2);
        r6.mLatch = r1;
        r1 = 2;
        r6.mWaitCondition = r1;
        r6.startBuffering();
        r6.waitImage(r4);
    L_0x0017:
        r2 = r6.mLock;
        monitor-enter(r2);
        r1 = r6.hasFocusedImage();	 Catch:{ all -> 0x0096 }
        if (r1 != 0) goto L_0x002d;
    L_0x0020:
        if (r7 == 0) goto L_0x0028;
    L_0x0022:
        r0 = r6.poll();	 Catch:{ all -> 0x0096 }
    L_0x0026:
        monitor-exit(r2);	 Catch:{ all -> 0x0096 }
    L_0x0027:
        return r0;
    L_0x0028:
        r0 = r6.peak();	 Catch:{ all -> 0x0096 }
        goto L_0x0026;
    L_0x002d:
        if (r7 == 0) goto L_0x0048;
    L_0x002f:
        r0 = r6.poll();	 Catch:{ all -> 0x0096 }
    L_0x0033:
        if (r0 == 0) goto L_0x0052;
    L_0x0035:
        r1 = r0.getFocusState();	 Catch:{ all -> 0x0096 }
        if (r1 != 0) goto L_0x0052;
    L_0x003b:
        r0.close();	 Catch:{ all -> 0x0096 }
        if (r7 != 0) goto L_0x004d;
    L_0x0040:
        r6.poll();	 Catch:{ all -> 0x0096 }
        r0 = r6.peak();	 Catch:{ all -> 0x0096 }
        goto L_0x0033;
    L_0x0048:
        r0 = r6.peak();	 Catch:{ all -> 0x0096 }
        goto L_0x0033;
    L_0x004d:
        r0 = r6.poll();	 Catch:{ all -> 0x0096 }
        goto L_0x0033;
    L_0x0052:
        if (r0 == 0) goto L_0x0099;
    L_0x0054:
        r1 = "CameraApp";
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0096 }
        r3.<init>();	 Catch:{ all -> 0x0096 }
        r4 = " Queue count ";
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = r6.mCurrentQueueCnt;	 Catch:{ all -> 0x0096 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = " Queue Front ";
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = r6.mQueueFront;	 Catch:{ all -> 0x0096 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = " Queue Rear ";
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = r6.mQueueRear;	 Catch:{ all -> 0x0096 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = " item ";
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r4 = r0.getFocusState();	 Catch:{ all -> 0x0096 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0096 }
        r3 = r3.toString();	 Catch:{ all -> 0x0096 }
        com.lge.camera.util.CamLog.m7i(r1, r3);	 Catch:{ all -> 0x0096 }
        monitor-exit(r2);	 Catch:{ all -> 0x0096 }
        goto L_0x0027;
    L_0x0096:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0096 }
        throw r1;
    L_0x0099:
        r6.startBuffering();	 Catch:{ all -> 0x0096 }
        r1 = new java.util.concurrent.CountDownLatch;	 Catch:{ all -> 0x0096 }
        r3 = 1;
        r1.<init>(r3);	 Catch:{ all -> 0x0096 }
        r6.mLatch = r1;	 Catch:{ all -> 0x0096 }
        r1 = 1;
        r6.mWaitCondition = r1;	 Catch:{ all -> 0x0096 }
        monitor-exit(r2);	 Catch:{ all -> 0x0096 }
        r1 = r6.waitImage(r4);
        if (r1 != 0) goto L_0x00b1;
    L_0x00ae:
        r0 = 0;
        goto L_0x0027;
    L_0x00b1:
        r2 = r6.mLock;
        monitor-enter(r2);
        r1 = "CameraApp";
        r3 = "Find Focused Image";
        com.lge.camera.util.CamLog.m7i(r1, r3);	 Catch:{ all -> 0x00d2 }
        r0 = r6.poll();	 Catch:{ all -> 0x00d2 }
    L_0x00bf:
        if (r0 == 0) goto L_0x00cf;
    L_0x00c1:
        r1 = r0.getFocusState();	 Catch:{ all -> 0x00d2 }
        if (r1 != 0) goto L_0x00cf;
    L_0x00c7:
        r0.close();	 Catch:{ all -> 0x00d2 }
        r0 = r6.poll();	 Catch:{ all -> 0x00d2 }
        goto L_0x00bf;
    L_0x00cf:
        monitor-exit(r2);	 Catch:{ all -> 0x00d2 }
        goto L_0x0027;
    L_0x00d2:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x00d2 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.MultiFrameBufferManager.getFocusedImage(boolean):com.lge.camera.device.api2.ImageItem");
    }

    private boolean checkImageState(ImageItem item, Builder builder) {
        if (item == null || !item.isValid() || builder == null) {
            return false;
        }
        CaptureRequest request = item.getMetadata().getRequest();
        Rect zoomRect = (Rect) builder.get(CaptureRequest.SCALER_CROP_REGION);
        if (zoomRect == null || zoomRect.equals(request.get(CaptureRequest.SCALER_CROP_REGION))) {
            return true;
        }
        CamLog.m7i(CameraConstants.TAG, "Not matched zoom");
        return false;
    }

    public ImageItem getMatchedImage(boolean isPoll, Builder builder) {
        if (builder == null) {
            return null;
        }
        int retry = 5;
        do {
            ImageItem item = getFocusedImage(isPoll);
            synchronized (this.mLock) {
                if (item == null) {
                    return null;
                } else if (checkImageState(item, builder)) {
                    return item;
                } else {
                    item.close();
                    if (!isPoll) {
                        poll();
                    }
                    CamLog.m7i(CameraConstants.TAG, "retry Image");
                    retry--;
                }
            }
        } while (retry > 0);
        return null;
    }

    private boolean waitImage(long time) {
        try {
            CamLog.m7i(CameraConstants.TAG, "Wait Images during " + time);
            boolean ret = this.mLatch.await(time, TimeUnit.MILLISECONDS);
            synchronized (this.mLock) {
                this.mWaitCondition = 0;
                this.mLatch = null;
            }
            return ret;
        } catch (InterruptedException e) {
            e.printStackTrace();
            synchronized (this.mLock) {
                this.mWaitCondition = 0;
                this.mLatch = null;
                return false;
            }
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mWaitCondition = 0;
                this.mLatch = null;
            }
        }
    }

    private void abortWaitingImage() {
        synchronized (this.mLock) {
            if (this.mLatch != null) {
                CamLog.m7i(CameraConstants.TAG, "abortWaitingImage " + this.mLatch.getCount());
                while (this.mLatch.getCount() > 0) {
                    this.mLatch.countDown();
                }
            }
        }
    }

    public ArrayList<ImageItem> poll(int count, boolean ignoreMatchedMeta) {
        if (this.mLatch != null) {
            return null;
        }
        synchronized (this.mLock) {
            if (getBufferSize() < count) {
                setBufferSize(count);
            }
            int waitCnt = count - this.mCurrentQueueCnt;
            ArrayList<ImageItem> imageItems;
            if (waitCnt > 0) {
                this.mIgnoreMeta = ignoreMatchedMeta;
                if (this.mIgnoreMeta) {
                    addIncomleteImage();
                    waitCnt = count - this.mCurrentQueueCnt;
                    if (waitCnt <= 0) {
                        this.mIgnoreMeta = false;
                        imageItems = getImageItems(count);
                        return imageItems;
                    }
                }
                startBuffering();
                this.mLatch = new CountDownLatch(waitCnt);
                this.mWaitCondition = 2;
                waitImage((long) (waitCnt * 200));
                synchronized (this.mLock) {
                    this.mIgnoreMeta = false;
                    imageItems = getImageItems(count);
                }
                return imageItems;
            }
            imageItems = getImageItems(count);
            return imageItems;
        }
    }

    private void addIncomleteImage() {
        synchronized (this.mLock) {
            if (this.mIncompleteImageCnt == 0) {
                return;
            }
            for (int i = 0; i < this.mIncompleteBuffer.length; i++) {
                ImageItem item = this.mIncompleteBuffer[i];
                if (!(item == null || item.getImage() == null)) {
                    addQueue(item);
                }
                this.mIncompleteBuffer[i] = null;
            }
            this.mIncompleteImageCnt = 0;
            this.mIncompleteMetaCnt = 0;
        }
    }

    public boolean hasFocusedImage() {
        return this.mFocusedAge > 0 && this.mFocusedAge <= this.mCurrentQueueCnt;
    }

    public int getBufferSize() {
        return this.mNumBuffer;
    }

    public int getFrameWidth() {
        return this.mFrameWidth;
    }

    public int getFrameHeight() {
        return this.mFrameHeight;
    }

    public void startBuffering() {
        synchronized (this.mLock) {
            if (getState() == 3) {
                CamLog.m3d(CameraConstants.TAG, "buffering is already started");
            } else if (getState() == 2 || getState() == 4) {
                setState(3);
                CamLog.m3d(CameraConstants.TAG, "buffering is started");
            } else {
                CamLog.m3d(CameraConstants.TAG, "cannot start buffering; do nothing : " + getState());
            }
        }
    }

    public void stopBuffering() {
        synchronized (this.mLock) {
            if (getState() != 3) {
                CamLog.m3d(CameraConstants.TAG, "buffering is not started; do nothing : " + getState());
                return;
            }
            setState(4);
            CamLog.m3d(CameraConstants.TAG, "buffering is stopped");
        }
    }

    private void setState(int state) {
        CamLog.m3d(CameraConstants.TAG, "mState = " + state);
        synchronized (this.mLock) {
            this.mState = state;
        }
    }

    public int getState() {
        return this.mState;
    }

    public boolean isConfigured() {
        return this.mState != 0;
    }

    public void release() {
        setState(0);
        clearBuffer();
        abortWaitingImage();
    }

    public void setMetaChecker(ResultChecker checker) {
        synchronized (this.mLock) {
            this.mMetaChecker = checker;
            this.mIgnoreMeta = false;
        }
    }

    private boolean isAddableMeta(TotalCaptureResult metadata) {
        if (this.mIgnoreMeta) {
            return false;
        }
        if (this.mMetaChecker == null) {
            return true;
        }
        return this.mMetaChecker.isValid(metadata);
    }

    private void checkImageCount() {
        if (this.mCurrentQueueCnt + this.mIncompleteImageCnt >= 10) {
            remove();
        }
    }

    /* JADX WARNING: Missing block: B:13:0x002e, code:
            if (waitImage(r10) != false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:14:0x0030, code:
            setMetaChecker(null);
            com.lge.camera.util.CamLog.m7i(com.lge.camera.constants.CameraConstants.TAG, "Fail Gahtering Image Timeout!!");
     */
    /* JADX WARNING: Missing block: B:19:0x003e, code:
            r3 = r7.mLock;
     */
    /* JADX WARNING: Missing block: B:20:0x0040, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            setMetaChecker(null);
            stopBuffering();
            com.lge.camera.util.CamLog.m7i(com.lge.camera.constants.CameraConstants.TAG, " GatherImage done " + r7.mCurrentQueueCnt);
     */
    /* JADX WARNING: Missing block: B:24:0x0062, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:34:?, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            return true;
     */
    public boolean gatherImage(int r8, com.lge.camera.device.api2.ResultChecker r9, long r10) {
        /*
        r7 = this;
        r5 = 0;
        r2 = 1;
        r1 = 0;
        r0 = 0;
        if (r8 > 0) goto L_0x0007;
    L_0x0006:
        return r1;
    L_0x0007:
        r3 = r7.mLock;
        monitor-enter(r3);
        r7.setMetaChecker(r9);	 Catch:{ all -> 0x003b }
        r7.removeInvalidImageItem(r9);	 Catch:{ all -> 0x003b }
        r7.setBufferSize(r8);	 Catch:{ all -> 0x003b }
        r4 = r7.mCurrentQueueCnt;	 Catch:{ all -> 0x003b }
        r0 = r8 - r4;
        if (r0 > 0) goto L_0x001f;
    L_0x0019:
        r7.stopBuffering();	 Catch:{ all -> 0x003b }
        monitor-exit(r3);	 Catch:{ all -> 0x003b }
        r1 = r2;
        goto L_0x0006;
    L_0x001f:
        r4 = new java.util.concurrent.CountDownLatch;	 Catch:{ all -> 0x003b }
        r4.<init>(r0);	 Catch:{ all -> 0x003b }
        r7.mLatch = r4;	 Catch:{ all -> 0x003b }
        r4 = 2;
        r7.mWaitCondition = r4;	 Catch:{ all -> 0x003b }
        monitor-exit(r3);	 Catch:{ all -> 0x003b }
        r3 = r7.waitImage(r10);
        if (r3 != 0) goto L_0x003e;
    L_0x0030:
        r7.setMetaChecker(r5);
        r2 = "CameraApp";
        r3 = "Fail Gahtering Image Timeout!!";
        com.lge.camera.util.CamLog.m7i(r2, r3);
        goto L_0x0006;
    L_0x003b:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x003b }
        throw r1;
    L_0x003e:
        r3 = r7.mLock;
        monitor-enter(r3);
        r1 = 0;
        r7.setMetaChecker(r1);	 Catch:{ all -> 0x0065 }
        r7.stopBuffering();	 Catch:{ all -> 0x0065 }
        r1 = "CameraApp";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0065 }
        r4.<init>();	 Catch:{ all -> 0x0065 }
        r5 = " GatherImage done ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0065 }
        r5 = r7.mCurrentQueueCnt;	 Catch:{ all -> 0x0065 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0065 }
        r4 = r4.toString();	 Catch:{ all -> 0x0065 }
        com.lge.camera.util.CamLog.m7i(r1, r4);	 Catch:{ all -> 0x0065 }
        monitor-exit(r3);	 Catch:{ all -> 0x0065 }
        r1 = r2;
        goto L_0x0006;
    L_0x0065:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0065 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.MultiFrameBufferManager.gatherImage(int, com.lge.camera.device.api2.ResultChecker, long):boolean");
    }

    private void removeInvalidImageItem(ResultChecker checker) {
        synchronized (this.mLock) {
            int i;
            ImageItem item;
            if (this.mCurrentQueueCnt > 0) {
                int cnt = this.mCurrentQueueCnt;
                for (i = 0; i < cnt; i++) {
                    item = poll();
                    if (item != null) {
                        if (checker == null || !checker.isValid(item.getMetadata())) {
                            item.close();
                        } else {
                            addQueue(item);
                        }
                    }
                }
            }
            if (this.mIncompleteMetaCnt <= 0) {
                return;
            }
            for (i = 0; i < this.mIncompleteBuffer.length; i++) {
                item = this.mIncompleteBuffer[i];
                if (!(item == null || item.getMetadata() == null || (checker != null && checker.isValid(item.getMetadata())))) {
                    this.mIncompleteBuffer[i] = null;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            return;
     */
    public void drop(android.hardware.camera2.CaptureRequest r4) {
        /*
        r3 = this;
        r1 = r3.mLock;
        monitor-enter(r1);
        r0 = r3.mLatch;	 Catch:{ all -> 0x001b }
        if (r0 == 0) goto L_0x000b;
    L_0x0007:
        r0 = r3.mIgnoreMeta;	 Catch:{ all -> 0x001b }
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x001b }
    L_0x000c:
        return;
    L_0x000d:
        r0 = r3.mMetaChecker;	 Catch:{ all -> 0x001b }
        if (r0 == 0) goto L_0x0027;
    L_0x0011:
        r0 = r3.mMetaChecker;	 Catch:{ all -> 0x001b }
        r0 = r0.isValid(r4);	 Catch:{ all -> 0x001b }
        if (r0 != 0) goto L_0x001e;
    L_0x0019:
        monitor-exit(r1);	 Catch:{ all -> 0x001b }
        goto L_0x000c;
    L_0x001b:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x001b }
        throw r0;
    L_0x001e:
        r0 = r3.mMetaChecker;	 Catch:{ all -> 0x001b }
        r2 = r4.hashCode();	 Catch:{ all -> 0x001b }
        r0.removeCheckCondition(r2);	 Catch:{ all -> 0x001b }
    L_0x0027:
        r0 = "CameraApp";
        r2 = " capture failed";
        com.lge.camera.util.CamLog.m7i(r0, r2);	 Catch:{ all -> 0x001b }
        r0 = r3.mLatch;	 Catch:{ all -> 0x001b }
        r0.countDown();	 Catch:{ all -> 0x001b }
        monitor-exit(r1);	 Catch:{ all -> 0x001b }
        goto L_0x000c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.device.api2.MultiFrameBufferManager.drop(android.hardware.camera2.CaptureRequest):void");
    }
}
