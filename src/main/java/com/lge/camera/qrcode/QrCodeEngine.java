package com.lge.camera.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.WindowManager;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.CalendarParsedResult;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.URIParsedResult;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.lge.camera.qrcode.QRCode.ICalendar;
import com.lge.camera.qrcode.QRCode.IContact;
import com.lge.camera.qrcode.QRCode.IEmail;
import com.lge.camera.qrcode.QRCode.IGeo;
import com.lge.camera.qrcode.QRCode.ISms;
import com.lge.camera.qrcode.QRCode.IUrl;
import com.lge.camera.qrcode.QRCode.IWifi;
import com.lge.camera.util.BitmapManagingUtil;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

public class QrCodeEngine {
    private static final int MSG_CALLBACK_QRCODE_DETECTED = 2;
    private static final int MSG_DELAY_TIME = 300;
    private static final int MSG_REQUEST_QRCODE_DETECTION = 1;
    private static final String TAG = "QrCodeEngine";
    private Handler mCallbackHandler = new Handler(new C13842());
    private Context mContext;
    private boolean mDetectMode;
    private int mPreviewH = 0;
    private int mPreviewW = 0;
    private QRCode mQRCode;
    private Handler mQRCodeHandler = new Handler(new C13831());
    private Stack<byte[]> mQRCodeStack;
    private QrInterface mQrInterface;
    private int mResizeH = 0;
    private int mResizeW = 0;
    private int mUnknownQRCodeCount;

    public interface QrInterface {
        boolean isPaused();

        void onQrCodeDetected(QRCode qRCode);
    }

