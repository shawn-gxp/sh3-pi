package com.microsoft.appcenter.crashes;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.ingestion.models.Exception;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.microsoft.appcenter.crashes.utils.ErrorLogHelper;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.async.AppCenterFuture;
import com.microsoft.appcenter.utils.storage.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* loaded from: classes.dex */
public class WrapperSdkExceptionManager {
    private static final String DATA_FILE_EXTENSION = ".dat";

    @VisibleForTesting
    static final Map<String, byte[]> sWrapperExceptionDataContainer = new HashMap();

    @VisibleForTesting
    WrapperSdkExceptionManager() {
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static UUID saveWrapperException(Thread thread, Throwable th, Exception exception, byte[] bArr) {
        try {
            UUID saveUncaughtException = Crashes.getInstance().saveUncaughtException(thread, th, exception);
            if (saveUncaughtException != null && bArr != 0) {
                sWrapperExceptionDataContainer.put(saveUncaughtException.toString(), bArr);
                File file = getFile(saveUncaughtException);
                FileManager.writeObject(file, bArr);
                AppCenterLog.debug(Crashes.LOG_TAG, "Saved raw wrapper exception data into " + file);
            }
            return saveUncaughtException;
        } catch (Exception e) {
            AppCenterLog.error(Crashes.LOG_TAG, "Failed to save wrapper exception data to file", e);
            return null;
        }
    }

    public static void deleteWrapperExceptionData(UUID uuid) {
        if (uuid == null) {
            AppCenterLog.error(Crashes.LOG_TAG, "Failed to delete wrapper exception data: null errorId");
            return;
        }
        File file = getFile(uuid);
        if (file.exists()) {
            if (loadWrapperExceptionData(uuid) == null) {
                AppCenterLog.error(Crashes.LOG_TAG, "Failed to delete wrapper exception data: data not found");
            }
            FileManager.delete(file);
        }
    }

    public static byte[] loadWrapperExceptionData(UUID uuid) {
        if (uuid == null) {
            AppCenterLog.error(Crashes.LOG_TAG, "Failed to load wrapper exception data: null errorId");
            return null;
        }
        byte[] bArr = sWrapperExceptionDataContainer.get(uuid.toString());
        if (bArr != null) {
            return bArr;
        }
        File file = getFile(uuid);
        if (file.exists()) {
            try {
                byte[] bArr2 = (byte[]) FileManager.readObject(file);
                if (bArr2 != null) {
                    sWrapperExceptionDataContainer.put(uuid.toString(), bArr2);
                }
                return bArr2;
            } catch (IOException | ClassNotFoundException e) {
                AppCenterLog.error(Crashes.LOG_TAG, "Cannot access wrapper exception data file " + file.getName(), e);
            }
        }
        return null;
    }

    private static File getFile(@NonNull UUID uuid) {
        return new File(ErrorLogHelper.getErrorStorageDirectory(), uuid.toString() + DATA_FILE_EXTENSION);
    }

    public static void trackException(Exception exception) {
        trackException(exception, null);
    }

    public static void trackException(Exception exception, Map<String, String> map) {
        Crashes.getInstance().queueException(exception, ErrorLogHelper.validateProperties(map, "HandledError"));
    }

    public static void setAutomaticProcessing(boolean z) {
        Crashes.getInstance().setAutomaticProcessing(z);
    }

    public static AppCenterFuture<Collection<ErrorReport>> getUnprocessedErrorReports() {
        return Crashes.getInstance().getUnprocessedErrorReports();
    }

    public static AppCenterFuture<Boolean> sendCrashReportsOrAwaitUserConfirmation(Collection<String> collection) {
        return Crashes.getInstance().sendCrashReportsOrAwaitUserConfirmation(collection);
    }

    public static void sendErrorAttachments(String str, Iterable<ErrorAttachmentLog> iterable) {
        Crashes.getInstance().sendErrorAttachments(str, iterable);
    }
}
