package com.lge.camera.solution;

import android.hardware.camera2.TotalCaptureResult;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;

public class SolutionPickResult {
    private int mEnabledCount;
    private int[] mEnabledSolutionList = new int[12];
    private StringBuffer mEnabledSolutionNames = new StringBuffer();
    private int mEnabledSolutions = 0;
    private int mFrameCount = 1;
    private boolean mIsSupportedFastShot = true;
    private boolean mLowLightDetect = false;
    private TotalCaptureResult mMeta = null;

    public void enableSolution(int solutionType, int maxFrameCount, String name, boolean isSupportedFastShot) {
        if (!isEnabledSolution(solutionType)) {
            if (this.mEnabledCount < 12) {
                this.mEnabledSolutionList[this.mEnabledCount] = solutionType;
                this.mEnabledSolutionNames.append("-");
                this.mEnabledSolutionNames.append(name);
            }
            this.mEnabledSolutions |= solutionType;
            this.mEnabledCount++;
            if (this.mFrameCount < maxFrameCount) {
                this.mFrameCount = maxFrameCount;
            }
            this.mIsSupportedFastShot &= isSupportedFastShot;
        }
    }

    public int getEnabledSolutions() {
        return this.mEnabledSolutions;
    }

    public boolean isEnabledSolution() {
        return this.mEnabledSolutions != 0;
    }

    public boolean isEnabledSolution(int solutionType) {
        return (this.mEnabledSolutions & solutionType) == solutionType;
    }

    public boolean isEnabledNightSolution() {
        return isEnabledSolution(8) || isEnabledSolution(1024);
    }

    public boolean isEnabledHDRSolution() {
        if (isEnabledSolution(4)) {
            return true;
        }
        boolean isEnabledHDRSolution = false;
        if (this.mMeta != null) {
            Integer backlightStatus = null;
            try {
                backlightStatus = (Integer) this.mMeta.get(ParamConstants.KEY_BACK_LIGHT_DETECTION);
            } catch (IllegalArgumentException e) {
                if (CamLog.isTagExceptionLogOn()) {
                    e.printStackTrace();
                }
            }
            if (backlightStatus != null) {
                isEnabledHDRSolution = Integer.compare(1, backlightStatus.intValue()) == 0;
            }
        }
        return isEnabledHDRSolution;
    }

    public int getFrameCount() {
        return this.mFrameCount;
    }

    public TotalCaptureResult getMeta() {
        return this.mMeta;
    }

    public void setMeta(TotalCaptureResult meta) {
        this.mMeta = meta;
    }

    public void reset() {
        this.mEnabledSolutions = 0;
        this.mEnabledCount = 0;
        this.mFrameCount = 0;
        this.mEnabledSolutionList = new int[12];
        this.mEnabledSolutionNames.setLength(0);
        this.mMeta = null;
        this.mIsSupportedFastShot = true;
    }

    public boolean isSupportedFastShot() {
        return this.mIsSupportedFastShot;
    }

    public int[] getEnabledSolutionList() {
        return this.mEnabledSolutionList;
    }

    public int getEnabledCount() {
        return this.mEnabledCount;
    }

    public String getEnabledSolutionName() {
        return this.mEnabledSolutionNames.toString();
    }
}
