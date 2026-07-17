package com.microsoft.appcenter.ingestion;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.http.AbstractAppCallTemplate;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.http.HttpUtils;
import com.microsoft.appcenter.http.ServiceCall;
import com.microsoft.appcenter.http.ServiceCallback;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import org.json.JSONException;

/* loaded from: classes.dex */
public class AppCenterIngestion implements Ingestion {

    @VisibleForTesting
    static final String API_PATH = "/logs?api-version=1.0.0";
    public static final String DEFAULT_LOG_URL = "https://in.appcenter.ms";

    @VisibleForTesting
    static final String INSTALL_ID = "Install-ID";
    private final HttpClient mHttpClient;
    private final LogSerializer mLogSerializer;
    private String mLogUrl = DEFAULT_LOG_URL;

    public AppCenterIngestion(@NonNull Context context, @NonNull LogSerializer logSerializer) {
        this.mLogSerializer = logSerializer;
        this.mHttpClient = HttpUtils.createHttpClient(context);
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public void setLogUrl(@NonNull String str) {
        this.mLogUrl = str;
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public ServiceCall sendAsync(String str, String str2, UUID uuid, LogContainer logContainer, ServiceCallback serviceCallback) throws IllegalArgumentException {
        HashMap hashMap = new HashMap();
        hashMap.put(INSTALL_ID, uuid.toString());
        hashMap.put(Constants.APP_SECRET, str2);
        if (str != null) {
            hashMap.put(Constants.AUTHORIZATION_HEADER, String.format(Constants.AUTH_TOKEN_FORMAT, str));
        }
        IngestionCallTemplate ingestionCallTemplate = new IngestionCallTemplate(this.mLogSerializer, logContainer);
        return this.mHttpClient.callAsync(this.mLogUrl + API_PATH, "POST", hashMap, ingestionCallTemplate, serviceCallback);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mHttpClient.close();
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public void reopen() {
        this.mHttpClient.reopen();
    }

    private static class IngestionCallTemplate extends AbstractAppCallTemplate {
        private final LogContainer mLogContainer;
        private final LogSerializer mLogSerializer;

        IngestionCallTemplate(LogSerializer logSerializer, LogContainer logContainer) {
            this.mLogSerializer = logSerializer;
            this.mLogContainer = logContainer;
        }

        @Override // com.microsoft.appcenter.http.HttpClient.CallTemplate
        public String buildRequestBody() throws JSONException {
            return this.mLogSerializer.serializeContainer(this.mLogContainer);
        }
    }
}
