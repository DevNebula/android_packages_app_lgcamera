package com.google.zxing.oned;

import com.arcsoft.stickerlibrary.sticker.StickerMessage;
import com.arcsoft.stickerlibrary.utils.ArcOffscreen;
import com.google.zxing.client.result.ExpandedProductParsedResult;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.CameraConstantsEx;
import com.lge.camera.managers.TimerManager;
import com.lge.panorama.Panorama;
import java.util.ArrayList;
import java.util.List;

final class EANManufacturerOrgSupport {
    private final List<String> countryIdentifiers = new ArrayList();
    private final List<int[]> ranges = new ArrayList();

    EANManufacturerOrgSupport() {
    }

    String lookupCountryIdentifier(String productCode) {
        initIfNeeded();
        int prefix = Integer.parseInt(productCode.substring(0, 3));
        int max = this.ranges.size();
        for (int i = 0; i < max; i++) {
            int[] range = (int[]) this.ranges.get(i);
            int start = range[0];
            if (prefix < start) {
                return null;
            }
            if (prefix <= (range.length == 1 ? start : range[1])) {
                return (String) this.countryIdentifiers.get(i);
            }
        }
        return null;
    }

    private void add(int[] range, String id) {
        this.ranges.add(range);
        this.countryIdentifiers.add(id);
    }

    private synchronized void initIfNeeded() {
        if (this.ranges.isEmpty()) {
            add(new int[]{0, 19}, "US/CA");
            add(new int[]{30, 39}, "US");
            add(new int[]{60, 139}, "US/CA");
            add(new int[]{300, 379}, "FR");
            add(new int[]{380}, "BG");
            add(new int[]{383}, "SI");
            add(new int[]{385}, "HR");
            add(new int[]{387}, "BA");
            add(new int[]{400, Panorama.STATUS_ERROR_ABORT_PANORAMA}, "DE");
            add(new int[]{Panorama.STATUS_ERROR_TOO_FAST, 459}, "JP");
            add(new int[]{460, 469}, "RU");
            add(new int[]{471}, "TW");
            add(new int[]{474}, "EE");
            add(new int[]{475}, "LV");
            add(new int[]{476}, "AZ");
            add(new int[]{477}, "LT");
            add(new int[]{478}, "UZ");
            add(new int[]{479}, "LK");
            add(new int[]{CameraConstants.SHUTTER_ZOOM_SUPPORTED_MIN_ZOOM_LEVEL}, "PH");
            add(new int[]{481}, "BY");
            add(new int[]{482}, "UA");
            add(new int[]{484}, "MD");
            add(new int[]{485}, "AM");
            add(new int[]{486}, "GE");
            add(new int[]{487}, "KZ");
            add(new int[]{489}, "HK");
            add(new int[]{490, 499}, "JP");
            add(new int[]{500, 509}, "GB");
            add(new int[]{520}, "GR");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER}, ExpandedProductParsedResult.POUND);
            add(new int[]{529}, "CY");
            add(new int[]{531}, "MK");
            add(new int[]{535}, "MT");
            add(new int[]{539}, "IE");
            add(new int[]{540, StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_AUDIO_RELEASE}, "BE/LU");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_ENCODER_VIDEO}, "PT");
            add(new int[]{569}, "IS");
            add(new int[]{570, 579}, "DK");
            add(new int[]{590}, "PL");
            add(new int[]{594}, "RO");
            add(new int[]{599}, "HU");
            add(new int[]{600, 601}, "ZA");
            add(new int[]{603}, "GH");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER}, "BH");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_CREATE}, "MU");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_START}, "MA");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_MUXER_STOP}, "DZ");
            add(new int[]{616}, "KE");
            add(new int[]{618}, "CI");
            add(new int[]{619}, "TN");
            add(new int[]{621}, "SY");
            add(new int[]{622}, "EG");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD}, "LY");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_CREATE}, "JO");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_START_RECORDING}, "IR");
            add(new int[]{StickerMessage.MSG_MEDIA_RECORDER_ERROR_AUDIO_RECORD_STOP}, "KW");
            add(new int[]{628}, "SA");
            add(new int[]{629}, "AE");
            add(new int[]{640, 649}, "FI");
            add(new int[]{690, 695}, "CN");
            add(new int[]{CameraConstants.BURST_COUNT_INVISIBLE_DELAY, 709}, "NO");
            add(new int[]{729}, "IL");
            add(new int[]{730, 739}, "SE");
            add(new int[]{740}, "GT");
            add(new int[]{741}, "SV");
            add(new int[]{742}, "HN");
            add(new int[]{743}, "NI");
            add(new int[]{744}, "CR");
            add(new int[]{745}, "PA");
            add(new int[]{746}, "DO");
            add(new int[]{750}, "MX");
            add(new int[]{754, 755}, "CA");
            add(new int[]{759}, "VE");
            add(new int[]{760, StickerMessage.MSG_STICKER_RESOURCE_BROKEN}, "CH");
            add(new int[]{770}, "CO");
            add(new int[]{ArcOffscreen.ASVL_PAF_RGB32_R8G8B8A8}, "UY");
            add(new int[]{775}, "PE");
            add(new int[]{777}, "BO");
            add(new int[]{779}, "AR");
            add(new int[]{780}, "CL");
            add(new int[]{784}, "PY");
            add(new int[]{785}, "PE");
            add(new int[]{786}, "EC");
            add(new int[]{789, 790}, "BR");
            add(new int[]{CameraConstantsEx.GOOGLE_ASSISTANT_TAKE_CMD_DELAY, 839}, "IT");
            add(new int[]{840, 849}, "ES");
            add(new int[]{TimerManager.ANI_DURATION}, "CU");
            add(new int[]{858}, "SK");
            add(new int[]{859}, "CZ");
            add(new int[]{860}, "YU");
            add(new int[]{865}, "MN");
            add(new int[]{867}, "KP");
            add(new int[]{868, 869}, "TR");
            add(new int[]{870, 879}, "NL");
            add(new int[]{880}, "KR");
            add(new int[]{885}, "TH");
            add(new int[]{888}, "SG");
            add(new int[]{890}, "IN");
            add(new int[]{893}, "VN");
            add(new int[]{896}, "PK");
            add(new int[]{899}, "ID");
            add(new int[]{900, 919}, "AT");
            add(new int[]{930, 939}, "AU");
            add(new int[]{940, 949}, "AZ");
            add(new int[]{955}, "MY");
            add(new int[]{958}, "MO");
        }
    }
}
