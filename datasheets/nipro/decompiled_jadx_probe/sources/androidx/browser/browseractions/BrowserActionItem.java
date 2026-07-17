package androidx.browser.browseractions;

import android.app.PendingIntent;
import android.net.Uri;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

@Deprecated
/* loaded from: classes.dex */
public class BrowserActionItem {

    @Nullable
    private final PendingIntent mAction;

    @DrawableRes
    private int mIconId;

    @Nullable
    private Uri mIconUri;

    @Nullable
    private Runnable mRunnableAction;
    private final String mTitle;

    public BrowserActionItem(@NonNull String title, @NonNull PendingIntent action, @DrawableRes int iconId) {
        this.mTitle = title;
        this.mAction = action;
        this.mIconId = iconId;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public BrowserActionItem(@NonNull String title, @NonNull PendingIntent action, @NonNull Uri iconUri) {
        this.mTitle = title;
        this.mAction = action;
        this.mIconUri = iconUri;
    }

    BrowserActionItem(@NonNull String title, @NonNull Runnable action) {
        this.mTitle = title;
        this.mAction = null;
        this.mRunnableAction = action;
    }

    public BrowserActionItem(@NonNull String title, @NonNull PendingIntent action) {
        this(title, action, 0);
    }

    public int getIconId() {
        return this.mIconId;
    }

    @NonNull
    public String getTitle() {
        return this.mTitle;
    }

    @NonNull
    public PendingIntent getAction() {
        PendingIntent pendingIntent = this.mAction;
        if (pendingIntent != null) {
            return pendingIntent;
        }
        throw new IllegalStateException("Can't call getAction on BrowserActionItem with null action.");
    }

    @Nullable
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public Uri getIconUri() {
        return this.mIconUri;
    }

    @Nullable
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    Runnable getRunnableAction() {
        return this.mRunnableAction;
    }
}
