package com.lge.camera.managers.ext.sticker.solutions;

import android.content.Context;
import com.lge.camera.managers.ext.sticker.solutions.arc.ArcSolution;

public class StickerSolutionFactory {
    public SolutionBase create(Context ctx, int type) {
        if (type == 2) {
            return new ArcSolution(ctx);
        }
        if (type == 1) {
            return new ArcSolution(ctx);
        }
        return new ArcSolution(ctx);
    }
}
