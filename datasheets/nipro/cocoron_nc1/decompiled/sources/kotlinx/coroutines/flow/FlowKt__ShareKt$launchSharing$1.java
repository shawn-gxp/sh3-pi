package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.FlowKt;

/* compiled from: Share.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharing$1", f = "Share.kt", i = {0, 1, 2, 3}, l = {206, 210, 211, 217}, m = "invokeSuspend", n = {"$this$launch", "$this$launch", "$this$launch", "$this$launch"}, s = {"L$0", "L$0", "L$0", "L$0"})
/* loaded from: classes.dex */
final class FlowKt__ShareKt$launchSharing$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Object $initialValue;
    final /* synthetic */ MutableSharedFlow $shared;
    final /* synthetic */ SharingStarted $started;
    final /* synthetic */ Flow $upstream;
    Object L$0;
    int label;
    private CoroutineScope p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowKt__ShareKt$launchSharing$1(SharingStarted sharingStarted, Flow flow, MutableSharedFlow mutableSharedFlow, Object obj, Continuation continuation) {
        super(2, continuation);
        this.$started = sharingStarted;
        this.$upstream = flow;
        this.$shared = mutableSharedFlow;
        this.$initialValue = obj;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        FlowKt__ShareKt$launchSharing$1 flowKt__ShareKt$launchSharing$1 = new FlowKt__ShareKt$launchSharing$1(this.$started, this.$upstream, this.$shared, this.$initialValue, continuation);
        flowKt__ShareKt$launchSharing$1.p$ = (CoroutineScope) obj;
        return flowKt__ShareKt$launchSharing$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((FlowKt__ShareKt$launchSharing$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0083 A[RETURN] */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        CoroutineScope coroutineScope;
        Flow flow;
        MutableSharedFlow mutableSharedFlow;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            coroutineScope = this.p$;
            if (this.$started == SharingStarted.INSTANCE.getEagerly()) {
                Flow flow2 = this.$upstream;
                MutableSharedFlow mutableSharedFlow2 = this.$shared;
                this.L$0 = coroutineScope;
                this.label = 1;
                if (flow2.collect(mutableSharedFlow2, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            } else if (this.$started == SharingStarted.INSTANCE.getLazily()) {
                StateFlow<Integer> subscriptionCount = this.$shared.getSubscriptionCount();
                AnonymousClass1 anonymousClass1 = new AnonymousClass1(null);
                this.L$0 = coroutineScope;
                this.label = 2;
                if (FlowKt.first(subscriptionCount, anonymousClass1, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                flow = this.$upstream;
                mutableSharedFlow = this.$shared;
                this.L$0 = coroutineScope;
                this.label = 3;
                if (flow.collect(mutableSharedFlow, this) == coroutine_suspended) {
                }
            } else {
                Flow distinctUntilChanged = FlowKt.distinctUntilChanged(this.$started.command(this.$shared.getSubscriptionCount()));
                AnonymousClass2 anonymousClass2 = new AnonymousClass2(null);
                this.L$0 = coroutineScope;
                this.label = 4;
                if (FlowKt.collectLatest(distinctUntilChanged, anonymousClass2, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            }
            return Unit.INSTANCE;
        }
        if (i != 1) {
            if (i == 2) {
                coroutineScope = (CoroutineScope) this.L$0;
                ResultKt.throwOnFailure(obj);
                flow = this.$upstream;
                mutableSharedFlow = this.$shared;
                this.L$0 = coroutineScope;
                this.label = 3;
                if (flow.collect(mutableSharedFlow, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                return Unit.INSTANCE;
            }
            if (i != 3 && i != 4) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
        ResultKt.throwOnFailure(obj);
        return Unit.INSTANCE;
    }

    /* compiled from: Share.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "T", "it", "", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
    @DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharing$1$1", f = "Share.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharing$1$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<Integer, Continuation<? super Boolean>, Object> {
        int label;
        private int p$0;

        AnonymousClass1(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass1 anonymousClass1 = new AnonymousClass1(continuation);
            Number number = (Number) obj;
            number.intValue();
            anonymousClass1.p$0 = number.intValue();
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(Integer num, Continuation<? super Boolean> continuation) {
            return ((AnonymousClass1) create(num, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            return Boxing.boxBoolean(this.p$0 > 0);
        }
    }

    /* compiled from: Share.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "T", "it", "Lkotlinx/coroutines/flow/SharingCommand;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 0})
    @DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharing$1$2", f = "Share.kt", i = {0}, l = {219}, m = "invokeSuspend", n = {"it"}, s = {"L$0"})
    /* renamed from: kotlinx.coroutines.flow.FlowKt__ShareKt$launchSharing$1$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<SharingCommand, Continuation<? super Unit>, Object> {
        Object L$0;
        int label;
        private SharingCommand p$0;

        AnonymousClass2(Continuation continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass2 anonymousClass2 = FlowKt__ShareKt$launchSharing$1.this.new AnonymousClass2(continuation);
            anonymousClass2.p$0 = (SharingCommand) obj;
            return anonymousClass2;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(SharingCommand sharingCommand, Continuation<? super Unit> continuation) {
            return ((AnonymousClass2) create(sharingCommand, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                SharingCommand sharingCommand = this.p$0;
                int i2 = FlowKt.WhenMappings.$EnumSwitchMapping$0[sharingCommand.ordinal()];
                if (i2 == 1) {
                    Flow flow = FlowKt__ShareKt$launchSharing$1.this.$upstream;
                    MutableSharedFlow mutableSharedFlow = FlowKt__ShareKt$launchSharing$1.this.$shared;
                    this.L$0 = sharingCommand;
                    this.label = 1;
                    if (flow.collect(mutableSharedFlow, this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                } else if (i2 == 3) {
                    if (FlowKt__ShareKt$launchSharing$1.this.$initialValue == SharedFlowKt.NO_VALUE) {
                        FlowKt__ShareKt$launchSharing$1.this.$shared.resetReplayCache();
                    } else {
                        FlowKt__ShareKt$launchSharing$1.this.$shared.tryEmit(FlowKt__ShareKt$launchSharing$1.this.$initialValue);
                    }
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
