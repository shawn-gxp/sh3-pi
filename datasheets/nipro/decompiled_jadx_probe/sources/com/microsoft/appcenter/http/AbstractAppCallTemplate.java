package com.microsoft.appcenter.http;

import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.http.HttpClient;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public abstract class AbstractAppCallTemplate implements HttpClient.CallTemplate {
    @Override // com.microsoft.appcenter.http.HttpClient.CallTemplate
    public void onBeforeCalling(URL url, Map<String, String> map) {
        if (AppCenterLog.getLogLevel() <= 2) {
            AppCenterLog.verbose("AppCenter", "Calling " + url + "...");
            HashMap hashMap = new HashMap(map);
            String str = (String) hashMap.get(Constants.APP_SECRET);
            if (str != null) {
                hashMap.put(Constants.APP_SECRET, HttpUtils.hideSecret(str));
            }
            String str2 = (String) hashMap.get(Constants.AUTHORIZATION_HEADER);
            if (str2 != null) {
                hashMap.put(Constants.AUTHORIZATION_HEADER, HttpUtils.hideAuthToken(str2));
            }
            AppCenterLog.verbose("AppCenter", "Headers: " + hashMap);
        }
    }
}
