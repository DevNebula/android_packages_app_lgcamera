package com.lge.camera.managers.ext.sticker.recentdb;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.lge.camera.managers.ext.sticker.AdapterInterface;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import com.lge.camera.managers.ext.sticker.solutions.IStickerLoader;
import com.lge.camera.managers.ext.sticker.solutions.IStickerLoader.OnLoadCompleteListener;
import com.lge.camera.managers.ext.sticker.solutions.StickerAdapter;
import com.lge.camera.managers.ext.sticker.solutions.StickerAdapter.OnStickerItemClickListener;
import com.lge.camera.util.CamLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecentUsedStickerAdapter extends StickerAdapter implements AdapterInterface, OnLoadCompleteListener {
    private static final String TAG = "RecentUsedStickerAdapter";
    private AtomicBoolean mIsLoaded;
    private String pendingConfig;

    class RecentLoader extends IStickerLoader {
        RecentLoader() {
        }

        public void load(Handler handle) {
            RecentUsedStickerAdapter.this.mIsLoaded.set(false);
            RecentDBHelper helper = RecentDBHelper.getInstance(this.mCtx);
            if (helper != null) {
                RecentUsedStickerAdapter.this.mStickerList.clear();
                RecentUsedStickerAdapter.this.mStickerList = helper.getRecentList();
            }
            RecentUsedStickerAdapter.this.mIsLoaded.set(true);
            RecentUsedStickerAdapter.this.onLoadComplete();
        }

        public boolean isListLoaded() {
            return RecentUsedStickerAdapter.this.mIsLoaded.get();
        }
    }

    public RecentUsedStickerAdapter(Context context, OnStickerItemClickListener listener, int height) {
        super(context, listener, 4, height);
        this.mIsLoaded = new AtomicBoolean(true);
        this.pendingConfig = "";
        this.mLoader = new RecentLoader();
    }

    public void load() {
        this.mLoader.load(this.mHandler);
    }

    public void load(String basePath) {
    }

    public void onLoadComplete() {
        CamLog.m3d(TAG, "pendingConfig = " + this.pendingConfig);
        if (!TextUtils.isEmpty(this.pendingConfig)) {
            if (this.mStickerList != null && this.mStickerList.size() > 0) {
                for (int i = 0; i < this.mStickerList.size(); i++) {
                    if (this.pendingConfig.equals(((StickerInformationDataClass) this.mStickerList.get(i)).configFile)) {
                        this.mSelectedPos = i;
                        break;
                    }
                }
            }
            this.pendingConfig = "";
        }
        CamLog.m5e(TAG, "mSelectedPos = " + this.mSelectedPos);
        notifyDataSetChanged();
    }

    public void setSelection(String configPath) {
        CamLog.m3d(TAG, "configPath = " + configPath);
        this.pendingConfig = configPath;
    }
}
