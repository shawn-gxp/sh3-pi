package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Ref;
import kotlinx.coroutines.CompletableDeferred;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: Share.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharingDeferred$1", f = "Share.kt", i = {0, 0, 0}, l = {415}, m = "invokeSuspend", n = {"$this$launch", "state", "$this$collect$iv"}, s = {"L$0", "L$1", "L$2"})
/* loaded from: classes.dex */
final class FlowKt__ShareKt$launchSharingDeferred$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ CompletableDeferred $result;
    final /* synthetic */ Flow $upstream;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    private CoroutineScope p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowKt__ShareKt$launchSharingDeferred$1(Flow flow, CompletableDeferred completableDeferred, Continuation continuation) {
        super(2, continuation);
        this.$upstream = flow;
        this.$result = completableDeferred;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        FlowKt__ShareKt$launchSharingDeferred$1 flowKt__ShareKt$launchSharingDeferred$1 = new FlowKt__ShareKt$launchSharingDeferred$1(this.$upstream, this.$result, continuation);
        flowKt__ShareKt$launchSharingDeferred$1.p$ = (CoroutineScope) obj;
        return flowKt__ShareKt$launchSharingDeferred$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((FlowKt__ShareKt$launchSharingDeferred$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Type inference failed for: r3v1, types: [T, kotlinx.coroutines.flow.MutableStateFlow] */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        try {
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                final CoroutineScope coroutineScope = this.p$;
                final Ref.ObjectRef objectRef = new Ref.ObjectRef();
                objectRef.element = (MutableStateFlow) 0;
                Flow flow = this.$upstream;
                FlowCollector flowCollector = new FlowCollector<T>() { // from class: kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharingDeferred$1$invokeSuspend$$inlined$collect$1
                    /* JADX WARN: Type inference failed for: r3v1, types: [T, kotlinx.coroutines.flow.MutableStateFlow] */
                    @Override // kotlinx.coroutines.flow.FlowCollector
                    public Object emit(Object obj2, Continuation continuation) {
                        Unit unit;
                        MutableStateFlow mutableStateFlow = (MutableStateFlow) objectRef.element;
                        if (mutableStateFlow != null) {
                            mutableStateFlow.setValue(obj2);
                            unit = Unit.INSTANCE;
                        } else {
                            Ref.ObjectRef objectRef2 = objectRef;
                            ?? r3 = (T) StateFlowKt.MutableStateFlow(obj2);
                            FlowKt__ShareKt$launchSharingDeferred$1.this.$result.complete(FlowKt.asStateFlow(r3));
                            Unit unit2 = Unit.INSTANCE;
                            objectRef2.element = r3;
                            unit = Unit.INSTANCE;
                        }
                        return unit == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? unit : Unit.INSTANCE;
                    }
                };
                this.L$0 = coroutineScope;
                this.L$1 = objectRef;
                this.L$2 = flow;
                this.label = 1;
                if (flow.collect(flowCollector, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            } else {
                if (i != 1) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                ResultKt.throwOnFailure(obj);
            }
            return Unit.INSTANCE;
        } catch (Throwable th) {
            this.$result.completeExceptionally(th);
            throw th;
        }
    }
}
