package com.microsoft.appcenter.utils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* loaded from: classes.dex */
public class HashUtils {
    private static final char[] HEXADECIMAL_OUTPUT = "0123456789abcdef".toCharArray();

    @VisibleForTesting
    HashUtils() {
    }

    @NonNull
    public static String sha256(@NonNull String str) {
        return sha256(str, "UTF-8");
    }

    @NonNull
    @VisibleForTesting
    static String sha256(@NonNull String str, String str2) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(str2));
            return encodeHex(messageDigest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static String encodeHex(@NonNull byte[] bArr) {
        char[] cArr = new char[bArr.length * 2];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            int i3 = i * 2;
            char[] cArr2 = HEXADECIMAL_OUTPUT;
            cArr[i3] = cArr2[i2 >>> 4];
            cArr[i3 + 1] = cArr2[i2 & 15];
        }
        return new String(cArr);
    }
}
