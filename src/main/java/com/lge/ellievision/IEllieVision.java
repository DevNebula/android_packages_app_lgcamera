package com.lge.ellievision;

import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.lge.ellievision.parceldata.IBitmap;
import java.util.List;

public interface IEllieVision extends IInterface {

    public static abstract class Stub extends Binder implements IEllieVision {
        private static final String DESCRIPTOR = "com.lge.ellievision.IEllieVision";
        static final int TRANSACTION_getDetectedCoordinate = 9;
        static final int TRANSACTION_getDetectedRect = 10;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_requestRecognize = 4;
        static final int TRANSACTION_requestRecognizeByte = 3;
        static final int TRANSACTION_requestRecognizeByteWithLocation = 5;
        static final int TRANSACTION_resetSceneHandShake = 6;
        static final int TRANSACTION_resetUnknownCount = 8;
        static final int TRANSACTION_setRecognitionThreshold = 7;
        static final int TRANSACTION_unregisterCallback = 2;

        private static class Proxy implements IEllieVision {
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

            public boolean registerCallback(IRemoteServiceCallback callback, String usage) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(usage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterCallback(IRemoteServiceCallback callback) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestRecognizeByte(IBitmap data, int srcW, int srcH, int degree) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(srcW);
                    _data.writeInt(srcH);
                    _data.writeInt(degree);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestRecognize(IBitmap data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void requestRecognizeByteWithLocation(IBitmap data, int srcW, int srcH, int degree) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(srcW);
                    _data.writeInt(srcH);
                    _data.writeInt(degree);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void resetSceneHandShake() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setRecognitionThreshold(float threshold) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(threshold);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void resetUnknownCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public List<PointF> getDetectedCoordinate(IBitmap data, int scrW, int scrH, int previewW, int previewH, int sensorOrientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(scrW);
                    _data.writeInt(scrH);
                    _data.writeInt(previewW);
                    _data.writeInt(previewH);
                    _data.writeInt(sensorOrientation);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    List<PointF> _result = _reply.createTypedArrayList(PointF.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getDetectedRect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Rect _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEllieVision asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEllieVision)) {
                return new Proxy(obj);
            }
            return (IEllieVision) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            IBitmap _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.lge.ellievision.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unregisterCallback(com.lge.ellievision.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (IBitmap) IBitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    requestRecognizeByte(_arg0, data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (IBitmap) IBitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    requestRecognize(_arg0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (IBitmap) IBitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    requestRecognizeByteWithLocation(_arg0, data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    resetSceneHandShake();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    setRecognitionThreshold(data.readFloat());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    resetUnknownCount();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (IBitmap) IBitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    List<PointF> _result2 = getDetectedCoordinate(_arg0, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    Rect _result3 = getDetectedRect();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    List<PointF> getDetectedCoordinate(IBitmap iBitmap, int i, int i2, int i3, int i4, int i5) throws RemoteException;

    Rect getDetectedRect() throws RemoteException;

    boolean registerCallback(IRemoteServiceCallback iRemoteServiceCallback, String str) throws RemoteException;

    void requestRecognize(IBitmap iBitmap) throws RemoteException;

    void requestRecognizeByte(IBitmap iBitmap, int i, int i2, int i3) throws RemoteException;

    void requestRecognizeByteWithLocation(IBitmap iBitmap, int i, int i2, int i3) throws RemoteException;

    void resetSceneHandShake() throws RemoteException;

    void resetUnknownCount() throws RemoteException;

    void setRecognitionThreshold(float f) throws RemoteException;

    boolean unregisterCallback(IRemoteServiceCallback iRemoteServiceCallback) throws RemoteException;
}
