package com.microsoft.appcenter.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.Constants;
import com.microsoft.appcenter.Flags;
import com.microsoft.appcenter.ingestion.models.Log;
import com.microsoft.appcenter.ingestion.models.one.CommonSchemaLog;
import com.microsoft.appcenter.ingestion.models.one.PartAUtils;
import com.microsoft.appcenter.persistence.Persistence;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.UUIDUtils;
import com.microsoft.appcenter.utils.crypto.CryptoUtils;
import com.microsoft.appcenter.utils.storage.DatabaseManager;
import com.microsoft.appcenter.utils.storage.FileManager;
import com.microsoft.appcenter.utils.storage.SQLiteUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;

/* loaded from: classes.dex */
public class DatabasePersistence extends Persistence {

    @VisibleForTesting
    static final String COLUMN_DATA_TYPE = "type";

    @VisibleForTesting
    static final String COLUMN_GROUP = "persistence_group";

    @VisibleForTesting
    static final String COLUMN_LOG = "log";

    @VisibleForTesting
    static final String COLUMN_PRIORITY = "priority";

    @VisibleForTesting
    static final String COLUMN_TARGET_KEY = "target_key";

    @VisibleForTesting
    static final String COLUMN_TARGET_TOKEN = "target_token";

    @VisibleForTesting
    static final String COLUMN_TIMESTAMP = "timestamp";

    @VisibleForTesting
    static final String DATABASE = "com.microsoft.appcenter.persistence";
    private static final String GET_SORT_ORDER = "priority DESC, oid";
    private static final String INDEX_PRIORITY = "ix_logs_priority";
    private static final String PAYLOAD_FILE_EXTENSION = ".json";
    private static final String PAYLOAD_LARGE_DIRECTORY = "/appcenter/database_large_payloads";
    private static final int PAYLOAD_MAX_SIZE = 1992294;

    @VisibleForTesting
    static final ContentValues SCHEMA = getContentValues("", "", "", "", "", 0, 0L);

    @VisibleForTesting
    static final String TABLE = "logs";
    private static final int VERSION = 5;

    @VisibleForTesting
    static final int VERSION_PRIORITY_KEY = 4;

    @VisibleForTesting
    static final int VERSION_TARGET_KEY = 3;

    @VisibleForTesting
    static final int VERSION_TYPE_API_KEY = 2;
    private final Context mContext;

    @VisibleForTesting
    final DatabaseManager mDatabaseManager;
    private final File mLargePayloadDirectory;

    @VisibleForTesting
    final Set<Long> mPendingDbIdentifiers;

    @VisibleForTesting
    final Map<String, List<Long>> mPendingDbIdentifiersGroups;

    public DatabasePersistence(Context context) {
        this(context, 5, SCHEMA);
    }

    DatabasePersistence(Context context, int i, ContentValues contentValues) {
        this.mContext = context;
        this.mPendingDbIdentifiersGroups = new HashMap();
        this.mPendingDbIdentifiers = new HashSet();
        this.mDatabaseManager = new DatabaseManager(context, DATABASE, TABLE, i, contentValues, new DatabaseManager.Listener() { // from class: com.microsoft.appcenter.persistence.DatabasePersistence.1
            private void createPriorityIndex(SQLiteDatabase sQLiteDatabase) {
                sQLiteDatabase.execSQL("CREATE INDEX `ix_logs_priority` ON logs (`priority`)");
            }

            @Override // com.microsoft.appcenter.utils.storage.DatabaseManager.Listener
            public void onCreate(SQLiteDatabase sQLiteDatabase) {
                createPriorityIndex(sQLiteDatabase);
            }

            @Override // com.microsoft.appcenter.utils.storage.DatabaseManager.Listener
            public boolean onUpgrade(SQLiteDatabase sQLiteDatabase, int i2, int i3) {
                if (i2 < 2) {
                    sQLiteDatabase.execSQL("ALTER TABLE logs ADD COLUMN `target_token` TEXT");
                    sQLiteDatabase.execSQL("ALTER TABLE logs ADD COLUMN `type` TEXT");
                }
                if (i2 < 3) {
                    sQLiteDatabase.execSQL("ALTER TABLE logs ADD COLUMN `target_key` TEXT");
                }
                if (i2 < 4) {
                    sQLiteDatabase.execSQL("ALTER TABLE logs ADD COLUMN `priority` INTEGER DEFAULT 1");
                }
                sQLiteDatabase.execSQL("ALTER TABLE logs ADD COLUMN `timestamp` INTEGER DEFAULT 0");
                createPriorityIndex(sQLiteDatabase);
                return true;
            }
        });
        File file = new File(Constants.FILES_PATH + PAYLOAD_LARGE_DIRECTORY);
        this.mLargePayloadDirectory = file;
        file.mkdirs();
    }

