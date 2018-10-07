package com.arcsoft.stickerlibrary.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontHelpUtils {
    private static final String ASSETS_FONTS_ITALIC_TTS = "fonts/Roboto-Italic.ttf";
    private static final String ASSETS_FONTS_LIGHT_ITALIC_TTS = "fonts/Roboto-LightItalic.ttf";
    private static final String ASSETS_FONTS_LIGHT_TTS = "fonts/Roboto-Light.ttf";
    private static final String ASSETS_FONTS_MEDIUM_TTS = "fonts/Roboto-Medium.ttf";
    private static final String ASSETS_FONTS_REGULAR_TTS = "fonts/Roboto-Regular.ttf";

    private FontHelpUtils() {
    }

    public static void setRobotoRegularFonts(Context context, TextView textView) {
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), ASSETS_FONTS_REGULAR_TTS));
    }

    public static void setRobotoMediumFonts(Context context, TextView textView) {
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), ASSETS_FONTS_MEDIUM_TTS));
    }

    public static void setRobotoLightFonts(Context context, TextView textView) {
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), ASSETS_FONTS_LIGHT_TTS));
    }

    public static void setRobotoItalicFonts(Context context, TextView textView) {
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), ASSETS_FONTS_ITALIC_TTS));
    }

    public static void setRobotoLightItalicFonts(Context context, TextView textView) {
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), ASSETS_FONTS_LIGHT_ITALIC_TTS));
    }
}
