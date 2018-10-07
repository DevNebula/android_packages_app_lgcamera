package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.arcsoft.stickerlibrary.utils.Constant;
import com.lge.camera.app.ProjectionMatrix;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class MultiViewFrameBase {
    public static final int FRAME_TYPE_COLLAGE = 4;
    public static final int FRAME_TYPE_DISPLAY = 1;
    public static final int FRAME_TYPE_RECORD = 2;
    public static final int GL_CAM1_TEXNUM = 33984;
    public static final int GL_CAM2_TEXNUM = 33985;
    public static final int GL_CAPTURE1_TEXNUM = 33991;
    public static final int GL_COLLAGE1_TEXNUM = 33987;
    public static final int GL_EXT_CAM_TEXNUM = 33986;
    public static final int MULTI_LAYOUT_QUAD = 4;
    public static final int MULTI_LAYOUT_SINGLE = 0;
    public static final int MULTI_LAYOUT_SPLIT = 1;
    public static final int MULTI_LAYOUT_TRIPLE01 = 2;
    public static final int MULTI_LAYOUT_TRIPLE02 = 3;
    public static final int PREVIEW_TEX_NUM = 3;
    protected static final int SNAP_MULTI_PREVIEW = 12;
    protected static final int SNAP_REAR_PREVIEW = 13;
    protected static int sCapturedIndex = -1;
    public static boolean sIsGenTex = false;
    protected final int ANIMATION_DUAL_CAM_CHANGE_DURATION;
    protected final int ANIMATION_MULTI_CAM_CHANGE_DURATION;
    protected final int ANIMATION_PIC_CHANGE_DURATION;
    protected final int FLOAT_SIZE_BYTES;
    protected float[] mColTexMatrix;
    protected int mCurLayout;
    protected int mDuration;
    protected int mFrameType;
    protected final String mFss;
    protected IMultiViewModule mGet;
    protected boolean mIsShowingAnimation;
    protected float[] mMVPMatrix;
    protected int mProgram;
    protected float[][] mTexMatrix;
    protected float[] mTextureCoord;
    protected float[] mTexturePrev;
    protected float[] mTextureTransition;
    protected float[] mTmpMatrixImg;
    protected float[] mVerticesCoord;
    protected float[] mVerticesPrev;
    protected float[] mVerticesTransition;
    protected final String mVss;
    protected int maPositionLoc;
    protected int maTexCoordsLoc;
    protected int muCameraIdLoc;
    protected int muMVPmatrixLoc;
    protected int muTexMatrixFrontLoc;
    protected int muTexMatrixImgLoc;
    protected int muTexMatrixRearLoc;
    protected int muTextureCameraLoc;
    protected int muTextureImageLoc;
    protected int muTextureTypeLoc;
    protected FloatBuffer pTexCoord;
    protected FloatBuffer pVertex;

    public MultiViewFrameBase() {
        this.mFrameType = 1;
        this.mVss = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrixRear;\nuniform mat4 uTexMatrixFront;\nuniform mat4 uTexMatrixImg;\nattribute vec4 aPosition;\nattribute vec4 aTexCoord;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordImg;\nvarying vec2 vTexCoordExt;\nvoid main() {\n  vTexCoordRear = (uTexMatrixRear * aTexCoord).xy;\n  vTexCoordFront = (uTexMatrixFront * aTexCoord).xy;\n  vTexCoordImg = (uTexMatrixImg * aTexCoord).xy;\n  vTexCoordExt = aTexCoord.xy;\n  gl_Position = uMVPMatrix * aPosition;\n}";
        this.mFss = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES uTextureCamera;\nuniform sampler2D uTextureImage;\nuniform float uTextureType;\nuniform float uCameraId;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordExt;\nvarying vec2 vTexCoordImg;\nvoid main() {\n  if (uTextureType == 1.0) {\n    gl_FragColor = texture2D(uTextureImage, vTexCoordImg);\n  } else {\n    if (uCameraId == 1.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordFront);\n    } else if (uCameraId == 0.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordRear);\n    } else {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordExt);\n    }\n  }}";
        this.FLOAT_SIZE_BYTES = 4;
        this.mCurLayout = 2;
        this.mIsShowingAnimation = false;
        this.ANIMATION_MULTI_CAM_CHANGE_DURATION = 10;
        this.ANIMATION_DUAL_CAM_CHANGE_DURATION = 100;
        this.ANIMATION_PIC_CHANGE_DURATION = 50;
        this.mColTexMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mTmpMatrixImg = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mFrameType = 1;
        CamLog.m3d(CameraConstants.TAG, "-rec- mFrameType = " + this.mFrameType);
    }

    public MultiViewFrameBase(int frameType) {
        this.mFrameType = 1;
        this.mVss = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrixRear;\nuniform mat4 uTexMatrixFront;\nuniform mat4 uTexMatrixImg;\nattribute vec4 aPosition;\nattribute vec4 aTexCoord;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordImg;\nvarying vec2 vTexCoordExt;\nvoid main() {\n  vTexCoordRear = (uTexMatrixRear * aTexCoord).xy;\n  vTexCoordFront = (uTexMatrixFront * aTexCoord).xy;\n  vTexCoordImg = (uTexMatrixImg * aTexCoord).xy;\n  vTexCoordExt = aTexCoord.xy;\n  gl_Position = uMVPMatrix * aPosition;\n}";
        this.mFss = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES uTextureCamera;\nuniform sampler2D uTextureImage;\nuniform float uTextureType;\nuniform float uCameraId;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordExt;\nvarying vec2 vTexCoordImg;\nvoid main() {\n  if (uTextureType == 1.0) {\n    gl_FragColor = texture2D(uTextureImage, vTexCoordImg);\n  } else {\n    if (uCameraId == 1.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordFront);\n    } else if (uCameraId == 0.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordRear);\n    } else {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordExt);\n    }\n  }}";
        this.FLOAT_SIZE_BYTES = 4;
        this.mCurLayout = 2;
        this.mIsShowingAnimation = false;
        this.ANIMATION_MULTI_CAM_CHANGE_DURATION = 10;
        this.ANIMATION_DUAL_CAM_CHANGE_DURATION = 100;
        this.ANIMATION_PIC_CHANGE_DURATION = 50;
        this.mColTexMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mTmpMatrixImg = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mFrameType = frameType;
        CamLog.m3d(CameraConstants.TAG, "-rec- mFrameType = " + this.mFrameType);
    }

    public void init(int layoutType, int degree, boolean singleVeiwRecording) {
    }

    public void init(int layoutType, int[] inputFileType, ArrayList<Integer> arrayList, int degree, boolean singleViewRecording) {
    }

    public void init(int degree) {
        initVertices(degree);
        setVertices();
        this.mProgram = buildProgram("uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrixRear;\nuniform mat4 uTexMatrixFront;\nuniform mat4 uTexMatrixImg;\nattribute vec4 aPosition;\nattribute vec4 aTexCoord;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordImg;\nvarying vec2 vTexCoordExt;\nvoid main() {\n  vTexCoordRear = (uTexMatrixRear * aTexCoord).xy;\n  vTexCoordFront = (uTexMatrixFront * aTexCoord).xy;\n  vTexCoordImg = (uTexMatrixImg * aTexCoord).xy;\n  vTexCoordExt = aTexCoord.xy;\n  gl_Position = uMVPMatrix * aPosition;\n}", "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES uTextureCamera;\nuniform sampler2D uTextureImage;\nuniform float uTextureType;\nuniform float uCameraId;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordExt;\nvarying vec2 vTexCoordImg;\nvoid main() {\n  if (uTextureType == 1.0) {\n    gl_FragColor = texture2D(uTextureImage, vTexCoordImg);\n  } else {\n    if (uCameraId == 1.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordFront);\n    } else if (uCameraId == 0.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordRear);\n    } else {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordExt);\n    }\n  }}");
        this.maPositionLoc = GLES20.glGetAttribLocation(this.mProgram, "aPosition");
        checkGlError("glGetAttribLocation - aPosition");
        this.maTexCoordsLoc = GLES20.glGetAttribLocation(this.mProgram, "aTexCoord");
        checkGlError("glGetAttribLocation - aTexCoord");
        this.muTextureTypeLoc = GLES20.glGetUniformLocation(this.mProgram, "uTextureType");
        checkGlError("glGetUniformLocation - uTextureType");
        this.muCameraIdLoc = GLES20.glGetUniformLocation(this.mProgram, "uCameraId");
        checkGlError("glGetUniformLocation - uCameraId");
        this.muTextureCameraLoc = GLES20.glGetUniformLocation(this.mProgram, "uTextureCamera");
        checkGlError("glGetUniformLocation - uTextureCamera");
        this.muTextureImageLoc = GLES20.glGetUniformLocation(this.mProgram, "uTextureImage");
        checkGlError("glGetUniformLocation - uTextureImage");
        this.muMVPmatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation - uMVPMatrix");
        this.muTexMatrixRearLoc = GLES20.glGetUniformLocation(this.mProgram, "uTexMatrixRear");
        checkGlError("glGetUniformLocation - uTexMatrixRear");
        this.muTexMatrixFrontLoc = GLES20.glGetUniformLocation(this.mProgram, "uTexMatrixFront");
        checkGlError("glGetUniformLocation - uTexMatrixFront");
        this.muTexMatrixImgLoc = GLES20.glGetUniformLocation(this.mProgram, "uTexMatrixImg");
        checkGlError("glGetUniformLocation - uTexMatrixImg");
        initDrawFrame();
    }

    protected void initVertices(int degree) {
    }

    protected void setVertices() {
        CamLog.m3d(CameraConstants.TAG, "-multiview- setVertices");
        this.pVertex = ByteBuffer.allocateDirect(this.mVerticesCoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pVertex.put(this.mVerticesCoord);
        this.pVertex.position(0);
        this.pTexCoord = ByteBuffer.allocateDirect(this.mTextureCoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(this.mTextureCoord);
        this.pTexCoord.position(0);
    }

    public void initDrawFrame() {
        GLES20.glUseProgram(this.mProgram);
        checkGlError("glUseProgram");
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        checkGlError("glEnableVertexAttribArray - attribPosition");
        GLES20.glEnableVertexAttribArray(this.maTexCoordsLoc);
        checkGlError("glEnableVertexAttribArray - attribTexCoords");
        if ((this.mFrameType & 2) != 0) {
            ProjectionMatrix.setRotation(90.0f);
        } else {
            ProjectionMatrix.setRotation(0.0f);
        }
        this.mMVPMatrix = ProjectionMatrix.getModelViewMatrix();
        GLES20.glUniformMatrix4fv(this.muMVPmatrixLoc, 1, false, this.mMVPMatrix, 0);
        checkGlError("glUniformMatrix4fv");
    }

    public static int[] initPreviewTexture(int numTexture) {
        int[] texture = new int[numTexture];
        GLES20.glGenTextures(numTexture, texture, 0);
        for (int i = 0; i < numTexture; i++) {
            GLES20.glActiveTexture(33984 + i);
            GLES20.glBindTexture(36197, texture[i]);
            GLES20.glTexParameteri(36197, 10242, 33071);
            GLES20.glTexParameteri(36197, 10243, 33071);
            GLES20.glTexParameteri(36197, 10241, 9729);
            GLES20.glTexParameteri(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
            checkGlError("glBindTexture: " + i);
        }
        return texture;
    }

    public static void deleteTexture(int[] texture, int count) {
        int length = count;
        if (texture != null) {
            if (length == 0) {
                length = texture.length;
            }
            CamLog.m3d(CameraConstants.TAG, "-gl - glDeleteTextures");
            GLES20.glDeleteTextures(texture.length, texture, 0);
            GLES20.glFlush();
        }
    }

    protected int buildProgram(String vertex, String fragment) {
        int vertexShader = buildShader(vertex, 35633);
        if (vertexShader == 0) {
            return 0;
        }
        int fragmentShader = buildShader(fragment, 35632);
        if (fragmentShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        checkGlError("glLinkProgram");
        int[] status = new int[1];
        GLES20.glGetProgramiv(program, 35714, status, 0);
        if (status[0] == 1) {
            return program;
        }
        CamLog.m3d(CameraConstants.TAG, "Error while linking program:\n" + GLES20.glGetProgramInfoLog(program));
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        GLES20.glDeleteProgram(program);
        return 0;
    }

    private int buildShader(String source, int type) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        checkGlError("glShaderSource");
        GLES20.glCompileShader(shader);
        checkGlError("glCompileShader");
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, 35713, status, 0);
        if (status[0] == 1) {
            return shader;
        }
        CamLog.m3d(CameraConstants.TAG, "MultiView - Error while compiling shader:\n" + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    public int getCurrentPreviewMode() {
        CamLog.m3d(CameraConstants.TAG, "getCurrentPreviewMode mCurLayout = " + this.mCurLayout);
        return this.mCurLayout;
    }

    public void changeLayout() {
    }

    public void changeLayout(int toViewType, boolean animationOn) {
    }

    public void showAnimation() {
    }

    public boolean isShowingAnimation() {
        return this.mIsShowingAnimation;
    }

    public void drawFrame(int[] preivewTexture, float[][] texMatrix) {
    }

    public void drawFrameCollage(int[] texture) {
    }

    public void drawFrameCollage(int[] texture, CollagePictureTakenListener listener) {
    }

    protected static void checkGlError(String msg) {
        int error = GLES20.glGetError();
        if (error != 0) {
            CamLog.m11w(CameraConstants.TAG, "MultiView - " + msg + " : GL error = 0x" + Integer.toHexString(error));
        }
    }

    public void changePreviewSize(int ratio) {
    }

    protected void moveToRecordingView() {
    }

    protected void processGesture(float x, float y, int degree) {
    }

    public int getMultiIntervalMaxCount() {
        return 0;
    }

    public int[] getCurCameraIdArray() {
        return null;
    }

    public void setCurCameraIdArray() {
    }

    protected void freezePreview(int viewIndex, String filePath) {
    }

    public void setInterface(IMultiViewModule multiviewModule) {
    }

    public void restoreVertex(int degree) {
    }

    public Bitmap captureTexture(int x, int y, int width, int height) {
        return null;
    }

    public boolean isCapturingPreview() {
        return false;
    }

    public int getCapturedIndex() {
        return sCapturedIndex;
    }

    public static void resetGenTex() {
        sIsGenTex = false;
    }

    public void swapView(int degree) {
    }

    public void setRotateState(int rotate) {
    }

    public void setRotateStateIncremental() {
    }

    public void rotateView(int degree) {
    }

    public void setBeforeTextureCoord() {
    }

    public void setImageForPostview() {
    }

    public int getLayoutListCountMVFrame() {
        return 0;
    }
}
