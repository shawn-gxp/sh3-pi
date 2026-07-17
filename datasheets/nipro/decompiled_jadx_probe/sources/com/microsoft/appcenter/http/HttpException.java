package com.microsoft.appcenter.http;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class HttpException extends IOException {
    private final Map<String, String> headers;
    private final String payload;
    private final int statusCode;

    public HttpException(int i) {
        this(i, "");
    }

    public HttpException(int i, @NonNull String str) {
        this(i, str, new HashMap());
    }

    public HttpException(int i, @NonNull String str, @NonNull Map<String, String> map) {
        super(getDetailMessage(i, str));
        this.payload = str;
        this.statusCode = i;
        this.headers = map;
    }

    @NonNull
    private static String getDetailMessage(int i, @NonNull String str) {
        if (TextUtils.isEmpty(str)) {
            return String.valueOf(i);
        }
        return i + " - " + str;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @NonNull
    public String getPayload() {
        return this.payload;
    }

    @NonNull
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || HttpException.class != obj.getClass()) {
            return false;
        }
        HttpException httpException = (HttpException) obj;
        return this.statusCode == httpException.statusCode && this.payload.equals(httpException.payload) && this.headers.equals(httpException.headers);
    }

    public int hashCode() {
        return (((this.statusCode * 31) + this.payload.hashCode()) * 31) + this.headers.hashCode();
    }
}
