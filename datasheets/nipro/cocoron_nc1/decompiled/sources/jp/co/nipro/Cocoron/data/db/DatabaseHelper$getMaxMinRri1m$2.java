package jp.co.nipro.cocoron.data.db;

import java.util.Date;
import java.util.List;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri1mDto;
import jp.co.nipro.cocoron.data.db.dto.MaxMinRri24hDto;
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
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri1mDto;", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$getMaxMinRri1m$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$getMaxMinRri1m$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super List<? extends MaxMinRri1mDto>>, Object> {
    final /* synthetic */ Date $date;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$getMaxMinRri1m$2(DatabaseHelper databaseHelper, Date date, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$date = date;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$getMaxMinRri1m$2(this.this$0, this.$date, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super List<? extends MaxMinRri1mDto>> continuation) {
        return ((DatabaseHelper$getMaxMinRri1m$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        Long boxLong;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            Date date = this.$date;
            long time = (date == null || (boxLong = Boxing.boxLong(date.getTime())) == null) ? new Date().getTime() : boxLong.longValue();
            dataBase = this.this$0.database;
            return dataBase.maxMinRriDao().filter1m((time - MaxMinRri1mDto.range) - MaxMinRri24hDto.range, time);
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
}
