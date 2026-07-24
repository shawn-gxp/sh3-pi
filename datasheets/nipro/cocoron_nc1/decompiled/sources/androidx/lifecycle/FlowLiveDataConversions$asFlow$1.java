package androidx.lifecycle;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.MainCoroutineDispatcher;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelIterator;
import kotlinx.coroutines.channels.ChannelKt;
import kotlinx.coroutines.flow.FlowCollector;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: FlowLiveData.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/flow/FlowCollector;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 1})
@DebugMetadata(c = "androidx.lifecycle.FlowLiveDataConversions$asFlow$1", f = "FlowLiveData.kt", i = {0, 0, 0, 1, 1, 2, 2}, l = {96, 100, 101}, m = "invokeSuspend", n = {"$this$flow", "channel", "observer", "$this$flow", "observer", "$this$flow", "observer"}, s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$0", "L$1"})
/* loaded from: classes.dex */
final class FlowLiveDataConversions$asFlow$1<T> extends SuspendLambda implements Function2<FlowCollector<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ LiveData $this_asFlow;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    FlowLiveDataConversions$asFlow$1(LiveData liveData, Continuation continuation) {
        super(2, continuation);
        this.$this_asFlow = liveData;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        FlowLiveDataConversions$asFlow$1 flowLiveDataConversions$asFlow$1 = new FlowLiveDataConversions$asFlow$1(this.$this_asFlow, completion);
        flowLiveDataConversions$asFlow$1.L$0 = obj;
        return flowLiveDataConversions$asFlow$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((FlowLiveDataConversions$asFlow$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:12:0x009e A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:14:0x009f  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00ac A[Catch: all -> 0x00e4, TRY_LEAVE, TryCatch #1 {all -> 0x00e4, blocks: (B:16:0x00a4, B:18:0x00ac), top: B:15:0x00a4 }] */
    /* JADX WARN: Removed duplicated region for block: B:22:0x00c3  */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v12 */
    /* JADX WARN: Type inference failed for: r4v22 */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v4, types: [androidx.lifecycle.Observer] */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* JADX WARN: Type inference failed for: r4v7 */
    /* JADX WARN: Type inference failed for: r4v8, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r7v10 */
    /* JADX WARN: Type inference failed for: r7v5, types: [java.lang.Object, kotlinx.coroutines.flow.FlowCollector] */
    /* JADX WARN: Type inference failed for: r7v9 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:21:0x00bf -> B:9:0x0090). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        FlowCollector flowCollector;
        Observer<T> observer;
        Channel channel;
        Throwable th;
        FlowLiveDataConversions$asFlow$1<T> flowLiveDataConversions$asFlow$1;
        ?? r7;
        Observer observer2;
        ChannelIterator channelIterator;
        ChannelIterator channelIterator2;
        Object hasNext;
        Observer<T> observer3;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        ?? r4 = 1;
        try {
            try {
                if (i == 0) {
                    ResultKt.throwOnFailure(obj);
                    flowCollector = (FlowCollector) this.L$0;
                    final Channel Channel$default = ChannelKt.Channel$default(-1, null, null, 6, null);
                    observer = new Observer<T>() { // from class: androidx.lifecycle.FlowLiveDataConversions$asFlow$1$observer$1
                        @Override // androidx.lifecycle.Observer
                        public final void onChanged(T t) {
                            Channel.this.offer(t);
                        }
                    };
                    MainCoroutineDispatcher immediate = Dispatchers.getMain().getImmediate();
                    AnonymousClass1 anonymousClass1 = new AnonymousClass1(observer, null);
                    this.L$0 = flowCollector;
                    this.L$1 = Channel$default;
                    this.L$2 = observer;
                    this.label = 1;
                    if (BuildersKt.withContext(immediate, anonymousClass1, this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    channel = Channel$default;
                } else if (i != 1) {
                    try {
                        if (i == 2) {
                            ChannelIterator channelIterator3 = (ChannelIterator) this.L$2;
                            Observer observer4 = (Observer) this.L$1;
                            FlowCollector flowCollector2 = (FlowCollector) this.L$0;
                            ResultKt.throwOnFailure(obj);
                            r7 = flowCollector2;
                            observer2 = observer4;
                            channelIterator = channelIterator3;
                            flowLiveDataConversions$asFlow$1 = this;
                            if (((Boolean) obj).booleanValue()) {
                            }
                        } else {
                            if (i != 3) {
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }
                            ChannelIterator channelIterator4 = (ChannelIterator) this.L$2;
                            Observer<T> observer5 = (Observer) this.L$1;
                            flowCollector = (FlowCollector) this.L$0;
                            ResultKt.throwOnFailure(obj);
                            channelIterator2 = channelIterator4;
                            observer3 = observer5;
                            flowLiveDataConversions$asFlow$1 = this;
                            r4 = observer3;
                            flowLiveDataConversions$asFlow$1.L$0 = flowCollector;
                            flowLiveDataConversions$asFlow$1.L$1 = r4;
                            flowLiveDataConversions$asFlow$1.L$2 = channelIterator2;
                            flowLiveDataConversions$asFlow$1.label = 2;
                            hasNext = channelIterator2.hasNext(flowLiveDataConversions$asFlow$1);
                            if (hasNext == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            Observer observer6 = r4;
                            channelIterator = channelIterator2;
                            obj = hasNext;
                            r7 = flowCollector;
                            observer2 = observer6;
                            try {
                                if (((Boolean) obj).booleanValue()) {
                                    BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain().getImmediate(), null, flowLiveDataConversions$asFlow$1.new AnonymousClass2(observer2, null), 2, null);
                                    return Unit.INSTANCE;
                                }
                                Object next = channelIterator.next();
                                flowLiveDataConversions$asFlow$1.L$0 = r7;
                                flowLiveDataConversions$asFlow$1.L$1 = observer2;
                                flowLiveDataConversions$asFlow$1.L$2 = channelIterator;
                                flowLiveDataConversions$asFlow$1.label = 3;
                                if (r7.emit(next, flowLiveDataConversions$asFlow$1) == coroutine_suspended) {
                                    return coroutine_suspended;
                                }
                                channelIterator2 = channelIterator;
                                r4 = observer2;
                                flowCollector = r7;
                                flowLiveDataConversions$asFlow$1.L$0 = flowCollector;
                                flowLiveDataConversions$asFlow$1.L$1 = r4;
                                flowLiveDataConversions$asFlow$1.L$2 = channelIterator2;
                                flowLiveDataConversions$asFlow$1.label = 2;
                                hasNext = channelIterator2.hasNext(flowLiveDataConversions$asFlow$1);
                                if (hasNext == coroutine_suspended) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                r4 = observer2;
                                BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain().getImmediate(), null, flowLiveDataConversions$asFlow$1.new AnonymousClass2(r4, null), 2, null);
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        flowLiveDataConversions$asFlow$1 = this;
                        BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain().getImmediate(), null, flowLiveDataConversions$asFlow$1.new AnonymousClass2(r4, null), 2, null);
                        throw th;
                    }
                } else {
                    observer = (Observer) this.L$2;
                    channel = (Channel) this.L$1;
                    flowCollector = (FlowCollector) this.L$0;
                    ResultKt.throwOnFailure(obj);
                }
                flowLiveDataConversions$asFlow$1.L$0 = flowCollector;
                flowLiveDataConversions$asFlow$1.L$1 = r4;
                flowLiveDataConversions$asFlow$1.L$2 = channelIterator2;
                flowLiveDataConversions$asFlow$1.label = 2;
                hasNext = channelIterator2.hasNext(flowLiveDataConversions$asFlow$1);
                if (hasNext == coroutine_suspended) {
                }
            } catch (Throwable th4) {
                th = th4;
                BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain().getImmediate(), null, flowLiveDataConversions$asFlow$1.new AnonymousClass2(r4, null), 2, null);
                throw th;
            }
            channelIterator2 = channel.iterator();
            observer3 = observer;
            flowLiveDataConversions$asFlow$1 = this;
            r4 = observer3;
        } catch (Throwable th5) {
            th = th5;
            r4 = observer;
            flowLiveDataConversions$asFlow$1 = this;
            BuildersKt__Builders_commonKt.launch$default(GlobalScope.INSTANCE, Dispatchers.getMain().getImmediate(), null, flowLiveDataConversions$asFlow$1.new AnonymousClass2(r4, null), 2, null);
            throw th;
        }
    }

    /* compiled from: FlowLiveData.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 1})
    @DebugMetadata(c = "androidx.lifecycle.FlowLiveDataConversions$asFlow$1$1", f = "FlowLiveData.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: androidx.lifecycle.FlowLiveDataConversions$asFlow$1$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Observer $observer;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(Observer observer, Continuation continuation) {
            super(2, continuation);
            this.$observer = observer;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return new AnonymousClass1(this.$observer, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            FlowLiveDataConversions$asFlow$1.this.$this_asFlow.observeForever(this.$observer);
            return Unit.INSTANCE;
        }
    }

    /* compiled from: FlowLiveData.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 1})
    @DebugMetadata(c = "androidx.lifecycle.FlowLiveDataConversions$asFlow$1$2", f = "FlowLiveData.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: androidx.lifecycle.FlowLiveDataConversions$asFlow$1$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Observer $observer;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass2(Observer observer, Continuation continuation) {
            super(2, continuation);
            this.$observer = observer;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
            Intrinsics.checkNotNullParameter(completion, "completion");
            return new AnonymousClass2(this.$observer, completion);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            FlowLiveDataConversions$asFlow$1.this.$this_asFlow.removeObserver(this.$observer);
            return Unit.INSTANCE;
        }
    }
}
