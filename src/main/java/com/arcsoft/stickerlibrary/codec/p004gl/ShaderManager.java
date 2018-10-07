package com.arcsoft.stickerlibrary.codec.p004gl;

import android.opengl.GLES20;
import com.arcsoft.stickerlibrary.codec.CodecLog;

/* renamed from: com.arcsoft.stickerlibrary.codec.gl.ShaderManager */
public class ShaderManager {
    private static final String TAG = "Arc_ShaderManager";

    public static int createProgram(String vertexShaderSource, String fragShaderSource) {
        int program = GLES20.glCreateProgram();
        if (program == 0) {
            CodecLog.m42e(TAG, "create program error ,error=" + GLES20.glGetError());
            return 0;
        }
        int vertexShader = ShaderManager.createShader(35633, vertexShaderSource);
        int fragShader = ShaderManager.createShader(35632, fragShaderSource);
        if (vertexShader == 0 || fragShader == 0) {
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragShader);
            GLES20.glDeleteProgram(program);
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragShader);
        GLES20.glLinkProgram(program);
        int[] status = new int[1];
        GLES20.glGetProgramiv(program, 35714, status, 0);
        if (status[0] != 0) {
            return program;
        }
        CodecLog.m42e(TAG, "createProgram error : " + GLES20.glGetProgramInfoLog(program));
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragShader);
        GLES20.glDeleteProgram(program);
        return 0;
    }

    private static int createShader(int type, String shaderSource) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            CodecLog.m42e(TAG, "create shader error, shader type=" + type + " , error=" + GLES20.glGetError());
            return 0;
        }
        GLES20.glShaderSource(shader, shaderSource);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, 35713, status, 0);
        if (status[0] != 0) {
            return shader;
        }
        CodecLog.m42e(TAG, "createShader error: " + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }
}
