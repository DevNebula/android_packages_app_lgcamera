package com.lge.octopus.tentacles.ble.central;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class LocalHandler extends Handler {
    public static final int LE_CONNECT_TIMEOUT = 10;
    private final WeakReference<IHandleMessage> mHandler;

    public interface IHandleMessage {
        void handleMessage(Message message);
    }

    public LocalHandler(IHandleMessage object) {
        this.mHandler = new WeakReference(object);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        CentralGattService centralGattService = (CentralGattService) this.mHandler.get();
        if (centralGattService != null) {
            centralGattService.handleMessage(msg);
        }
    }
}
