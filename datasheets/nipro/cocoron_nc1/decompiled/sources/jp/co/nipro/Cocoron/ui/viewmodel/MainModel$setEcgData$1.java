package jp.co.nipro.cocoron.ui.viewmodel;

import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.entity.ECGData;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: MainModel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.ui.viewmodel.MainModel$setEcgData$1", f = "MainModel.kt", i = {}, l = {240}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class MainModel$setEcgData$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Ref.LongRef $date;
    final /* synthetic */ Ref.LongRef $range;
    Object L$0;
    int label;
    final /* synthetic */ MainModel this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    MainModel$setEcgData$1(MainModel mainModel, Ref.LongRef longRef, Ref.LongRef longRef2, Continuation continuation) {
        super(2, continuation);
        this.this$0 = mainModel;
        this.$date = longRef;
        this.$range = longRef2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new MainModel$setEcgData$1(this.this$0, this.$date, this.$range, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((MainModel$setEcgData$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        MainModel mainModel;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            MainModel mainModel2 = this.this$0;
            DatabaseHelper companion = DatabaseHelper.INSTANCE.getInstance();
            long j = this.$date.element;
            long j2 = this.$range.element;
            this.L$0 = mainModel2;
            this.label = 1;
            Object ecgData = companion.getEcgData(j, j2, this);
            if (ecgData == coroutine_suspended) {
                return coroutine_suspended;
            }
            mainModel = mainModel2;
            obj = ecgData;
        } else {
            if (i != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            mainModel = (MainModel) this.L$0;
            ResultKt.throwOnFailure(obj);
        }
        mainModel.setHisEcgData((ECGData) obj);
        this.this$0.clearEcgView();
        return Unit.INSTANCE;
    }
}
