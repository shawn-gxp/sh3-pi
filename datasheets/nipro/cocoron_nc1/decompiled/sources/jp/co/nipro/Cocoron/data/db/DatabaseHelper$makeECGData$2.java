package jp.co.nipro.cocoron.data.db;

import androidx.recyclerview.widget.ItemTouchHelper;
import java.util.Arrays;
import java.util.Calendar;
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
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$makeECGData$2", f = "DatabaseHelper.kt", i = {0, 0, 0}, l = {379}, m = "invokeSuspend", n = {"now", "calendar", "date"}, s = {"L$0", "L$1", "L$2"})
/* loaded from: classes.dex */
final class DatabaseHelper$makeECGData$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int I$0;
    int I$1;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$makeECGData$2(DatabaseHelper databaseHelper, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$makeECGData$2(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$makeECGData$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x00a7  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x00b2  */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0075  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:14:0x0064 -> B:6:0x0073). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:8:0x009d -> B:5:0x00a0). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        Date date;
        Calendar calendar;
        DatabaseHelper$makeECGData$2 databaseHelper$makeECGData$2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            date = new Date();
            calendar = Calendar.getInstance();
            Intrinsics.checkNotNullExpressionValue(calendar, "Calendar.getInstance()");
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            calendar.add(2, -1);
            databaseHelper$makeECGData$2 = this;
            if (calendar.getTime().compareTo(date) >= 0) {
            }
        } else if (i == 1) {
            int i2 = this.I$1;
            int i3 = this.I$0;
            UByteArray[] uByteArrayArr = (UByteArray[]) this.L$3;
            Date date2 = (Date) this.L$2;
            Calendar calendar2 = (Calendar) this.L$1;
            Date date3 = (Date) this.L$0;
            ResultKt.throwOnFailure(obj);
            date = date3;
            Date date4 = date2;
            UByteArray[] dmmyECG = uByteArrayArr;
            DatabaseHelper$makeECGData$2 databaseHelper$makeECGData$22 = this;
            int length = i3;
            Calendar calendar3 = calendar2;
            calendar3.add(14, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
            i2++;
            if (i2 < length) {
                byte[] storage = dmmyECG[i2].getStorage();
                DatabaseHelper databaseHelper = databaseHelper$makeECGData$22.this$0;
                byte[] copyOf = Arrays.copyOf(storage, storage.length);
                Intrinsics.checkNotNullExpressionValue(copyOf, "java.util.Arrays.copyOf(this, size)");
                Date time = calendar3.getTime();
                databaseHelper$makeECGData$22.L$0 = date;
                databaseHelper$makeECGData$22.L$1 = calendar3;
                databaseHelper$makeECGData$22.L$2 = date4;
                databaseHelper$makeECGData$22.L$3 = dmmyECG;
                databaseHelper$makeECGData$22.I$0 = length;
                databaseHelper$makeECGData$22.I$1 = i2;
                databaseHelper$makeECGData$22.label = 1;
                if (databaseHelper.insertECG(copyOf, time, databaseHelper$makeECGData$22) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                calendar3.add(14, ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
                i2++;
                if (i2 < length) {
                    calendar3.setTime(date4);
                    calendar3.add(10, 1);
                    calendar = calendar3;
                    databaseHelper$makeECGData$2 = databaseHelper$makeECGData$22;
                    if (calendar.getTime().compareTo(date) >= 0) {
                        Date time2 = calendar.getTime();
                        dmmyECG = ECGData.INSTANCE.getDmmyECG();
                        length = dmmyECG.length;
                        date4 = time2;
                        databaseHelper$makeECGData$22 = databaseHelper$makeECGData$2;
                        calendar3 = calendar;
                        i2 = 0;
                        if (i2 < length) {
                        }
                    } else {
                        return Unit.INSTANCE;
                    }
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
