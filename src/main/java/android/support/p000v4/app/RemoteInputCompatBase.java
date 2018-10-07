package android.support.p000v4.app;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@TargetApi(9)
@RequiresApi(9)
/* renamed from: android.support.v4.app.RemoteInputCompatBase */
class RemoteInputCompatBase {

    /* renamed from: android.support.v4.app.RemoteInputCompatBase$RemoteInput */
    public static abstract class RemoteInput {

        /* renamed from: android.support.v4.app.RemoteInputCompatBase$RemoteInput$Factory */
        public interface Factory {
            RemoteInput build(String str, CharSequence charSequence, CharSequence[] charSequenceArr, boolean z, Bundle bundle);

            RemoteInput[] newArray(int i);
        }

        protected abstract boolean getAllowFreeFormInput();

        protected abstract CharSequence[] getChoices();

        protected abstract Bundle getExtras();

        protected abstract CharSequence getLabel();

        protected abstract String getResultKey();
    }

    RemoteInputCompatBase() {
    }
}