    /* renamed from: com.lge.camera.qrcode.QrCodeEngine$1 */
    class C13831 implements Callback {
        C13831() {
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (QrCodeEngine.this.mQRCodeStack != null && !QrCodeEngine.this.mQRCodeStack.isEmpty()) {
                        byte[] stream = (byte[]) QrCodeEngine.this.mQRCodeStack.pop();
                        new QRCodeAsyncTask(QrCodeEngine.this.mPreviewW, QrCodeEngine.this.mPreviewH, QrCodeEngine.this.mResizeW, QrCodeEngine.this.mResizeH).execute(new byte[][]{stream});
                        break;
                    }
                    QrCodeEngine.this.mQRCodeHandler.sendEmptyMessageDelayed(1, 300);
                    break;
            }
            return false;
        }
    }

    /* renamed from: com.lge.camera.qrcode.QrCodeEngine$2 */
    class C13842 implements Callback {
        C13842() {
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (QrCodeEngine.this.mQRCode != null) {
                        QrCodeEngine.this.mQrInterface.onQrCodeDetected(QrCodeEngine.this.mQRCode);
                        QrCodeEngine.this.mUnknownQRCodeCount = 0;
                    } else if (QrCodeEngine.this.mUnknownQRCodeCount > 5) {
                        QrCodeEngine.this.mQrInterface.onQrCodeDetected(QrCodeEngine.this.mQRCode);
                        QrCodeEngine.this.mUnknownQRCodeCount = 0;
                    } else {
                        QrCodeEngine.this.mUnknownQRCodeCount = QrCodeEngine.this.mUnknownQRCodeCount + 1;
                    }
                    CamLog.m3d(QrCodeEngine.TAG, "re-request qr code detection");
                    QrCodeEngine.this.mQRCodeHandler.sendEmptyMessage(1);
                    break;
            }
            return false;
        }
    }

    class QRCodeAsyncTask extends AsyncTask<byte[], Integer, Void> {
        private int mPreviewH;
        private int mPreviewW;
        private int mResizeH;
        private int mResizeW;

        public QRCodeAsyncTask(int previewW, int previewH, int resizeW, int resizeH) {
            this.mPreviewW = previewW;
            this.mPreviewH = previewH;
            this.mResizeW = resizeW;
            this.mResizeH = resizeH;
        }

        protected Void doInBackground(byte[]... stream) {
            if (QrCodeEngine.this.mDetectMode) {
                QrCodeEngine.this.mQRCode = QrCodeEngine.this.detectQRCode(stream[0], this.mResizeW, this.mResizeH, this.mPreviewW, this.mPreviewH);
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            if (QrCodeEngine.this.mDetectMode) {
                QrCodeEngine.this.mCallbackHandler.sendEmptyMessage(2);
            } else {
                cancel(true);
            }
        }

        protected void onCancelled() {
            cancel(true);
        }
    }

    public QrCodeEngine(Context context, QrInterface qrInterface) {
        this.mContext = context;
        this.mQrInterface = qrInterface;
    }

    public void sendPreviewImage(byte[] stream, int resizeW, int resizeH, int previewW, int previewH) {
        if (this.mDetectMode && !this.mQrInterface.isPaused()) {
            this.mResizeW = resizeW;
            this.mResizeH = resizeH;
            this.mPreviewW = previewW;
            this.mPreviewH = previewH;
            if (this.mQRCodeStack == null) {
                this.mQRCodeStack = new Stack();
                this.mQRCodeHandler.sendEmptyMessage(1);
            }
            this.mQRCodeStack.push(stream);
        }
    }

    public void startDetection() {
        this.mDetectMode = true;
    }

    public void stopDetection() {
        this.mQRCodeHandler.removeMessages(1);
        this.mQRCodeHandler.removeMessages(2);
        this.mDetectMode = false;
        if (this.mQRCodeStack != null) {
            this.mQRCodeStack.clear();
            this.mQRCodeStack = null;
        }
    }

    private QRCode detectQRCode(byte[] stream, int resizeW, int resizeH, int previewW, int previewH) {
        int degree = 0;
        switch (((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation()) {
            case 0:
                degree = 90;
                break;
            case 1:
                degree = 0;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) degree);
        Bitmap bitmap = BitmapManagingUtil.getBitmapFromByteArray(stream, resizeW, resizeH, matrix);
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getHeight() < resizeW || bitmap.getWidth() < resizeH) {
            CamLog.m5e(TAG, "illegal size");
            return null;
        }
        int[] intArray = new int[(resizeW * resizeH)];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, resizeH, resizeW);
        BinaryBitmap binaryBmp = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray)));
        QRCodeMultiReader multiReader = new QRCodeMultiReader();
        Result result = null;
        if (multiReader != null) {
            try {
                Result[] resultArray = multiReader.decodeMultiple(binaryBmp);
                if (resultArray != null && resultArray.length > 0) {
                    result = getCenterNearestResult(resultArray, previewH, previewW);
                }
            } catch (NotFoundException e) {
                if (bitmap == null || bitmap.isRecycled()) {
                    return null;
                }
                bitmap.recycle();
                return null;
            } catch (IndexOutOfBoundsException e2) {
                if (bitmap == null || bitmap.isRecycled()) {
                    return null;
                }
                bitmap.recycle();
                return null;
            } catch (Throwable th) {
                if (bitmap == null || bitmap.isRecycled()) {
                    return null;
                }
                bitmap.recycle();
                return null;
            }
        }
        QRCode qrCode = createIQRCode(result, previewH, previewW);
        if (bitmap == null || bitmap.isRecycled()) {
            return qrCode;
        }
        bitmap.recycle();
        return qrCode;
    }

    private Result getCenterNearestResult(Result[] results, int width, int height) {
        ArrayList<Double> arrayList = new ArrayList();
        ArrayList<ResultPoint> rectArrayList = new ArrayList();
        HashMap<Double, Result> hm = new HashMap();
        int length = results.length;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                Result r = results[i2];
                for (ResultPoint rp : r.getResultPoints()) {
                    rectArrayList.add(rp);
                }
                Rect rect = getBoundaryRect(rectArrayList, width, height);
                float rectCenterX = (float) ((rect.left + rect.right) / 2);
                float rectCenterY = (float) ((rect.top + rect.bottom) / 2);
                double distanceFromCenter = Math.sqrt((double) (((((float) width) - rectCenterX) * (((float) width) - rectCenterX)) + ((((float) height) - rectCenterY) * (((float) height) - rectCenterY))));
                hm.put(Double.valueOf(distanceFromCenter), r);
                arrayList.add(Double.valueOf(distanceFromCenter));
                i = i2 + 1;
            } else {
                Collections.sort(arrayList);
                return (Result) hm.get(Double.valueOf(((Double) arrayList.get(0)).doubleValue()));
            }
        }
    }

    private String getSummaryFromResult(ParsedResult parsedResult) {
        return parsedResult.getDisplayResult();
    }

    private Rect getBoundaryRect(ArrayList<ResultPoint> rectArrayList, int boundaryEnd, int boundaryBottom) {
        if (rectArrayList == null) {
            return null;
        }
        ArrayList<Integer> alx = new ArrayList();
        ArrayList<Integer> aly = new ArrayList();
        for (int i = 0; i < rectArrayList.size(); i++) {
            alx.add(Integer.valueOf((int) ((ResultPoint) rectArrayList.get(i)).getX()));
            aly.add(Integer.valueOf((int) ((ResultPoint) rectArrayList.get(i)).getY()));
        }
        Collections.sort(alx);
        Collections.sort(aly);
        int tempLeft = ((Integer) alx.get(0)).intValue();
        int tempRight = ((Integer) alx.get(rectArrayList.size() - 1)).intValue();
        int offset = (tempRight - tempLeft) / 5;
        return new Rect(Math.max(tempLeft - offset, 0), Math.max(((Integer) aly.get(0)).intValue() - offset, 0), Math.min(tempRight + offset, boundaryEnd), Math.min(((Integer) aly.get(rectArrayList.size() - 1)).intValue() + offset, boundaryBottom));
    }

    private IContact getContactInfo(ParsedResult parsedResult) {
        AddressBookParsedResult addressResult = (AddressBookParsedResult) parsedResult;
        return new IContact(addressResult.getNames(), addressResult.getNicknames(), addressResult.getPronunciation(), addressResult.getPhoneNumbers(), addressResult.getPhoneTypes(), addressResult.getEmails(), addressResult.getEmailTypes(), addressResult.getNote(), addressResult.getInstantMessenger(), addressResult.getAddresses(), addressResult.getAddressTypes(), addressResult.getOrg(), addressResult.getTitle(), addressResult.getURLs(), addressResult.getBirthday(), addressResult.getGeo());
    }

    private IEmail getEmailInfo(ParsedResult parsedResult) {
        EmailAddressParsedResult emailAddressParsedResult = (EmailAddressParsedResult) parsedResult;
        return new IEmail(emailAddressParsedResult.getTos(), emailAddressParsedResult.getCCs(), emailAddressParsedResult.getBCCs(), emailAddressParsedResult.getSubject(), emailAddressParsedResult.getBody());
    }

    private IUrl getUrlInfo(ParsedResult parsedResult) {
        URIParsedResult urlParsedResult = (URIParsedResult) parsedResult;
        return new IUrl(urlParsedResult.getURI(), urlParsedResult.getTitle());
    }

    private ISms getSmsInfo(ParsedResult parsedResult) {
        SMSParsedResult smsParsedResult = (SMSParsedResult) parsedResult;
        return new ISms(smsParsedResult.getNumbers(), smsParsedResult.getVias(), smsParsedResult.getSubject(), smsParsedResult.getBody());
    }

    private String getPhoneNumber(ParsedResult parsedResult) {
        return ((TelParsedResult) parsedResult).getNumber();
    }

    private IWifi getWifiInfo(ParsedResult parsedResult) {
        WifiParsedResult wifiParsedResult = (WifiParsedResult) parsedResult;
        return new IWifi(wifiParsedResult.getSsid(), wifiParsedResult.getNetworkEncryption(), wifiParsedResult.getPassword(), wifiParsedResult.isHidden());
    }

    private IGeo getGeoInfo(ParsedResult parsedResult) {
        GeoParsedResult geoParsedResult = (GeoParsedResult) parsedResult;
        return new IGeo(geoParsedResult.getLatitude(), geoParsedResult.getLongitude(), geoParsedResult.getAltitude(), geoParsedResult.getQuery());
    }

    private ICalendar getCalendarInfo(ParsedResult parsedResult) {
        CalendarParsedResult calendarParsedResult = (CalendarParsedResult) parsedResult;
        return new ICalendar(calendarParsedResult.getSummary(), calendarParsedResult.getStart().getTime(), calendarParsedResult.isStartAllDay(), calendarParsedResult.getEnd().getTime(), calendarParsedResult.isEndAllDay(), calendarParsedResult.getLocation(), calendarParsedResult.getOrganizer(), calendarParsedResult.getAttendees(), calendarParsedResult.getDescription(), calendarParsedResult.getLatitude(), calendarParsedResult.getLongitude());
    }

    private QRCode createIQRCode(Result result, int boundaryEnd, int boundaryBottom) {
        if (result == null) {
            return null;
        }
        ArrayList<ResultPoint> rectArrayList = new ArrayList();
        for (ResultPoint rp : result.getResultPoints()) {
            rectArrayList.add(rp);
        }
        String rawResult = result.getText();
        ParsedResult parsedResult = ResultParser.parseResult(result);
        int type = getTypeFromResult(parsedResult);
        String summary = getSummaryFromResult(parsedResult);
        Rect rect = getBoundaryRect(rectArrayList, boundaryEnd, boundaryBottom);
        IContact contact = new IContact();
        IEmail email = new IEmail();
        IUrl url = new IUrl();
        IGeo geo = new IGeo();
        String phoneNumber = null;
        ISms sms = new ISms();
        ICalendar calendar = new ICalendar();
        IWifi wifi = new IWifi();
        switch (type) {
            case 1:
                contact = getContactInfo(parsedResult);
                break;
            case 2:
                email = getEmailInfo(parsedResult);
                break;
            case 3:
                url = getUrlInfo(parsedResult);
                break;
            case 5:
                geo = getGeoInfo(parsedResult);
                break;
            case 6:
                phoneNumber = getPhoneNumber(parsedResult);
                break;
            case 7:
                sms = getSmsInfo(parsedResult);
                break;
            case 8:
                calendar = getCalendarInfo(parsedResult);
                break;
            case 9:
                wifi = getWifiInfo(parsedResult);
                break;
        }
        return new QRCode(rawResult, type, summary, rect, contact, email, url, geo, phoneNumber, sms, calendar, wifi);
    }

    private int getTypeFromResult(ParsedResult parsedResult) {
        ParsedResultType parsedResultType = parsedResult.getType();
        if (parsedResultType == ParsedResultType.ADDRESSBOOK) {
            return 1;
        }
        if (parsedResultType == ParsedResultType.CALENDAR) {
            return 8;
        }
        if (parsedResultType == ParsedResultType.EMAIL_ADDRESS) {
            return 2;
        }
        if (parsedResultType == ParsedResultType.GEO) {
            return 5;
        }
        if (parsedResultType == ParsedResultType.SMS) {
            return 7;
        }
        if (parsedResultType == ParsedResultType.TEL) {
            return 6;
        }
        if (parsedResultType == ParsedResultType.URI) {
            return 3;
        }
        if (parsedResultType == ParsedResultType.WIFI) {
            return 9;
        }
        return 4;
    }
}
