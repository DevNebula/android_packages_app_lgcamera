package com.lge.camera.util;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import com.lge.camera.C0088R;

public class DialogCreator {
    public static final int DIALOG_FINGERPRINT_INITIAL_GUIDE = 140;
    public static final int DIALOG_ID_AU_CLOUD = 6;
    public static final int DIALOG_ID_DELETE = 141;
    public static final int DIALOG_ID_DELETE_CONFIRM = 2;
    public static final int DIALOG_ID_DELETE_CONFIRM_FILTER = 149;
    public static final int DIALOG_ID_DELETE_CONFIRM_STICKER = 151;
    public static final int DIALOG_ID_DELETE_MODE = 150;
    public static final int DIALOG_ID_DELETE_OVERLAP_SAMPLE_PROJECT = 143;
    public static final int DIALOG_ID_DELETE_OVERLAP_USER_PROJECT = 142;
    public static final int DIALOG_ID_ENABLE_GALLERY = 4;
    public static final int DIALOG_ID_ENABLE_GOOGLE_PHOTOS = 11;
    public static final int DIALOG_ID_ENABLE_YOUTUBE = 17;
    public static final int DIALOG_ID_EXTERNAL_STORAGE_FULL = 1;
    public static final int DIALOG_ID_GRAPHY_FULL_OF_IMAGE = 16;
    public static final int DIALOG_ID_GRAPHY_INSTALL = 14;
    public static final int DIALOG_ID_GRAPHY_ON = 15;
    public static final int DIALOG_ID_HELP_OTHER = 121;
    public static final int DIALOG_ID_HIFI = 139;
    public static final int DIALOG_ID_INIT_CNAS = 147;
    public static final int DIALOG_ID_INIT_STORAGE = 8;
    public static final int DIALOG_ID_LOCATION_CONSENT = 138;
    public static final int DIALOG_ID_MANUAL_MODE_INITIAL_GUIDE = 148;
    public static final int DIALOG_ID_NO_EXTERNAL = 0;
    public static final int DIALOG_ID_PROGRESS = 3;
    public static final int DIALOG_ID_PROGRESS_BAR = 13;
    public static final int DIALOG_ID_QUICK_CLIP_SHARE = 129;
    public static final int DIALOG_ID_SAVING_MULTIVIEW = 132;
    public static final int DIALOG_ID_SAVING_PROGRESS = 5;
    public static final int DIALOG_ID_SELECT_IMAGE_SIZE = 9;
    public static final int DIALOG_ID_SELECT_OVERLAP_SAMPLE = 146;
    public static final int DIALOG_ID_SELECT_SAVE_DIRECTION = 10;
    public static final int DIALOG_ID_SNAP_CLIP_STORAGE = 134;
    public static final int DIALOG_ID_SNAP_DELETE = 131;
    public static final int DIALOG_ID_SNAP_INIT = 130;
    public static final int DIALOG_ID_SNAP_JUST_SAVE = 133;
    public static final int DIALOG_ID_STORAGE_FULL_1_EXTERNAL = 123;
    public static final int DIALOG_ID_STORAGE_FULL_1_INTERNAL = 122;
    public static final int DIALOG_ID_STORAGE_FULL_2_EXTERNAL = 125;
    public static final int DIALOG_ID_STORAGE_FULL_2_INTERNAL = 124;
    public static final int DIALOG_ID_STORAGE_FULL_2_NAS_TO_EXTERNAL = 127;
    public static final int DIALOG_ID_STORAGE_FULL_2_NAS_TO_INTERNAL = 126;
    public static final int DIALOG_ID_STORAGE_FULL_3 = 128;
    public static final int DIALOG_ID_STORAGE_FULL_NAS_CACHE = 145;
    public static final int DIALOG_ID_TAG_LOCATION = 7;
    public static final int DIALOG_ID_TAG_LOCATION_FROM_SETTING = 12;
    public static final int DIALOG_ID_UNDO = 144;

    public static int getDialogWidth(Context context, boolean isLand) {
        if (isLand) {
            return Utils.getPx(context, C0088R.dimen.rotate_dialog_horizontal_width);
        }
        return Utils.getPx(context, C0088R.dimen.rotate_dialog_vertical_width);
    }

    public static Dialog create(Context context, int id) {
        return create(context, id, null, null, null, null);
    }

    public static Dialog create(Context context, int id, OnClickListener listener1) {
        return create(context, id, listener1, null, null, null);
    }

    public static Dialog create(Context context, int id, OnClickListener listener1, OnClickListener listener2) {
        return create(context, id, listener1, listener2, null, null);
    }

    public static Dialog create(Context context, int id, OnClickListener listener1, OnClickListener listener2, OnClickListener listener3) {
        return create(context, id, listener1, listener2, listener3, null);
    }

    public static Dialog create(Context context, int id, OnClickListener listener1, OnClickListener listener2, OnClickListener listener3, Object arg1) {
        Builder builder = new Builder(context);
        switch (id) {
            case 2:
                if (context != null) {
                    builder.setTitle(C0088R.string.dlg_title_delete).setMessage(C0088R.string.sp_photo_will_be_deleted_NORMAL).setPositiveButton(C0088R.string.yes, listener1).setNegativeButton(C0088R.string.no, listener2);
                    break;
                }
                break;
            case 4:
                builder.setTitle(C0088R.string.sp_note_dialog_title_NORMAL).setMessage(C0088R.string.sp_enable_app_msg_NORMAL).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1).setNegativeButton(C0088R.string.cancel, listener2);
                break;
            case 8:
                builder.setTitle(C0088R.string.camera_storage_init_dialog_title).setMessage(C0088R.string.sp_save_to_sdcard_NORMAL1).setPositiveButton(C0088R.string.change, listener1).setNegativeButton(C0088R.string.cancel, listener2);
                break;
            case 122:
                builder.setTitle(C0088R.string.sp_storage_full_popup_ics_title_NORMAL).setIconAttribute(16843605).setMessage(C0088R.string.storage_full_msg_1_internal).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1);
                break;
            case 123:
                builder.setTitle(C0088R.string.sp_storage_full_popup_ics_title_NORMAL).setIconAttribute(16843605).setMessage(C0088R.string.storage_full_msg_1_external).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1);
                break;
            case 124:
                builder.setTitle(C0088R.string.sp_storage_full_popup_ics_title_NORMAL).setIconAttribute(16843605).setMessage(C0088R.string.storage_full_msg_2_internal).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1);
                break;
            case 125:
                builder.setTitle(C0088R.string.sp_storage_full_popup_ics_title_NORMAL).setIconAttribute(16843605).setMessage(C0088R.string.storage_full_msg_2_external).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1);
                break;
            case 128:
                builder.setTitle(C0088R.string.sp_storage_full_popup_ics_title_NORMAL).setIconAttribute(16843605).setMessage(C0088R.string.storage_full_msg_3).setPositiveButton(C0088R.string.sp_ok_NORMAL, listener1);
                break;
            default:
                return null;
        }
        return builder.create();
    }
}
