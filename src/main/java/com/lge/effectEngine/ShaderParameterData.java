package com.lge.effectEngine;

import android.graphics.Color;

public class ShaderParameterData {
    public static final String TAG = "ShaderParameterData";
    private static ShaderParameterData mInstance = null;
    private float bValue = 0.114f;
    private int blurStrength = 10;
    private float[] blurValue = new float[1];
    private float brightness = 1.1f;
    private int frameShadowColor = Color.argb(255, 0, 0, 0);
    private float frameShadowDX = 15.0f;
    private float frameShadowDY = 15.0f;
    private float frameShadowsScale = 1.0f;
    private float frameShadowsSize = 30.0f;
    private float frameThickness = 2.0f;
    private float gValue = 0.587f;
    private float[] grayValue = new float[3];
    private float innerRadius = 0.4f;
    private boolean isFPSMode;
    private int[] marginLeft;
    private float[] normToWideRatio = new float[]{0.6f, 0.85f, 0.85f, 0.55f, 0.55f};
    private float outRadius = 0.8f;
    private float perspectiveAngle = 0.45f;
    private float[] perspectiveValue = new float[1];
    private float rValue = 0.299f;
    private float singleModeRatio = 0.55f;
    private float vignettingAlpha = 0.95f;
    private float[] vignettingValue = new float[4];

    private ShaderParameterData() {
        this.blurValue[0] = (float) this.blurStrength;
        this.perspectiveValue[0] = this.perspectiveAngle;
        this.vignettingValue[0] = this.innerRadius;
        this.vignettingValue[1] = this.outRadius;
        this.vignettingValue[2] = this.brightness;
        this.vignettingValue[3] = this.vignettingAlpha;
        this.grayValue[0] = this.rValue;
        this.grayValue[1] = this.gValue;
        this.grayValue[2] = this.bValue;
        this.marginLeft = new int[6];
    }

    public int[] getMarginLeft() {
        return this.marginLeft;
    }

    public void setMarginLeft(int[] margins) {
        this.marginLeft = (int[]) margins.clone();
    }

    public float getSingleModeRatio() {
        return this.singleModeRatio;
    }

    public void setSingleModeRatio(float singleModeRatio) {
        this.singleModeRatio = singleModeRatio;
    }

    public float[] getNormToWideRatio() {
        return this.normToWideRatio;
    }

    public void setNormToWideRatio(float[] normToWideRatio) {
        this.normToWideRatio = normToWideRatio;
    }

    public static ShaderParameterData getInstance() {
        if (mInstance == null) {
            mInstance = new ShaderParameterData();
        }
        return mInstance;
    }

    public void setBlurValue(float[] blurValue) {
        this.blurValue[0] = blurValue[0];
    }

    public void setPerspectiveValue(float[] perspectiveValue) {
        this.perspectiveValue[0] = perspectiveValue[0];
    }

    public void setVignettingValue(float[] vignettingValue) {
        this.vignettingValue[0] = vignettingValue[0];
        this.vignettingValue[1] = vignettingValue[1];
        this.vignettingValue[2] = vignettingValue[2];
        this.vignettingValue[3] = vignettingValue[3];
    }

    public void setGrayValue(float[] grayValue) {
        this.grayValue[0] = grayValue[0];
        this.grayValue[1] = grayValue[1];
        this.grayValue[2] = grayValue[2];
    }

    public float[] getBlurValue() {
        return this.blurValue;
    }

    public float[] getPerspectiveValue() {
        return this.perspectiveValue;
    }

    public float[] getVignettingValue() {
        return this.vignettingValue;
    }

    public float[] getGrayValue() {
        return this.grayValue;
    }

    public boolean isFPSMode() {
        return this.isFPSMode;
    }

    public void setFPSMode(boolean isFPSMode) {
        this.isFPSMode = isFPSMode;
    }

    public float getFrameThickness() {
        return this.frameThickness;
    }

    public void setFrameThickness(float frameThickness) {
        this.frameThickness = frameThickness;
    }

    public float getFrameShadowsScale() {
        return this.frameShadowsScale;
    }

    public void setFrameShadowsScale(float frameShadowsScale) {
        this.frameShadowsScale = frameShadowsScale;
    }

    public float getFrameShadowsSize() {
        return this.frameShadowsSize;
    }

    public void setFrameShadowsSize(float frameShadowsSize) {
        this.frameShadowsSize = frameShadowsSize;
    }

    public float getFrameShadowDX() {
        return this.frameShadowDX;
    }

    public void setFrameShadowDX(float frameShadowDX) {
        this.frameShadowDX = frameShadowDX;
    }

    public float getFrameShadowDY() {
        return this.frameShadowDY;
    }

    public void setFrameShadowDY(float frameShadowDY) {
        this.frameShadowDY = frameShadowDY;
    }

    public int getFrameShadowColor() {
        return this.frameShadowColor;
    }

    public void setFrameShadowColor(int frameShadowColor) {
        this.frameShadowColor = frameShadowColor;
    }
}
