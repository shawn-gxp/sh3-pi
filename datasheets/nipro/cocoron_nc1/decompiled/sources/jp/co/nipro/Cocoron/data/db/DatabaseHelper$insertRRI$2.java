package jp.co.nipro.cocoron.data.db;

import java.util.Date;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dao.MaxMinRriDao;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri12hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri7dDto;
import jp.co.nipro.cocoron.data.db.dto.RRIDto;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$insertRRI$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$insertRRI$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Object>, Object> {
    final /* synthetic */ Date $date;
    final /* synthetic */ int $outservice;
    final /* synthetic */ int $rri;
    private /* synthetic */ Object L$0;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$insertRRI$2(DatabaseHelper databaseHelper, Date date, int i, int i2, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$date = date;
        this.$rri = i;
        this.$outservice = i2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        DatabaseHelper$insertRRI$2 databaseHelper$insertRRI$2 = new DatabaseHelper$insertRRI$2(this.this$0, this.$date, this.$rri, this.$outservice, completion);
        databaseHelper$insertRRI$2.L$0 = obj;
        return databaseHelper$insertRRI$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Object> continuation) {
        return ((DatabaseHelper$insertRRI$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x007c, code lost:
    
        if (r12 != null) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x00d7, code lost:
    
        if (r12 != null) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x0132, code lost:
    
        if (r12 != null) goto L46;
     */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        DatabaseHelper.DataBase dataBase2;
        DatabaseHelper.DataBase dataBase3;
        DatabaseHelper.DataBase dataBase4;
        DatabaseHelper.DataBase dataBase5;
        DatabaseHelper.DataBase dataBase6;
        DatabaseHelper.DataBase dataBase7;
        DatabaseHelper.DataBase dataBase8;
        DatabaseHelper.DataBase dataBase9;
        DatabaseHelper.DataBase dataBase10;
        DatabaseHelper.DataBase dataBase11;
        DatabaseHelper.DataBase dataBase12;
        DatabaseHelper.DataBase dataBase13;
        Long boxLong;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            Date date = this.$date;
            long time = (date == null || (boxLong = Boxing.boxLong(date.getTime())) == null) ? new Date().getTime() : boxLong.longValue();
            dataBase = this.this$0.database;
            dataBase.rriDao().insert(new RRIDto(time, this.$rri, this.$outservice));
            dataBase2 = this.this$0.database;
            MaxMinRri12hDto filter12hOne$default = MaxMinRriDao.DefaultImpls.filter12hOne$default(dataBase2.maxMinRriDao(), time - MaxMinRri12hDto.duration, 0L, 2, null);
            if (filter12hOne$default != null) {
                int max = filter12hOne$default.getMax();
                int i = this.$rri;
                if (max < i) {
                    filter12hOne$default.setMax(i);
                }
                int min = filter12hOne$default.getMin();
                int i2 = this.$rri;
                if (min > i2) {
                    filter12hOne$default.setMin(i2);
                }
                dataBase13 = this.this$0.database;
                dataBase13.maxMinRriDao().update12h(filter12hOne$default);
            }
            long startTimestamp = this.this$0.getStartTimestamp(time, MaxMinRri12hDto.duration);
            int i3 = this.$rri;
            MaxMinRri12hDto maxMinRri12hDto = new MaxMinRri12hDto(startTimestamp, i3, i3);
            dataBase3 = this.this$0.database;
            dataBase3.maxMinRriDao().insert12h(maxMinRri12hDto);
            Unit unit = Unit.INSTANCE;
            dataBase4 = this.this$0.database;
            MaxMinRri24hDto filter24hOne$default = MaxMinRriDao.DefaultImpls.filter24hOne$default(dataBase4.maxMinRriDao(), time - MaxMinRri24hDto.duration, 0L, 2, null);
            if (filter24hOne$default != null) {
                int max2 = filter24hOne$default.getMax();
                int i4 = this.$rri;
                if (max2 < i4) {
                    filter24hOne$default.setMax(i4);
                }
                int min2 = filter24hOne$default.getMin();
                int i5 = this.$rri;
                if (min2 > i5) {
                    filter24hOne$default.setMin(i5);
                }
                dataBase12 = this.this$0.database;
                dataBase12.maxMinRriDao().update24h(filter24hOne$default);
            }
            long startTimestamp2 = this.this$0.getStartTimestamp(time, MaxMinRri24hDto.duration);
            int i6 = this.$rri;
            MaxMinRri24hDto maxMinRri24hDto = new MaxMinRri24hDto(startTimestamp2, i6, i6);
            dataBase5 = this.this$0.database;
            dataBase5.maxMinRriDao().insert24h(maxMinRri24hDto);
            Unit unit2 = Unit.INSTANCE;
            dataBase6 = this.this$0.database;
            MaxMinRri7dDto filter7dOne$default = MaxMinRriDao.DefaultImpls.filter7dOne$default(dataBase6.maxMinRriDao(), time - MaxMinRri7dDto.duration, 0L, 2, null);
            if (filter7dOne$default != null) {
                int max3 = filter7dOne$default.getMax();
                int i7 = this.$rri;
                if (max3 < i7) {
                    filter7dOne$default.setMax(i7);
                }
                int min3 = filter7dOne$default.getMin();
                int i8 = this.$rri;
                if (min3 > i8) {
                    filter7dOne$default.setMin(i8);
                }
                dataBase11 = this.this$0.database;
                dataBase11.maxMinRriDao().update7d(filter7dOne$default);
            }
            long startTimestamp3 = this.this$0.getStartTimestamp(time, MaxMinRri7dDto.duration);
            int i9 = this.$rri;
            MaxMinRri7dDto maxMinRri7dDto = new MaxMinRri7dDto(startTimestamp3, i9, i9);
            dataBase7 = this.this$0.database;
            dataBase7.maxMinRriDao().insert7d(maxMinRri7dDto);
            Unit unit3 = Unit.INSTANCE;
            dataBase8 = this.this$0.database;
            MaxMinRri1mDto filter1mOne$default = MaxMinRriDao.DefaultImpls.filter1mOne$default(dataBase8.maxMinRriDao(), time - 43200000, 0L, 2, null);
            if (filter1mOne$default != null) {
                int max4 = filter1mOne$default.getMax();
                int i10 = this.$rri;
                if (max4 < i10) {
                    filter1mOne$default.setMax(i10);
                }
                int min4 = filter1mOne$default.getMin();
                int i11 = this.$rri;
                if (min4 > i11) {
                    filter1mOne$default.setMin(i11);
                }
                dataBase10 = this.this$0.database;
                dataBase10.maxMinRriDao().update1m(filter1mOne$default);
                if (filter1mOne$default != null) {
                    return filter1mOne$default;
                }
            }
            long startTimestamp4 = this.this$0.getStartTimestamp(time, 43200000L);
            int i12 = this.$rri;
            MaxMinRri1mDto maxMinRri1mDto = new MaxMinRri1mDto(startTimestamp4, i12, i12);
            dataBase9 = this.this$0.database;
            dataBase9.maxMinRriDao().insert1m(maxMinRri1mDto);
            return Unit.INSTANCE;
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
}
