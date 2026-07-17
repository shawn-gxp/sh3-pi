package com.microsoft.appcenter.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;

/* loaded from: classes.dex */
public class HandlerUtils {

    @VisibleForTesting
    static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() == sMainHandler.getLooper().getThread()) {
            runnable.run();
        } else {
            sMainHandler.post(runnable);
        }
    }

    public static Handler getMainHandler() {
        return sMainHandler;
    }
}
