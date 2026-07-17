package androidx.browser.customtabs;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY})
/* loaded from: classes.dex */
public interface PostMessageBackend {
    void onDisconnectChannel(@NonNull Context appContext);

    boolean onNotifyMessageChannelReady(@Nullable Bundle extras);

    boolean onPostMessage(@NonNull String message, @Nullable Bundle extras);
}
