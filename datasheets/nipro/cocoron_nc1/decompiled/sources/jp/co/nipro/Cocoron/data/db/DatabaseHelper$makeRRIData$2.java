package jp.co.nipro.cocoron.data.db;

import java.util.Calendar;
import java.util.Date;
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
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$makeRRIData$2", f = "DatabaseHelper.kt", i = {0, 0}, l = {303}, m = "invokeSuspend", n = {"now", "calendar"}, s = {"L$0", "L$1"})
/* loaded from: classes.dex */
final class DatabaseHelper$makeRRIData$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$makeRRIData$2(DatabaseHelper databaseHelper, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$makeRRIData$2(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$makeRRIData$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0044  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:9:0x005b -> B:5:0x005e). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        Calendar calendar;
        Date date;
        DatabaseHelper$makeRRIData$2 databaseHelper$makeRRIData$2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            Date date2 = new Date();
            calendar = Calendar.getInstance();
            Intrinsics.checkNotNullExpressionValue(calendar, "Calendar.getInstance()");
            calendar.add(2, -1);
            date = date2;
            databaseHelper$makeRRIData$2 = this;
            if (calendar.getTime().compareTo(date) < 0) {
            }
        } else if (i == 1) {
            calendar = (Calendar) this.L$1;
            date = (Date) this.L$0;
            ResultKt.throwOnFailure(obj);
            databaseHelper$makeRRIData$2 = this;
            calendar.add(13, 30);
            if (calendar.getTime().compareTo(date) < 0) {
                DatabaseHelper databaseHelper = databaseHelper$makeRRIData$2.this$0;
                int random = databaseHelper.random(30, 210);
                Date time = calendar.getTime();
                databaseHelper$makeRRIData$2.L$0 = date;
                databaseHelper$makeRRIData$2.L$1 = calendar;
                databaseHelper$makeRRIData$2.label = 1;
                if (databaseHelper.insertRRI(random, 0, time, databaseHelper$makeRRIData$2) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                calendar.add(13, 30);
                if (calendar.getTime().compareTo(date) < 0) {
                    return Unit.INSTANCE;
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
