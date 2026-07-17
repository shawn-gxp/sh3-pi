package com.microsoft.appcenter.ingestion.models.json;

import androidx.annotation.NonNull;
import com.microsoft.appcenter.ingestion.models.CommonProperties;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.LogContainer;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/* loaded from: classes.dex */
public class DefaultLogSerializer implements LogSerializer {
    private static final String LOGS = "logs";
    private final Map<String, LogFactory> mLogFactories = new HashMap();

    @NonNull
    private JSONStringer writeLog(JSONStringer jSONStringer, Log log) throws JSONException {
        jSONStringer.object();
        log.write(jSONStringer);
        jSONStringer.endObject();
        return jSONStringer;
    }

    @NonNull
    private Log readLog(JSONObject jSONObject, String str) throws JSONException {
        if (str == null) {
            str = jSONObject.getString(CommonProperties.TYPE);
        }
        LogFactory logFactory = this.mLogFactories.get(str);
        if (logFactory == null) {
            throw new JSONException("Unknown log type: " + str);
        }
        Log create = logFactory.create();
        create.read(jSONObject);
        return create;
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    @NonNull
    public String serializeLog(@NonNull Log log) throws JSONException {
        return writeLog(new JSONStringer(), log).toString();
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    @NonNull
    public Log deserializeLog(@NonNull String str, String str2) throws JSONException {
        return readLog(new JSONObject(str), str2);
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    public Collection<CommonSchemaLog> toCommonSchemaLog(@NonNull Log log) {
        return this.mLogFactories.get(log.getType()).toCommonSchemaLogs(log);
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    @NonNull
    public String serializeContainer(@NonNull LogContainer logContainer) throws JSONException {
        JSONStringer jSONStringer = new JSONStringer();
        jSONStringer.object();
        jSONStringer.key(LOGS).array();
        Iterator<Log> it = logContainer.getLogs().iterator();
        while (it.hasNext()) {
            writeLog(jSONStringer, it.next());
        }
        jSONStringer.endArray();
        jSONStringer.endObject();
        return jSONStringer.toString();
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    @NonNull
    public LogContainer deserializeContainer(@NonNull String str, String str2) throws JSONException {
        JSONObject jSONObject = new JSONObject(str);
        LogContainer logContainer = new LogContainer();
        JSONArray jSONArray = jSONObject.getJSONArray(LOGS);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            arrayList.add(readLog(jSONArray.getJSONObject(i), str2));
        }
        logContainer.setLogs(arrayList);
        return logContainer;
    }

    @Override // com.microsoft.appcenter.ingestion.models.json.LogSerializer
    public void addLogFactory(@NonNull String str, @NonNull LogFactory logFactory) {
        this.mLogFactories.put(str, logFactory);
    }
}
