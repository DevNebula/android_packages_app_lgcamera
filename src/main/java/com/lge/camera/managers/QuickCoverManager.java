package com.lge.camera.managers;

public class QuickCoverManager extends ManagerInterfaceImpl {
    protected ModuleInterface mGet = null;
    private int mQuickCoverPage = 0;
    private int mQuickCoverRatio = 0;
    private int mQuickCoverState = 0;

    public QuickCoverManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void setQuickCoverState(int state) {
        this.mQuickCoverState = state;
    }

    public int getQuickCoverState() {
        return this.mQuickCoverState;
    }

    public void setQuickCoverPage(int page) {
        this.mQuickCoverPage = page;
    }

    public int getQuickCoverPage() {
        return this.mQuickCoverPage;
    }

    public void setQuickCoverRatio(int ratio) {
        this.mQuickCoverRatio = ratio;
    }

    public int getQuickCoverRatio() {
        return this.mQuickCoverRatio;
    }
}
