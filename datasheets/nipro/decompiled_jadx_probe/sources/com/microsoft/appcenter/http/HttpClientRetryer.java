package com.microsoft.appcenter.http;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class HttpClientRetryer extends HttpClientDecorator {

    @VisibleForTesting
    static final long[] RETRY_INTERVALS = {TimeUnit.SECONDS.toMillis(10), TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(20)};
    private final Handler mHandler;
    private final Random mRandom;

    HttpClientRetryer(HttpClient httpClient) {
        this(httpClient, new Handler(Looper.getMainLooper()));
    }

    @VisibleForTesting
    HttpClientRetryer(HttpClient httpClient, Handler handler) {
        super(httpClient);
        this.mRandom = new Random();
        this.mHandler = handler;
    }

    @Override // com.microsoft.appcenter.http.HttpClient
    public ServiceCall callAsync(String str, String str2, Map<String, String> map, HttpClient.CallTemplate callTemplate, ServiceCallback serviceCallback) {
        RetryableCall retryableCall = new RetryableCall(this.mDecoratedApi, str, str2, map, callTemplate, serviceCallback);
        retryableCall.run();
        return retryableCall;
    }

    private class RetryableCall extends HttpClientCallDecorator {
        private int mRetryCount;

        RetryableCall(HttpClient httpClient, String str, String str2, Map<String, String> map, HttpClient.CallTemplate callTemplate, ServiceCallback serviceCallback) {
            super(httpClient, str, str2, map, callTemplate, serviceCallback);
        }

        @Override // com.microsoft.appcenter.http.HttpClientCallDecorator, com.microsoft.appcenter.http.ServiceCall
        public synchronized void cancel() {
            HttpClientRetryer.this.mHandler.removeCallbacks(this);
            super.cancel();
        }

        @Override // com.microsoft.appcenter.http.HttpClientCallDecorator, com.microsoft.appcenter.http.ServiceCallback
        public void onCallFailed(Exception exc) {
            String str;
            if (this.mRetryCount < HttpClientRetryer.RETRY_INTERVALS.length && HttpUtils.isRecoverableError(exc)) {
                long parseLong = (!(exc instanceof HttpException) || (str = ((HttpException) exc).getHeaders().get("x-ms-retry-after-ms")) == null) ? 0L : Long.parseLong(str);
                if (parseLong == 0) {
                    long[] jArr = HttpClientRetryer.RETRY_INTERVALS;
                    int i = this.mRetryCount;
                    this.mRetryCount = i + 1;
                    parseLong = HttpClientRetryer.this.mRandom.nextInt((int) r1) + (jArr[i] / 2);
                }
                String str2 = "Try #" + this.mRetryCount + " failed and will be retried in " + parseLong + " ms";
                if (exc instanceof UnknownHostException) {
                    str2 = str2 + " (UnknownHostException)";
                }
                AppCenterLog.warn("AppCenter", str2, exc);
                HttpClientRetryer.this.mHandler.postDelayed(this, parseLong);
                return;
            }
            this.mServiceCallback.onCallFailed(exc);
        }
    }
}
