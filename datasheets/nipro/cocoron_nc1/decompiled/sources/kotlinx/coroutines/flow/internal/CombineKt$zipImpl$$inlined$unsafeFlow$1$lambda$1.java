package kotlinx.coroutines.flow.internal;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.InlineMarker;
import kotlinx.coroutines.CompletableJob;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt__JobKt;
import kotlinx.coroutines.channels.ChannelsKt;
import kotlinx.coroutines.channels.ProduceKt;
import kotlinx.coroutines.channels.ProducerScope;
import kotlinx.coroutines.channels.ReceiveChannel;
import kotlinx.coroutines.channels.SendChannel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.internal.ThreadContextKt;

/* compiled from: Combine.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003\"\u0004\b\u0002\u0010\u0004*\u00020\u0005H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007¨\u0006\b"}, d2 = {"<anonymous>", "", "T1", "T2", "R", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "kotlinx/coroutines/flow/internal/CombineKt$zipImpl$1$1"}, k = 3, mv = {1, 4, 0})
/* loaded from: classes.dex */
final class CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ FlowCollector $this_unsafeFlow;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    int label;
    private CoroutineScope p$;
    final /* synthetic */ CombineKt$zipImpl$$inlined$unsafeFlow$1 this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1(FlowCollector flowCollector, Continuation continuation, CombineKt$zipImpl$$inlined$unsafeFlow$1 combineKt$zipImpl$$inlined$unsafeFlow$1) {
        super(2, continuation);
        this.$this_unsafeFlow = flowCollector;
        this.this$0 = combineKt$zipImpl$$inlined$unsafeFlow$1;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1 combineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1 = new CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1(this.$this_unsafeFlow, continuation, this.this$0);
        combineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.p$ = (CoroutineScope) obj;
        return combineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* compiled from: Combine.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0016\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003\"\u0004\b\u0002\u0010\u0004*\b\u0012\u0004\u0012\u00020\u00060\u0005H\u008a@¢\u0006\u0004\b\u0007\u0010\b¨\u0006\t"}, d2 = {"<anonymous>", "", "T1", "T2", "R", "Lkotlinx/coroutines/channels/ProducerScope;", "", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "kotlinx/coroutines/flow/internal/CombineKt$zipImpl$1$1$second$1"}, k = 3, mv = {1, 4, 0})
    /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<ProducerScope<? super Object>, Continuation<? super Unit>, Object> {
        Object L$0;
        Object L$1;
        int label;
        private ProducerScope p$;

        AnonymousClass1(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass1 anonymousClass1 = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.new AnonymousClass1(continuation);
            anonymousClass1.p$ = (ProducerScope) obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(ProducerScope<? super Object> producerScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                final ProducerScope producerScope = this.p$;
                Flow flow = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.this$0.$flow2$inlined;
                FlowCollector flowCollector = new FlowCollector<T2>() { // from class: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$.inlined.unsafeFlow.1.lambda.1.1.1
                    @Override // kotlinx.coroutines.flow.FlowCollector
                    public Object emit(Object obj2, Continuation continuation) {
                        SendChannel channel = ProducerScope.this.getChannel();
                        if (obj2 == null) {
                            obj2 = NullSurrogateKt.NULL;
                        }
                        Object send = channel.send(obj2, continuation);
                        return send == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? send : Unit.INSTANCE;
                    }
                };
                this.L$0 = producerScope;
                this.L$1 = flow;
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
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x00ac, code lost:
    
        kotlinx.coroutines.channels.ReceiveChannel.DefaultImpls.cancel$default(r1, (java.util.concurrent.CancellationException) null, 1, (java.lang.Object) null);
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x00c3, code lost:
    
        return kotlin.Unit.INSTANCE;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x00be, code lost:
    
        if (r1.isClosedForReceive() == false) goto L21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x00aa, code lost:
    
        if (r1.isClosedForReceive() == false) goto L21;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [int] */
    /* JADX WARN: Type inference failed for: r1v1, types: [kotlinx.coroutines.channels.ReceiveChannel] */
    /* JADX WARN: Type inference failed for: r1v6 */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        final CompletableJob Job$default;
        ReceiveChannel receiveChannel;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        ?? r1 = this.label;
        try {
            if (r1 == 0) {
                ResultKt.throwOnFailure(obj);
                CoroutineScope coroutineScope = this.p$;
                ReceiveChannel produce$default = ProduceKt.produce$default(coroutineScope, null, 0, new AnonymousClass1(null), 3, null);
                Job$default = JobKt__JobKt.Job$default((Job) null, 1, (Object) null);
                Objects.requireNonNull(produce$default, "null cannot be cast to non-null type kotlinx.coroutines.channels.SendChannel<*>");
                ((SendChannel) produce$default).invokeOnClose(new Function1<Throwable, Unit>() { // from class: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.2
                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    {
                        super(1);
                    }

                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
                        invoke2(th);
                        return Unit.INSTANCE;
                    }

                    /* renamed from: invoke, reason: avoid collision after fix types in other method */
                    public final void invoke2(Throwable th) {
                        if (Job$default.isActive()) {
                            Job$default.cancel((CancellationException) new AbortFlowException(CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.$this_unsafeFlow));
                        }
                    }
                });
                try {
                    CoroutineContext coroutineContext = coroutineScope.getCoroutineContext();
                    Object threadContextElements = ThreadContextKt.threadContextElements(coroutineContext);
                    CoroutineContext plus = coroutineScope.getCoroutineContext().plus(Job$default);
                    Unit unit = Unit.INSTANCE;
                    AnonymousClass3 anonymousClass3 = new AnonymousClass3(coroutineContext, threadContextElements, produce$default, null);
                    this.L$0 = coroutineScope;
                    this.L$1 = produce$default;
                    this.L$2 = Job$default;
                    this.L$3 = coroutineContext;
                    this.L$4 = threadContextElements;
                    this.label = 1;
                    if (ChannelFlowKt.withContextUndispatched$default(plus, unit, null, anonymousClass3, this, 4, null) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    receiveChannel = produce$default;
                } catch (AbortFlowException e) {
                    e = e;
                    receiveChannel = produce$default;
                    FlowExceptions_commonKt.checkOwnership(e, this.$this_unsafeFlow);
                } catch (Throwable th) {
                    th = th;
                    r1 = produce$default;
                    if (!r1.isClosedForReceive()) {
                        ReceiveChannel.DefaultImpls.cancel$default((ReceiveChannel) r1, (CancellationException) null, 1, (Object) null);
                    }
                    throw th;
                }
            } else if (r1 == 1) {
                receiveChannel = (ReceiveChannel) this.L$1;
                try {
                    ResultKt.throwOnFailure(obj);
                } catch (AbortFlowException e2) {
                    e = e2;
                    FlowExceptions_commonKt.checkOwnership(e, this.$this_unsafeFlow);
                }
            } else {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* compiled from: Combine.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003\"\u0004\b\u0002\u0010\u00042\u0006\u0010\u0005\u001a\u00020\u0001H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007¨\u0006\b"}, d2 = {"<anonymous>", "", "T1", "T2", "R", "it", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "kotlinx/coroutines/flow/internal/CombineKt$zipImpl$1$1$2"}, k = 3, mv = {1, 4, 0})
    /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1$3, reason: invalid class name */
    static final class AnonymousClass3 extends SuspendLambda implements Function2<Unit, Continuation<? super Unit>, Object> {
        final /* synthetic */ Object $cnt;
        final /* synthetic */ CoroutineContext $scopeContext;
        final /* synthetic */ ReceiveChannel $second;
        Object L$0;
        Object L$1;
        int label;
        private Unit p$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass3(CoroutineContext coroutineContext, Object obj, ReceiveChannel receiveChannel, Continuation continuation) {
            super(2, continuation);
            this.$scopeContext = coroutineContext;
            this.$cnt = obj;
            this.$second = receiveChannel;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass3 anonymousClass3 = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.new AnonymousClass3(this.$scopeContext, this.$cnt, this.$second, continuation);
            anonymousClass3.p$0 = (Unit) obj;
            return anonymousClass3;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(Unit unit, Continuation<? super Unit> continuation) {
            return ((AnonymousClass3) create(unit, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                Unit unit = this.p$0;
                Flow flow = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.this$0.$flow$inlined;
                FlowCollector flowCollector = new FlowCollector<T1>() { // from class: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$.inlined.unsafeFlow.1.lambda.1.3.1

                    /* compiled from: Combine.kt */
                    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003\"\u0004\b\u0002\u0010\u00042\u0006\u0010\u0005\u001a\u00020\u0001H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007¨\u0006\t"}, d2 = {"<anonymous>", "", "T1", "T2", "R", "it", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "kotlinx/coroutines/flow/internal/CombineKt$zipImpl$1$1$2$1$1", "kotlinx/coroutines/flow/internal/CombineKt$zipImpl$1$1$2$invokeSuspend$$inlined$collect$1$lambda$1"}, k = 3, mv = {1, 4, 0})
                    /* renamed from: kotlinx.coroutines.flow.internal.CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1$3$1$1, reason: invalid class name and collision with other inner class name */
                    static final class C00081 extends SuspendLambda implements Function2<Unit, Continuation<? super Unit>, Object> {
                        final /* synthetic */ Object $value;
                        Object L$0;
                        Object L$1;
                        Object L$2;
                        int label;
                        private Unit p$0;
                        final /* synthetic */ AnonymousClass1 this$0;

                        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                        C00081(Object obj, Continuation continuation, AnonymousClass1 anonymousClass1) {
                            super(2, continuation);
                            this.$value = obj;
                            this.this$0 = anonymousClass1;
                        }

                        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
                            C00081 c00081 = new C00081(this.$value, continuation, this.this$0);
                            c00081.p$0 = (Unit) obj;
                            return c00081;
                        }

                        @Override // kotlin.jvm.functions.Function2
                        public final Object invoke(Unit unit, Continuation<? super Unit> continuation) {
                            return ((C00081) create(unit, continuation)).invokeSuspend(Unit.INSTANCE);
                        }

                        /* JADX WARN: Removed duplicated region for block: B:15:0x009a A[RETURN] */
                        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                        /*
                            Code decompiled incorrectly, please refer to instructions dump.
                        */
                        public final Object invokeSuspend(Object obj) {
                            Unit unit;
                            FlowCollector flowCollector;
                            Object obj2;
                            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                            int i = this.label;
                            if (i == 0) {
                                ResultKt.throwOnFailure(obj);
                                Unit unit2 = this.p$0;
                                ReceiveChannel receiveChannel = AnonymousClass3.this.$second;
                                this.L$0 = unit2;
                                this.label = 1;
                                Object receiveOrNull = ChannelsKt.receiveOrNull(receiveChannel, this);
                                if (receiveOrNull == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                unit = unit2;
                                obj = receiveOrNull;
                            } else {
                                if (i != 1) {
                                    if (i != 2) {
                                        if (i == 3) {
                                            ResultKt.throwOnFailure(obj);
                                            return Unit.INSTANCE;
                                        }
                                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                                    }
                                    flowCollector = (FlowCollector) this.L$2;
                                    obj2 = this.L$1;
                                    unit = (Unit) this.L$0;
                                    ResultKt.throwOnFailure(obj);
                                    this.L$0 = unit;
                                    this.L$1 = obj2;
                                    this.label = 3;
                                    if (flowCollector.emit(obj, this) == coroutine_suspended) {
                                        return coroutine_suspended;
                                    }
                                    return Unit.INSTANCE;
                                }
                                Unit unit3 = (Unit) this.L$0;
                                ResultKt.throwOnFailure(obj);
                                unit = unit3;
                            }
                            if (obj == null) {
                                throw new AbortFlowException(CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.$this_unsafeFlow);
                            }
                            flowCollector = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.$this_unsafeFlow;
                            Function3 function3 = CombineKt$zipImpl$$inlined$unsafeFlow$1$lambda$1.this.this$0.$transform$inlined;
                            Object obj3 = this.$value;
                            Object obj4 = obj == NullSurrogateKt.NULL ? null : obj;
                            this.L$0 = unit;
                            this.L$1 = obj;
                            this.L$2 = flowCollector;
                            this.label = 2;
                            InlineMarker.mark(6);
                            InlineMarker.mark(6);
                            Object invoke = function3.invoke(obj3, obj4, this);
                            InlineMarker.mark(7);
                            InlineMarker.mark(7);
                            if (invoke == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            obj2 = obj;
                            obj = invoke;
                            this.L$0 = unit;
                            this.L$1 = obj2;
                            this.label = 3;
                            if (flowCollector.emit(obj, this) == coroutine_suspended) {
                            }
                            return Unit.INSTANCE;
                        }
                    }

                    @Override // kotlinx.coroutines.flow.FlowCollector
                    public Object emit(Object obj2, Continuation continuation) {
                        Object withContextUndispatched = ChannelFlowKt.withContextUndispatched(AnonymousClass3.this.$scopeContext, Unit.INSTANCE, AnonymousClass3.this.$cnt, new C00081(obj2, null, this), continuation);
                        return withContextUndispatched == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? withContextUndispatched : Unit.INSTANCE;
                    }
                };
                this.L$0 = unit;
                this.L$1 = flow;
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
        }
    }
}
