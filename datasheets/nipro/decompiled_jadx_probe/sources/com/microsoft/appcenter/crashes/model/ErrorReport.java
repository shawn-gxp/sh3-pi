package com.microsoft.appcenter.crashes.model;

import com.microsoft.appcenter.ingestion.models.Device;
import java.util.Date;

/* loaded from: classes.dex */
public class ErrorReport {
    private Date appErrorTime;
    private Date appStartTime;
    private Device device;
    private String id;
    private String threadName;
    private Throwable throwable;

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public void setThreadName(String str) {
        this.threadName = str;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setThrowable(Throwable th) {
        this.throwable = th;
    }

    public Date getAppStartTime() {
        return this.appStartTime;
    }

    public void setAppStartTime(Date date) {
        this.appStartTime = date;
    }

    public Date getAppErrorTime() {
        return this.appErrorTime;
    }

    public void setAppErrorTime(Date date) {
        this.appErrorTime = date;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
