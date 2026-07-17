package com.microsoft.appcenter.http;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.utils.NetworkStateHelper;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLException;

/* loaded from: classes.dex */
public class HttpUtils {

    @VisibleForTesting
    static final int MAX_CHARACTERS_DISPLAYED_FOR_SECRET = 8;
    private static final Class[] RECOVERABLE_EXCEPTIONS = {EOFException.class, InterruptedIOException.class, SocketException.class, UnknownHostException.class, RejectedExecutionException.class};
    private static final Pattern CONNECTION_ISSUE_PATTERN = Pattern.compile("connection (time|reset|abort)|failure in ssl library, usually a protocol error|anchor for certification path not found");
    private static final Pattern TOKEN_VALUE_PATTERN = Pattern.compile(":[^\"]+");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("-[^,]+(,|$)");

    @VisibleForTesting
    HttpUtils() {
    }

    public static boolean isRecoverableError(Throwable th) {
        String message;
        if (th instanceof HttpException) {
            int statusCode = ((HttpException) th).getStatusCode();
            return statusCode >= 500 || statusCode == 408 || statusCode == 429;
        }
        for (Class cls : RECOVERABLE_EXCEPTIONS) {
            if (cls.isAssignableFrom(th.getClass())) {
                return true;
            }
        }
        Throwable cause = th.getCause();
        if (cause != null) {
            for (Class cls2 : RECOVERABLE_EXCEPTIONS) {
                if (cls2.isAssignableFrom(cause.getClass())) {
                    return true;
                }
            }
        }
        return (th instanceof SSLException) && (message = th.getMessage()) != null && CONNECTION_ISSUE_PATTERN.matcher(message.toLowerCase(Locale.US)).find();
    }

    public static String hideSecret(@NonNull String str) {
        int length = str.length() - (str.length() < 8 ? 0 : 8);
        char[] cArr = new char[length];
        Arrays.fill(cArr, '*');
        return new String(cArr) + str.substring(length);
    }

    public static String hideApiKeys(@NonNull String str) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = API_KEY_PATTERN.matcher(str);
        int i = 0;
        while (matcher.find()) {
            sb.append(str.substring(i, matcher.start()));
            sb.append("-***");
            sb.append(matcher.group(1));
            i = matcher.end();
        }
        if (i < str.length()) {
            sb.append(str.substring(i));
        }
        return sb.toString();
    }

    public static String hideTickets(@NonNull String str) {
        return TOKEN_VALUE_PATTERN.matcher(str).replaceAll(":***");
    }

    static String hideAuthToken(@NonNull String str) {
        return str.split("\\s+")[0] + " ***";
    }

    public static HttpClient createHttpClient(@NonNull Context context) {
        return createHttpClient(context, true);
    }

    public static HttpClient createHttpClient(@NonNull Context context, boolean z) {
        return new HttpClientRetryer(new HttpClientNetworkStateHandler(new DefaultHttpClient(z), NetworkStateHelper.getSharedInstance(context)));
    }
}
