package com.lge.camera.device;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera.Face;
import android.util.Size;

public class FaceCommon {
    private int mId;
    private Point mLeftEye;
    private Point mMouth;
    private int mPreviewHeightOfactiveSize;
    private int mPreviewWidthOfactiveSize;
    private Rect mRect;
    private Point mRightEye;
    private int mScore;

    public FaceCommon(Face face) {
        copyCommonFace(face);
    }

    public FaceCommon(android.hardware.camera2.params.Face face) {
        copyCommonFace(face);
    }

    public FaceCommon(android.hardware.camera2.params.Face face, Rect activeSizeRect, Size pictureSize, float zoomRatio) {
        copyCommonFace(face, activeSizeRect, pictureSize, zoomRatio);
    }

    public void copyCommonFace(Face face) {
        this.mId = face.id;
        this.mLeftEye = face.leftEye;
        this.mRightEye = face.rightEye;
        this.mMouth = face.mouth;
        this.mRect = face.rect;
        this.mScore = face.score;
    }

    public void copyCommonFace(android.hardware.camera2.params.Face face) {
        this.mId = face.getId();
        this.mLeftEye = face.getLeftEyePosition();
        this.mRightEye = face.getRightEyePosition();
        this.mMouth = face.getMouthPosition();
        this.mRect = face.getBounds();
        this.mScore = face.getScore();
    }

    public void copyCommonFace(android.hardware.camera2.params.Face face, Rect activeSizeRect, Size pictureSize, float zoomRatio) {
        convertByActiveSizeRect(face, activeSizeRect, pictureSize, zoomRatio);
        this.mId = face.getId();
        this.mLeftEye = face.getLeftEyePosition();
        this.mRightEye = face.getRightEyePosition();
        this.mMouth = face.getMouthPosition();
        this.mScore = face.getScore();
    }

    private void convertByActiveSizeRect(android.hardware.camera2.params.Face face, Rect activeSizeRect, Size pictureSize, float zoomRatio) {
        Rect rect = face.getBounds();
        int activeSizeWidth = activeSizeRect.right - activeSizeRect.left;
        int activeSizeHeight = activeSizeRect.bottom - activeSizeRect.top;
        int activeSizeCenterX = activeSizeWidth / 2;
        int activeSizeCenterY = activeSizeHeight / 2;
        float currPictureRatio = ((float) pictureSize.getWidth()) / ((float) pictureSize.getHeight());
        float stretchGap;
        float centerGap;
        if (((float) activeSizeWidth) / ((float) activeSizeHeight) < currPictureRatio) {
            stretchGap = ((float) activeSizeHeight) / ((float) Math.round(((float) activeSizeWidth) / currPictureRatio));
            centerGap = (stretchGap - 1.0f) * ((float) activeSizeCenterY);
            rect.top = ((int) (((float) rect.top) * stretchGap)) - ((int) centerGap);
            rect.bottom = ((int) (((float) rect.bottom) * stretchGap)) - ((int) centerGap);
        } else {
            stretchGap = ((float) activeSizeWidth) / ((float) Math.round(((float) activeSizeHeight) * currPictureRatio));
            centerGap = (stretchGap - 1.0f) * ((float) activeSizeCenterX);
            rect.left = ((int) (((float) rect.left) * stretchGap)) - ((int) centerGap);
            rect.right = ((int) (((float) rect.right) * stretchGap)) - ((int) centerGap);
        }
        rect.left = Math.round((float) (rect.left - activeSizeCenterX));
        rect.top = Math.round((float) (rect.top - activeSizeCenterY));
        rect.right = Math.round((float) (rect.right - activeSizeCenterX));
        rect.bottom = Math.round((float) (rect.bottom - activeSizeCenterY));
        rect.scale(zoomRatio);
        this.mRect = rect;
        this.mPreviewWidthOfactiveSize = activeSizeWidth;
        this.mPreviewHeightOfactiveSize = activeSizeHeight;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public Point getLeftEye() {
        return this.mLeftEye;
    }

    public void setLeftEye(Point mLeftEye) {
        this.mLeftEye = mLeftEye;
    }

    public Point getRightEye() {
        return this.mRightEye;
    }

    public void setRightEye(Point mRightEye) {
        this.mRightEye = mRightEye;
    }

    public Point getMouth() {
        return this.mMouth;
    }

    public void setMouth(Point mMouth) {
        this.mMouth = mMouth;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    public int getScore() {
        return this.mScore;
    }

    public void setScore(int score) {
        this.mScore = score;
    }

    public Rect getRect() {
        return this.mRect;
    }

    public int getPreviewWidthOfactiveSize() {
        return this.mPreviewWidthOfactiveSize;
    }

    public int getPreviewHeightOfactiveSize() {
        return this.mPreviewHeightOfactiveSize;
    }
}
