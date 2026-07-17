package com.microsoft.appcenter.ingestion;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.http.DefaultHttpClient;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.http.HttpUtils;
import com.microsoft.appcenter.http.ServiceCall;
import com.microsoft.appcenter.http.ServiceCallback;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.TicketCache;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OneCollectorIngestion implements Ingestion {

    @VisibleForTesting
    static final String API_KEY = "apikey";
    private static final String CLIENT_VERSION_FORMAT = "ACS-Android-Java-no-%s-no";

    @VisibleForTesting
    static final String CLIENT_VERSION_KEY = "Client-Version";
    private static final String CONTENT_TYPE_VALUE = "application/x-json-stream; charset=utf-8";
    private static final String DEFAULT_LOG_URL = "https://mobile.events.data.microsoft.com/OneCollector/1.0";

    @VisibleForTesting
    static final String STRICT = "Strict";

    @VisibleForTesting
    static final String TICKETS = "Tickets";

    @VisibleForTesting
    static final String UPLOAD_TIME_KEY = "Upload-Time";
    private final HttpClient mHttpClient;
    private final LogSerializer mLogSerializer;
    private String mLogUrl = DEFAULT_LOG_URL;

    public OneCollectorIngestion(@NonNull Context context, @NonNull LogSerializer logSerializer) {
        this.mLogSerializer = logSerializer;
        this.mHttpClient = HttpUtils.createHttpClient(context);
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public ServiceCall sendAsync(String str, String str2, UUID uuid, LogContainer logContainer, ServiceCallback serviceCallback) throws IllegalArgumentException {
        HashMap hashMap = new HashMap();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        Iterator<Log> it = logContainer.getLogs().iterator();
        while (it.hasNext()) {
            linkedHashSet.addAll(it.next().getTransmissionTargetTokens());
        }
        StringBuilder sb = new StringBuilder();
        Iterator it2 = linkedHashSet.iterator();
        while (it2.hasNext()) {
            sb.append((String) it2.next());
            sb.append(",");
        }
        if (!linkedHashSet.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        hashMap.put(API_KEY, sb.toString());
        JSONObject jSONObject = new JSONObject();
        Iterator<Log> it3 = logContainer.getLogs().iterator();
        while (it3.hasNext()) {
            List<String> ticketKeys = ((CommonSchemaLog) it3.next()).getExt().getProtocol().getTicketKeys();
            if (ticketKeys != null) {
                for (String str3 : ticketKeys) {
                    String ticket = TicketCache.getTicket(str3);
                    if (ticket != null) {
                        try {
                            jSONObject.put(str3, ticket);
                        } catch (JSONException e) {
                            AppCenterLog.error("AppCenter", "Cannot serialize tickets, sending log anonymously", e);
                        }
                    }
                }
            }
        }
        if (jSONObject.length() > 0) {
            hashMap.put(TICKETS, jSONObject.toString());
            if (Constants.APPLICATION_DEBUGGABLE) {
                hashMap.put(STRICT, Boolean.TRUE.toString());
            }
        }
        hashMap.put(DefaultHttpClient.CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
        hashMap.put(CLIENT_VERSION_KEY, String.format(CLIENT_VERSION_FORMAT, "2.1.0"));
        hashMap.put(UPLOAD_TIME_KEY, String.valueOf(System.currentTimeMillis()));
        return this.mHttpClient.callAsync(this.mLogUrl, "POST", hashMap, new IngestionCallTemplate(this.mLogSerializer, logContainer), serviceCallback);
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public void setLogUrl(@NonNull String str) {
        this.mLogUrl = str;
    }

    @Override // com.microsoft.appcenter.ingestion.Ingestion
    public void reopen() {
        this.mHttpClient.reopen();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mHttpClient.close();
    }

    private static class IngestionCallTemplate implements HttpClient.CallTemplate {
        private final LogContainer mLogContainer;
        private final LogSerializer mLogSerializer;

        IngestionCallTemplate(LogSerializer logSerializer, LogContainer logContainer) {
            this.mLogSerializer = logSerializer;
            this.mLogContainer = logContainer;
        }

        @Override // com.microsoft.appcenter.http.HttpClient.CallTemplate
        public String buildRequestBody() throws JSONException {
            StringBuilder sb = new StringBuilder();
            Iterator<Log> it = this.mLogContainer.getLogs().iterator();
            while (it.hasNext()) {
                sb.append(this.mLogSerializer.serializeLog(it.next()));
                sb.append('\n');
            }
            return sb.toString();
        }

        @Override // com.microsoft.appcenter.http.HttpClient.CallTemplate
        public void onBeforeCalling(URL url, Map<String, String> map) {
            if (AppCenterLog.getLogLevel() <= 2) {
                AppCenterLog.verbose("AppCenter", "Calling " + url + "...");
                HashMap hashMap = new HashMap(map);
                String str = (String) hashMap.get(OneCollectorIngestion.API_KEY);
                if (str != null) {
                    hashMap.put(OneCollectorIngestion.API_KEY, HttpUtils.hideApiKeys(str));
                }
                String str2 = (String) hashMap.get(OneCollectorIngestion.TICKETS);
                if (str2 != null) {
                    hashMap.put(OneCollectorIngestion.TICKETS, HttpUtils.hideTickets(str2));
                }
                AppCenterLog.verbose("AppCenter", "Headers: " + hashMap);
            }
        }
    }
}
