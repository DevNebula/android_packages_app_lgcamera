package com.arcsoft.stickerlibrary.api;

import java.nio.ByteBuffer;

public interface StickerCaptureCallback {
    int GetCaptureResult(ByteBuffer byteBuffer);
}
