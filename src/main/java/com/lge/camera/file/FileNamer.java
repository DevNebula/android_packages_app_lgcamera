package com.lge.camera.file;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.MediaStore.Images.Media;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.constants.MultimediaProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.SecureImageUtil;
import com.lge.camera.util.SharedPreferenceUtil;
import com.lge.camera.util.SharedPreferenceUtilBase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FileNamer {
    private static final int BURSTID_LENGTH_DCM = 8;
    private static final int BURSTID_LENGTH_NEWNAMING = 15;
    private static final int BURSTID_LENGTH_VZW = 10;
    private static final int FILE_NUM_MAX = 999;
    private static final int FILE_NUM_MAX_DCM = 99999;
    private static final int STATUS_NOT_READY = 0;
    private static final int STATUS_READY = 1;
    private static FileNamer sFileNamer = null;
    private final String NOT_BURST = "not_burst";
    private int mBurstCount = 0;
    private long mBurstCountAsc = 96;
    private String mBurstFirstTimeTemp = "";
    private Thread mCheckFileNameThread = null;
    private int mCheckingDCFStatus = 0;
    private int mCheckingNormalStatus = 0;
    private String mCurBurstIdForSecure = "";
    private int mDCF1stNumber;
    private String mDCFFileName;
    private long mDCFNumber;
    private int mDigitnum;
    private String mFileName;
    private ArrayList<String> mFileNameInSaving;
    private long mFileNumber;
    private long mGIFCountAsc = 97;
    private boolean mIsCheckingDCF;
    private boolean mIsCheckingNormal;
    private boolean mIsThreadStopped = false;
    private Object mLock = new Object();
    private String mPrevBurstTimeForSecure = "";
    private int mStorageOldState = -2;
    private int mStorageState;
    private String mTakeTime = "";
    private int mTempAsc = 0;
    private String sBurstFirstNameDCM = "";
    private String sBurstFirstTime = "";

    public static FileNamer get() {
        if (sFileNamer == null) {
            sFileNamer = new FileNamer();
        }
        return sFileNamer;
    }

    private FileNamer() {
    }

    public void startFileNamer(Context context, String dir, int storage, boolean useThread) {
        if (!ModelProperties.isUseNewNamingRule()) {
            CamLog.m7i(CameraConstants.TAG, "create()-start");
            initializeNumber(context, storage);
            if (ModelProperties.useDCFRule()) {
                generateFileName_DCF(context, dir, useThread);
            } else {
                generateFileName_Normal(context, storage, dir, useThread);
            }
            CamLog.m7i(CameraConstants.TAG, "create()-end");
        }
    }

    public String getFileName(Context context, int mode, int storage, String dir, boolean useThread) {
        String ret = "";
        switch (ModelProperties.getCarrierCode()) {
            case 6:
                CamLog.m7i(CameraConstants.TAG, "getFileName for CDMA");
                ret = generateFileName_Date(dir, "");
                CamLog.m7i(CameraConstants.TAG, "get file name = " + ret);
                return ret;
            default:
                if (ModelProperties.useDCFRule() && this.mCheckingDCFStatus == 1) {
                    addDCFCount();
                    this.mCheckingDCFStatus = 0;
                    generateFileName_DCF(context, dir, useThread);
                    ret = this.mDCFFileName;
                    CamLog.m7i(CameraConstants.TAG, "get file name = " + ret);
                    return ret;
                } else if (this.mCheckingNormalStatus == 1) {
                    this.mCheckingNormalStatus = 0;
                    generateFileName_Normal(context, storage, dir, useThread);
                    this.mFileNumber++;
                    this.mFileName = makeFileName_Normal(mode, this.mFileNumber);
                    CamLog.m3d(CameraConstants.TAG, "mFileNumber = " + this.mFileNumber);
                    ret = this.mFileName;
                    CamLog.m7i(CameraConstants.TAG, "get file name = " + ret);
                    return ret;
                } else {
                    CamLog.m7i(CameraConstants.TAG, "error! get file name fail!");
                    return ret;
                }
        }
    }

    private void initializeNumber(Context context, int storage) {
        if (context == null) {
            CamLog.m3d(CameraConstants.TAG, "Cannot initialize file number because context is null");
            return;
        }
        this.mFileNumber = SharedPreferenceUtil.getAccumulatedMediaCount(context, storage);
        this.mCheckingNormalStatus = 0;
        this.mDCF1stNumber = SharedPreferenceUtil.getAccumulatedDCFFirstCount(context);
        this.mDCFNumber = SharedPreferenceUtil.getAccumulatedDCFCount(context);
        if (this.mDCF1stNumber == -1 && this.mDCFNumber == 0) {
            this.mDCFNumber = 1;
        }
        if (this.mDCF1stNumber == -1 || this.mDCF1stNumber == 0) {
            this.mDCF1stNumber = 48;
        }
        this.mDigitnum = SharedPreferenceUtil.getAccumulatedDCFDigit(context);
        if (this.mDigitnum == 0) {
            this.mDigitnum = 4;
        }
        this.mCheckingDCFStatus = 0;
    }

    private synchronized void generateFileName_Normal(Context context, int storage, String dir, boolean useThread) {
        generateFileName_Normal(context, storage, dir, useThread, null);
    }

    private synchronized void generateFileName_Normal(Context context, int storage, String dir, boolean useThread, String suffix) {
        CamLog.m7i(CameraConstants.TAG, "startCheckFileName useThread ? : " + useThread);
        stopThread();
        this.mIsThreadStopped = false;
        if (this.mCheckingNormalStatus == 1) {
            CamLog.m11w(CameraConstants.TAG, "mImageFileStatus == STATUS_READY");
        } else if (useThread) {
            final Context context2 = context;
            final int i = storage;
            final String str = dir;
            final String str2 = suffix;
            this.mCheckFileNameThread = new Thread(new Runnable() {
                public void run() {
                    CamLog.m7i(CameraConstants.TAG, "startCheckFileName_Normal with thread: " + FileNamer.this.mFileNumber);
                    long startTime = System.currentTimeMillis();
                    FileNamer.this.checkFileName_Normal(context2, i, str, Thread.interrupted(), str2);
                    CamLog.m7i(CameraConstants.TAG, "startCheckFileName_Normal with thread (time = " + (System.currentTimeMillis() - startTime) + "ms)");
                }
            });
            this.mCheckFileNameThread.start();
        } else {
            CamLog.m7i(CameraConstants.TAG, "startCheckFileName_Normal without thread: " + this.mFileNumber);
            long startTime = System.currentTimeMillis();
            checkFileName_Normal(context, storage, dir, false, suffix);
            CamLog.m7i(CameraConstants.TAG, "finished without thread (elapse time = " + (System.currentTimeMillis() - startTime) + "ms)");
        }
    }

    private void checkFileName_Normal(Context context, int storage, String dir, boolean useThread, String suffix) {
        this.mIsCheckingNormal = true;
        long fileNumber = this.mFileNumber;
        String pictureFilePath = "";
        String videoFilePath3G = "";
        String videoFilePathMP4 = "";
        while (true) {
            pictureFilePath = dir + makeFileName_Normal(0, fileNumber);
            if (suffix != null) {
                pictureFilePath = pictureFilePath + suffix;
            }
            pictureFilePath = pictureFilePath + ".jpg";
            videoFilePath3G = dir + makeFileName_Normal(1, fileNumber) + MultimediaProperties.VIDEO_EXTENSION_3GP;
            videoFilePathMP4 = dir + makeFileName_Normal(1, fileNumber) + ".mp4";
            if (!FileManager.isFileExist(pictureFilePath) && !FileManager.isFileExist(videoFilePath3G) && !FileManager.isFileExist(videoFilePathMP4) && !isFileExistInSavingList(pictureFilePath)) {
                break;
            }
            fileNumber++;
            if (this.mIsThreadStopped || (useThread && Thread.interrupted())) {
                break;
            }
        }
        this.mFileNumber = fileNumber;
        if (this.mIsThreadStopped) {
            CamLog.m11w(CameraConstants.TAG, "startCheckFileName is stop in Camera!");
            this.mIsCheckingNormal = false;
            this.mIsThreadStopped = false;
            return;
        }
        this.mCheckingNormalStatus = 1;
        this.mIsCheckingNormal = false;
        if (context != null) {
            SharedPreferenceUtil.saveAccumulatedMediaCount(context, storage, this.mFileNumber);
        }
    }

    private static String makeFileName_Normal(int purpose, long count) {
        boolean isDCM;
        String fileName = "";
        if (ModelProperties.getCarrierCode() == 4) {
            isDCM = true;
        } else {
            isDCM = false;
        }
        String prefix = (purpose == 0 || (purpose == 0 && isDCM)) ? "IMG" : "MOV";
        long maxCount = isDCM ? 99999 : 999;
        String specifier = isDCM ? "%s%05d" : "%s%03d";
        if (count > maxCount) {
            return String.format(Locale.US, "%s%d", new Object[]{prefix, Long.valueOf(count)});
        }
        return String.format(Locale.US, specifier, new Object[]{prefix, Long.valueOf(count)});
    }

    private synchronized void generateFileName_DCF(final Context context, final String dir, boolean useThread) {
        CamLog.m7i(CameraConstants.TAG, "startCheckFileName_DCF useThread : " + useThread);
        if (this.mCheckingDCFStatus == 1) {
            CamLog.m11w(CameraConstants.TAG, "mDCFFileStatus == STATUS_READY");
        } else {
            CamLog.m7i(CameraConstants.TAG, "startCheckFileName stopThread.");
            stopThread();
            this.mIsThreadStopped = false;
            if (useThread) {
                this.mCheckFileNameThread = new Thread(new Runnable() {
                    public void run() {
                        CamLog.m7i(CameraConstants.TAG, "startCheckFileName_DCF with thread : " + FileNamer.this.mDCF1stNumber + ", " + FileNamer.this.mDCFNumber);
                        long startTime = System.currentTimeMillis();
                        FileNamer.this.checkFileName_DCF(context, dir, Thread.interrupted());
                        CamLog.m7i(CameraConstants.TAG, "startCheckFileName_DCF is finished with thread (time = " + (System.currentTimeMillis() - startTime) + "ms)");
                    }
                });
                this.mCheckFileNameThread.start();
            } else {
                CamLog.m7i(CameraConstants.TAG, "startCheckFileName_DCF without thread : " + this.mDCF1stNumber + ", " + this.mDCFNumber);
                long startTime = System.currentTimeMillis();
                checkFileName_DCF(context, dir, false);
                CamLog.m7i(CameraConstants.TAG, "startCheckFileName_DCF is finished without thread (time = " + (System.currentTimeMillis() - startTime) + "ms)");
            }
        }
    }

    private void checkFileName_DCF(Context context, String dir, boolean useThread) {
        this.mIsCheckingDCF = true;
        String fileName = makeFileName_DCF(this.mDCF1stNumber, this.mDigitnum, this.mDCFNumber);
        while (true) {
            if (!FileManager.isFileExist(dir + fileName + ".jpg") && !FileManager.isFileExist(dir + fileName + MultimediaProperties.VIDEO_EXTENSION_3GP) && !FileManager.isFileExist(dir + fileName + ".mp4") && !isFileExistInSavingList(dir + fileName + ".jpg")) {
                break;
            }
            addDCFCount();
            fileName = makeFileName_DCF(this.mDCF1stNumber, this.mDigitnum, this.mDCFNumber);
            if (this.mIsThreadStopped || (useThread && Thread.interrupted())) {
                break;
            }
        }
        if (this.mIsThreadStopped) {
            CamLog.m11w(CameraConstants.TAG, "startCheckFileName_DCF is stop without Thread by DCF rules!");
            this.mIsCheckingDCF = false;
            this.mIsThreadStopped = false;
            return;
        }
        this.mDCFFileName = fileName;
        this.mCheckingDCFStatus = 1;
        CamLog.m7i(CameraConstants.TAG, "dcf file is ready " + this.mDCFFileName);
        this.mIsCheckingDCF = false;
        if (context != null) {
            SharedPreferenceUtilBase.saveAccumulatedDCFCount(context, this.mDCFNumber);
            SharedPreferenceUtilBase.saveAccumulatedDCFFirstCount(context, this.mDCF1stNumber);
            SharedPreferenceUtilBase.saveAccumulatedDCFDigit(context, this.mDigitnum);
        }
    }

    private static String makeFileName_DCF(int firstNumber, int digit, long count) {
        String fileName = String.format("CAM%s", new Object[]{Character.valueOf((char) firstNumber)});
        String fileNum = String.valueOf(count);
        int tmpNum = digit - fileNum.length();
        for (int i = 0; i < tmpNum; i++) {
            fileName = fileName + "0";
        }
        return fileName + fileNum;
    }

    private String generateFileName_Date(String dir, String shotMode) {
        CamLog.m7i(CameraConstants.TAG, "startCheckFileNameCDMA");
        this.mIsCheckingNormal = true;
        String fileName = makeCurrentDateToString_Date();
        long start = 96;
        while (true) {
            if (FileManager.isFileExist(dir + fileName + ".jpg") || FileManager.isFileExist(dir + fileName + ".mp4") || FileManager.isFileExist(dir + fileName + MultimediaProperties.VIDEO_EXTENSION_3GP)) {
                start++;
                fileName = makeFileName_Date(fileName, start);
            } else {
                this.mFileName = fileName;
                this.mIsCheckingNormal = false;
                return this.mFileName;
            }
        }
    }

    private String makeFileName_Date(String fileName, long AscCode) {
        if (fileName.length() >= 11) {
            fileName = fileName.substring(0, 10);
        }
        if (AscCode > 122) {
            return fileName + String.valueOf((char) ((int) 122)) + "[" + this.mTempAsc + "]";
        }
        this.mTempAsc = 0;
        return fileName + String.valueOf((char) ((int) AscCode));
    }

    private String makeCurrentDateToString_Date() {
        Date mDate = new Date();
        String ymd = new SimpleDateFormat("yyyyMMdd", Locale.US).format(mDate);
        String currentTime = ymd + new SimpleDateFormat("HHmmss", Locale.US).format(mDate);
        CamLog.m7i(CameraConstants.TAG, "currentTime = " + currentTime);
        String month = currentTime.substring(4, 6);
        String monthDay = currentTime.substring(6, 8);
        String year = currentTime.substring(2, 4);
        String hour = currentTime.substring(8, 10);
        String minute = currentTime.substring(10, 12);
        String fileName = month + "" + monthDay + "" + year + "" + hour + "" + minute;
        CamLog.m7i(CameraConstants.TAG, "fileName : " + month + "." + monthDay + "." + year + "." + hour + "." + minute);
        return fileName;
    }

    public boolean isFileNamerReady() {
        if (ModelProperties.useDCFRule()) {
            if (this.mCheckingDCFStatus == 1) {
                return true;
            }
            return false;
        } else if (this.mCheckingNormalStatus != 1) {
            return false;
        } else {
            return true;
        }
    }

    public void setStorageState(Context context, int storage, String dir, int state) {
        CamLog.m3d(CameraConstants.TAG, "setStorageState " + state);
        if (this.mStorageOldState != state) {
            this.mStorageOldState = state;
            if ((this.mStorageState & 1) == 1 && (this.mStorageState & 16) == 16) {
                CamLog.m11w(CameraConstants.TAG, "storage state :  AVAILABLE ");
                if (!isFileNamerReady()) {
                    CamLog.m11w(CameraConstants.TAG, "setStorageState : startCheckFileName with thread");
                    if (ModelProperties.useDCFRule()) {
                        generateFileName_DCF(context, dir, true);
                        return;
                    } else {
                        generateFileName_Normal(context, storage, dir, true);
                        return;
                    }
                }
                return;
            }
            CamLog.m11w(CameraConstants.TAG, "storage state : NOT AVAILABLE, " + this.mStorageState);
            stopThread();
            this.mCheckingNormalStatus = 0;
            this.mCheckingDCFStatus = 0;
            return;
        }
        CamLog.m3d(CameraConstants.TAG, "setStorageState: status same");
    }

    public void reload(Context context, int mode, int storage, String dir, boolean useThread) {
        if (!ModelProperties.isUseNewNamingRule()) {
            CamLog.m7i(CameraConstants.TAG, "filenamer reload call");
            if (ModelProperties.useDCFRule()) {
                this.mCheckingDCFStatus = 0;
                generateFileName_DCF(context, dir, useThread);
                return;
            }
            initializeNumber(context, storage);
            generateFileName_Normal(context, storage, dir, useThread);
        }
    }

    private void stopThread() {
        if (this.mCheckFileNameThread != null && this.mCheckFileNameThread.isAlive()) {
            this.mCheckFileNameThread.interrupt();
            try {
                this.mCheckFileNameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mCheckFileNameThread = null;
        this.mIsThreadStopped = true;
    }

    public void setErrorFeedback(int mode) {
        this.mFileNumber--;
        if (ModelProperties.useDCFRule()) {
            subtractDCFCount();
            CamLog.m11w(CameraConstants.TAG, "error feedback dcf = " + this.mDCF1stNumber + ", " + this.mDCFNumber);
        }
        CamLog.m11w(CameraConstants.TAG, "error feedback mdeia = " + this.mFileNumber);
    }

    public void close(Context context, int storage) {
        CamLog.m7i(CameraConstants.TAG, "FileNamingHelper close 1/3 " + this.mIsCheckingNormal);
        if (sFileNamer == null) {
            CamLog.m3d(CameraConstants.TAG, "Already close().");
            return;
        }
        if (this.mIsCheckingNormal || this.mIsCheckingDCF) {
            stopThread();
        }
        while (true) {
            if (!this.mIsCheckingNormal && !this.mIsCheckingDCF) {
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (context != null) {
            SharedPreferenceUtil.saveAccumulatedMediaCount(context, storage, this.mFileNumber);
            CamLog.m7i(CameraConstants.TAG, "FileNamingHelper close 2/3 mImageFileNumber:" + this.mFileNumber);
            SharedPreferenceUtilBase.saveAccumulatedDCFCount(context, this.mDCFNumber);
            SharedPreferenceUtilBase.saveAccumulatedDCFFirstCount(context, this.mDCF1stNumber);
            SharedPreferenceUtilBase.saveAccumulatedDCFDigit(context, this.mDigitnum);
            CamLog.m7i(CameraConstants.TAG, "FileNamingHelper close 3/3 mDCFNumber:" + this.mDCF1stNumber + "/" + this.mDCFNumber + "/" + this.mDigitnum);
        } else {
            CamLog.m3d(CameraConstants.TAG, "Cannot accumulate DCF because context is null");
        }
        releaseSavingList();
        this.mFileName = null;
        this.mDCFFileName = null;
        sFileNamer = null;
    }

    public void saveCountToPref(Context context, int storage) {
        if (context != null) {
            SharedPreferenceUtil.saveAccumulatedMediaCount(context, storage, this.mFileNumber);
        }
    }

    private void addDCFCount() {
        int digitNum = this.mDigitnum;
        int dcfFirstNumber = this.mDCF1stNumber;
        long dcfNumber = this.mDCFNumber + 1;
        if (Math.pow(10.0d, (double) digitNum) <= ((double) dcfNumber)) {
            dcfNumber = 0;
            if ((dcfFirstNumber > 47 && dcfFirstNumber < 57) || (dcfFirstNumber > 65 && dcfFirstNumber < 90)) {
                dcfFirstNumber++;
            } else if (dcfFirstNumber == 57) {
                dcfFirstNumber = 65;
            } else if (dcfFirstNumber == 90) {
                dcfFirstNumber = 48;
                digitNum++;
                dcfNumber = 1;
            }
        }
        this.mDigitnum = digitNum;
        this.mDCF1stNumber = dcfFirstNumber;
        this.mDCFNumber = dcfNumber;
    }

    private void subtractDCFCount() {
        this.mDCFNumber--;
        double tmpNum = Math.pow(10.0d, (double) this.mDigitnum);
        if (this.mDCF1stNumber == 48 && this.mDCFNumber == 0) {
            this.mDCFNumber = ((long) tmpNum) - 1;
            this.mDCF1stNumber = 90;
            this.mDigitnum--;
        } else if (this.mDCFNumber == -1) {
            this.mDCFNumber = ((long) tmpNum) - 1;
            if ((this.mDCF1stNumber > 48 && this.mDCF1stNumber < 58) || (this.mDCF1stNumber > 66 && this.mDCF1stNumber < 91)) {
                this.mDCF1stNumber--;
            } else if (this.mDCF1stNumber == 65) {
                this.mDCF1stNumber = 57;
            } else if (this.mDCF1stNumber == 48) {
                this.mDCF1stNumber = 90;
                this.mDigitnum--;
            }
        }
        CamLog.m7i(CameraConstants.TAG, "subtractDCFCount " + this.mDCF1stNumber + "/" + this.mDCFNumber + "/" + this.mDigitnum);
    }

    public String getFileNewName(Context context, int useType, int storage, String dir, boolean useThread, String shotMode) {
        return getFileNewName(context, useType, storage, dir, this.mTakeTime, useThread, shotMode);
    }

    public String getFileNewName(Context context, int useType, int storage, String dir, String markTime, boolean useThread, String shotMode) {
        if (!CameraConstants.MODE_BURST.equals(shotMode)) {
            this.mCurBurstIdForSecure = "not_burst";
        }
        String ret;
        if (ModelProperties.getCarrierCode() == 6) {
            ret = makeFileNameForVZW(useType, dir, shotMode);
            CamLog.m7i(CameraConstants.TAG, "getFileName for VZW : " + ret);
            return ret;
        } else if (ModelProperties.getCarrierCode() == 4) {
            ret = makeFileNameForDCM(context, storage, useType, dir, shotMode, useThread);
            CamLog.m7i(CameraConstants.TAG, "getFileName for DCF : " + ret);
            return ret;
        } else {
            ret = startCheckFileNamebyTime(useType, dir, markTime, shotMode);
            CamLog.m7i(CameraConstants.TAG, "getFileName for newNaming : " + ret);
            return ret;
        }
    }

    public String getFileNewNameMultiShot(Context context, int useType, int storage, String dir, boolean useThread, String shotMode, int countMultishot) {
        boolean isBurstShot = CameraConstants.MODE_BURST.equals(shotMode);
        if (countMultishot > 0) {
            if (isBurstShot) {
                processBurstCount(countMultishot, context);
            }
        } else if (isBurstShot) {
            this.sBurstFirstTime = "";
            this.sBurstFirstNameDCM = "";
        } else {
            this.mCurBurstIdForSecure = "not_burst";
        }
        return getFileNewName(context, useType, storage, dir, useThread, shotMode);
    }

    public void setBurstFirstTime(String takeTime) {
        if (ModelProperties.getCarrierCode() == 6) {
            String sYMD = new SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().getTime());
            String sYY = sYMD.substring(0, 4).substring(2, 4);
            String sMM = sYMD.substring(4, 6);
            this.mBurstFirstTimeTemp = sMM + sYMD.substring(6, 8) + sYY + takeTime.substring(0, 4);
            return;
        }
        this.mBurstFirstTimeTemp = getNamebyDate() + "_" + getNamebyTime();
    }

    public boolean isNewBurstShotInSecure() {
        if (!"".equals(this.mPrevBurstTimeForSecure) && (this.mPrevBurstTimeForSecure == null || this.mPrevBurstTimeForSecure.equals(this.mCurBurstIdForSecure))) {
            return false;
        }
        if (!"not_burst".equals(this.mCurBurstIdForSecure)) {
            this.mPrevBurstTimeForSecure = this.mCurBurstIdForSecure;
        }
        return true;
    }

    private String startCheckFileNamebyTime(int useType, String dir, String markTime, String shotMode) {
        String fullFileName = "";
        String fileName = "";
        int sameCount;
        if (useType == 0 || useType == 2) {
            fileName = makeFilenamebyTime(markTime, shotMode, useType);
            String ext = useType == 2 ? CameraConstants.CAM_RAW_EXTENSION : ".jpg";
            fullFileName = dir + fileName + ext;
            sameCount = 0;
            while (true) {
                if (!FileManager.isFileExist(fullFileName) && !isFileExistInSavingList(fullFileName)) {
                    break;
                }
                sameCount++;
                fullFileName = dir + fileName + "(" + Integer.toString(sameCount) + ")" + ext;
            }
            if (sameCount > 0) {
                fileName = fileName + "(" + Integer.toString(sameCount) + ")";
            }
            return fileName;
        }
        fileName = makeFilenamebyTime(markTime, shotMode, useType);
        fullFileName = dir + fileName;
        sameCount = 0;
        while (true) {
            if (!FileManager.isFileExist(fullFileName + ".mp4") && !FileManager.isFileExist(fullFileName + MultimediaProperties.VIDEO_EXTENSION_3GP)) {
                break;
            }
            sameCount++;
            fullFileName = fileName + "(" + Integer.toString(sameCount) + ")";
        }
        if (sameCount > 0) {
            fileName = fileName + "(" + Integer.toString(sameCount) + ")";
        }
        return fileName;
    }

    public void markTakeTime(String shotMode) {
        getTakeTime(shotMode);
    }

    public String getTakeTime(String shotMode) {
        String takeTime = getNamebyTime();
        if (CameraConstants.MODE_BURST.equals(shotMode)) {
            setBurstFirstTime(takeTime);
            CamLog.m3d(CameraConstants.TAG, "markTakeTime mBurstFirstTime : " + shotMode + " : " + this.sBurstFirstTime);
            this.mBurstCountAsc = 96;
        } else {
            this.mTakeTime = getNamebyDate() + "_" + takeTime;
            CamLog.m3d(CameraConstants.TAG, "markTakeTime shotmode : " + shotMode + " mTakeTime : " + this.mTakeTime);
        }
        return this.mTakeTime;
    }

    public String makeFilenamebyTime(String markTime, String shotMode, int cameraType) {
        String filename_final = null;
        if (cameraType == 0) {
            if (CameraConstants.MODE_BURST.equals(shotMode)) {
                if (this.mBurstCount > 1) {
                    filename_final = this.sBurstFirstTime + getImageModeName(shotMode);
                }
            } else if (markTime != null && markTime.length() > 0) {
                filename_final = markTime + getImageModeName(shotMode);
            }
        }
        if (filename_final != null) {
            return filename_final;
        }
        if (CameraConstants.MODE_BURST.equals(shotMode)) {
            return this.sBurstFirstTime + getImageModeName(shotMode);
        }
        return getNamebyDate() + "_" + getNamebyTime();
    }

    private String makeFileNameForDCM(Context context, int storage, int useType, String dir, String shotMode, boolean useThread) {
        this.mIsCheckingNormal = true;
        String fileName = makeFileName_Normal(useType, this.mFileNumber);
        if (CameraConstants.MODE_BURST.equals(shotMode) && this.sBurstFirstNameDCM.length() > 0) {
            CamLog.m3d(CameraConstants.TAG, "BurstFirstNameDCM = " + this.sBurstFirstNameDCM + " fileName = " + fileName);
            fileName = this.sBurstFirstNameDCM;
        }
        String shotModeName = getImageModeName(shotMode);
        if (useType == 0 && CameraConstants.MODE_BURST.equals(shotMode)) {
            fileName = fileName + shotModeName;
        } else {
            this.mFileNumber++;
            this.mCheckingNormalStatus = 0;
            generateFileName_Normal(context, storage, dir, useThread, shotModeName);
            fileName = makeFileName_Normal(useType, this.mFileNumber) + shotModeName;
            CamLog.m3d(CameraConstants.TAG, "fileName after makeFileName_Normal = " + fileName);
        }
        CamLog.m3d(CameraConstants.TAG, "fileName with shotmode = " + fileName);
        this.mIsCheckingNormal = false;
        return fileName;
    }

    private String makeFileNameForVZW(int useType, String dir, String shotMode) {
        this.mIsCheckingNormal = true;
        long start = 96;
        String fileName = makeCurrentDateToString_Date();
        if (CameraConstants.MODE_BURST.equals(shotMode) && this.mBurstCount > 1) {
            fileName = this.sBurstFirstTime;
        }
        String shotModeName = getImageModeName(shotMode);
        String fileNameWithShotMode = fileName + shotModeName;
        if (useType != 0) {
            while (true) {
                if (!FileManager.isFileExist(dir + fileNameWithShotMode + ".mp4") && !FileManager.isFileExist(dir + fileNameWithShotMode + MultimediaProperties.VIDEO_EXTENSION_3GP)) {
                    break;
                }
                start++;
                fileNameWithShotMode = makeFileName_Date(fileName, start);
            }
        } else {
            while (true) {
                if (!FileManager.isFileExist(dir + fileNameWithShotMode + ".jpg") && !isFileExistInSavingList(dir + fileNameWithShotMode + ".jpg")) {
                    break;
                } else if (shotModeName == null) {
                    start++;
                    fileNameWithShotMode = makeFileName_Date(fileName, start);
                } else if (CameraConstants.MODE_BURST.equals(shotMode)) {
                    if (this.mBurstCount == 1) {
                        this.mBurstCountAsc++;
                        increaseTempForVZW(this.mBurstCountAsc, true);
                    }
                    fileNameWithShotMode = makeFileName_Date(fileName, this.mBurstCountAsc);
                    if (SecureImageUtil.useSecureLockImage()) {
                        this.mCurBurstIdForSecure = fileNameWithShotMode;
                    }
                    fileNameWithShotMode = fileNameWithShotMode + shotModeName;
                } else {
                    start++;
                    fileNameWithShotMode = makeFileName_Date(fileName, start) + shotModeName;
                    increaseTempForVZW(start, false);
                }
            }
            if (CameraConstants.MODE_BURST.equals(shotMode) && this.mBurstCountAsc != 96) {
                fileNameWithShotMode = makeFileName_Date(fileName, this.mBurstCountAsc) + shotModeName;
            }
            if (CameraConstants.MODE_GIF.equals(shotMode)) {
                if (FileManager.isFileExist(dir + fileNameWithShotMode + String.valueOf((char) ((int) this.mGIFCountAsc)) + "_GIF.gif")) {
                    this.mGIFCountAsc++;
                }
                fileNameWithShotMode = makeFileName_Date(fileName, this.mGIFCountAsc);
            }
        }
        this.mFileName = fileNameWithShotMode;
        this.mIsCheckingNormal = false;
        return this.mFileName;
    }

    private void increaseTempForVZW(long asc, boolean isBurst) {
        if (isBurst) {
            if (asc > 123) {
                this.mTempAsc++;
            }
        } else if (asc > 122) {
            this.mTempAsc++;
        }
    }

    private String getImageModeName(String shotMode) {
        if (CameraConstants.MODE_HDR_PICTURE.equals(shotMode)) {
            return CameraConstants.POSTF_NAME_HDR;
        }
        if (!CameraConstants.MODE_BURST.equals(shotMode)) {
            return "";
        }
        String sCount = Integer.toString(this.mBurstCount);
        if (sCount.length() == 1) {
            sCount = "0" + sCount;
        }
        return CameraConstants.POSTF_NAME_BURST + sCount;
    }

    private String getNamebyDate() {
        String sYMD = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        CamLog.m3d(CameraConstants.TAG, "sYMD = " + sYMD);
        return sYMD;
    }

    private String getNamebyTime() {
        String sHMS = new SimpleDateFormat("HHmmss", Locale.US).format(new Date());
        CamLog.m3d(CameraConstants.TAG, "sHMS = " + sHMS);
        return sHMS;
    }

    private void processBurstCount(int countBurstshot, Context context) {
        this.mBurstCount = countBurstshot;
        if (countBurstshot == 1) {
            int cmpLength;
            if (ModelProperties.getCarrierCode() == 4) {
                cmpLength = 8;
            } else if (ModelProperties.getCarrierCode() == 6) {
                cmpLength = 10;
            } else {
                cmpLength = 15;
            }
            if (ModelProperties.getCarrierCode() == 4) {
                this.mFileNumber++;
                long fileNumber = this.mFileNumber;
                String fileName = this.sBurstFirstNameDCM;
                while (true) {
                    fileName = makeFileName_Normal(0, fileNumber);
                    if (!isExistBurstId(context, fileName)) {
                        break;
                    }
                    fileNumber++;
                }
                this.mFileNumber = fileNumber;
                this.sBurstFirstNameDCM = fileName;
            }
            if (ModelProperties.getCarrierCode() != 6 && this.sBurstFirstTime != null && this.sBurstFirstTime.length() > 0 && this.mBurstFirstTimeTemp != null && this.mBurstFirstTimeTemp.length() > 0) {
                if (!this.sBurstFirstTime.substring(0, cmpLength).equals(this.mBurstFirstTimeTemp.substring(0, cmpLength))) {
                    this.sBurstFirstTime = this.mBurstFirstTimeTemp;
                } else if (this.sBurstFirstTime.matches(".*\\(.*")) {
                    this.sBurstFirstTime = this.mBurstFirstTimeTemp + "(" + Integer.toString(Integer.valueOf(this.sBurstFirstTime.split("\\(")[1].replace(")", "")).intValue() + 1) + ")";
                } else {
                    this.sBurstFirstTime = this.mBurstFirstTimeTemp + "(1)";
                }
                this.mCurBurstIdForSecure = this.sBurstFirstTime;
            } else if ((this.sBurstFirstTime != null && this.sBurstFirstTime.length() <= 0) || this.mBurstCount <= 1) {
                this.sBurstFirstTime = this.mBurstFirstTimeTemp;
                this.mCurBurstIdForSecure = this.sBurstFirstTime;
            }
        }
    }

    private boolean isExistBurstId(Context context, String burstId) {
        CamLog.m3d(CameraConstants.TAG, "checkExistBurstId - start");
        String[] burstShotProjection = new String[]{"_id", "burst_id"};
        String burstShotSelection = "(burst_id='" + burstId + "')";
        CamLog.m3d(CameraConstants.TAG, "burstShotSelection : " + burstShotSelection);
        Cursor burstShotQueryCursor = null;
        try {
            CamLog.m3d(CameraConstants.TAG, "qurry start");
            burstShotQueryCursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, burstShotProjection, burstShotSelection, null, null);
            if (burstShotQueryCursor == null) {
                if (burstShotQueryCursor != null) {
                    burstShotQueryCursor.close();
                }
                return false;
            }
            CamLog.m3d(CameraConstants.TAG, "burstShotQueryCursor count : " + burstShotQueryCursor.getCount());
            if (burstShotQueryCursor.getCount() == 0) {
                burstShotQueryCursor.close();
                if (burstShotQueryCursor != null) {
                    burstShotQueryCursor.close();
                }
                return false;
            }
            if (burstShotQueryCursor != null) {
                burstShotQueryCursor.close();
            }
            CamLog.m3d(CameraConstants.TAG, "checkExistBurstId - end");
            return true;
        } catch (SQLiteException e) {
            CamLog.m6e(CameraConstants.TAG, "cursor error ", e);
            if (burstShotQueryCursor != null) {
                burstShotQueryCursor.close();
            }
        } catch (IllegalStateException e2) {
            CamLog.m6e(CameraConstants.TAG, "cursor error ", e2);
            if (burstShotQueryCursor != null) {
                burstShotQueryCursor.close();
            }
        } catch (SecurityException e3) {
            CamLog.m6e(CameraConstants.TAG, "Security Exception error ", e3);
            if (burstShotQueryCursor != null) {
                burstShotQueryCursor.close();
            }
        } catch (Throwable th) {
            if (burstShotQueryCursor != null) {
                burstShotQueryCursor.close();
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:?, code:
            return false;
     */
    private boolean isFileExistInSavingList(java.lang.String r3) {
        /*
        r2 = this;
        r1 = r2.mLock;
        monitor-enter(r1);
        r0 = r2.mFileNameInSaving;	 Catch:{ all -> 0x001a }
        if (r0 == 0) goto L_0x0017;
    L_0x0007:
        r0 = r2.mFileNameInSaving;	 Catch:{ all -> 0x001a }
        r0 = r0.size();	 Catch:{ all -> 0x001a }
        if (r0 <= 0) goto L_0x0017;
    L_0x000f:
        r0 = r2.mFileNameInSaving;	 Catch:{ all -> 0x001a }
        r0 = r0.contains(r3);	 Catch:{ all -> 0x001a }
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
    L_0x0016:
        return r0;
    L_0x0017:
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
        r0 = 0;
        goto L_0x0016;
    L_0x001a:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x001a }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.file.FileNamer.isFileExistInSavingList(java.lang.String):boolean");
    }

    public void addFileNameInSaving(String fileName) {
        synchronized (this.mLock) {
            if (this.mFileNameInSaving == null) {
                this.mFileNameInSaving = new ArrayList();
            }
            if (this.mFileNameInSaving != null) {
                this.mFileNameInSaving.add(fileName);
            }
        }
    }

    public void removeFileNameInSaving(String fileName) {
        synchronized (this.mLock) {
            if (this.mFileNameInSaving != null) {
                this.mFileNameInSaving.remove(fileName);
            }
        }
    }

    public void releaseSavingList() {
        synchronized (this.mLock) {
            if (this.mFileNameInSaving != null) {
                this.mFileNameInSaving.clear();
                this.mFileNameInSaving = null;
            }
        }
    }
}
