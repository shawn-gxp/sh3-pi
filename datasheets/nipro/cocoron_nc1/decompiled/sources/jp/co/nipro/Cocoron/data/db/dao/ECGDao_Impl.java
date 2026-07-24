package jp.co.nipro.cocoron.data.db.dao;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.util.ArrayList;
import java.util.List;
import jp.co.nipro.cocoron.data.db.dto.ECGDto;

/* loaded from: classes.dex */
public final class ECGDao_Impl implements ECGDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<ECGDto> __deletionAdapterOfECGDto;
    private final EntityInsertionAdapter<ECGDto> __insertionAdapterOfECGDto;
    private final SharedSQLiteStatement __preparedStmtOfDeleteTo;
    private final EntityDeletionOrUpdateAdapter<ECGDto> __updateAdapterOfECGDto;

    public ECGDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfECGDto = new EntityInsertionAdapter<ECGDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.ECGDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_ecg` (`date`,`ecg`) VALUES (?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, ECGDto value) {
                stmt.bindLong(1, value.getDate());
                if (value.getEcg() == null) {
                    stmt.bindNull(2);
                } else {
                    stmt.bindBlob(2, value.getEcg());
                }
            }
        };
        this.__deletionAdapterOfECGDto = new EntityDeletionOrUpdateAdapter<ECGDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.ECGDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_ecg` WHERE `date` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, ECGDto value) {
                stmt.bindLong(1, value.getDate());
            }
        };
        this.__updateAdapterOfECGDto = new EntityDeletionOrUpdateAdapter<ECGDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.ECGDao_Impl.3
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_ecg` SET `date` = ?,`ecg` = ? WHERE `date` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, ECGDto value) {
                stmt.bindLong(1, value.getDate());
                if (value.getEcg() == null) {
                    stmt.bindNull(2);
                } else {
                    stmt.bindBlob(2, value.getEcg());
                }
                stmt.bindLong(3, value.getDate());
            }
        };
        this.__preparedStmtOfDeleteTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.ECGDao_Impl.4
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_ecg WHERE date<?";
            }
        };
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public void insert(final ECGDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfECGDto.insert((EntityInsertionAdapter<ECGDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public void delete(final ECGDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfECGDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public void deleteAll(final List<ECGDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfECGDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public void update(final ECGDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfECGDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public void deleteTo(final long dateTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDeleteTo.acquire();
        acquire.bindLong(1, dateTime);
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteTo.release(acquire);
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public int getCount() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT count(*) from tbl_ecg", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? query.getInt(0) : 0;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public List<ECGDto> getToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_ecg WHERE date<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "date");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "ecg");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new ECGDto(query.getLong(columnIndexOrThrow), query.getBlob(columnIndexOrThrow2)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public ECGDto filterOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_ecg WHERE date>? and date<? order by date limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new ECGDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "date")), query.getBlob(CursorUtil.getColumnIndexOrThrow(query, "ecg"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.ECGDao
    public List<ECGDto> filter(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_ecg WHERE date>? and date<? order by date", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "date");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "ecg");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new ECGDto(query.getLong(columnIndexOrThrow), query.getBlob(columnIndexOrThrow2)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
