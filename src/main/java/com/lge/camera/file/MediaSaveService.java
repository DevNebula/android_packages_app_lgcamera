package com.lge.camera.file;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class MediaSaveService extends Service {
    private static final int SAVE_TASK_LIMIT = 1000;
    private final IBinder mBinder = new LocalBinder();
    private boolean mIsUnregisterQueueStatusListener = false;
    private OnQueueStatusListener mOnQueueStatusListener;
    private int mTaskNumber = 0;

    public interface OnQueueStatusListener {
        void onQueueStatus(int i);

        void onQueueStatus(boolean z);
    }

    public interface OnMediaSavedListener {
        void onMediaSaved(Uri uri);
    }

    public interface OnLocalSaveByTimeListener {
        Uri onLocalSave(String str);

        void onPostExecute(Uri uri);

        void onPreExecute();
    }

    public interface OnLocalSaveListener {
        Uri onLocalSave(String str, String str2);

        void onPostExecute(Uri uri);

        void onPreExecute();
    }

    private class ImageSaveTask extends AsyncTask<Void, Void, Uri> {
        private long dateTaken;
        private int degree;
        private String dir;
        private ExifInterface exif;
        private byte[] extraExif;
        private String fileName;
        private byte[] jpegData;
        private OnMediaSavedListener listener;
        private Location location;
        private ContentResolver resolver;

        public ImageSaveTask(byte[] jpegData, byte[] extraExif, ContentResolver resolver, String dir, String fileName, long dateTaken, Location location, int degree, ExifInterface exif, boolean isFlip, OnMediaSavedListener listener) {
            this.jpegData = jpegData;
            this.extraExif = extraExif;
            this.resolver = resolver;
            this.dir = dir;
            this.fileName = fileName;
            this.dateTaken = dateTaken;
            this.location = location;
            this.degree = degree;
            this.exif = exif;
            this.listener = listener;
        }

        protected void onPreExecute() {
        }

        protected Uri doInBackground(Void... v) {
            return FileManager.addJpegImage(this.jpegData, this.extraExif, this.resolver, this.dir, this.fileName, this.dateTaken, this.location, this.degree, this.exif, false);
        }

        protected void onPostExecute(Uri uri) {
            if (this.listener != null) {
                this.listener.onMediaSaved(uri);
            }
            MediaSaveService.this.mTaskNumber = MediaSaveService.this.mTaskNumber - 1;
            if (MediaSaveService.this.isQueueFull()) {
                MediaSaveService.this.onQueueFull();
            } else {
                MediaSaveService.this.onQueueAvailable();
            }
            if (MediaSaveService.this.mOnQueueStatusListener != null) {
                MediaSaveService.this.mOnQueueStatusListener.onQueueStatus(MediaSaveService.this.mTaskNumber);
            }
            if (MediaSaveService.this.mTaskNumber == 0 && MediaSaveService.this.mIsUnregisterQueueStatusListener) {
                MediaSaveService.this.mOnQueueStatusListener = null;
            }
            this.jpegData = null;
            this.location = null;
            this.resolver = null;
            this.listener = null;
        }
    }

    public class LocalBinder extends Binder {
        public MediaSaveService getService() {
            return MediaSaveService.this;
        }
    }

    protected class LocalSaveTask extends AsyncTask<Void, Void, Uri> {
        private String mDir = null;
        private String mFileName = null;
        private OnLocalSaveListener mLocalSaveListener = null;

        public LocalSaveTask(OnLocalSaveListener listener, String dir, String fileName) {
            this.mLocalSaveListener = listener;
            this.mDir = dir;
            this.mFileName = fileName;
        }

        protected void onPreExecute() {
            if (this.mLocalSaveListener != null) {
                this.mLocalSaveListener.onPreExecute();
            }
        }

        protected Uri doInBackground(Void... v) {
            if (this.mLocalSaveListener == null) {
                return null;
            }
            CamLog.m3d(CameraConstants.TAG, "-filename- doInBackground mDir = " + this.mDir + " mFileName = " + this.mFileName);
            return this.mLocalSaveListener.onLocalSave(this.mDir, this.mFileName);
        }

        protected void onPostExecute(Uri uri) {
            MediaSaveService.this.postExecuteOnLocalSave(uri);
            if (this.mLocalSaveListener != null) {
                this.mLocalSaveListener.onPostExecute(uri);
                this.mLocalSaveListener = null;
            }
        }
    }

    protected class LocalSaveTaskByTime extends AsyncTask<Void, Void, Uri> {
        private OnLocalSaveByTimeListener mListener = null;
        private String mMarkTime = null;

        public LocalSaveTaskByTime(OnLocalSaveByTimeListener listener, String markTime) {
            this.mListener = listener;
            this.mMarkTime = markTime;
        }

        protected void onPreExecute() {
            if (this.mListener != null) {
                this.mListener.onPreExecute();
            }
        }

        protected Uri doInBackground(Void... v) {
            if (this.mListener == null) {
                return null;
            }
            CamLog.m3d(CameraConstants.TAG, "-filename- doInBackground mMarkTime = " + this.mMarkTime);
            return this.mListener.onLocalSave(this.mMarkTime);
        }

        protected void onPostExecute(Uri uri) {
            MediaSaveService.this.postExecuteOnLocalSave(uri);
            if (this.mListener != null) {
                this.mListener.onPostExecute(uri);
                this.mListener = null;
            }
        }
    }

    private class VideoSaveTask extends AsyncTask<Void, Void, Uri> {
        private String dir;
        private long duration;
        private String fileName;
        private long fileSize;
        private OnMediaSavedListener listener;
        private Location location;
        private Context mContext;
        private String mUuid;
        private int purpose;
        private String resolution;
        private ContentResolver resolver;
        private int specialModeColumn;

        public VideoSaveTask(Context context, ContentResolver resolver, String dir, String fileName, String resolution, long duration, long fileSize, Location location, int purpose, OnMediaSavedListener listener, int specialModeColumn, String uuid) {
            this.resolver = resolver;
            this.dir = dir;
            this.fileName = fileName;
            this.resolution = resolution;
            this.duration = duration;
            this.fileSize = fileSize;
            this.location = location;
            this.purpose = purpose;
            this.listener = listener;
            this.mContext = context;
            this.specialModeColumn = specialModeColumn;
            this.mUuid = uuid;
        }

        protected void onPreExecute() {
        }

        protected Uri doInBackground(Void... v) {
            return FileManager.registerVideoUri(this.mContext, this.resolver, this.dir, this.fileName, this.resolution, this.duration, this.fileSize, this.location, this.purpose, this.specialModeColumn, this.mUuid);
        }

        protected void onPostExecute(Uri uri) {
            if (this.listener != null) {
                this.listener.onMediaSaved(uri);
            }
            if (MediaSaveService.this.mTaskNumber == 0 && MediaSaveService.this.mIsUnregisterQueueStatusListener) {
                MediaSaveService.this.mOnQueueStatusListener = null;
            }
            this.resolver = null;
            this.location = null;
            this.listener = null;
            this.mContext = null;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        return 1;
    }

    public void onDestroy() {
        this.mOnQueueStatusListener = null;
    }

    public void onCreate() {
        this.mTaskNumber = 0;
    }

    public void setQueueStatusListener(OnQueueStatusListener listener) {
        if (listener != null || this.mTaskNumber <= 0) {
            this.mOnQueueStatusListener = listener;
            this.mIsUnregisterQueueStatusListener = false;
            if (listener != null) {
                listener.onQueueStatus(isQueueFull());
                return;
            }
            return;
        }
        this.mIsUnregisterQueueStatusListener = true;
    }

    public void addImage(byte[] jpegData, byte[] extraExif, ContentResolver resolver, String dir, String fileName, long dateTaken, Location loc, int degree, ExifInterface exif, boolean isFlip, OnMediaSavedListener listener) {
        if (isQueueFull()) {
            Log.e(CameraConstants.TAG, "Cannot add image when the queue is full");
            return;
        }
        ImageSaveTask task = new ImageSaveTask(jpegData, extraExif, resolver, dir, fileName, dateTaken, loc == null ? null : new Location(loc), degree, exif, isFlip, listener);
        this.mTaskNumber++;
        if (isQueueFull()) {
            onQueueFull();
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void addVideo(Context context, ContentResolver resolver, String dir, String fileName, String resolution, long duration, long fileSize, Location location, int purpose, OnMediaSavedListener listener) {
        addVideo(context, resolver, dir, fileName, resolution, duration, fileSize, location, purpose, listener, 0, "");
    }

    public void addVideo(Context context, ContentResolver resolver, String dir, String fileName, String resolution, long duration, long fileSize, Location location, int purpose, OnMediaSavedListener listener, String uuid) {
        addVideo(context, resolver, dir, fileName, resolution, duration, fileSize, location, purpose, listener, 0, uuid);
    }

    public void addVideo(Context context, ContentResolver resolver, String dir, String fileName, String resolution, long duration, long fileSize, Location location, int purpose, OnMediaSavedListener listener, int specialModeColumn) {
        addVideo(context, resolver, dir, fileName, resolution, duration, fileSize, location, purpose, listener, specialModeColumn, "");
    }

    public void addVideo(Context context, ContentResolver resolver, String dir, String fileName, String resolution, long duration, long fileSize, Location location, int purpose, OnMediaSavedListener listener, int specialModeColumn, String uuid) {
        new VideoSaveTask(context, resolver, dir, fileName, resolution, duration, fileSize, location, purpose, listener, specialModeColumn, uuid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void processLocal(OnLocalSaveListener listener, String dir, String fileName) {
        CamLog.m3d(CameraConstants.TAG, "-filename- processLocal dir = " + dir + " fileName = " + fileName);
        LocalSaveTask task = new LocalSaveTask(listener, dir, fileName);
        this.mTaskNumber++;
        if (isQueueFull()) {
            onQueueFull();
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void processLocal(OnLocalSaveByTimeListener listener, String markTime) {
        CamLog.m3d(CameraConstants.TAG, "-filename- processLocal markTime = " + markTime);
        LocalSaveTaskByTime task = new LocalSaveTaskByTime(listener, markTime);
        this.mTaskNumber++;
        if (isQueueFull()) {
            onQueueFull();
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public int getQueueCount() {
        return this.mTaskNumber;
    }

    public boolean isQueueFull() {
        return this.mTaskNumber >= 1000;
    }

    private void onQueueFull() {
        if (this.mOnQueueStatusListener != null) {
            this.mOnQueueStatusListener.onQueueStatus(true);
        }
    }

    private void onQueueAvailable() {
        if (this.mOnQueueStatusListener != null) {
            this.mOnQueueStatusListener.onQueueStatus(false);
        }
    }

    public void postExecuteOnLocalSave(Uri uri) {
        this.mTaskNumber--;
        if (isQueueFull()) {
            onQueueFull();
        } else {
            onQueueAvailable();
        }
        if (this.mOnQueueStatusListener != null) {
            this.mOnQueueStatusListener.onQueueStatus(this.mTaskNumber);
        }
    }
}
