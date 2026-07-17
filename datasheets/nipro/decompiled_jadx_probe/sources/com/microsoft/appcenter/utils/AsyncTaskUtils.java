package com.microsoft.appcenter.utils;

import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.concurrent.RejectedExecutionException;

/* loaded from: classes.dex */
public class AsyncTaskUtils {
    @VisibleForTesting
    AsyncTaskUtils() {
    }

    @NonNull
    @SafeVarargs
    public static <Params, Type extends AsyncTask<Params, ?, ?>> Type execute(String str, @NonNull Type type, Params... paramsArr) {
        try {
            return (Type) type.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, paramsArr);
        } catch (RejectedExecutionException e) {
            AppCenterLog.warn(str, "THREAD_POOL_EXECUTOR saturated, fall back on SERIAL_EXECUTOR which has an unbounded queue", e);
            return (Type) type.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, paramsArr);
        }
    }
}
