package androidx.browser.trusted;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import com.google.common.util.concurrent.ListenableFuture;

/* loaded from: classes.dex */
class FutureUtils {
    @NonNull
    static <T> ListenableFuture<T> immediateFailedFuture(@NonNull Throwable cause) {
        ResolvableFuture create = ResolvableFuture.create();
        create.setException(cause);
        return create;
    }

    private FutureUtils() {
    }
}
