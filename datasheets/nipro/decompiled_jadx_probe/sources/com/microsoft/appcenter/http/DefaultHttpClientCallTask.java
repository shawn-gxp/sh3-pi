package com.microsoft.appcenter.http;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Pair;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

/* loaded from: classes.dex */
class DefaultHttpClientCallTask extends AsyncTask<Void, Void, Object> {
    private static final int CONNECT_TIMEOUT = 60000;
    private static final int DEFAULT_STRING_BUILDER_CAPACITY = 16;
    private static final int MAX_PRETTIFY_LOG_LENGTH = 4096;
    private static final int MIN_GZIP_LENGTH = 1400;
    private static final int READ_BUFFER_SIZE = 1024;
    private static final int READ_TIMEOUT = 20000;
    private static final int THREAD_STATS_TAG = -667034599;
    private static final int WRITE_BUFFER_SIZE = 1024;
    private final HttpClient.CallTemplate mCallTemplate;
    private final boolean mCompressionEnabled;
    private final Map<String, String> mHeaders;
    private final String mMethod;
    private final ServiceCallback mServiceCallback;
    private final Tracker mTracker;
    private final String mUrl;
    private static final Pattern TOKEN_REGEX_URL_ENCODED = Pattern.compile("token=[^&]+");
    private static final Pattern TOKEN_REGEX_JSON = Pattern.compile("token\":\"[^\"]+\"");

    interface Tracker {
        void onFinish(DefaultHttpClientCallTask defaultHttpClientCallTask);

        void onStart(DefaultHttpClientCallTask defaultHttpClientCallTask);
    }

    DefaultHttpClientCallTask(String str, String str2, Map<String, String> map, HttpClient.CallTemplate callTemplate, ServiceCallback serviceCallback, Tracker tracker, boolean z) {
        this.mUrl = str;
        this.mMethod = str2;
        this.mHeaders = map;
        this.mCallTemplate = callTemplate;
        this.mServiceCallback = serviceCallback;
        this.mTracker = tracker;
        this.mCompressionEnabled = z;
    }

    private static InputStream getInputStream(HttpsURLConnection httpsURLConnection) throws IOException {
        int responseCode = httpsURLConnection.getResponseCode();
        if (responseCode >= 200 && responseCode < 400) {
            return httpsURLConnection.getInputStream();
        }
        return httpsURLConnection.getErrorStream();
    }

    private void writePayload(OutputStream outputStream, byte[] bArr) throws IOException {
        for (int i = 0; i < bArr.length; i += 1024) {
            outputStream.write(bArr, i, Math.min(bArr.length - i, 1024));
            if (isCancelled()) {
                return;
            }
        }
    }

