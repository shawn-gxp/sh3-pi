package androidx.browser.trusted;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.customtabs.trusted.ITrustedWebActivityCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/* loaded from: classes.dex */
public class TrustedWebActivityCallbackRemote {
    private final ITrustedWebActivityCallback mCallbackBinder;

    private TrustedWebActivityCallbackRemote(@NonNull ITrustedWebActivityCallback callbackBinder) {
        this.mCallbackBinder = callbackBinder;
    }

    @Nullable
    static TrustedWebActivityCallbackRemote fromBinder(@Nullable IBinder binder) {
        ITrustedWebActivityCallback asInterface = binder == null ? null : ITrustedWebActivityCallback.Stub.asInterface(binder);
        if (asInterface == null) {
            return null;
        }
        return new TrustedWebActivityCallbackRemote(asInterface);
    }

    public void runExtraCallback(@NonNull String callbackName, @NonNull Bundle args) throws RemoteException {
        this.mCallbackBinder.onExtraCallback(callbackName, args);
    }
}
