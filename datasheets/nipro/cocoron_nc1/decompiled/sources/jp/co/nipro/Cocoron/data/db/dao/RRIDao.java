package jp.co.nipro.cocoron.data.db.dao;

import java.util.List;
import jp.co.nipro.cocoron.data.db.dto.RRIDto;
import kotlin.Metadata;
import kotlin.jvm.internal.LongCompanionObject;

/* compiled from: RRIDao.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0016\u0010\u0006\u001a\u00020\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bH'J\u0010\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH'J \u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\r\u001a\u00020\u000b2\b\b\u0002\u0010\u000e\u001a\u00020\u000bH'J\u001c\u0010\u000f\u001a\u0004\u0018\u00010\u00052\u0006\u0010\r\u001a\u00020\u000b2\b\b\u0002\u0010\u000e\u001a\u00020\u000bH'J\b\u0010\u0010\u001a\u00020\u0011H'J\u0016\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\n\u001a\u00020\u000bH'J\u0010\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'J\u0010\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H'¨\u0006\u0015"}, d2 = {"Ljp/co/nipro/cocoron/data/db/dao/RRIDao;", "", "delete", "", "dto", "Ljp/co/nipro/cocoron/data/db/dto/RRIDto;", "deleteAll", "dtos", "", "deleteTo", "dateTime", "", "filter", "frm", "to", "filterOne", "getCount", "", "getToDateTime", "insert", "update", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public interface RRIDao {
    void delete(RRIDto dto);

    void deleteAll(List<RRIDto> dtos);

    void deleteTo(long dateTime);

    List<RRIDto> filter(long frm, long to);

    RRIDto filterOne(long frm, long to);

    int getCount();

    List<RRIDto> getToDateTime(long dateTime);

    void insert(RRIDto dto);

    void update(RRIDto dto);

    /* compiled from: RRIDao.kt */
    @Metadata(bv = {1, 0, 3}, k = 3, mv = {1, 4, 2})
    public static final class DefaultImpls {
        public static /* synthetic */ RRIDto filterOne$default(RRIDao rRIDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filterOne");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return rRIDao.filterOne(j, j2);
        }

        public static /* synthetic */ List filter$default(RRIDao rRIDao, long j, long j2, int i, Object obj) {
            if (obj != null) {
                throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: filter");
            }
            if ((i & 2) != 0) {
                j2 = LongCompanionObject.MAX_VALUE;
            }
            return rRIDao.filter(j, j2);
        }
    }
}
