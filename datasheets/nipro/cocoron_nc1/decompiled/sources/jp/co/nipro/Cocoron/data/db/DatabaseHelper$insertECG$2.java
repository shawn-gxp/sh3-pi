package jp.co.nipro.cocoron.data.db;

import java.util.Date;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dao.ECGDao;
import jp.co.nipro.cocoron.data.db.dto.ECGDto;
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
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$insertECG$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$insertECG$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Date $date;
    final /* synthetic */ byte[] $ecg;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$insertECG$2(DatabaseHelper databaseHelper, Date date, byte[] bArr, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$date = date;
        this.$ecg = bArr;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$insertECG$2(this.this$0, this.$date, this.$ecg, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$insertECG$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        Long boxLong;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label != 0) {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        ResultKt.throwOnFailure(obj);
        dataBase = this.this$0.database;
        ECGDao ecgDao = dataBase.ecgDao();
        Date date = this.$date;
        ecgDao.insert(new ECGDto((date == null || (boxLong = Boxing.boxLong(date.getTime())) == null) ? new Date().getTime() : boxLong.longValue(), this.$ecg));
        return Unit.INSTANCE;
    }
}
