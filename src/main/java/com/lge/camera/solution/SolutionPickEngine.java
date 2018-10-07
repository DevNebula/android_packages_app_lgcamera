package com.lge.camera.solution;

import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SolutionPickEngine {
    private static final String CAMERA_ID = "cameraid";
    private static final String FACE_DETECT = "facedetect";
    private static final String FAST_SHOT_TO_SHOT = "fastshot2shot";
    private static final String FILM_MODE = "film";
    private static final String FLASH_MODE = "flash";
    private static final String HDR_MODE = "hdr";
    private static final String LOW_LIGHT_GAIN = "lowlightgain";
    private static final String MAX_FPS = "maxfps";
    private static final String MAX_GAIN_LEVEL = "maxgain";
    private static final String MAX_LUX_INDEX = "maxluxidx";
    private static final String MAX_VIDEO_HEIGHT = "maxvideoheight";
    private static final String MAX_VIDEO_WIDTH = "maxvideowidth";
    private static final String MAX_ZOOM_LEVEL = "maxzoom";
    private static final String MIN_GAIN_LEVEL = "mingain";
    private static final String MIN_LUX_INDEX = "minluxidx";
    private static final String MIN_ZOOM_LEVEL = "minzoom";
    private static final String SHOT_MODE = "shotmode";
    private static final String SOLUTION_INDEX = "solutionindex";
    private static final String SOLUTION_MAX_FRAME_COUNT = "maxframecount";
    private static final String SOLUTION_NAME = "solutionname";
    private static final String SOLUTION_NEED_USER_SELECT = "needuserselect";
    private static final String UI_SHOT_MODE = "uishotmode";

    public int getSolutionFrameCount(SolutionParameters params) {
        SolutionPickResult pickResult = new SolutionPickResult();
        HashMap<String, String> superzoom = getSolution(1);
        if (superzoom != null && checkSolutionCondition(superzoom, params, pickResult)) {
            return Integer.parseInt((String) superzoom.get(SOLUTION_MAX_FRAME_COUNT));
        }
        HashMap<String, String> nightshot = getSolution(8);
        if (nightshot == null || (!params.isSumBinningMode() && !checkSolutionCondition(nightshot, params, pickResult))) {
            return 0;
        }
        return Integer.parseInt((String) nightshot.get(SOLUTION_MAX_FRAME_COUNT));
    }

    public SolutionPickResult checkSolution(SolutionParameters params, int solutionIndex) {
        SolutionPickResult pickResult = new SolutionPickResult();
        Iterator<HashMap<String, String>> it = ((ArrayList) SolutionParsingUtil.sSolutionList.get(solutionIndex)).iterator();
        while (it.hasNext()) {
            checkSolutionCondition((HashMap) it.next(), params, pickResult);
        }
        return pickResult;
    }

    private boolean checkSolutionCondition(java.util.HashMap<java.lang.String, java.lang.String> r12, com.lge.camera.solution.SolutionParameters r13, com.lge.camera.solution.SolutionPickResult r14) {
        /*
        r11 = this;
        r7 = r12.entrySet();
        r9 = r7.iterator();
    L_0x0008:
        r7 = r9.hasNext();
        if (r7 == 0) goto L_0x028f;
    L_0x000e:
        r3 = r9.next();
        r3 = (java.util.Map.Entry) r3;
        r7 = r3.getKey();
        r7 = (java.lang.String) r7;
        r8 = -1;
        r10 = r7.hashCode();
        switch(r10) {
            case -2021527170: goto L_0x00bb;
            case -1081131771: goto L_0x00eb;
            case -466884977: goto L_0x00af;
            case -340850787: goto L_0x00a3;
            case -300605027: goto L_0x0047;
            case -149579008: goto L_0x0051;
            case 103158: goto L_0x008d;
            case 3143044: goto L_0x0098;
            case 97513456: goto L_0x0083;
            case 139808434: goto L_0x00df;
            case 844668643: goto L_0x0079;
            case 845248311: goto L_0x0065;
            case 862174246: goto L_0x003d;
            case 912287008: goto L_0x00d3;
            case 1048904512: goto L_0x00c7;
            case 1064466641: goto L_0x006f;
            case 1065046309: goto L_0x005b;
            default: goto L_0x0022;
        };
    L_0x0022:
        r7 = r8;
    L_0x0023:
        switch(r7) {
            case 0: goto L_0x0027;
            case 1: goto L_0x00f7;
            case 2: goto L_0x0123;
            case 3: goto L_0x0136;
            case 4: goto L_0x014b;
            case 5: goto L_0x0160;
            case 6: goto L_0x019c;
            case 7: goto L_0x01b1;
            case 8: goto L_0x01cb;
            case 9: goto L_0x01eb;
            case 10: goto L_0x0205;
            case 11: goto L_0x0219;
            case 12: goto L_0x022c;
            case 13: goto L_0x023f;
            case 14: goto L_0x0252;
            case 15: goto L_0x0267;
            case 16: goto L_0x027c;
            default: goto L_0x0026;
        };
    L_0x0026:
        goto L_0x0008;
    L_0x0027:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        r8 = 1;
        if (r7 <= r8) goto L_0x0008;
    L_0x0034:
        r7 = r14.getFrameCount();
        r8 = 1;
        if (r7 <= r8) goto L_0x0008;
    L_0x003b:
        r7 = 0;
    L_0x003c:
        return r7;
    L_0x003d:
        r10 = "maxframecount";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0045:
        r7 = 0;
        goto L_0x0023;
    L_0x0047:
        r10 = "needuserselect";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x004f:
        r7 = 1;
        goto L_0x0023;
    L_0x0051:
        r10 = "cameraid";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0059:
        r7 = 2;
        goto L_0x0023;
    L_0x005b:
        r10 = "minzoom";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0063:
        r7 = 3;
        goto L_0x0023;
    L_0x0065:
        r10 = "maxzoom";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x006d:
        r7 = 4;
        goto L_0x0023;
    L_0x006f:
        r10 = "mingain";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0077:
        r7 = 5;
        goto L_0x0023;
    L_0x0079:
        r10 = "maxgain";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0081:
        r7 = 6;
        goto L_0x0023;
    L_0x0083:
        r10 = "flash";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x008b:
        r7 = 7;
        goto L_0x0023;
    L_0x008d:
        r10 = "hdr";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x0095:
        r7 = 8;
        goto L_0x0023;
    L_0x0098:
        r10 = "film";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00a0:
        r7 = 9;
        goto L_0x0023;
    L_0x00a3:
        r10 = "shotmode";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00ab:
        r7 = 10;
        goto L_0x0023;
    L_0x00af:
        r10 = "maxvideowidth";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00b7:
        r7 = 11;
        goto L_0x0023;
    L_0x00bb:
        r10 = "maxvideoheight";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00c3:
        r7 = 12;
        goto L_0x0023;
    L_0x00c7:
        r10 = "facedetect";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00cf:
        r7 = 13;
        goto L_0x0023;
    L_0x00d3:
        r10 = "minluxidx";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00db:
        r7 = 14;
        goto L_0x0023;
    L_0x00df:
        r10 = "maxluxidx";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00e7:
        r7 = 15;
        goto L_0x0023;
    L_0x00eb:
        r10 = "maxfps";
        r7 = r7.equals(r10);
        if (r7 == 0) goto L_0x0022;
    L_0x00f3:
        r7 = 16;
        goto L_0x0023;
    L_0x00f7:
        r5 = 0;
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = "true";
        r7 = r7.equals(r8);
        if (r7 == 0) goto L_0x011e;
    L_0x0106:
        r7 = "solutionindex";
        r7 = r12.get(r7);
        r7 = (java.lang.String) r7;
        r8 = java.lang.Integer.parseInt(r7);
        r7 = "solutionname";
        r7 = r12.get(r7);
        r7 = (java.lang.String) r7;
        r5 = r11.isSolutionEnabledByUser(r8, r7, r13);
    L_0x011e:
        if (r5 != 0) goto L_0x0008;
    L_0x0120:
        r7 = 0;
        goto L_0x003c;
    L_0x0123:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = r13.isRearCamera();
        r7 = r7.equals(r8);
        if (r7 != 0) goto L_0x0008;
    L_0x0133:
        r7 = 0;
        goto L_0x003c;
    L_0x0136:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r8 = r13.getZoomRatio();
        r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r7 <= 0) goto L_0x0008;
    L_0x0148:
        r7 = 0;
        goto L_0x003c;
    L_0x014b:
        r8 = r13.getZoomRatio();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r7 = (r8 > r7 ? 1 : (r8 == r7 ? 0 : -1));
        if (r7 < 0) goto L_0x0008;
    L_0x015d:
        r7 = 0;
        goto L_0x003c;
    L_0x0160:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = ",";
        r6 = r7.split(r8);
        r7 = r6.length;
        r8 = 1;
        if (r7 <= r8) goto L_0x018d;
    L_0x0170:
        r7 = r13.getCameraId();
        r7 = r6[r7];
        r7 = java.lang.Float.parseFloat(r7);
        r4 = java.lang.Float.valueOf(r7);
    L_0x017e:
        r7 = r4.floatValue();
        r8 = r13.getRealGain();
        r7 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1));
        if (r7 <= 0) goto L_0x0008;
    L_0x018a:
        r7 = 0;
        goto L_0x003c;
    L_0x018d:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r4 = java.lang.Float.valueOf(r7);
        goto L_0x017e;
    L_0x019c:
        r8 = r13.getRealGain();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r7 = (r8 > r7 ? 1 : (r8 == r7 ? 0 : -1));
        if (r7 < 0) goto L_0x0008;
    L_0x01ae:
        r7 = 0;
        goto L_0x003c;
    L_0x01b1:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = "off";
        r7 = r7.equals(r8);
        if (r7 != 0) goto L_0x01c9;
    L_0x01bf:
        r1 = 1;
    L_0x01c0:
        r7 = r11.isFlashRequired(r13);
        if (r7 == r1) goto L_0x0008;
    L_0x01c6:
        r7 = 0;
        goto L_0x003c;
    L_0x01c9:
        r1 = 0;
        goto L_0x01c0;
    L_0x01cb:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = "off";
        r7 = r7.equals(r8);
        if (r7 != 0) goto L_0x01e9;
    L_0x01d9:
        r2 = 1;
    L_0x01da:
        r7 = r13.getHDRMode();
        if (r7 == r2) goto L_0x0008;
    L_0x01e0:
        r7 = r13.getBackLightDetected();
        if (r7 == 0) goto L_0x0008;
    L_0x01e6:
        r7 = 0;
        goto L_0x003c;
    L_0x01e9:
        r2 = 0;
        goto L_0x01da;
    L_0x01eb:
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r8 = "off";
        r7 = r7.equals(r8);
        if (r7 != 0) goto L_0x0203;
    L_0x01f9:
        r0 = 1;
    L_0x01fa:
        r7 = r13.getFilmEmulatorMode();
        if (r7 == r0) goto L_0x0008;
    L_0x0200:
        r7 = 0;
        goto L_0x003c;
    L_0x0203:
        r0 = 0;
        goto L_0x01fa;
    L_0x0205:
        r8 = r13.getShootMode();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        r7 = r7 & r8;
        if (r7 != 0) goto L_0x0008;
    L_0x0216:
        r7 = 0;
        goto L_0x003c;
    L_0x0219:
        r8 = r13.getVideoWidth();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        if (r8 <= r7) goto L_0x0008;
    L_0x0229:
        r7 = 0;
        goto L_0x003c;
    L_0x022c:
        r8 = r13.getVideoHeight();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        if (r8 <= r7) goto L_0x0008;
    L_0x023c:
        r7 = 0;
        goto L_0x003c;
    L_0x023f:
        r8 = r13.getFaceCount();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        if (r8 == r7) goto L_0x0008;
    L_0x024f:
        r7 = 0;
        goto L_0x003c;
    L_0x0252:
        r8 = r13.getLuxIndex();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r7 = (r8 > r7 ? 1 : (r8 == r7 ? 0 : -1));
        if (r7 >= 0) goto L_0x0008;
    L_0x0264:
        r7 = 0;
        goto L_0x003c;
    L_0x0267:
        r8 = r13.getLuxIndex();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Float.parseFloat(r7);
        r7 = (r8 > r7 ? 1 : (r8 == r7 ? 0 : -1));
        if (r7 < 0) goto L_0x0008;
    L_0x0279:
        r7 = 0;
        goto L_0x003c;
    L_0x027c:
        r8 = r13.getFpsValue();
        r7 = r3.getValue();
        r7 = (java.lang.String) r7;
        r7 = java.lang.Integer.parseInt(r7);
        if (r8 <= r7) goto L_0x0008;
    L_0x028c:
        r7 = 0;
        goto L_0x003c;
    L_0x028f:
        r7 = "solutionindex";
        r7 = r12.get(r7);
        r7 = (java.lang.String) r7;
        r9 = java.lang.Integer.parseInt(r7);
        r7 = "maxframecount";
        r7 = r12.get(r7);
        r7 = (java.lang.String) r7;
        r10 = java.lang.Integer.parseInt(r7);
        r7 = "solutionname";
        r7 = r12.get(r7);
        r7 = (java.lang.String) r7;
        r8 = "fastshot2shot";
        r8 = r12.get(r8);
        if (r8 != 0) goto L_0x02be;
    L_0x02b7:
        r8 = 0;
    L_0x02b8:
        r14.enableSolution(r9, r10, r7, r8);
        r7 = 1;
        goto L_0x003c;
    L_0x02be:
        r8 = 1;
        goto L_0x02b8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.lge.camera.solution.SolutionPickEngine.checkSolutionCondition(java.util.HashMap, com.lge.camera.solution.SolutionParameters, com.lge.camera.solution.SolutionPickResult):boolean");
    }

    private boolean isSolutionEnabledByUser(int solutionType, String name, SolutionParameters params) {
        switch (solutionType) {
            case 1:
                return params.getSuperZoomEnabled();
            case 4:
                if (params.getHDRMode() && params.getBackLightDetected()) {
                    return true;
                }
                return false;
            case 8:
                return params.getNightShotEnabled();
            case 16:
                return params.getBeautyEnabled();
            case 32:
                return params.getFilmEmulatorMode();
            case 64:
                if (name.equals("autocontrastfortext")) {
                    return params.getAutoContrastForTextEnabled();
                }
                return params.getAutoContrastEnabled();
            case 128:
                return params.getSignatureEanbled();
            case 256:
                return params.getOutFocusEanbled();
            case 1024:
                return params.getNightShotEnabled();
            case 2048:
                return params.getHDRMode();
            case 4096:
                return params.getHDRMode() && !params.getAutoContrastForTextEnabled();
            default:
                return false;
        }
    }

    private HashMap<String, String> getSolution(int type) {
        Iterator<HashMap<String, String>> it = ((ArrayList) SolutionParsingUtil.sSolutionList.get(0)).iterator();
        while (it.hasNext()) {
            HashMap<String, String> currentSolution = (HashMap) it.next();
            if ((Integer.parseInt((String) currentSolution.get(SOLUTION_INDEX)) & type) != 0) {
                return currentSolution;
            }
        }
        return null;
    }

    public int getFrameCount(int type) {
        HashMap<String, String> currentSolution = getSolution(type);
        if (currentSolution == null) {
            return 0;
        }
        int frameCount = Integer.parseInt((String) currentSolution.get(SOLUTION_MAX_FRAME_COUNT));
        CamLog.m3d(CameraConstants.TAG, "Solution name : " + ((String) currentSolution.get(SOLUTION_NAME)) + ", frame count : " + frameCount);
        return frameCount;
    }

    private boolean isFlashRequired(SolutionParameters params) {
        String flashMode = params.getFlash();
        if (flashMode == null) {
            return false;
        }
        return "on".equals(flashMode);
    }
}
