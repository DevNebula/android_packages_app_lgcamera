package com.lge.camera.managers;

import android.graphics.ColorFilter;
import android.graphics.RectF;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.components.CineZoomBar;
import com.lge.camera.components.CineZoomBarDisabled;
import com.lge.camera.components.CineZoomGuideView;
import com.lge.camera.components.CineZoomView;
import com.lge.camera.components.RotateImageButton;
import com.lge.camera.components.RotateLayout;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.device.ParamConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.Utils;
import com.lge.hardware.LGCamera.CineZoomData;

public class CineZoomManagerBase extends ManagerInterfaceImpl {
    protected final int CZ_CMD_CROP = 0;
    protected final int CZ_CMD_ZOOM_IN = 1;
    protected final int CZ_CMD_ZOOM_OUT = 2;
    protected final int CZ_CMD_ZOOM_PAUSE = 3;
    protected final int CZ_CMD_ZOOM_RESET = 4;
    protected final int CZ_DIRECTION_ZOOM_IN = 0;
    protected final int CZ_DIRECTION_ZOOM_OUT = 1;
    protected final int CZ_STATUS_IDLE = 0;
    protected final int CZ_STATUS_IN = 1;
    protected final int CZ_STATUS_IN_DONE = 2;
    protected final int CZ_STATUS_OUT = 3;
    protected final int CZ_STATUS_OUT_DONE = 4;
    protected final int CZ_STATUS_PAUSE = 5;
    protected final int SPEED_X1 = 300;
    protected final int SPEED_X2 = 200;
    protected final int SPEED_X3 = 100;
    protected ImageView mArrowBtn;
    protected LinearLayout mCZchildLayout;
    protected CameraProxy mCameraDevice;
    protected CineZoomBar mCineJogZoomBar;
    protected CineZoomBarDisabled mCineJogZoomBarDisabled;
    protected RelativeLayout mCineJogZoomLayout;
    protected LinearLayout mCineJogZoomSpeedBar;
    protected View mCineModeBaseView;
    protected boolean mCineZoomBarEnabled = true;
    protected RotateImageButton mCineZoomButton;
    protected CineZoomGuideView mCineZoomGuideView;
    protected LinearLayout mCineZoomLayout;
    protected CineZoomView mCineZoomView;
    protected int mDirection = 0;
    protected TextView mGuideTextView;
    protected boolean mIsCineZoomJogBarTouching = false;
    protected RotateImageButton mMinusButton;
    protected RotateImageButton mPlayPauseButton;
    protected RotateImageButton mPlusButton;
    protected boolean mShutterClickedRecordingNotStarted = false;
    protected RotateImageButton mSpeedButton;
    protected int mStatus = 0;
    protected RotateTextView mTV1;
    protected RotateTextView mTV2;
    protected RotateTextView mTV3;
    protected RotateTextView mTV4;
    protected RotateTextView mTV5;
    protected RotateTextView mTV6;
    protected RotateTextView mTV7;
    protected boolean mWasCineZoomBefoerRecordingPause = false;
    protected RotateImageButton mZoomInOutButton;
    protected int mZoomSpeed = 200;
    final String pause = this.mGet.getAppContext().getString(C0088R.string.camera_cz_pause);
    final String start = this.mGet.getAppContext().getString(C0088R.string.camera_cz_start);

    public CineZoomManagerBase(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        this.mCameraDevice = this.mGet.getCameraDevice();
    }

    public void onResumeBefore() {
        super.onResumeBefore();
        if (this.mCameraDevice == null) {
            this.mCameraDevice = this.mGet.getCameraDevice();
        }
    }

