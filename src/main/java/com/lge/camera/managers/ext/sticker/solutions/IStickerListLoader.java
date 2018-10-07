package com.lge.camera.managers.ext.sticker.solutions;

import android.content.Context;

public abstract class IStickerListLoader extends IStickerLoader {
    protected int mSolutionType = 3;

    public abstract int whatType();

    public IStickerListLoader(int solutionType) {
        this.mSolutionType = solutionType;
    }

    public IStickerListLoader(Context ctx) {
        this.mCtx = ctx;
    }
}
