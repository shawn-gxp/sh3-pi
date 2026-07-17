package com.google.android.gms.common.api.internal;

import android.os.DeadObjectException;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.TaskCompletionSource;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
abstract class zad<T> extends zac {
    protected final TaskCompletionSource<T> zaa;

    public zad(int i, TaskCompletionSource<T> taskCompletionSource) {
        super(i);
        this.zaa = taskCompletionSource;
    }

    @Override // com.google.android.gms.common.api.internal.zai
    public final void zac(@NonNull Status status) {
        this.zaa.trySetException(new ApiException(status));
    }

    @Override // com.google.android.gms.common.api.internal.zai
    public final void zad(@NonNull Exception exc) {
        this.zaa.trySetException(exc);
    }

    @Override // com.google.android.gms.common.api.internal.zai
    public void zae(@NonNull zaaa zaaaVar, boolean z) {
    }

    @Override // com.google.android.gms.common.api.internal.zai
    public final void zaf(zabl<?> zablVar) throws DeadObjectException {
        try {
            zag(zablVar);
        } catch (DeadObjectException e) {
            zac(zai.zah(e));
            throw e;
        } catch (RemoteException e2) {
            zac(zai.zah(e2));
        } catch (RuntimeException e3) {
            this.zaa.trySetException(e3);
        }
    }

    protected abstract void zag(zabl<?> zablVar) throws RemoteException;
}
