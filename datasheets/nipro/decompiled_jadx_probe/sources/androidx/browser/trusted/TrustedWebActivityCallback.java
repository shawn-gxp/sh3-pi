package androidx.browser.trusted;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/* loaded from: classes.dex */
public abstract class TrustedWebActivityCallback {
    public abstract void onExtraCallback(@NonNull String callbackName, @Nullable Bundle args);
}
