package com.google.android.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class BaseProxy implements IInterface {
    private final String mDescriptor;
    private final IBinder mRemote;

    protected BaseProxy(IBinder remote, String descriptor) {
        this.mRemote = remote;
        this.mDescriptor = descriptor;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    protected Parcel obtainAndWriteInterfaceToken() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(this.mDescriptor);
        return parcel;
    }

    protected Parcel transactAndReadException(int code, Parcel in) throws RemoteException {
        in = Parcel.obtain();
        try {
            this.mRemote.transact(code, in, in, 0);
            in.readException();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            in.recycle();
            throw e;
        }
        return in;
    }

    protected void transactAndReadExceptionReturnVoid(int code, Parcel in) throws RemoteException {
        Parcel out = Parcel.obtain();
        try {
            this.mRemote.transact(code, in, out, 0);
            out.readException();
        } finally {
            in.recycle();
            out.recycle();
        }
    }

    protected void transactOneway(int code, Parcel in) throws RemoteException {
        try {
            this.mRemote.transact(code, in, null, 1);
        } finally {
            in.recycle();
        }
    }
}
