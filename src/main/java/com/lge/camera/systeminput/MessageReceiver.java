package com.lge.camera.systeminput;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;

public class MessageReceiver extends CameraBroadCastReceiver {
    private static final Uri CONTENT_URI_MMS = Uri.parse("content://mms/inbox");
    private static final Uri CONTENT_URI_SMS = Uri.parse("content://sms/inbox");
    private static final String FIELD_DATE = "date";
    private static final String FIELD_ID = "_id";

    public MessageReceiver(ReceiverInterface receiverInterface) {
        super(receiverInterface);
    }

    public void onReceive(Context context, Intent intent) {
        if (checkOnReceive(intent)) {
            CamLog.m3d(CameraConstants.TAG, "BroadCastReceiver action = " + intent.getAction());
            String action = intent.getAction();
            int messageType = 1;
            boolean isReadAllMsg = false;
            if ("com.lge.message.MSG_RECEIVED_ACTION".equals(action)) {
                messageType = doMessageReceivedAction(intent);
                this.mGet.showModuleToast(this.mGet.getAppContext().getString(C0088R.string.message_received), CameraConstants.TOAST_LENGTH_SHORT);
            } else if ("lge.intent.action.UNREAD_MESSAGES".equals(action)) {
                int remainMsgCount = Integer.parseInt(intent.getStringExtra("number"));
                CamLog.m3d(CameraConstants.TAG, "Remain message count is " + remainMsgCount);
                if (remainMsgCount == 0) {
                    isReadAllMsg = true;
                }
            }
            try {
                this.mGet.setMessageIndicatorReceived(messageType, isReadAllMsg);
            } catch (NumberFormatException e) {
                CamLog.m5e(CameraConstants.TAG, "failure to read msg number");
            }
            CamLog.m3d(CameraConstants.TAG, "worning intent rescived!!");
        }
    }

    private int doMessageReceivedAction(Intent intent) {
        int messageType;
        String msg_type = intent.getStringExtra("msg_type");
        CamLog.m3d(CameraConstants.TAG, "mail received msg_type = " + msg_type);
        if (msg_type == null || !msg_type.equals("mms")) {
            messageType = 1;
        } else {
            messageType = 2;
        }
        try {
            this.mGet.setMessageIndicatorReceived(messageType, false);
        } catch (NumberFormatException e) {
            CamLog.m5e(CameraConstants.TAG, "failure to read msg number");
        }
        return messageType;
    }

    public int getRecentMessageType() {
        if (ModelProperties.isWifiOnlyModel(this.mGet.getAppContext())) {
            return 0;
        }
        return getRecentMessageTypeForNormal(this.mGet.getActivity().getContentResolver(), 0, 0, 0);
    }

    private int getRecentMessageTypeForNormal(ContentResolver cr, long smsReceivedTime, long mmsReceivedTime, int type) {
        Cursor cursor = null;
        String[] projection = new String[]{"_id", FIELD_DATE};
        String selectionMMS = "read=0 and m_type=132";
        try {
            cursor = cr.query(CONTENT_URI_SMS, projection, "read=0 and type=1", null, null);
            if (cursor != null) {
                smsReceivedTime = getRecentMessageTime(cursor);
                cursor.close();
            }
            cursor = cr.query(CONTENT_URI_MMS, projection, selectionMMS, null, null);
            if (cursor != null) {
                mmsReceivedTime = getRecentMessageTime(cursor);
            }
            type = getTypeByReceivedTime(smsReceivedTime, mmsReceivedTime, type);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable e) {
            CamLog.m6e(CameraConstants.TAG, "getRecentMessageTypeForNormal Exception! ", e);
            type = 1;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return type;
    }

    private int getTypeByReceivedTime(long smsReceivedTime, long mmsReceivedTime, int type) {
        if (ModelProperties.isDomesticModel()) {
            mmsReceivedTime *= 1000;
        }
        if (smsReceivedTime > mmsReceivedTime) {
            return 1;
        }
        if (smsReceivedTime >= mmsReceivedTime) {
            return type;
        }
        if (ModelProperties.isDomesticModel()) {
            return 2;
        }
        return 1;
    }

    private long getRecentMessageTime(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return 0;
        }
        cursor.moveToFirst();
        return cursor.getLong(cursor.getColumnIndex(FIELD_DATE));
    }

    protected IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter("com.lge.message.MSG_RECEIVED_ACTION");
        intentFilter.addAction("com.lge.message.SMS_RECEIVED_ACTION_FOR_LGE_APPL");
        intentFilter.addAction("com.lge.message.MMS_RECEIVED_ACTION_FOR_LGE_APPL");
        intentFilter.addAction("lge.intent.action.UNREAD_MESSAGES");
        intentFilter.addAction("lge.intent.action.UNREAD_SKT_MESSAGES");
        intentFilter.addAction("lge.intent.action.ACTION_UNREAD_SMS");
        return intentFilter;
    }

    protected CameraBroadCastReceiver getReceiver() {
        return this;
    }
}
