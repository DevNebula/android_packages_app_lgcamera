package com.lge.camera.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.util.CamLog;
import com.lge.media.MediaEditor;
import com.lge.media.MediaEditor.OnInfoListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MediaEditorManager extends ManagerInterfaceImpl {
    public static final int MERGE_COMPLETED = 0;
    public static final int USER_CANCELED = -1003;
    private String mFilePath = null;
    private ArrayList<String> mFilePathList = null;
    protected OnInfoListener mInfoListener;
    protected MediaEditor mMediaEditor;
    ContentResolver mResolver = null;

    /* renamed from: com.lge.camera.managers.MediaEditorManager$1 */
    class C10961 implements OnInfoListener {
        C10961() {
        }

        public void onInfo(MediaEditor me, int what, int extra) {
            CamLog.m3d(CameraConstants.TAG, "mediaEditor onInfo :" + what + " , ext:" + extra);
            switch (what) {
                case 101:
                    CamLog.m3d(CameraConstants.TAG, "mediaEditor processing : " + extra);
                    return;
                case 102:
                    CamLog.m3d(CameraConstants.TAG, "mediaEditor process done. result : " + extra);
                    if (extra == 0) {
                        Uri uri = MediaEditorManager.this.insertDB(MediaEditorManager.this.mFilePath);
                        if (uri != null) {
                            MediaEditorManager.this.mGet.notifyNewMediaFromVideoTrim(uri);
                            return;
                        }
                        return;
                    }
                    MediaEditorManager.this.mGet.notifyNewMediaFromVideoTrim(null);
                    return;
                default:
                    CamLog.m3d(CameraConstants.TAG, "mediaEditor onInfo(DEFAULT, " + extra + ")");
                    MediaEditorManager.this.mGet.notifyNewMediaFromVideoTrim(null);
                    return;
            }
        }
    }

    public MediaEditorManager(ModuleInterface moduleInterface, String filePath, ArrayList<String> filePathList, int bitrate) {
        super(moduleInterface);
        CamLog.m3d(CameraConstants.TAG, "mediaEditor created");
        this.mFilePath = filePath;
        this.mFilePathList = filePathList;
        this.mResolver = this.mGet.getActivity().getContentResolver();
        setListeners();
    }

    private int initMediaEditor(String path, ArrayList<String> mFilePathList) {
        CamLog.m3d(CameraConstants.TAG, "mediaEditor init ");
        int ret = 0;
        try {
            if (this.mMediaEditor == null) {
                this.mMediaEditor = MediaEditor.getInstance();
            }
            this.mMediaEditor.reset();
            this.mMediaEditor.setInputFile((String) mFilePathList.get(0));
            this.mMediaEditor.getWidth(0);
            this.mMediaEditor.getHeight(0);
            this.mMediaEditor.setOnInfoListener(this.mInfoListener);
            Iterator it = mFilePathList.iterator();
            while (it.hasNext()) {
                ret = this.mMediaEditor.mergeAdd((String) it.next());
            }
            return ret;
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, " initMediaEditor Exception : " + e);
            return -1;
        }
    }

    public boolean mergeVideoClips() {
        CamLog.m3d(CameraConstants.TAG, "mediaEditor mergeVideoClips");
        if (this.mMediaEditor == null) {
            this.mMediaEditor = MediaEditor.getInstance();
        }
        int ret = initMediaEditor(this.mFilePath, this.mFilePathList);
        CamLog.m3d(CameraConstants.TAG, "mediaEditor init result : " + ret);
        if (ret == -1) {
            return false;
        }
        try {
            CamLog.m3d(CameraConstants.TAG, "MediaEditor mergeSave result : " + this.mMediaEditor.mergeSave(this.mFilePath));
        } catch (IllegalStateException e) {
            CamLog.m3d(CameraConstants.TAG, "IllegalStateException : " + e);
            e.printStackTrace();
        } catch (IOException e2) {
            CamLog.m3d(CameraConstants.TAG, "IOException : " + e2);
            e2.printStackTrace();
        }
        return true;
    }

    public boolean release() {
        if (this.mMediaEditor == null) {
            return false;
        }
        CamLog.m3d(CameraConstants.TAG, "mediaEditor release");
        this.mMediaEditor.release();
        this.mMediaEditor = null;
        return true;
    }

    private void setListeners() {
        CamLog.m3d(CameraConstants.TAG, "mediaEditor setListeners");
        this.mInfoListener = new C10961();
    }

    private Uri insertDB(String filename) {
        CamLog.m3d(CameraConstants.TAG, "mediaEditor insertDB filename : " + filename);
        File file = new File(filename);
        if (file == null || !file.exists()) {
            return null;
        }
        long size = file.length();
        MediaMetadataRetriever meta = new MediaMetadataRetriever();
        meta.setDataSource(filename);
        int duration = Integer.parseInt(meta.extractMetadata(9));
        int width = Integer.parseInt(meta.extractMetadata(18));
        int height = Integer.parseInt(meta.extractMetadata(19));
        meta.release();
        ContentValues content = new ContentValues();
        content.put("title", filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf(".")));
        content.put("_display_name", filename);
        content.put("mime_type", MultimediaProperties.VIDEO_MIME_TYPE);
        if (width > 0 && height > 0) {
            content.put("resolution", width + "x" + height);
        }
        content.put("_data", filename);
        content.put("_size", Long.valueOf(size));
        content.put("duration", Integer.valueOf(duration));
        content.put(CameraConstants.MODE_COLUMN, String.valueOf(10));
        if (this.mResolver != null) {
            Uri uri = this.mResolver.insert(Media.EXTERNAL_CONTENT_URI, content);
            CamLog.m3d(CameraConstants.TAG, "mediaEditor insert to DB done.");
            return uri;
        }
        CamLog.m5e(CameraConstants.TAG, "ContentResolver is null.");
        return null;
    }
}
