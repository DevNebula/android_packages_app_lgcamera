package com.lge.camera.managers.ext.sticker.solutions;

import android.content.Context;
import com.lge.camera.managers.ext.sticker.solutions.arc.ArcStickerListLoader;

public class StickerListFactory {
    public IStickerListLoader createListLoader(Context ctx, int type) {
        if (type == 2) {
            return new ArcStickerListLoader(type);
        }
        if (type != 1) {
            return new ArcStickerListLoader(type);
        }
        return null;
    }
}
