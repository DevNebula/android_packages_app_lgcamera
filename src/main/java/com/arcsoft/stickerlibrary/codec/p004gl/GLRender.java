package com.arcsoft.stickerlibrary.codec.p004gl;

import android.opengl.GLES20;
import com.arcsoft.stickerlibrary.codec.CodecLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/* renamed from: com.arcsoft.stickerlibrary.codec.gl.GLRender */
public class GLRender {
    private static final String FRAG_SHADER = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
    private static final int SIZE_OF_FLOAT = 4;
    private static final String TAG = "Arc_GLRender";
    private static final String VERTEX_SHADER = "attribute vec2 vPos;\nattribute vec2 vTex;\nvarying   vec2 vTextureCoord;\nvoid main(){\ngl_Position=vec4(vPos,0.0,1.0);\nvTextureCoord = vTex;\n}\n";
    private int mFrameHeight;
    private int mFrameWidth;
    private boolean mIsMirror;
    private boolean mIsRenderIsReady;
    private int mPositionHandle;
    private int mProgram;
    private int mSamplerHandle;
    private int mTexCoordHandle;
    private int[] mVbos = new int[2];
    private int orientation;

    public GLRender(int frameWidth, int frameHeight, int orientation, boolean isMirror) {
        if (frameHeight * frameHeight <= 0 || frameWidth < 0 || frameHeight < 0) {
            throw new RuntimeException("GLRender() frameWidth=" + frameWidth + " ,frameHeight=" + frameHeight + " invalid.");
        }
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        this.mIsRenderIsReady = false;
    }

    public void initRender() {
        GLES20.glGenBuffers(2, this.mVbos, 0);
        float[] vertices = new float[]{-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f};
        ByteBuffer byteVertex = ByteBuffer.allocateDirect(vertices.length * 4);
        byteVertex.order(ByteOrder.nativeOrder());
        FloatBuffer floatVertex = byteVertex.asFloatBuffer();
        floatVertex.put(vertices);
        floatVertex.position(0);
        GLES20.glBindBuffer(34962, this.mVbos[0]);
        GLES20.glBufferData(34962, vertices.length * 4, floatVertex, 35044);
        GLES20.glBindBuffer(34962, 0);
        float[] textCoords = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
        ByteBuffer byteTextCoord = ByteBuffer.allocateDirect(textCoords.length * 4);
        byteTextCoord.order(ByteOrder.nativeOrder());
        FloatBuffer floatTextCoord = byteTextCoord.asFloatBuffer();
        floatTextCoord.put(textCoords);
        floatTextCoord.position(0);
        GLES20.glBindBuffer(34962, this.mVbos[1]);
        GLES20.glBufferData(34962, textCoords.length * 4, floatTextCoord, 35044);
        GLES20.glBindBuffer(34962, 0);
        this.mProgram = ShaderManager.createProgram(VERTEX_SHADER, FRAG_SHADER);
        if (this.mProgram == 0) {
            CodecLog.m42e(TAG, "initRender()-> create program failed");
            return;
        }
        this.mPositionHandle = GLES20.glGetAttribLocation(this.mProgram, "vPos");
        this.mTexCoordHandle = GLES20.glGetAttribLocation(this.mProgram, "vTex");
        this.mSamplerHandle = GLES20.glGetUniformLocation(this.mProgram, "sTexture");
        this.mIsRenderIsReady = true;
    }

    public void renderWithTextureId(int textureId) {
        if (this.mIsRenderIsReady) {
            CodecLog.m41d(TAG, "GL renderWithTextureId() in.error=" + GLES20.glGetError());
            GLES20.glClear(16384);
            CodecLog.m41d(TAG, "GL renderWithTextureId() errorA--=." + GLES20.glGetError());
            GLES20.glUseProgram(this.mProgram);
            CodecLog.m41d(TAG, "GL renderWithTextureId() errorA-=." + GLES20.glGetError());
            GLES20.glBindBuffer(34962, this.mVbos[0]);
            GLES20.glVertexAttribPointer(this.mPositionHandle, 2, 5126, false, 8, 0);
            GLES20.glEnableVertexAttribArray(this.mPositionHandle);
            CodecLog.m41d(TAG, "GL renderWithTextureId() errorA=." + GLES20.glGetError());
            GLES20.glBindBuffer(34962, this.mVbos[1]);
            GLES20.glVertexAttribPointer(this.mTexCoordHandle, 2, 5126, false, 8, 0);
            GLES20.glEnableVertexAttribArray(this.mTexCoordHandle);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, textureId);
            GLES20.glUniform1i(this.mSamplerHandle, 0);
            CodecLog.m41d(TAG, "GL renderWithTextureId() errorB=." + GLES20.glGetError());
            GLES20.glDrawArrays(6, 0, 4);
            CodecLog.m41d(TAG, "GL renderWithTextureId() errorC=." + GLES20.glGetError());
            GLES20.glDisableVertexAttribArray(this.mPositionHandle);
            GLES20.glDisableVertexAttribArray(this.mTexCoordHandle);
            GLES20.glBindBuffer(34962, 0);
            GLES20.glBindTexture(3553, 0);
            CodecLog.m41d(TAG, "GL renderWithTextureId() out.");
            return;
        }
        CodecLog.m42e(TAG, "GLRender is not initialized yet.");
    }

    public void unInitRender() {
        if (this.mIsRenderIsReady) {
            GLES20.glDeleteProgram(this.mProgram);
            GLES20.glDeleteBuffers(2, this.mVbos, 0);
        }
        this.mIsRenderIsReady = false;
    }

    public void updateMirrorAndOrientation() {
    }
}
