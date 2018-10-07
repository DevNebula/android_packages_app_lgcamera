package com.lge.camera.components;

import com.lge.camera.C0088R;
import com.lge.camera.settings.Setting;

public class QuickButtonTypeFilmEmulator extends QuickButtonType {
    public QuickButtonTypeFilmEmulator(boolean enable) {
        super(C0088R.id.quick_button_film_emulator, Setting.KEY_FILM_EMULATOR, -2, -2, true, false, new int[]{C0088R.string.Advanced_beauty_filter, C0088R.string.Advanced_beauty_filter}, enable, new int[]{C0088R.drawable.btn_quickbutton_filter_off, C0088R.drawable.btn_quickbutton_filter_off}, new int[]{84, 83}, 0, null, 0, C0088R.drawable.btn_quickbutton_filter_pressed);
    }
}
