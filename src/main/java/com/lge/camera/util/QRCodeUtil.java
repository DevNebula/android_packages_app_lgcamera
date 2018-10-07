package com.lge.camera.util;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import com.lge.camera.qrcode.QRCode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class QRCodeUtil {
    private static final String[] ADDRESS_TYPE_STRINGS = new String[]{"home", "work"};
    private static final int[] ADDRESS_TYPE_VALUES = new int[]{1, 2};
    public static final String[] EMAIL_KEYS = new String[]{"email", "secondary_email", "tertiary_email"};
    public static final String[] EMAIL_TYPE_KEYS = new String[]{"email_type", "secondary_email_type", "tertiary_email_type"};
    private static final String[] EMAIL_TYPE_STRINGS = new String[]{"home", "work", "mobile"};
    private static final int[] EMAIL_TYPE_VALUES = new int[]{1, 2, 4};
    public static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");
    public static final Pattern HEX_DIGITS_64 = Pattern.compile("[0-9A-Fa-f]{64}");
    private static final int NO_TYPE = -1;
    public static final String[] PHONE_KEYS = new String[]{"phone", "secondary_phone", "tertiary_phone"};
    public static final String[] PHONE_TYPE_KEYS = new String[]{"phone_type", "secondary_phone_type", "tertiary_phone_type"};
    private static final String[] PHONE_TYPE_STRINGS = new String[]{"home", "work", "mobile", "fax", "pager", "main"};
    private static final int[] PHONE_TYPE_VALUES = new int[]{1, 3, 2, 4, 6, 12};

    private static void putExtra(Intent intent, String key, String value) {
        if (value != null && !value.isEmpty()) {
            intent.putExtra(key, value);
        }
    }

    public static Intent makeIntentForAddContact(QRCode qrCode) {
        Intent intent = new Intent("android.intent.action.INSERT_OR_EDIT", Contacts.CONTENT_URI);
        intent.setType("vnd.android.cursor.item/contact");
        makeIntentForAddContectBasicInfo(qrCode, intent);
        makeIntentForAddContectAdditionalInfo(qrCode, intent);
        return intent;
    }

    public static void makeIntentForAddContectBasicInfo(QRCode qrCode, Intent intent) {
        int i;
        ContentValues row;
        if (qrCode.contact.name != null) {
            putExtra(intent, "name", qrCode.contact.name[0]);
        }
        if (qrCode.contact.pronunciation != null) {
            putExtra(intent, "phonetic_name", qrCode.contact.pronunciation);
        }
        if (qrCode.contact.phoneNumber != null) {
            int phoneCount = Math.min(qrCode.contact.phoneNumber.length, PHONE_KEYS.length);
            i = 0;
            while (i < phoneCount) {
                putExtra(intent, "phone", qrCode.contact.phoneNumber[i]);
                if (qrCode.contact.phoneType != null && i < qrCode.contact.phoneType.length) {
                    putExtra(intent, PHONE_TYPE_KEYS[i], qrCode.contact.phoneType[i]);
                }
                i++;
            }
        }
        if (qrCode.contact.email != null) {
            int emailCount = Math.min(qrCode.contact.email.length, EMAIL_KEYS.length);
            i = 0;
            while (i < emailCount) {
                putExtra(intent, EMAIL_KEYS[i], qrCode.contact.email[i]);
                if (qrCode.contact.emailType != null && i < qrCode.contact.emailType.length) {
                    int type = toEmailContractType(qrCode.contact.emailType[i]);
                    if (type >= 0) {
                        intent.putExtra(EMAIL_TYPE_KEYS[i], type);
                    }
                }
                i++;
            }
        }
        ArrayList<ContentValues> data = new ArrayList();
        if (qrCode.contact.url != null) {
            for (String url : qrCode.contact.url) {
                row = new ContentValues(2);
                row.put("mimetype", "vnd.android.cursor.item/website");
                row.put("data1", url);
                data.add(row);
            }
        }
        if (qrCode.contact.birthday != null) {
            row = new ContentValues(3);
            row.put("mimetype", "vnd.android.cursor.item/contact_event");
            row.put("data2", Integer.valueOf(3));
            row.put("data1", qrCode.contact.birthday);
            data.add(row);
        }
        if (qrCode.contact.nickname != null) {
            for (String nickname : qrCode.contact.nickname) {
                if (nickname != null && !nickname.isEmpty()) {
                    row = new ContentValues(3);
                    row.put("mimetype", "vnd.android.cursor.item/nickname");
                    row.put("data2", Integer.valueOf(1));
                    row.put("data1", nickname);
                    data.add(row);
                    break;
                }
            }
        }
        if (!data.isEmpty()) {
            intent.putParcelableArrayListExtra("data", data);
        }
    }

    public static void makeIntentForAddContectAdditionalInfo(QRCode qrCode, Intent intent) {
        StringBuilder aggregatedNotes = new StringBuilder();
        if (qrCode.contact.note != null) {
            aggregatedNotes.append(10).append(qrCode.contact.note);
        }
        if (qrCode.contact.geo != null) {
            aggregatedNotes.append(10).append(qrCode.contact.geo[0]).append(',').append(qrCode.contact.geo[1]);
        }
        if (aggregatedNotes.length() > 0) {
            putExtra(intent, "notes", aggregatedNotes.substring(1));
        }
        if (qrCode.contact.instantMessenger != null) {
            putExtra(intent, "im_handle", qrCode.contact.instantMessenger);
        }
        if (qrCode.contact.address != null) {
            putExtra(intent, "postal", qrCode.contact.address[0]);
        }
        if (qrCode.contact.addressType != null) {
            int type = toAddressContractType(qrCode.contact.addressType[0]);
            if (type >= 0) {
                intent.putExtra("postal_type", type);
            }
        }
        if (qrCode.contact.org != null) {
            putExtra(intent, "company", qrCode.contact.org);
        }
        if (qrCode.contact.title != null) {
            putExtra(intent, "job_title", qrCode.contact.title);
        }
    }

    public static Intent makeIntentForSendEmail(QRCode qrCode) {
        Intent intent = new Intent("android.intent.action.SEND");
        if (qrCode.email.tos != null) {
            intent.putExtra("android.intent.extra.EMAIL", qrCode.email.tos);
        }
        if (qrCode.email.ccs != null) {
            intent.putExtra("android.intent.extra.CC", qrCode.email.ccs);
        }
        if (qrCode.email.bccs != null) {
            intent.putExtra("android.intent.extra.BCC", qrCode.email.bccs);
        }
        putExtra(intent, "android.intent.extra.SUBJECT", qrCode.email.subject);
        putExtra(intent, "android.intent.extra.TEXT", qrCode.email.body);
        intent.setType("text/plain");
        return intent;
    }

    public static Intent makeIntentForOpenUrl(QRCode qrCode) {
        String url = qrCode.url.url;
        if (url.startsWith("HTTP://")) {
            url = "http" + url.substring(4);
        } else if (url.startsWith("HTTPS://")) {
            url = "https" + url.substring(5);
        }
        return new Intent("android.intent.action.VIEW", Uri.parse(url));
    }

    public static Intent makeIntentForSendSms(QRCode qrCode) {
        Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:" + qrCode.sms.phoneNumbers[0]));
        putExtra(intent, "sms_body", qrCode.sms.body);
        return intent;
    }

    public static Intent makeIntentForDialPhone(QRCode qrCode) {
        return new Intent("android.intent.action.DIAL", Uri.parse("tel:" + qrCode.phoneNumber));
    }

    public static Intent makeIntentForAddCalendar(QRCode qrCode) {
        return new Intent("android.intent.action.INSERT").setData(Events.CONTENT_URI).putExtra("beginTime", qrCode.calendar.start).putExtra("allDay", qrCode.calendar.startAllDay).putExtra("endTime", qrCode.calendar.end).putExtra("title", qrCode.calendar.summary).putExtra("description", qrCode.calendar.description).putExtra("calendar_id", 2).putExtra("eventLocation", qrCode.calendar.location);
    }

    public static Intent makeIntentForGeo(QRCode qrCode) {
        StringBuilder result = new StringBuilder();
        result.append("geo:");
        result.append(qrCode.geo.latitude);
        result.append(',');
        result.append(qrCode.geo.longitude);
        if (qrCode.geo.altitude > 0.0d) {
            result.append(',');
            result.append(qrCode.geo.altitude);
        }
        if (qrCode.geo.query != null) {
            result.append('?');
            result.append(qrCode.geo.query);
        }
        return new Intent("android.intent.action.VIEW", Uri.parse(result.toString()));
    }

    public static Intent makeIntentForWebSearch(QRCode qrCode) {
        Intent intent = new Intent("android.intent.action.WEB_SEARCH");
        intent.putExtra("query", qrCode.rawValue);
        return intent;
    }

    public static String convertToQuotedString(String string) {
        if (string == null) {
            return null;
        }
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int lastPos = string.length() - 1;
        if (lastPos >= 0) {
            return (string.charAt(0) == '\"' && string.charAt(lastPos) == '\"') ? string : '\"' + string + '\"';
        } else {
            return string;
        }
    }

    public static boolean isHexWepKey(CharSequence wepKey) {
        if (wepKey == null) {
            return false;
        }
        int length = wepKey.length();
        if ((length == 10 || length == 26 || length == 58) && HEX_DIGITS.matcher(wepKey).matches()) {
            return true;
        }
        return false;
    }

    private static int toEmailContractType(String typeString) {
        return doToContractType(typeString, EMAIL_TYPE_STRINGS, EMAIL_TYPE_VALUES);
    }

    private static int toPhoneContractType(String typeString) {
        return doToContractType(typeString, PHONE_TYPE_STRINGS, PHONE_TYPE_VALUES);
    }

    private static int toAddressContractType(String typeString) {
        return doToContractType(typeString, ADDRESS_TYPE_STRINGS, ADDRESS_TYPE_VALUES);
    }

    private static int doToContractType(String typeString, String[] types, int[] values) {
        if (typeString == null) {
            return -1;
        }
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            if (typeString.startsWith(type) || typeString.startsWith(type.toUpperCase(Locale.ENGLISH))) {
                return values[i];
            }
        }
        return -1;
    }
}
