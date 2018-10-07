package com.lge.camera.managers.ext.sticker.solutions.arc;

import android.content.Context;
import android.os.Handler;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import com.lge.camera.managers.ext.sticker.solutions.IStickerListLoader;
import com.lge.camera.util.CamLog;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ArcStickerListLoader extends IStickerListLoader {
    private static final String ICON_FILE = "icon.png";
    private static final String SETTING_CONFIG_FILE = "setting.xml";
    private static final String TAG = "ArcStickerListLoader";
    private boolean isListLoaded = false;

    public ArcStickerListLoader(int solutionType) {
        super(solutionType);
    }

    public ArcStickerListLoader(Context ctx) {
        super(ctx);
    }

    public void load(final Handler handle) {
        new Thread(new Runnable() {

            /* renamed from: com.lge.camera.managers.ext.sticker.solutions.arc.ArcStickerListLoader$1$1 */
            class C13621 implements FileFilter {
                C13621() {
                }

                public boolean accept(File file) {
                    return file.isDirectory();
                }
            }

            /* renamed from: com.lge.camera.managers.ext.sticker.solutions.arc.ArcStickerListLoader$1$2 */
            class C13632 implements Comparator<File> {
                C13632() {
                }

                public int compare(File o1, File o2) {
                    return o1.compareTo(o2);
                }
            }

            /* renamed from: com.lge.camera.managers.ext.sticker.solutions.arc.ArcStickerListLoader$1$3 */
            class C13643 implements Runnable {
                C13643() {
                }

                public void run() {
                    if (ArcStickerListLoader.this.mListener != null) {
                        ArcStickerListLoader.this.mListener.onLoadComplete();
                    }
                }
            }

            public void run() {
                ArcStickerListLoader.this.isListLoaded = false;
                if (ArcStickerListLoader.this.mList != null) {
                    ArcStickerListLoader.this.mList.clear();
                    ArcStickerListLoader.this.mList = null;
                }
                ArcStickerListLoader.this.mList = new ArrayList();
                CamLog.m3d(ArcStickerListLoader.TAG, "mBasePath = " + ArcStickerListLoader.this.mBasePath);
                File folder = new File(ArcStickerListLoader.this.mBasePath);
                if (folder.exists()) {
                    File[] folderlist = folder.listFiles(new C13621());
                    if (folderlist != null) {
                        Arrays.sort(folderlist, new C13632());
                        for (File eachFolder : folderlist) {
                            File icon = new File(eachFolder, ArcStickerListLoader.ICON_FILE);
                            File config = new File(eachFolder, ArcStickerListLoader.SETTING_CONFIG_FILE);
                            if (config != null && config.exists() && icon != null && icon.exists()) {
                                StickerInformationDataClass sid = new StickerInformationDataClass();
                                sid.sticker_name = eachFolder.getName();
                                sid.sticker_data_position = 0;
                                sid.icon_path = icon.getAbsolutePath();
                                sid.solution_type = ArcStickerListLoader.this.whatType();
                                sid.sticker_id = eachFolder.getName();
                                sid.configFile = config.getAbsolutePath();
                                CamLog.m3d(ArcStickerListLoader.TAG, "sid = " + sid.toString());
                                if (ArcStickerListLoader.this.mList != null) {
                                    ArcStickerListLoader.this.mList.add(sid);
                                } else {
                                    CamLog.m3d(ArcStickerListLoader.TAG, "maybe another thread run");
                                    return;
                                }
                            }
                        }
                        ArcStickerListLoader.this.isListLoaded = true;
                        CamLog.m3d(ArcStickerListLoader.TAG, "StickerlistLoadEnd");
                        handle.post(new C13643());
                    }
                }
            }
        }).start();
    }

    public boolean isListLoaded() {
        return this.isListLoaded;
    }

    public int whatType() {
        return this.mSolutionType;
    }
}
