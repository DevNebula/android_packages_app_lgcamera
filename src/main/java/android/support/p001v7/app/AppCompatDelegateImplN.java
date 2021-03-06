package android.support.p001v7.app;

import android.content.Context;
import android.support.p001v7.app.AppCompatDelegateImplV23.AppCompatWindowCallbackV23;
import android.support.p001v7.app.AppCompatDelegateImplV9.PanelFeatureState;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.Window;
import android.view.Window.Callback;
import java.util.List;

/* renamed from: android.support.v7.app.AppCompatDelegateImplN */
class AppCompatDelegateImplN extends AppCompatDelegateImplV23 {

    /* renamed from: android.support.v7.app.AppCompatDelegateImplN$AppCompatWindowCallbackN */
    class AppCompatWindowCallbackN extends AppCompatWindowCallbackV23 {
        AppCompatWindowCallbackN(Callback callback) {
            super(callback);
        }

        public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
            PanelFeatureState panel = AppCompatDelegateImplN.this.getPanelState(0, true);
            if (panel == null || panel.menu == null) {
                super.onProvideKeyboardShortcuts(data, menu, deviceId);
            } else {
                super.onProvideKeyboardShortcuts(data, panel.menu, deviceId);
            }
        }
    }

    AppCompatDelegateImplN(Context context, Window window, AppCompatCallback callback) {
        super(context, window, callback);
    }

    Callback wrapWindowCallback(Callback callback) {
        return new AppCompatWindowCallbackN(callback);
    }
}
