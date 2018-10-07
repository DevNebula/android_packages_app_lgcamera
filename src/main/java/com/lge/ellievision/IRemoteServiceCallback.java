package com.lge.ellievision;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.lge.ellievision.parceldata.IRecognitionResults;
import com.lge.ellievision.parceldata.ISceneCategory;

public interface IRemoteServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IRemoteServiceCallback {
        private static final String DESCRIPTOR = "com.lge.ellievision.IRemoteServiceCallback";
        static final int TRANSACTION_onRecognitionComplete = 1;
        static final int TRANSACTION_onSceneChanged = 2;

        private static class Proxy implements IRemoteServiceCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onRecognitionComplete(IRecognitionResults iRecognitionResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (iRecognitionResults != null) {
                        _data.writeInt(1);
                        iRecognitionResults.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSceneChanged(ISceneCategory iSceneCategory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (iSceneCategory != null) {
                        _data.writeInt(1);
                        iSceneCategory.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteServiceCallback)) {
                return new Proxy(obj);
            }
            return (IRemoteServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    IRecognitionResults _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (IRecognitionResults) IRecognitionResults.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onRecognitionComplete(_arg0);
                    return true;
                case 2:
                    ISceneCategory _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ISceneCategory) ISceneCategory.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onSceneChanged(_arg02);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onRecognitionComplete(IRecognitionResults iRecognitionResults) throws RemoteException;

    void onSceneChanged(ISceneCategory iSceneCategory) throws RemoteException;
}
