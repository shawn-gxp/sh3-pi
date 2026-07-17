package androidx.browser.customtabs;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/* loaded from: classes.dex */
public class CustomTabsCallback {
    public static final int NAVIGATION_ABORTED = 4;
    public static final int NAVIGATION_FAILED = 3;
    public static final int NAVIGATION_FINISHED = 2;
    public static final int NAVIGATION_STARTED = 1;

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final String ONLINE_EXTRAS_KEY = "online";
    public static final int TAB_HIDDEN = 6;
    public static final int TAB_SHOWN = 5;

    public void extraCallback(@NonNull String callbackName, @Nullable Bundle args) {
    }

    @Nullable
    public Bundle extraCallbackWithResult(@NonNull String callbackName, @Nullable Bundle args) {
        return null;
    }

    public void onMessageChannelReady(@Nullable Bundle extras) {
    }

    public void onNavigationEvent(int navigationEvent, @Nullable Bundle extras) {
    }

    public void onPostMessage(@NonNull String message, @Nullable Bundle extras) {
    }

    public void onRelationshipValidationResult(int relation, @NonNull Uri requestedOrigin, boolean result, @Nullable Bundle extras) {
    }
}
