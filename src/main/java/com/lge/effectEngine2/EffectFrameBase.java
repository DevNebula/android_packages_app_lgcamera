package com.lge.effectEngine2;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.arcsoft.stickerlibrary.utils.Constant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class EffectFrameBase {
    private static final String FRAGMENT_SHADER_BLUR = "precision mediump float;\nuniform sampler2D sTexture;\nuniform float iter;\nuniform float p_size_x;\nuniform float p_size_y;\nvarying vec2 vTextureCoord;\nvec3 KawaseBlurFilter( sampler2D tex, vec2 texCoord, vec2 pixelSize, float iteration )\n{\nvec2 texCoordSample;\nvec2 halfPixelSize = pixelSize / 2.0;\nvec2 dUV = ( pixelSize.xy * vec2( iteration, iteration ) ) + halfPixelSize.xy;\nvec3 cOut;\n// Sample top left pixel\ntexCoordSample.x = texCoord.x - dUV.x;\ntexCoordSample.y = texCoord.y + dUV.y;\ncOut = texture2D( tex, texCoordSample ).xyz;\n// Sample top right pixel\ntexCoordSample.x = texCoord.x + dUV.x;\ntexCoordSample.y = texCoord.y + dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Sample bottom right pixel\ntexCoordSample.x = texCoord.x + dUV.x;\ntexCoordSample.y = texCoord.y - dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Sample bottom left pixel\ntexCoordSample.x = texCoord.x - dUV.x;\ntexCoordSample.y = texCoord.y - dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Average \ncOut *= 0.25;\nreturn cOut;\n}\nvoid main() {\n    vec2 p_size;\n    p_size.x = p_size_x;\n    p_size.y = p_size_y;\n    gl_FragColor.xyz = KawaseBlurFilter( sTexture, vTextureCoord, p_size, iter );\n    gl_FragColor.w = 1.0;\n}\n";
    private static final String FRAGMENT_SHADER_BLUR_FAST = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[14];\nvoid main()\n{\n    gl_FragColor = vec4(0.0);\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 0])*0.0229;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 1])*0.0345;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 2])*0.0486;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 3])*0.0644;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 4])*0.0801;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 5])*0.0936;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 6])*0.1029;\n    gl_FragColor += texture2D(sTexture, vTextureCoord      )*0.1061;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 7])*0.1029;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 8])*0.0936;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 9])*0.0801;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[10])*0.0644;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[11])*0.0486;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[12])*0.0345;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[13])*0.0229;\n}\n";
    private static final String FRAGMENT_SHADER_GRAY = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float rValue;\nuniform float gValue;\nuniform float bValue;\nvoid main() {\n    vec4 tc = texture2D(sTexture, vTextureCoord);\n    float color = tc.r * rValue + tc.g * gValue + tc.b * bValue;\n    gl_FragColor = vec4(color, color, color, 1.0);\n}\n";
    private static final String FRAGMENT_SHADER_MASK = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nuniform sampler2D mask;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       vec4 cMask = texture2D(mask, vTextureCoordOrg);\n       gl_FragColor =  step(0.5, cMask.r) * cc;\n}\n";
    private static final String FRAGMENT_SHADER_MASK_OES = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nuniform sampler2D mask;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       vec4 cMask = texture2D(mask, vTextureCoordOrg);\n       gl_FragColor =  step(0.5, cMask.r) * cc;\n}\n";
    private static final String FRAGMENT_SHADER_NONE = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       gl_FragColor = cc;\n}\n";
    private static final String FRAGMENT_SHADER_NONE_OES = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       gl_FragColor =  cc;\n}\n";
    private static final String FRAGMENT_SHADER_PERSPECTIVE = "precision highp float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float angle;//0~0.5\nvoid main()\n{\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n  vec2 p = vTextureCoordOrg;\n  vec2 m = vec2(0.5, 0.5);\n  vec2 d = p - m;\n  float r = length(d); \n  float power = (3.141592 / (sqrt(dot(m, m)))) *(angle);\n  float bind = sqrt(dot(m, m));//stick to corners\n  vec2 uv = m + normalize(d) * tan(r * power) * bind / tan( bind * power);\n  vec3 col = texture2D(sTexture, uv).xyz;\n  gl_FragColor = vec4(col, 1.0);\n}\n";
    private static final String FRAGMENT_SHADER_VIGNETTING = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float innerRadius;\nuniform float outRadius;\nuniform float brightness;\nuniform float vignettingAlpha;\n\nvoid main() {\n    vec4 color = texture2D(sTexture, vTextureCoord);\n    color.rgb = color.rgb * brightness;\n    float dist = length(vec2(0.5, 0.5) - vTextureCoord);\n    float normal = smoothstep(outRadius, innerRadius, dist) * vignettingAlpha + (1.0 - vignettingAlpha);\n    color.rgb = mix(color.rgb, color.rgb * normal, 1.0);\n    gl_FragColor =  color;\n}\n";
    protected static final int FRAMEBUFFER_NEXT_BLUR_ID = 3;
    protected static final int FRAMEBUFFER_NEXT_BLUR_TEX_ID = 3;
    protected static final int FRAMEBUFFER_NEXT_ID = 1;
    protected static final int FRAMEBUFFER_NEXT_TEXTURE_ID = 1;
    protected static final int FRAMEBUFFER_PREV_BLUR_ID = 2;
    protected static final int FRAMEBUFFER_PREV_BLUR_TEX_ID = 2;
    protected static final int FRAMEBUFFER_PREV_ID = 0;
    protected static final int FRAMEBUFFER_PREV_TEXTURE_ID = 0;
    protected static final int NUM_FBOTEXTURES = 4;
    protected static final int NUM_FRAMEBUFFERS = 4;
    protected static final int NUM_PROGRAMS = 10;
    private static final int PROGRAM_BLUR = 2;
    protected static final int PROGRAM_BLUR_H = 3;
    protected static final int PROGRAM_BLUR_V = 4;
    protected static final int PROGRAM_GRAY = 5;
    protected static final int PROGRAM_MASK = 8;
    protected static final int PROGRAM_MASK_OES = 9;
    protected static final int PROGRAM_NONE = 0;
    protected static final int PROGRAM_NONE_OES = 1;
    protected static final int PROGRAM_PERS = 6;
    protected static final int PROGRAM_VIGNECT = 7;
    protected static final int SIZEOF_FLOAT = 4;
    private static final String TAG = "EffectFrameBase";
    private static final String VERTEX_BLUR_H_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[14];\nvoid main()\n{\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n    for( int i=0; i < 7; i++){\n        float val = float(i-7)*0.005;\n        v_blurTexCoords[ i] = vTextureCoord + vec2(val, 0.0);\n    }\n    for( int j=7; j < 14; j++){\n        float val = float(j-6)*0.005;\n        v_blurTexCoords[ j] = vTextureCoord + vec2(val, 0.0);\n    }\n}\n";
    private static final String VERTEX_BLUR_V_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[14];\nvoid main()\n{\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n    for( int i=0; i < 7; i++){\n        float val = float(i-7)*0.005;\n        v_blurTexCoords[ i] = vTextureCoord + vec2(0.0, val);\n    }\n    for( int j=7; j < 14; j++){\n        float val = float(j-6)*0.005;\n        v_blurTexCoords[ j] = vTextureCoord + vec2(0.0, val);\n    }\n}\n";
    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n";
    protected float division_ratio;
    protected boolean isDualCameraMode;
    protected int mAngleLoc;
    protected float[] mBlurKernel = new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f};
    protected int mBlurKernelIdx;
    protected int mCoordsPerVertex;
    protected int mCurrentFrameBuf;
    protected int[] mFboTextures = new int[4];
    protected int[] mFramebufferIDs = new int[4];
    protected int mGrayBValueLoc;
    protected int mGrayGValueLoc;
    protected int mGrayRValueLoc;
    protected float[] mIdentityMatrix;
    protected int mIterLoc;
    protected int mLastTextureID;
    protected int mPSizeXLoc;
    protected int mPSizeYLoc;
    protected int[] mPrograms = new int[10];
    protected FloatBuffer mTexCoordArray;
    protected FloatBuffer mTexCoordCropArray;
    protected int mTexCoordStride;
    protected int mTexture1LocBlur;
    protected FloatBuffer mVertexArray;
    protected int mVertexCount;
    protected int mVertexStride;
    protected int mVignettingAlphaLoc;
    protected int mVignettingBrightnessLoc;
    protected int mVignettingInnerRadiusLoc;
    protected int mVignettingOutRadiusLoc;
    protected int[] maPositionLocs = new int[10];
    protected int[] maTextureCoordLocs = new int[10];
    protected int[] muMVPMatrixLocs = new int[10];
    protected int[] muTexMatrixLocs = new int[10];
    protected float norm_to_wide_ratio_singlemode;
    protected int scale_of_blur;
    protected int scale_of_fbo;

    protected abstract Rect convertCoord(float f, float f2, float f3, float f4);

    protected abstract void drawTexture(int i, float[] fArr, int i2, int i3, int i4, int i5);

    public EffectFrameBase(boolean isDualMode) {
        this.isDualCameraMode = isDualMode;
        this.scale_of_fbo = 2;
        this.scale_of_blur = 2;
        this.norm_to_wide_ratio_singlemode = ShaderParameterData.getInstance().getSingleModeRatio();
        this.division_ratio = ShaderParameterData.getInstance().getNormToWideRatio()[3];
        createVectorInfo();
    }

    private void createVectorInfo() {
        this.mIdentityMatrix = new float[16];
        Matrix.setIdentityM(this.mIdentityMatrix, 0);
        float[] RECTANGLE_COORDS = new float[]{-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
        float[] RECTANGLE_TEX_COORDS = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
        float[] RECTANGLE_TEX_COORDS_CROP = new float[]{0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), (this.norm_to_wide_ratio_singlemode / 2.0f) + 0.5f, (this.norm_to_wide_ratio_singlemode / 2.0f) + 0.5f, (this.norm_to_wide_ratio_singlemode / 2.0f) + 0.5f, 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), (this.norm_to_wide_ratio_singlemode / 2.0f) + 0.5f, 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f)};
        ByteBuffer vertexByteBuf = ByteBuffer.allocateDirect(RECTANGLE_COORDS.length * 4);
        vertexByteBuf.order(ByteOrder.nativeOrder());
        this.mVertexArray = vertexByteBuf.asFloatBuffer();
        this.mVertexArray.put(RECTANGLE_COORDS);
        this.mVertexArray.position(0);
        ByteBuffer textureByteBuf = ByteBuffer.allocateDirect(RECTANGLE_TEX_COORDS.length * 4);
        textureByteBuf.order(ByteOrder.nativeOrder());
        this.mTexCoordArray = textureByteBuf.asFloatBuffer();
        this.mTexCoordArray.put(RECTANGLE_TEX_COORDS);
        this.mTexCoordArray.position(0);
        ByteBuffer textureCropByteBuf = ByteBuffer.allocateDirect(RECTANGLE_TEX_COORDS_CROP.length * 4);
        textureCropByteBuf.order(ByteOrder.nativeOrder());
        this.mTexCoordCropArray = textureCropByteBuf.asFloatBuffer();
        this.mTexCoordCropArray.put(RECTANGLE_TEX_COORDS_CROP);
        this.mTexCoordCropArray.position(0);
        this.mCoordsPerVertex = 2;
        this.mVertexStride = this.mCoordsPerVertex * 4;
        this.mVertexCount = RECTANGLE_COORDS.length / this.mCoordsPerVertex;
        this.mTexCoordStride = 8;
    }

    public void createPrograms() {
        this.mPrograms[1] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_NONE_OES);
        this.mPrograms[0] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_NONE);
        this.mPrograms[2] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_BLUR);
        this.mPrograms[3] = createProgram(VERTEX_BLUR_H_SHADER, FRAGMENT_SHADER_BLUR_FAST);
        this.mPrograms[4] = createProgram(VERTEX_BLUR_V_SHADER, FRAGMENT_SHADER_BLUR_FAST);
        this.mPrograms[5] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_GRAY);
        this.mPrograms[6] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_PERSPECTIVE);
        this.mPrograms[7] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_VIGNETTING);
        this.mPrograms[8] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_MASK);
        this.mPrograms[9] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_MASK_OES);
        Log.d(TAG, "Created Programs complete");
    }

    public void createLocations() {
        for (int programIndex = 0; programIndex < 10; programIndex++) {
            if (this.mPrograms[programIndex] == 0) {
                throw new RuntimeException("Unable to create program: " + programIndex);
            }
            this.maPositionLocs[programIndex] = GLES20.glGetAttribLocation(this.mPrograms[programIndex], "aPosition");
            this.maTextureCoordLocs[programIndex] = GLES20.glGetAttribLocation(this.mPrograms[programIndex], "aTextureCoord");
            this.muMVPMatrixLocs[programIndex] = GLES20.glGetUniformLocation(this.mPrograms[programIndex], "uMVPMatrix");
            this.muTexMatrixLocs[programIndex] = GLES20.glGetUniformLocation(this.mPrograms[programIndex], "uTexMatrix");
        }
        this.mIterLoc = GLES20.glGetUniformLocation(this.mPrograms[2], "iter");
        this.mPSizeXLoc = GLES20.glGetUniformLocation(this.mPrograms[2], "p_size_x");
        this.mPSizeYLoc = GLES20.glGetUniformLocation(this.mPrograms[2], "p_size_y");
        this.mAngleLoc = GLES20.glGetUniformLocation(this.mPrograms[6], "angle");
        this.mVignettingInnerRadiusLoc = GLES20.glGetUniformLocation(this.mPrograms[7], "innerRadius");
        this.mVignettingOutRadiusLoc = GLES20.glGetUniformLocation(this.mPrograms[7], "outRadius");
        this.mVignettingBrightnessLoc = GLES20.glGetUniformLocation(this.mPrograms[7], "brightness");
        this.mVignettingAlphaLoc = GLES20.glGetUniformLocation(this.mPrograms[7], "vignettingAlpha");
        this.mGrayRValueLoc = GLES20.glGetUniformLocation(this.mPrograms[5], "rValue");
        this.mGrayGValueLoc = GLES20.glGetUniformLocation(this.mPrograms[5], "gValue");
        this.mGrayBValueLoc = GLES20.glGetUniformLocation(this.mPrograms[5], "bValue");
    }

    protected void createFrameBuffers(int width, int height) {
        GLES20.glGenTextures(4, this.mFboTextures, 0);
        GLES20.glGenFramebuffers(4, this.mFramebufferIDs, 0);
        Log.d(TAG, "texture : " + this.mFboTextures[0] + "," + this.mFboTextures[1] + "," + this.mFboTextures[2] + "," + this.mFboTextures[3]);
        Log.d(TAG, "fbo : " + this.mFramebufferIDs[0] + "," + this.mFramebufferIDs[1] + "," + this.mFramebufferIDs[2] + "," + this.mFramebufferIDs[3]);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[0]);
        bindTextureFBO(this.mFboTextures[0], width / this.scale_of_fbo, height / this.scale_of_fbo);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[1]);
        bindTextureFBO(this.mFboTextures[1], width / this.scale_of_fbo, height / this.scale_of_fbo);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[2]);
        bindTextureFBO(this.mFboTextures[2], (width / this.scale_of_fbo) / this.scale_of_blur, (height / this.scale_of_fbo) / this.scale_of_blur);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[3]);
        bindTextureFBO(this.mFboTextures[3], (width / this.scale_of_fbo) / this.scale_of_blur, (height / this.scale_of_fbo) / this.scale_of_blur);
    }

    protected void release() {
        Log.d(TAG, "deleting programs ");
        for (int programIndex = 0; programIndex < 10; programIndex++) {
            GLES20.glDeleteProgram(this.mPrograms[programIndex]);
            this.mPrograms[programIndex] = -1;
        }
        releaseFrameBuffers();
    }

    protected void releaseFrameBuffers() {
        Log.d(TAG, "deleting fbo textures..");
        GLES20.glDeleteTextures(4, this.mFboTextures, 0);
        Log.d(TAG, "deleting FBOs..");
        GLES20.glDeleteFramebuffers(4, this.mFramebufferIDs, 0);
    }

    protected void filterDraw(int mode, int frameBufferWidth, int frameBufferHeight) {
        if (isCheckedMode(mode, 4)) {
            bindFBOAndTexture();
            drawTexture(5, null, 0, 0, frameBufferWidth, frameBufferHeight);
        }
        if (isCheckedMode(mode, 1)) {
            float time = ((float) (System.nanoTime() - System.nanoTime())) / 1000000.0f;
            int steps = (int) ShaderParameterData.getInstance().getBlurValue()[0];
            this.mCurrentFrameBuf = 2;
            bindFBOAndTexture(this.mFboTextures[this.mLastTextureID], false);
            this.mBlurKernelIdx = 0;
            drawTexture(2, null, 0, 0, frameBufferWidth / this.scale_of_blur, frameBufferHeight / this.scale_of_blur);
            this.mCurrentFrameBuf = 3;
            for (int i = 1; i < steps; i++) {
                bindFBOAndTexture();
                this.mBlurKernelIdx = i;
                drawTexture(2, null, 0, 0, frameBufferWidth / this.scale_of_blur, frameBufferHeight / this.scale_of_blur);
            }
            this.mCurrentFrameBuf = 0;
            bindFBOAndTexture(this.mFboTextures[this.mLastTextureID], false);
            this.mBlurKernelIdx = 1;
            drawTexture(2, null, 0, 0, frameBufferWidth, frameBufferHeight);
            this.mCurrentFrameBuf = 1;
        }
        if (isCheckedMode(mode, 2)) {
            bindFBOAndTexture();
            drawTexture(6, null, 0, 0, frameBufferWidth, frameBufferHeight);
        }
        if (isCheckedMode(mode, 8)) {
            bindFBOAndTexture();
            drawTexture(7, null, 0, 0, frameBufferWidth, frameBufferHeight);
        }
    }

    protected void setTextureParam(int programID, int width, int height) {
        ShaderParameterData paramData = ShaderParameterData.getInstance();
        switch (programID) {
            case 2:
                float p_size_x = 1.0f / ((float) width);
                float p_size_y = 1.0f / ((float) height);
                GLES20.glUniform1f(this.mIterLoc, this.mBlurKernel[this.mBlurKernelIdx]);
                GLES20.glUniform1f(this.mPSizeXLoc, p_size_x);
                GLES20.glUniform1f(this.mPSizeYLoc, p_size_y);
                return;
            case 5:
                GLES20.glUniform1f(this.mGrayRValueLoc, paramData.getGrayValue()[0]);
                GLES20.glUniform1f(this.mGrayGValueLoc, paramData.getGrayValue()[1]);
                GLES20.glUniform1f(this.mGrayBValueLoc, paramData.getGrayValue()[2]);
                return;
            case 6:
                GLES20.glUniform1f(this.mAngleLoc, paramData.getPerspectiveValue()[0]);
                return;
            case 7:
                GLES20.glUniform1f(this.mVignettingInnerRadiusLoc, paramData.getVignettingValue()[0]);
                GLES20.glUniform1f(this.mVignettingOutRadiusLoc, paramData.getVignettingValue()[1]);
                GLES20.glUniform1f(this.mVignettingBrightnessLoc, paramData.getVignettingValue()[2]);
                GLES20.glUniform1f(this.mVignettingAlphaLoc, paramData.getVignettingValue()[3]);
                return;
            default:
                return;
        }
    }

    protected void setVertexParam(int programID, int width, int height, float[] mvpMatrix) {
        FloatBuffer texCoordArray;
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[programID], 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[programID], 1, false, this.mIdentityMatrix, 0);
        GLES20.glEnableVertexAttribArray(this.maPositionLocs[programID]);
        GLES20.glVertexAttribPointer(this.maPositionLocs[programID], this.mCoordsPerVertex, 5126, false, this.mVertexStride, this.mVertexArray);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[programID]);
        if (this.isDualCameraMode) {
            texCoordArray = this.mTexCoordArray;
        } else {
            texCoordArray = this.mTexCoordCropArray;
        }
        GLES20.glVertexAttribPointer(this.maTextureCoordLocs[programID], 2, 5126, false, this.mTexCoordStride, texCoordArray);
    }

    protected void bindTextureOES(int textureID) {
        GLES20.glBindTexture(36197, textureID);
        GLES20.glTexParameterf(36197, 10241, 9729.0f);
        GLES20.glTexParameterf(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        checkGlError("bindTexturesOES");
    }

    protected void bindTextureFBO(int textureID, int width, int height) {
        bindTexture2D(textureID);
        GLES20.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, null);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, textureID, 0);
        checkGlError("bindTexturesFrameBuffer");
    }

    protected void bindTexture2D(int textureID) {
        GLES20.glBindTexture(3553, textureID);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("bindTextures2D");
    }

    protected void bindFBOAndTexture() {
        switch (this.mCurrentFrameBuf) {
            case 0:
                bindFBOAndTexture(this.mFboTextures[1], false);
                this.mCurrentFrameBuf = 1;
                return;
            case 1:
                bindFBOAndTexture(this.mFboTextures[0], false);
                this.mCurrentFrameBuf = 0;
                return;
            case 2:
                bindFBOAndTexture(this.mFboTextures[3], false);
                this.mCurrentFrameBuf = 3;
                return;
            case 3:
                bindFBOAndTexture(this.mFboTextures[2], false);
                this.mCurrentFrameBuf = 2;
                return;
            default:
                return;
        }
    }

    protected void bindFBOAndTexture(int textureID, boolean isOES) {
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[this.mCurrentFrameBuf]);
        GLES20.glActiveTexture(33984);
        if (isOES) {
            GLES20.glBindTexture(36197, 0);
            GLES20.glBindTexture(36197, textureID);
        } else {
            GLES20.glBindTexture(3553, textureID);
        }
        this.mLastTextureID = this.mCurrentFrameBuf;
    }

    protected void bindRenderBufferAndTexture() {
        GLES20.glActiveTexture(33984);
        if (this.mCurrentFrameBuf == 0) {
            GLES20.glBindTexture(3553, this.mFboTextures[1]);
        } else {
            GLES20.glBindTexture(3553, this.mFboTextures[0]);
        }
    }

    protected static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(35632, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
        if (linkStatus[0] == 1) {
            return program;
        }
        Log.e(TAG, "Could not link program: ");
        Log.e(TAG, GLES20.glGetProgramInfoLog(program));
        GLES20.glDeleteProgram(program);
        return 0;
    }

    protected static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        Log.e(TAG, "Could not compile shader " + shaderType + ":");
        Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    protected static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            new StringBuilder(String.valueOf(op)).append(": glError 0x").append(Integer.toHexString(error)).toString();
        }
    }

    protected static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    protected static boolean isCheckedMode(int mode, int type) {
        return (mode & type) > 0;
    }
}
