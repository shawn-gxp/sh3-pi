package com.microsoft.appcenter.ingestion.models;

import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.ingestion.models.json.JSONDateUtils;
import com.microsoft.appcenter.ingestion.models.json.JSONUtils;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/* loaded from: classes.dex */
public abstract class AbstractLog implements Log {

    @VisibleForTesting
    static final String DEVICE = "device";
    private static final String DISTRIBUTION_GROUP_ID = "distributionGroupId";
    private static final String SID = "sid";
    private static final String TIMESTAMP = "timestamp";
    private static final String USER_ID = "userId";
    private Device device;
    private String distributionGroupId;
    private UUID sid;
    private Object tag;
    private Date timestamp;
    private final Set<String> transmissionTargetTokens = new LinkedHashSet();
    private String userId;

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setTimestamp(Date date) {
        this.timestamp = date;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public UUID getSid() {
        return this.sid;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setSid(UUID uuid) {
        this.sid = uuid;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public String getDistributionGroupId() {
        return this.distributionGroupId;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setDistributionGroupId(String str) {
        this.distributionGroupId = str;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public String getUserId() {
        return this.userId;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setUserId(String str) {
        this.userId = str;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public Device getDevice() {
        return this.device;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setDevice(Device device) {
        this.device = device;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public Object getTag() {
        return this.tag;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public void setTag(Object obj) {
        this.tag = obj;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public synchronized void addTransmissionTarget(String str) {
        this.transmissionTargetTokens.add(str);
    }

    @Override // com.microsoft.appcenter.ingestion.models.Log
    public synchronized Set<String> getTransmissionTargetTokens() {
        return Collections.unmodifiableSet(this.transmissionTargetTokens);
    }

    @Override // com.microsoft.appcenter.ingestion.models.Model
    public void write(JSONStringer jSONStringer) throws JSONException {
        JSONUtils.write(jSONStringer, CommonProperties.TYPE, getType());
        jSONStringer.key(TIMESTAMP).value(JSONDateUtils.toString(getTimestamp()));
        JSONUtils.write(jSONStringer, SID, getSid());
        JSONUtils.write(jSONStringer, DISTRIBUTION_GROUP_ID, getDistributionGroupId());
        JSONUtils.write(jSONStringer, USER_ID, getUserId());
        if (getDevice() != null) {
            jSONStringer.key(DEVICE).object();
            getDevice().write(jSONStringer);
            jSONStringer.endObject();
        }
    }

    @Override // com.microsoft.appcenter.ingestion.models.Model
    public void read(JSONObject jSONObject) throws JSONException {
        if (!jSONObject.getString(CommonProperties.TYPE).equals(getType())) {
            throw new JSONException("Invalid type");
        }
        setTimestamp(JSONDateUtils.toDate(jSONObject.getString(TIMESTAMP)));
        if (jSONObject.has(SID)) {
            setSid(UUID.fromString(jSONObject.getString(SID)));
        }
        setDistributionGroupId(jSONObject.optString(DISTRIBUTION_GROUP_ID, null));
        setUserId(jSONObject.optString(USER_ID, null));
        if (jSONObject.has(DEVICE)) {
            Device device = new Device();
            device.read(jSONObject.getJSONObject(DEVICE));
            setDevice(device);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractLog abstractLog = (AbstractLog) obj;
        if (!this.transmissionTargetTokens.equals(abstractLog.transmissionTargetTokens)) {
            return false;
        }
        Date date = this.timestamp;
        if (date == null ? abstractLog.timestamp != null : !date.equals(abstractLog.timestamp)) {
            return false;
        }
        UUID uuid = this.sid;
        if (uuid == null ? abstractLog.sid != null : !uuid.equals(abstractLog.sid)) {
            return false;
        }
        String str = this.distributionGroupId;
        if (str == null ? abstractLog.distributionGroupId != null : !str.equals(abstractLog.distributionGroupId)) {
            return false;
        }
        String str2 = this.userId;
        if (str2 == null ? abstractLog.userId != null : !str2.equals(abstractLog.userId)) {
            return false;
        }
        Device device = this.device;
        if (device == null ? abstractLog.device != null : !device.equals(abstractLog.device)) {
            return false;
        }
        Object obj2 = this.tag;
        Object obj3 = abstractLog.tag;
        return obj2 != null ? obj2.equals(obj3) : obj3 == null;
    }

    public int hashCode() {
        int hashCode = this.transmissionTargetTokens.hashCode() * 31;
        Date date = this.timestamp;
        int hashCode2 = (hashCode + (date != null ? date.hashCode() : 0)) * 31;
        UUID uuid = this.sid;
        int hashCode3 = (hashCode2 + (uuid != null ? uuid.hashCode() : 0)) * 31;
        String str = this.distributionGroupId;
        int hashCode4 = (hashCode3 + (str != null ? str.hashCode() : 0)) * 31;
        String str2 = this.userId;
        int hashCode5 = (hashCode4 + (str2 != null ? str2.hashCode() : 0)) * 31;
        Device device = this.device;
        int hashCode6 = (hashCode5 + (device != null ? device.hashCode() : 0)) * 31;
        Object obj = this.tag;
        return hashCode6 + (obj != null ? obj.hashCode() : 0);
    }
}
