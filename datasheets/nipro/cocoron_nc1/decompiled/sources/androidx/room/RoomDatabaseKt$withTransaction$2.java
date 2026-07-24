package androidx.room;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: RoomDatabase.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\f\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "R", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 1, 15})
@DebugMetadata(c = "androidx.room.RoomDatabaseKt$withTransaction$2", f = "RoomDatabase.kt", i = {0, 0}, l = {58}, m = "invokeSuspend", n = {"$this$withContext", "transactionElement"}, s = {"L$0", "L$1"})
/* loaded from: classes.dex */
final class RoomDatabaseKt$withTransaction$2<R> extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super R>, Object> {
    final /* synthetic */ Function1 $block;
    final /* synthetic */ RoomDatabase $this_withTransaction;
    Object L$0;
    Object L$1;
    int label;
    private CoroutineScope p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    RoomDatabaseKt$withTransaction$2(RoomDatabase roomDatabase, Function1 function1, Continuation continuation) {
        super(2, continuation);
        this.$this_withTransaction = roomDatabase;
        this.$block = function1;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkParameterIsNotNull(completion, "completion");
        RoomDatabaseKt$withTransaction$2 roomDatabaseKt$withTransaction$2 = new RoomDatabaseKt$withTransaction$2(this.$this_withTransaction, this.$block, completion);
        roomDatabaseKt$withTransaction$2.p$ = (CoroutineScope) obj;
        return roomDatabaseKt$withTransaction$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Object obj) {
        return ((RoomDatabaseKt$withTransaction$2) create(coroutineScope, (Continuation) obj)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        TransactionElement transactionElement;
        TransactionElement transactionElement2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        try {
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                CoroutineScope coroutineScope = this.p$;
                CoroutineContext.Element element = coroutineScope.getCoroutineContext().get(TransactionElement.INSTANCE);
                if (element == null) {
                    Intrinsics.throwNpe();
                }
                transactionElement = (TransactionElement) element;
                transactionElement.acquire();
                try {
                    this.$this_withTransaction.beginTransaction();
                    try {
                        Function1 function1 = this.$block;
                        this.L$0 = coroutineScope;
                        this.L$1 = transactionElement;
                        this.label = 1;
                        obj = function1.invoke(this);
                        if (obj == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        transactionElement2 = transactionElement;
                    } catch (Throwable th) {
                        th = th;
                        this.$this_withTransaction.endTransaction();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    transactionElement.release();
                    throw th;
                }
            } else if (i == 1) {
                transactionElement2 = (TransactionElement) this.L$1;
                try {
                    ResultKt.throwOnFailure(obj);
                } catch (Throwable th3) {
                    th = th3;
                    this.$this_withTransaction.endTransaction();
                    throw th;
                }
            } else {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            this.$this_withTransaction.setTransactionSuccessful();
            this.$this_withTransaction.endTransaction();
            transactionElement2.release();
            return obj;
        } catch (Throwable th4) {
            th = th4;
            transactionElement = coroutine_suspended;
        }
    }
}
