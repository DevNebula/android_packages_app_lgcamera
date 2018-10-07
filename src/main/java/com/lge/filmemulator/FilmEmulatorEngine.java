package com.lge.filmemulator;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.lge.filmemulatorengine.FilmEmulatorRendererPreview;
import com.lge.filmemulatorengine.OnFilmEmulationViewListener;

public class FilmEmulatorEngine {
    public static final int ERRCODE_NO_SURFACE = 0;
    public static final int FLAG_REVERSE_REC_HOR = 2;
    public static final int FLAG_REVERSE_REC_VER = 1;
    private static final String TAG = "FilmEmulationRenderer";
    private FilmEmulatorRendererPreview mPreviewRenderer;

    public FilmEmulatorEngine(Surface surface, String[] luts, int level, boolean reverse, Drawable[] deleteBtnDrawable, Drawable background, OnFilmEmulationViewListener listener, float previewRatio, String[] filmNames, int downloadIndex) {
        Log.d(TAG, "Renderer start...");
        this.mPreviewRenderer = new FilmEmulatorRendererPreview(surface, luts, level, reverse, deleteBtnDrawable, background, listener, previewRatio, filmNames, downloadIndex);
        this.mPreviewRenderer.start();
        this.mPreviewRenderer.waitUntilReady();
    }

    public void release() {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().sendShutdown();
            try {
                this.mPreviewRenderer.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("join was interrupted preview renderer");
            }
        }
        this.mPreviewRenderer = null;
    }

    public FilmEmulatorRendererPreview getPreviewRenderer() {
        return this.mPreviewRenderer;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().sendSurfaceChanged(holder, width, height);
        }
    }

    public void prepareRecording(Surface recSurface, int width, int height, int idx, boolean isCinema, int flagReverse) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().sendPrepareRecording(recSurface, width, height, idx, isCinema, flagReverse);
        }
    }

    public void startRecoding() {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().sendStartRecording();
        }
    }

    public void stopRecoding(boolean onlyStopDrawing) {
        if (this.mPreviewRenderer == null) {
            return;
        }
        if (onlyStopDrawing) {
            this.mPreviewRenderer.stopRecordingDirect();
            return;
        }
        this.mPreviewRenderer.getHanlder().sendStopRecording();
        this.mPreviewRenderer.waitStopRecording();
    }

    public void setFPSshow(boolean isFPSshow) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.isFPSshow = isFPSshow;
        }
    }

    public void changeCamera() {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.changeCamera();
        }
    }

    public void changePictureSize(float ratio) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.changePictureSize(ratio);
        }
    }

    public void checkSelectedLut(int lut) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.setSelectedLUT(lut);
        }
    }

    public void setDegree(int degree, boolean updateFilterName) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.setDegree(degree, updateFilterName);
        }
    }

    public void pressFilmMenu(int lut) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.pressFilmMenu(lut);
        }
    }

    public void removeDownloadedLut(int lutNumber) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().removeDownloadedLUT(lutNumber);
        }
    }

    public void changeBackground(Drawable drawable) {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.changeBackground(drawable);
        }
    }

    public void resetLutView() {
        if (this.mPreviewRenderer != null) {
            this.mPreviewRenderer.getHanlder().resetLutView();
        }
    }
}
