package com.lge.camera.managers.ext;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout.LayoutParams;
import com.lge.camera.C0088R;
import com.lge.camera.components.QRCodeGuideBox;
import com.lge.camera.components.RotateTextView;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.LdbConstants;
import com.lge.camera.device.CameraManager.CameraProxy;
import com.lge.camera.device.CameraParameters;
import com.lge.camera.managers.ManagerInterfaceImpl;
import com.lge.camera.managers.ModuleInterface;
import com.lge.camera.qrcode.QRCode;
import com.lge.camera.qrcode.QrCodeEngine;
import com.lge.camera.qrcode.QrCodeEngine.QrInterface;
import com.lge.camera.settings.Setting;
import com.lge.camera.util.AnimationUtil;
import com.lge.camera.util.AudioUtil;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.ColorConverter;
import com.lge.camera.util.ColorUtil;
import com.lge.camera.util.HandlerRunnable;
import com.lge.camera.util.LdbUtil;
import com.lge.camera.util.QRCodeUtil;
import java.util.List;

public class QRCodeManager extends ManagerInterfaceImpl implements QrInterface {
    private static final int FAILURE_NO_NETWORK_ID = -1;
    private static final int QR_FRAME = 10;
    private static final String RECOGNITION_THREAD_NAME = "qr_recognition";
    private static final long RECOGNITION_THREAD_SLEEPTIME = 600;
    private ViewGroup mBaseView = null;
    private String mBeforeQRCodeSummary = null;
    private int mBeforeQRCodeType = 0;
    private boolean mIsRunningThread;
    private int mMoveStartX = 0;
    private byte[] mOutRGBArrayQR;
    private int mPreviewCallbackTime = 0;
    private int mPreviewH = 0;
    private Size mPreviewSize = null;
    private int mPreviewW = 0;
    private QRCode mQRCode = null;
    private QRCodeGuideBox mQRCodeGuideBox = null;
    private View mQRCodeInfoBox = null;
    private RotateTextView mQRCodeText = null;
    private RotateTextView mQRCodeTextDesc = null;
    private View mQRCodeView = null;
    private Runnable mQRRecognitionRunnable = new C12521();
    private QRThread mQRRecognitionThread;
    private QrCodeEngine mQrCodeEngine;
    private HandlerRunnable mReleaseQRCodeInfoBox = new HandlerRunnable(this) {
        public void handleRun() {
            if (QRCodeManager.this.mQRCodeInfoBox != null && QRCodeManager.this.mQRCodeInfoBox.getVisibility() == 0) {
                QRCodeManager.this.startQRCodeAnimation(false, true);
            }
        }
    };
    private int mResizedPreviewHeight;
    private int mResizedPreviewWidth;
    private int mTouchStartX = 0;
    private WifiManager mWifiManager;

    /* renamed from: com.lge.camera.managers.ext.QRCodeManager$1 */
    class C12521 implements Runnable {
        C12521() {
        }

        public void run() {
            QRCodeManager.this.scanQR();
        }
    }

