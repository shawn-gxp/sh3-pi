package com.microsoft.appcenter.utils.async;

/* loaded from: classes.dex */
public interface AppCenterFuture<T> {
    T get();

    boolean isDone();

    void thenAccept(AppCenterConsumer<T> appCenterConsumer);
}
