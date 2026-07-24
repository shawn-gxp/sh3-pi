package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.internal.InlineMarker;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: SafeCollector.common.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0019\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002*\u0001\u0000\b\n\u0018\u00002\b\u0012\u0004\u0012\u00028\u00000\u0001J\u001f\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00028\u00000\u0005H\u0096@ø\u0001\u0000¢\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0007¸\u0006\u0000"}, d2 = {"kotlinx/coroutines/flow/internal/SafeCollector_commonKt$unsafeFlow$1", "Lkotlinx/coroutines/flow/Flow;", "collect", "", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "kotlinx-coroutines-core"}, k = 1, mv = {1, 4, 0})
/* loaded from: classes.dex */
public final class FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1<T> implements Flow<T> {
    final /* synthetic */ Function4 $predicate$inlined;
    final /* synthetic */ Flow $this_retryWhen$inlined;

    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001a\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\u0010\u0000\u001a\u0004\u0018\u00010\u0001\"\u0004\b\u0000\u0010\u00022\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u0002H\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0096@¨\u0006\b"}, d2 = {"collect", "", "T", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "continuation", "Lkotlin/coroutines/Continuation;", "", "kotlinx/coroutines/flow/internal/SafeCollector_commonKt$unsafeFlow$1$collect$1"}, k = 3, mv = {1, 4, 0})
    @DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1", f = "Errors.kt", i = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}, l = {117, 119}, m = "collect", n = {"this", "collector", "continuation", "$receiver", "attempt", "shallRetry", "this", "collector", "continuation", "$receiver", "attempt", "cause"}, s = {"L$0", "L$1", "L$2", "L$3", "J$0", "I$0", "L$0", "L$1", "L$2", "L$3", "J$0", "L$4"})
    /* renamed from: kotlinx.coroutines.flow.FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1$1, reason: invalid class name */
    public static final class AnonymousClass1 extends ContinuationImpl {
        int I$0;
        long J$0;
        Object L$0;
        Object L$1;
        Object L$2;
        Object L$3;
        Object L$4;
        int label;
        /* synthetic */ Object result;

        public AnonymousClass1(Continuation continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1.this.collect(null, this);
        }
    }

    public FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1(Flow flow, Function4 function4) {
        this.$this_retryWhen$inlined = flow;
        this.$predicate$inlined = function4;
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x00c4  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x00d2  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x00d5  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0088 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0089  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0096  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00c9  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:25:0x0094 -> B:14:0x00ca). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:29:0x00b9 -> B:11:0x00bc). Please report as a decompilation issue!!! */
    @Override // kotlinx.coroutines.flow.Flow
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Object collect(FlowCollector flowCollector, Continuation continuation) {
        AnonymousClass1 anonymousClass1;
        int i;
        long j;
        FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1<T> flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1;
        Object obj;
        AnonymousClass1 anonymousClass12;
        Object obj2;
        FlowCollector flowCollector2;
        int i2;
        FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1<T> flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12;
        FlowCollector flowCollector3;
        Object obj3;
        FlowCollector flowCollector4;
        Throwable th;
        Object catchImpl;
        if (continuation instanceof AnonymousClass1) {
            anonymousClass1 = (AnonymousClass1) continuation;
            if ((anonymousClass1.label & Integer.MIN_VALUE) != 0) {
                anonymousClass1.label -= Integer.MIN_VALUE;
                Object obj4 = anonymousClass1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                i = anonymousClass1.label;
                if (i != 0) {
                    ResultKt.throwOnFailure(obj4);
                    j = 0;
                    flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1 = this;
                    obj = coroutine_suspended;
                    anonymousClass12 = anonymousClass1;
                    obj2 = anonymousClass1;
                    flowCollector2 = flowCollector;
                    Flow flow = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1.$this_retryWhen$inlined;
                    anonymousClass12.L$0 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1;
                    anonymousClass12.L$1 = flowCollector2;
                    anonymousClass12.L$2 = obj2;
                    anonymousClass12.L$3 = flowCollector;
                    anonymousClass12.J$0 = j;
                    anonymousClass12.I$0 = 0;
                    anonymousClass12.label = 1;
                    catchImpl = FlowKt.catchImpl(flow, flowCollector, anonymousClass12);
                    if (catchImpl != obj) {
                    }
                } else if (i == 1) {
                    i2 = anonymousClass1.I$0;
                    j = anonymousClass1.J$0;
                    flowCollector4 = (FlowCollector) anonymousClass1.L$3;
                    obj3 = (Continuation) anonymousClass1.L$2;
                    flowCollector3 = (FlowCollector) anonymousClass1.L$1;
                    flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12 = (FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1) anonymousClass1.L$0;
                    ResultKt.throwOnFailure(obj4);
                    th = (Throwable) obj4;
                    if (th != null) {
                    }
                    flowCollector2 = flowCollector3;
                    Object obj5 = coroutine_suspended;
                    anonymousClass12 = anonymousClass1;
                    obj2 = obj3;
                    long j2 = j;
                    if (i2 == 0) {
                    }
                } else {
                    if (i != 2) {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                    Throwable th2 = (Throwable) anonymousClass1.L$4;
                    j = anonymousClass1.J$0;
                    flowCollector4 = (FlowCollector) anonymousClass1.L$3;
                    obj3 = (Continuation) anonymousClass1.L$2;
                    flowCollector3 = (FlowCollector) anonymousClass1.L$1;
                    flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12 = (FlowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1) anonymousClass1.L$0;
                    ResultKt.throwOnFailure(obj4);
                    if (!((Boolean) obj4).booleanValue()) {
                        j++;
                        i2 = 1;
                        flowCollector2 = flowCollector3;
                        Object obj52 = coroutine_suspended;
                        anonymousClass12 = anonymousClass1;
                        obj2 = obj3;
                        long j22 = j;
                        if (i2 == 0) {
                            return Unit.INSTANCE;
                        }
                        flowCollector = flowCollector4;
                        obj = obj52;
                        j = j22;
                        flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12;
                        Flow flow2 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1.$this_retryWhen$inlined;
                        anonymousClass12.L$0 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1;
                        anonymousClass12.L$1 = flowCollector2;
                        anonymousClass12.L$2 = obj2;
                        anonymousClass12.L$3 = flowCollector;
                        anonymousClass12.J$0 = j;
                        anonymousClass12.I$0 = 0;
                        anonymousClass12.label = 1;
                        catchImpl = FlowKt.catchImpl(flow2, flowCollector, anonymousClass12);
                        if (catchImpl != obj) {
                            return obj;
                        }
                        Object obj6 = obj;
                        flowCollector4 = flowCollector;
                        i2 = 0;
                        flowCollector3 = flowCollector2;
                        obj4 = catchImpl;
                        flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$1;
                        obj3 = obj2;
                        anonymousClass1 = anonymousClass12;
                        coroutine_suspended = obj6;
                        th = (Throwable) obj4;
                        if (th != null) {
                            Function4 function4 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12.$predicate$inlined;
                            Long boxLong = Boxing.boxLong(j);
                            anonymousClass1.L$0 = flowKt__ErrorsKt$retryWhen$$inlined$unsafeFlow$12;
                            anonymousClass1.L$1 = flowCollector3;
                            anonymousClass1.L$2 = obj3;
                            anonymousClass1.L$3 = flowCollector4;
                            anonymousClass1.J$0 = j;
                            anonymousClass1.L$4 = th;
                            anonymousClass1.label = 2;
                            InlineMarker.mark(6);
                            Object invoke = function4.invoke(flowCollector4, th, boxLong, anonymousClass1);
                            InlineMarker.mark(7);
                            if (invoke == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            obj4 = invoke;
                            th2 = th;
                            if (!((Boolean) obj4).booleanValue()) {
                                throw th2;
                            }
                        }
                        flowCollector2 = flowCollector3;
                        Object obj522 = coroutine_suspended;
                        anonymousClass12 = anonymousClass1;
                        obj2 = obj3;
                        long j222 = j;
                        if (i2 == 0) {
                        }
                    }
                }
            }
        }
        anonymousClass1 = new AnonymousClass1(continuation);
        Object obj42 = anonymousClass1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        i = anonymousClass1.label;
        if (i != 0) {
        }
    }
}
