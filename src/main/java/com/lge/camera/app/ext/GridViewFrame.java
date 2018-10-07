package com.lge.camera.app.ext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.arcsoft.stickerlibrary.utils.Constant;
import com.lge.camera.app.ProjectionMatrix;
import com.lge.camera.components.GridViewLayout;
import com.lge.camera.components.GridViewLayoutInfo;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class GridViewFrame {
    public static final int FRAME_TYPE_COLLAGE = 4;
    public static final int FRAME_TYPE_DISPLAY = 1;
    public static final int FRAME_TYPE_RECORD = 2;
    public static final int GL_CAM1_TEXNUM = 33984;
    public static final int GL_CAM2_TEXNUM = 33985;
    public static final int GL_CAPTURE1_TEXNUM = 33991;
    public static final int GL_COLLAGE1_TEXNUM = 33987;
    public static final int GL_EXT_CAM_TEXNUM = 33986;
    private static final int NUM_TEXTURE = 4;
    public static final int PREVIEW_TEX_NUM = 3;
    private static Bitmap[] sImageBitmap = new Bitmap[4];
    private static int sNumTextureCollage = 0;
    private static int[] sTypeCollage;
    protected final int FLOAT_SIZE_BYTES;
    protected float[] mColTexMatrix;
    protected int mFrameType;
    protected final String mFss;
    protected GridViewLayout mGridViewLayout;
    private int[] mInputFileType;
    private int[] mIsRearCam;
    protected float[] mMVPMatrix;
    protected int mProgram;
    private ArrayList<Integer> mRecordingDegrees;
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

    public GridViewFrame() {
        this.mFrameType = 1;
        this.mVss = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrixRear;\nuniform mat4 uTexMatrixFront;\nuniform mat4 uTexMatrixImg;\nattribute vec4 aPosition;\nattribute vec4 aTexCoord;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordImg;\nvarying vec2 vTexCoordExt;\nvoid main() {\n  vTexCoordRear = (uTexMatrixRear * aTexCoord).xy;\n  vTexCoordFront = (uTexMatrixFront * aTexCoord).xy;\n  vTexCoordImg = (uTexMatrixImg * aTexCoord).xy;\n  vTexCoordExt = aTexCoord.xy;\n  gl_Position = uMVPMatrix * aPosition;\n}";
        this.mFss = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES uTextureCamera;\nuniform sampler2D uTextureImage;\nuniform float uTextureType;\nuniform float uCameraId;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordExt;\nvarying vec2 vTexCoordImg;\nvoid main() {\n  if (uTextureType == 1.0) {\n    gl_FragColor = texture2D(uTextureImage, vTexCoordImg);\n  } else {\n    if (uCameraId == 1.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordFront);\n    } else if (uCameraId == 0.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordRear);\n    } else {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordExt);\n    }\n  }}";
        this.FLOAT_SIZE_BYTES = 4;
        this.mColTexMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mTmpMatrixImg = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mFrameType = 1;
        CamLog.m3d(CameraConstants.TAG, "-rec- mFrameType = " + this.mFrameType);
    }

    public GridViewFrame(int frameType) {
        this.mFrameType = 1;
        this.mVss = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrixRear;\nuniform mat4 uTexMatrixFront;\nuniform mat4 uTexMatrixImg;\nattribute vec4 aPosition;\nattribute vec4 aTexCoord;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordImg;\nvarying vec2 vTexCoordExt;\nvoid main() {\n  vTexCoordRear = (uTexMatrixRear * aTexCoord).xy;\n  vTexCoordFront = (uTexMatrixFront * aTexCoord).xy;\n  vTexCoordImg = (uTexMatrixImg * aTexCoord).xy;\n  vTexCoordExt = aTexCoord.xy;\n  gl_Position = uMVPMatrix * aPosition;\n}";
        this.mFss = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES uTextureCamera;\nuniform sampler2D uTextureImage;\nuniform float uTextureType;\nuniform float uCameraId;\nvarying vec2 vTexCoordRear;\nvarying vec2 vTexCoordFront;\nvarying vec2 vTexCoordExt;\nvarying vec2 vTexCoordImg;\nvoid main() {\n  if (uTextureType == 1.0) {\n    gl_FragColor = texture2D(uTextureImage, vTexCoordImg);\n  } else {\n    if (uCameraId == 1.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordFront);\n    } else if (uCameraId == 0.0) {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordRear);\n    } else {\n       gl_FragColor = texture2D(uTextureCamera, vTexCoordExt);\n    }\n  }}";
        this.FLOAT_SIZE_BYTES = 4;
        this.mColTexMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mTmpMatrixImg = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        this.mFrameType = frameType;
        CamLog.m3d(CameraConstants.TAG, "-rec- mFrameType = " + this.mFrameType);
    }

    public void init(int[] inputFileType, ArrayList<Integer> recordingDegrees, int degree, int[] isRearCam) {
        this.mInputFileType = inputFileType;
        this.mRecordingDegrees = recordingDegrees;
        this.mIsRearCam = isRearCam;
        init(degree);
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
        if (this.mGridViewLayout == null) {
            this.mGridViewLayout = new GridViewLayout(degree, new GridViewLayoutInfo());
        }
        this.mGridViewLayout.setCameraIdArray(this.mIsRearCam);
        this.mVerticesCoord = (float[]) this.mGridViewLayout.getCurVertex().clone();
        if ((this.mFrameType & 6) != 0) {
            if (this.mInputFileType != null) {
                this.mTextureCoord = (float[]) this.mGridViewLayout.getTexCoordCollage(this.mInputFileType, this.mRecordingDegrees).clone();
            } else {
                CamLog.m5e(CameraConstants.TAG, "-rec- input file type should be specified");
            }
        }
        this.mVerticesPrev = (float[]) this.mVerticesCoord.clone();
        this.mVerticesTransition = (float[]) this.mVerticesCoord.clone();
        this.mTexturePrev = (float[]) this.mTextureCoord.clone();
        this.mTextureTransition = (float[]) this.mTextureCoord.clone();
    }

    protected void setVertices() {
        CamLog.m3d(CameraConstants.TAG, "-gridview- setVertices");
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

    public static void deleteTexture(int[] texture, int count) {
        int length = count;
        if (texture != null) {
            if (length == 0) {
                length = texture.length;
            }
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
        CamLog.m3d(CameraConstants.TAG, "gridview - Error while compiling shader:\n" + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    protected static void checkGlError(String msg) {
        int error = GLES20.glGetError();
        if (error != 0) {
            CamLog.m11w(CameraConstants.TAG, "gridview - " + msg + " : GL error = 0x" + Integer.toHexString(error));
        }
    }

    public static int[] initTextureCollage(int numTexture, int[] type, ArrayList<String> fileNameArray) {
        sNumTextureCollage = numTexture;
        sTypeCollage = type;
        int[] texture = new int[numTexture];
        GLES20.glGenTextures(numTexture, texture, 0);
        for (int i = 0; i < numTexture; i++) {
            GLES20.glActiveTexture(33987 + i);
            if (type[i] == 2) {
                GLES20.glBindTexture(36197, texture[i]);
                GLES20.glTexParameteri(36197, 10242, 33071);
                GLES20.glTexParameteri(36197, 10243, 33071);
                GLES20.glTexParameteri(36197, 10241, 9729);
                GLES20.glTexParameteri(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
            } else {
                String fileName = (String) fileNameArray.get(i);
                new Options().inSampleSize = 2;
                Bitmap bitmap = sImageBitmap[i];
                if (bitmap != null) {
                    GLES20.glBindTexture(3553, texture[i]);
                    GLES20.glTexParameterf(3553, 10241, 9729.0f);
                    GLES20.glTexParameterf(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
                    GLUtils.texImage2D(3553, 0, bitmap, 0);
                } else {
                    CamLog.m5e(CameraConstants.TAG, "bitmap is null; please check the existence of the file : " + fileName);
                }
            }
            checkGlError("glBindTexture: " + i);
        }
        GLES20.glBindTexture(36197, 0);
        return texture;
    }

    public static void setImageBitmap(Bitmap[] bm) {
        sImageBitmap = bm;
    }

    private void initCollageTexture(int[] texture) {
        GLES20.glUniform1f(this.muCameraIdLoc, 0.0f);
        checkGlError("glUniform1f");
        if (this.mColTexMatrix != null) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixRearLoc, 1, false, this.mColTexMatrix, 0);
            checkGlError("glUniformMatrix4fv");
            GLES20.glUniformMatrix4fv(this.muTexMatrixFrontLoc, 1, false, this.mColTexMatrix, 0);
            checkGlError("glUniformMatrix4fv");
        }
        if (this.mTmpMatrixImg != null) {
            GLES20.glUniformMatrix4fv(this.muTexMatrixImgLoc, 1, false, this.mTmpMatrixImg, 0);
            checkGlError("glUniformMatrix4fv");
        }
        int i = 0;
        while (i < sNumTextureCollage) {
            GLES20.glActiveTexture(33987 + i);
            if (sTypeCollage == null || sTypeCollage[i] != 2) {
                GLES20.glBindTexture(3553, texture[i]);
                GLES20.glUniform1f(this.muTextureTypeLoc, 1.0f);
                checkGlError("glUniform1f");
                GLES20.glUniform1i(this.muTextureImageLoc, i + 3);
                checkGlError("glUniform1i");
            } else {
                GLES20.glBindTexture(36197, texture[i]);
                GLES20.glUniform1f(this.muTextureTypeLoc, 0.0f);
                checkGlError("glUniform1f");
                GLES20.glUniform1i(this.muTextureCameraLoc, i + 3);
                checkGlError("glUniform1i");
            }
            GLES20.glDrawArrays(5, i * 4, 4);
            checkGlError("glDrawArrays");
            i++;
        }
    }

    public void drawFrameCollage(int[] texture) {
        GLES20.glUseProgram(this.mProgram);
        checkGlError("glUseProgram");
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        checkGlError("glEnableVertexAttribArray - attribPosition");
        GLES20.glEnableVertexAttribArray(this.maTexCoordsLoc);
        checkGlError("glEnableVertexAttribArray - attribTexCoords");
        this.pVertex.position(0);
        this.pTexCoord.position(0);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, 5126, false, 0, this.pVertex);
        GLES20.glVertexAttribPointer(this.maTexCoordsLoc, 2, 5126, false, 0, this.pTexCoord);
        if ((this.mFrameType & 2) != 0) {
            ProjectionMatrix.setRotation(90.0f);
        } else {
            ProjectionMatrix.setRotation(0.0f);
        }
        this.mMVPMatrix = ProjectionMatrix.getModelViewMatrix();
        GLES20.glUniformMatrix4fv(this.muMVPmatrixLoc, 1, false, this.mMVPMatrix, 0);
        checkGlError("glUniformMatrix4fv");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        checkGlError("glClearColor");
        GLES20.glClear(16384);
        checkGlError("glClear");
        initCollageTexture(texture);
    }
}
