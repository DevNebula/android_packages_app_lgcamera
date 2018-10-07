package com.lge.camera.managers;

public class IntervalShotManagerIF extends ManagerInterfaceImpl {
    public IntervalShotManagerIF(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void showIntervalshotLayout() {
    }

    public void hideIntervalshotLayout() {
    }

    public void updateThumbnail(int index, byte[] data, boolean setFlip) {
    }

    public void updateThumbnail(int index, byte[] data, int degree, float ratio) {
    }

    public void updateTimer(int time, int index) {
    }

    public int getIntervalshotVisibiity() {
        return 4;
    }

    public void showIntervalshotEnteringGuide() {
    }

    public void hideIntervalshotEnteringGuide() {
    }

    public void startWatingUI(int index) {
    }

    public void stopWaitingUI() {
    }

    public void setGuideTextLayoutParam() {
    }

    public void moveThumbnailImage(int from, int to) {
    }

    public int getIntervalShotState() {
        return 0;
    }

    public void setIntervalShotState(int state) {
    }
}
