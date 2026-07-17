package com.microsoft.appcenter.utils.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.appcenter.utils.AppCenterLog;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/* loaded from: classes.dex */
public class DatabaseManager implements Closeable {
    public static final String PRIMARY_KEY = "oid";
    public static final String[] SELECT_PRIMARY_KEY = {PRIMARY_KEY};
    private final Context mContext;
    private final String mDatabase;
    private final String mDefaultTable;
    private final Listener mListener;
    private SQLiteOpenHelper mSQLiteOpenHelper;
    private final ContentValues mSchema;

    public static class DefaultListener implements Listener {
        @Override // com.microsoft.appcenter.utils.storage.DatabaseManager.Listener
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
        }

        @Override // com.microsoft.appcenter.utils.storage.DatabaseManager.Listener
        public boolean onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            return false;
        }
    }

    public interface Listener {
        void onCreate(SQLiteDatabase sQLiteDatabase);

        boolean onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);
    }

    public DatabaseManager(Context context, String str, String str2, int i, ContentValues contentValues, Listener listener) {
        this(context, str, str2, i, contentValues, listener, null);
    }

    DatabaseManager(Context context, String str, String str2, int i, ContentValues contentValues, Listener listener, final String[] strArr) {
        this.mContext = context;
        this.mDatabase = str;
        this.mDefaultTable = str2;
        this.mSchema = contentValues;
        this.mListener = listener;
        this.mSQLiteOpenHelper = new SQLiteOpenHelper(context, str, null, i) { // from class: com.microsoft.appcenter.utils.storage.DatabaseManager.1
            @Override // android.database.sqlite.SQLiteOpenHelper
            public void onCreate(SQLiteDatabase sQLiteDatabase) {
                DatabaseManager databaseManager = DatabaseManager.this;
                databaseManager.createTable(sQLiteDatabase, databaseManager.mDefaultTable, DatabaseManager.this.mSchema, strArr);
                DatabaseManager.this.mListener.onCreate(sQLiteDatabase);
            }

            @Override // android.database.sqlite.SQLiteOpenHelper
            public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i2, int i3) {
                if (DatabaseManager.this.mListener.onUpgrade(sQLiteDatabase, i2, i3)) {
                    return;
                }
                DatabaseManager databaseManager = DatabaseManager.this;
                databaseManager.dropTable(sQLiteDatabase, databaseManager.mDefaultTable);
                onCreate(sQLiteDatabase);
            }
        };
    }

    private static ContentValues buildValues(Cursor cursor, ContentValues contentValues) {
        ContentValues contentValues2 = new ContentValues();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (!cursor.isNull(i)) {
                String columnName = cursor.getColumnName(i);
                if (columnName.equals(PRIMARY_KEY)) {
                    contentValues2.put(columnName, Long.valueOf(cursor.getLong(i)));
                } else {
                    Object obj = contentValues.get(columnName);
                    if (obj instanceof byte[]) {
                        contentValues2.put(columnName, cursor.getBlob(i));
                    } else if (obj instanceof Double) {
                        contentValues2.put(columnName, Double.valueOf(cursor.getDouble(i)));
                    } else if (obj instanceof Float) {
                        contentValues2.put(columnName, Float.valueOf(cursor.getFloat(i)));
                    } else if (obj instanceof Integer) {
                        contentValues2.put(columnName, Integer.valueOf(cursor.getInt(i)));
                    } else if (obj instanceof Long) {
                        contentValues2.put(columnName, Long.valueOf(cursor.getLong(i)));
                    } else if (obj instanceof Short) {
                        contentValues2.put(columnName, Short.valueOf(cursor.getShort(i)));
                    } else if (obj instanceof Boolean) {
                        contentValues2.put(columnName, Boolean.valueOf(cursor.getInt(i) == 1));
                    } else {
                        contentValues2.put(columnName, cursor.getString(i));
                    }
                }
            }
        }
        return contentValues2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dropTable(@NonNull SQLiteDatabase sQLiteDatabase, @NonNull String str) {
        sQLiteDatabase.execSQL(String.format("DROP TABLE `%s`", str));
    }

    public void resetDatabase() {
        close();
        this.mContext.deleteDatabase(this.mDatabase);
        getDatabase();
    }

    public void createTable(@NonNull String str, @NonNull ContentValues contentValues, String[] strArr) {
        createTable(getDatabase(), str, contentValues, strArr);
    }

    public ContentValues buildValues(Cursor cursor) {
        return buildValues(cursor, this.mSchema);
    }

    @Nullable
    public ContentValues nextValues(Cursor cursor) {
        try {
            if (cursor.moveToNext()) {
                return buildValues(cursor);
            }
            return null;
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to get next cursor value: ", e);
            return null;
        }
    }

    public long replace(@NonNull String str, @NonNull ContentValues contentValues, String... strArr) {
        SQLiteQueryBuilder newSQLiteQueryBuilder = SQLiteUtils.newSQLiteQueryBuilder();
        ArrayList arrayList = new ArrayList();
        try {
            ArrayList arrayList2 = new ArrayList();
            for (String str2 : strArr) {
                arrayList2.add(str2 + " = ?");
                arrayList.add(contentValues.getAsString(str2));
            }
            newSQLiteQueryBuilder.appendWhere(TextUtils.join(" AND ", arrayList2));
            if (arrayList.size() > 0) {
                Cursor cursor = getCursor(str, newSQLiteQueryBuilder, null, (String[]) arrayList.toArray(new String[0]), null);
                try {
                    ContentValues nextValues = nextValues(cursor);
                    if (nextValues != null && !cursor.moveToNext()) {
                        contentValues.put(PRIMARY_KEY, nextValues.getAsInteger(PRIMARY_KEY));
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                    throw th;
                }
            }
            return getDatabase().replace(str, null, contentValues);
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", String.format("Failed to replace values (%s) from database %s.", contentValues.toString(), this.mDatabase), e);
            return -1L;
        }
    }

    public long put(@NonNull ContentValues contentValues, @NonNull String str) {
        Long l = null;
        Cursor cursor = null;
        while (l == null) {
            try {
                try {
                    l = Long.valueOf(getDatabase().insertOrThrow(this.mDefaultTable, null, contentValues));
                } catch (RuntimeException e) {
                    l = -1L;
                    AppCenterLog.error("AppCenter", String.format("Failed to insert values (%s) to database %s.", contentValues.toString(), this.mDatabase), e);
                }
            } catch (SQLiteFullException e2) {
                AppCenterLog.debug("AppCenter", "Storage is full, trying to delete the oldest log that has the lowest priority which is lower or equal priority than the new log");
                if (cursor == null) {
                    String asString = contentValues.getAsString(str);
                    SQLiteQueryBuilder newSQLiteQueryBuilder = SQLiteUtils.newSQLiteQueryBuilder();
                    newSQLiteQueryBuilder.appendWhere(str + " <= ?");
                    cursor = getCursor(newSQLiteQueryBuilder, SELECT_PRIMARY_KEY, new String[]{asString}, str + " , " + PRIMARY_KEY);
                }
                if (cursor.moveToNext()) {
                    long j = cursor.getLong(0);
                    delete(j);
                    AppCenterLog.debug("AppCenter", "Deleted log id=" + j);
                } else {
                    throw e2;
                }
            }
        }
        if (cursor != null) {
            try {
                cursor.close();
            } catch (RuntimeException unused) {
            }
        }
        return l.longValue();
    }

    public void delete(@IntRange(from = 0) long j) {
        delete(this.mDefaultTable, j);
    }

    private void delete(@NonNull String str, @IntRange(from = 0) long j) {
        delete(str, PRIMARY_KEY, Long.valueOf(j));
    }

    public int delete(@NonNull String str, String str2, String[] strArr) {
        try {
            return getDatabase().delete(str, str2, strArr);
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", String.format("Failed to delete values that match condition=\"%s\" and values=\"%s\" from database %s.", str2, Arrays.toString(strArr), this.mDatabase), e);
            return 0;
        }
    }

    public int delete(@Nullable String str, @Nullable Object obj) {
        return delete(this.mDefaultTable, str, obj);
    }

    public int delete(@NonNull String str, @Nullable String str2, @Nullable Object obj) {
        return delete(str, str2 + " = ?", new String[]{String.valueOf(obj)});
    }

    public void clear() {
        try {
            getDatabase().delete(this.mDefaultTable, null, null);
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to clear the table.", e);
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        try {
            this.mSQLiteOpenHelper.close();
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to close the database.", e);
        }
    }

    public final long getRowCount() {
        try {
            return DatabaseUtils.queryNumEntries(getDatabase(), this.mDefaultTable);
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Failed to get row count of database.", e);
            return -1L;
        }
    }

    public Cursor getCursor(@Nullable SQLiteQueryBuilder sQLiteQueryBuilder, String[] strArr, @Nullable String[] strArr2, @Nullable String str) throws RuntimeException {
        return getCursor(this.mDefaultTable, sQLiteQueryBuilder, strArr, strArr2, str);
    }

    public Cursor getCursor(@NonNull String str, @Nullable SQLiteQueryBuilder sQLiteQueryBuilder, String[] strArr, @Nullable String[] strArr2, @Nullable String str2) throws RuntimeException {
        if (sQLiteQueryBuilder == null) {
            sQLiteQueryBuilder = SQLiteUtils.newSQLiteQueryBuilder();
        }
        SQLiteQueryBuilder sQLiteQueryBuilder2 = sQLiteQueryBuilder;
        sQLiteQueryBuilder2.setTables(str);
        return sQLiteQueryBuilder2.query(getDatabase(), strArr, null, strArr2, null, null, str2);
    }

    @VisibleForTesting
    SQLiteDatabase getDatabase() {
        try {
            return this.mSQLiteOpenHelper.getWritableDatabase();
        } catch (RuntimeException e) {
            AppCenterLog.warn("AppCenter", "Failed to open database. Trying to delete database (may be corrupted).", e);
            if (this.mContext.deleteDatabase(this.mDatabase)) {
                AppCenterLog.info("AppCenter", "The database was successfully deleted.");
            } else {
                AppCenterLog.warn("AppCenter", "Failed to delete database.");
            }
            return this.mSQLiteOpenHelper.getWritableDatabase();
        }
    }

    @VisibleForTesting
    void setSQLiteOpenHelper(@NonNull SQLiteOpenHelper sQLiteOpenHelper) {
        this.mSQLiteOpenHelper.close();
        this.mSQLiteOpenHelper = sQLiteOpenHelper;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createTable(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues, String[] strArr) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
        sb.append(str);
        sb.append("` (oid INTEGER PRIMARY KEY AUTOINCREMENT");
        for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
            sb.append(", `");
            sb.append(entry.getKey());
            sb.append("` ");
            Object value = entry.getValue();
            if ((value instanceof Double) || (value instanceof Float)) {
                sb.append("REAL");
            } else if ((value instanceof Number) || (value instanceof Boolean)) {
                sb.append("INTEGER");
            } else if (value instanceof byte[]) {
                sb.append("BLOB");
            } else {
                sb.append("TEXT");
            }
        }
        if (strArr != null && strArr.length > 0) {
            sb.append(", UNIQUE(`");
            sb.append(TextUtils.join("`, `", strArr));
            sb.append("`)");
        }
        sb.append(");");
        sQLiteDatabase.execSQL(sb.toString());
    }

    public boolean setMaxSize(long j) {
        try {
            SQLiteDatabase database = getDatabase();
            long maximumSize = database.setMaximumSize(j);
            long pageSize = database.getPageSize();
            long j2 = j / pageSize;
            if (j % pageSize != 0) {
                j2++;
            }
            if (maximumSize != j2 * pageSize) {
                AppCenterLog.error("AppCenter", "Could not change maximum database size to " + j + " bytes, current maximum size is " + maximumSize + " bytes.");
                return false;
            }
            if (j == maximumSize) {
                AppCenterLog.info("AppCenter", "Changed maximum database size to " + maximumSize + " bytes.");
                return true;
            }
            AppCenterLog.info("AppCenter", "Changed maximum database size to " + maximumSize + " bytes (next multiple of page size).");
            return true;
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Could not change maximum database size.", e);
            return false;
        }
    }

    public long getMaxSize() {
        try {
            return getDatabase().getMaximumSize();
        } catch (RuntimeException e) {
            AppCenterLog.error("AppCenter", "Could not get maximum database size.", e);
            return -1L;
        }
    }
}
