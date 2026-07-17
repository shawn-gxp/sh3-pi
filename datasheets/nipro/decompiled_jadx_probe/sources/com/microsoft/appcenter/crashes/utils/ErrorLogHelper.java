package com.microsoft.appcenter.crashes.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.Exception;
import com.microsoft.appcenter.crashes.ingestion.models.ManagedErrorLog;
import com.microsoft.appcenter.crashes.ingestion.models.StackFrame;
import com.microsoft.appcenter.crashes.ingestion.models.Thread;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.DeviceInfoHelper;
import com.microsoft.appcenter.utils.UUIDUtils;
import com.microsoft.appcenter.utils.context.UserIdContext;
import com.microsoft.appcenter.utils.storage.FileManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* loaded from: classes.dex */
public class ErrorLogHelper {

    @VisibleForTesting
    static final int CAUSE_LIMIT = 16;
    private static final int CAUSE_LIMIT_HALF = 8;

    @VisibleForTesting
    static final String ERROR_DIRECTORY = "error";
    public static final String ERROR_LOG_FILE_EXTENSION = ".json";

    @VisibleForTesting
    public static final int FRAME_LIMIT = 256;
    private static final int FRAME_LIMIT_HALF = 128;
    private static final int MAX_PROPERTY_COUNT = 20;
    public static final int MAX_PROPERTY_ITEM_LENGTH = 125;
    private static final String MINIDUMP_DIRECTORY = "minidump";
    private static final String NEW_MINIDUMP_DIRECTORY = "new";
    private static final String PENDING_MINIDUMP_DIRECTORY = "pending";
    public static final String THROWABLE_FILE_EXTENSION = ".throwable";
    private static File sErrorLogDirectory;
    private static File sNewMinidumpDirectory;
    private static File sPendingMinidumpDirectory;

    @NonNull
    public static ManagedErrorLog createErrorLog(@NonNull Context context, @NonNull Thread thread, @NonNull Throwable th, @NonNull Map<Thread, StackTraceElement[]> map, long j) {
        return createErrorLog(context, thread, getModelExceptionFromThrowable(th), map, j, true);
    }

