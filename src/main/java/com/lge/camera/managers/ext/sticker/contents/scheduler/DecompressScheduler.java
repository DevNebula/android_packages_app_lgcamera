package com.lge.camera.managers.ext.sticker.contents.scheduler;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import com.arcsoft.stickerlibrary.utils.ZipUtil;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil;
import com.lge.camera.managers.ext.sticker.utils.StickerUtil;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DecompressScheduler extends ThreadPoolExecutor {
    private static final int ENCRYTED_VERSION_BASE = 10100000;
    private static final String PRELOADED_WORKER_SET = "PRELOADED_CONTENT";
    private static final String TAG = "DecompressScheduler";
    private static final boolean USE_DECRYPTION = true;
    private static DecompressScheduler mInstance;
    private Callback mCallback;
    private WeakReference<Context> mContext;
    private HashSet<String> mWorkerSet = new HashSet();

    public interface Callback {
        void onDecompressComplete(String str);

        void onDecompressStarted(String str);

        void onPreloadDecompressComplete(boolean z);

        void onPreloadDecompressStart();
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler$1 */
    class C13531 implements FileFilter {
        C13531() {
        }

        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler$2 */
    class C13542 implements FilenameFilter {
        C13542() {
        }

        public boolean accept(File file, String s) {
            return s.endsWith(".origin");
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler$3 */
    class C13553 implements FilenameFilter {
        C13553() {
        }

        public boolean accept(File dir, String name) {
            if (name.endsWith(ZipUtil.EXT)) {
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler$4 */
    class C13564 implements Comparator<File> {
        C13564() {
        }

        public int compare(File o1, File o2) {
            if (o1.lastModified() > o2.lastModified()) {
                return 1;
            }
            return -1;
        }
    }

    public class Job implements Runnable {
        private Uri mDownLoadedFileUri;
        private String mFilesDir;
        private String mFoldername;

        public Job(Uri uri, String fname, String filesdir) {
            this.mDownLoadedFileUri = uri;
            this.mFoldername = fname;
            this.mFilesDir = filesdir;
        }

        /* JADX WARNING: Removed duplicated region for block: B:105:0x039e A:{SYNTHETIC, Splitter: B:105:0x039e} */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x03a3 A:{SYNTHETIC, Splitter: B:108:0x03a3} */
        /* JADX WARNING: Removed duplicated region for block: B:77:0x02fb A:{SYNTHETIC, Splitter: B:77:0x02fb} */
        /* JADX WARNING: Removed duplicated region for block: B:80:0x0300 A:{SYNTHETIC, Splitter: B:80:0x0300} */
        /* JADX WARNING: Removed duplicated region for block: B:92:0x035c A:{SYNTHETIC, Splitter: B:92:0x035c} */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x0361 A:{SYNTHETIC, Splitter: B:95:0x0361} */
        /* JADX WARNING: Removed duplicated region for block: B:105:0x039e A:{SYNTHETIC, Splitter: B:105:0x039e} */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x03a3 A:{SYNTHETIC, Splitter: B:108:0x03a3} */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x02ac A:{SYNTHETIC, Splitter: B:58:0x02ac} */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x02b1 A:{SYNTHETIC, Splitter: B:61:0x02b1} */
        /* JADX WARNING: Removed duplicated region for block: B:77:0x02fb A:{SYNTHETIC, Splitter: B:77:0x02fb} */
        /* JADX WARNING: Removed duplicated region for block: B:80:0x0300 A:{SYNTHETIC, Splitter: B:80:0x0300} */
        /* JADX WARNING: Removed duplicated region for block: B:92:0x035c A:{SYNTHETIC, Splitter: B:92:0x035c} */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x0361 A:{SYNTHETIC, Splitter: B:95:0x0361} */
        public void run() {
            /*
            r26 = this;
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mContext;
            r10 = r4.get();
            r10 = (android.content.Context) r10;
            r0 = r26;
            r4 = r0.mDownLoadedFileUri;
            if (r4 == 0) goto L_0x00ce;
        L_0x0014:
            if (r10 == 0) goto L_0x00ce;
        L_0x0016:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : downalod = decompress file info :  ";
            r5 = r5.append(r6);
            r0 = r26;
            r6 = r0.mDownLoadedFileUri;
            r6 = r6.toString();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m3d(r4, r5);
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : download = decompress start : ";
            r5 = r5.append(r6);
            r6 = java.lang.System.nanoTime();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m5e(r4, r5);
            r13 = new java.io.File;
            r0 = r26;
            r4 = r0.mFilesDir;
            r5 = "Sticker";
            r13.<init>(r4, r5);
            r4 = r13.exists();
            if (r4 != 0) goto L_0x0066;
        L_0x0063:
            r13.mkdir();
        L_0x0066:
            r0 = r26;
            r4 = r0.mDownLoadedFileUri;
            r22 = r4.getPathSegments();
            if (r22 == 0) goto L_0x00eb;
        L_0x0070:
            r4 = r22.size();
            if (r4 <= 0) goto L_0x00eb;
        L_0x0076:
            r5 = "DecompressScheduler";
            r4 = new java.lang.StringBuilder;
            r4.<init>();
            r6 = "Decompress Thread : pathsegment = ";
            r6 = r4.append(r6);
            r4 = r22.size();
            r4 = r4 + -2;
            r0 = r22;
            r4 = r0.get(r4);
            r4 = (java.lang.String) r4;
            r4 = r6.append(r4);
            r4 = r4.toString();
            com.lge.camera.util.CamLog.m3d(r5, r4);
            r0 = r26;
            r5 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r4 = r22.size();
            r4 = r4 + -1;
            r0 = r22;
            r4 = r0.get(r4);
            r4 = (java.lang.String) r4;
            r4 = r6.append(r4);
            r6 = ".origin";
            r4 = r4.append(r6);
            r4 = r4.toString();
            r4 = r5.skipThisContent(r13, r4);
            if (r4 == 0) goto L_0x00cf;
        L_0x00c7:
            r4 = "DecompressScheduler";
            r5 = "Decompress Thread : downalod = decompress file info : this is spartha!!!! already exist";
            com.lge.camera.util.CamLog.m3d(r4, r5);
        L_0x00ce:
            return;
        L_0x00cf:
            r4 = "Sticker";
            r5 = r22.size();
            r5 = r5 + -2;
            r0 = r22;
            r5 = r0.get(r5);
            r4 = r4.equals(r5);
            if (r4 != 0) goto L_0x00eb;
        L_0x00e3:
            r4 = "DecompressScheduler";
            r5 = "Decompress Thread : downalod = decompress file info : this is not  sticker content!!!!!!";
            com.lge.camera.util.CamLog.m3d(r4, r5);
            goto L_0x00ce;
        L_0x00eb:
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mWorkerSet;
            r0 = r26;
            r5 = r0.mFoldername;
            r4.add(r5);
            r12 = new java.io.File;
            r4 = new java.lang.StringBuilder;
            r4.<init>();
            r0 = r26;
            r5 = r0.mFoldername;
            r4 = r4.append(r5);
            r5 = ".zip";
            r4 = r4.append(r5);
            r4 = r4.toString();
            r12.<init>(r13, r4);
            r17 = 0;
            r20 = 0;
            r4 = r10.getContentResolver();	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r0 = r26;
            r5 = r0.mDownLoadedFileUri;	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r6 = "r";
            r23 = r4.openFileDescriptor(r5, r6);	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r4 = r12.exists();	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            if (r4 != 0) goto L_0x0131;
        L_0x012e:
            r12.createNewFile();	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
        L_0x0131:
            r16 = r23.getFileDescriptor();	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r4 = r16.valid();	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            if (r4 == 0) goto L_0x0249;
        L_0x013b:
            r18 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r0 = r18;
            r1 = r16;
            r0.<init>(r1);	 Catch:{ FileNotFoundException -> 0x03f1, IOException -> 0x02e6, Exception -> 0x032b }
            r21 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x03f4, IOException -> 0x03e5, Exception -> 0x03d9, all -> 0x03cf }
            r0 = r21;
            r0.<init>(r12);	 Catch:{ FileNotFoundException -> 0x03f4, IOException -> 0x03e5, Exception -> 0x03d9, all -> 0x03cf }
            r4 = com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil.getRemotePreloadPackageVersion(r10);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = 10100000; // 0x9a1d20 float:1.4153114E-38 double:4.990063E-317;
            if (r4 < r5) goto L_0x0280;
        L_0x0154:
            r4 = r18.available();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r9 = new byte[r4];	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = 0;
            r5 = r18.available();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r18;
            r0.read(r9, r4, r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r2 = new com.lge.camera.zipcrypto.AESWrapper;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r2.<init>();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r11 = r2.decrypt(r9);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = 0;
            r5 = r11.length;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r21;
            r0.write(r11, r4, r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
        L_0x0174:
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.mCallback;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            if (r4 == 0) goto L_0x018d;
        L_0x017e:
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.mCallback;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r12.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4.onDecompressStarted(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
        L_0x018d:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5.<init>();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = "Decompress Thread : downalod = file copy end : ";
            r5 = r5.append(r6);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = java.lang.System.nanoTime();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r5.append(r6);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r5.toString();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            com.lge.camera.managers.ext.sticker.utils.UnZipUtil.Decompress(r12, r13);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            if (r22 == 0) goto L_0x01fe;
        L_0x01ae:
            r4 = r22.size();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            if (r4 <= 0) goto L_0x01fe;
        L_0x01b4:
            r19 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4.<init>();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r13.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.append(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = "/";
            r4 = r4.append(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r26;
            r5 = r0.mFoldername;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.append(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r4.toString();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6.<init>();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r22.size();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4 + -1;
            r0 = r22;
            r4 = r0.get(r4);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = (java.lang.String) r4;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r6.append(r4);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = ".origin";
            r4 = r4.append(r6);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.toString();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r19;
            r0.<init>(r5, r4);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r19.createNewFile();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
        L_0x01fe:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5.<init>();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = "Decompress Thread : downalod = decompress success: ";
            r5 = r5.append(r6);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r6 = java.lang.System.nanoTime();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r5.append(r6);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r5.toString();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r12.delete();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.mCallback;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            if (r4 == 0) goto L_0x0245;
        L_0x0227:
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.mWorkerSet;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r26;
            r5 = r0.mFoldername;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4.remove(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = r4.mCallback;	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r5 = r12.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4.onDecompressComplete(r5);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
        L_0x0245:
            r20 = r21;
            r17 = r18;
        L_0x0249:
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mWorkerSet;
            r0 = r26;
            r5 = r0.mFoldername;
            r4.remove(r5);
            if (r17 == 0) goto L_0x025d;
        L_0x025a:
            r17.close();	 Catch:{ IOException -> 0x02d2 }
        L_0x025d:
            if (r20 == 0) goto L_0x0262;
        L_0x025f:
            r20.close();	 Catch:{ IOException -> 0x02d7 }
        L_0x0262:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : downalod = decompress finally end : ";
            r5 = r5.append(r6);
            r6 = java.lang.System.nanoTime();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m3d(r4, r5);
            goto L_0x00ce;
        L_0x0280:
            r3 = r18.getChannel();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r8 = r21.getChannel();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r4 = 0;
            r6 = r3.size();	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            r3.transferTo(r4, r6, r8);	 Catch:{ FileNotFoundException -> 0x0293, IOException -> 0x03ea, Exception -> 0x03de, all -> 0x03d3 }
            goto L_0x0174;
        L_0x0293:
            r14 = move-exception;
            r20 = r21;
            r17 = r18;
        L_0x0298:
            r14.printStackTrace();	 Catch:{ all -> 0x038c }
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mWorkerSet;
            r0 = r26;
            r5 = r0.mFoldername;
            r4.remove(r5);
            if (r17 == 0) goto L_0x02af;
        L_0x02ac:
            r17.close();	 Catch:{ IOException -> 0x02dc }
        L_0x02af:
            if (r20 == 0) goto L_0x02b4;
        L_0x02b1:
            r20.close();	 Catch:{ IOException -> 0x02e1 }
        L_0x02b4:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : downalod = decompress finally end : ";
            r5 = r5.append(r6);
            r6 = java.lang.System.nanoTime();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m3d(r4, r5);
            goto L_0x00ce;
        L_0x02d2:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x025d;
        L_0x02d7:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x0262;
        L_0x02dc:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x02af;
        L_0x02e1:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x02b4;
        L_0x02e6:
            r14 = move-exception;
        L_0x02e7:
            r14.printStackTrace();	 Catch:{ all -> 0x038c }
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mWorkerSet;
            r0 = r26;
            r5 = r0.mFoldername;
            r4.remove(r5);
            if (r17 == 0) goto L_0x02fe;
        L_0x02fb:
            r17.close();	 Catch:{ IOException -> 0x0321 }
        L_0x02fe:
            if (r20 == 0) goto L_0x0303;
        L_0x0300:
            r20.close();	 Catch:{ IOException -> 0x0326 }
        L_0x0303:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : downalod = decompress finally end : ";
            r5 = r5.append(r6);
            r6 = java.lang.System.nanoTime();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m3d(r4, r5);
            goto L_0x00ce;
        L_0x0321:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x02fe;
        L_0x0326:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x0303;
        L_0x032b:
            r15 = move-exception;
        L_0x032c:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x038c }
            r5.<init>();	 Catch:{ all -> 0x038c }
            r6 = "excpetion :  ";
            r5 = r5.append(r6);	 Catch:{ all -> 0x038c }
            r6 = r15.getMessage();	 Catch:{ all -> 0x038c }
            r5 = r5.append(r6);	 Catch:{ all -> 0x038c }
            r5 = r5.toString();	 Catch:{ all -> 0x038c }
            com.lge.camera.util.CamLog.m5e(r4, r5);	 Catch:{ all -> 0x038c }
            r15.printStackTrace();	 Catch:{ all -> 0x038c }
            r0 = r26;
            r4 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r4 = r4.mWorkerSet;
            r0 = r26;
            r5 = r0.mFoldername;
            r4.remove(r5);
            if (r17 == 0) goto L_0x035f;
        L_0x035c:
            r17.close();	 Catch:{ IOException -> 0x0382 }
        L_0x035f:
            if (r20 == 0) goto L_0x0364;
        L_0x0361:
            r20.close();	 Catch:{ IOException -> 0x0387 }
        L_0x0364:
            r4 = "DecompressScheduler";
            r5 = new java.lang.StringBuilder;
            r5.<init>();
            r6 = "Decompress Thread : downalod = decompress finally end : ";
            r5 = r5.append(r6);
            r6 = java.lang.System.nanoTime();
            r5 = r5.append(r6);
            r5 = r5.toString();
            com.lge.camera.util.CamLog.m3d(r4, r5);
            goto L_0x00ce;
        L_0x0382:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x035f;
        L_0x0387:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x0364;
        L_0x038c:
            r4 = move-exception;
        L_0x038d:
            r0 = r26;
            r5 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r5 = r5.mWorkerSet;
            r0 = r26;
            r6 = r0.mFoldername;
            r5.remove(r6);
            if (r17 == 0) goto L_0x03a1;
        L_0x039e:
            r17.close();	 Catch:{ IOException -> 0x03c5 }
        L_0x03a1:
            if (r20 == 0) goto L_0x03a6;
        L_0x03a3:
            r20.close();	 Catch:{ IOException -> 0x03ca }
        L_0x03a6:
            r5 = "DecompressScheduler";
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "Decompress Thread : downalod = decompress finally end : ";
            r6 = r6.append(r7);
            r24 = java.lang.System.nanoTime();
            r0 = r24;
            r6 = r6.append(r0);
            r6 = r6.toString();
            com.lge.camera.util.CamLog.m3d(r5, r6);
            throw r4;
        L_0x03c5:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x03a1;
        L_0x03ca:
            r14 = move-exception;
            r14.printStackTrace();
            goto L_0x03a6;
        L_0x03cf:
            r4 = move-exception;
            r17 = r18;
            goto L_0x038d;
        L_0x03d3:
            r4 = move-exception;
            r20 = r21;
            r17 = r18;
            goto L_0x038d;
        L_0x03d9:
            r15 = move-exception;
            r17 = r18;
            goto L_0x032c;
        L_0x03de:
            r15 = move-exception;
            r20 = r21;
            r17 = r18;
            goto L_0x032c;
        L_0x03e5:
            r14 = move-exception;
            r17 = r18;
            goto L_0x02e7;
        L_0x03ea:
            r14 = move-exception;
            r20 = r21;
            r17 = r18;
            goto L_0x02e7;
        L_0x03f1:
            r14 = move-exception;
            goto L_0x0298;
        L_0x03f4:
            r14 = move-exception;
            r17 = r18;
            goto L_0x0298;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.Job.run():void");
        }
    }

    public class ResourceDecompressJob implements Runnable {
        private String mFilesDir;
        private int resId;

        public ResourceDecompressJob(String filesDir, int res) {
            this.resId = res;
            this.mFilesDir = filesDir;
        }

        /* JADX WARNING: Removed duplicated region for block: B:53:0x02a9 A:{SYNTHETIC, Splitter: B:53:0x02a9} */
        /* JADX WARNING: Removed duplicated region for block: B:56:0x02ae A:{Catch:{ IOException -> 0x02ea }} */
        /* JADX WARNING: Removed duplicated region for block: B:68:0x0311 A:{SYNTHETIC, Splitter: B:68:0x0311} */
        /* JADX WARNING: Removed duplicated region for block: B:71:0x0316 A:{Catch:{ IOException -> 0x0346 }} */
        /* JADX WARNING: Removed duplicated region for block: B:77:0x034e A:{SYNTHETIC, Splitter: B:77:0x034e} */
        /* JADX WARNING: Removed duplicated region for block: B:80:0x0353 A:{Catch:{ IOException -> 0x0386 }} */
        public void run() {
            /*
            r24 = this;
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r18 = r0;
            r18 = r18.mContext;
            r7 = r18.get();
            r7 = (android.content.Context) r7;
            if (r7 == 0) goto L_0x0212;
        L_0x0012:
            r0 = r24;
            r0 = r0.resId;
            r18 = r0;
            if (r18 == 0) goto L_0x0212;
        L_0x001a:
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "Decompress Thread : preloaded = start : ";
            r19 = r19.append(r20);
            r20 = java.lang.System.nanoTime();
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m5e(r18, r19);
            r10 = new java.io.File;
            r0 = r24;
            r0 = r0.mFilesDir;
            r18 = r0;
            r0 = r18;
            r10.<init>(r0);
            r15 = 0;
            r13 = 0;
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r0;
            r18 = r18.mWorkerSet;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = "PRELOADED_CONTENT";
            r18.add(r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r17 = new com.lge.camera.managers.ext.sticker.utils.ResourceLoader$RemoteRawResource;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = new com.lge.camera.managers.ext.sticker.utils.ResourceLoader;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18.<init>();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18.getClass();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = "com.lge.camera.sticker.res";
            r20 = "sticker";
            r17.<init>(r19, r20);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r0;
            r18 = r18.mContext;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r18.get();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = (android.content.Context) r18;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r15 = r17.getRawResource(r18);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            if (r15 != 0) goto L_0x00ac;
        L_0x007b:
            r18 = "DecompressScheduler";
            r19 = "Check com.lge.camera.sticker.res";
            com.lge.camera.util.CamLog.m11w(r18, r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r7.getResources();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r0 = r24;
            r0 = r0.resId;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = r0;
            r15 = r18.openRawResource(r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19.<init>();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r20 = "Decompress Thread : preloaded = start : ";
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r20 = java.lang.System.nanoTime();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = r19.toString();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
        L_0x00ac:
            r14 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18.<init>();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = r10.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r19 = "/PreloadedSticker.zip";
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = r18.toString();	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r0 = r18;
            r14.<init>(r0);	 Catch:{ FileNotFoundException -> 0x0395, IOException -> 0x0392, Exception -> 0x02ef }
            r18 = com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil.getRemotePreloadPackageVersion(r7);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = 10100000; // 0x9a1d20 float:1.4153114E-38 double:4.990063E-317;
            r0 = r18;
            r1 = r19;
            if (r0 < r1) goto L_0x0213;
        L_0x00d7:
            r18 = r15.available();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r18;
            r6 = new byte[r0];	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = 0;
            r19 = r15.available();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r18;
            r1 = r19;
            r15.read(r6, r0, r1);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r4 = new com.lge.camera.zipcrypto.AESWrapper;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r4.<init>();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r8 = r4.decrypt(r6);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = 0;
            r0 = r8.length;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r0;
            r0 = r18;
            r1 = r19;
            r14.write(r8, r0, r1);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = "DecompressScheduler";
            r19 = "Decompress Thread : use encryption";
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
        L_0x0108:
            r9 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18.<init>();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r10.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = "/PreloadedSticker.zip";
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r18.toString();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r18;
            r9.<init>(r0);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r9.exists();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            if (r18 == 0) goto L_0x027e;
        L_0x012c:
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19.<init>();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r20 = "threadrun; + ";
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r24;
            r0 = r0.resId;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r20 = r0;
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r20 = " copy complete";
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r19.toString();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            if (r18 == 0) goto L_0x0169;
        L_0x015c:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18.onPreloadDecompressStart();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
        L_0x0169:
            com.lge.camera.managers.ext.sticker.utils.UnZipUtil.Decompress(r9, r10);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r16 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18.<init>();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r10.getAbsolutePath();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = "/PreloadedSticker/preloaded.origin";
            r18 = r18.append(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r18.toString();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r16;
            r1 = r18;
            r0.<init>(r1);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r16.createNewFile();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r9.exists();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            if (r18 == 0) goto L_0x0198;
        L_0x0195:
            r9.delete();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
        L_0x0198:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r0 = r18;
            r0.decompressAlreadyDownloaded(r7);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            if (r18 == 0) goto L_0x01be;
        L_0x01af:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = 1;
            r18.onPreloadDecompressComplete(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
        L_0x01be:
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19.<init>();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r20 = "Decompress Thread : preloaded = decompress success : ";
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r20 = java.lang.System.nanoTime();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r19.append(r20);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r19.toString();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil.saveCurrentPreloadPackageVersion(r7);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
        L_0x01dd:
            if (r14 == 0) goto L_0x01e2;
        L_0x01df:
            r14.close();	 Catch:{ IOException -> 0x02de }
        L_0x01e2:
            if (r15 == 0) goto L_0x01e7;
        L_0x01e4:
            r15.close();	 Catch:{ IOException -> 0x02de }
        L_0x01e7:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r18 = r0;
            r18 = r18.mWorkerSet;
            r19 = "PRELOADED_CONTENT";
            r18.remove(r19);
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "Decompress Thread : preloaded = finally end : ";
            r19 = r19.append(r20);
            r20 = java.lang.System.nanoTime();
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m5e(r18, r19);
        L_0x0212:
            return;
        L_0x0213:
            r18 = r15.available();	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r0 = r18;
            r5 = new byte[r0];	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = 0;
            r0 = r5.length;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r0;
            r0 = r18;
            r1 = r19;
            r15.read(r5, r0, r1);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = 0;
            r0 = r5.length;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = r0;
            r0 = r18;
            r1 = r19;
            r14.write(r5, r0, r1);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = "DecompressScheduler";
            r19 = "Decompress Thread : no use encryption";
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            goto L_0x0108;
        L_0x023c:
            r11 = move-exception;
            r13 = r14;
        L_0x023e:
            r18 = "DecompressScheduler";
            r19 = "Decompress Thread : FileNotFoundException";
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ all -> 0x034b }
            r11.printStackTrace();	 Catch:{ all -> 0x034b }
            if (r13 == 0) goto L_0x024d;
        L_0x024a:
            r13.close();	 Catch:{ IOException -> 0x02e4 }
        L_0x024d:
            if (r15 == 0) goto L_0x0252;
        L_0x024f:
            r15.close();	 Catch:{ IOException -> 0x02e4 }
        L_0x0252:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r18 = r0;
            r18 = r18.mWorkerSet;
            r19 = "PRELOADED_CONTENT";
            r18.remove(r19);
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "Decompress Thread : preloaded = finally end : ";
            r19 = r19.append(r20);
            r20 = java.lang.System.nanoTime();
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m5e(r18, r19);
            goto L_0x0212;
        L_0x027e:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            if (r18 == 0) goto L_0x01dd;
        L_0x028a:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r18 = r0;
            r18 = r18.mCallback;	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            r19 = 0;
            r18.onPreloadDecompressComplete(r19);	 Catch:{ FileNotFoundException -> 0x023c, IOException -> 0x029b, Exception -> 0x038e, all -> 0x038b }
            goto L_0x01dd;
        L_0x029b:
            r11 = move-exception;
            r13 = r14;
        L_0x029d:
            r18 = "DecompressScheduler";
            r19 = "Decompress Thread : IOException";
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ all -> 0x034b }
            r11.printStackTrace();	 Catch:{ all -> 0x034b }
            if (r13 == 0) goto L_0x02ac;
        L_0x02a9:
            r13.close();	 Catch:{ IOException -> 0x02ea }
        L_0x02ac:
            if (r15 == 0) goto L_0x02b1;
        L_0x02ae:
            r15.close();	 Catch:{ IOException -> 0x02ea }
        L_0x02b1:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r18 = r0;
            r18 = r18.mWorkerSet;
            r19 = "PRELOADED_CONTENT";
            r18.remove(r19);
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "Decompress Thread : preloaded = finally end : ";
            r19 = r19.append(r20);
            r20 = java.lang.System.nanoTime();
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m5e(r18, r19);
            goto L_0x0212;
        L_0x02de:
            r11 = move-exception;
            r11.printStackTrace();
            goto L_0x01e7;
        L_0x02e4:
            r11 = move-exception;
            r11.printStackTrace();
            goto L_0x0252;
        L_0x02ea:
            r11 = move-exception;
            r11.printStackTrace();
            goto L_0x02b1;
        L_0x02ef:
            r12 = move-exception;
        L_0x02f0:
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;	 Catch:{ all -> 0x034b }
            r19.<init>();	 Catch:{ all -> 0x034b }
            r20 = "excpetion :  ";
            r19 = r19.append(r20);	 Catch:{ all -> 0x034b }
            r20 = r12.getMessage();	 Catch:{ all -> 0x034b }
            r19 = r19.append(r20);	 Catch:{ all -> 0x034b }
            r19 = r19.toString();	 Catch:{ all -> 0x034b }
            com.lge.camera.util.CamLog.m5e(r18, r19);	 Catch:{ all -> 0x034b }
            r12.printStackTrace();	 Catch:{ all -> 0x034b }
            if (r13 == 0) goto L_0x0314;
        L_0x0311:
            r13.close();	 Catch:{ IOException -> 0x0346 }
        L_0x0314:
            if (r15 == 0) goto L_0x0319;
        L_0x0316:
            r15.close();	 Catch:{ IOException -> 0x0346 }
        L_0x0319:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r18 = r0;
            r18 = r18.mWorkerSet;
            r19 = "PRELOADED_CONTENT";
            r18.remove(r19);
            r18 = "DecompressScheduler";
            r19 = new java.lang.StringBuilder;
            r19.<init>();
            r20 = "Decompress Thread : preloaded = finally end : ";
            r19 = r19.append(r20);
            r20 = java.lang.System.nanoTime();
            r19 = r19.append(r20);
            r19 = r19.toString();
            com.lge.camera.util.CamLog.m5e(r18, r19);
            goto L_0x0212;
        L_0x0346:
            r11 = move-exception;
            r11.printStackTrace();
            goto L_0x0319;
        L_0x034b:
            r18 = move-exception;
        L_0x034c:
            if (r13 == 0) goto L_0x0351;
        L_0x034e:
            r13.close();	 Catch:{ IOException -> 0x0386 }
        L_0x0351:
            if (r15 == 0) goto L_0x0356;
        L_0x0353:
            r15.close();	 Catch:{ IOException -> 0x0386 }
        L_0x0356:
            r0 = r24;
            r0 = com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.this;
            r19 = r0;
            r19 = r19.mWorkerSet;
            r20 = "PRELOADED_CONTENT";
            r19.remove(r20);
            r19 = "DecompressScheduler";
            r20 = new java.lang.StringBuilder;
            r20.<init>();
            r21 = "Decompress Thread : preloaded = finally end : ";
            r20 = r20.append(r21);
            r22 = java.lang.System.nanoTime();
            r0 = r20;
            r1 = r22;
            r20 = r0.append(r1);
            r20 = r20.toString();
            com.lge.camera.util.CamLog.m5e(r19, r20);
            throw r18;
        L_0x0386:
            r11 = move-exception;
            r11.printStackTrace();
            goto L_0x0356;
        L_0x038b:
            r18 = move-exception;
            r13 = r14;
            goto L_0x034c;
        L_0x038e:
            r12 = move-exception;
            r13 = r14;
            goto L_0x02f0;
        L_0x0392:
            r11 = move-exception;
            goto L_0x029d;
        L_0x0395:
            r11 = move-exception;
            goto L_0x023e;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.ResourceDecompressJob.run():void");
        }
    }

    private DecompressScheduler(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, Context ctx) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.mContext = new WeakReference(ctx);
    }

    public static synchronized DecompressScheduler getInstance(Context ctx) {
        DecompressScheduler decompressScheduler;
        synchronized (DecompressScheduler.class) {
            if (mInstance == null || mInstance.isShutdown()) {
                mInstance = new DecompressScheduler(2, 10, CameraConstants.TOAST_LENGTH_LONG, TimeUnit.SECONDS, new LinkedBlockingQueue(), ctx);
            }
            decompressScheduler = mInstance;
        }
        return decompressScheduler;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public boolean skipThisContent(File destFolder, String OriginFile) {
        File[] folders = destFolder.listFiles(new C13531());
        if (folders == null) {
            return false;
        }
        for (File folder : folders) {
            CamLog.m5e(TAG, "folder  + " + folder.getAbsolutePath());
            File[] origins = folder.listFiles(new C13542());
            if (origins != null && origins.length > 0 && origins[0] != null && origins[0].getName().startsWith(OriginFile)) {
                return true;
            }
        }
        return false;
    }

    public void excuteJob(Uri uri, String filesDir) {
        super.execute(new Job(uri, Long.toString(System.currentTimeMillis()), filesDir));
    }

    public void preloadedExcuteJob(String filesDir, int resid) {
        super.execute(new ResourceDecompressJob(filesDir, resid));
    }

    public boolean getIsDecompressing(String foldername) {
        if (this.mWorkerSet != null) {
            return this.mWorkerSet.contains(foldername);
        }
        return false;
    }

    public boolean getPrealoadPackageDecompressing() {
        if (this.mWorkerSet != null) {
            return this.mWorkerSet.contains(PRELOADED_WORKER_SET);
        }
        return false;
    }

    public void shutdown() {
        if (this.mContext != null) {
            this.mContext.clear();
            this.mContext = null;
        }
        if (mInstance != null) {
            mInstance = null;
        }
        if (this.mWorkerSet != null) {
            this.mWorkerSet.clear();
            this.mWorkerSet = null;
        }
        super.shutdown();
    }

    private void decompressAlreadyDownloaded(Context ctx) {
        File dnfolder = new File(Environment.getExternalStorageDirectory(), "LGWorld/Camera/Sticker");
        if (dnfolder.exists()) {
            File[] zips = dnfolder.listFiles(new C13553());
            if (zips != null && zips.length > 0) {
                ArrayList<File> flist = new ArrayList();
                for (File z : zips) {
                    flist.add(z);
                }
                flist.sort(new C13564());
                Iterator it = flist.iterator();
                while (it.hasNext()) {
                    unzipNeed(((File) it.next()).getAbsolutePath(), Long.toString(System.currentTimeMillis()), ctx);
                }
            }
        }
    }

    public static boolean needDecompressPreloadedContents(Context ctx) {
        File f = new File(ctx.getFilesDir().getAbsolutePath(), "PreloadedSticker");
        if (f.exists()) {
            if (!new File(f.getAbsolutePath() + "/preloaded.origin").exists()) {
                StickerUtil.deleteRecursive(f);
            } else if (!StickerPreloadPackageUtil.isPreloadPackageUpdated(ctx)) {
                return false;
            } else {
                StickerUtil.deleteRecursive(f);
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0197 A:{SYNTHETIC, Splitter: B:58:0x0197} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x019c A:{SYNTHETIC, Splitter: B:61:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0197 A:{SYNTHETIC, Splitter: B:58:0x0197} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x019c A:{SYNTHETIC, Splitter: B:61:0x019c} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x017a A:{SYNTHETIC, Splitter: B:45:0x017a} */
    /* JADX WARNING: Removed duplicated region for block: B:80:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x017f A:{SYNTHETIC, Splitter: B:48:0x017f} */
    private void unzipNeed(java.lang.String r22, java.lang.String r23, android.content.Context r24) {
        /*
        r21 = this;
        r12 = new java.io.File;
        r0 = r21;
        r4 = r0.mContext;
        r4 = r4.get();
        r4 = (android.content.Context) r4;
        r4 = r4.getFilesDir();
        r4 = r4.getAbsolutePath();
        r5 = "Sticker";
        r12.<init>(r4, r5);
        r16 = 0;
        r19 = 0;
        r4 = r12.exists();	 Catch:{ Exception -> 0x01b4 }
        if (r4 != 0) goto L_0x0026;
    L_0x0023:
        r12.mkdir();	 Catch:{ Exception -> 0x01b4 }
    L_0x0026:
        r4 = "DecompressScheduler";
        r5 = "Decompress Thread : unzipNeed";
        com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ Exception -> 0x01b4 }
        r11 = new java.io.File;	 Catch:{ Exception -> 0x01b4 }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01b4 }
        r4.<init>();	 Catch:{ Exception -> 0x01b4 }
        r0 = r23;
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x01b4 }
        r5 = ".zip";
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x01b4 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x01b4 }
        r11.<init>(r12, r4);	 Catch:{ Exception -> 0x01b4 }
        r13 = new java.io.File;	 Catch:{ Exception -> 0x01b4 }
        r0 = r22;
        r13.<init>(r0);	 Catch:{ Exception -> 0x01b4 }
        r4 = "DecompressScheduler";
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01b4 }
        r5.<init>();	 Catch:{ Exception -> 0x01b4 }
        r6 = "Decompress Thread : unzipNeed donwloadedZip = ";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r6 = r13.getAbsolutePath();	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x01b4 }
        com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ Exception -> 0x01b4 }
        r18 = new java.io.File;	 Catch:{ Exception -> 0x01b4 }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01b4 }
        r4.<init>();	 Catch:{ Exception -> 0x01b4 }
        r5 = r12.getAbsolutePath();	 Catch:{ Exception -> 0x01b4 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x01b4 }
        r5 = "/";
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x01b4 }
        r0 = r23;
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x01b4 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x01b4 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01b4 }
        r5.<init>();	 Catch:{ Exception -> 0x01b4 }
        r6 = r13.getName();	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r6 = ".origin";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x01b4 }
        r0 = r18;
        r0.<init>(r4, r5);	 Catch:{ Exception -> 0x01b4 }
        r4 = r18.getName();	 Catch:{ Exception -> 0x01b4 }
        r0 = r21;
        r4 = r0.skipThisContent(r12, r4);	 Catch:{ Exception -> 0x01b4 }
        if (r4 == 0) goto L_0x00e8;
    L_0x00b1:
        r4 = "DecompressScheduler";
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01b4 }
        r5.<init>();	 Catch:{ Exception -> 0x01b4 }
        r6 = "Decompress Thread : unzipNeed donwloadedZip = ";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r6 = r13.getAbsolutePath();	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r6 = " skip";
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x01b4 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x01b4 }
        com.lge.camera.util.CamLog.m3d(r4, r5);	 Catch:{ Exception -> 0x01b4 }
        if (r16 == 0) goto L_0x00d8;
    L_0x00d5:
        r16.close();	 Catch:{ IOException -> 0x00de }
    L_0x00d8:
        if (r19 == 0) goto L_0x00dd;
    L_0x00da:
        r19.close();	 Catch:{ IOException -> 0x00e3 }
    L_0x00dd:
        return;
    L_0x00de:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x00d8;
    L_0x00e3:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x00dd;
    L_0x00e8:
        r4 = r13.exists();	 Catch:{ Exception -> 0x01b4 }
        if (r4 == 0) goto L_0x0132;
    L_0x00ee:
        r17 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x01b4 }
        r0 = r17;
        r0.<init>(r13);	 Catch:{ Exception -> 0x01b4 }
        r20 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x01b6, all -> 0x01aa }
        r0 = r20;
        r0.<init>(r11);	 Catch:{ Exception -> 0x01b6, all -> 0x01aa }
        r4 = com.lge.camera.managers.ext.sticker.utils.StickerPreloadPackageUtil.getRemotePreloadPackageVersion(r24);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r5 = 10100000; // 0x9a1d20 float:1.4153114E-38 double:4.990063E-317;
        if (r4 < r5) goto L_0x0142;
    L_0x0105:
        r4 = r17.available();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r9 = new byte[r4];	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r4 = 0;
        r5 = r17.available();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r0 = r17;
        r0.read(r9, r4, r5);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r2 = new com.lge.camera.zipcrypto.AESWrapper;	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r2.<init>();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r10 = r2.decrypt(r9);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r4 = 0;
        r5 = r10.length;	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r0 = r20;
        r0.write(r10, r4, r5);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
    L_0x0125:
        com.lge.camera.managers.ext.sticker.utils.UnZipUtil.Decompress(r11, r12);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r18.createNewFile();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r11.delete();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r19 = r20;
        r16 = r17;
    L_0x0132:
        if (r16 == 0) goto L_0x0137;
    L_0x0134:
        r16.close();	 Catch:{ IOException -> 0x018a }
    L_0x0137:
        if (r19 == 0) goto L_0x00dd;
    L_0x0139:
        r19.close();	 Catch:{ IOException -> 0x013d }
        goto L_0x00dd;
    L_0x013d:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x00dd;
    L_0x0142:
        r3 = r17.getChannel();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r8 = r20.getChannel();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r4 = 0;
        r6 = r3.size();	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        r3.transferTo(r4, r6, r8);	 Catch:{ Exception -> 0x0154, all -> 0x01ae }
        goto L_0x0125;
    L_0x0154:
        r15 = move-exception;
        r19 = r20;
        r16 = r17;
    L_0x0159:
        r4 = "DecompressScheduler";
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0194 }
        r5.<init>();	 Catch:{ all -> 0x0194 }
        r6 = "excpetion :  ";
        r5 = r5.append(r6);	 Catch:{ all -> 0x0194 }
        r6 = r15.getMessage();	 Catch:{ all -> 0x0194 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0194 }
        r5 = r5.toString();	 Catch:{ all -> 0x0194 }
        com.lge.camera.util.CamLog.m5e(r4, r5);	 Catch:{ all -> 0x0194 }
        r15.printStackTrace();	 Catch:{ all -> 0x0194 }
        if (r16 == 0) goto L_0x017d;
    L_0x017a:
        r16.close();	 Catch:{ IOException -> 0x018f }
    L_0x017d:
        if (r19 == 0) goto L_0x00dd;
    L_0x017f:
        r19.close();	 Catch:{ IOException -> 0x0184 }
        goto L_0x00dd;
    L_0x0184:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x00dd;
    L_0x018a:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x0137;
    L_0x018f:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x017d;
    L_0x0194:
        r4 = move-exception;
    L_0x0195:
        if (r16 == 0) goto L_0x019a;
    L_0x0197:
        r16.close();	 Catch:{ IOException -> 0x01a0 }
    L_0x019a:
        if (r19 == 0) goto L_0x019f;
    L_0x019c:
        r19.close();	 Catch:{ IOException -> 0x01a5 }
    L_0x019f:
        throw r4;
    L_0x01a0:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x019a;
    L_0x01a5:
        r14 = move-exception;
        r14.printStackTrace();
        goto L_0x019f;
    L_0x01aa:
        r4 = move-exception;
        r16 = r17;
        goto L_0x0195;
    L_0x01ae:
        r4 = move-exception;
        r19 = r20;
        r16 = r17;
        goto L_0x0195;
    L_0x01b4:
        r15 = move-exception;
        goto L_0x0159;
    L_0x01b6:
        r15 = move-exception;
        r16 = r17;
        goto L_0x0159;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.managers.ext.sticker.contents.scheduler.DecompressScheduler.unzipNeed(java.lang.String, java.lang.String, android.content.Context):void");
    }
}
