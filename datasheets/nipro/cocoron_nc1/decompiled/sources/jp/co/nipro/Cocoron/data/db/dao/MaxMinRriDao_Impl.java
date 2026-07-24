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
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;

/* loaded from: classes.dex */
public final class MaxMinRriDao_Impl implements MaxMinRriDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri12hDto> __deletionAdapterOfMaxMinRri12hDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri1mDto> __deletionAdapterOfMaxMinRri1mDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri24hDto> __deletionAdapterOfMaxMinRri24hDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri7dDto> __deletionAdapterOfMaxMinRri7dDto;
    private final EntityInsertionAdapter<MaxMinRri12hDto> __insertionAdapterOfMaxMinRri12hDto;
    private final EntityInsertionAdapter<MaxMinRri1mDto> __insertionAdapterOfMaxMinRri1mDto;
    private final EntityInsertionAdapter<MaxMinRri24hDto> __insertionAdapterOfMaxMinRri24hDto;
    private final EntityInsertionAdapter<MaxMinRri7dDto> __insertionAdapterOfMaxMinRri7dDto;
    private final SharedSQLiteStatement __preparedStmtOfDelete12hTo;
    private final SharedSQLiteStatement __preparedStmtOfDelete1mTo;
    private final SharedSQLiteStatement __preparedStmtOfDelete24hTo;
    private final SharedSQLiteStatement __preparedStmtOfDelete7dTo;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri12hDto> __updateAdapterOfMaxMinRri12hDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri1mDto> __updateAdapterOfMaxMinRri1mDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri24hDto> __updateAdapterOfMaxMinRri24hDto;
    private final EntityDeletionOrUpdateAdapter<MaxMinRri7dDto> __updateAdapterOfMaxMinRri7dDto;

    public MaxMinRriDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfMaxMinRri12hDto = new EntityInsertionAdapter<MaxMinRri12hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_max_min_12h` (`start`,`max`,`min`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri12hDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
            }
        };
        this.__insertionAdapterOfMaxMinRri24hDto = new EntityInsertionAdapter<MaxMinRri24hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.2
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_max_min_24h` (`start`,`max`,`min`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri24hDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
            }
        };
        this.__insertionAdapterOfMaxMinRri7dDto = new EntityInsertionAdapter<MaxMinRri7dDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.3
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_max_min_7d` (`start`,`max`,`min`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri7dDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
            }
        };
        this.__insertionAdapterOfMaxMinRri1mDto = new EntityInsertionAdapter<MaxMinRri1mDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.4
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `tbl_max_min_1m` (`start`,`max`,`min`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri1mDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
            }
        };
        this.__deletionAdapterOfMaxMinRri12hDto = new EntityDeletionOrUpdateAdapter<MaxMinRri12hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.5
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_max_min_12h` WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri12hDto value) {
                stmt.bindLong(1, value.getStart());
            }
        };
        this.__deletionAdapterOfMaxMinRri24hDto = new EntityDeletionOrUpdateAdapter<MaxMinRri24hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.6
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_max_min_24h` WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri24hDto value) {
                stmt.bindLong(1, value.getStart());
            }
        };
        this.__deletionAdapterOfMaxMinRri7dDto = new EntityDeletionOrUpdateAdapter<MaxMinRri7dDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.7
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_max_min_7d` WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri7dDto value) {
                stmt.bindLong(1, value.getStart());
            }
        };
        this.__deletionAdapterOfMaxMinRri1mDto = new EntityDeletionOrUpdateAdapter<MaxMinRri1mDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.8
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM `tbl_max_min_1m` WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri1mDto value) {
                stmt.bindLong(1, value.getStart());
            }
        };
        this.__updateAdapterOfMaxMinRri12hDto = new EntityDeletionOrUpdateAdapter<MaxMinRri12hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.9
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_max_min_12h` SET `start` = ?,`max` = ?,`min` = ? WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri12hDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
                stmt.bindLong(4, value.getStart());
            }
        };
        this.__updateAdapterOfMaxMinRri24hDto = new EntityDeletionOrUpdateAdapter<MaxMinRri24hDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.10
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_max_min_24h` SET `start` = ?,`max` = ?,`min` = ? WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri24hDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
                stmt.bindLong(4, value.getStart());
            }
        };
        this.__updateAdapterOfMaxMinRri7dDto = new EntityDeletionOrUpdateAdapter<MaxMinRri7dDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.11
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_max_min_7d` SET `start` = ?,`max` = ?,`min` = ? WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri7dDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
                stmt.bindLong(4, value.getStart());
            }
        };
        this.__updateAdapterOfMaxMinRri1mDto = new EntityDeletionOrUpdateAdapter<MaxMinRri1mDto>(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.12
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR REPLACE `tbl_max_min_1m` SET `start` = ?,`max` = ?,`min` = ? WHERE `start` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement stmt, MaxMinRri1mDto value) {
                stmt.bindLong(1, value.getStart());
                stmt.bindLong(2, value.getMax());
                stmt.bindLong(3, value.getMin());
                stmt.bindLong(4, value.getStart());
            }
        };
        this.__preparedStmtOfDelete12hTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.13
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_max_min_12h WHERE start<?";
            }
        };
        this.__preparedStmtOfDelete24hTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.14
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_max_min_24h WHERE start<?";
            }
        };
        this.__preparedStmtOfDelete7dTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.15
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_max_min_7d WHERE start<?";
            }
        };
        this.__preparedStmtOfDelete1mTo = new SharedSQLiteStatement(__db) { // from class: jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao_Impl.16
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE from tbl_max_min_1m WHERE start<?";
            }
        };
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void insert12h(final MaxMinRri12hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfMaxMinRri12hDto.insert((EntityInsertionAdapter<MaxMinRri12hDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void insert24h(final MaxMinRri24hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfMaxMinRri24hDto.insert((EntityInsertionAdapter<MaxMinRri24hDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void insert7d(final MaxMinRri7dDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfMaxMinRri7dDto.insert((EntityInsertionAdapter<MaxMinRri7dDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void insert1m(final MaxMinRri1mDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfMaxMinRri1mDto.insert((EntityInsertionAdapter<MaxMinRri1mDto>) dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete12h(final MaxMinRri12hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri12hDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete12hAll(final List<MaxMinRri12hDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri12hDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete24h(final MaxMinRri24hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri24hDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete24hAll(final List<MaxMinRri24hDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri24hDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete7d(final MaxMinRri7dDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri7dDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete7dAll(final List<MaxMinRri7dDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri7dDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete1m(final MaxMinRri1mDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri1mDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete1mAll(final List<MaxMinRri1mDto> dtos) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfMaxMinRri1mDto.handleMultiple(dtos);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void update12h(final MaxMinRri12hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfMaxMinRri12hDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void update24h(final MaxMinRri24hDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfMaxMinRri24hDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void update7d(final MaxMinRri7dDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfMaxMinRri7dDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void update1m(final MaxMinRri1mDto dto) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfMaxMinRri1mDto.handle(dto);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete12hTo(final long dateTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDelete12hTo.acquire();
        acquire.bindLong(1, dateTime);
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDelete12hTo.release(acquire);
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete24hTo(final long dateTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDelete24hTo.acquire();
        acquire.bindLong(1, dateTime);
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDelete24hTo.release(acquire);
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete7dTo(final long dateTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDelete7dTo.acquire();
        acquire.bindLong(1, dateTime);
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDelete7dTo.release(acquire);
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public void delete1mTo(final long dateTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDelete1mTo.acquire();
        acquire.bindLong(1, dateTime);
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDelete1mTo.release(acquire);
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri12hDto> get12hToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_12h WHERE start<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri12hDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public MaxMinRri12hDto filter12hOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_12h WHERE start>? and start<? order by start limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new MaxMinRri12hDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "start")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "max")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "min"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri12hDto> filter12h(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_12h WHERE start>? and start<? order by start", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri12hDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri24hDto> get24hToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_24h WHERE start<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri24hDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public MaxMinRri24hDto filter24hOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_24h WHERE start>? and start<? order by start limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new MaxMinRri24hDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "start")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "max")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "min"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri24hDto> filter24h(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_24h WHERE start>? and start<? order by start", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri24hDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri7dDto> get7dToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_7d WHERE start<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri7dDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public MaxMinRri7dDto filter7dOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_7d WHERE start>? and start<? order by start limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new MaxMinRri7dDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "start")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "max")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "min"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri7dDto> filter7d(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_7d WHERE start>? and start<? order by start", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri7dDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri1mDto> get1mToDateTime(final long dateTime) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_1m WHERE start<?", 1);
        acquire.bindLong(1, dateTime);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri1mDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public MaxMinRri1mDto filter1mOne(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_1m WHERE start>? and start<? order by start limit 1", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            return query.moveToFirst() ? new MaxMinRri1mDto(query.getLong(CursorUtil.getColumnIndexOrThrow(query, "start")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "max")), query.getInt(CursorUtil.getColumnIndexOrThrow(query, "min"))) : null;
        } finally {
            query.close();
            acquire.release();
        }
    }

    @Override // jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao
    public List<MaxMinRri1mDto> filter1m(final long frm, final long to) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * from tbl_max_min_1m WHERE start>? and start<? order by start", 2);
        acquire.bindLong(1, frm);
        acquire.bindLong(2, to);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "start");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "max");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "min");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                arrayList.add(new MaxMinRri1mDto(query.getLong(columnIndexOrThrow), query.getInt(columnIndexOrThrow2), query.getInt(columnIndexOrThrow3)));
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }
}
