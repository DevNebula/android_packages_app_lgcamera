package com.lge.camera.qrcode;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class QRCode implements Parcelable {
    public static final int CALENDAR = 8;
    public static final int CONTACT = 1;
    public static final Creator<QRCode> CREATOR = new C13751();
    public static final int EMAIL = 2;
    public static final int GEO = 5;
    public static final int SMS = 7;
    public static final int TEL = 6;
    public static final int TEXT = 4;
    public static final int URL = 3;
    public static final int WIFI = 9;
    public ICalendar calendar;
    public IContact contact;
    public IEmail email;
    public IGeo geo;
    public String phoneNumber;
    public String rawValue;
    public Rect rect;
    public ISms sms;
    public String summary;
    public int type;
    public IUrl url;
    public IWifi wifi;

    /* renamed from: com.lge.camera.qrcode.QRCode$1 */
    static class C13751 implements Creator<QRCode> {
        C13751() {
        }

        public QRCode createFromParcel(Parcel in) {
            return new QRCode(in, null);
        }

        public QRCode[] newArray(int size) {
            return new QRCode[size];
        }
    }

    public static class ICalendar implements Parcelable {
        public static final Creator<ICalendar> CREATOR = new C13761();
        public String[] attendees;
        public String description;
        public long end;
        public boolean endAllDay;
        public double latitude;
        public String location;
        public double longitude;
        public String organizer;
        public long start;
        public boolean startAllDay;
        public String summary;

        /* renamed from: com.lge.camera.qrcode.QRCode$ICalendar$1 */
        static class C13761 implements Creator<ICalendar> {
            C13761() {
            }

            public ICalendar createFromParcel(Parcel in) {
                return new ICalendar(in);
            }

            public ICalendar[] newArray(int size) {
                return new ICalendar[size];
            }
        }

        public ICalendar(String summary, long start, boolean startAllDay, long end, boolean endAllDay, String location, String organizer, String[] attendees, String description, double latitude, double longitude) {
            this.summary = summary;
            this.start = start;
            this.startAllDay = startAllDay;
            this.end = end;
            this.endAllDay = endAllDay;
            this.location = location;
            this.organizer = organizer;
            this.attendees = attendees;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
            dest.writeString(this.summary);
            dest.writeLong(this.start);
            dest.writeInt(this.startAllDay ? 1 : 0);
            dest.writeLong(this.end);
            if (!this.endAllDay) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeString(this.location);
            dest.writeString(this.organizer);
            dest.writeStringArray(this.attendees);
            dest.writeString(this.description);
            dest.writeDouble(this.latitude);
            dest.writeDouble(this.longitude);
        }

        public ICalendar(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            boolean z = true;
            this.summary = in.readString();
            this.start = in.readLong();
            this.startAllDay = in.readInt() == 1;
            this.end = in.readLong();
            if (in.readInt() != 1) {
                z = false;
            }
            this.endAllDay = z;
            this.location = in.readString();
            this.organizer = in.readString();
            this.attendees = in.createStringArray();
            this.description = in.readString();
            this.latitude = in.readDouble();
            this.longitude = in.readDouble();
        }
    }

    public static class IContact implements Parcelable {
        public static final Creator<IContact> CREATOR = new C13771();
        public String[] address;
        public String[] addressType;
        public String birthday;
        public String[] email;
        public String[] emailType;
        public String[] geo;
        public String instantMessenger;
        public String[] name;
        public String[] nickname;
        public String note;
        public String org;
        public String[] phoneNumber;
        public String[] phoneType;
        public String pronunciation;
        public String title;
        public String[] url;

        /* renamed from: com.lge.camera.qrcode.QRCode$IContact$1 */
        static class C13771 implements Creator<IContact> {
            C13771() {
            }

            public IContact createFromParcel(Parcel in) {
                return new IContact(in);
            }

            public IContact[] newArray(int size) {
                return new IContact[size];
            }
        }

        public IContact(String[] name, String[] nickname, String pronunciation, String[] phoneNumber, String[] phoneType, String[] email, String[] emailType, String note, String instantMessenger, String[] address, String[] addressType, String org, String title, String[] url, String birthday, String[] geo) {
            this.name = name;
            this.nickname = nickname;
            this.pronunciation = pronunciation;
            this.phoneNumber = phoneNumber;
            this.phoneType = phoneType;
            this.email = email;
            this.emailType = emailType;
            this.note = note;
            this.instantMessenger = instantMessenger;
            this.address = address;
            this.addressType = addressType;
            this.org = org;
            this.title = title;
            this.url = url;
            this.birthday = birthday;
            this.geo = geo;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(this.name);
            dest.writeStringArray(this.nickname);
            dest.writeString(this.pronunciation);
            dest.writeStringArray(this.phoneNumber);
            dest.writeStringArray(this.phoneType);
            dest.writeStringArray(this.email);
            dest.writeStringArray(this.emailType);
            dest.writeString(this.note);
            dest.writeString(this.instantMessenger);
            dest.writeStringArray(this.address);
            dest.writeStringArray(this.addressType);
            dest.writeString(this.org);
            dest.writeString(this.title);
            dest.writeStringArray(this.url);
            dest.writeString(this.birthday);
            dest.writeStringArray(this.geo);
        }

        public IContact(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            this.name = in.createStringArray();
            this.nickname = in.createStringArray();
            this.pronunciation = in.readString();
            this.phoneNumber = in.createStringArray();
            this.phoneType = in.createStringArray();
            this.email = in.createStringArray();
            this.emailType = in.createStringArray();
            this.note = in.readString();
            this.instantMessenger = in.readString();
            this.address = in.createStringArray();
            this.addressType = in.createStringArray();
            this.org = in.readString();
            this.title = in.readString();
            this.url = in.createStringArray();
            this.birthday = in.readString();
            this.geo = in.createStringArray();
        }
    }

    public static class IEmail implements Parcelable {
        public static final Creator<IEmail> CREATOR = new C13781();
        public String[] bccs;
        public String body;
        public String[] ccs;
        public String subject;
        public String[] tos;

        /* renamed from: com.lge.camera.qrcode.QRCode$IEmail$1 */
        static class C13781 implements Creator<IEmail> {
            C13781() {
            }

            public IEmail createFromParcel(Parcel in) {
                return new IEmail(in, null);
            }

            public IEmail[] newArray(int size) {
                return new IEmail[size];
            }
        }

        /* synthetic */ IEmail(Parcel x0, C13751 x1) {
            this(x0);
        }

        public IEmail(String[] tos, String[] ccs, String[] bccs, String subject, String body) {
            this.tos = tos;
            this.ccs = ccs;
            this.bccs = bccs;
            this.subject = subject;
            this.body = body;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(this.tos);
            dest.writeStringArray(this.ccs);
            dest.writeStringArray(this.bccs);
            dest.writeString(this.subject);
            dest.writeString(this.body);
        }

        private IEmail(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            this.tos = in.createStringArray();
            this.ccs = in.createStringArray();
            this.bccs = in.createStringArray();
            this.subject = in.readString();
            this.body = in.readString();
        }
    }

    public static class IGeo implements Parcelable {
        public static final Creator<IGeo> CREATOR = new C13791();
        public double altitude;
        public double latitude;
        public double longitude;
        public String query;

        /* renamed from: com.lge.camera.qrcode.QRCode$IGeo$1 */
        static class C13791 implements Creator<IGeo> {
            C13791() {
            }

            public IGeo createFromParcel(Parcel in) {
                return new IGeo(in, null);
            }

            public IGeo[] newArray(int size) {
                return new IGeo[size];
            }
        }

        /* synthetic */ IGeo(Parcel x0, C13751 x1) {
            this(x0);
        }

        public IGeo(double latitude, double longitude, double altitude, String query) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.query = query;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(this.latitude);
            dest.writeDouble(this.longitude);
            dest.writeDouble(this.altitude);
            dest.writeString(this.query);
        }

        private IGeo(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            this.latitude = in.readDouble();
            this.longitude = in.readDouble();
            this.altitude = in.readDouble();
            this.query = in.readString();
        }
    }

    public static class ISms implements Parcelable {
        public static final Creator<ISms> CREATOR = new C13801();
        public String body;
        public String[] phoneNumbers;
        public String subject;
        public String[] vias;

        /* renamed from: com.lge.camera.qrcode.QRCode$ISms$1 */
        static class C13801 implements Creator<ISms> {
            C13801() {
            }

            public ISms createFromParcel(Parcel in) {
                return new ISms(in, null);
            }

            public ISms[] newArray(int size) {
                return new ISms[size];
            }
        }

        /* synthetic */ ISms(Parcel x0, C13751 x1) {
            this(x0);
        }

        public ISms(String phoneNumber, String via, String subject, String body) {
            this.phoneNumbers = new String[]{phoneNumber};
            this.vias = new String[]{via};
            this.subject = subject;
            this.body = body;
        }

        public ISms(String[] phoneNumbers, String[] vias, String subject, String body) {
            this.phoneNumbers = phoneNumbers;
            this.vias = vias;
            this.subject = subject;
            this.body = body;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringArray(this.phoneNumbers);
            dest.writeStringArray(this.vias);
            dest.writeString(this.subject);
            dest.writeString(this.body);
        }

        private ISms(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            this.phoneNumbers = in.createStringArray();
            this.vias = in.createStringArray();
            this.subject = in.readString();
            this.body = in.readString();
        }
    }

    public static class IUrl implements Parcelable {
        public static final Creator<IUrl> CREATOR = new C13811();
        public String title;
        public String url;

        /* renamed from: com.lge.camera.qrcode.QRCode$IUrl$1 */
        static class C13811 implements Creator<IUrl> {
            C13811() {
            }

            public IUrl createFromParcel(Parcel in) {
                return new IUrl(in, null);
            }

            public IUrl[] newArray(int size) {
                return new IUrl[size];
            }
        }

        public IUrl(String url, String title) {
            this.url = url;
            this.title = title;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeString(this.title);
        }

        private IUrl(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            this.url = in.readString();
            this.title = in.readString();
        }
    }

    public static class IWifi implements Parcelable {
        public static final Creator<IWifi> CREATOR = new C13821();
        public boolean hidden;
        public String networkEncryption;
        public String password;
        public String ssid;

        /* renamed from: com.lge.camera.qrcode.QRCode$IWifi$1 */
        static class C13821 implements Creator<IWifi> {
            C13821() {
            }

            public IWifi createFromParcel(Parcel in) {
                return new IWifi(in, null);
            }

            public IWifi[] newArray(int size) {
                return new IWifi[size];
            }
        }

        /* synthetic */ IWifi(Parcel x0, C13751 x1) {
            this(x0);
        }

        public IWifi(String ssid, String networkEncryption, String password, boolean hidden) {
            this.ssid = ssid;
            this.networkEncryption = networkEncryption;
            this.password = password;
            this.hidden = hidden;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.ssid);
            dest.writeString(this.networkEncryption);
            dest.writeString(this.password);
            dest.writeInt(this.hidden ? 1 : 0);
        }

        private IWifi(Parcel in) {
            readFromParcel(in);
        }

        public void readFromParcel(Parcel in) {
            boolean z = true;
            this.ssid = in.readString();
            this.networkEncryption = in.readString();
            this.password = in.readString();
            if (in.readInt() != 1) {
                z = false;
            }
            this.hidden = z;
        }
    }

    /* synthetic */ QRCode(Parcel x0, C13751 x1) {
        this(x0);
    }

    public QRCode(String rv, int type, String summary, Rect rect, IContact iContact, IEmail iEmail, IUrl iUrl, IGeo iGeo, String phoneNumber, ISms iSms, ICalendar iCalendar, IWifi iWifi) {
        this.rawValue = rv;
        this.type = type;
        this.summary = summary;
        this.rect = new Rect(rect.left, rect.top, rect.right, rect.bottom);
        this.contact = new IContact(iContact.name, iContact.nickname, iContact.pronunciation, iContact.phoneNumber, iContact.phoneType, iContact.email, iContact.emailType, iContact.note, iContact.instantMessenger, iContact.address, iContact.addressType, iContact.org, iContact.title, iContact.url, iContact.birthday, iContact.geo);
        this.email = new IEmail(iEmail.tos, iEmail.ccs, iEmail.bccs, iEmail.subject, iEmail.body);
        this.url = new IUrl(iUrl.url, iUrl.title);
        this.geo = new IGeo(iGeo.latitude, iGeo.longitude, iGeo.altitude, iGeo.query);
        this.phoneNumber = phoneNumber;
        this.sms = new ISms(iSms.phoneNumbers, iSms.vias, iSms.body, iSms.subject);
        this.calendar = new ICalendar(iCalendar.summary, iCalendar.start, iCalendar.startAllDay, iCalendar.end, iCalendar.endAllDay, iCalendar.location, iCalendar.organizer, iCalendar.attendees, iCalendar.description, iCalendar.latitude, iCalendar.longitude);
        this.wifi = new IWifi(iWifi.ssid, iWifi.networkEncryption, iWifi.password, iWifi.hidden);
    }

    private QRCode(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.rawValue);
        dest.writeInt(this.type);
        dest.writeString(this.summary);
        dest.writeParcelable(this.rect, 0);
        dest.writeParcelable(this.contact, 0);
        dest.writeParcelable(this.email, 0);
        dest.writeParcelable(this.url, 0);
        dest.writeParcelable(this.geo, 0);
        dest.writeString(this.phoneNumber);
        dest.writeParcelable(this.sms, 0);
        dest.writeParcelable(this.calendar, 0);
        dest.writeParcelable(this.wifi, 0);
    }

    public void readFromParcel(Parcel in) {
        this.rawValue = in.readString();
        this.type = in.readInt();
        this.summary = in.readString();
        this.rect = (Rect) in.readParcelable(Rect.class.getClassLoader());
        this.contact = (IContact) in.readParcelable(IContact.class.getClassLoader());
        this.email = (IEmail) in.readParcelable(IEmail.class.getClassLoader());
        this.url = (IUrl) in.readParcelable(IUrl.class.getClassLoader());
        this.geo = (IGeo) in.readParcelable(IGeo.class.getClassLoader());
        this.phoneNumber = in.readString();
        this.sms = (ISms) in.readParcelable(ISms.class.getClassLoader());
        this.calendar = (ICalendar) in.readParcelable(ICalendar.class.getClassLoader());
        this.wifi = (IWifi) in.readParcelable(IWifi.class.getClassLoader());
    }
}
