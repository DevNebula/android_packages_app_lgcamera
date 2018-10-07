package com.google.android.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.p000v4.view.ViewCompat;

public abstract class BaseStub extends Binder implements IInterface {
    private static TransactionInterceptor globalInterceptor = null;

    static synchronized void installTransactionInterceptorPackagePrivate(TransactionInterceptor interceptor) {
        synchronized (BaseStub.class) {
            if (interceptor == null) {
                throw new IllegalArgumentException("null interceptor");
            } else if (globalInterceptor != null) {
                throw new IllegalStateException("Duplicate TransactionInterceptor installation.");
            } else {
                globalInterceptor = interceptor;
            }
        }
    }

    protected BaseStub(String descriptor) {
        attachInterface(this, descriptor);
    }

    public IBinder asBinder() {
        return this;
    }

    protected boolean routeToSuperOrEnforceInterface(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code > ViewCompat.MEASURED_SIZE_MASK) {
            return super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface(getInterfaceDescriptor());
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (routeToSuperOrEnforceInterface(code, data, reply, flags)) {
            return true;
        }
        if (globalInterceptor == null) {
            return dispatchTransaction(code, data, reply, flags);
        }
        return globalInterceptor.interceptTransaction(this, code, data, reply, flags);
    }

    protected boolean dispatchTransaction(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }
}
