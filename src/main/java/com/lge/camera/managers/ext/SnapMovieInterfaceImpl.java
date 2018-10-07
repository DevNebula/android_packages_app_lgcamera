package com.lge.camera.managers.ext;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.util.SparseArray;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.file.FileManager;
import com.lge.camera.managers.ManagerInterfaceImpl;

public abstract class SnapMovieInterfaceImpl extends ManagerInterfaceImpl {
    public static final String EXTRA_NAME_RESULT = "style_only_result";
    protected static final String EXTRA_NAME_VIDEO_EDITOR_PATH = "save_path";
    protected static final String EXTRA_NAME_VIDEO_EDITOR_SECURE = "is_secure_camera";
    protected static final String EXTRA_NAME_VIDEO_EDITOR_TYPE_PREVIEW = "preview_only";
    public static final String EXTRA_NAME_VIDEO_EDITOR_TYPE_STYLE = "style_only";
    protected static final int FILE_NAME_3_DIGIT = 3;
    protected static final String FILE_NAME_SNAP_PREFIX = "00";
    protected static final String FILE_NAME_SNAP_THUMB = "_thumb";
    public static final String RESULT_FAIL = "fail";
    public static final int SHOT_TIME_EXTRA_DAMPER = 20;
    public static final int SHOT_TIME_MAX = 60500;
    public static final int SHOT_TIME_MAX_NO_DAMPER = 60000;
    public static final int SHOT_TIME_PICTURE = 3000;
    public static final int SHOT_TIME_REC_2SEC = 2000;
    public static final int SHOT_TIME_REC_3SEC = 3000;
    public static final int SHOT_TIME_VIDEO_MIN = MultimediaProperties.getMinRecordingTime();
    public static final int SHOT_TIME_VIDEO_NONE = -1;
    public static final int SNAP_TIME_DAMPER = 500;
    public static final int STATUS_NONE = 0;
    public static final int STATUS_PREVIEW = 1;
    public static final int STATUS_SAVING = 3;
    public static final int STATUS_TAKING = 2;
    public static final boolean SUPPORT_ROTATION_LOCK = true;
    public static final int THUMB_RESIZE_RATE = 4;
    protected static final String VIDEO_EDITOR_CLASS = "com.lge.videostudio.VEMainActivity";
    protected static final String VIDEO_EDITOR_PACKAGE = "com.lge.videostudio";
    protected final int NOT_AVAILABLE_POS = -1;
    protected final int TIME_EXIT_EDIT = 5000;
    public SnapMovieInterface mGet = null;
    protected int mStatus = 0;

    protected class ShotItem {
        private int mDuration;
        private String mFilePath;

        public ShotItem(String filePath, int duration) {
            this.mFilePath = filePath;
            this.mDuration = duration;
        }

        public String getFilePath() {
            return this.mFilePath;
        }

        public String getFilePathThumb() {
            return this.mFilePath.replace(".mp4", "_thumb.jpg");
        }

        public int getDuration() {
            return this.mDuration;
        }

        public void setDuration(int duration) {
            this.mDuration = duration;
        }

        public Bitmap getThumbnail() {
            Bitmap bitmap = getThumbFromTempFile();
            if (bitmap == null) {
                getThumbFromRealFile();
            }
            return bitmap;
        }

        public int getType() {
            int result = 0;
            if (this.mFilePath == null) {
                return 0;
            }
            if (this.mFilePath.contains(".jpg")) {
                result = 0;
            } else if (this.mFilePath.contains(".mp4")) {
                result = 1;
            }
            return result;
        }

        private Bitmap getThumbFromRealFile() {
            int type = getType();
            if (type == 0) {
                return ThumbnailUtils.createImageThumbnail(this.mFilePath, 1);
            }
            if (type == 1) {
                return ThumbnailUtils.createVideoThumbnail(this.mFilePath, 1);
            }
            return null;
        }

        private Bitmap getThumbFromTempFile() {
            String tempPath = getFilePathThumb();
            if (FileManager.isFileExist(tempPath)) {
                return ThumbnailUtils.createImageThumbnail(tempPath, 1);
            }
            return null;
        }
    }

    protected class ShotItemList extends SparseArray<ShotItem> {
        protected ShotItemList() {
        }

        public int getLastIndex() {
            int size = size();
            return size > 0 ? size - 1 : 0;
        }

        public ShotItem getLastItem() {
            return (ShotItem) valueAt(getLastIndex());
        }

        public int getCurrentTime() {
            int result = 0;
            for (int i = 0; i < size(); i++) {
                result += ((ShotItem) valueAt(i)).getDuration();
            }
            return result;
        }

        public int getCurrentTime(int index) {
            int result = 0;
            if (index < size()) {
                for (int i = 0; i <= index; i++) {
                    result += ((ShotItem) valueAt(i)).getDuration();
                }
            }
            return result;
        }

        public int getMaxBitrate() {
            int result = 1;
            MediaMetadataRetriever meta = new MediaMetadataRetriever();
            for (int i = 0; i < size(); i++) {
                meta.setDataSource(((ShotItem) valueAt(i)).getFilePath());
                int bitrate = Integer.parseInt(meta.extractMetadata(20));
                if (result < bitrate) {
                    result = bitrate;
                }
            }
            return result;
        }
    }

    public SnapMovieInterfaceImpl(SnapMovieInterface snapMovieInterface) {
        super(snapMovieInterface);
        this.mGet = snapMovieInterface;
    }
}