    private static ContentValues getContentValues(@Nullable String str, @Nullable String str2, String str3, String str4, String str5, int i, Long l) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_GROUP, str);
        contentValues.put(COLUMN_LOG, str2);
        contentValues.put(COLUMN_TARGET_TOKEN, str3);
        contentValues.put("type", str4);
        contentValues.put(COLUMN_TARGET_KEY, str5);
        contentValues.put(COLUMN_PRIORITY, Integer.valueOf(i));
        contentValues.put(COLUMN_TIMESTAMP, l);
        return contentValues;
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public boolean setMaxStorageSize(long j) {
        return this.mDatabaseManager.setMaxSize(j);
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x00aa, code lost:
    
        r8 = null;
     */
    @Override // com.microsoft.appcenter.persistence.Persistence
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public long putLog(@NonNull Log log, @NonNull String str, @IntRange(from = 1, to = 2) int i) throws Persistence.PersistenceException {
        String str2;
        String str3;
        try {
            try {
                AppCenterLog.debug("AppCenter", "Storing a log to the Persistence database for log type " + log.getType() + " with flags=" + i);
                String serializeLog = getLogSerializer().serializeLog(log);
                int length = serializeLog.getBytes("UTF-8").length;
                boolean z = length >= PAYLOAD_MAX_SIZE;
                if (!(log instanceof CommonSchemaLog)) {
                    str2 = null;
                    str3 = null;
                } else {
                    if (z) {
                        throw new Persistence.PersistenceException("Log is larger than 1992294 bytes, cannot send to OneCollector.");
                    }
                    String next = log.getTransmissionTargetTokens().iterator().next();
                    str3 = PartAUtils.getTargetKey(next);
                    str2 = CryptoUtils.getInstance(this.mContext).encrypt(next);
                }
                long maxSize = this.mDatabaseManager.getMaxSize();
                if (maxSize == -1) {
                    throw new Persistence.PersistenceException("Failed to store a log to the Persistence database.");
                }
                if (!z && maxSize <= length) {
                    throw new Persistence.PersistenceException("Log is too large (" + length + " bytes) to store in database. Current maximum database size is " + maxSize + " bytes.");
                }
                String str4 = serializeLog;
                long put = this.mDatabaseManager.put(getContentValues(str, str4, str2, log.getType(), str3, Flags.getPersistenceFlag(i, false), Long.valueOf(log.getTimestamp().getTime())), COLUMN_PRIORITY);
                if (put == -1) {
                    throw new Persistence.PersistenceException("Failed to store a log to the Persistence database for log type " + log.getType() + ".");
                }
                AppCenterLog.debug("AppCenter", "Stored a log to the Persistence database for log type " + log.getType() + " with databaseId=" + put);
                if (z) {
                    AppCenterLog.debug("AppCenter", "Payload is larger than what SQLite supports, storing payload in a separate file.");
                    File largePayloadGroupDirectory = getLargePayloadGroupDirectory(str);
                    largePayloadGroupDirectory.mkdir();
                    File largePayloadFile = getLargePayloadFile(largePayloadGroupDirectory, put);
                    try {
                        FileManager.write(largePayloadFile, serializeLog);
                        AppCenterLog.debug("AppCenter", "Payload written to " + largePayloadFile);
                    } catch (IOException e) {
                        this.mDatabaseManager.delete(put);
                        throw e;
                    }
                }
                return put;
            } catch (JSONException e2) {
                throw new Persistence.PersistenceException("Cannot convert to JSON string.", e2);
            }
        } catch (IOException e3) {
            throw new Persistence.PersistenceException("Cannot save large payload in a file.", e3);
        }
    }

    @NonNull
    @VisibleForTesting
    File getLargePayloadGroupDirectory(String str) {
        return new File(this.mLargePayloadDirectory, str);
    }

    @NonNull
    @VisibleForTesting
    File getLargePayloadFile(File file, long j) {
        return new File(file, j + ".json");
    }

    private void deleteLog(File file, long j) {
        getLargePayloadFile(file, j).delete();
        this.mDatabaseManager.delete(j);
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public void deleteLogs(@NonNull String str, @NonNull String str2) {
        AppCenterLog.debug("AppCenter", "Deleting logs from the Persistence database for " + str + " with " + str2);
        AppCenterLog.debug("AppCenter", "The IDs for deleting log(s) is/are:");
        List<Long> remove = this.mPendingDbIdentifiersGroups.remove(str + str2);
        File largePayloadGroupDirectory = getLargePayloadGroupDirectory(str);
        if (remove != null) {
            for (Long l : remove) {
                AppCenterLog.debug("AppCenter", "\t" + l);
                deleteLog(largePayloadGroupDirectory, l.longValue());
                this.mPendingDbIdentifiers.remove(l);
            }
        }
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public void deleteLogs(String str) {
        AppCenterLog.debug("AppCenter", "Deleting all logs from the Persistence database for " + str);
        File largePayloadGroupDirectory = getLargePayloadGroupDirectory(str);
        File[] listFiles = largePayloadGroupDirectory.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                file.delete();
            }
        }
        largePayloadGroupDirectory.delete();
        AppCenterLog.debug("AppCenter", "Deleted " + this.mDatabaseManager.delete(COLUMN_GROUP, str) + " logs.");
        Iterator<String> it = this.mPendingDbIdentifiersGroups.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(str)) {
                it.remove();
            }
        }
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public int countLogs(@NonNull String str) {
        return countLogs("persistence_group = ?", str);
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public int countLogs(@NonNull Date date) {
        return countLogs("timestamp < ?", String.valueOf(date.getTime()));
    }

    private int countLogs(String str, String... strArr) {
        SQLiteQueryBuilder newSQLiteQueryBuilder = SQLiteUtils.newSQLiteQueryBuilder();
        newSQLiteQueryBuilder.appendWhere(str);
        int i = 0;
        try {
            Cursor cursor = this.mDatabaseManager.getCursor(newSQLiteQueryBuilder, new String[]{"COUNT(*)"}, strArr, null);
            try {
                cursor.moveToNext();
                i = cursor.getInt(0);
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to get logs count: ", e);
        }
        return i;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.microsoft.appcenter.persistence.Persistence
    @Nullable
    public String getLogs(@NonNull String str, @NonNull Collection<String> collection, @IntRange(from = 0) int i, @NonNull List<Log> list, @Nullable Date date, @Nullable Date date2) {
        int i2;
        Cursor cursor;
        Cursor cursor2;
        AppCenterLog.debug("AppCenter", "Trying to get " + i + " logs from the Persistence database for " + str);
        SQLiteQueryBuilder newSQLiteQueryBuilder = SQLiteUtils.newSQLiteQueryBuilder();
        newSQLiteQueryBuilder.appendWhere("persistence_group = ?");
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        if (!collection.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i3 = 0; i3 < collection.size(); i3++) {
                sb.append("?,");
            }
            sb.deleteCharAt(sb.length() - 1);
            newSQLiteQueryBuilder.appendWhere(" AND ");
            newSQLiteQueryBuilder.appendWhere("target_key NOT IN (" + sb.toString() + ")");
            arrayList.addAll(collection);
        }
        if (date != null) {
            newSQLiteQueryBuilder.appendWhere(" AND ");
            newSQLiteQueryBuilder.appendWhere("timestamp >= ?");
            arrayList.add(String.valueOf(date.getTime()));
        }
        if (date2 != null) {
            newSQLiteQueryBuilder.appendWhere(" AND ");
            newSQLiteQueryBuilder.appendWhere("timestamp < ?");
            arrayList.add(String.valueOf(date2.getTime()));
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        ArrayList arrayList2 = new ArrayList();
        File largePayloadGroupDirectory = getLargePayloadGroupDirectory(str);
        String[] strArr = (String[]) arrayList.toArray(new String[0]);
        try {
            cursor = this.mDatabaseManager.getCursor(newSQLiteQueryBuilder, null, strArr, GET_SORT_ORDER);
            i2 = 0;
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to get logs: ", e);
            i2 = 0;
            cursor = null;
        }
        while (cursor != null) {
            ContentValues nextValues = this.mDatabaseManager.nextValues(cursor);
            if (nextValues == null || i2 >= i) {
                break;
            }
            Long asLong = nextValues.getAsLong(DatabaseManager.PRIMARY_KEY);
            if (asLong == null) {
                AppCenterLog.error("AppCenter", "Empty database record, probably content was larger than 2MB, need to delete as it's now corrupted.");
                Iterator<Long> it = getLogsIds(newSQLiteQueryBuilder, strArr).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        cursor2 = cursor;
                        break;
                    }
                    Long next = it.next();
                    if (!this.mPendingDbIdentifiers.contains(next) && !linkedHashMap.containsKey(next)) {
                        cursor2 = cursor;
                        deleteLog(largePayloadGroupDirectory, next.longValue());
                        AppCenterLog.error("AppCenter", "Empty database corrupted empty record deleted, id=" + next);
                        break;
                    }
                    cursor = cursor;
                }
            } else {
                cursor2 = cursor;
                if (this.mPendingDbIdentifiers.contains(asLong)) {
                    continue;
                } else {
                    try {
                        String asString = nextValues.getAsString(COLUMN_LOG);
                        if (asString == null) {
                            File largePayloadFile = getLargePayloadFile(largePayloadGroupDirectory, asLong.longValue());
                            AppCenterLog.debug("AppCenter", "Read payload file " + largePayloadFile);
                            asString = FileManager.read(largePayloadFile);
                            if (asString == null) {
                                throw new JSONException("Log payload is null and not stored as a file.");
                            }
                        }
                        Log deserializeLog = getLogSerializer().deserializeLog(asString, nextValues.getAsString("type"));
                        String asString2 = nextValues.getAsString(COLUMN_TARGET_TOKEN);
                        if (asString2 != null) {
                            deserializeLog.addTransmissionTarget(CryptoUtils.getInstance(this.mContext).decrypt(asString2, false).getDecryptedData());
                        }
                        linkedHashMap.put(asLong, deserializeLog);
                        i2++;
                    } catch (JSONException e2) {
                        AppCenterLog.error("AppCenter", "Cannot deserialize a log in the database", e2);
                        arrayList2.add(asLong);
                    }
                }
            }
            cursor = cursor2;
        }
        Cursor cursor3 = cursor;
        if (cursor3 != null) {
            try {
                cursor3.close();
            } catch (RuntimeException unused) {
            }
        }
        if (arrayList2.size() > 0) {
            Iterator it2 = arrayList2.iterator();
            while (it2.hasNext()) {
                deleteLog(largePayloadGroupDirectory, ((Long) it2.next()).longValue());
            }
            AppCenterLog.warn("AppCenter", "Deleted logs that cannot be deserialized");
        }
        if (linkedHashMap.size() <= 0) {
            AppCenterLog.debug("AppCenter", "No logs found in the Persistence database at the moment");
            return null;
        }
        String uuid = UUIDUtils.randomUUID().toString();
        AppCenterLog.debug("AppCenter", "Returning " + linkedHashMap.size() + " log(s) with an ID, " + uuid);
        AppCenterLog.debug("AppCenter", "The SID/ID pairs for returning log(s) is/are:");
        ArrayList arrayList3 = new ArrayList();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            Long l = (Long) entry.getKey();
            this.mPendingDbIdentifiers.add(l);
            arrayList3.add(l);
            list.add(entry.getValue());
            AppCenterLog.debug("AppCenter", "\t" + ((Log) entry.getValue()).getSid() + " / " + l);
        }
        this.mPendingDbIdentifiersGroups.put(str + uuid, arrayList3);
        return uuid;
    }

    @Override // com.microsoft.appcenter.persistence.Persistence
    public void clearPendingLogState() {
        this.mPendingDbIdentifiers.clear();
        this.mPendingDbIdentifiersGroups.clear();
        AppCenterLog.debug("AppCenter", "Cleared pending log states");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        this.mDatabaseManager.close();
    }

    private List<Long> getLogsIds(SQLiteQueryBuilder sQLiteQueryBuilder, String[] strArr) {
        ArrayList arrayList = new ArrayList();
        try {
            Cursor cursor = this.mDatabaseManager.getCursor(sQLiteQueryBuilder, DatabaseManager.SELECT_PRIMARY_KEY, strArr, null);
            while (cursor.moveToNext()) {
                try {
                    arrayList.add(this.mDatabaseManager.buildValues(cursor).getAsLong(DatabaseManager.PRIMARY_KEY));
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            cursor.close();
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to get corrupted ids: ", e);
        }
        return arrayList;
    }
}
