package com.lge.camera.components;

public interface DrumControllerListener {
    boolean isAvailableToMoveDrum();

    void onDrumScrollReleased(DrumItem drumItem);

    void onItemSelected(DrumItem drumItem, boolean z);

    void playDrumEffectSound();
}
