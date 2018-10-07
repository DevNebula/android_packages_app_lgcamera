package com.google.android.apps.gsa.publicsearch;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.aidl.BaseProxy;
import com.google.android.aidl.BaseStub;
import com.google.android.aidl.Codecs;

public interface IPublicSearchService extends IInterface {

    public static abstract class Stub extends BaseStub implements IPublicSearchService {
        private static final String DESCRIPTOR = "com.google.android.apps.gsa.publicsearch.IPublicSearchService";
        static final int TRANSACTION_beginSession = 1;

        public static class Proxy extends BaseProxy implements IPublicSearchService {
            Proxy(IBinder remote) {
                super(remote, Stub.DESCRIPTOR);
            }

            public IPublicSearchServiceSession beginSession(String sessionType, IPublicSearchServiceSessionCallback callback, byte[] serializedSessionContext) throws RemoteException {
                Parcel data = obtainAndWriteInterfaceToken();
                data.writeString(sessionType);
                Codecs.writeStrongBinder(data, callback);
                data.writeByteArray(serializedSessionContext);
                Parcel reply = transactAndReadException(1, data);
                IPublicSearchServiceSession retval = com.google.android.apps.gsa.publicsearch.IPublicSearchServiceSession.Stub.asInterface(reply.readStrongBinder());
                reply.recycle();
                return retval;
            }
        }

        public Stub() {
            super(DESCRIPTOR);
        }

        public static IPublicSearchService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IPublicSearchService) {
                return (IPublicSearchService) iin;
            }
            return new Proxy(obj);
        }

        protected boolean dispatchTransaction(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                return false;
            }
            IPublicSearchServiceSession retval = beginSession(data.readString(), com.google.android.apps.gsa.publicsearch.IPublicSearchServiceSessionCallback.Stub.asInterface(data.readStrongBinder()), data.createByteArray());
            reply.writeNoException();
            Codecs.writeStrongBinder(reply, retval);
            return true;
        }
    }

    IPublicSearchServiceSession beginSession(String str, IPublicSearchServiceSessionCallback iPublicSearchServiceSessionCallback, byte[] bArr) throws RemoteException;
}