    /* renamed from: com.lge.camera.managers.ext.QRCodeManager$2 */
    class C12532 implements OnTouchListener {
        C12532() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            int posX = (int) event.getX();
            switch (event.getActionMasked() & 255) {
                case 0:
                    CamLog.m3d(CameraConstants.TAG, "ACTION_DOWN posX : " + posX);
                    QRCodeManager.this.mTouchStartX = posX;
                    break;
                case 1:
                    CamLog.m3d(CameraConstants.TAG, "ACTION_UP");
                    if (QRCodeManager.this.mMoveStartX - QRCodeManager.this.mTouchStartX > 100) {
                        QRCodeManager.this.startQRCodeAnimation(false, true);
                    } else if (QRCodeManager.this.mTouchStartX - QRCodeManager.this.mMoveStartX > 100) {
                        QRCodeManager.this.startQRCodeAnimation(false, false);
                    } else if (QRCodeManager.this.mQRCode != null) {
                        QRCodeManager.this.doActionForQR(QRCodeManager.this.mQRCode);
                    }
                    QRCodeManager.this.mTouchStartX = 0;
                    QRCodeManager.this.mMoveStartX = 0;
                    break;
                case 2:
                    QRCodeManager.this.mMoveStartX = posX;
                    break;
            }
            return true;
        }
    }

    /* renamed from: com.lge.camera.managers.ext.QRCodeManager$3 */
    class C12543 implements OnClickListener {
        C12543() {
        }

        public void onClick(View arg0) {
            CamLog.m3d(CameraConstants.TAG, "QR info box is clicked");
            if (QRCodeManager.this.mQRCode != null) {
                QRCodeManager.this.doActionForQR(QRCodeManager.this.mQRCode);
            }
        }
    }

    public class QRThread extends Thread {
        private Runnable mRunnable;
        private boolean mRunning;
        private long mSleepTime;

        public QRThread(String name, Runnable r, long sleepTime) {
            setName(name);
            this.mRunnable = r;
            this.mSleepTime = sleepTime;
        }

        public synchronized void start() {
            this.mRunning = true;
            super.start();
        }

        public void run() {
            super.run();
            while (this.mRunning) {
                this.mRunnable.run();
                try {
                    sleep(this.mSleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            interrupt();
        }

        public void stopThread() {
            this.mRunning = false;
        }
    }

    public QRCodeManager(ModuleInterface moduleInterface) {
        super(moduleInterface);
    }

    public void init() {
        super.init();
        this.mBaseView = (ViewGroup) this.mGet.findViewById(C0088R.id.camera_controls);
        this.mQRCodeView = this.mGet.inflateView(C0088R.layout.qr_code_view);
        this.mQrCodeEngine = new QrCodeEngine(getAppContext(), this);
        View quickButtonView = this.mGet.findViewById(C0088R.id.quick_button_layout_root);
        int index = 0;
        if (quickButtonView != null) {
            index = this.mBaseView.indexOfChild(quickButtonView) + 1;
        }
        this.mBaseView.addView(this.mQRCodeView, index, new LayoutParams(-1, -1));
        this.mQRCodeInfoBox = this.mQRCodeView.findViewById(C0088R.id.qr_code_infobox);
        this.mQRCodeText = (RotateTextView) this.mQRCodeView.findViewById(C0088R.id.qr_code_text);
        this.mQRCodeTextDesc = (RotateTextView) this.mQRCodeView.findViewById(C0088R.id.qr_code_description);
        setRotateDegree(this.mGet.getOrientationDegree(), false);
        View previewFrame = this.mGet.getPreviewFrameLayout();
        if (previewFrame != null) {
            this.mQRCodeGuideBox = (QRCodeGuideBox) previewFrame.findViewById(C0088R.id.qr_code_view);
            if (this.mQRCodeGuideBox != null) {
                this.mQRCodeGuideBox.setVisibility(4);
                this.mQRCodeGuideBox.init();
                this.mQRCodeGuideBox.setInitialDegree(this.mGet.getOrientationDegree());
            }
        }
        setBtnClickListener();
    }

    private void setBtnClickListener() {
        if (this.mQRCodeInfoBox != null) {
            this.mQRCodeInfoBox.setOnTouchListener(new C12532());
            this.mQRCodeInfoBox.setOnClickListener(new C12543());
        }
    }

    public void startDetectQR() {
        CamLog.m3d(CameraConstants.TAG, "startDetectQR");
        this.mIsRunningThread = true;
        this.mGet.removePostRunnable(this.mReleaseQRCodeInfoBox);
        getResizedPreviewSize();
        this.mOutRGBArrayQR = new byte[((this.mResizedPreviewWidth * this.mResizedPreviewHeight) * 4)];
        if (this.mQrCodeEngine != null) {
            this.mQrCodeEngine.startDetection();
        }
        this.mQRRecognitionThread = new QRThread(RECOGNITION_THREAD_NAME, this.mQRRecognitionRunnable, 600);
        this.mQRRecognitionThread.start();
    }

    public void stopDetectQR() {
        CamLog.m3d(CameraConstants.TAG, "stopDetectQR");
        if (this.mQrCodeEngine != null) {
            this.mQrCodeEngine.stopDetection();
        }
        if (this.mQRRecognitionThread != null && this.mQRRecognitionThread.isAlive() && this.mIsRunningThread) {
            this.mQRRecognitionThread.stopThread();
            this.mIsRunningThread = false;
        }
        resetQR();
    }

    private void resetQR() {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (QRCodeManager.this.mQRCodeGuideBox != null && QRCodeManager.this.mQRCodeGuideBox.getVisibility() == 0) {
                    CamLog.m3d(CameraConstants.TAG, "resetQR");
                    QRCodeManager.this.mQRCodeGuideBox.setVisibility(4);
                    QRCodeManager.this.mQRCodeGuideBox.setPreviewSize(QRCodeManager.this.mGet.getAppContext(), 0, 0);
                    QRCodeManager.this.mQRCodeGuideBox.setBitmapSize(QRCodeManager.this.mGet.getAppContext(), 0, 0);
                    QRCodeManager.this.mQRCodeGuideBox.setRectangleArea(0, 0, 0, 0);
                }
            }
        });
    }

    public boolean isRunningQRCode() {
        return this.mIsRunningThread;
    }

    public void setRotateDegree(int degree, boolean animation) {
        if (this.mQRCodeGuideBox != null && this.mQRCodeGuideBox.getVisibility() != 0) {
            this.mQRCodeGuideBox.setDegree(degree);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mPreviewSize = null;
        if (!(this.mBaseView == null || this.mQRCodeView == null)) {
            this.mBaseView.removeView(this.mQRCodeView);
            this.mBaseView = null;
        }
        if (this.mQRCodeGuideBox != null) {
            this.mQRCodeGuideBox.unbind();
            this.mQRCodeGuideBox = null;
        }
        this.mQRCodeView = null;
        this.mQRCodeInfoBox = null;
        this.mQRCodeText = null;
        this.mQRCodeTextDesc = null;
        this.mWifiManager = null;
    }

    public void onImageData(Image image, byte[] data, CameraProxy camera) {
        if (FunctionProperties.getSupportedHal() != 2) {
            if (data == null) {
                return;
            }
        } else if (image == null) {
            return;
        }
        if (this.mPreviewW == 0 || this.mPreviewH == 0 || this.mResizedPreviewWidth == 0 || this.mResizedPreviewHeight == 0) {
            CamLog.m5e(CameraConstants.TAG, "preview size is zero");
            return;
        }
        if (this.mPreviewCallbackTime > 10 && this.mIsRunningThread && this.mOutRGBArrayQR != null && this.mQRCodeGuideBox != null) {
            this.mPreviewCallbackTime = 0;
            if (FunctionProperties.getSupportedHal() != 2) {
                ColorConverter.yuvToRgbArrayWithResize(this.mOutRGBArrayQR, data, this.mPreviewW, this.mPreviewH, this.mResizedPreviewWidth, this.mResizedPreviewHeight, this.mPreviewW, 2);
            } else {
                ColorConverter.nv21ToRgbArrayWithResize(this.mOutRGBArrayQR, image.getPlanes()[0].getBuffer(), image.getPlanes()[2].getBuffer(), this.mPreviewW, this.mPreviewH, this.mResizedPreviewWidth, this.mResizedPreviewHeight, image.getPlanes()[0].getRowStride(), 2);
            }
            this.mQRCodeGuideBox.setBitmapSize(this.mGet.getAppContext(), this.mResizedPreviewWidth, this.mResizedPreviewHeight);
        }
        if (this.mIsRunningThread) {
            this.mPreviewCallbackTime++;
        } else {
            this.mPreviewCallbackTime = 0;
        }
    }

    private void scanQR() {
        if ("on".equals(this.mGet.getSettingValue(Setting.KEY_QR)) && this.mOutRGBArrayQR != null && this.mPreviewSize != null && this.mQrCodeEngine != null && !this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !this.mGet.isTimerShotCountdown() && this.mGet.checkModuleValidate(192) && !this.mGet.isAnimationShowing()) {
            if (!this.mIsRunningThread) {
                CamLog.m3d(CameraConstants.TAG, "mIsRunningThread : " + this.mIsRunningThread);
            } else if (this.mPreviewW == 0 || this.mPreviewH == 0 || this.mResizedPreviewWidth == 0 || this.mResizedPreviewHeight == 0) {
                CamLog.m5e(CameraConstants.TAG, "preview size is zero");
            } else if (this.mOutRGBArrayQR != null) {
                this.mQrCodeEngine.sendPreviewImage(this.mOutRGBArrayQR, this.mResizedPreviewWidth, this.mResizedPreviewHeight, this.mPreviewW, this.mPreviewH);
            }
        }
    }

    private void setQRCodeInfo(final QRCode qrCode) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (qrCode != null && QRCodeManager.this.mPreviewSize != null && QRCodeManager.this.mQRCodeGuideBox != null && QRCodeManager.this.mQRCodeInfoBox != null && QRCodeManager.this.mQRCodeText != null && QRCodeManager.this.mQRCodeTextDesc != null && !QRCodeManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !QRCodeManager.this.mGet.isAnimationShowing() && !QRCodeManager.this.mGet.isTimerShotCountdown()) {
                    String type = QRCodeManager.this.getTypeName(qrCode);
                    if (QRCodeManager.this.mQRCodeInfoBox.getVisibility() != 0 || QRCodeManager.this.isDefferentQRCodeRecognition(qrCode)) {
                        QRCodeManager.this.mGet.keepScreenOnAwhile();
                        if (QRCodeManager.this.mQRCodeInfoBox.getVisibility() != 0) {
                            QRCodeManager.this.startQRCodeAnimation(true, false);
                        }
                        QRCodeManager.this.mGet.removePostRunnable(QRCodeManager.this.mReleaseQRCodeInfoBox);
                        QRCodeManager.this.mGet.postOnUiThread(QRCodeManager.this.mReleaseQRCodeInfoBox, CameraConstants.TOAST_LENGTH_MIDDLE_SHORT);
                        QRCodeManager.this.mQRCode = qrCode;
                        QRCodeManager.this.mQRCodeText.setTypeface(Typeface.DEFAULT);
                        QRCodeManager.this.mQRCodeText.setText(type);
                        QRCodeManager.this.mQRCodeTextDesc.setTypeface(Typeface.DEFAULT);
                        QRCodeManager.this.mQRCodeTextDesc.setColorFilter(ColorUtil.getColorMatrix(50.0f, 255.0f, 255.0f, 255.0f));
                        String qrCodeStr = QRCodeManager.this.setQRCodeDescription(qrCode);
                        if (qrCodeStr == null || "".equals(qrCodeStr)) {
                            QRCodeManager.this.mQRCodeTextDesc.setVisibility(8);
                        } else {
                            QRCodeManager.this.mQRCodeTextDesc.setText(qrCodeStr);
                            QRCodeManager.this.mQRCodeTextDesc.setVisibility(0);
                            QRCodeManager.this.mQRCodeInfoBox.setContentDescription(qrCodeStr);
                        }
                        CamLog.m3d(CameraConstants.TAG, "QRCode detected type : " + qrCode.type);
                    }
                    if ((!QRCodeManager.this.mGet.isMenuShowing(CameraConstants.MENU_TYPE_ALL) && !QRCodeManager.this.mGet.isAnimationShowing()) || !QRCodeManager.this.mGet.isTimerShotCountdown()) {
                        QRCodeManager.this.mQRCodeGuideBox.setVisibility(0);
                        QRCodeManager.this.mQRCodeGuideBox.setPreviewSize(QRCodeManager.this.mGet.getAppContext(), QRCodeManager.this.mPreviewSize.getWidth(), QRCodeManager.this.mPreviewSize.getHeight());
                        QRCodeManager.this.mQRCodeGuideBox.setRectangleArea(qrCode.rect.left, qrCode.rect.top, qrCode.rect.right, qrCode.rect.bottom);
                    }
                }
            }
        });
    }

    private void startQRCodeAnimation(final boolean show, boolean hideToRight) {
        if (this.mQRCodeInfoBox != null) {
            AnimationUtil.startQRCodeAnimation(this.mQRCodeInfoBox, show, hideToRight, new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    if (show) {
                        QRCodeManager.this.mQRCodeInfoBox.setVisibility(0);
                        AudioUtil.performHapticFeedback(QRCodeManager.this.mQRCodeInfoBox, 65580);
                        LdbUtil.sendLDBIntent(QRCodeManager.this.mGet.getAppContext(), LdbConstants.LDB_FEAT_NAME_QR);
                    }
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (!show) {
                        QRCodeManager.this.mQRCodeInfoBox.setVisibility(4);
                        QRCodeManager.this.mQRCodeGuideBox.setVisibility(4);
                    }
                }
            });
        }
    }

    private boolean isDefferentQRCodeRecognition(QRCode qrCode) {
        String[] desc = qrCode.summary.split("\\n");
        if (this.mBeforeQRCodeType == 0 || qrCode.type == this.mBeforeQRCodeType || this.mBeforeQRCodeSummary == null || desc[0].equals(this.mBeforeQRCodeSummary)) {
            return false;
        }
        return true;
    }

    private String getTypeName(QRCode qrCode) {
        String qrtype = "etc";
        String[] desc = qrCode.summary.split("\\n");
        switch (qrCode.type) {
            case 1:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_contact_rev2);
            case 2:
                return String.format(this.mGet.getAppContext().getString(C0088R.string.qrcode_type_email), new Object[]{qrCode.email.tos[0]});
            case 3:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_url_rev2);
            case 4:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_text_rev2);
            case 5:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_geo);
            case 6:
                return String.format(this.mGet.getAppContext().getString(C0088R.string.qrcode_type_tel), new Object[]{qrCode.phoneNumber});
            case 7:
                return String.format(this.mGet.getAppContext().getString(C0088R.string.qrcode_type_sms), new Object[]{desc[0]});
            case 8:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_calendar_rev2);
            case 9:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_wifi_rev2);
            default:
                return this.mGet.getAppContext().getString(C0088R.string.qrcode_type_unknown);
        }
    }

    private String setQRCodeDescription(QRCode qrCode) {
        String[] desc = qrCode.summary.split("\\n");
        this.mBeforeQRCodeType = qrCode.type;
        this.mBeforeQRCodeSummary = desc[0];
        switch (qrCode.type) {
            case 1:
                return qrCode.contact.phoneNumber[0];
            case 3:
                return qrCode.url.url;
            case 4:
            case 8:
            case 9:
                return desc[0];
            default:
                return "";
        }
    }

    private void doActionForQR(QRCode qrCode) {
        if (qrCode.type != 9) {
            stopDetectQR();
            this.mQRCodeInfoBox.setVisibility(4);
            if (4 == qrCode.type) {
                shareText(qrCode.rawValue);
                return;
            }
            Intent intent = parseResult(qrCode);
            if (intent != null) {
                try {
                    intent.addFlags(268435456);
                    this.mGet.getActivity().startActivity(intent);
                    this.mQRCode = null;
                } catch (ActivityNotFoundException e) {
                    CamLog.m5e(CameraConstants.TAG, e.getMessage());
                }
            }
        } else if (changeNetwork(qrCode) == -1 && this.mWifiManager != null) {
            this.mWifiManager.disconnect();
        }
    }

    public void shareText(String text) {
        String title = this.mGet.getAppContext().getResources().getString(C0088R.string.sp_share_via_NORMAL);
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType("text/plain");
        shareIntent.putExtra("android.intent.extra.TEXT", text);
        this.mGet.getActivity().startActivity(Intent.createChooser(shareIntent, title));
    }

    private Intent parseResult(QRCode qrCode) {
        Intent intent = new Intent();
        CamLog.m3d(CameraConstants.TAG, "QRCode type : " + qrCode.type);
        switch (qrCode.type) {
            case 1:
                return QRCodeUtil.makeIntentForAddContact(qrCode);
            case 2:
                return QRCodeUtil.makeIntentForSendEmail(qrCode);
            case 3:
                return QRCodeUtil.makeIntentForOpenUrl(qrCode);
            case 5:
                return QRCodeUtil.makeIntentForGeo(qrCode);
            case 6:
                return QRCodeUtil.makeIntentForDialPhone(qrCode);
            case 7:
                return QRCodeUtil.makeIntentForSendSms(qrCode);
            case 8:
                return QRCodeUtil.makeIntentForAddCalendar(qrCode);
            default:
                return intent;
        }
    }

    public void onSurfaceChanged() {
        CamLog.m3d(CameraConstants.TAG, "onSurfaceChanged");
        getResizedPreviewSize();
        this.mOutRGBArrayQR = new byte[((this.mResizedPreviewWidth * this.mResizedPreviewHeight) * 4)];
    }

    private void getResizedPreviewSize() {
        CameraProxy cameraDevice = this.mGet.getCameraDevice();
        if (cameraDevice != null) {
            CameraParameters parameters = cameraDevice.getParameters();
            if (parameters != null) {
                this.mPreviewSize = parameters.getPreviewSize();
                if (this.mPreviewSize != null) {
                    this.mPreviewW = this.mPreviewSize.getWidth();
                    this.mPreviewH = this.mPreviewSize.getHeight();
                    Size size = getBestResizeValue(this.mPreviewW, this.mPreviewH);
                    this.mResizedPreviewWidth = size.getWidth();
                    this.mResizedPreviewHeight = size.getHeight();
                    this.mResizedPreviewWidth -= this.mResizedPreviewWidth % 8;
                    this.mResizedPreviewHeight -= this.mResizedPreviewHeight % 8;
                }
            }
        }
    }

    private Size getBestResizeValue(int orgWidth, int orgHeight) {
        int bestWidth = 0;
        int bestHeight = 0;
        int tempWidth = orgWidth;
        int tempHeight = orgHeight;
        int i = 8;
        while (i >= 2) {
            if (i % 2 == 0 && tempWidth % i == 0 && tempHeight % i == 0 && (tempWidth / i) % 2 == 0 && (tempHeight / i) % 2 == 0) {
                bestWidth = tempWidth / i;
                bestHeight = tempHeight / i;
                if (bestWidth > 600 || bestHeight > 600) {
                    break;
                }
            }
            i--;
        }
        return new Size(bestWidth, bestHeight);
    }

    public void onQrCodeDetected(final QRCode qrCode) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (qrCode == null) {
                    QRCodeManager.this.resetQR();
                } else {
                    QRCodeManager.this.setQRCodeInfo(qrCode);
                }
            }
        });
    }

    public boolean isPaused() {
        return this.mGet.isPaused();
    }

    public void setQRLayoutVisibility(final int visibility) {
        this.mGet.runOnUiThread(new HandlerRunnable(this) {
            public void handleRun() {
                if (QRCodeManager.this.mQRCodeInfoBox != null && QRCodeManager.this.mQRCodeGuideBox != null) {
                    QRCodeManager.this.mGet.removePostRunnable(QRCodeManager.this.mReleaseQRCodeInfoBox);
                    QRCodeManager.this.mQRCodeInfoBox.setVisibility(visibility);
                    QRCodeManager.this.mQRCodeGuideBox.setVisibility(visibility);
                }
            }
        });
    }

    private int changeNetwork(QRCode qrCode) {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) getActivity().getSystemService("wifi");
        }
        if (qrCode.wifi.ssid == null) {
            return -1;
        }
        if (qrCode.wifi.networkEncryption.equalsIgnoreCase("WPA") && qrCode.wifi.networkEncryption.equalsIgnoreCase("WPA2") && !qrCode.wifi.networkEncryption.equalsIgnoreCase("WEP") && !qrCode.wifi.networkEncryption.equalsIgnoreCase("nopass")) {
            return -1;
        }
        if (qrCode.wifi.password == null || qrCode.wifi.networkEncryption == null || qrCode.wifi.networkEncryption.equalsIgnoreCase("nopass")) {
            return changeNetworkUnEncrypted(qrCode);
        }
        if (qrCode.wifi.networkEncryption.equalsIgnoreCase("WPA")) {
            return changeNetworkWPA(qrCode);
        }
        return changeNetworkWEP(qrCode);
    }

    private int changeNetworkUnEncrypted(QRCode qrCode) {
        CamLog.m3d(CameraConstants.TAG, "Empty password prompting a simple account setting");
        WifiConfiguration config = changeNetworkCommon(qrCode);
        config.wepKeys[0] = "";
        config.allowedKeyManagement.set(0);
        config.wepTxKeyIndex = 0;
        return requestNetworkChange(config);
    }

    private int changeNetworkWPA(QRCode qrCode) {
        WifiConfiguration config = changeNetworkCommon(qrCode);
        String pass = qrCode.wifi.password;
        if (QRCodeUtil.HEX_DIGITS_64.matcher(pass).matches()) {
            CamLog.m3d(CameraConstants.TAG, "A 64 bit hex password entered.");
            config.preSharedKey = pass;
        } else {
            CamLog.m3d(CameraConstants.TAG, "A normal password entered: I am quoting it.");
            config.preSharedKey = QRCodeUtil.convertToQuotedString(pass);
        }
        config.allowedAuthAlgorithms.set(0);
        config.allowedProtocols.set(0);
        config.allowedKeyManagement.set(1);
        config.allowedGroupCiphers.set(2);
        config.allowedGroupCiphers.set(3);
        config.allowedProtocols.set(1);
        return requestNetworkChange(config);
    }

    private int changeNetworkWEP(QRCode qrCode) {
        WifiConfiguration config = changeNetworkCommon(qrCode);
        String pass = qrCode.wifi.password;
        if (QRCodeUtil.isHexWepKey(pass)) {
            config.wepKeys[0] = pass;
        } else {
            config.wepKeys[0] = QRCodeUtil.convertToQuotedString(pass);
        }
        config.allowedAuthAlgorithms.set(1);
        config.allowedGroupCiphers.set(3);
        config.allowedGroupCiphers.set(2);
        config.allowedGroupCiphers.set(0);
        config.allowedGroupCiphers.set(1);
        config.allowedKeyManagement.set(0);
        config.wepTxKeyIndex = 0;
        return requestNetworkChange(config);
    }

    private WifiConfiguration changeNetworkCommon(QRCode qrCode) {
        CamLog.m3d(CameraConstants.TAG, "Adding new configuration: \nSSID: " + qrCode.wifi.ssid + "\nType: " + qrCode.wifi.networkEncryption);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = QRCodeUtil.convertToQuotedString(qrCode.wifi.ssid);
        config.hiddenSSID = true;
        return config;
    }

    private int requestNetworkChange(WifiConfiguration config) {
        return updateNetwork(config, false);
    }

    private int updateNetwork(WifiConfiguration config, boolean disableOthers) {
        if (this.mWifiManager == null) {
            return -1;
        }
        if (this.mWifiManager.getWifiState() != 3) {
            this.mWifiManager.setWifiEnabled(true);
        }
        WifiConfiguration found = findNetworkInExistingConfig(config.SSID);
        this.mWifiManager.disconnect();
        if (found != null) {
            CamLog.m3d(CameraConstants.TAG, "Removing network " + found.networkId);
            this.mWifiManager.removeNetwork(found.networkId);
            this.mWifiManager.saveConfiguration();
        }
        int networkId = this.mWifiManager.addNetwork(config);
        CamLog.m3d(CameraConstants.TAG, "Inserted/Modified network " + networkId);
        if (networkId < 0) {
            return -1;
        }
        if (!this.mWifiManager.enableNetwork(networkId, disableOthers)) {
            return -1;
        }
        CamLog.m3d(CameraConstants.TAG, "Network is enabled. networkId : " + networkId);
        this.mWifiManager.reassociate();
        return networkId;
    }

    private WifiConfiguration findNetworkInExistingConfig(String ssid) {
        if (this.mWifiManager == null) {
            return null;
        }
        List<WifiConfiguration> existingConfigs = this.mWifiManager.getConfiguredNetworks();
        if (!(existingConfigs == null || existingConfigs.isEmpty())) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssid)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }
}
