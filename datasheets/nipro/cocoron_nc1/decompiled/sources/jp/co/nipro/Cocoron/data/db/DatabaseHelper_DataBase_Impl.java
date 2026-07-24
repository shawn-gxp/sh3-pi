package jp.co.nipro.cocoron.data.db;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.util.HashMap;
import java.util.HashSet;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dao.ECGDao;
import jp.co.nipro.cocoron.data.db.dao.ECGDao_Impl;
import jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao;
import jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl;
import jp.co.nipro.cocoron.data.db.dao.RRIDao;
import jp.co.nipro.cocoron.data.db.dao.RRIDao_Impl;

/* loaded from: classes.dex */
public final class DatabaseHelper_DataBase_Impl extends DatabaseHelper.DataBase {
    private volatile ECGDao _eCGDao;
    private volatile MaxMinRriDao _maxMinRriDao;
    private volatile RRIDao _rRIDao;

    @Override // androidx.room.RoomDatabase
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
        return configuration.sqliteOpenHelperFactory.create(SupportSQLiteOpenHelper.Configuration.builder(configuration.context).name(configuration.name).callback(new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) { // from class: jp.co.nipro.cocoron.data.db.DatabaseHelper_DataBase_Impl.1
            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPostMigrate(SupportSQLiteDatabase _db) {
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void createAllTables(SupportSQLiteDatabase _db) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_ecg` (`date` INTEGER NOT NULL, `ecg` BLOB NOT NULL, PRIMARY KEY(`date`))");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_rri` (`date` INTEGER NOT NULL, `rri` INTEGER NOT NULL, `outservice` INTEGER NOT NULL, PRIMARY KEY(`date`))");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_max_min_12h` (`start` INTEGER NOT NULL, `max` INTEGER NOT NULL, `min` INTEGER NOT NULL, PRIMARY KEY(`start`))");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_max_min_24h` (`start` INTEGER NOT NULL, `max` INTEGER NOT NULL, `min` INTEGER NOT NULL, PRIMARY KEY(`start`))");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_max_min_7d` (`start` INTEGER NOT NULL, `max` INTEGER NOT NULL, `min` INTEGER NOT NULL, PRIMARY KEY(`start`))");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `tbl_max_min_1m` (`start` INTEGER NOT NULL, `max` INTEGER NOT NULL, `min` INTEGER NOT NULL, PRIMARY KEY(`start`))");
                _db.execSQL(RoomMasterTable.CREATE_QUERY);
                _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a15713ea545545c6849b17739f5bf9ce')");
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void dropAllTables(SupportSQLiteDatabase _db) {
                _db.execSQL("DROP TABLE IF EXISTS `tbl_ecg`");
                _db.execSQL("DROP TABLE IF EXISTS `tbl_rri`");
                _db.execSQL("DROP TABLE IF EXISTS `tbl_max_min_12h`");
                _db.execSQL("DROP TABLE IF EXISTS `tbl_max_min_24h`");
                _db.execSQL("DROP TABLE IF EXISTS `tbl_max_min_7d`");
                _db.execSQL("DROP TABLE IF EXISTS `tbl_max_min_1m`");
                if (DatabaseHelper_DataBase_Impl.this.mCallbacks != null) {
                    int size = DatabaseHelper_DataBase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) DatabaseHelper_DataBase_Impl.this.mCallbacks.get(i)).onDestructiveMigration(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected void onCreate(SupportSQLiteDatabase _db) {
                if (DatabaseHelper_DataBase_Impl.this.mCallbacks != null) {
                    int size = DatabaseHelper_DataBase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) DatabaseHelper_DataBase_Impl.this.mCallbacks.get(i)).onCreate(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onOpen(SupportSQLiteDatabase _db) {
                DatabaseHelper_DataBase_Impl.this.mDatabase = _db;
                DatabaseHelper_DataBase_Impl.this.internalInitInvalidationTracker(_db);
                if (DatabaseHelper_DataBase_Impl.this.mCallbacks != null) {
                    int size = DatabaseHelper_DataBase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) DatabaseHelper_DataBase_Impl.this.mCallbacks.get(i)).onOpen(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPreMigrate(SupportSQLiteDatabase _db) {
                DBUtil.dropFtsSyncTriggers(_db);
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
                HashMap hashMap = new HashMap(2);
                hashMap.put("date", new TableInfo.Column("date", "INTEGER", true, 1, null, 1));
                hashMap.put("ecg", new TableInfo.Column("ecg", "BLOB", true, 0, null, 1));
                TableInfo tableInfo = new TableInfo("tbl_ecg", hashMap, new HashSet(0), new HashSet(0));
                TableInfo read = TableInfo.read(_db, "tbl_ecg");
                if (!tableInfo.equals(read)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_ecg(jp.co.nipro.cocoron.data.db.dto.ECGDto).\n Expected:\n" + tableInfo + "\n Found:\n" + read);
                }
                HashMap hashMap2 = new HashMap(3);
                hashMap2.put("date", new TableInfo.Column("date", "INTEGER", true, 1, null, 1));
                hashMap2.put("rri", new TableInfo.Column("rri", "INTEGER", true, 0, null, 1));
                hashMap2.put("outservice", new TableInfo.Column("outservice", "INTEGER", true, 0, null, 1));
                TableInfo tableInfo2 = new TableInfo("tbl_rri", hashMap2, new HashSet(0), new HashSet(0));
                TableInfo read2 = TableInfo.read(_db, "tbl_rri");
                if (!tableInfo2.equals(read2)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_rri(jp.co.nipro.cocoron.data.db.dto.RRIDto).\n Expected:\n" + tableInfo2 + "\n Found:\n" + read2);
                }
                HashMap hashMap3 = new HashMap(3);
                hashMap3.put("start", new TableInfo.Column("start", "INTEGER", true, 1, null, 1));
                hashMap3.put("max", new TableInfo.Column("max", "INTEGER", true, 0, null, 1));
                hashMap3.put("min", new TableInfo.Column("min", "INTEGER", true, 0, null, 1));
                TableInfo tableInfo3 = new TableInfo("tbl_max_min_12h", hashMap3, new HashSet(0), new HashSet(0));
                TableInfo read3 = TableInfo.read(_db, "tbl_max_min_12h");
                if (!tableInfo3.equals(read3)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_max_min_12h(jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto).\n Expected:\n" + tableInfo3 + "\n Found:\n" + read3);
                }
                HashMap hashMap4 = new HashMap(3);
                hashMap4.put("start", new TableInfo.Column("start", "INTEGER", true, 1, null, 1));
                hashMap4.put("max", new TableInfo.Column("max", "INTEGER", true, 0, null, 1));
                hashMap4.put("min", new TableInfo.Column("min", "INTEGER", true, 0, null, 1));
                TableInfo tableInfo4 = new TableInfo("tbl_max_min_24h", hashMap4, new HashSet(0), new HashSet(0));
                TableInfo read4 = TableInfo.read(_db, "tbl_max_min_24h");
                if (!tableInfo4.equals(read4)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_max_min_24h(jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto).\n Expected:\n" + tableInfo4 + "\n Found:\n" + read4);
                }
                HashMap hashMap5 = new HashMap(3);
                hashMap5.put("start", new TableInfo.Column("start", "INTEGER", true, 1, null, 1));
                hashMap5.put("max", new TableInfo.Column("max", "INTEGER", true, 0, null, 1));
                hashMap5.put("min", new TableInfo.Column("min", "INTEGER", true, 0, null, 1));
                TableInfo tableInfo5 = new TableInfo("tbl_max_min_7d", hashMap5, new HashSet(0), new HashSet(0));
                TableInfo read5 = TableInfo.read(_db, "tbl_max_min_7d");
                if (!tableInfo5.equals(read5)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_max_min_7d(jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto).\n Expected:\n" + tableInfo5 + "\n Found:\n" + read5);
                }
                HashMap hashMap6 = new HashMap(3);
                hashMap6.put("start", new TableInfo.Column("start", "INTEGER", true, 1, null, 1));
                hashMap6.put("max", new TableInfo.Column("max", "INTEGER", true, 0, null, 1));
                hashMap6.put("min", new TableInfo.Column("min", "INTEGER", true, 0, null, 1));
                TableInfo tableInfo6 = new TableInfo("tbl_max_min_1m", hashMap6, new HashSet(0), new HashSet(0));
                TableInfo read6 = TableInfo.read(_db, "tbl_max_min_1m");
                if (!tableInfo6.equals(read6)) {
                    return new RoomOpenHelper.ValidationResult(false, "tbl_max_min_1m(jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto).\n Expected:\n" + tableInfo6 + "\n Found:\n" + read6);
                }
                return new RoomOpenHelper.ValidationResult(true, null);
            }
        }, "a15713ea545545c6849b17739f5bf9ce", "3338f997c07aa0aea626f962345a8531")).build());
    }

    @Override // androidx.room.RoomDatabase
    protected InvalidationTracker createInvalidationTracker() {
        return new InvalidationTracker(this, new HashMap(0), new HashMap(0), "tbl_ecg", "tbl_rri", "tbl_max_min_12h", "tbl_max_min_24h", "tbl_max_min_7d", "tbl_max_min_1m");
    }

    @Override // androidx.room.RoomDatabase
    public void clearAllTables() {
        super.assertNotMainThread();
        SupportSQLiteDatabase writableDatabase = super.getOpenHelper().getWritableDatabase();
        try {
            super.beginTransaction();
            writableDatabase.execSQL("DELETE FROM `tbl_ecg`");
            writableDatabase.execSQL("DELETE FROM `tbl_rri`");
            writableDatabase.execSQL("DELETE FROM `tbl_max_min_12h`");
            writableDatabase.execSQL("DELETE FROM `tbl_max_min_24h`");
            writableDatabase.execSQL("DELETE FROM `tbl_max_min_7d`");
            writableDatabase.execSQL("DELETE FROM `tbl_max_min_1m`");
            super.setTransactionSuccessful();
        } finally {
            super.endTransaction();
            writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close();
            if (!writableDatabase.inTransaction()) {
                writableDatabase.execSQL("VACUUM");
            }
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.DatabaseHelper.DataBase
    public ECGDao ecgDao() {
        ECGDao eCGDao;
        if (this._eCGDao != null) {
            return this._eCGDao;
        }
        synchronized (this) {
            if (this._eCGDao == null) {
                this._eCGDao = new ECGDao_Impl(this);
            }
            eCGDao = this._eCGDao;
        }
        return eCGDao;
    }

    @Override // jp.co.nipro.cocoron.data.db.DatabaseHelper.DataBase
    public RRIDao rriDao() {
        RRIDao rRIDao;
        if (this._rRIDao != null) {
            return this._rRIDao;
        }
        synchronized (this) {
            if (this._rRIDao == null) {
                this._rRIDao = new RRIDao_Impl(this);
            }
            rRIDao = this._rRIDao;
        }
        return rRIDao;
    }

    @Override // jp.co.nipro.cocoron.data.db.DatabaseHelper.DataBase
    public MaxMinRriDao maxMinRriDao() {
        MaxMinRriDao maxMinRriDao;
        if (this._maxMinRriDao != null) {
            return this._maxMinRriDao;
        }
        synchronized (this) {
            if (this._maxMinRriDao == null) {
                this._maxMinRriDao = new MaxMinRriDao_Impl(this);
            }
            maxMinRriDao = this._maxMinRriDao;
        }
        return maxMinRriDao;
    }
}
