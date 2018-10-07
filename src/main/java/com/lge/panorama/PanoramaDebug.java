package com.lge.panorama;

import android.media.Image;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PanoramaDebug {
    public static final int DUMP_MAX = 30;
    public static final Object mFrameSyncObjet = new Object();
    public final String DUMP_DIR = "yuv_dump";
    public final String FILE_PATH = "/mnt/sdcard/DCIM/";
    public final String PREV_DUMP_DIR = "preview_dump";
    public final String TAG = CameraConstants.TAG;
    public String mDumpDir = "";
    public Thread mDumpThread = null;
    public boolean mDumping = true;
    public long mFrameCount = 0;
    public int mInputH = 2340;
    public int mInputW = 4160;
    public String mJpgDir = "";
    public ConcurrentLinkedQueue<byte[]> mQueueFrame = null;

    /* renamed from: com.lge.panorama.PanoramaDebug$3 */
    class C14473 implements Runnable {
        C14473() {
        }

        public void run() {
            while (PanoramaDebug.this.mDumping) {
                synchronized (PanoramaDebug.mFrameSyncObjet) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    String fileName = "yuv_raw_" + String.format("%d", new Object[]{Long.valueOf(PanoramaDebug.this.mFrameCount)});
                    byte[] data = PanoramaDebug.this.getCurFrame();
                    if (data == null) {
                    } else {
                        PanoramaDebug.this.d_save_convert_raw(data, PanoramaDebug.this.mDumpDir, fileName, PanoramaDebug.this.mInputW, PanoramaDebug.this.mInputH);
                        Log.d(CameraConstants.TAG, "Frame is saved : " + fileName);
                        PanoramaDebug panoramaDebug = PanoramaDebug.this;
                        panoramaDebug.mFrameCount++;
                    }
                }
            }
        }
    }

    public interface PanoramaDebugListener {
        boolean saveComplete();
    }

    public void d_save_raw(byte[] data, String folderPath, String name) {
        saveYUVraw(data, String.format(Locale.US, "%s/%s.yuv", new Object[]{folderPath, name}));
    }

    public void setInputSize(int width, int height) {
        Log.d(CameraConstants.TAG, "[FRAME_DUMP] mInputW : " + width + ", mInputH : " + height);
        this.mInputW = width;
        this.mInputH = height;
    }

    public void d_save_convert_raw(byte[] data, String folderPath, String name, int input_width, int input_height) {
        Log.d(CameraConstants.TAG, "[FRAME_DUMP] d_save_convert_raw : (input_width, input_height) " + input_width + ", " + input_height);
        saveYUVraw(removeYUVstride_scnaline(data, input_width, input_height), String.format(Locale.US, "%s/%s.yuv", new Object[]{folderPath, name}));
    }

    public boolean createDumpFolder(String dump_dir) {
        File dumpDir = new File("/mnt/sdcard/DCIM/" + dump_dir);
        if (dumpDir != null) {
            if (dumpDir.exists()) {
                if (dumpDir.isDirectory()) {
                    return true;
                }
            } else if (dumpDir.mkdir() && dumpDir.exists() && dumpDir.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    public String createCurrentDumpDir(String dump_dir) {
        String dumpPath = "/mnt/sdcard/DCIM/" + dump_dir + "/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File dumpDir = new File(dumpPath);
        if (dumpDir != null) {
            if (dumpDir.exists()) {
                if (dumpDir.isDirectory()) {
                    return dumpPath;
                }
            } else if (dumpDir.mkdir() && dumpDir.exists() && dumpDir.isDirectory()) {
                return dumpPath;
            }
        }
        return "/mnt/sdcard/DCIM/" + dump_dir;
    }

    public void createDumpPath() {
        if (createDumpFolder("yuv_dump")) {
            this.mDumpDir = createCurrentDumpDir("yuv_dump");
            if (this.mDumpDir == null) {
                Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
                return;
            } else {
                this.mJpgDir = createJpegPath(this.mDumpDir, "yuv_dump");
                return;
            }
        }
        Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
    }

    public String createJpegPath(String dumpPath, String dump_dir) {
        if (dumpPath == null) {
            Log.e(CameraConstants.TAG, "Fail to create jpeg folder!!");
            return null;
        }
        String jpgPath = dumpPath + "/jpeg";
        File jpgDir = new File(jpgPath);
        if (jpgDir != null) {
            if (jpgDir.exists()) {
                if (jpgDir.isDirectory()) {
                    return jpgPath;
                }
            } else if (jpgDir.mkdir() && jpgDir.exists() && jpgDir.isDirectory()) {
                return jpgPath;
            }
        }
        return "/mnt/sdcard/DCIM/" + dump_dir + "/jpeg";
    }

    public void createFrameQueue() {
        this.mQueueFrame = new ConcurrentLinkedQueue();
    }

    public void directSaveFrameUsingThread(byte[] data) {
        if (data == null) {
            Log.e(CameraConstants.TAG, "byte data is null");
            return;
        }
        if (this.mDumpDir == null) {
            Log.e(CameraConstants.TAG, "Dump dir is not set!");
        }
        final byte[] cloneData = (byte[]) data.clone();
        new Thread(new Runnable() {
            public void run() {
                PanoramaDebug.this.d_save_convert_raw(cloneData, PanoramaDebug.this.mDumpDir, "yuv_raw_" + String.format("%d", new Object[]{Long.valueOf(PanoramaDebug.this.mFrameCount)}), PanoramaDebug.this.mInputW, PanoramaDebug.this.mInputH);
                PanoramaDebug panoramaDebug = PanoramaDebug.this;
                panoramaDebug.mFrameCount++;
            }
        }).start();
    }

    public String directSaveFrameByteArray(byte[] data) {
        if (data == null) {
            Log.e(CameraConstants.TAG, "byte data is null");
            return null;
        }
        if (this.mDumpDir == null) {
            Log.e(CameraConstants.TAG, "Dump dir is not set!");
        }
        String fileName = "yuv_raw_" + String.format("%d", new Object[]{Long.valueOf(this.mFrameCount)});
        d_save_convert_raw(data, this.mDumpDir, fileName, this.mInputW, this.mInputH);
        this.mFrameCount++;
        return fileName;
    }

    public void addFrame(byte[] data) {
        if (this.mQueueFrame == null) {
            createFrameQueue();
        }
        if (data == null) {
            Log.d(CameraConstants.TAG, "Frame data is null.");
        } else if (this.mQueueFrame.size() <= 30) {
            ByteBuffer new_buf = ByteBuffer.allocateDirect(data.length);
            byte[] temp = new_buf.array();
            Log.d(CameraConstants.TAG, "[FRAME_DUMP] PANO_DEBUG add frame : " + data + ", newbuf : " + new_buf + "- [START]");
            System.arraycopy(data, 0, temp, 0, data.length);
            this.mQueueFrame.offer(temp);
            Log.d(CameraConstants.TAG, "[FRAME_DUMP] PANO_DEBUG add frame, mQueueFrame : " + this.mQueueFrame.size() + "- [END]");
        }
    }

    public void pollDumpQueue(int checkSize) {
        if (this.mQueueFrame != null) {
            ConcurrentLinkedQueue<byte[]> tempQueue = new ConcurrentLinkedQueue();
            while (this.mQueueFrame.size() > 0) {
                byte[] buffer = (byte[]) this.mQueueFrame.poll();
                if (this.mQueueFrame.size() == checkSize - 1) {
                    Log.d(CameraConstants.TAG, "[FRAME_DUMP] pollDumpQueue : " + buffer);
                } else {
                    tempQueue.offer(buffer);
                }
            }
            this.mQueueFrame.clear();
            this.mQueueFrame = tempQueue;
        }
    }

    public void saveQueueAll(final PanoramaDebugListener listener) {
        if (this.mQueueFrame != null) {
            synchronized (mFrameSyncObjet) {
                new Thread(new Runnable() {
                    public void run() {
                        while (PanoramaDebug.this.mQueueFrame.size() > 0) {
                            byte[] data = (byte[]) PanoramaDebug.this.mQueueFrame.poll();
                            String fileName = PanoramaDebug.this.directSaveFrameByteArray(data);
                            if (fileName != null) {
                                PanoramaDebug.this.saveJpegFileWithRemovingStrideScanline(data, PanoramaDebug.this.mInputW, PanoramaDebug.this.mInputH, PanoramaDebug.this.mJpgDir + "/" + fileName + ".jpg");
                            }
                        }
                        if (listener != null) {
                            listener.saveComplete();
                        }
                    }
                }).start();
            }
        } else if (listener != null) {
            listener.saveComplete();
        }
    }

    public void unbind() {
        synchronized (mFrameSyncObjet) {
            if (this.mQueueFrame != null) {
                this.mQueueFrame.clear();
                this.mQueueFrame = null;
            }
            if (this.mDumping) {
                stopDumpThread();
            }
        }
    }

    public byte[] getCurFrame() {
        if (this.mQueueFrame == null || this.mQueueFrame.size() <= 0) {
            return null;
        }
        return (byte[]) this.mQueueFrame.poll();
    }

    public void startDumpThread(String dump_dir) {
        if (createDumpFolder(dump_dir)) {
            this.mDumpDir = createCurrentDumpDir(dump_dir);
            if (this.mDumpDir == null) {
                Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
                return;
            }
            this.mDumping = true;
            this.mFrameCount = 0;
            if (this.mDumpThread == null) {
                this.mDumpThread = new Thread(new C14473());
                this.mDumpThread.start();
                return;
            }
            return;
        }
        Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
    }

    public void stopDumpThread() {
        this.mDumping = false;
        if (this.mDumpThread != null && this.mDumpThread.isAlive()) {
            try {
                this.mDumpThread.interrupt();
                this.mDumpThread.join(CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mDumpThread = null;
    }

    public byte[] removeYUVstride_scnaline(byte[] input, int input_width, int input_height) {
        int h;
        int stride = (input_width + 63) & -64;
        int scanline = (input_height + 63) & -64;
        Log.d(CameraConstants.TAG, "[FRAME_DUMP] stride : " + stride + ", scanline : " + scanline);
        byte[] dst = new byte[((int) (((float) (input_width * input_height)) * 1.5f))];
        for (h = 0; h < input_height; h++) {
            System.arraycopy(input, h * stride, dst, h * input_width, input_width);
        }
        int uv_h = input_height >> 1;
        int uv_pos = ((stride * scanline) + 63) & -64;
        int dst_pos = input_width * input_height;
        for (h = 0; h < uv_h; h++) {
            System.arraycopy(input, (h * stride) + uv_pos, dst, (h * input_width) + dst_pos, input_width);
        }
        return dst;
    }

    public boolean saveJpegFileWithRemovingStrideScanline(byte[] yuv_input, int input_width, int input_height, String jpgPath) {
        return saveJpegFile(removeYUVstride_scnaline(yuv_input, input_width, input_height), input_width, input_height, jpgPath, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d A:{SYNTHETIC, Splitter: B:12:0x002d} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0035 A:{Catch:{ IOException -> 0x0039 }} */
    public boolean saveJpegFile(byte[] r11, int r12, int r13, java.lang.String r14, int[] r15) {
        /*
        r10 = this;
        r0 = new android.graphics.YuvImage;
        r2 = 17;
        r1 = r11;
        r3 = r12;
        r4 = r13;
        r5 = r15;
        r0.<init>(r1, r2, r3, r4, r5);
        r7 = 0;
        r8 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0027 }
        r8.<init>(r14);	 Catch:{ FileNotFoundException -> 0x0027 }
        r1 = new android.graphics.Rect;	 Catch:{ FileNotFoundException -> 0x0044, all -> 0x0041 }
        r2 = 0;
        r3 = 0;
        r1.<init>(r2, r3, r12, r13);	 Catch:{ FileNotFoundException -> 0x0044, all -> 0x0041 }
        r2 = 90;
        r9 = r0.compressToJpeg(r1, r2, r8);	 Catch:{ FileNotFoundException -> 0x0044, all -> 0x0041 }
        r8.flush();	 Catch:{ FileNotFoundException -> 0x0044, all -> 0x0041 }
        if (r8 == 0) goto L_0x0026;
    L_0x0023:
        r8.close();	 Catch:{ IOException -> 0x003e }
    L_0x0026:
        return r9;
    L_0x0027:
        r6 = move-exception;
    L_0x0028:
        r6.printStackTrace();	 Catch:{ all -> 0x0032 }
        if (r7 == 0) goto L_0x0030;
    L_0x002d:
        r7.close();	 Catch:{ IOException -> 0x0039 }
    L_0x0030:
        r9 = 0;
        goto L_0x0026;
    L_0x0032:
        r1 = move-exception;
    L_0x0033:
        if (r7 == 0) goto L_0x0038;
    L_0x0035:
        r7.close();	 Catch:{ IOException -> 0x0039 }
    L_0x0038:
        throw r1;	 Catch:{ IOException -> 0x0039 }
    L_0x0039:
        r6 = move-exception;
    L_0x003a:
        r6.printStackTrace();
        goto L_0x0030;
    L_0x003e:
        r6 = move-exception;
        r7 = r8;
        goto L_0x003a;
    L_0x0041:
        r1 = move-exception;
        r7 = r8;
        goto L_0x0033;
    L_0x0044:
        r6 = move-exception;
        r7 = r8;
        goto L_0x0028;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.panorama.PanoramaDebug.saveJpegFile(byte[], int, int, java.lang.String, int[]):boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0019 A:{SYNTHETIC, Splitter: B:13:0x0019} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0025 A:{SYNTHETIC, Splitter: B:19:0x0025} */
    public void saveYUVraw(byte[] r5, java.lang.String r6) {
        /*
        r4 = this;
        r1 = 0;
        r2 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0013 }
        r2.<init>(r6);	 Catch:{ FileNotFoundException -> 0x0013 }
        r2.write(r5);	 Catch:{ FileNotFoundException -> 0x002f, all -> 0x002c }
        r2.flush();	 Catch:{ FileNotFoundException -> 0x002f, all -> 0x002c }
        if (r2 == 0) goto L_0x0032;
    L_0x000e:
        r2.close();	 Catch:{ IOException -> 0x0029 }
        r1 = r2;
    L_0x0012:
        return;
    L_0x0013:
        r0 = move-exception;
    L_0x0014:
        r0.printStackTrace();	 Catch:{ all -> 0x0022 }
        if (r1 == 0) goto L_0x0012;
    L_0x0019:
        r1.close();	 Catch:{ IOException -> 0x001d }
        goto L_0x0012;
    L_0x001d:
        r0 = move-exception;
    L_0x001e:
        r0.printStackTrace();
        goto L_0x0012;
    L_0x0022:
        r3 = move-exception;
    L_0x0023:
        if (r1 == 0) goto L_0x0028;
    L_0x0025:
        r1.close();	 Catch:{ IOException -> 0x001d }
    L_0x0028:
        throw r3;	 Catch:{ IOException -> 0x001d }
    L_0x0029:
        r0 = move-exception;
        r1 = r2;
        goto L_0x001e;
    L_0x002c:
        r3 = move-exception;
        r1 = r2;
        goto L_0x0023;
    L_0x002f:
        r0 = move-exception;
        r1 = r2;
        goto L_0x0014;
    L_0x0032:
        r1 = r2;
        goto L_0x0012;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.panorama.PanoramaDebug.saveYUVraw(byte[], java.lang.String):void");
    }

    public void savePanoramaMiniPreview() {
        if (createDumpFolder("preview_dump")) {
            this.mDumpDir = createCurrentDumpDir("preview_dump");
            if (this.mDumpDir == null) {
                Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
                return;
            } else {
                this.mJpgDir = createJpegPath(this.mDumpDir, "preview_dump");
                return;
            }
        }
        Log.e(CameraConstants.TAG, "Fail to create dump folder!!");
    }

    public void directSaveFrame(Image image, int frameType) {
        if (image == null) {
            Log.e(CameraConstants.TAG, "Input ImageReader is null");
            return;
        }
        if (this.mDumpDir == null) {
            Log.e(CameraConstants.TAG, "Dump dir is not set!");
        }
        ByteBuffer yPlanes = image.getPlanes()[0].getBuffer();
        ByteBuffer vuPlanes = image.getPlanes()[2].getBuffer();
        int input_width = image.getWidth();
        int input_height = image.getHeight();
        int[] strides = new int[]{image.getPlanes()[0].getRowStride(), image.getPlanes()[1].getRowStride(), image.getPlanes()[2].getRowStride()};
        int ySize = strides[0] * image.getHeight();
        byte[] yuvBuffer = new byte[((ySize * 3) / 2)];
        yPlanes.get(yuvBuffer, 0, yPlanes.remaining());
        vuPlanes.get(yuvBuffer, ySize, vuPlanes.remaining());
        String fileName = "HAL3_ImageReader_raw_" + String.format("%d", new Object[]{Long.valueOf(this.mFrameCount)});
        if (frameType == 0) {
            saveYUVraw(yuvBuffer, this.mDumpDir + "/" + fileName + ".yuv");
        } else if (frameType == 1) {
            saveJpegFile(yuvBuffer, input_width, input_height, this.mJpgDir + "/" + fileName + ".jpg", strides);
        }
        this.mFrameCount++;
    }
}
