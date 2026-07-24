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
import jp.co.nipro.cocoron.data.db.dto.RRIDto;

/* loaded from: classes.dex */
public final class RRIDao_Impl implements RRIDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<RRIDto> __deletionAdapterOfRRIDto;
    private final EntityInsertionAdapter<RRIDto> __insertionAdapterOfRRIDto;
    private final SharedSQLiteStatement __preparedStmtOfDeleteTo;
    private final EntityDeletionOrUpdateAdapter<RRIDto> __updateAdapterOfRRIDto;

    public RRIDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfRRIDto = new EntityInsertionAdapter<RRIDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.RRIDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_rri` (`date`,`rri`,`outservice`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, RRIDto value) {
                stmt.bindLong(1, value.getDate());
                stmt.bindLong(2, value.getRri());
                stmt.bindLong(3, value.getOutservice());
            }
        };
        this.__deletionAdapterOfRRIDto = new EntityDeletionOrUpdateAdapter<RRIDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.RRIDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_rri` WHERE `date` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, RRIDto value) {
                stmt.bindLong(1, value.getDate());
            }
        };
        this.__updateAdapterOfRRIDto = new EntityDeletionOrUpdateAdapter<RRIDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.RRIDao_Impl.3
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_rri` SET `date` = ?,`rri` = ?,`outservice` = ? WHERE `date` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, RRIDto value) {
                stmt.bindLong(1, value.getDate());
                stmt.bindLong(2, value.getRri());
                stmt.bindLong(3, value.getOutservice());
                stmt.bindLong(4, value.getDate());
            }
        };
        this.__preparedStmtOfDeleteTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.RRIDao_Impl.4
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_rri WHERE date<?";
            }
        };
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public void insert(final RRIDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfRRIDto.insert((EntityInsertionAdapter<RRIDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public void delete(final RRIDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfRRIDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public void deleteAll(final List<RRIDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfRRIDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public void update(final RRIDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfRRIDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
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

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public List<RRIDto> getToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_rri WHERE date<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "date");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "rri");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "outservice");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new RRIDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public int getCount() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT count(*) from tbl_rri", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? query.getInt(0) : 0;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public RRIDto filterOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_rri WHERE date>? and date<? order by date limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new RRIDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "date")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "rri")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "outservice"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.RRIDao
    public List<RRIDto> filter(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_rri WHERE date>? and date<? order by date", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "date");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "rri");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "outservice");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new RRIDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
