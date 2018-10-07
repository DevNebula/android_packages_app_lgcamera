package com.lge.camera.app.ext;

import com.lge.camera.components.MultiViewLayout;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;

public class SnapMovieFrame extends MultiViewFrame {
    public SnapMovieFrame(IMultiViewModule multiviewModule) {
        super(multiviewModule);
    }

    public SnapMovieFrame(int type, IMultiViewModule multiviewModule) {
        super(type, multiviewModule);
    }

    public SnapMovieFrame(int type, int layoutIndex, IMultiViewModule multiviewModule) {
        super(type, multiviewModule);
        this.mCurLayout = layoutIndex;
        this.mGet = multiviewModule;
    }

    protected void initVertices(int degree) {
        CamLog.m3d(CameraConstants.TAG, "initVertices - SnapMovie");
        if (this.mGet != null) {
            this.mMultiViewLayoutList = this.mGet.getMultiviewArrayList();
            if (this.mMultiViewLayoutList == null) {
                this.mMultiViewLayoutList = this.mGet.makeMultiviewLayout();
            }
            CamLog.m3d(CameraConstants.TAG, "-multiview- initVertices mCurPreviewMode = " + this.mCurLayout);
            ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).restoreCurTexture(degree);
            CamLog.m3d(CameraConstants.TAG, "getLayoutName = " + ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getLayoutName());
            this.mVerticesCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurVertex().clone();
            this.mTextureCoord = (float[]) ((MultiViewLayout) this.mMultiViewLayoutList.get(this.mCurLayout)).getCurTexCoord().clone();
            this.mVerticesPrev = (float[]) this.mVerticesCoord.clone();
            this.mVerticesTransition = (float[]) this.mVerticesCoord.clone();
            this.mTexturePrev = (float[]) this.mTextureCoord.clone();
            this.mTextureTransition = (float[]) this.mTextureCoord.clone();
        }
    }

    public void changeLayout(int toViewType, boolean animationOn) {
        super.changeLayout(toViewType, animationOn);
    }

    public void drawFrame(int[] texture, float[][] texMatrix) {
        super.drawFrame(texture, texMatrix);
    }
}
