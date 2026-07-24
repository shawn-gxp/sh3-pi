package jp.co.nipro.cocoron.data.db;

import java.util.Arrays;
import java.util.Date;
import jp.co.nipro.cocoron.data.entity.ECGData;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.UByteArray;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$makeDmmyECGData$2", f = "DatabaseHelper.kt", i = {0}, l = {393}, m = "invokeSuspend", n = {"time"}, s = {"L$0"})
/* loaded from: classes.dex */
final class DatabaseHelper$makeDmmyECGData$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$makeDmmyECGData$2(DatabaseHelper databaseHelper, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$makeDmmyECGData$2(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$makeDmmyECGData$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0075  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0042  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:8:0x0069 -> B:5:0x006c). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        int length;
        Ref.LongRef longRef;
        DatabaseHelper$makeDmmyECGData$2 databaseHelper$makeDmmyECGData$2;
        UByteArray[] uByteArrayArr;
        int i;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i2 = this.label;
        if (i2 == 0) {
            ResultKt.throwOnFailure(obj);
            Ref.LongRef longRef2 = new Ref.LongRef();
            longRef2.element = 1612850400000L;
            UByteArray[] dmmyECG = ECGData.INSTANCE.getDmmyECG();
            length = dmmyECG.length;
            longRef = longRef2;
            databaseHelper$makeDmmyECGData$2 = this;
            uByteArrayArr = dmmyECG;
            i = 0;
            if (i < length) {
            }
        } else if (i2 == 1) {
            i = this.I$1;
            length = this.I$0;
            uByteArrayArr = (UByteArray[]) this.L$1;
            longRef = (Ref.LongRef) this.L$0;
            ResultKt.throwOnFailure(obj);
            databaseHelper$makeDmmyECGData$2 = this;
            longRef.element += 250;
            i++;
            if (i < length) {
                byte[] storage = uByteArrayArr[i].getStorage();
                DatabaseHelper databaseHelper = databaseHelper$makeDmmyECGData$2.this$0;
                byte[] copyOf = Arrays.copyOf(storage, storage.length);
                Intrinsics.checkNotNullExpressionValue(copyOf, "java.util.Arrays.copyOf(this, size)");
                Date date = new Date(longRef.element);
                databaseHelper$makeDmmyECGData$2.L$0 = longRef;
                databaseHelper$makeDmmyECGData$2.L$1 = uByteArrayArr;
                databaseHelper$makeDmmyECGData$2.I$0 = length;
                databaseHelper$makeDmmyECGData$2.I$1 = i;
                databaseHelper$makeDmmyECGData$2.label = 1;
                if (databaseHelper.insertECG(copyOf, date, databaseHelper$makeDmmyECGData$2) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                longRef.element += 250;
                i++;
                if (i < length) {
                    return Unit.INSTANCE;
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
