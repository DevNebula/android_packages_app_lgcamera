package com.lge.camera.managers.ext.sticker.solutions;

import android.content.Context;
import android.os.Handler;
import com.lge.camera.managers.ext.sticker.StickerInformationDataClass;
import java.util.ArrayList;

public abstract class IStickerLoader {
    protected String mBasePath;
    protected Context mCtx;
    protected ArrayList<StickerInformationDataClass> mList = new ArrayList();
    protected OnLoadCompleteListener mListener;

    public interface OnLoadCompleteListener {
        void onLoadComplete();
    }

    public abstract boolean isListLoaded();

    public abstract void load(Handler handler);

    public IStickerLoader(Context ctx) {
        this.mCtx = ctx;
    }

    public ArrayList<StickerInformationDataClass> getList() {
        return this.mList;
    }

    public void setBasePath(String path) {
        this.mBasePath = path;
    }

    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) {
        this.mListener = listener;
    }
}
