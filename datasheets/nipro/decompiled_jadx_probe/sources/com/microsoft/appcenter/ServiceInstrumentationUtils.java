package com.microsoft.appcenter;

import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.InstrumentationRegistryHelper;

/* loaded from: classes.dex */
class ServiceInstrumentationUtils {

    @VisibleForTesting
    static final String DISABLE_ALL_SERVICES = "All";

    @VisibleForTesting
    static final String DISABLE_SERVICES = "APP_CENTER_DISABLE";

    ServiceInstrumentationUtils() {
    }

    static boolean isServiceDisabledByInstrumentation(String str) {
        try {
            String string = InstrumentationRegistryHelper.getArguments().getString(DISABLE_SERVICES);
            if (string == null) {
                return false;
            }
            for (String str2 : string.split(",")) {
                String trim = str2.trim();
                if (trim.equals(DISABLE_ALL_SERVICES) || trim.equals(str)) {
                    return true;
                }
            }
            return false;
        } catch (IllegalStateException | LinkageError unused) {
            AppCenterLog.debug("AppCenter", "Cannot read instrumentation variables in a non-test environment.");
            return false;
        }
    }
}
