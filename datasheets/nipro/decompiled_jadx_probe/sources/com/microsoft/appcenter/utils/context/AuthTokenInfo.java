package com.microsoft.appcenter.utils.context;

import java.util.Calendar;
import java.util.Date;

/* loaded from: classes.dex */
public class AuthTokenInfo {
    private static final int EXPIRATION_OFFSET_TO_REFRESH_SEC = 600;
    private final String mAuthToken;
    private final Date mEndTime;
    private final Date mStartTime;

    public AuthTokenInfo() {
        this(null, null, null);
    }

    public AuthTokenInfo(String str, Date date, Date date2) {
        this.mAuthToken = str;
        this.mStartTime = date;
        this.mEndTime = date2;
    }

    boolean isAboutToExpire() {
        if (this.mEndTime == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(13, EXPIRATION_OFFSET_TO_REFRESH_SEC);
        return calendar.getTime().after(this.mEndTime);
    }

    public String getAuthToken() {
        return this.mAuthToken;
    }

    public Date getStartTime() {
        return this.mStartTime;
    }

    public Date getEndTime() {
        return this.mEndTime;
    }
}
