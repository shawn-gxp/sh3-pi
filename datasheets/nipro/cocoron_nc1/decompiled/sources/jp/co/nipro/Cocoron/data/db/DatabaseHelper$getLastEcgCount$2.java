package jp.co.nipro.cocoron.data.db;

import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dto.ECGDto;
import jp.co.nipro.cocoron.data.value.ECGMeasurementCharacteristicValue;
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
import kotlin.jvm.internal.LongCompanionObject;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$getLastEcgCount$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$getLastEcgCount$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Integer>, Object> {
    final /* synthetic */ long $date;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$getLastEcgCount$2(DatabaseHelper databaseHelper, long j, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$date = j;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$getLastEcgCount$2(this.this$0, this.$date, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Integer> continuation) {
        return ((DatabaseHelper$getLastEcgCount$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label != 0) {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        ResultKt.throwOnFailure(obj);
        int i = 0;
        dataBase = this.this$0.database;
        for (ECGDto eCGDto : dataBase.ecgDao().filter(this.$date - 10000, LongCompanionObject.MAX_VALUE)) {
            ECGMeasurementCharacteristicValue eCGMeasurementCharacteristicValue = new ECGMeasurementCharacteristicValue();
            eCGMeasurementCharacteristicValue.readECGMeasurementPacket(eCGDto.getEcg());
            i += eCGMeasurementCharacteristicValue.getDataSize();
        }
        return Boxing.boxInt(i);
    }
}
