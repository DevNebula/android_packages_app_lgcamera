package com.lge.camera.components;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;
import com.arcsoft.stickerlibrary.utils.Constant;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraSwitchingAnimationRenderer implements Renderer {
    public static final int CAMERA_SWITCH_ANI = 1;
    public static final int CROPZOOM_TO_NORMAL_ANI = 4;
    public static final int CROPZOOM_TO_WIDE_ANI = 5;
    public static final int IN_AND_OUT_ANI = 8;
    public static final int NOT_DEFINED_ANI = -1;
    public static final int SWITCH_TO_NORMALCAM_ANI = 2;
    public static final int SWITCH_TO_NORMAL_LIGHTFRAME_FRONTCAM_ANI = 6;
    public static final int SWITCH_TO_WIDECAM_ANI = 3;
    public static final int SWITCH_TO_WIDE_LIGHTFRAME_FRONTCAM_ANI = 7;
    private final float CROP_NORMAL_TO_WIDE_RATIO;
    private final float CROP_WIDE_TO_NORMAL_RATIO;
    private final float NORMAL_TO_WIDE_CAM_RATIO;
    private final float WIDE_TO_NORMAL_CAM_RATIO;
    private final float animationDuration;
    private final float farDistance;
    private float mAccel;
    private float mAlpha;
    private float mAngle;
    private long mAnimationStartTime;
    private int mAnimationType;
    private CameraSwitchingAnimationCallback mCallback;
    private int mFlipDirection;
    private boolean mIsDrawFrame;
    private boolean mIsFastToSlowAccel;
    private boolean mIsGenTextureCalled;
    private boolean mIsUpdatedImage;
    private float mNearFar;
    private float mScale;
    private final float[] mTexture;
    private Bitmap mTextureBmp;
    private FloatBuffer mTextureBuffer;
    private final float[] mTextureMirror;
    private final float[] mTextureMirrorPort;
    private int[] mTextureName;
    private float[] mTextureTransition;
    private final float[] mToWideTextureForCamera;
    private final float[] mToWideTextureForCropZoom;
    private float mUnlimitAngle;
    private FloatBuffer mVertexBuffer;
    private final float[] mVertices;
    private final float nearFarStep;
    private final float rotationStep;

    public interface CameraSwitchingAnimationCallback {
        void drawFrame();
    }

    public CameraSwitchingAnimationRenderer() {
        this.mVertices = new float[]{-1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f};
        this.mTexture = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f};
        this.mTextureMirror = new float[]{1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
        this.mTextureMirrorPort = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
        this.mToWideTextureForCropZoom = new float[]{-0.4f, 1.4f, -0.4f, -0.4f, 1.4f, 1.4f, 1.4f, -0.4f};
        this.mToWideTextureForCamera = new float[]{-0.1f, 1.1f, -0.1f, -0.1f, 1.1f, 1.1f, 1.1f, -0.1f};
        this.mTextureTransition = null;
        this.mTextureName = new int[1];
        this.mTextureBmp = null;
        this.mIsGenTextureCalled = false;
        this.mIsDrawFrame = false;
        this.mAnimationType = 0;
        this.mAngle = 0.0f;
        this.mUnlimitAngle = 0.0f;
        this.mAccel = 0.0f;
        this.mAlpha = 1.0f;
        this.mNearFar = 0.0f;
        this.mAnimationStartTime = 0;
        this.mFlipDirection = 0;
        this.mScale = 1.0f;
        this.mIsFastToSlowAccel = false;
        this.mIsUpdatedImage = false;
        this.animationDuration = 300.0f;
        this.rotationStep = 0.6f;
        this.farDistance = 2.0f;
        this.nearFarStep = 0.013333334f;
        this.NORMAL_TO_WIDE_CAM_RATIO = 0.45f;
        this.WIDE_TO_NORMAL_CAM_RATIO = 1.5f;
        this.CROP_NORMAL_TO_WIDE_RATIO = 0.66f;
        this.CROP_WIDE_TO_NORMAL_RATIO = 1.3f;
        this.mVertexBuffer = getFloatBufferFromFloatArray(this.mVertices);
        this.mTextureBuffer = getFloatBufferFromTextureArray(this.mTexture);
        this.mTextureTransition = (float[]) this.mTexture.clone();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig egl) {
        CamLog.m3d(CameraConstants.TAG, "Renderer - onSurfaceCreated - gl = " + gl + ", EGLConfig = " + egl);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glHint(3152, 4353);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        CamLog.m3d(CameraConstants.TAG, "Renderer - onSurfaceChanged - gl = " + gl);
        gl.glViewport(0, 0, w, h);
    }

    private void calculateAngleAndNearfar(long time) {
        if (this.mAngle < 0.0f) {
            this.mAngle = 0.0f;
            this.mNearFar = 0.0f;
        } else if (this.mAngle >= 0.0f && this.mAngle < 90.0f) {
            this.mNearFar = -0.013333334f * ((float) time);
        } else if (this.mAngle < 90.0f || this.mAngle >= 180.0f) {
            this.mAngle = 180.0f;
            this.mNearFar = 0.0f;
        } else {
            this.mNearFar = -2.0f + (0.013333334f * (((float) time) - 150.0f));
        }
    }

    private void setScaleAccelValue() {
    }

    public void onDrawFrame(GL10 gl) {
        if (this.mIsDrawFrame) {
            if (!this.mIsUpdatedImage) {
                loadTexture(gl);
                this.mIsUpdatedImage = true;
            }
            long time = SystemClock.uptimeMillis() - this.mAnimationStartTime;
            this.mAngle = 0.6f * ((float) time);
            this.mUnlimitAngle = 0.6f * ((float) time);
            setScaleAccelValue();
            calculateAngleAndNearfar(time);
            if (this.mAnimationType == 1) {
                blendAlpha(gl, 0.5f);
                this.mTextureBuffer = getFloatBufferFromTextureArray(getSwitchCameraTexture());
                setupProjection(gl, this.mNearFar);
                setupRotation(gl);
            } else if (this.mAnimationType == 2 || this.mAnimationType == 3 || this.mAnimationType == 4 || this.mAnimationType == 5) {
                setupProjection(gl, 0.0f);
                blendAlpha(gl, 0.5f);
                if (this.mAnimationType == 2 || this.mAnimationType == 4) {
                    this.mTextureBuffer = getFloatBufferFromTextureArray(this.mTexture);
                    float scale = calculateScale(this.mAnimationType);
                    CamLog.m3d(CameraConstants.TAG, "mUnlimitAngle = " + this.mUnlimitAngle + " scale = " + scale);
                    setupScale(gl, scale);
                } else {
                    CamLog.m3d(CameraConstants.TAG, "mUnlimitAngle = " + this.mUnlimitAngle + " mAccel = " + this.mAccel);
                    for (int i = 0; i < this.mTexture.length; i++) {
                        if (this.mUnlimitAngle <= 360.0f) {
                            if (this.mAnimationType == 3) {
                                this.mTextureTransition[i] = this.mTextureTransition[i] + (((this.mToWideTextureForCamera[i] - this.mTexture[i]) * this.mAccel) / 50.0f);
                            } else if (this.mAnimationType == 5) {
                                this.mTextureTransition[i] = this.mTextureTransition[i] + (((this.mToWideTextureForCropZoom[i] - this.mTexture[i]) * this.mAccel) / 150.0f);
                            }
                        }
                    }
                    this.mTextureBuffer = getFloatBufferFromTextureArray(this.mTextureTransition);
                }
            } else if (this.mAnimationType == 6 || this.mAnimationType == 7) {
                CamLog.m3d(CameraConstants.TAG, "no anim, dummy");
            } else if (this.mAnimationType == 8) {
                setupProjection(gl, 0.0f);
                blendAlpha(gl, calculateAlpha());
                this.mTextureBuffer = getFloatBufferFromTextureArray(this.mTexture);
            }
            if (this.mIsDrawFrame) {
                draw(gl);
                if (this.mCallback != null) {
                    this.mCallback.drawFrame();
                    return;
                }
                return;
            }
            CamLog.m5e(CameraConstants.TAG, "mUnlimitAngle = " + this.mUnlimitAngle + ", mIsDrawFramem, return : " + this.mIsDrawFrame);
        }
    }

    private float[] getSwitchCameraTexture() {
        if (this.mAngle < 90.0f) {
            return this.mTexture;
        }
        if (this.mFlipDirection == 0 || this.mFlipDirection == 1) {
            return this.mTextureMirrorPort;
        }
        return this.mTextureMirror;
    }

    private float calculateAlpha() {
        this.mAlpha -= 0.004f;
        if (((double) this.mAlpha) < 0.5d) {
            this.mAlpha = 0.5f;
        }
        return this.mAlpha;
    }

    private float calculateScale(int aniType) {
        if (aniType == 2) {
            if (!this.mIsFastToSlowAccel) {
                this.mScale += 0.01f;
            } else if (this.mUnlimitAngle < 120.0f) {
                this.mScale += 0.016f;
            } else {
                this.mScale += 0.004f;
            }
            if (this.mScale > 1.5f) {
                this.mScale = 1.5f;
            }
        } else if (aniType == 3) {
            this.mScale -= 0.02f;
            if (this.mScale < 0.45f) {
                this.mScale = 0.45f;
            }
        } else if (aniType == 4) {
            this.mScale += 0.01f;
            if (this.mScale > 1.3f) {
                this.mScale = 1.3f;
            }
        } else if (aniType == 5) {
            this.mScale -= 0.01f;
            if (this.mScale < 0.66f) {
                this.mScale = 0.66f;
            }
        }
        return this.mScale;
    }

    private void setupRotation(GL10 gl) {
        if (this.mFlipDirection == 0) {
            gl.glRotatef(this.mAngle, 1.0f, 0.0f, 0.0f);
        } else if (this.mFlipDirection == 1) {
            gl.glRotatef(-this.mAngle, 1.0f, 0.0f, 0.0f);
        } else if (this.mFlipDirection == 3) {
            gl.glRotatef(-this.mAngle, 0.0f, 1.0f, 0.0f);
        } else {
            gl.glRotatef(this.mAngle, 0.0f, 1.0f, 0.0f);
        }
    }

    private void setupProjection(GL10 gl, float nearFar) {
        gl.glClear(16640);
        gl.glMatrixMode(5889);
        gl.glLoadIdentity();
        if (this.mAnimationType == 1) {
            gl.glFrustumf(-1.0f, 1.0f, -1.0f, 1.0f, 18.0f, 20.0f);
            GLU.gluLookAt(gl, 0.0f, 0.0f, 18.0f - nearFar, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        } else {
            gl.glFrustumf(-1.0f, 1.0f, -1.0f, 1.0f, 5.0f, 20.0f);
            GLU.gluLookAt(gl, 0.0f, 0.0f, 5.0f - nearFar, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        }
        gl.glMatrixMode(Constant.PHOTO_EFFECT_ID_GRAY);
        gl.glLoadIdentity();
    }

    public void blendAlpha(GL10 gl, float alpha) {
        gl.glEnable(3042);
        gl.glBlendFunc(1, 771);
        gl.glColor4f(alpha, alpha, alpha, alpha);
    }

    public void setupScale(GL10 gl, float scale) {
        gl.glScalef(scale, scale, 1.0f);
    }

    public void draw(GL10 gl) {
        gl.glEnable(3553);
        gl.glBindTexture(3553, this.mTextureName[0]);
        gl.glEnableClientState(32884);
        gl.glEnableClientState(32888);
        gl.glFrontFace(2304);
        gl.glVertexPointer(3, 5126, 0, this.mVertexBuffer);
        gl.glTexCoordPointer(2, 5126, 0, this.mTextureBuffer);
        gl.glDrawArrays(5, 0, this.mVertices.length / 3);
        gl.glDisableClientState(32888);
        gl.glDisableClientState(32884);
        gl.glDisable(3553);
    }

    public void setTextureBitmap(Bitmap bmp) {
        CamLog.m3d(CameraConstants.TAG, "setTextureBitmap bmp = " + bmp);
        this.mTextureBmp = bmp;
        this.mIsUpdatedImage = false;
    }

    public void setFlipDirection(int direction) {
        this.mFlipDirection = direction;
    }

    public synchronized void loadTexture(GL10 gl) {
        CamLog.m3d(CameraConstants.TAG, "loadTexture");
        this.mAnimationStartTime = SystemClock.uptimeMillis();
        if (this.mTextureBmp == null || this.mTextureBmp.isRecycled()) {
            CamLog.m5e(CameraConstants.TAG, "Texture Bitmap is not set");
        } else {
            if (!this.mIsGenTextureCalled) {
                gl.glGenTextures(this.mTextureName.length, this.mTextureName, 0);
                this.mIsGenTextureCalled = true;
                CamLog.m3d(CameraConstants.TAG, "glGenTextures = " + this.mTextureName[0]);
            }
            gl.glBindTexture(3553, this.mTextureName[0]);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLES20.glTexParameteri(3553, 10241, 9729);
            gl.glTexParameterf(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
            if (!(this.mTextureBmp == null || this.mTextureBmp.isRecycled())) {
                GLUtils.texImage2D(3553, 0, this.mTextureBmp, 0);
            }
            if (this.mTextureBmp != null) {
                this.mTextureBmp.recycle();
                this.mTextureBmp = null;
            }
        }
    }

    public void startAnimation(int animationType) {
        CamLog.m3d(CameraConstants.TAG, "startAnimation animationType = " + animationType);
        this.mIsDrawFrame = true;
        this.mAnimationType = animationType;
        this.mAnimationStartTime = SystemClock.uptimeMillis();
        this.mAngle = 0.0f;
        this.mAlpha = animationType == 1 ? 0.5f : 1.0f;
        this.mScale = 1.0f;
        this.mAccel = 2.0f;
        this.mUnlimitAngle = 0.0f;
        this.mTextureTransition = (float[]) this.mTexture.clone();
    }

    public void setAnimtationCallback(CameraSwitchingAnimationCallback callback) {
        this.mCallback = callback;
    }

    FloatBuffer getFloatBufferFromFloatArray(float[] array) {
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(array.length * 4);
        tempBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = tempBuffer.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    FloatBuffer getFloatBufferFromTextureArray(float[] texture) {
        ByteBuffer tbb = ByteBuffer.allocateDirect(texture.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = tbb.asFloatBuffer();
        buffer.put(texture);
        buffer.position(0);
        return buffer;
    }

    public void destroy() {
        this.mVertexBuffer = null;
        this.mTextureBuffer = null;
        this.mTextureName = null;
        this.mTextureBmp = null;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        CamLog.m3d(CameraConstants.TAG, "CameraSwitchingAnimationRenderer - finalize()");
    }

    public boolean isAvailabeDrawFrame() {
        return this.mIsDrawFrame;
    }

    public void stopAnimation() {
        this.mIsDrawFrame = false;
    }

    public int getAnimationType() {
        return this.mAnimationType;
    }

    public void resetAnimType() {
        this.mAnimationType = 0;
    }

    public boolean checkCameraSwitchingAnim(boolean isOpticZoomSupported) {
        if (this.mAnimationType != 1) {
            return !isOpticZoomSupported && this.mAnimationType > 1 && this.mAnimationType < 4;
        } else {
            return true;
        }
    }
}
