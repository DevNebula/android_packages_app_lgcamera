package com.lge.camera.managers.ext.sticker;

import android.graphics.Bitmap;

public class StickerInformationDataClass {
    public String configFile = "";
    public Bitmap icon_image = null;
    public String icon_path = "";
    public int solution_type = -1;
    public int sticker_data_position = -1;
    public String sticker_id = "";
    public String sticker_name = "";

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/ID = ").append(this.sticker_id).append("\n").append("/icon path = ").append(this.icon_path).append("\n").append("/sticker_name = ").append(this.sticker_name).append("\n").append("/solution_type = ").append(this.solution_type).append("\n").append("/sticker_data_position = ").append(this.sticker_data_position).append("\n").append("/configFile").append(this.configFile);
        return sb.toString();
    }
}
