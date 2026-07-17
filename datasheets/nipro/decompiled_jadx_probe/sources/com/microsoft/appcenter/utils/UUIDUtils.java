package com.microsoft.appcenter.utils;

import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.VisibleForTesting;
import java.util.Random;
import java.util.UUID;

/* loaded from: classes.dex */
public class UUIDUtils {

    @VisibleForTesting
    static Implementation sImplementation = new Implementation() { // from class: com.microsoft.appcenter.utils.UUIDUtils.1
        @Override // com.microsoft.appcenter.utils.UUIDUtils.Implementation
        public UUID randomUUID() {
            return UUID.randomUUID();
        }
    };
    private static Random sRandom;

    @VisibleForTesting
    interface Implementation {
        UUID randomUUID();
    }

    @VisibleForTesting
    UUIDUtils() {
    }

    public static UUID randomUUID() {
        try {
            return sImplementation.randomUUID();
        } catch (SecurityException e) {
            initFailOver(e);
            return new UUID((sRandom.nextLong() & (-61441)) | PlaybackStateCompat.ACTION_PREPARE, (sRandom.nextLong() & 4611686018427387903L) | Long.MIN_VALUE);
        }
    }

    private static synchronized void initFailOver(SecurityException securityException) {
        synchronized (UUIDUtils.class) {
            if (sRandom == null) {
                sRandom = new Random();
                AppCenterLog.error("AppCenter", "UUID.randomUUID failed, using Random as fallback", securityException);
            }
        }
    }
}
