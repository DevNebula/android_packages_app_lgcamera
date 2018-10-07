package com.arcsoft.stickerlibrary.sticker;

import android.os.Environment;

public final class AppGlobalDef {
    public static final String ASSETS_TEMPLATE_FILENAME = "StickerTemplate.zip";
    public static final int FILE_WRITE_SYSTEM_CACHE_ESTIMATE = 512000;
    public static final int MAX_PREVIEW_COUNT_FOR_FPS = 20;
    public static final String THEMES_PATH = (WORK_DIR + "/StickerTemplate/");
    public static final String WORK_DIR = Environment.getExternalStorageDirectory().toString();
    public static final String settingPref = "settingPref";
    public static final String strKeyCopyAssetResouceCompleted = "copy_asset_resource_completed";
    public static final String strKeyFirstLaunch = "first_launch";
}
