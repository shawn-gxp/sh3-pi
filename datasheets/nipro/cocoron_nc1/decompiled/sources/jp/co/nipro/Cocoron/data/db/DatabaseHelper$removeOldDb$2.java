package jp.co.nipro.cocoron.data.db;

import java.util.Calendar;
import java.util.Date;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$removeOldDb$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$removeOldDb$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$removeOldDb$2(DatabaseHelper databaseHelper, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$removeOldDb$2(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$removeOldDb$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        DatabaseHelper.DataBase dataBase2;
        DatabaseHelper.DataBase dataBase3;
        DatabaseHelper.DataBase dataBase4;
        DatabaseHelper.DataBase dataBase5;
        DatabaseHelper.DataBase dataBase6;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label != 0) {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        ResultKt.throwOnFailure(obj);
        Calendar calendar = Calendar.getInstance();
        Intrinsics.checkNotNullExpressionValue(calendar, "Calendar.getInstance()");
        calendar.add(2, -1);
        Date time = calendar.getTime();
        Intrinsics.checkNotNullExpressionValue(time, "calendar.getTime()");
        dataBase = this.this$0.database;
        dataBase.ecgDao().deleteTo(time.getTime());
        dataBase2 = this.this$0.database;
        dataBase2.rriDao().deleteTo(time.getTime());
        dataBase3 = this.this$0.database;
        dataBase3.maxMinRriDao().delete12hTo(time.getTime());
        dataBase4 = this.this$0.database;
        dataBase4.maxMinRriDao().delete24hTo(time.getTime());
        dataBase5 = this.this$0.database;
        dataBase5.maxMinRriDao().delete7dTo(time.getTime());
        dataBase6 = this.this$0.database;
        dataBase6.maxMinRriDao().delete1mTo(time.getTime());
        return Unit.INSTANCE;
    }
}
