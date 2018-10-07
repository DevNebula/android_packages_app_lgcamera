package com.lge.camera.managers.ext.sticker.contents.scheduler;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.managers.ext.sticker.utils.StickerUtil;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeleteContentScheduler extends ThreadPoolExecutor {
    private static final String TAG = "DeleteContentScheduler";
    private static DeleteContentScheduler mInstance;
    private WeakReference<Context> mContext;

    public class JobFromCamera implements Runnable {
        private String mFolderPath;

        /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DeleteContentScheduler$JobFromCamera$1 */
        class C13571 implements FilenameFilter {
            C13571() {
            }

            public boolean accept(File file, String s) {
                if (s.endsWith(".origin")) {
                    return true;
                }
                return false;
            }
        }

        public JobFromCamera(String folder) {
            this.mFolderPath = folder;
        }

        public void run() {
            if (!TextUtils.isEmpty(this.mFolderPath)) {
                File folder = new File(this.mFolderPath);
                File[] origins = folder.listFiles(new C13571());
                String originalFileName = "";
                if (origins != null && origins.length > 0) {
                    originalFileName = origins[0].getName();
                }
                CamLog.m5e(DeleteContentScheduler.TAG, "originalFileName  + " + originalFileName);
                StickerUtil.deleteRecursive(folder);
                if (!TextUtils.isEmpty(originalFileName)) {
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/LGWorld/Camera/Sticker", originalFileName.replace(".origin", ""));
                    CamLog.m5e(DeleteContentScheduler.TAG, "f =   + " + f.getAbsolutePath());
                    if (f != null && f.exists()) {
                        f.delete();
                    }
                }
            }
        }
    }

    public class JobFromReceiver implements Runnable {
        private Uri mUri;

        /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DeleteContentScheduler$JobFromReceiver$1 */
        class C13581 implements FileFilter {
            C13581() {
            }

            public boolean accept(File file) {
                return file.isDirectory();
            }
        }

        /* renamed from: com.lge.camera.managers.ext.sticker.contents.scheduler.DeleteContentScheduler$JobFromReceiver$2 */
        class C13592 implements FilenameFilter {
            C13592() {
            }

            public boolean accept(File file, String s) {
                return s.endsWith(".origin");
            }
        }

        public JobFromReceiver(Uri deletedUri) {
            this.mUri = deletedUri;
        }

        public void run() {
            if (this.mUri != null && DeleteContentScheduler.this.mContext != null) {
                List<String> segments = this.mUri.getPathSegments();
                Context ctx = (Context) DeleteContentScheduler.this.mContext.get();
                if (segments != null && segments.size() > 0 && ctx != null) {
                    String deletedName = (String) segments.get(segments.size() - 1);
                    CamLog.m3d(DeleteContentScheduler.TAG, "deletedName  + " + deletedName);
                    File[] folders = new File(ctx.getFilesDir().getAbsolutePath() + "/Sticker").listFiles(new C13581());
                    if (folders != null) {
                        for (File folder : folders) {
                            CamLog.m5e(DeleteContentScheduler.TAG, "folder  + " + folder.getAbsolutePath());
                            File[] origins = folder.listFiles(new C13592());
                            if (origins != null && origins.length > 0 && origins[0] != null && origins[0].getName().startsWith(deletedName)) {
                                StickerUtil.deleteRecursive(folder);
                            }
                        }
                    }
                }
            }
        }
    }

    public DeleteContentScheduler(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, Context ctx) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.mContext = new WeakReference(ctx);
    }

    public static synchronized DeleteContentScheduler getInstance(Context ctx) {
        DeleteContentScheduler deleteContentScheduler;
        synchronized (DeleteContentScheduler.class) {
            if (mInstance == null || mInstance.isShutdown()) {
                mInstance = new DeleteContentScheduler(2, 10, CameraConstants.TOAST_LENGTH_LONG, TimeUnit.SECONDS, new LinkedBlockingQueue(), ctx);
            }
            deleteContentScheduler = mInstance;
        }
        return deleteContentScheduler;
    }

    public void excuteJob(Uri uri) {
        super.execute(new JobFromReceiver(uri));
    }

    public void excuteJob(String folder) {
        super.execute(new JobFromCamera(folder));
    }

    public void shutdown() {
        if (this.mContext != null) {
            this.mContext.clear();
            this.mContext = null;
        }
        if (mInstance != null) {
            mInstance = null;
        }
        super.shutdown();
    }
}
