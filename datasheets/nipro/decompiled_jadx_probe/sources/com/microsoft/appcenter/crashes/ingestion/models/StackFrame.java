package com.microsoft.appcenter.crashes.ingestion.models;

import com.microsoft.appcenter.ingestion.models.Model;
import com.microsoft.appcenter.ingestion.models.json.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/* loaded from: classes.dex */
public class StackFrame implements Model {
    private static final String CLASS_NAME = "className";
    private static final String FILE_NAME = "fileName";
    private static final String LINE_NUMBER = "lineNumber";
    private static final String METHOD_NAME = "methodName";
    private String className;
    private String fileName;
    private Integer lineNumber;
    private String methodName;

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String str) {
        this.methodName = str;
    }

    public Integer getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(Integer num) {
        this.lineNumber = num;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    @Override // com.microsoft.appcenter.ingestion.models.Model
    public void read(JSONObject jSONObject) throws JSONException {
        setClassName(jSONObject.optString(CLASS_NAME, null));
        setMethodName(jSONObject.optString(METHOD_NAME, null));
        setLineNumber(JSONUtils.readInteger(jSONObject, LINE_NUMBER));
        setFileName(jSONObject.optString(FILE_NAME, null));
    }

    @Override // com.microsoft.appcenter.ingestion.models.Model
    public void write(JSONStringer jSONStringer) throws JSONException {
        JSONUtils.write(jSONStringer, CLASS_NAME, getClassName());
        JSONUtils.write(jSONStringer, METHOD_NAME, getMethodName());
        JSONUtils.write(jSONStringer, LINE_NUMBER, getLineNumber());
        JSONUtils.write(jSONStringer, FILE_NAME, getFileName());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || StackFrame.class != obj.getClass()) {
            return false;
        }
        StackFrame stackFrame = (StackFrame) obj;
        String str = this.className;
        if (str == null ? stackFrame.className != null : !str.equals(stackFrame.className)) {
            return false;
        }
        String str2 = this.methodName;
        if (str2 == null ? stackFrame.methodName != null : !str2.equals(stackFrame.methodName)) {
            return false;
        }
        Integer num = this.lineNumber;
        if (num == null ? stackFrame.lineNumber != null : !num.equals(stackFrame.lineNumber)) {
            return false;
        }
        String str3 = this.fileName;
        String str4 = stackFrame.fileName;
        return str3 != null ? str3.equals(str4) : str4 == null;
    }

    public int hashCode() {
        String str = this.className;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.methodName;
        int hashCode2 = (hashCode + (str2 != null ? str2.hashCode() : 0)) * 31;
        Integer num = this.lineNumber;
        int hashCode3 = (hashCode2 + (num != null ? num.hashCode() : 0)) * 31;
        String str3 = this.fileName;
        return hashCode3 + (str3 != null ? str3.hashCode() : 0);
    }
}
