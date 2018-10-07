package com.lge.camera.app;

import android.opengl.Matrix;

public class ProjectionMatrix {
    private static float sAngle;
    private static boolean sMatrixReady;
    private static float[] sModelViewMatrix = null;
    private static float sPosX;
    private static float sPosY;
    private static float sScaleX = 1.0f;
    private static float sScaleY = 1.0f;

    private static void recomputeMatrix() {
        sModelViewMatrix = new float[16];
        Matrix.setIdentityM(sModelViewMatrix, 0);
        Matrix.translateM(sModelViewMatrix, 0, sPosX, sPosY, 0.0f);
        if (sAngle != 0.0f) {
            Matrix.rotateM(sModelViewMatrix, 0, sAngle, 0.0f, 0.0f, 1.0f);
        }
        Matrix.scaleM(sModelViewMatrix, 0, sScaleX, sScaleY, 1.0f);
        sMatrixReady = true;
    }

    public static float getScaleX() {
        return sScaleX;
    }

    public static float getScaleY() {
        return sScaleY;
    }

    public static void setScale(float scaleX, float scaleY) {
        sScaleX += scaleX;
        sScaleY += scaleY;
        sMatrixReady = false;
    }

    public static float getRotation() {
        return sAngle;
    }

    public static void setRotation(float angle) {
        while (angle >= 360.0f) {
            angle -= 360.0f;
        }
        while (angle <= -360.0f) {
            angle += 360.0f;
        }
        sAngle = angle;
        sMatrixReady = false;
    }

    public static float getPositionX() {
        return sPosX;
    }

    public static float getPositionY() {
        return sPosY;
    }

    public static void setPosition(float posX, float posY) {
        sPosX += posX;
        sPosY += posY;
        sMatrixReady = false;
    }

    public static float[] getModelViewMatrix() {
        if (!sMatrixReady) {
            recomputeMatrix();
        }
        return sModelViewMatrix;
    }

    public static void releaseMatrix() {
        sModelViewMatrix = null;
    }
}