    @NonNull
    public static ManagedErrorLog createErrorLog(@NonNull Context context, @NonNull Thread thread, @NonNull Exception exception, @NonNull Map<Thread, StackTraceElement[]> map, long j, boolean z) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses;
        ManagedErrorLog managedErrorLog = new ManagedErrorLog();
        managedErrorLog.setId(UUIDUtils.randomUUID());
        managedErrorLog.setTimestamp(new Date());
        managedErrorLog.setUserId(UserIdContext.getInstance().getUserId());
        try {
            managedErrorLog.setDevice(DeviceInfoHelper.getDeviceInfo(context));
        } catch (DeviceInfoHelper.DeviceInfoException e) {
            AppCenterLog.error(Crashes.LOG_TAG, "Could not attach device properties snapshot to error log, will attach at sending time", e);
        }
        managedErrorLog.setProcessId(Integer.valueOf(Process.myPid()));
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null && (runningAppProcesses = activityManager.getRunningAppProcesses()) != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.pid == Process.myPid()) {
                    managedErrorLog.setProcessName(runningAppProcessInfo.processName);
                }
            }
        }
        if (managedErrorLog.getProcessName() == null) {
            managedErrorLog.setProcessName("");
        }
        managedErrorLog.setArchitecture(getArchitecture());
        managedErrorLog.setErrorThreadId(Long.valueOf(thread.getId()));
        managedErrorLog.setErrorThreadName(thread.getName());
        managedErrorLog.setFatal(Boolean.valueOf(z));
        managedErrorLog.setAppLaunchTimestamp(new Date(j));
        managedErrorLog.setException(exception);
        ArrayList arrayList = new ArrayList(map.size());
        for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
            Thread thread2 = new Thread();
            thread2.setId(entry.getKey().getId());
            thread2.setName(entry.getKey().getName());
            thread2.setFrames(getModelFramesFromStackTrace(entry.getValue()));
            arrayList.add(thread2);
        }
        managedErrorLog.setThreads(arrayList);
        return managedErrorLog;
    }

    @TargetApi(21)
    private static String getArchitecture() {
        if (Build.VERSION.SDK_INT >= 21) {
            return Build.SUPPORTED_ABIS[0];
        }
        return Build.CPU_ABI;
    }

    @NonNull
    public static synchronized File getErrorStorageDirectory() {
        File file;
        synchronized (ErrorLogHelper.class) {
            if (sErrorLogDirectory == null) {
                File file2 = new File(Constants.FILES_PATH, "error");
                sErrorLogDirectory = file2;
                FileManager.mkdir(file2.getAbsolutePath());
            }
            file = sErrorLogDirectory;
        }
        return file;
    }

    @NonNull
    public static synchronized File getNewMinidumpDirectory() {
        File file;
        synchronized (ErrorLogHelper.class) {
            if (sNewMinidumpDirectory == null) {
                File file2 = new File(new File(getErrorStorageDirectory().getAbsolutePath(), MINIDUMP_DIRECTORY), NEW_MINIDUMP_DIRECTORY);
                sNewMinidumpDirectory = file2;
                FileManager.mkdir(file2.getPath());
            }
            file = sNewMinidumpDirectory;
        }
        return file;
    }

    @NonNull
    public static synchronized File getPendingMinidumpDirectory() {
        File file;
        synchronized (ErrorLogHelper.class) {
            if (sPendingMinidumpDirectory == null) {
                File file2 = new File(new File(getErrorStorageDirectory().getAbsolutePath(), MINIDUMP_DIRECTORY), PENDING_MINIDUMP_DIRECTORY);
                sPendingMinidumpDirectory = file2;
                FileManager.mkdir(file2.getPath());
            }
            file = sPendingMinidumpDirectory;
        }
        return file;
    }

    @NonNull
    public static File[] getStoredErrorLogFiles() {
        File[] listFiles = getErrorStorageDirectory().listFiles(new FilenameFilter() { // from class: com.microsoft.appcenter.crashes.utils.ErrorLogHelper.1
            @Override // java.io.FilenameFilter
            public boolean accept(File file, String str) {
                return str.endsWith(ErrorLogHelper.ERROR_LOG_FILE_EXTENSION);
            }
        });
        return listFiles != null ? listFiles : new File[0];
    }

    @NonNull
    public static File[] getNewMinidumpFiles() {
        File[] listFiles = getNewMinidumpDirectory().listFiles();
        return listFiles != null ? listFiles : new File[0];
    }

    @Nullable
    public static File getLastErrorLogFile() {
        return FileManager.lastModifiedFile(getErrorStorageDirectory(), new FilenameFilter() { // from class: com.microsoft.appcenter.crashes.utils.ErrorLogHelper.2
            @Override // java.io.FilenameFilter
            public boolean accept(File file, String str) {
                return str.endsWith(ErrorLogHelper.ERROR_LOG_FILE_EXTENSION);
            }
        });
    }

    @Nullable
    public static File getStoredThrowableFile(@NonNull UUID uuid) {
        return getStoredFile(uuid, THROWABLE_FILE_EXTENSION);
    }

    public static void removeStoredThrowableFile(@NonNull UUID uuid) {
        File storedThrowableFile = getStoredThrowableFile(uuid);
        if (storedThrowableFile != null) {
            AppCenterLog.info(Crashes.LOG_TAG, "Deleting throwable file " + storedThrowableFile.getName());
            FileManager.delete(storedThrowableFile);
        }
    }

    @Nullable
    static File getStoredErrorLogFile(@NonNull UUID uuid) {
        return getStoredFile(uuid, ERROR_LOG_FILE_EXTENSION);
    }

    public static void removeStoredErrorLogFile(@NonNull UUID uuid) {
        File storedErrorLogFile = getStoredErrorLogFile(uuid);
        if (storedErrorLogFile != null) {
            AppCenterLog.info(Crashes.LOG_TAG, "Deleting error log file " + storedErrorLogFile.getName());
            FileManager.delete(storedErrorLogFile);
        }
    }

    @NonNull
    public static ErrorReport getErrorReportFromErrorLog(@NonNull ManagedErrorLog managedErrorLog, Throwable th) {
        ErrorReport errorReport = new ErrorReport();
        errorReport.setId(managedErrorLog.getId().toString());
        errorReport.setThreadName(managedErrorLog.getErrorThreadName());
        errorReport.setThrowable(th);
        errorReport.setAppStartTime(managedErrorLog.getAppLaunchTimestamp());
        errorReport.setAppErrorTime(managedErrorLog.getTimestamp());
        errorReport.setDevice(managedErrorLog.getDevice());
        return errorReport;
    }

    @VisibleForTesting
    static void setErrorLogDirectory(File file) {
        sErrorLogDirectory = file;
    }

    @Nullable
    private static File getStoredFile(@NonNull final UUID uuid, @NonNull final String str) {
        File[] listFiles = getErrorStorageDirectory().listFiles(new FilenameFilter() { // from class: com.microsoft.appcenter.crashes.utils.ErrorLogHelper.3
            @Override // java.io.FilenameFilter
            public boolean accept(File file, String str2) {
                return str2.startsWith(uuid.toString()) && str2.endsWith(str);
            }
        });
        if (listFiles == null || listFiles.length <= 0) {
            return null;
        }
        return listFiles[0];
    }

    @NonNull
    public static Exception getModelExceptionFromThrowable(@NonNull Throwable th) {
        LinkedList<Throwable> linkedList = new LinkedList();
        while (th != null) {
            linkedList.add(th);
            th = th.getCause();
        }
        if (linkedList.size() > 16) {
            AppCenterLog.warn(Crashes.LOG_TAG, "Crash causes truncated from " + linkedList.size() + " to 16 causes.");
            linkedList.subList(8, linkedList.size() - 8).clear();
        }
        Exception exception = null;
        Exception exception2 = null;
        for (Throwable th2 : linkedList) {
            Exception exception3 = new Exception();
            exception3.setType(th2.getClass().getName());
            exception3.setMessage(th2.getMessage());
            exception3.setFrames(getModelFramesFromStackTrace(th2));
            if (exception == null) {
                exception = exception3;
            } else {
                exception2.setInnerExceptions(Collections.singletonList(exception3));
            }
            exception2 = exception3;
        }
        return exception;
    }

    @NonNull
    private static List<StackFrame> getModelFramesFromStackTrace(@NonNull Throwable th) {
        StackTraceElement[] stackTrace = th.getStackTrace();
        if (stackTrace.length > 256) {
            StackTraceElement[] stackTraceElementArr = new StackTraceElement[256];
            System.arraycopy(stackTrace, 0, stackTraceElementArr, 0, 128);
            System.arraycopy(stackTrace, stackTrace.length - 128, stackTraceElementArr, 128, 128);
            th.setStackTrace(stackTraceElementArr);
            AppCenterLog.warn(Crashes.LOG_TAG, "Crash frames truncated from " + stackTrace.length + " to 256 frames.");
            stackTrace = stackTraceElementArr;
        }
        return getModelFramesFromStackTrace(stackTrace);
    }

    @NonNull
    private static List<StackFrame> getModelFramesFromStackTrace(@NonNull StackTraceElement[] stackTraceElementArr) {
        ArrayList arrayList = new ArrayList();
        for (StackTraceElement stackTraceElement : stackTraceElementArr) {
            arrayList.add(getModelStackFrame(stackTraceElement));
        }
        return arrayList;
    }

    @NonNull
    private static StackFrame getModelStackFrame(StackTraceElement stackTraceElement) {
        StackFrame stackFrame = new StackFrame();
        stackFrame.setClassName(stackTraceElement.getClassName());
        stackFrame.setMethodName(stackTraceElement.getMethodName());
        stackFrame.setLineNumber(Integer.valueOf(stackTraceElement.getLineNumber()));
        stackFrame.setFileName(stackTraceElement.getFileName());
        return stackFrame;
    }

    public static Map<String, String> validateProperties(Map<String, String> map, String str) {
        if (map == null) {
            return null;
        }
        HashMap hashMap = new HashMap();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<String, String> next = it.next();
            String key = next.getKey();
            String value = next.getValue();
            if (hashMap.size() >= 20) {
                AppCenterLog.warn(Crashes.LOG_TAG, String.format("%s : properties cannot contain more than %s items. Skipping other properties.", str, 20));
                break;
            }
            if (key == null || key.isEmpty()) {
                AppCenterLog.warn(Crashes.LOG_TAG, String.format("%s : a property key cannot be null or empty. Property will be skipped.", str));
            } else if (value == null) {
                AppCenterLog.warn(Crashes.LOG_TAG, String.format("%s : property '%s' : property value cannot be null. Property '%s' will be skipped.", str, key, key));
            } else {
                if (key.length() > 125) {
                    AppCenterLog.warn(Crashes.LOG_TAG, String.format("%s : property '%s' : property key length cannot be longer than %s characters. Property key will be truncated.", str, key, 125));
                    key = key.substring(0, 125);
                }
                if (value.length() > 125) {
                    AppCenterLog.warn(Crashes.LOG_TAG, String.format("%s : property '%s' : property value cannot be longer than %s characters. Property value will be truncated.", str, key, 125));
                    value = value.substring(0, 125);
                }
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }
}
