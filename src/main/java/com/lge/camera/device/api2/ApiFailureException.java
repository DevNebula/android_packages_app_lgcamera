package com.lge.camera.device.api2;

class ApiFailureException extends Exception {
    public ApiFailureException(Throwable cause) {
        super(cause);
    }

    public ApiFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiFailureException(String message) {
        super(message);
    }
}
