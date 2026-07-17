package com.google.android.gms.common.api;

import androidx.annotation.NonNull;
import androidx.annotation.RecentlyNonNull;
import androidx.annotation.RecentlyNullable;
import androidx.annotation.WorkerThread;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.internal.zack;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
/* loaded from: classes.dex */
public abstract class ResultTransform<R extends Result, S extends Result> {
    @NonNull
    public final PendingResult<S> createFailedResult(@RecentlyNonNull Status status) {
        return new zack(status);
    }

    @NonNull
    public Status onFailure(@RecentlyNonNull Status status) {
        return status;
    }

    @RecentlyNullable
    @WorkerThread
    public abstract PendingResult<S> onSuccess(@RecentlyNonNull R r);
}
