package com.google.android.gms.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;

/* compiled from: com.google.android.gms:play-services-tasks@@17.2.1 */
/* loaded from: classes.dex */
public class TaskCompletionSource<TResult> {
    private final zzw<TResult> zza = new zzw<>();

    public TaskCompletionSource() {
    }

    @NonNull
    public Task<TResult> getTask() {
        return this.zza;
    }

    public void setException(@RecentlyNonNull Exception exc) {
        this.zza.zzc(exc);
    }

    public void setResult(@Nullable TResult tresult) {
        this.zza.zza(tresult);
    }

    public boolean trySetException(@RecentlyNonNull Exception exc) {
        return this.zza.zzd(exc);
    }

    public boolean trySetResult(@Nullable TResult tresult) {
        return this.zza.zzb(tresult);
    }

    public TaskCompletionSource(@RecentlyNonNull CancellationToken cancellationToken) {
        cancellationToken.onCanceledRequested(new zzs(this));
    }
}
