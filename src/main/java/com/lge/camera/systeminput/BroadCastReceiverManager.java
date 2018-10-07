package com.lge.camera.systeminput;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.FunctionProperties;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.Iterator;

public class BroadCastReceiverManager {
    private ArrayList<CameraBroadCastReceiver> mReceiverArrayList = new ArrayList();
    private int mRegisterReceiver = 0;
    private ArrayList<AsyncTask<Void, Void, Void>> mRegisterTaskList = new ArrayList();

    public void registerAllReceiver(final ReceiverInterface receiverInterface, final int addType) {
        AsyncTask task = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... arg0) {
                synchronized (BroadCastReceiverManager.this.mReceiverArrayList) {
                    if (BroadCastReceiverManager.this.mReceiverArrayList == null || BroadCastReceiverManager.this.mRegisterReceiver == 15) {
                    } else {
                        BroadCastReceiverManager.this.registerPrimaryReceivers(receiverInterface, addType);
                        BroadCastReceiverManager.this.registerSecondaryReceivers(receiverInterface, addType);
                        if (BroadCastReceiverManager.this.isNeedToAdd(addType, 4) || BroadCastReceiverManager.this.isNeedToAdd(addType, 8)) {
                            BroadCastReceiverManager.this.mRegisterReceiver = BroadCastReceiverManager.this.mRegisterReceiver | 4;
                            BroadCastReceiverManager.this.registerReceiver(ScreenOffReceiver.class.getName(), receiverInterface);
                        }
                        if (BroadCastReceiverManager.this.isNeedToAdd(addType, 8)) {
                            BroadCastReceiverManager.this.mRegisterReceiver = BroadCastReceiverManager.this.mRegisterReceiver | 8;
                            BroadCastReceiverManager.this.registerReceiver(QuickWindowCaseReceiver.class.getName(), receiverInterface);
                            BroadCastReceiverManager.this.registerReceiver(QuickCoverMdmReceiver.class.getName(), receiverInterface);
                        }
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                BroadCastReceiverManager.this.mRegisterTaskList.remove(this);
            }
        };
        this.mRegisterTaskList.add(task);
        task.execute((Object[]) new Void[0]);
    }

    private void registerPrimaryReceivers(ReceiverInterface receiverInterface, int addType) {
        if (isNeedToAdd(addType, 1)) {
            this.mRegisterReceiver |= 1;
            registerReceiver(BatteryReceiver.class.getName(), receiverInterface);
            registerReceiver(LGMdmReceiver.class.getName(), receiverInterface);
            registerReceiver(CameraPolicyReceiver.class.getName(), receiverInterface);
            registerReceiver(TemperatureReceiver.class.getName(), receiverInterface);
            if (ModelProperties.isKeyPadSupported(receiverInterface.getAppContext())) {
                registerReceiver(EndKeyReceiver.class.getName(), receiverInterface);
            }
        }
    }

    private void registerSecondaryReceivers(ReceiverInterface receiverInterface, int addType) {
        if (isNeedToAdd(addType, 2)) {
            this.mRegisterReceiver |= 2;
            registerReceiver(MessageReceiver.class.getName(), receiverInterface);
            registerReceiver(HdmiReceiver.class.getName(), receiverInterface);
            registerReceiver(MediaReceiver.class.getName(), receiverInterface);
            registerReceiver(HeadsetReceiver.class.getName(), receiverInterface);
            registerReceiver(CallPopUpReceiver.class.getName(), receiverInterface);
            registerReceiver(DayDreamReceiver.class.getName(), receiverInterface);
            registerReceiver(VoiceMailReceiver.class.getName(), receiverInterface);
            registerReceiver(BluetoothReceiver.class.getName(), receiverInterface);
            registerReceiver(QuickClipReceiver.class.getName(), receiverInterface);
            registerReceiver(AudioRaMReceiver.class.getName(), receiverInterface);
            registerReceiver(GalleryDeleteReceiver.class.getName(), receiverInterface);
            registerReceiver(LGLensSupportChangedReceiver.class.getName(), receiverInterface);
            if (FunctionProperties.isSupportedMode(CameraConstants.MODE_MANUAL_VIDEO)) {
                registerReceiver(AudioNoiseReceiver.class.getName(), receiverInterface);
            }
            registerReceiver(FlashOffIntentReceiver.class.getName(), receiverInterface);
            if (ModelProperties.getCarrierCode() == 1) {
                registerReceiver(UplusNotificationReceiver.class.getName(), receiverInterface);
            }
        }
    }

    public void unregisterReceivers(final Activity activity) {
        if (this.mRegisterTaskList.size() == 0) {
            doUnregister(activity);
        } else {
            new Thread(new Runnable() {
                public void run() {
                    while (BroadCastReceiverManager.this.mRegisterTaskList.size() > 0) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    BroadCastReceiverManager.this.doUnregister(activity);
                }
            }).start();
        }
    }

    private void doUnregister(Activity activity) {
        synchronized (this.mReceiverArrayList) {
            if (this.mReceiverArrayList != null) {
                Iterator it = this.mReceiverArrayList.iterator();
                while (it.hasNext()) {
                    checkAndUnRegisterReceiver(activity, (CameraBroadCastReceiver) it.next());
                }
                this.mReceiverArrayList.clear();
                this.mRegisterReceiver = 0;
            }
        }
    }

    private void registerReceiver(String receiverName, ReceiverInterface receiverInterface) {
        try {
            CamLog.m3d(CameraConstants.TAG, "register receiver : " + receiverName);
            CameraBroadCastReceiver receiver = (CameraBroadCastReceiver) Class.forName(receiverName).getConstructor(new Class[]{ReceiverInterface.class}).newInstance(new Object[]{receiverInterface});
            checkAndRegisterReceiver(receiverInterface.getActivity(), receiver.getReceiver(), receiver.getIntentFilter());
            if (this.mReceiverArrayList != null) {
                this.mReceiverArrayList.add(receiver);
            }
        } catch (Exception e) {
            CamLog.m5e(CameraConstants.TAG, "registerReceiver error : " + e);
        }
    }

    private void checkAndRegisterReceiver(Activity activity, BroadcastReceiver receiver, IntentFilter intentFilter) {
        if (activity != null && receiver != null && intentFilter != null) {
            activity.registerReceiver(receiver, intentFilter);
        }
    }

    private void checkAndUnRegisterReceiver(Activity activity, BroadcastReceiver receiver) {
        if (activity != null && receiver != null) {
            try {
                activity.unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                CamLog.m11w(CameraConstants.TAG, "Can not unregister : " + receiver);
            }
        }
    }

    private boolean isNeedToAdd(int requestType, int checkType) {
        if ((checkType & requestType) == 0 || (this.mRegisterReceiver & checkType) != 0) {
            return false;
        }
        return true;
    }
}
