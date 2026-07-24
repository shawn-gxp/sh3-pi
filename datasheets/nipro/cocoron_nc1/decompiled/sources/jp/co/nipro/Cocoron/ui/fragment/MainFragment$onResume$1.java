package jp.co.nipro.cocoron.ui.fragment;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import kotlinx.coroutines.DelayKt;

/* compiled from: MainFragment.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.ui.fragment.MainFragment$onResume$1", f = "MainFragment.kt", i = {}, l = {617}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class MainFragment$onResume$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int label;
    final /* synthetic */ MainFragment this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    MainFragment$onResume$1(MainFragment mainFragment, Continuation continuation) {
        super(2, continuation);
        this.this$0 = mainFragment;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new MainFragment$onResume$1(this.this$0, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((MainFragment$onResume$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            ConstraintLayout constraintLayout = this.this$0.getViewBinding().sendIntervalPickerArea;
            Intrinsics.checkNotNullExpressionValue(constraintLayout, "viewBinding.sendIntervalPickerArea");
            constraintLayout.setAlpha(0.0f);
            ConstraintLayout constraintLayout2 = this.this$0.getViewBinding().sendIntervalPickerArea;
            Intrinsics.checkNotNullExpressionValue(constraintLayout2, "viewBinding.sendIntervalPickerArea");
            constraintLayout2.setVisibility(0);
            this.label = 1;
            if (DelayKt.delay(100L, this) == coroutine_suspended) {
                return coroutine_suspended;
            }
        } else {
            if (i != 1) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
        }
        ConstraintLayout constraintLayout3 = this.this$0.getViewBinding().sendIntervalPickerArea;
        Intrinsics.checkNotNullExpressionValue(constraintLayout3, "viewBinding.sendIntervalPickerArea");
        constraintLayout3.setVisibility(8);
        ConstraintLayout constraintLayout4 = this.this$0.getViewBinding().sendIntervalPickerArea;
        Intrinsics.checkNotNullExpressionValue(constraintLayout4, "viewBinding.sendIntervalPickerArea");
        constraintLayout4.setAlpha(1.0f);
        return Unit.INSTANCE;
    }
}
