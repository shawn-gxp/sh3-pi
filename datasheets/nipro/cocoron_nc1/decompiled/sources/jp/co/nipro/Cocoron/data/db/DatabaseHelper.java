package jp.co.nipro.cocoron.data.db;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.Date;
import java.util.List;
import java.util.Random;
import jp.co.nipro.cocoron.common.BaseApplication;
import jp.co.nipro.cocoron.data.db.dao.ECGDao;
import jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao;
import jp.co.nipro.cocoron.data.db.dao.RRIDao;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;
import jp.co.nipro.cocoron.data.db.dto.RRIDto;
import jp.co.nipro.cocoron.data.entity.ECGData;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0014\u0018\u0000 52\u00020\u0001:\u000256B\u0007\b\u0016¢\u0006\u0002\u0010\u0002J\u0011\u0010\u0005\u001a\u00020\u0006H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J#\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\rJ\u0019\u0010\u000e\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000bH\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u000fJ\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J!\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u00112\b\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0016J!\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u00112\b\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0016J!\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u00112\b\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0016J!\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00112\b\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0016J\u0016\u0010\u001d\u001a\u00020\u000b2\u0006\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u001f\u001a\u00020\u000bJ%\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010$J/\u0010%\u001a\u00020\u00012\u0006\u0010&\u001a\u00020\u00062\b\b\u0002\u0010'\u001a\u00020\u00062\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0015H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010(J\u0011\u0010)\u001a\u00020!H\u0087@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J\u0011\u0010*\u001a\u00020!H\u0087@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J\u0011\u0010+\u001a\u00020!H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J\u0019\u0010,\u001a\u00020!2\u0006\u0010-\u001a\u00020\u0006H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010.J\u0016\u0010/\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\u0006\u00101\u001a\u00020\u0006J\u0016\u00102\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\u0006\u00101\u001a\u00020\u0006J\u0011\u00103\u001a\u00020!H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0007J\u0011\u00104\u001a\u00020\u0006H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e¢\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019¨\u00067"}, d2 = {"Ljp/co/nipro/cocoron/data/db/DatabaseHelper;", "", "()V", "database", "Ljp/co/nipro/cocoron/data/db/DatabaseHelper$DataBase;", "ecgCount", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEcgData", "Ljp/co/nipro/cocoron/data/entity/ECGData;", "date", "", "range", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLastEcgCount", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLastRri", "", "Ljp/co/nipro/cocoron/data/db/dto/RRIDto;", "getMaxMinRri12h", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri12hDto;", "Ljava/util/Date;", "(Ljava/util/Date;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMaxMinRri1m", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri1mDto;", "getMaxMinRri24h", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri24hDto;", "getMaxMinRri7d", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri7dDto;", "getStartTimestamp", "curr", "duration", "insertECG", "", "ecg", "", "([BLjava/util/Date;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertRRI", "rri", "outservice", "(IILjava/util/Date;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "makeDmmyECGData", "makeECGData", "makeRRIData", "makeTestRRIData", "mode", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "random", "mean", "deviation", "randomLine", "removeOldDb", "rriCount", "Companion", "DataBase", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class DatabaseHelper {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static DatabaseHelper INSTANCE;
    private DataBase database;

    /* compiled from: DatabaseHelper.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b'\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&¨\u0006\t"}, d2 = {"Ljp/co/nipro/cocoron/data/db/DatabaseHelper$DataBase;", "Landroidx/room/RoomDatabase;", "()V", "ecgDao", "Ljp/co/nipro/cocoron/data/db/dao/ECGDao;", "maxMinRriDao", "Ljp/co/nipro/cocoron/data/db/dao/MaxMinRriDao;", "rriDao", "Ljp/co/nipro/cocoron/data/db/dao/RRIDao;", "app_release"}, k = 1, mv = {1, 4, 2})
    public static abstract class DataBase extends RoomDatabase {
        public abstract ECGDao ecgDao();

        public abstract MaxMinRriDao maxMinRriDao();

        public abstract RRIDao rriDao();
    }

    public DatabaseHelper() {
        RoomDatabase build = Room.databaseBuilder(BaseApplication.INSTANCE.getContext(), DataBase.class, "heartline.sqlite").build();
        Intrinsics.checkNotNullExpressionValue(build, "Room.databaseBuilder(\n  …sqlite\"\n        ).build()");
        this.database = (DataBase) build;
    }

    /* compiled from: DatabaseHelper.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\b"}, d2 = {"Ljp/co/nipro/cocoron/data/db/DatabaseHelper$Companion;", "", "()V", "INSTANCE", "Ljp/co/nipro/cocoron/data/db/DatabaseHelper;", "destroyInstance", "", "getInstance", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final DatabaseHelper getInstance() {
            if (DatabaseHelper.INSTANCE == null) {
                synchronized (Reflection.getOrCreateKotlinClass(DatabaseHelper.class)) {
                    DatabaseHelper.INSTANCE = new DatabaseHelper();
                    Unit unit = Unit.INSTANCE;
                }
            }
            DatabaseHelper databaseHelper = DatabaseHelper.INSTANCE;
            Intrinsics.checkNotNull(databaseHelper);
            return databaseHelper;
        }

        public final void destroyInstance() {
            DatabaseHelper.INSTANCE = (DatabaseHelper) null;
        }
    }

    public final Object removeOldDb(Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$removeOldDb$2(this, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public static /* synthetic */ Object insertRRI$default(DatabaseHelper databaseHelper, int i, int i2, Date date, Continuation continuation, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        if ((i3 & 4) != 0) {
            date = (Date) null;
        }
        return databaseHelper.insertRRI(i, i2, date, continuation);
    }

    public final Object insertRRI(int i, int i2, Date date, Continuation<Object> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$insertRRI$2(this, date, i, i2, null), continuation);
    }

    public static /* synthetic */ Object insertECG$default(DatabaseHelper databaseHelper, byte[] bArr, Date date, Continuation continuation, int i, Object obj) {
        if ((i & 2) != 0) {
            date = (Date) null;
        }
        return databaseHelper.insertECG(bArr, date, continuation);
    }

    public final Object insertECG(byte[] bArr, Date date, Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$insertECG$2(this, date, bArr, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public final Object getMaxMinRri12h(Date date, Continuation<? super List<MaxMinRri12hDto>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getMaxMinRri12h$2(this, date, null), continuation);
    }

    public final Object getMaxMinRri24h(Date date, Continuation<? super List<MaxMinRri24hDto>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getMaxMinRri24h$2(this, date, null), continuation);
    }

    public final Object getMaxMinRri7d(Date date, Continuation<? super List<MaxMinRri7dDto>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getMaxMinRri7d$2(this, date, null), continuation);
    }

    public final Object getMaxMinRri1m(Date date, Continuation<? super List<MaxMinRri1mDto>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getMaxMinRri1m$2(this, date, null), continuation);
    }

    public final Object getEcgData(long j, long j2, Continuation<? super ECGData> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getEcgData$2(this, j, j2, null), continuation);
    }

    public final Object getLastEcgCount(long j, Continuation<? super Integer> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getLastEcgCount$2(this, j, null), continuation);
    }

    public final Object getLastRri(Continuation<? super List<RRIDto>> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$getLastRri$2(this, null), continuation);
    }

    public final Object rriCount(Continuation<? super Integer> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$rriCount$2(this, null), continuation);
    }

    public final Object ecgCount(Continuation<? super Integer> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$ecgCount$2(this, null), continuation);
    }

    public final Object makeRRIData(Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$makeRRIData$2(this, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public final Object makeTestRRIData(int i, Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$makeTestRRIData$2(this, i, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public final Object makeECGData(Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$makeECGData$2(this, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public final Object makeDmmyECGData(Continuation<? super Unit> continuation) {
        Object withContext = BuildersKt.withContext(Dispatchers.getIO(), new DatabaseHelper$makeDmmyECGData$2(this, null), continuation);
        return withContext == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContext : Unit.INSTANCE;
    }

    public final int random(int mean, int deviation) {
        return (int) ((Math.sqrt(deviation - mean) * new Random().nextGaussian() * 1.2d) + ((deviation + mean) / 2));
    }

    public final int randomLine(int mean, int deviation) {
        return new Random().nextInt((deviation - mean) + 1) + mean;
    }

    public final long getStartTimestamp(long curr, long duration) {
        return (curr / duration) * duration;
    }
}
