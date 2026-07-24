package androidx.room;

import androidx.room.InvalidationTracker;
import java.util.Set;
import java.util.concurrent.Callable;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelIterator;
import kotlinx.coroutines.channels.ChannelKt;
import kotlinx.coroutines.flow.FlowCollector;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: CoroutinesRoom.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u0010\u0012\f\u0012\n \u0004*\u0004\u0018\u0001H\u0002H\u00020\u0003H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, d2 = {"<anonymous>", "", "R", "Lkotlinx/coroutines/flow/FlowCollector;", "kotlin.jvm.PlatformType", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 1, 15})
@DebugMetadata(c = "androidx.room.CoroutinesRoom$Companion$createFlow$1", f = "CoroutinesRoom.kt", i = {0, 0, 0, 0, 0}, l = {75}, m = "invokeSuspend", n = {"$this$flow", "observerChannel", "observer", "flowContext", "queryContext"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4"})
/* loaded from: classes.dex */
final class CoroutinesRoom$Companion$createFlow$1<R> extends SuspendLambda implements Function2<FlowCollector<? super R>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Callable $callable;
    final /* synthetic */ RoomDatabase $db;
    final /* synthetic */ boolean $inTransaction;
    final /* synthetic */ String[] $tableNames;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    int label;
    private FlowCollector p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    CoroutinesRoom$Companion$createFlow$1(String[] strArr, boolean z, RoomDatabase roomDatabase, Callable callable, Continuation continuation) {
        super(2, continuation);
        this.$tableNames = strArr;
        this.$inTransaction = z;
        this.$db = roomDatabase;
        this.$callable = callable;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkParameterIsNotNull(completion, "completion");
        CoroutinesRoom$Companion$createFlow$1 coroutinesRoom$Companion$createFlow$1 = new CoroutinesRoom$Companion$createFlow$1(this.$tableNames, this.$inTransaction, this.$db, this.$callable, completion);
        coroutinesRoom$Companion$createFlow$1.p$ = (FlowCollector) obj;
        return coroutinesRoom$Companion$createFlow$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((CoroutinesRoom$Companion$createFlow$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Type inference failed for: r12v0, types: [androidx.room.CoroutinesRoom$Companion$createFlow$1$observer$1, java.lang.Object] */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            FlowCollector flowCollector = this.p$;
            final Channel Channel = ChannelKt.Channel(-1);
            final String[] strArr = this.$tableNames;
            ?? r12 = new InvalidationTracker.Observer(strArr) { // from class: androidx.room.CoroutinesRoom$Companion$createFlow$1$observer$1
                @Override // androidx.room.InvalidationTracker.Observer
                public void onInvalidated(Set<String> tables) {
                    Intrinsics.checkParameterIsNotNull(tables, "tables");
                    Channel.offer(Unit.INSTANCE);
                }
            };
            Channel.offer(Unit.INSTANCE);
            CoroutineContext coroutineContext = get$context();
            CoroutineDispatcher transactionDispatcher = this.$inTransaction ? CoroutinesRoomKt.getTransactionDispatcher(this.$db) : CoroutinesRoomKt.getQueryDispatcher(this.$db);
            AnonymousClass1 anonymousClass1 = new AnonymousClass1(flowCollector, r12, Channel, coroutineContext, null);
            this.L$0 = flowCollector;
            this.L$1 = Channel;
            this.L$2 = r12;
            this.L$3 = coroutineContext;
            this.L$4 = transactionDispatcher;
            this.label = 1;
            if (BuildersKt.withContext(transactionDispatcher, anonymousClass1, this) == coroutine_suspended) {
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

    /* compiled from: CoroutinesRoom.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "R", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 1, 15})
    @DebugMetadata(c = "androidx.room.CoroutinesRoom$Companion$createFlow$1$1", f = "CoroutinesRoom.kt", i = {0, 1, 1, 1}, l = {80, 82}, m = "invokeSuspend", n = {"$this$withContext", "$this$withContext", "signal", "result"}, s = {"L$0", "L$0", "L$1", "L$3"})
    /* renamed from: androidx.room.CoroutinesRoom$Companion$createFlow$1$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ CoroutineContext $flowContext;
        final /* synthetic */ CoroutinesRoom$Companion$createFlow$1$observer$1 $observer;
        final /* synthetic */ Channel $observerChannel;
        final /* synthetic */ FlowCollector $this_flow;
        Object L$0;
        Object L$1;
        Object L$2;
        Object L$3;
        int label;
        private CoroutineScope p$;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(FlowCollector flowCollector, CoroutinesRoom$Companion$createFlow$1$observer$1 coroutinesRoom$Companion$createFlow$1$observer$1, Channel channel, CoroutineContext coroutineContext, Continuation continuation) {
            super(2, continuation);
            this.$this_flow = flowCollector;
            this.$observer = coroutinesRoom$Companion$createFlow$1$observer$1;
            this.$observerChannel = channel;
            this.$flowContext = coroutineContext;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkParameterIsNotNull(completion, "completion");
            AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.$this_flow, this.$observer, this.$observerChannel, this.$flowContext, completion);
            anonymousClass1.p$ = (CoroutineScope) obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x005c A[RETURN] */
        /* JADX WARN: Removed duplicated region for block: B:15:0x005d  */
        /* JADX WARN: Removed duplicated region for block: B:18:0x0068 A[Catch: all -> 0x00a5, TRY_LEAVE, TryCatch #0 {all -> 0x00a5, blocks: (B:11:0x0050, B:16:0x0060, B:18:0x0068), top: B:10:0x0050 }] */
        /* JADX WARN: Removed duplicated region for block: B:22:0x0093  */
        /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:21:0x0091 -> B:10:0x0050). Please report as a decompilation issue!!! */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public final Object invokeSuspend(Object obj) {
            AnonymousClass1 anonymousClass1;
            CoroutineScope coroutineScope;
            ChannelIterator it;
            Object hasNext;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            try {
                try {
                    if (i == 0) {
                        ResultKt.throwOnFailure(obj);
                        coroutineScope = this.p$;
                        CoroutinesRoom$Companion$createFlow$1.this.$db.getInvalidationTracker().addObserver(this.$observer);
                        it = this.$observerChannel.iterator();
                    } else if (i == 1) {
                        it = (ChannelIterator) this.L$1;
                        CoroutineScope coroutineScope2 = (CoroutineScope) this.L$0;
                        ResultKt.throwOnFailure(obj);
                        CoroutineScope coroutineScope3 = coroutineScope2;
                        anonymousClass1 = this;
                        if (!((Boolean) obj).booleanValue()) {
                            Unit unit = (Unit) it.next();
                            Object call = CoroutinesRoom$Companion$createFlow$1.this.$callable.call();
                            CoroutineContext coroutineContext = anonymousClass1.$flowContext;
                            C00051 c00051 = anonymousClass1.new C00051(call, null);
                            anonymousClass1.L$0 = coroutineScope3;
                            anonymousClass1.L$1 = unit;
                            anonymousClass1.L$2 = it;
                            anonymousClass1.L$3 = call;
                            anonymousClass1.label = 2;
                            if (BuildersKt.withContext(coroutineContext, c00051, anonymousClass1) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            coroutineScope = coroutineScope3;
                            anonymousClass1.L$0 = coroutineScope;
                            anonymousClass1.L$1 = it;
                            anonymousClass1.label = 1;
                            hasNext = it.hasNext(anonymousClass1);
                            if (hasNext != coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            coroutineScope3 = coroutineScope;
                            obj = hasNext;
                            if (!((Boolean) obj).booleanValue()) {
                                CoroutinesRoom$Companion$createFlow$1.this.$db.getInvalidationTracker().removeObserver(anonymousClass1.$observer);
                                return Unit.INSTANCE;
                            }
                        }
                    } else {
                        if (i != 2) {
                            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }
                        it = (ChannelIterator) this.L$2;
                        CoroutineScope coroutineScope4 = (CoroutineScope) this.L$0;
                        ResultKt.throwOnFailure(obj);
                        coroutineScope = coroutineScope4;
                    }
                    anonymousClass1.L$0 = coroutineScope;
                    anonymousClass1.L$1 = it;
                    anonymousClass1.label = 1;
                    hasNext = it.hasNext(anonymousClass1);
                    if (hasNext != coroutine_suspended) {
                    }
                } catch (Throwable th) {
                    th = th;
                    CoroutinesRoom$Companion$createFlow$1.this.$db.getInvalidationTracker().removeObserver(anonymousClass1.$observer);
                    throw th;
                }
                anonymousClass1 = this;
            } catch (Throwable th2) {
                th = th2;
                anonymousClass1 = this;
            }
        }

        /* compiled from: CoroutinesRoom.kt */
        @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "R", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 1, 15})
        @DebugMetadata(c = "androidx.room.CoroutinesRoom$Companion$createFlow$1$1$1", f = "CoroutinesRoom.kt", i = {0}, l = {82}, m = "invokeSuspend", n = {"$this$withContext"}, s = {"L$0"})
        /* renamed from: androidx.room.CoroutinesRoom$Companion$createFlow$1$1$1, reason: invalid class name and collision with other inner class name */
        static final class C00051 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
            final /* synthetic */ Object $result;
            Object L$0;
            int label;
            private CoroutineScope p$;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            C00051(Object obj, Continuation continuation) {
                super(2, continuation);
                this.$result = obj;
            }

            @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
            public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
                Intrinsics.checkParameterIsNotNull(completion, "completion");
                C00051 c00051 = AnonymousClass1.this.new C00051(this.$result, completion);
                c00051.p$ = (CoroutineScope) obj;
                return c00051;
            }

            @Override // kotlin.jvm.functions.Function2
            public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
                return ((C00051) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
            }

            @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
            public final Object invokeSuspend(Object obj) {
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                int i = this.label;
                if (i == 0) {
                    ResultKt.throwOnFailure(obj);
                    CoroutineScope coroutineScope = this.p$;
                    FlowCollector flowCollector = AnonymousClass1.this.$this_flow;
                    Object obj2 = this.$result;
                    this.L$0 = coroutineScope;
                    this.label = 1;
                    if (flowCollector.emit(obj2, this) == coroutine_suspended) {
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
}
