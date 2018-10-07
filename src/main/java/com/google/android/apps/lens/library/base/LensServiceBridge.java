package com.google.android.apps.lens.library.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.apps.gsa.publicsearch.IPublicSearchService;
import com.google.android.apps.gsa.publicsearch.IPublicSearchServiceSession;
import com.google.android.apps.gsa.publicsearch.IPublicSearchServiceSessionCallback.Stub;
import com.google.android.apps.gsa.publicsearch.SystemParcelableWrapper;
import com.google.android.apps.gsa.search.shared.service.proto.nano.ClientEventProto;
import com.google.android.apps.gsa.search.shared.service.proto.nano.LensServiceClientEvent;
import com.google.android.apps.gsa.search.shared.service.proto.nano.LensServiceClientEventData;
import com.google.android.apps.gsa.search.shared.service.proto.nano.LensServiceEvent;
import com.google.android.apps.gsa.search.shared.service.proto.nano.LensServiceEventData;
import com.google.android.apps.gsa.search.shared.service.proto.nano.ServiceEventProto;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;

public class LensServiceBridge extends Stub implements ServiceConnection {
    private static final String BIND_INTENT_ACTION = "com.google.android.apps.gsa.publicsearch.IPublicSearchService";
    private static final boolean DEBUG = false;
    private static final String LENS_CLIENT_SESSION_TYPE = "LENS_SERVICE_SESSION";
    private static final String TAG = "LensServiceBridge";
    private static final int TARGET_SERVICE_API_VERSION = 1;
    @NonNull
    private final Context context;
    @Nullable
    private IPublicSearchService lensService;
    @Nullable
    private volatile IPublicSearchServiceSession lensServiceSession;
    private int serviceApiVersion;

    public LensServiceBridge(@NonNull Context context) {
        this.context = context;
    }

    public boolean bindService() {
        ensureOnMainThread();
        Intent intent = new Intent(BIND_INTENT_ACTION);
        intent.setPackage("com.google.android.googlequicksearchbox");
        try {
            if (this.context.bindService(intent, this, 65)) {
                return true;
            }
            Log.e(TAG, "Unable to bind Lens service.");
            return false;
        } catch (SecurityException e) {
            Log.i(TAG, "Unable to bind Lens service due to security exception. Maybe the service is not available yet.");
            return false;
        }
    }

    public void unbindService() {
        ensureOnMainThread();
        endLensSession();
        this.context.unbindService(this);
        this.lensService = null;
        this.lensServiceSession = null;
        this.serviceApiVersion = 0;
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        ensureOnMainThread();
        Log.i(TAG, "Lens service connected.");
        this.lensService = IPublicSearchService.Stub.asInterface(service);
        beginLensSession();
    }

    public void onServiceDisconnected(ComponentName name) {
        ensureOnMainThread();
        Log.w(TAG, "Lens service disconnected.");
    }

    public void onServiceEvent(byte[] serializedServiceEvent, SystemParcelableWrapper systemParcelableWrapper) {
        try {
            ServiceEventProto serviceEventProto = ServiceEventProto.parseFrom(serializedServiceEvent);
            if (serviceEventProto.getEventId() == 240 && serviceEventProto.hasExtension(LensServiceEvent.lensServiceEventData)) {
                this.serviceApiVersion = ((LensServiceEventData) serviceEventProto.getExtension(LensServiceEvent.lensServiceEventData)).getServiceApiVersion();
            }
        } catch (InvalidProtocolBufferNanoException e) {
            Log.e(TAG, "Unable to parse the protobuf.", e);
        }
    }

    public boolean prewarmLensActivity() {
        Exception e;
        ensureOnMainThread();
        if (isLensSessionReady()) {
            try {
                this.lensServiceSession.onGenericClientEvent(MessageNano.toByteArray(new ClientEventProto().setEventId(347)));
                return true;
            } catch (RemoteException e2) {
                e = e2;
                Log.e(TAG, "Unable to send prewarm signal.", e);
                return false;
            } catch (SecurityException e3) {
                e = e3;
                Log.e(TAG, "Unable to send prewarm signal.", e);
                return false;
            }
        }
        Log.i(TAG, "Lens session is not ready for prewarm.");
        return false;
    }

    private boolean isLensSessionReady() {
        return (this.lensService == null || this.lensServiceSession == null || this.serviceApiVersion <= 0) ? false : true;
    }

    private void beginLensSession() {
        Exception e;
        if (this.lensService != null) {
            ClientEventProto clientEvent = new ClientEventProto().setEventId(348);
            clientEvent.setExtension(LensServiceClientEvent.lensServiceClientEventData, new LensServiceClientEventData().setTargetServiceApiVersion(1));
            try {
                this.lensServiceSession = this.lensService.beginSession(LENS_CLIENT_SESSION_TYPE, this, MessageNano.toByteArray(clientEvent));
                return;
            } catch (RemoteException e2) {
                e = e2;
            } catch (SecurityException e3) {
                e = e3;
            }
        } else {
            return;
        }
        Log.e(TAG, "Unable to begin Lens service session.", e);
    }

    private void endLensSession() {
        Exception e;
        if (this.lensService != null && this.lensServiceSession != null) {
            try {
                this.lensServiceSession.onGenericClientEvent(MessageNano.toByteArray(new ClientEventProto().setEventId(345)));
                return;
            } catch (RemoteException e2) {
                e = e2;
            } catch (SecurityException e3) {
                e = e3;
            }
        } else {
            return;
        }
        Log.e(TAG, "Unable to end Lens service session.", e);
    }

    private void ensureOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("This should be running on the main thread.");
        }
    }
}
