package jp.co.nipro.cocoron.data.db.dao;

import java.util.List;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;
import kotlin.Metadata;
import kotlin.jvm.internal.LongCompanionObject;

/* compiled from: MaxMinRriDao.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0019\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0016\u0010\u0006\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bH'J\u0010\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH'J\u0010\u0010\f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\rH'J\u0016\u0010\u000e\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\r0\bH'J\u0010\u0010\u000f\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH'J\u0010\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0011H'J\u0016\u0010\u0012\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00110\bH'J\u0010\u0010\u0013\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH'J\u0010\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0015H'J\u0016\u0010\u0016\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00150\bH'J\u0010\u0010\u0017\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH'J \u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J\u001c\u0010\u001b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J \u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\r0\b2\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J\u001c\u0010\u001d\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J \u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00110\b2\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J\u001c\u0010\u001f\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J \u0010 \u001a\b\u0012\u0004\u0012\u00020\u00150\b2\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J\u001c\u0010!\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u000bH'J\u0016\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\n\u001a\u00020\u000bH'J\u0016\u0010#\u001a\b\u0012\u0004\u0012\u00020\r0\b2\u0006\u0010\n\u001a\u00020\u000bH'J\u0016\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00110\b2\u0006\u0010\n\u001a\u00020\u000bH'J\u0016\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00150\b2\u0006\u0010\n\u001a\u00020\u000bH'J\u0010\u0010&\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0010\u0010'\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\rH'J\u0010\u0010(\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0011H'J\u0010\u0010)\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0015H'J\u0010\u0010*\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0010\u0010+\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\rH'J\u0010\u0010,\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0011H'J\u0010\u0010-\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0015H'¨\u0006."}, d2 = {"Ljp/co/nipro/cocoron/data/db/dao/MaxMinRriDao;", "", "delete12h", "", "dto", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri12hDto;", "delete12hAll", "dtos", "", "delete12hTo", "dateTime", "", "delete1m", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri1mDto;", "delete1mAll", "delete1mTo", "delete24h", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri24hDto;", "delete24hAll", "delete24hTo", "delete7d", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri7dDto;", "delete7dAll", "delete7dTo", "filter12h", "frm", "to", "filter12hOne", "filter1m", "filter1mOne", "filter24h", "filter24hOne", "filter7d", "filter7dOne", "get12hToDateTime", "get1mToDateTime", "get24hToDateTime", "get7dToDateTime", "insert12h", "insert1m", "insert24h", "insert7d", "update12h", "update1m", "update24h", "update7d", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public interface MaxMinRriDao {
    void delete12h(MaxMinRri12hDto dto);

    void delete12hAll(List<MaxMinRri12hDto> dtos);

    void delete12hTo(long dateTime);

    void delete1m(MaxMinRri1mDto dto);

    void delete1mAll(List<MaxMinRri1mDto> dtos);

    void delete1mTo(long dateTime);

    void delete24h(MaxMinRri24hDto dto);

    void delete24hAll(List<MaxMinRri24hDto> dtos);

    void delete24hTo(long dateTime);

    void delete7d(MaxMinRri7dDto dto);

    void delete7dAll(List<MaxMinRri7dDto> dtos);

    void delete7dTo(long dateTime);

    List<MaxMinRri12hDto> filter12h(long frm, long to);

    MaxMinRri12hDto filter12hOne(long frm, long to);

    List<MaxMinRri1mDto> filter1m(long frm, long to);

    MaxMinRri1mDto filter1mOne(long frm, long to);

    List<MaxMinRri24hDto> filter24h(long frm, long to);

    MaxMinRri24hDto filter24hOne(long frm, long to);

    List<MaxMinRri7dDto> filter7d(long frm, long to);

    MaxMinRri7dDto filter7dOne(long frm, long to);

    List<MaxMinRri12hDto> get12hToDateTime(long dateTime);

    List<MaxMinRri1mDto> get1mToDateTime(long dateTime);

    List<MaxMinRri24hDto> get24hToDateTime(long dateTime);

    List<MaxMinRri7dDto> get7dToDateTime(long dateTime);

    void insert12h(MaxMinRri12hDto dto);

    void insert1m(MaxMinRri1mDto dto);

    void insert24h(MaxMinRri24hDto dto);

    void insert7d(MaxMinRri7dDto dto);

    void update12h(MaxMinRri12hDto dto);

    void update1m(MaxMinRri1mDto dto);

    void update24h(MaxMinRri24hDto dto);

    void update7d(MaxMinRri7dDto dto);

    /* compiled from: MaxMinRriDao.kt */
    @Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 4, 2})
    public static final class DefaultImpls {
        public static /* synthetic */ MaxMinRri12hDto filter12hOne$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter12hOne");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter12hOne(j, j2);
        }

        public static /* synthetic */ List filter12h$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter12h");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter12h(j, j2);
        }

        public static /* synthetic */ MaxMinRri24hDto filter24hOne$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter24hOne");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter24hOne(j, j2);
        }

        public static /* synthetic */ List filter24h$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter24h");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter24h(j, j2);
        }

        public static /* synthetic */ MaxMinRri7dDto filter7dOne$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter7dOne");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter7dOne(j, j2);
        }

        public static /* synthetic */ List filter7d$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter7d");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter7d(j, j2);
        }

        public static /* synthetic */ MaxMinRri1mDto filter1mOne$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter1mOne");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter1mOne(j, j2);
        }

        public static /* synthetic */ List filter1m$default(MaxMinRriDao maxMinRriDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter1m");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return maxMinRriDao.filter1m(j, j2);
        }
    }
}
