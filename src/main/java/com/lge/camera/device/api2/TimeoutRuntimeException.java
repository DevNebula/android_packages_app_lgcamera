package com.lge.camera.device.api2;

public class TimeoutRuntimeException extends RuntimeException {
    public TimeoutRuntimeException(String message) {
        super(message);
    }

    public TimeoutRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