    protected void setCineZoom(int command) {
        CamLog.m3d(CameraConstants.TAG, "CineZoom setCineZoom command : " + command);
        if (this.mCameraDevice == null) {
            CamLog.m3d(CameraConstants.TAG, "CineZoom setCineZoom cameraDevice is null");
        } else if (FunctionProperties.getSupportedHal() != 2 && this.mCameraDevice.getCamera() == null) {
            CamLog.m3d(CameraConstants.TAG, "CineZoom setCineZoom cameraDevice getCamera is null");
        } else if (this.mGet != null && this.mCineZoomGuideView != null) {
            if (command == 1) {
                this.mStatus = 1;
                this.mDirection = 0;
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (CineZoomManagerBase.this.mCineZoomView != null && CineZoomManagerBase.this.mPlayPauseButton != null) {
                            CineZoomManagerBase.this.mCineZoomView.setIsPlaying(true);
                            CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.pause);
                            CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(1);
                        }
                    }
                }, 0);
            } else if (command == 2) {
                this.mStatus = 3;
                this.mDirection = 1;
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (CineZoomManagerBase.this.mCineZoomView != null && CineZoomManagerBase.this.mPlayPauseButton != null) {
                            CineZoomManagerBase.this.mCineZoomView.setIsPlaying(true);
                            CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.pause);
                            CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(1);
                        }
                    }
                }, 0);
            } else if (command == 4) {
                this.mStatus = 0;
                this.mDirection = 0;
                String toastMsg = this.mGet.getAppContext().getString(C0088R.string.camera_cz_point_zoom_off);
                if (toastMsg != null) {
                    this.mGet.showToastConstant(toastMsg);
                }
            } else if (command == 3) {
                if (FunctionProperties.getSupportedHal() == 2 && this.mStatus != 0) {
                    this.mStatus = 5;
                }
                this.mGet.postOnUiThread(new HandlerRunnable(this) {
                    public void handleRun() {
                        if (CineZoomManagerBase.this.mCineZoomView != null && CineZoomManagerBase.this.mPlayPauseButton != null) {
                            CineZoomManagerBase.this.mCineZoomView.setIsPlaying(false);
                            CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.start);
                            CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(0);
                        }
                    }
                }, 0);
            }
            RectF guideRectF = this.mCineZoomGuideView.getGuideRectF();
            if (this.mGet != null && this.mGet.getCameraDevice() != null) {
                CameraParameters param = this.mGet.getCameraDevice().getParameters();
                if (param != null && guideRectF != null) {
                    if (FunctionProperties.getSupportedHal() == 2) {
                        param.set(ParamConstants.KEY_POINT_ZOOM, Integer.toString(command) + " " + Integer.toString(this.mZoomSpeed) + " " + Integer.toString((int) guideRectF.left) + " " + Integer.toString((int) guideRectF.top) + " " + Integer.toString((int) guideRectF.right) + " " + Integer.toString((int) guideRectF.bottom));
                        this.mGet.getCameraDevice().setParameters(param);
                        return;
                    }
                    this.mCameraDevice.setCineZoom(this.mCameraDevice.getParameters(), command, this.mZoomSpeed, guideRectF);
                }
            }
        }
    }

    public void onCineZoomHAL3(int status) {
        CamLog.m3d(CameraConstants.TAG, "[CineZoom] onCineZoomHAL3 status : " + status);
        if (this.mStatus != 0) {
            if (status == 2) {
                onZoomInDone();
            } else if (status == 4) {
                onZoomOutDone();
            } else {
                onCallbackRect();
            }
        }
    }

    public void onCineZoom(CineZoomData cineZoomData) {
        CamLog.m3d(CameraConstants.TAG, "[CineZoom] onCineZoom onCineZoom mStatus : " + this.mStatus);
        if (this.mStatus != 0 && cineZoomData != null) {
            CamLog.m3d(CameraConstants.TAG, "[CineZoom] onCineZoom onCineZoom state : " + cineZoomData.state);
            if (cineZoomData.state == 1) {
                onZoomInDone();
            } else if (cineZoomData.state == 2) {
                onZoomOutDone();
            } else if (cineZoomData.state == 3) {
                onCallbackRect(cineZoomData);
            }
        }
    }

    private void onZoomInDone() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            this.mDirection = 1;
            this.mGet.postOnUiThread(new HandlerRunnable(this) {
                public void handleRun() {
                    if (CineZoomManagerBase.this.mCineZoomView != null && CineZoomManagerBase.this.mPlayPauseButton != null && CineZoomManagerBase.this.mZoomInOutButton != null) {
                        CineZoomManagerBase.this.mCineZoomView.setIsPlaying(false);
                        CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.start);
                        CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(0);
                        CineZoomManagerBase.this.mZoomInOutButton.setText(CineZoomManagerBase.this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_out));
                        CineZoomManagerBase.this.mZoomInOutButton.setImageLevel(1);
                        CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mZoomInOutButton, false);
                    }
                }
            }, 0);
        }
    }

    private void onZoomOutDone() {
        this.mStatus = 0;
        this.mDirection = 0;
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CineZoomManagerBase.this.mPlayPauseButton != null && CineZoomManagerBase.this.mZoomInOutButton != null && CineZoomManagerBase.this.mCineZoomView != null && CineZoomManagerBase.this.mCineZoomGuideView != null && CineZoomManagerBase.this.mGuideTextView != null) {
                    CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.start);
                    CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(0);
                    CineZoomManagerBase.this.mZoomInOutButton.setText(CineZoomManagerBase.this.mGet.getAppContext().getString(C0088R.string.camera_cz_zoom_in));
                    CineZoomManagerBase.this.mZoomInOutButton.setImageLevel(0);
                    CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mZoomInOutButton, false);
                    CineZoomManagerBase.this.mCineZoomView.setVisibility(8);
                    CineZoomManagerBase.this.mCineZoomGuideView.setVisibility(0);
                    CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mSpeedButton, true);
                    CineZoomManagerBase.this.mGuideTextView.setText(C0088R.string.camera_cz_guide_zoom_start);
                    CineZoomManagerBase.this.mGuideTextView.setText(C0088R.string.camera_cz_tap_area_drag_slider1);
                    CineZoomManagerBase.this.setCinemaModeGuideViewVisibility(true);
                }
            }
        }, 0);
    }

    private void onCallbackRect(CineZoomData cineZoomData) {
        final int left = (int) (((float) ((this.mCineZoomGuideView.CAM_DEVICE_WIDTH - cineZoomData.top) - cineZoomData.height)) / this.mCineZoomGuideView.mRATIO_W);
        final int top = (int) (((float) cineZoomData.left) / this.mCineZoomGuideView.mRATIO_H);
        final int right = (int) (((float) (this.mCineZoomGuideView.CAM_DEVICE_WIDTH - cineZoomData.top)) / this.mCineZoomGuideView.mRATIO_W);
        final int bottom = (int) (((float) (cineZoomData.left + cineZoomData.width)) / this.mCineZoomGuideView.mRATIO_H);
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CineZoomManagerBase.this.mPlayPauseButton != null && CineZoomManagerBase.this.mCineZoomGuideView != null && CineZoomManagerBase.this.mCineZoomView != null) {
                    if (CineZoomManagerBase.this.mShutterClickedRecordingNotStarted) {
                        CineZoomManagerBase.this.mShutterClickedRecordingNotStarted = false;
                        CineZoomManagerBase.this.mPlayPauseButton.setText(CineZoomManagerBase.this.pause);
                        CineZoomManagerBase.this.mPlayPauseButton.setImageLevel(1);
                        CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mZoomInOutButton, true);
                        CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mPlayPauseButton, true);
                        CineZoomManagerBase.this.setBtnEnabled(CineZoomManagerBase.this.mSpeedButton, true);
                    }
                    if (CineZoomManagerBase.this.mStatus == 1) {
                        CineZoomManagerBase.this.mCineZoomGuideView.setVisibility(8);
                        CineZoomManagerBase.this.setCinemaModeGuideViewVisibility(false);
                        CineZoomManagerBase.this.mCineZoomView.setBounds(left, top, right, bottom);
                        CineZoomManagerBase.this.mCineZoomView.setVisibility(0);
                        CineZoomManagerBase.this.mCineZoomView.invalidate();
                    }
                }
            }
        }, 0);
    }

    private void onCallbackRect() {
        this.mGet.postOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (CineZoomManagerBase.this.mPlayPauseButton != null && CineZoomManagerBase.this.mCineZoomGuideView != null && CineZoomManagerBase.this.mCineZoomView != null) {
                    if (CineZoomManagerBase.this.mShutterClickedRecordingNotStarted) {
                        CineZoomManagerBase.this.mShutterClickedRecordingNotStarted = false;
                    }
                    if (CineZoomManagerBase.this.mStatus == 1) {
                        CineZoomManagerBase.this.mCineZoomGuideView.setVisibility(8);
                        CineZoomManagerBase.this.setCinemaModeGuideViewVisibility(false);
                    }
                }
            }
        }, 0);
    }

    protected void setBtnEnabled(RotateImageButton btn, boolean enabled) {
        if (btn != null) {
            ColorFilter colorFilter;
            if (enabled) {
                colorFilter = ColorUtil.getNormalColorByAlpha();
            } else {
                colorFilter = ColorUtil.getDimColorByAlpha();
            }
            btn.setEnabled(enabled);
            btn.setColorFilter(colorFilter);
        }
    }

    protected void setCineZoomBarEnabled(boolean enabled) {
        int i = 8;
        if (this.mCineJogZoomBar != null && this.mCineJogZoomBarDisabled != null && this.mPlusButton != null && this.mMinusButton != null && this.mTV1 != null && this.mTV2 != null && this.mTV3 != null && this.mTV5 != null && this.mTV6 != null && this.mTV7 != null) {
            int i2;
            ColorFilter colorFilter;
            CineZoomBar cineZoomBar = this.mCineJogZoomBar;
            if (enabled) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            cineZoomBar.setVisibility(i2);
            CineZoomBarDisabled cineZoomBarDisabled = this.mCineJogZoomBarDisabled;
            if (!enabled) {
                i = 0;
            }
            cineZoomBarDisabled.setVisibility(i);
            this.mPlusButton.setEnabled(enabled);
            this.mMinusButton.setEnabled(enabled);
            this.mTV1.setEnabled(enabled);
            this.mTV2.setEnabled(enabled);
            this.mTV3.setEnabled(enabled);
            this.mTV5.setEnabled(enabled);
            this.mTV6.setEnabled(enabled);
            this.mTV7.setEnabled(enabled);
            if (enabled) {
                colorFilter = ColorUtil.getNormalColorByAlpha();
            } else {
                colorFilter = ColorUtil.getDimColorByAlpha();
            }
            this.mPlusButton.setColorFilter(colorFilter);
            this.mMinusButton.setColorFilter(colorFilter);
            this.mTV1.setColorFilter(colorFilter);
            this.mTV2.setColorFilter(colorFilter);
            this.mTV3.setColorFilter(colorFilter);
            this.mTV5.setColorFilter(colorFilter);
            this.mTV6.setColorFilter(colorFilter);
            this.mTV7.setColorFilter(colorFilter);
            this.mCineZoomBarEnabled = enabled;
        }
    }

    protected void setCinemaModeGuideViewVisibility(boolean show) {
        if (this.mCineModeBaseView != null) {
            updateCinemaModeGuideViewDegree(getOrientationDegree());
            int visibility = show ? 0 : 8;
            if (this.mGuideTextView != null) {
                this.mGuideTextView.setVisibility(visibility);
            }
        }
    }

    public void updateCinemaModeGuideViewDegree(int degree) {
        RotateLayout mGuideTextLayout = (RotateLayout) this.mGet.findViewById(C0088R.id.cine_mode_guide_layout_rotate);
        TextView textView = (TextView) this.mGet.findViewById(C0088R.id.cine_mode_guide_layout_textview);
        if (mGuideTextLayout != null && textView != null) {
            mGuideTextLayout.rotateLayout(degree);
            LayoutParams lp = (LayoutParams) mGuideTextLayout.getLayoutParams();
            LayoutParams textLp = (LayoutParams) textView.getLayoutParams();
            textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_port_width);
            Utils.resetLayoutParameter(lp);
            switch (Utils.convertDegree(this.mGet.getAppContext().getResources(), degree)) {
                case 0:
                    lp.addRule(10, 1);
                    lp.addRule(14, 1);
                    lp.topMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.cine_zoom_guide_text_marginTop) + textView.getLineHeight();
                    break;
                case 90:
                    lp.addRule(20, 1);
                    lp.addRule(15, 1);
                    lp.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_land_reverse_marginBottom));
                    textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.cine_zoom_guide_text_land_width);
                    break;
                case 180:
                    lp.addRule(10, 1);
                    lp.addRule(14, 1);
                    lp.topMargin = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.cine_zoom_guide_text_marginTop) + textView.getLineHeight();
                    break;
                case 270:
                    lp.addRule(20, 1);
                    lp.addRule(15, 1);
                    lp.setMarginStart(Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.popout_guide_text_land_reverse_marginBottom) + textView.getLineHeight());
                    textLp.width = Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.cine_zoom_guide_text_land_width);
                    break;
            }
            mGuideTextLayout.setLayoutParams(lp);
            textView.setLayoutParams(textLp);
        }
    }
}
