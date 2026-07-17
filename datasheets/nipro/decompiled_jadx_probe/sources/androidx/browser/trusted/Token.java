package androidx.browser.trusted;

import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/* loaded from: classes.dex */
public final class Token {
    private static final String TAG = "Token";

    @NonNull
    private final TokenContents mContents;

    @Nullable
    public static Token create(@NonNull String packageName, @NonNull PackageManager packageManager) {
        List<byte[]> fingerprintsForPackage = PackageIdentityUtils.getFingerprintsForPackage(packageName, packageManager);
        if (fingerprintsForPackage == null) {
            return null;
        }
        try {
            return new Token(TokenContents.create(packageName, fingerprintsForPackage));
        } catch (IOException e) {
            Log.e(TAG, "Exception when creating token.", e);
            return null;
        }
    }

    @NonNull
    public static Token deserialize(@NonNull byte[] serialized) {
        return new Token(TokenContents.deserialize(serialized));
    }

    private Token(@NonNull TokenContents contents) {
        this.mContents = contents;
    }

    @NonNull
    public byte[] serialize() {
        return this.mContents.serialize();
    }

    public boolean matches(@NonNull String packageName, @NonNull PackageManager packageManager) {
        return PackageIdentityUtils.packageMatchesToken(packageName, packageManager, this.mContents);
    }
}