    private String readResponse(HttpsURLConnection httpsURLConnection) throws IOException {
        StringBuilder sb = new StringBuilder(Math.max(httpsURLConnection.getContentLength(), 16));
        InputStream inputStream = getInputStream(httpsURLConnection);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            char[] cArr = new char[1024];
            do {
                int read = inputStreamReader.read(cArr);
                if (read <= 0) {
                    break;
                }
                sb.append(cArr, 0, read);
            } while (!isCancelled());
            return sb.toString();
        } finally {
            inputStream.close();
        }
    }

    private Pair<String, Map<String, String>> doHttpCall() throws Exception {
        String str;
        byte[] bArr;
        String replaceAll;
        if (!this.mUrl.startsWith("https")) {
            throw new IOException("App Center support only HTTPS connection.");
        }
        URL url = new URL(this.mUrl);
        URLConnection openConnection = url.openConnection();
        if (!(openConnection instanceof HttpsURLConnection)) {
            throw new IOException("App Center supports only HTTPS connection.");
        }
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) openConnection;
        try {
            if (Build.VERSION.SDK_INT <= 21) {
                httpsURLConnection.setSSLSocketFactory(new TLS1_2SocketFactory());
            }
            httpsURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
            httpsURLConnection.setReadTimeout(READ_TIMEOUT);
            httpsURLConnection.setRequestMethod(this.mMethod);
            boolean z = false;
            if (!this.mMethod.equals("POST") || this.mCallTemplate == null) {
                str = null;
                bArr = null;
            } else {
                str = this.mCallTemplate.buildRequestBody();
                bArr = str.getBytes("UTF-8");
                if (this.mCompressionEnabled && bArr.length >= MIN_GZIP_LENGTH) {
                    z = true;
                }
                if (!this.mHeaders.containsKey(DefaultHttpClient.CONTENT_TYPE_KEY)) {
                    this.mHeaders.put(DefaultHttpClient.CONTENT_TYPE_KEY, "application/json");
                }
            }
            if (z) {
                this.mHeaders.put("Content-Encoding", "gzip");
            }
            for (Map.Entry<String, String> entry : this.mHeaders.entrySet()) {
                httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (isCancelled()) {
                return null;
            }
            if (this.mCallTemplate != null) {
                this.mCallTemplate.onBeforeCalling(url, this.mHeaders);
            }
            if (bArr != null) {
                if (AppCenterLog.getLogLevel() <= 2) {
                    if (str.length() < 4096) {
                        str = TOKEN_REGEX_URL_ENCODED.matcher(str).replaceAll("token=***");
                        if ("application/json".equals(this.mHeaders.get(DefaultHttpClient.CONTENT_TYPE_KEY))) {
                            str = new JSONObject(str).toString(2);
                        }
                    }
                    AppCenterLog.verbose("AppCenter", str);
                }
                if (z) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
                    GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                    gZIPOutputStream.write(bArr);
                    gZIPOutputStream.close();
                    bArr = byteArrayOutputStream.toByteArray();
                }
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setFixedLengthStreamingMode(bArr.length);
                OutputStream outputStream = httpsURLConnection.getOutputStream();
                try {
                    writePayload(outputStream, bArr);
                    outputStream.close();
                } catch (Throwable th) {
                    outputStream.close();
                    throw th;
                }
            }
            if (isCancelled()) {
                return null;
            }
            int responseCode = httpsURLConnection.getResponseCode();
            String readResponse = readResponse(httpsURLConnection);
            if (AppCenterLog.getLogLevel() <= 2) {
                String headerField = httpsURLConnection.getHeaderField(DefaultHttpClient.CONTENT_TYPE_KEY);
                if (headerField != null && !headerField.startsWith("text/") && !headerField.startsWith("application/")) {
                    replaceAll = "<binary>";
                    AppCenterLog.verbose("AppCenter", "HTTP response status=" + responseCode + " payload=" + replaceAll);
                }
                replaceAll = TOKEN_REGEX_JSON.matcher(readResponse).replaceAll("token\":\"***\"");
                AppCenterLog.verbose("AppCenter", "HTTP response status=" + responseCode + " payload=" + replaceAll);
            }
            HashMap hashMap = new HashMap();
            for (Map.Entry entry2 : httpsURLConnection.getHeaderFields().entrySet()) {
                hashMap.put(entry2.getKey(), ((List) entry2.getValue()).iterator().next());
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new HttpException(responseCode, readResponse, hashMap);
            }
            return new Pair<>(readResponse, hashMap);
        } finally {
            httpsURLConnection.disconnect();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Object doInBackground(Void... voidArr) {
        TrafficStats.setThreadStatsTag(THREAD_STATS_TAG);
        try {
            return doHttpCall();
        } catch (Exception e) {
            return e;
        } finally {
            TrafficStats.clearThreadStatsTag();
        }
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        this.mTracker.onStart(this);
    }

    @Override // android.os.AsyncTask
    protected void onPostExecute(Object obj) {
        this.mTracker.onFinish(this);
        if (obj instanceof Exception) {
            this.mServiceCallback.onCallFailed((Exception) obj);
        } else {
            Pair pair = (Pair) obj;
            this.mServiceCallback.onCallSucceeded((String) pair.first, (Map) pair.second);
        }
    }

    @Override // android.os.AsyncTask
    protected void onCancelled(Object obj) {
        if ((obj instanceof Pair) || (obj instanceof HttpException)) {
            onPostExecute(obj);
        } else {
            this.mTracker.onFinish(this);
        }
    }
}
