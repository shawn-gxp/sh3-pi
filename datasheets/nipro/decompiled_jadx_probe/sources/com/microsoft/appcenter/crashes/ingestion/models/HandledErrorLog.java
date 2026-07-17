package com.microsoft.appcenter.crashes.ingestion.models;

import com.microsoft.appcenter.ingestion.models.CommonProperties;
import com.microsoft.appcenter.ingestion.models.LogWithProperties;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/* loaded from: classes.dex */
public class HandledErrorLog extends LogWithProperties {
    private static final String EXCEPTION = "exception";
    public static final String TYPE = "handledError";
    private Exception exception;
    private UUID id;

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public String getType() {
        return TYPE;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override // com.microsoft.appcenter.ingestion.models.LogWithProperties, com.microsoft.appcenter.ingestion.models.AbstractLog, com.microsoft.appcenter.ingestion.models.Model
    public void read(JSONObject jSONObject) throws JSONException {
        super.read(jSONObject);
        setId(UUID.fromString(jSONObject.getString(CommonProperties.ID)));
        if (jSONObject.has(EXCEPTION)) {
            JSONObject jSONObject2 = jSONObject.getJSONObject(EXCEPTION);
            Exception exception = new Exception();
            exception.read(jSONObject2);
            setException(exception);
        }
    }

    @Override // com.microsoft.appcenter.ingestion.models.LogWithProperties, com.microsoft.appcenter.ingestion.models.AbstractLog, com.microsoft.appcenter.ingestion.models.Model
    public void write(JSONStringer jSONStringer) throws JSONException {
        super.write(jSONStringer);
        jSONStringer.key(CommonProperties.ID).value(getId());
        if (getException() != null) {
            jSONStringer.key(EXCEPTION).object();
            this.exception.write(jSONStringer);
            jSONStringer.endObject();
        }
    }

    @Override // com.microsoft.appcenter.ingestion.models.LogWithProperties, com.microsoft.appcenter.ingestion.models.AbstractLog
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || HandledErrorLog.class != obj.getClass() || !super.equals(obj)) {
            return false;
        }
        HandledErrorLog handledErrorLog = (HandledErrorLog) obj;
        UUID uuid = this.id;
        if (uuid == null ? handledErrorLog.id != null : !uuid.equals(handledErrorLog.id)) {
            return false;
        }
        Exception exception = this.exception;
        Exception exception2 = handledErrorLog.exception;
        return exception != null ? exception.equals(exception2) : exception2 == null;
    }

    @Override // com.microsoft.appcenter.ingestion.models.LogWithProperties, com.microsoft.appcenter.ingestion.models.AbstractLog
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        UUID uuid = this.id;
        int hashCode2 = (hashCode + (uuid != null ? uuid.hashCode() : 0)) * 31;
        Exception exception = this.exception;
        return hashCode2 + (exception != null ? exception.hashCode() : 0);
    }
}
