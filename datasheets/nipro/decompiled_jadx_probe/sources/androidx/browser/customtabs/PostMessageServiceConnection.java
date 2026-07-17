package androidx.browser.customtabs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.customtabs.ICustomTabsCallback;
import android.support.customtabs.IPostMessageService;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/* loaded from: classes.dex */
public abstract class PostMessageServiceConnection implements PostMessageBackend, ServiceConnection {
    private static final String TAG = "PostMessageServConn";
    private final Object mLock = new Object();
    private boolean mMessageChannelCreated;

    @Nullable
    private String mPackageName;

    @Nullable
    private IPostMessageService mService;
    private final ICustomTabsCallback mSessionBinder;

    public void onPostMessageServiceDisconnected() {
    }

    public PostMessageServiceConnection(@NonNull CustomTabsSessionToken session) {
        IBinder callbackBinder = session.getCallbackBinder();
        if (callbackBinder == null) {
            throw new IllegalArgumentException("Provided session must have binder.");
        }
        this.mSessionBinder = ICustomTabsCallback.Stub.asInterface(callbackBinder);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public void setPackageName(@NonNull String packageName) {
        this.mPackageName = packageName;
    }

    public boolean bindSessionToPostMessageService(@NonNull Context context, @NonNull String packageName) {
        Intent intent = new Intent();
        intent.setClassName(packageName, PostMessageService.class.getName());
        boolean bindService = context.bindService(intent, this, 1);
        if (!bindService) {
            Log.w(TAG, "Could not bind to PostMessageService in client.");
        }
        return bindService;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public boolean bindSessionToPostMessageService(@NonNull Context appContext) {
        String str = this.mPackageName;
        if (str == null) {
            throw new IllegalStateException("setPackageName must be called before bindSessionToPostMessageService.");
        }
        return bindSessionToPostMessageService(appContext, str);
    }

    private boolean isBoundToService() {
        return this.mService != null;
    }

    public void unbindFromContext(@NonNull Context context) {
        if (isBoundToService()) {
            context.unbindService(this);
            this.mService = null;
        }
    }

    @Override // android.content.ServiceConnection
    public final void onServiceConnected(@NonNull ComponentName name, @NonNull IBinder service) {
        this.mService = IPostMessageService.Stub.asInterface(service);
        onPostMessageServiceConnected();
    }

    @Override // android.content.ServiceConnection
    public final void onServiceDisconnected(@NonNull ComponentName name) {
        this.mService = null;
        onPostMessageServiceDisconnected();
    }

    @Override // androidx.browser.customtabs.PostMessageBackend
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public final boolean onNotifyMessageChannelReady(@Nullable Bundle extras) {
        return notifyMessageChannelReady(extras);
    }

    public final boolean notifyMessageChannelReady(@Nullable Bundle extras) {
        this.mMessageChannelCreated = true;
        return notifyMessageChannelReadyInternal(extras);
    }

    private boolean notifyMessageChannelReadyInternal(@Nullable Bundle extras) {
        if (this.mService == null) {
            return false;
        }
        synchronized (this.mLock) {
            try {
                try {
                    this.mService.onMessageChannelReady(this.mSessionBinder, extras);
                } catch (RemoteException unused) {
                    return false;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return true;
    }

    @Override // androidx.browser.customtabs.PostMessageBackend
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public final boolean onPostMessage(@NonNull String message, @Nullable Bundle extras) {
        return postMessage(message, extras);
    }

    public final boolean postMessage(@NonNull String message, @Nullable Bundle extras) {
        if (this.mService == null) {
            return false;
        }
        synchronized (this.mLock) {
            try {
                try {
                    this.mService.onPostMessage(this.mSessionBinder, message, extras);
                } catch (RemoteException unused) {
                    return false;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return true;
    }

    @Override // androidx.browser.customtabs.PostMessageBackend
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public void onDisconnectChannel(@NonNull Context appContext) {
        unbindFromContext(appContext);
    }

    public void onPostMessageServiceConnected() {
        if (this.mMessageChannelCreated) {
            notifyMessageChannelReadyInternal(null);
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public void cleanup(@NonNull Context context) {
        if (isBoundToService()) {
            unbindFromContext(context);
        }
    }
}
