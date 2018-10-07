package com.lge.camera.managers.ext;

import com.lge.camera.managers.ModuleInterface;

public interface CinemaFilterInterface extends ModuleInterface {
    String getAssistantStringFlag(String str, String str2);

    boolean isRecordingState();

    boolean isVoiceAssistantSpecified();

    void setCinemaQuickButtonIcon();
}
