package android.support.p000v4.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: FragmentManager */
/* renamed from: android.support.v4.app.FragmentManagerState */
final class FragmentManagerState implements Parcelable {
    public static final Creator<FragmentManagerState> CREATOR = new C00101();
    FragmentState[] mActive;
    int[] mAdded;
    BackStackState[] mBackStack;

    /* compiled from: FragmentManager */
    /* renamed from: android.support.v4.app.FragmentManagerState$1 */
    static class C00101 implements Creator<FragmentManagerState> {
        C00101() {
        }

        public FragmentManagerState createFromParcel(Parcel in) {
            return new FragmentManagerState(in);
        }

        public FragmentManagerState[] newArray(int size) {
            return new FragmentManagerState[size];
        }
    }

    public FragmentManagerState(Parcel in) {
        this.mActive = (FragmentState[]) in.createTypedArray(FragmentState.CREATOR);
        this.mAdded = in.createIntArray();
        this.mBackStack = (BackStackState[]) in.createTypedArray(BackStackState.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.mActive, flags);
        dest.writeIntArray(this.mAdded);
        dest.writeTypedArray(this.mBackStack, flags);
    }
}