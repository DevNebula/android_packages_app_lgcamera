package com.arcsoft.stickerlibrary.utils;

import android.content.Context;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class XMLParser {
    public static final int PARAMS_TYPE_ALIGN = 1;
    public static final int PARAMS_TYPE_BEAUTY = 2;
    public static final int PARAMS_TYPE_FUNNY_FACE = 6;
    public static final int PARAMS_TYPE_MASK = 5;
    public static final int PARAMS_TYPE_STICKER_2D = 3;
    public static final int PARAMS_TYPE_STICKER_3D = 4;
    private ArrayList<SuperParams> mParamsList = new ArrayList();

    public class SuperParams {
        public int mParamsType = 0;
    }

    public class AlignParams extends SuperParams {
        public String mAlignTrackPath;

        public AlignParams() {
            super();
            this.mAlignTrackPath = null;
            this.mParamsType = 1;
        }
    }

    public class BeautyParams extends SuperParams {
        public int mBeautySkinBright;
        public int mBeautySkinSoften;

        public BeautyParams() {
            super();
            this.mBeautySkinSoften = 0;
            this.mBeautySkinBright = 0;
            this.mParamsType = 2;
        }
    }

    public class FunnyFaceParams extends SuperParams {
        public int mExpressionType;
        public int mIntensity;

        public FunnyFaceParams() {
            super();
            this.mExpressionType = 0;
            this.mIntensity = 0;
            this.mParamsType = 6;
        }
    }

    public class MaskParams extends SuperParams {
        public String mMaskConfigPath;
        public String mMaskFaceHouseDataPath;
        public String mMaskImgPath;
        public String mMaskTemplateSourcePath;

        public MaskParams() {
            super();
            this.mMaskTemplateSourcePath = null;
            this.mMaskConfigPath = null;
            this.mMaskFaceHouseDataPath = null;
            this.mMaskImgPath = null;
            this.mParamsType = 5;
        }
    }

    public class Sticker2dParams extends SuperParams {
        public int m2dEyeBlink;
        public int m2dEyebrowRaise;
        public int m2dHeadNod;
        public int m2dHeadShake;
        public int m2dMouthOpen;
        public String m2dTemplatePath;

        public Sticker2dParams() {
            super();
            this.m2dTemplatePath = null;
            this.m2dMouthOpen = 0;
            this.m2dEyeBlink = 0;
            this.m2dEyebrowRaise = 0;
            this.m2dHeadNod = 0;
            this.m2dHeadShake = 0;
            this.mParamsType = 3;
        }
    }

    public class Sticker3dParams extends SuperParams {
        public String m3dConfigPath;
        public String m3dFaceHouseDataPath;
        public String m3dTemplatePath;
        public String m3dTemplateSourcePath;

        public Sticker3dParams() {
            super();
            this.m3dTemplateSourcePath = null;
            this.m3dTemplatePath = null;
            this.m3dConfigPath = null;
            this.m3dFaceHouseDataPath = null;
            this.mParamsType = 4;
        }
    }

    public ArrayList<SuperParams> getParamsList() {
        return this.mParamsList;
    }

    public void load(Context context, String xmlPath, String trackPath, String modelPath) {
        Exception e;
        Throwable th;
        File file = new File(xmlPath);
        if (this.mParamsList != null) {
            this.mParamsList.clear();
        }
        InputStream in = null;
        try {
            if (file.exists()) {
                InputStream fileInputStream = new FileInputStream(file);
                try {
                    XmlPullParser pullParser = Xml.newPullParser();
                    pullParser.setInput(fileInputStream, "UTF-8");
                    String folderPath = "";
                    if (file.getParentFile() != null) {
                        folderPath = file.getParentFile().getParent();
                        if (!folderPath.isEmpty()) {
                            folderPath = folderPath + "/";
                        }
                    }
                    LogUtil.LogD("stick2d parser:", "sticker folder = " + folderPath);
                    for (int event = pullParser.getEventType(); event != 1; event = pullParser.next()) {
                        switch (event) {
                            case 2:
                                String tag = pullParser.getName();
                                if (!"face_align".equals(tag)) {
                                    if (!"beauty".equals(tag)) {
                                        if (!"funny".equals(tag)) {
                                            if (!"stick2d".equals(tag)) {
                                                if (!"stick3d".equals(tag)) {
                                                    if (!"mask".equals(tag)) {
                                                        break;
                                                    }
                                                    boolean bMaskTagStarted = true;
                                                    MaskParams maskParams = new MaskParams();
                                                    int maskSubEvent = pullParser.getEventType();
                                                    while (bMaskTagStarted && maskSubEvent != 1) {
                                                        switch (maskSubEvent) {
                                                            case 2:
                                                                tag = pullParser.getName();
                                                                if (!"MaskTemplateSourcePath".equals(tag)) {
                                                                    if (!"MaskConfigPath".equals(tag)) {
                                                                        if (!"MaskFaceHouseDataPath".equals(tag)) {
                                                                            if (!"maskImg".equals(tag)) {
                                                                                break;
                                                                            }
                                                                            maskParams.mMaskImgPath = pullParser.nextText();
                                                                            maskParams.mMaskImgPath = folderPath + maskParams.mMaskImgPath;
                                                                            break;
                                                                        } else if (modelPath != null && !modelPath.isEmpty()) {
                                                                            maskParams.mMaskFaceHouseDataPath = modelPath;
                                                                            break;
                                                                        } else {
                                                                            maskParams.mMaskFaceHouseDataPath = pullParser.nextText();
                                                                            maskParams.mMaskFaceHouseDataPath = folderPath + maskParams.mMaskFaceHouseDataPath;
                                                                            break;
                                                                        }
                                                                    }
                                                                    maskParams.mMaskConfigPath = pullParser.nextText();
                                                                    maskParams.mMaskConfigPath = folderPath + maskParams.mMaskConfigPath;
                                                                    break;
                                                                }
                                                                maskParams.mMaskTemplateSourcePath = pullParser.nextText();
                                                                maskParams.mMaskTemplateSourcePath = folderPath + maskParams.mMaskTemplateSourcePath;
                                                                break;
                                                            case 3:
                                                                if (!"mask".equals(pullParser.getName())) {
                                                                    break;
                                                                }
                                                                bMaskTagStarted = false;
                                                                break;
                                                            default:
                                                                break;
                                                        }
                                                        maskSubEvent = pullParser.next();
                                                        maskSubEvent = pullParser.getEventType();
                                                    }
                                                    this.mParamsList.add(maskParams);
                                                    break;
                                                }
                                                boolean bSticker3dTagStarted = true;
                                                Sticker3dParams sticker3dParams = new Sticker3dParams();
                                                int stick3dSubEvent = pullParser.getEventType();
                                                while (bSticker3dTagStarted && stick3dSubEvent != 1) {
                                                    switch (stick3dSubEvent) {
                                                        case 2:
                                                            tag = pullParser.getName();
                                                            if (!"Sticker3dTemplateSourcePath".equals(tag)) {
                                                                if (!"Sticker3dTemplatePath".equals(tag)) {
                                                                    if (!"Sticker3dConfigPath".equals(tag)) {
                                                                        if ("Sticker3dFaceHouseDataPath".equals(tag)) {
                                                                            if (modelPath != null && !modelPath.isEmpty()) {
                                                                                sticker3dParams.m3dFaceHouseDataPath = modelPath;
                                                                                break;
                                                                            }
                                                                            sticker3dParams.m3dFaceHouseDataPath = pullParser.nextText();
                                                                            sticker3dParams.m3dFaceHouseDataPath = folderPath + sticker3dParams.m3dFaceHouseDataPath;
                                                                            break;
                                                                        }
                                                                        break;
                                                                    }
                                                                    sticker3dParams.m3dConfigPath = pullParser.nextText();
                                                                    sticker3dParams.m3dConfigPath = folderPath + sticker3dParams.m3dConfigPath;
                                                                    break;
                                                                }
                                                                sticker3dParams.m3dTemplatePath = pullParser.nextText();
                                                                sticker3dParams.m3dTemplatePath = folderPath + sticker3dParams.m3dTemplatePath;
                                                                break;
                                                            }
                                                            sticker3dParams.m3dTemplateSourcePath = pullParser.nextText();
                                                            sticker3dParams.m3dTemplateSourcePath = folderPath + sticker3dParams.m3dTemplateSourcePath;
                                                            break;
                                                            break;
                                                        case 3:
                                                            if (!"stick3d".equals(pullParser.getName())) {
                                                                break;
                                                            }
                                                            bSticker3dTagStarted = false;
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    stick3dSubEvent = pullParser.next();
                                                    stick3dSubEvent = pullParser.getEventType();
                                                }
                                                this.mParamsList.add(sticker3dParams);
                                                break;
                                            }
                                            boolean bSticker2dTagStarted = true;
                                            Sticker2dParams sticker2dParams = new Sticker2dParams();
                                            sticker2dParams.m2dTemplatePath = pullParser.getAttributeValue(null, "Sticker2dTemplatePath");
                                            sticker2dParams.m2dTemplatePath = folderPath + sticker2dParams.m2dTemplatePath;
                                            int stick2dSubEvent = pullParser.getEventType();
                                            while (bSticker2dTagStarted && stick2dSubEvent != 1) {
                                                switch (stick2dSubEvent) {
                                                    case 2:
                                                        tag = pullParser.getName();
                                                        if (!"Sticker2dMouthOpen".equalsIgnoreCase(tag)) {
                                                            if (!"Sticker2dEyeBlink".equalsIgnoreCase(tag)) {
                                                                if (!"Sticker2dEyebrowRaise".equalsIgnoreCase(tag)) {
                                                                    if (!"Sticker2dHeadNod".equalsIgnoreCase(tag)) {
                                                                        if (!"Sticker2dHeadShake".equalsIgnoreCase(tag)) {
                                                                            break;
                                                                        }
                                                                        sticker2dParams.m2dHeadShake = new Integer(pullParser.nextText().trim()).intValue();
                                                                        LogUtil.LogD("stick2d parser:", "head shake = " + sticker2dParams.m2dHeadShake);
                                                                        break;
                                                                    }
                                                                    sticker2dParams.m2dHeadNod = new Integer(pullParser.nextText().trim()).intValue();
                                                                    LogUtil.LogD("stick2d parser:", "head nod = " + sticker2dParams.m2dHeadNod);
                                                                    break;
                                                                }
                                                                sticker2dParams.m2dEyebrowRaise = new Integer(pullParser.nextText().trim()).intValue();
                                                                LogUtil.LogD("stick2d parser:", "eye brow raise = " + sticker2dParams.m2dEyebrowRaise);
                                                                break;
                                                            }
                                                            sticker2dParams.m2dEyeBlink = new Integer(pullParser.nextText().trim()).intValue();
                                                            LogUtil.LogD("stick2d parser:", "eye blink = " + sticker2dParams.m2dEyeBlink);
                                                            break;
                                                        }
                                                        sticker2dParams.m2dMouthOpen = new Integer(pullParser.nextText().trim()).intValue();
                                                        LogUtil.LogD("stick2d parser:", "mouth open = " + sticker2dParams.m2dMouthOpen);
                                                        break;
                                                    case 3:
                                                        tag = pullParser.getName();
                                                        LogUtil.LogD("stick2d END_TAG:", "tag = " + tag);
                                                        if (!"stick2d".equals(tag)) {
                                                            break;
                                                        }
                                                        bSticker2dTagStarted = false;
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                stick2dSubEvent = pullParser.next();
                                                LogUtil.LogD("stick2d parser:", "subevent = " + stick2dSubEvent);
                                            }
                                            this.mParamsList.add(sticker2dParams);
                                            break;
                                        }
                                        boolean bFunnyTagStarted = true;
                                        FunnyFaceParams funnyParams = new FunnyFaceParams();
                                        int funnySubEvent = pullParser.getEventType();
                                        while (bFunnyTagStarted && funnySubEvent != 1) {
                                            switch (funnySubEvent) {
                                                case 2:
                                                    tag = pullParser.getName();
                                                    if (!"ExpressionType".equals(tag)) {
                                                        if (!"Intensity".equals(tag)) {
                                                            break;
                                                        }
                                                        funnyParams.mIntensity = Integer.valueOf(pullParser.nextText()).intValue();
                                                        break;
                                                    }
                                                    funnyParams.mExpressionType = Integer.valueOf(pullParser.nextText()).intValue();
                                                    break;
                                                case 3:
                                                    if (!"funny".equals(pullParser.getName())) {
                                                        break;
                                                    }
                                                    bFunnyTagStarted = false;
                                                    break;
                                                default:
                                                    break;
                                            }
                                            funnySubEvent = pullParser.next();
                                            funnySubEvent = pullParser.getEventType();
                                        }
                                        this.mParamsList.add(funnyParams);
                                        break;
                                    }
                                    boolean bBeautyTagStarted = true;
                                    BeautyParams beautyParams = new BeautyParams();
                                    int beautySubEvent = pullParser.getEventType();
                                    while (bBeautyTagStarted && beautySubEvent != 1) {
                                        switch (beautySubEvent) {
                                            case 2:
                                                tag = pullParser.getName();
                                                if (!"SkinSoften".equals(tag)) {
                                                    if (!"SkinBright".equals(tag)) {
                                                        break;
                                                    }
                                                    beautyParams.mBeautySkinBright = Integer.valueOf(pullParser.nextText()).intValue();
                                                    break;
                                                }
                                                beautyParams.mBeautySkinSoften = Integer.valueOf(pullParser.nextText()).intValue();
                                                break;
                                            case 3:
                                                if (!"beauty".equals(pullParser.getName())) {
                                                    break;
                                                }
                                                bBeautyTagStarted = false;
                                                break;
                                            default:
                                                break;
                                        }
                                        beautySubEvent = pullParser.next();
                                        beautySubEvent = pullParser.getEventType();
                                    }
                                    this.mParamsList.add(beautyParams);
                                    break;
                                }
                                AlignParams alignParams = new AlignParams();
                                alignParams.mAlignTrackPath = pullParser.getAttributeValue(null, "TrackDataPath");
                                if (trackPath == null || trackPath.isEmpty()) {
                                    alignParams.mAlignTrackPath = folderPath + alignParams.mAlignTrackPath;
                                } else {
                                    alignParams.mAlignTrackPath = trackPath;
                                }
                                this.mParamsList.add(alignParams);
                                break;
                                break;
                            default:
                                break;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                            in = fileInputStream;
                            return;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            in = fileInputStream;
                            return;
                        }
                    }
                    in = fileInputStream;
                    return;
                } catch (Exception e3) {
                    e = e3;
                    in = fileInputStream;
                } catch (Throwable th2) {
                    th = th2;
                    in = fileInputStream;
                }
            } else if (in != null) {
                try {
                    in.close();
                    return;
                } catch (IOException e22) {
                    e22.printStackTrace();
                    return;
                }
            } else {
                return;
            }
        } catch (Exception e4) {
            e = e4;
        }
        try {
            LogUtil.LogE("Parser Error:", "" + e.getMessage());
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        } catch (Throwable th3) {
            th = th3;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
            throw th;
        }
    }
}
