package com.lge.effectEngine2;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import com.arcsoft.stickerlibrary.utils.Constant;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class EffectFrameDrawer {
    private static final int BOUNDARY_INPUT_TEXTURE_ID = 8;
    private static final String FRAGMENT_SHADER_BLUR = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform float iter;\nuniform float p_size_x;\nuniform float p_size_y;\nuniform sampler2D sTexture;\nvec3 KawaseBlurFilter( sampler2D tex, vec2 texCoord, vec2 pixelSize, float iteration )\n{\nvec2 texCoordSample;\nvec2 halfPixelSize = pixelSize / 2.0;\nvec2 dUV = ( pixelSize.xy * vec2( iteration, iteration ) ) + halfPixelSize.xy;\nvec3 cOut;\n// Sample top left pixel\ntexCoordSample.x = texCoord.x - dUV.x;\ntexCoordSample.y = texCoord.y + dUV.y;\ncOut = texture2D( tex, texCoordSample ).xyz;\n// Sample top right pixel\ntexCoordSample.x = texCoord.x + dUV.x;\ntexCoordSample.y = texCoord.y + dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Sample bottom right pixel\ntexCoordSample.x = texCoord.x + dUV.x;\ntexCoordSample.y = texCoord.y - dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Sample bottom left pixel\ntexCoordSample.x = texCoord.x - dUV.x;\ntexCoordSample.y = texCoord.y - dUV.y;\ncOut += texture2D( tex, texCoordSample ).xyz;\n// Average \ncOut *= 0.25f;\nreturn cOut;\n}\nvoid main() {\n    vec2 p_size;\n    p_size.x = p_size_x;\n    p_size.y = p_size_y;\n    gl_FragColor.xyz = KawaseBlurFilter( sTexture, vTextureCoord, p_size, iter );\n    gl_FragColor.w = 1.0;\n}\n";
    private static final String FRAGMENT_SHADER_BLUR_FAST = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[28];\nvoid main()\n{\n    gl_FragColor = vec4(0.0);\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 0])*0.0016;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 1])*0.0027;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 2])*0.0045;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 3])*0.0071;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 4])*0.0108;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 5])*0.0158;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 6])*0.0223;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 7])*0.0301;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 8])*0.0390;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[ 9])*0.0486;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[10])*0.0582;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[11])*0.0669;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[12])*0.0739;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[13])*0.0785;\n    gl_FragColor += texture2D(sTexture, vTextureCoord      )*0.0801;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[14])*0.0785;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[15])*0.0739;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[16])*0.0669;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[17])*0.0582;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[18])*0.0486;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[19])*0.0390;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[20])*0.0301;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[21])*0.0223;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[22])*0.0158;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[23])*0.0108;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[24])*0.0071;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[25])*0.0045;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[26])*0.0027;\n    gl_FragColor += texture2D(sTexture, v_blurTexCoords[27])*0.0016;\n}\n";
    private static final String FRAGMENT_SHADER_GRAY = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float rValue;\nuniform float gValue;\nuniform float bValue;\nvoid main() {\n    vec4 tc = texture2D(sTexture, vTextureCoord);\n    float color = tc.r * rValue + tc.g * gValue + tc.b * bValue;\n    gl_FragColor = vec4(color, color, color, 1.0);\n}\n";
    private static final String FRAGMENT_SHADER_MASK = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nuniform sampler2D mask;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       vec4 cMask = texture2D(mask, vTextureCoordOrg);\n       gl_FragColor =  step(0.5, cMask.r) * cc;\n}\n";
    private static final String FRAGMENT_SHADER_MASK_OES = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nuniform sampler2D mask;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       vec4 cMask = texture2D(mask, vTextureCoordOrg);\n       gl_FragColor =  step(0.5, cMask.r) * cc;\n}\n";
    private static final String FRAGMENT_SHADER_NONE = "precision mediump float;\nuniform sampler2D sTexture;\nvarying vec2 vTextureCoord;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       gl_FragColor = cc;\n}\n";
    private static final String FRAGMENT_SHADER_NONE_OES = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n       vec4 cc = texture2D(sTexture, vTextureCoordOrg);\n       gl_FragColor =  cc;\n}\n";
    private static final String FRAGMENT_SHADER_PERSPECTIVE = "precision highp float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float angle;//0~0.5\nvoid main()\n{\n       vec2 vTextureCoordOrg;\n       vTextureCoordOrg.x = vTextureCoord.x;\n       vTextureCoordOrg.y = vTextureCoord.y;\n  vec2 p = vTextureCoordOrg;\n  vec2 m = vec2(0.5, 0.5);\n  vec2 d = p - m;\n  float r = length(d); \n  float power = (3.141592 / (sqrt(dot(m, m)))) *(angle);\n  float bind = sqrt(dot(m, m));//stick to corners\n  vec2 uv = m + normalize(d) * tan(r * power) * bind / tan( bind * power);\n  vec3 col = texture2D(sTexture, uv).xyz;\n  gl_FragColor = vec4(col, 1.0);\n}\n";
    private static final String FRAGMENT_SHADER_VIGNETTING = "precision mediump float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform float innerRadius;\nuniform float outRadius;\nuniform float brightness;\nuniform float vignettingAlpha;\n\nvoid main() {\n    vec4 color = texture2D(sTexture, vTextureCoord);\n    color.rgb = color.rgb * brightness;\n    float dist = length(vec2(0.5, 0.5) - vTextureCoord);\n    float normal = smoothstep(outRadius, innerRadius, dist) * vignettingAlpha + (1.0 - vignettingAlpha);\n    color.rgb = mix(color.rgb, color.rgb * normal, 1.0);\n    gl_FragColor =  color;\n}\n";
    private static final int FRAMEBUFFER_NEXT_BLUR_ID = 3;
    private static final int FRAMEBUFFER_NEXT_BLUR_TEX_ID = 3;
    private static final int FRAMEBUFFER_NEXT_ID = 1;
    private static final int FRAMEBUFFER_NEXT_TEXTURE_ID = 1;
    private static final int FRAMEBUFFER_PREV_BLUR_ID = 2;
    private static final int FRAMEBUFFER_PREV_BLUR_TEX_ID = 2;
    private static final int FRAMEBUFFER_PREV_ID = 0;
    private static final int FRAMEBUFFER_PREV_TEXTURE_ID = 0;
    private static final int MASK_INPUT_TEXTURE_ID = 9;
    private static final int NORM_CAM_OES_TEXTURE_ID = 4;
    private static final int NORM_INPUT_TEXTURE_ID = 6;
    private static final int NUM_FRAMEBUFFERS = 4;
    private static final int NUM_PROGRAMS = 9;
    private static final int NUM_TEXTURES = 10;
    public static final int PAINT_PICTURE = 1;
    public static final int PAINT_PREVIEW = 0;
    public static final int PAINT_RECORD = 2;
    private static final int PROGRAM_BLUR_H = 2;
    private static final int PROGRAM_BLUR_V = 3;
    private static final int PROGRAM_GRAY = 4;
    private static final int PROGRAM_MASK = 7;
    private static final int PROGRAM_MASK_OES = 8;
    private static final int PROGRAM_NONE = 0;
    private static final int PROGRAM_NONE_OES = 1;
    private static final int PROGRAM_PERS = 5;
    private static final int PROGRAM_VIGNECT = 6;
    private static final int SIZEOF_FLOAT = 4;
    private static final String TAG = "FrameDrawer";
    private static final String VERTEX_BLUR_H_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[28];\nvoid main()\n{\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n    for( int i=0; i < 14; i++){\n        float val = float(i-14)*0.003;\n        v_blurTexCoords[ i] = vTextureCoord + vec2(val, 0.0);\n    }\n    for( int j=14; j < 28; j++){\n        float val = float(j-13)*0.003;\n        v_blurTexCoords[ j] = vTextureCoord + vec2(val, 0.0);\n    }\n}\n";
    private static final String VERTEX_BLUR_V_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvarying vec2 v_blurTexCoords[28];\nvoid main()\n{\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n    for( int i=0; i < 14; i++){\n        float val = float(i-14)*0.003;\n        v_blurTexCoords[ i] = vTextureCoord + vec2(0.0, val);\n    }\n    for( int j=14; j < 28; j++){\n        float val = float(j-13)*0.003;\n        v_blurTexCoords[ j] = vTextureCoord + vec2(0.0, val);\n    }\n}\n";
    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n";
    private static final int WIDE_CAM_OES_TEXTURE_ID = 5;
    private static final int WIDE_INPUT_TEXTURE_ID = 7;
    private float division_ratio;
    private boolean isDualCameraMode;
    private int mAngleLoc;
    private float[] mBlurKernel = new float[]{0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 7.0f, 8.0f, 9.0f, 10.0f};
    private int mCoordsPerVertex;
    private int mCurrentFrameBuf;
    private int[] mFramebufferIDs = new int[4];
    private int mGrayBValueLoc;
    private int mGrayGValueLoc;
    private int mGrayRValueLoc;
    private float[] mIdentityMatrix;
    private int mIterLoc;
    private int mLastTextureID;
    private int mPSizeXLoc;
    private int mPSizeYLoc;
    private int[] mPrograms = new int[9];
    private FloatBuffer mTexCoordArray;
    private FloatBuffer mTexCoordCropArray;
    private FloatBuffer mTexCoordDivHorArray;
    private FloatBuffer mTexCoordDivVerArray;
    private int mTexCoordStride;
    private int mTexture1LocBlur;
    private int[] mTextures = new int[10];
    private FloatBuffer mVertexArray;
    private int mVertexCount;
    private int mVertexStride;
    private int mVignettingAlphaLoc;
    private int mVignettingBrightnessLoc;
    private int mVignettingInnerRadiusLoc;
    private int mVignettingOutRadiusLoc;
    private int[] maPositionLocs = new int[9];
    private int[] maTextureCoordLocs = new int[9];
    private int[] muMVPMatrixLocs = new int[9];
    private int[] muTexMatrixLocs = new int[9];
    private float norm_to_wide_ratio_singlemode;
    private int scale_of_blur;
    private int scale_of_fbo;

    public EffectFrameDrawer(boolean isDualMode) {
        this.isDualCameraMode = isDualMode;
        this.scale_of_fbo = 2;
        this.scale_of_blur = 4;
        this.norm_to_wide_ratio_singlemode = ShaderParameterData.getInstance().getSingleModeRatio();
        this.division_ratio = ShaderParameterData.getInstance().getNormToWideRatio()[3];
        createVectorInfo();
    }

    private void createVectorInfo() {
        this.mIdentityMatrix = new float[16];
        Matrix.setIdentityM(this.mIdentityMatrix, 0);
        float[] RECTANGLE_COORDS = new float[]{-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
        float[] RECTANGLE_TEX_COORDS = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        float[] RECTANGLE_TEX_COORDS_CROP = new float[]{0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f + (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f + (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f + (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f + (this.norm_to_wide_ratio_singlemode / 2.0f), 0.5f - (this.norm_to_wide_ratio_singlemode / 2.0f)};
        float[] RECTANGLE_TEX_COORDS_DIV_VER = new float[]{0.5f - (this.division_ratio / 2.0f), 1.0f, 0.5f + (this.division_ratio / 2.0f), 1.0f, 0.5f - (this.division_ratio / 2.0f), 0.0f, 0.5f + (this.division_ratio / 2.0f), 0.0f};
        float[] RECTANGLE_TEX_COORDS_DIV_HOR = new float[]{0.0f, 0.5f + (this.division_ratio / 2.0f), 1.0f, 0.5f + (this.division_ratio / 2.0f), 0.0f, 0.5f - (this.division_ratio / 2.0f), 1.0f, 0.5f - (this.division_ratio / 2.0f)};
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
        ByteBuffer textureDivVerByteBuf = ByteBuffer.allocateDirect(RECTANGLE_TEX_COORDS_DIV_VER.length * 4);
        textureDivVerByteBuf.order(ByteOrder.nativeOrder());
        this.mTexCoordDivVerArray = textureDivVerByteBuf.asFloatBuffer();
        this.mTexCoordDivVerArray.put(RECTANGLE_TEX_COORDS_DIV_VER);
        this.mTexCoordDivVerArray.position(0);
        ByteBuffer textureDivHorByteBuf = ByteBuffer.allocateDirect(RECTANGLE_TEX_COORDS_DIV_HOR.length * 4);
        textureDivHorByteBuf.order(ByteOrder.nativeOrder());
        this.mTexCoordDivHorArray = textureDivHorByteBuf.asFloatBuffer();
        this.mTexCoordDivHorArray.put(RECTANGLE_TEX_COORDS_DIV_HOR);
        this.mTexCoordDivHorArray.position(0);
        this.mCoordsPerVertex = 2;
        this.mVertexStride = this.mCoordsPerVertex * 4;
        this.mVertexCount = RECTANGLE_COORDS.length / this.mCoordsPerVertex;
        this.mTexCoordStride = 8;
    }

    public void createPrograms() {
        this.mPrograms[1] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_NONE_OES);
        this.mPrograms[0] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_NONE);
        this.mPrograms[2] = createProgram(VERTEX_BLUR_H_SHADER, FRAGMENT_SHADER_BLUR_FAST);
        this.mPrograms[3] = createProgram(VERTEX_BLUR_V_SHADER, FRAGMENT_SHADER_BLUR_FAST);
        this.mPrograms[4] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_GRAY);
        this.mPrograms[5] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_PERSPECTIVE);
        this.mPrograms[6] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_VIGNETTING);
        this.mPrograms[7] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_MASK);
        this.mPrograms[8] = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_MASK_OES);
        Log.d(TAG, "Created Programs complete");
    }

    public void createLocations() {
        for (int programIndex = 0; programIndex < 9; programIndex++) {
            if (this.mPrograms[programIndex] == 0) {
                throw new RuntimeException("Unable to create program: " + programIndex);
            }
            this.maPositionLocs[programIndex] = GLES20.glGetAttribLocation(this.mPrograms[programIndex], "aPosition");
            this.maTextureCoordLocs[programIndex] = GLES20.glGetAttribLocation(this.mPrograms[programIndex], "aTextureCoord");
            this.muMVPMatrixLocs[programIndex] = GLES20.glGetUniformLocation(this.mPrograms[programIndex], "uMVPMatrix");
            this.muTexMatrixLocs[programIndex] = GLES20.glGetUniformLocation(this.mPrograms[programIndex], "uTexMatrix");
        }
        this.mAngleLoc = GLES20.glGetUniformLocation(this.mPrograms[5], "angle");
        this.mVignettingInnerRadiusLoc = GLES20.glGetUniformLocation(this.mPrograms[6], "innerRadius");
        this.mVignettingOutRadiusLoc = GLES20.glGetUniformLocation(this.mPrograms[6], "outRadius");
        this.mVignettingBrightnessLoc = GLES20.glGetUniformLocation(this.mPrograms[6], "brightness");
        this.mVignettingAlphaLoc = GLES20.glGetUniformLocation(this.mPrograms[6], "vignettingAlpha");
        this.mGrayRValueLoc = GLES20.glGetUniformLocation(this.mPrograms[4], "rValue");
        this.mGrayGValueLoc = GLES20.glGetUniformLocation(this.mPrograms[4], "gValue");
        this.mGrayBValueLoc = GLES20.glGetUniformLocation(this.mPrograms[4], "bValue");
    }

    public int[] createTextureObject(int width, int height) {
        Log.d(TAG, "createTextureObject");
        GLES20.glGenTextures(10, this.mTextures, 0);
        checkGlError("glGenTextures");
        if (this.isDualCameraMode) {
            bindTextureOES(4);
        }
        bindTextureOES(5);
        bindTexture2D(6);
        bindTexture2D(7);
        bindTexture2D(8);
        bindTexture2D(9);
        GLES20.glGenFramebuffers(4, this.mFramebufferIDs, 0);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[0]);
        bindTextureFBO(0, width / this.scale_of_fbo, height / this.scale_of_fbo);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[1]);
        bindTextureFBO(1, width / this.scale_of_fbo, height / this.scale_of_fbo);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[2]);
        bindTextureFBO(2, (width / this.scale_of_fbo) / this.scale_of_blur, (height / this.scale_of_fbo) / this.scale_of_blur);
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[3]);
        bindTextureFBO(3, (width / this.scale_of_fbo) / this.scale_of_blur, (height / this.scale_of_fbo) / this.scale_of_blur);
        return new int[]{this.mTextures[4], this.mTextures[5]};
    }

    public void release() {
        Log.d(TAG, "deleting programs ");
        for (int programIndex = 0; programIndex < 9; programIndex++) {
            GLES20.glDeleteProgram(this.mPrograms[programIndex]);
            this.mPrograms[programIndex] = -1;
        }
        Log.d(TAG, "deleting textures..");
        GLES20.glDeleteTextures(10, this.mTextures, 0);
        Log.d(TAG, "deleting FBOs..");
        GLES20.glDeleteFramebuffers(4, this.mFramebufferIDs, 0);
    }

    public Bitmap draw(float[] mvpMatrix, float[] bMatrix, int mode, int frameType, int width, int height, boolean isPicture) {
        return draw(mvpMatrix, bMatrix, mode, frameType, width, height, isPicture, false);
    }

    public synchronized Bitmap draw(float[] mvpMatrix, float[] bMatrix, int mode, int frameType, int width, int height, boolean isPicture, boolean isRec) {
        Bitmap bmp;
        int i;
        int filterWidth = width / this.scale_of_fbo;
        int filterHeight = height / this.scale_of_fbo;
        float f = (float) width;
        float[] normToWideRatio = ShaderParameterData.getInstance().getNormToWideRatio();
        if (frameType == 4) {
            i = 3;
        } else {
            i = frameType;
        }
        int innerFrameWidth = (int) (normToWideRatio[i] * f);
        f = (float) height;
        normToWideRatio = ShaderParameterData.getInstance().getNormToWideRatio();
        if (frameType == 4) {
            i = 3;
        } else {
            i = frameType;
        }
        int innerFrameHeight = (int) (normToWideRatio[i] * f);
        int innerFrameLeft = (int) (((float) (width - innerFrameWidth)) * 0.5f);
        int innerFrameTop = (int) (((float) (height - innerFrameHeight)) * 0.5f);
        this.mCurrentFrameBuf = 0;
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        checkGlError("blend");
        if (isPicture) {
            bindFBOAndTexture(7, false);
            drawTexture(0, null, 0, 0, filterWidth, filterHeight);
        } else {
            bindFBOAndTexture(5, true);
            drawTexture(1, null, 0, 0, filterWidth, filterHeight);
        }
        this.mCurrentFrameBuf = 1;
        if (isCheckedMode(mode, 4)) {
            bindFBOAndTexture();
            drawTexture(4, null, 0, 0, filterWidth, filterHeight);
        }
        if (isCheckedMode(mode, 1)) {
            long start = System.nanoTime();
            this.mCurrentFrameBuf = 2;
            bindFBOAndTexture(this.mLastTextureID, false);
            drawTexture(2, null, 0, 0, filterWidth / this.scale_of_blur, filterHeight / this.scale_of_blur);
            this.mCurrentFrameBuf = 3;
            bindFBOAndTexture();
            drawTexture(3, null, 0, 0, filterWidth / this.scale_of_blur, filterHeight / this.scale_of_blur);
            this.mCurrentFrameBuf = 0;
            bindFBOAndTexture(this.mLastTextureID, false);
            drawTexture(0, null, 0, 0, filterWidth, filterHeight);
            this.mCurrentFrameBuf = 1;
            float nanoTime = ((float) (System.nanoTime() - start)) / 1000000.0f;
        }
        if (isCheckedMode(mode, 2)) {
            bindFBOAndTexture();
            drawTexture(5, null, 0, 0, filterWidth, filterHeight);
        }
        if (isCheckedMode(mode, 8)) {
            bindFBOAndTexture();
            drawTexture(6, null, 0, 0, filterWidth, filterHeight);
        }
        bindRenderBufferAndTexture();
        drawTexture(0, mvpMatrix, 0, 0, width, height);
        checkGlError("draw to render");
        GLES20.glActiveTexture(33984);
        int iWidth = (int) (((float) width) * this.division_ratio);
        int iHeight = (int) (((float) height) * this.division_ratio);
        int iTop = (int) (((float) (height - iHeight)) * 0.5f);
        int iLeft = (int) (((float) (width - iWidth)) * 0.5f);
        if (isPicture) {
            if (this.isDualCameraMode) {
                GLES20.glBindTexture(3553, this.mTextures[6]);
            } else {
                GLES20.glBindTexture(3553, this.mTextures[7]);
            }
            if (frameType == 3) {
                drawTexture(7, mvpMatrix, iLeft, 0, iWidth, height, this.isDualCameraMode, 3, false);
            } else if (frameType == 4) {
                drawTexture(7, mvpMatrix, 0, iTop, width, iHeight, this.isDualCameraMode, 4, false);
            } else {
                drawTexture(7, mvpMatrix, innerFrameLeft, innerFrameTop, innerFrameWidth, innerFrameHeight, this.isDualCameraMode);
            }
        } else {
            if (this.isDualCameraMode) {
                GLES20.glBindTexture(36197, this.mTextures[4]);
            } else {
                GLES20.glBindTexture(36197, this.mTextures[5]);
            }
            if ((frameType == 3 && !isRec) || (frameType == 4 && isRec)) {
                drawTexture(8, mvpMatrix, 0, iTop, width, iHeight, this.isDualCameraMode, 3, isRec);
            } else if (!(frameType == 3 && isRec) && (frameType != 4 || isRec)) {
                drawTexture(8, mvpMatrix, innerFrameLeft, innerFrameTop, innerFrameWidth, innerFrameHeight, this.isDualCameraMode);
            } else {
                drawTexture(8, mvpMatrix, iLeft, 0, iWidth, height, this.isDualCameraMode, 4, isRec);
            }
        }
        checkGlError("normal");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.mTextures[8]);
        if (isPicture) {
            drawTexture(0, null, 0, 0, width, height);
        } else {
            float[] rotateMatrix = (float[]) mvpMatrix.clone();
            Matrix.rotateM(rotateMatrix, 0, -90.0f, 0.0f, 0.0f, 1.0f);
            drawTexture(0, rotateMatrix, 0, 0, width, height);
        }
        checkGlError("boundary");
        GLES20.glDisable(3042);
        if (isPicture) {
            Buffer buf = ByteBuffer.allocateDirect((width * height) * 4);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            GLES20.glReadPixels(0, 0, width, height, 6408, 5121, buf);
            checkGlError("glReadPixels");
            bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
        } else {
            bmp = null;
        }
        return bmp;
    }

    private void drawTexture(int programID, float[] mvpMatrix, int x, int y, int width, int height) {
        drawTexture(programID, mvpMatrix, x, y, width, height, 0, true, -1, false);
    }

    private void drawTexture(int programID, float[] mvpMatrix, int x, int y, int width, int height, boolean isDualMode) {
        drawTexture(programID, mvpMatrix, x, y, width, height, 0, isDualMode, -1, false);
    }

    private void drawTexture(int programID, float[] mvpMatrix, int x, int y, int width, int height, boolean isDualMode, int divType, boolean isRec) {
        drawTexture(programID, mvpMatrix, x, y, width, height, 0, isDualMode, divType, isRec);
    }

    private void drawTexture(int programID, float[] mvpMatrix, int x, int y, int width, int height, int blurIter, boolean isDualMode, int divType, boolean isRec) {
        if (mvpMatrix == null) {
            mvpMatrix = new float[16];
            Matrix.orthoM(mvpMatrix, 0, 0.0f, (float) width, 0.0f, (float) height, -1.0f, 1.0f);
            Matrix.translateM(mvpMatrix, 0, ((float) width) / 2.0f, ((float) height) / 2.0f, 0.0f);
            Matrix.scaleM(mvpMatrix, 0, (float) width, (float) (-height), 1.0f);
        }
        GLES20.glUseProgram(this.mPrograms[programID]);
        GLES20.glViewport(x, y, width, height);
        checkGlError("glUseProgram : " + programID);
        setVertexParam(programID, width, height, mvpMatrix, isDualMode, divType, isRec);
        setTextureParam(programID, width, height, blurIter);
        checkGlError("glParams : " + programID);
        GLES20.glDrawArrays(5, 0, this.mVertexCount);
        GLES20.glDisableVertexAttribArray(this.maPositionLocs[programID]);
        GLES20.glDisableVertexAttribArray(this.maTextureCoordLocs[programID]);
        GLES20.glBindFramebuffer(36160, 0);
        GLES20.glBindRenderbuffer(36161, 0);
        GLES20.glBindTexture(3553, 0);
        GLES20.glUseProgram(0);
    }

    private void setTextureParam(int programID, int width, int height, int blurIter) {
        int loc;
        ShaderParameterData paramData = ShaderParameterData.getInstance();
        switch (programID) {
            case 4:
                GLES20.glUniform1f(this.mGrayRValueLoc, paramData.getGrayValue()[0]);
                GLES20.glUniform1f(this.mGrayGValueLoc, paramData.getGrayValue()[1]);
                GLES20.glUniform1f(this.mGrayBValueLoc, paramData.getGrayValue()[2]);
                break;
            case 5:
                GLES20.glUniform1f(this.mAngleLoc, paramData.getPerspectiveValue()[0]);
                break;
            case 6:
                GLES20.glUniform1f(this.mVignettingInnerRadiusLoc, paramData.getVignettingValue()[0]);
                GLES20.glUniform1f(this.mVignettingOutRadiusLoc, paramData.getVignettingValue()[1]);
                GLES20.glUniform1f(this.mVignettingBrightnessLoc, paramData.getVignettingValue()[2]);
                GLES20.glUniform1f(this.mVignettingAlphaLoc, paramData.getVignettingValue()[3]);
                break;
            case 7:
                loc = GLES20.glGetUniformLocation(this.mPrograms[programID], "mask");
                GLES20.glActiveTexture(33985);
                GLES20.glBindTexture(3553, this.mTextures[9]);
                GLES20.glUniform1i(loc, 1);
                break;
            case 8:
                break;
        }
        loc = GLES20.glGetUniformLocation(this.mPrograms[programID], "mask");
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this.mTextures[9]);
        GLES20.glUniform1i(loc, 1);
        checkGlError("glUniform1f");
    }

    private void setVertexParam(int programID, int width, int height, float[] mvpMatrix, boolean isDualMode, int divType, boolean isRec) {
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLocs[programID], 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLocs[programID], 1, false, this.mIdentityMatrix, 0);
        GLES20.glEnableVertexAttribArray(this.maPositionLocs[programID]);
        GLES20.glVertexAttribPointer(this.maPositionLocs[programID], this.mCoordsPerVertex, 5126, false, this.mVertexStride, this.mVertexArray);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLocs[programID]);
        FloatBuffer texCoordArray;
        if ((divType == 3 && !isRec) || (divType == 4 && isRec)) {
            texCoordArray = this.mTexCoordDivVerArray;
        } else if ((divType == 3 && isRec) || (divType == 4 && !isRec)) {
            texCoordArray = this.mTexCoordDivHorArray;
        } else if (isDualMode) {
            texCoordArray = this.mTexCoordArray;
        } else {
            texCoordArray = this.mTexCoordCropArray;
        }
        GLES20.glVertexAttribPointer(this.maTextureCoordLocs[programID], 2, 5126, false, this.mTexCoordStride, this.mTexCoordCropArray);
    }

    public void setWideTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[7]);
        GLUtils.texImage2D(3553, 0, data, 0);
        checkGlError("setWideTexture");
    }

    public void setNormalTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[6]);
        GLUtils.texImage2D(3553, 0, data, 0);
        checkGlError("setNormalTexture");
    }

    public void setBoundaryTexture(Bitmap data) {
        GLES20.glBindTexture(3553, this.mTextures[8]);
        GLUtils.texImage2D(3553, 0, data, 0);
        checkGlError("setBoundaryTexture");
    }

    public void setMaskTexture(Bitmap mask) {
        GLES20.glBindTexture(3553, this.mTextures[9]);
        GLUtils.texImage2D(3553, 0, mask, 0);
        checkGlError("setMaskTexutre");
    }

    private boolean isCheckedMode(int mode, int type) {
        return (mode & type) > 0;
    }

    private void bindTextureOES(int textureID) {
        GLES20.glBindTexture(36197, this.mTextures[textureID]);
        GLES20.glTexParameterf(36197, 10241, 9729.0f);
        GLES20.glTexParameterf(36197, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729.0f);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        checkGlError("bindTexturesOES");
    }

    private void bindTextureFBO(int textureID, int width, int height) {
        bindTexture2D(textureID);
        GLES20.glGenerateMipmap(3553);
        GLES20.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, null);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.mTextures[textureID], 0);
        checkGlError("bindTexturesFrameBuffer");
    }

    private void bindTexture2D(int textureID) {
        GLES20.glBindTexture(3553, this.mTextures[textureID]);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, Constant.PHOTO_EFFECT_ID_GRAYNEGATIVE, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("bindTextures2D");
    }

    private void bindFBOAndTexture() {
        switch (this.mCurrentFrameBuf) {
            case 0:
                bindFBOAndTexture(1, false);
                this.mCurrentFrameBuf = 1;
                return;
            case 1:
                bindFBOAndTexture(0, false);
                this.mCurrentFrameBuf = 0;
                return;
            case 2:
                bindFBOAndTexture(3, false);
                this.mCurrentFrameBuf = 3;
                return;
            case 3:
                bindFBOAndTexture(2, false);
                this.mCurrentFrameBuf = 2;
                return;
            default:
                return;
        }
    }

    private void bindFBOAndTexture(int textureID, boolean isOES) {
        GLES20.glBindFramebuffer(36160, this.mFramebufferIDs[this.mCurrentFrameBuf]);
        GLES20.glActiveTexture(33984);
        if (isOES) {
            GLES20.glBindTexture(36197, 0);
            GLES20.glBindTexture(36197, this.mTextures[textureID]);
        } else {
            GLES20.glBindTexture(3553, this.mTextures[textureID]);
        }
        this.mLastTextureID = this.mCurrentFrameBuf;
    }

    private void bindRenderBufferAndTexture() {
        GLES20.glActiveTexture(33984);
        if (this.mCurrentFrameBuf == 0) {
            GLES20.glBindTexture(3553, this.mTextures[1]);
        } else {
            GLES20.glBindTexture(3553, this.mTextures[0]);
        }
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
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

    private static int loadShader(int shaderType, String source) {
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

    private static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            new StringBuilder(String.valueOf(op)).append(": glError 0x").append(Integer.toHexString(error)).toString();
        }
    }

    private static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }
}
