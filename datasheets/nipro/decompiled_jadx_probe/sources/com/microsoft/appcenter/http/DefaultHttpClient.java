package com.microsoft.appcenter.http;

import android.os.AsyncTask;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.http.DefaultHttpClientCallTask;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.HandlerUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

/* loaded from: classes.dex */
public class DefaultHttpClient implements HttpClient, DefaultHttpClientCallTask.Tracker {
    static final String CHARSET_NAME = "UTF-8";
    static final String CONTENT_ENCODING_KEY = "Content-Encoding";
    static final String CONTENT_ENCODING_VALUE = "gzip";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    static final String CONTENT_TYPE_VALUE = "application/json";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    static final String X_MS_RETRY_AFTER_MS_HEADER = "x-ms-retry-after-ms";
    private final boolean mCompressionEnabled;
    private final Set<DefaultHttpClientCallTask> mTasks;

    @Override // com.microsoft.appcenter.http.HttpClient
    public void reopen() {
    }

    public DefaultHttpClient() {
        this(true);
    }

    public DefaultHttpClient(boolean z) {
        this.mTasks = new HashSet();
        this.mCompressionEnabled = z;
    }

    @VisibleForTesting
    Set<DefaultHttpClientCallTask> getTasks() {
        return this.mTasks;
    }

    @Override // com.microsoft.appcenter.http.HttpClient
    public ServiceCall callAsync(String str, String str2, Map<String, String> map, HttpClient.CallTemplate callTemplate, final ServiceCallback serviceCallback) {
        final DefaultHttpClientCallTask defaultHttpClientCallTask = new DefaultHttpClientCallTask(str, str2, map, callTemplate, serviceCallback, this, this.mCompressionEnabled);
        try {
            defaultHttpClientCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        } catch (RejectedExecutionException e) {
            HandlerUtils.runOnUiThread(new Runnable() { // from class: com.microsoft.appcenter.http.DefaultHttpClient.1
                @Override // java.lang.Runnable
                public void run() {
                    serviceCallback.onCallFailed(e);
                }
            });
        }
        return new ServiceCall() { // from class: com.microsoft.appcenter.http.DefaultHttpClient.2
            @Override // com.microsoft.appcenter.http.ServiceCall
            public void cancel() {
                defaultHttpClientCallTask.cancel(true);
            }
        };
    }

    @Override // com.microsoft.appcenter.http.DefaultHttpClientCallTask.Tracker
    public synchronized void onStart(DefaultHttpClientCallTask defaultHttpClientCallTask) {
        this.mTasks.add(defaultHttpClientCallTask);
    }

    @Override // com.microsoft.appcenter.http.DefaultHttpClientCallTask.Tracker
    public synchronized void onFinish(DefaultHttpClientCallTask defaultHttpClientCallTask) {
        this.mTasks.remove(defaultHttpClientCallTask);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public synchronized void close() {
        if (this.mTasks.size() > 0) {
            AppCenterLog.debug("AppCenter", "Cancelling " + this.mTasks.size() + " network call(s).");
            Iterator<DefaultHttpClientCallTask> it = this.mTasks.iterator();
            while (it.hasNext()) {
                it.next().cancel(true);
            }
            this.mTasks.clear();
        }
    }

    @VisibleForTesting
    boolean isCompressionEnabled() {
        return this.mCompressionEnabled;
    }
}
