package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlin.KotlinNothingValueException;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.internal.DispatchedContinuation;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.Symbol;

/* compiled from: CancellableContinuationImpl.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000¶\u0001\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0001\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\b\u0011\u0018\u0000*\u0006\b\u0000\u0010\u0001 \u00002\t\u0012\u0004\u0012\u00028\u00000\u008a\u00012\t\u0012\u0004\u0012\u00028\u00000\u008b\u00012\u00060rj\u0002`sB\u001d\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u0002\u0012\u0006\u0010\u0005\u001a\u00020\u0004¢\u0006\u0004\b\u0006\u0010\u0007J\u0019\u0010\u000b\u001a\u00020\n2\b\u0010\t\u001a\u0004\u0018\u00010\bH\u0002¢\u0006\u0004\b\u000b\u0010\fJ\u001f\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\r2\b\u0010\u0010\u001a\u0004\u0018\u00010\u000f¢\u0006\u0004\b\u0012\u0010\u0013JB\u0010\u0012\u001a\u00020\u00112'\u0010\u000e\u001a#\u0012\u0015\u0012\u0013\u0018\u00010\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u00110\u0014j\u0002`\u00172\b\u0010\u0010\u001a\u0004\u0018\u00010\u000fH\u0002¢\u0006\u0004\b\u0012\u0010\u0018J\u001e\u0010\u001b\u001a\u00020\u00112\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00110\u0019H\u0082\b¢\u0006\u0004\b\u001b\u0010\u001cJ8\u0010\u001e\u001a\u00020\u00112!\u0010\u001d\u001a\u001d\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u00110\u00142\u0006\u0010\u0010\u001a\u00020\u000f¢\u0006\u0004\b\u001e\u0010\u0018J\u0019\u0010 \u001a\u00020\u001f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u000fH\u0016¢\u0006\u0004\b \u0010!J!\u0010%\u001a\u00020\u00112\b\u0010\"\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0010\u001a\u00020\u000fH\u0010¢\u0006\u0004\b#\u0010$J\u0017\u0010&\u001a\u00020\u001f2\u0006\u0010\u0010\u001a\u00020\u000fH\u0002¢\u0006\u0004\b&\u0010!J\u000f\u0010'\u001a\u00020\u001fH\u0002¢\u0006\u0004\b'\u0010(J\u0017\u0010*\u001a\u00020\u00112\u0006\u0010)\u001a\u00020\bH\u0016¢\u0006\u0004\b*\u0010+J\u000f\u0010.\u001a\u00020\u0011H\u0000¢\u0006\u0004\b,\u0010-J\u000f\u0010/\u001a\u00020\u0011H\u0002¢\u0006\u0004\b/\u0010-J\u0017\u00101\u001a\u00020\u00112\u0006\u00100\u001a\u00020\u0004H\u0002¢\u0006\u0004\b1\u00102J\u0017\u00105\u001a\u00020\u000f2\u0006\u00104\u001a\u000203H\u0016¢\u0006\u0004\b5\u00106J\u001b\u0010:\u001a\u0004\u0018\u00010\u000f2\b\u00107\u001a\u0004\u0018\u00010\bH\u0010¢\u0006\u0004\b8\u00109J\u0011\u0010;\u001a\u0004\u0018\u00010\bH\u0001¢\u0006\u0004\b;\u0010<J\u0017\u0010?\u001a\n\u0018\u00010=j\u0004\u0018\u0001`>H\u0016¢\u0006\u0004\b?\u0010@J\u001f\u0010C\u001a\u00028\u0001\"\u0004\b\u0001\u0010\u00012\b\u00107\u001a\u0004\u0018\u00010\bH\u0010¢\u0006\u0004\bA\u0010BJ\u000f\u0010D\u001a\u00020\u0011H\u0016¢\u0006\u0004\bD\u0010-J8\u0010E\u001a\u00020\u00112'\u0010\u000e\u001a#\u0012\u0015\u0012\u0013\u0018\u00010\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u00110\u0014j\u0002`\u0017H\u0016¢\u0006\u0004\bE\u0010FJ\u000f\u0010G\u001a\u00020\u001fH\u0002¢\u0006\u0004\bG\u0010(J8\u0010H\u001a\u00020\r2'\u0010\u000e\u001a#\u0012\u0015\u0012\u0013\u0018\u00010\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u00110\u0014j\u0002`\u0017H\u0002¢\u0006\u0004\bH\u0010IJB\u0010J\u001a\u00020\u00112'\u0010\u000e\u001a#\u0012\u0015\u0012\u0013\u0018\u00010\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u00110\u0014j\u0002`\u00172\b\u00107\u001a\u0004\u0018\u00010\bH\u0002¢\u0006\u0004\bJ\u0010KJ\u000f\u0010M\u001a\u00020LH\u0014¢\u0006\u0004\bM\u0010NJ\u0017\u0010Q\u001a\u00020\u00112\u0006\u0010\u0010\u001a\u00020\u000fH\u0000¢\u0006\u0004\bO\u0010PJ\u000f\u0010R\u001a\u00020\u001fH\u0001¢\u0006\u0004\bR\u0010(J<\u0010T\u001a\u00020\u00112\u0006\u0010S\u001a\u00028\u00002#\u0010\u001d\u001a\u001f\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0014H\u0016¢\u0006\u0004\bT\u0010UJH\u0010V\u001a\u00020\u00112\b\u0010\t\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0005\u001a\u00020\u00042%\b\u0002\u0010\u001d\u001a\u001f\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0014H\u0002¢\u0006\u0004\bV\u0010WJ \u0010Z\u001a\u00020\u00112\f\u0010Y\u001a\b\u0012\u0004\u0012\u00028\u00000XH\u0016ø\u0001\u0000¢\u0006\u0004\bZ\u0010+JZ\u0010]\u001a\u0004\u0018\u00010\b2\u0006\u00107\u001a\u00020[2\b\u0010\t\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0005\u001a\u00020\u00042#\u0010\u001d\u001a\u001f\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u00142\b\u0010\\\u001a\u0004\u0018\u00010\bH\u0002¢\u0006\u0004\b]\u0010^J\u000f\u0010_\u001a\u00020\u0011H\u0002¢\u0006\u0004\b_\u0010-J\u0011\u0010a\u001a\u0004\u0018\u00010\bH\u0010¢\u0006\u0004\b`\u0010<J\u000f\u0010b\u001a\u00020LH\u0016¢\u0006\u0004\bb\u0010NJ\u000f\u0010c\u001a\u00020\u001fH\u0002¢\u0006\u0004\bc\u0010(J#\u0010c\u001a\u0004\u0018\u00010\b2\u0006\u0010S\u001a\u00028\u00002\b\u0010\\\u001a\u0004\u0018\u00010\bH\u0016¢\u0006\u0004\bc\u0010dJH\u0010c\u001a\u0004\u0018\u00010\b2\u0006\u0010S\u001a\u00028\u00002\b\u0010\\\u001a\u0004\u0018\u00010\b2#\u0010\u001d\u001a\u001f\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0014H\u0016¢\u0006\u0004\bc\u0010eJJ\u0010g\u001a\u0004\u0018\u00010f2\b\u0010\t\u001a\u0004\u0018\u00010\b2\b\u0010\\\u001a\u0004\u0018\u00010\b2#\u0010\u001d\u001a\u001f\u0012\u0013\u0012\u00110\u000f¢\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0010\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0014H\u0002¢\u0006\u0004\bg\u0010hJ\u0019\u0010j\u001a\u0004\u0018\u00010\b2\u0006\u0010i\u001a\u00020\u000fH\u0016¢\u0006\u0004\bj\u0010kJ\u000f\u0010l\u001a\u00020\u001fH\u0002¢\u0006\u0004\bl\u0010(J\u001b\u0010n\u001a\u00020\u0011*\u00020m2\u0006\u0010S\u001a\u00028\u0000H\u0016¢\u0006\u0004\bn\u0010oJ\u001b\u0010p\u001a\u00020\u0011*\u00020m2\u0006\u0010i\u001a\u00020\u000fH\u0016¢\u0006\u0004\bp\u0010qR\u001e\u0010v\u001a\n\u0018\u00010rj\u0004\u0018\u0001`s8V@\u0016X\u0096\u0004¢\u0006\u0006\u001a\u0004\bt\u0010uR\u001c\u0010x\u001a\u00020w8\u0016@\u0016X\u0096\u0004¢\u0006\f\n\u0004\bx\u0010y\u001a\u0004\bz\u0010{R\"\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u00028\u0000@\u0000X\u0080\u0004¢\u0006\f\n\u0004\b\u0003\u0010|\u001a\u0004\b}\u0010~R\u0016\u0010\u007f\u001a\u00020\u001f8V@\u0016X\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u007f\u0010(R\u0018\u0010\u0080\u0001\u001a\u00020\u001f8V@\u0016X\u0096\u0004¢\u0006\u0007\u001a\u0005\b\u0080\u0001\u0010(R\u0018\u0010\u0081\u0001\u001a\u00020\u001f8V@\u0016X\u0096\u0004¢\u0006\u0007\u001a\u0005\b\u0081\u0001\u0010(R/\u0010\u0087\u0001\u001a\u0005\u0018\u00010\u0082\u00012\t\u0010S\u001a\u0005\u0018\u00010\u0082\u00018B@BX\u0082\u000e¢\u0006\u0010\u001a\u0006\b\u0083\u0001\u0010\u0084\u0001\"\u0006\b\u0085\u0001\u0010\u0086\u0001R\u0019\u00107\u001a\u0004\u0018\u00010\b8@@\u0000X\u0080\u0004¢\u0006\u0007\u001a\u0005\b\u0088\u0001\u0010<\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0089\u0001"}, d2 = {"Lkotlinx/coroutines/CancellableContinuationImpl;", "T", "Lkotlin/coroutines/Continuation;", "delegate", "", "resumeMode", "<init>", "(Lkotlin/coroutines/Continuation;I)V", "", "proposedUpdate", "", "alreadyResumedError", "(Ljava/lang/Object;)Ljava/lang/Void;", "Lkotlinx/coroutines/CancelHandler;", "handler", "", "cause", "", "callCancelHandler", "(Lkotlinx/coroutines/CancelHandler;Ljava/lang/Throwable;)V", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "Lkotlinx/coroutines/CompletionHandler;", "(Lkotlin/jvm/functions/Function1;Ljava/lang/Throwable;)V", "Lkotlin/Function0;", "block", "callCancelHandlerSafely", "(Lkotlin/jvm/functions/Function0;)V", "onCancellation", "callOnCancellation", "", "cancel", "(Ljava/lang/Throwable;)Z", "takenState", "cancelCompletedResult$kotlinx_coroutines_core", "(Ljava/lang/Object;Ljava/lang/Throwable;)V", "cancelCompletedResult", "cancelLater", "checkCompleted", "()Z", "token", "completeResume", "(Ljava/lang/Object;)V", "detachChild$kotlinx_coroutines_core", "()V", "detachChild", "detachChildIfNonResuable", "mode", "dispatchResume", "(I)V", "Lkotlinx/coroutines/Job;", "parent", "getContinuationCancellationCause", "(Lkotlinx/coroutines/Job;)Ljava/lang/Throwable;", "state", "getExceptionalResult$kotlinx_coroutines_core", "(Ljava/lang/Object;)Ljava/lang/Throwable;", "getExceptionalResult", "getResult", "()Ljava/lang/Object;", "Ljava/lang/StackTraceElement;", "Lkotlinx/coroutines/internal/StackTraceElement;", "getStackTraceElement", "()Ljava/lang/StackTraceElement;", "getSuccessfulResult$kotlinx_coroutines_core", "(Ljava/lang/Object;)Ljava/lang/Object;", "getSuccessfulResult", "initCancellability", "invokeOnCancellation", "(Lkotlin/jvm/functions/Function1;)V", "isReusable", "makeCancelHandler", "(Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/CancelHandler;", "multipleHandlersError", "(Lkotlin/jvm/functions/Function1;Ljava/lang/Object;)V", "", "nameString", "()Ljava/lang/String;", "parentCancelled$kotlinx_coroutines_core", "(Ljava/lang/Throwable;)V", "parentCancelled", "resetStateReusable", "value", "resume", "(Ljava/lang/Object;Lkotlin/jvm/functions/Function1;)V", "resumeImpl", "(Ljava/lang/Object;ILkotlin/jvm/functions/Function1;)V", "Lkotlin/Result;", "result", "resumeWith", "Lkotlinx/coroutines/NotCompleted;", "idempotent", "resumedState", "(Lkotlinx/coroutines/NotCompleted;Ljava/lang/Object;ILkotlin/jvm/functions/Function1;Ljava/lang/Object;)Ljava/lang/Object;", "setupCancellation", "takeState$kotlinx_coroutines_core", "takeState", "toString", "tryResume", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;", "Lkotlinx/coroutines/internal/Symbol;", "tryResumeImpl", "(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/internal/Symbol;", "exception", "tryResumeWithException", "(Ljava/lang/Throwable;)Ljava/lang/Object;", "trySuspend", "Lkotlinx/coroutines/CoroutineDispatcher;", "resumeUndispatched", "(Lkotlinx/coroutines/CoroutineDispatcher;Ljava/lang/Object;)V", "resumeUndispatchedWithException", "(Lkotlinx/coroutines/CoroutineDispatcher;Ljava/lang/Throwable;)V", "Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "Lkotlinx/coroutines/internal/CoroutineStackFrame;", "getCallerFrame", "()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;", "callerFrame", "Lkotlin/coroutines/CoroutineContext;", "context", "Lkotlin/coroutines/CoroutineContext;", "getContext", "()Lkotlin/coroutines/CoroutineContext;", "Lkotlin/coroutines/Continuation;", "getDelegate$kotlinx_coroutines_core", "()Lkotlin/coroutines/Continuation;", "isActive", "isCancelled", "isCompleted", "Lkotlinx/coroutines/DisposableHandle;", "getParentHandle", "()Lkotlinx/coroutines/DisposableHandle;", "setParentHandle", "(Lkotlinx/coroutines/DisposableHandle;)V", "parentHandle", "getState$kotlinx_coroutines_core", "kotlinx-coroutines-core", "Lkotlinx/coroutines/DispatchedTask;", "Lkotlinx/coroutines/CancellableContinuation;"}, k = 1, mv = {1, 4, 0})
/* loaded from: classes.dex */
public class CancellableContinuationImpl<T> extends DispatchedTask<T> implements CancellableContinuation<T>, CoroutineStackFrame {
    private static final AtomicIntegerFieldUpdater _decision$FU = AtomicIntegerFieldUpdater.newUpdater(CancellableContinuationImpl.class, "_decision");
    private static final AtomicReferenceFieldUpdater _state$FU = AtomicReferenceFieldUpdater.newUpdater(CancellableContinuationImpl.class, Object.class, "_state");
    private volatile int _decision;
    private volatile Object _parentHandle;
    private volatile Object _state;
    private final CoroutineContext context;
    private final Continuation<T> delegate;

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    public StackTraceElement getStackTraceElement() {
        return null;
    }

    protected String nameString() {
        return "CancellableContinuation";
    }

    @Override // kotlinx.coroutines.DispatchedTask
    public final Continuation<T> getDelegate$kotlinx_coroutines_core() {
        return this.delegate;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public CancellableContinuationImpl(Continuation<? super T> continuation, int i) {
        super(i);
        this.delegate = continuation;
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(i != -1)) {
                throw new AssertionError();
            }
        }
        this.context = continuation.getContext();
        this._decision = 0;
        this._state = Active.INSTANCE;
        this._parentHandle = null;
    }

    @Override // kotlin.coroutines.Continuation
    public CoroutineContext getContext() {
        return this.context;
    }

    private final DisposableHandle getParentHandle() {
        return (DisposableHandle) this._parentHandle;
    }

    private final void setParentHandle(DisposableHandle disposableHandle) {
        this._parentHandle = disposableHandle;
    }

    /* renamed from: getState$kotlinx_coroutines_core, reason: from getter */
    public final Object get_state() {
        return this._state;
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public boolean isActive() {
        return get_state() instanceof NotCompleted;
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public boolean isCompleted() {
        return !(get_state() instanceof NotCompleted);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public boolean isCancelled() {
        return get_state() instanceof CancelledContinuation;
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void initCancellability() {
        setupCancellation();
    }

    private final boolean isReusable() {
        Continuation<T> continuation = this.delegate;
        return (continuation instanceof DispatchedContinuation) && ((DispatchedContinuation) continuation).isReusable(this);
    }

    public final boolean resetStateReusable() {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(this.resumeMode == 2)) {
                throw new AssertionError();
            }
        }
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(getParentHandle() != NonDisposableHandle.INSTANCE)) {
                throw new AssertionError();
            }
        }
        Object obj = this._state;
        if (DebugKt.getASSERTIONS_ENABLED() && !(!(obj instanceof NotCompleted))) {
            throw new AssertionError();
        }
        if ((obj instanceof CompletedContinuation) && ((CompletedContinuation) obj).idempotentResume != null) {
            detachChild$kotlinx_coroutines_core();
            return false;
        }
        this._decision = 0;
        this._state = Active.INSTANCE;
        return true;
    }

    private final void setupCancellation() {
        Job job;
        if (checkCompleted() || getParentHandle() != null || (job = (Job) this.delegate.getContext().get(Job.INSTANCE)) == null) {
            return;
        }
        DisposableHandle invokeOnCompletion$default = Job.DefaultImpls.invokeOnCompletion$default(job, true, false, new ChildContinuation(job, this), 2, null);
        setParentHandle(invokeOnCompletion$default);
        if (!isCompleted() || isReusable()) {
            return;
        }
        invokeOnCompletion$default.dispose();
        setParentHandle(NonDisposableHandle.INSTANCE);
    }

    private final boolean checkCompleted() {
        Throwable checkPostponedCancellation;
        boolean isCompleted = isCompleted();
        if (!DispatchedTaskKt.isReusableMode(this.resumeMode)) {
            return isCompleted;
        }
        Continuation<T> continuation = this.delegate;
        if (!(continuation instanceof DispatchedContinuation)) {
            continuation = null;
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
        if (dispatchedContinuation == null || (checkPostponedCancellation = dispatchedContinuation.checkPostponedCancellation(this)) == null) {
            return isCompleted;
        }
        if (!isCompleted) {
            cancel(checkPostponedCancellation);
        }
        return true;
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    public CoroutineStackFrame getCallerFrame() {
        Continuation<T> continuation = this.delegate;
        if (!(continuation instanceof CoroutineStackFrame)) {
            continuation = null;
        }
        return (CoroutineStackFrame) continuation;
    }

    @Override // kotlinx.coroutines.DispatchedTask
    public Object takeState$kotlinx_coroutines_core() {
        return get_state();
    }

    private final boolean cancelLater(Throwable cause) {
        if (!DispatchedTaskKt.isReusableMode(this.resumeMode)) {
            return false;
        }
        Continuation<T> continuation = this.delegate;
        if (!(continuation instanceof DispatchedContinuation)) {
            continuation = null;
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
        if (dispatchedContinuation != null) {
            return dispatchedContinuation.postponeCancellation(cause);
        }
        return false;
    }

    public final void parentCancelled$kotlinx_coroutines_core(Throwable cause) {
        if (cancelLater(cause)) {
            return;
        }
        cancel(cause);
        detachChildIfNonResuable();
    }

    private final void callCancelHandlerSafely(Function0<Unit> block) {
        try {
            block.invoke();
        } catch (Throwable th) {
            CoroutineExceptionHandlerKt.handleCoroutineException(getContext(), new CompletionHandlerException("Exception in invokeOnCancellation handler for " + this, th));
        }
    }

    public final void callCancelHandler(CancelHandler handler, Throwable cause) {
        try {
            handler.invoke(cause);
        } catch (Throwable th) {
            CoroutineExceptionHandlerKt.handleCoroutineException(getContext(), new CompletionHandlerException("Exception in invokeOnCancellation handler for " + this, th));
        }
    }

    public final void callOnCancellation(Function1<? super Throwable, Unit> onCancellation, Throwable cause) {
        try {
            onCancellation.invoke(cause);
        } catch (Throwable th) {
            CoroutineExceptionHandlerKt.handleCoroutineException(getContext(), new CompletionHandlerException("Exception in resume onCancellation handler for " + this, th));
        }
    }

    public Throwable getContinuationCancellationCause(Job parent) {
        return parent.getCancellationException();
    }

    public final Object getResult() {
        Job job;
        setupCancellation();
        if (trySuspend()) {
            return IntrinsicsKt.getCOROUTINE_SUSPENDED();
        }
        Object obj = get_state();
        if (!(obj instanceof CompletedExceptionally)) {
            if (DispatchedTaskKt.isCancellableMode(this.resumeMode) && (job = (Job) getContext().get(Job.INSTANCE)) != null && !job.isActive()) {
                CancellationException cancellationException = job.getCancellationException();
                cancelCompletedResult$kotlinx_coroutines_core(obj, cancellationException);
                CancellableContinuationImpl<T> cancellableContinuationImpl = this;
                if (DebugKt.getRECOVER_STACK_TRACES() && (cancellableContinuationImpl instanceof CoroutineStackFrame)) {
                    throw StackTraceRecoveryKt.recoverFromStackFrame(cancellationException, cancellableContinuationImpl);
                }
                throw cancellationException;
            }
            return getSuccessfulResult$kotlinx_coroutines_core(obj);
        }
        Throwable th = ((CompletedExceptionally) obj).cause;
        CancellableContinuationImpl<T> cancellableContinuationImpl2 = this;
        if (DebugKt.getRECOVER_STACK_TRACES() && (cancellableContinuationImpl2 instanceof CoroutineStackFrame)) {
            throw StackTraceRecoveryKt.recoverFromStackFrame(th, cancellableContinuationImpl2);
        }
        throw th;
    }

    @Override // kotlin.coroutines.Continuation
    public void resumeWith(Object result) {
        resumeImpl$default(this, CompletionStateKt.toState(result, this), this.resumeMode, null, 4, null);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void resume(T value, Function1<? super Throwable, Unit> onCancellation) {
        resumeImpl(value, this.resumeMode, onCancellation);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void invokeOnCancellation(Function1<? super Throwable, Unit> handler) {
        CancelHandler makeCancelHandler = makeCancelHandler(handler);
        while (true) {
            Object obj = this._state;
            if (obj instanceof Active) {
                if (_state$FU.compareAndSet(this, obj, makeCancelHandler)) {
                    return;
                }
            } else if (obj instanceof CancelHandler) {
                multipleHandlersError(handler, obj);
            } else {
                boolean z = obj instanceof CompletedExceptionally;
                if (z) {
                    if (!((CompletedExceptionally) obj).makeHandled()) {
                        multipleHandlersError(handler, obj);
                    }
                    if (obj instanceof CancelledContinuation) {
                        if (!z) {
                            obj = null;
                        }
                        CompletedExceptionally completedExceptionally = (CompletedExceptionally) obj;
                        callCancelHandler(handler, completedExceptionally != null ? completedExceptionally.cause : null);
                        return;
                    }
                    return;
                }
                if (obj instanceof CompletedContinuation) {
                    CompletedContinuation completedContinuation = (CompletedContinuation) obj;
                    if (completedContinuation.cancelHandler != null) {
                        multipleHandlersError(handler, obj);
                    }
                    if (makeCancelHandler instanceof BeforeResumeCancelHandler) {
                        return;
                    }
                    if (completedContinuation.getCancelled()) {
                        callCancelHandler(handler, completedContinuation.cancelCause);
                        return;
                    } else {
                        if (_state$FU.compareAndSet(this, obj, CompletedContinuation.copy$default(completedContinuation, null, makeCancelHandler, null, null, null, 29, null))) {
                            return;
                        }
                    }
                } else {
                    if (makeCancelHandler instanceof BeforeResumeCancelHandler) {
                        return;
                    }
                    if (_state$FU.compareAndSet(this, obj, new CompletedContinuation(obj, makeCancelHandler, null, null, null, 28, null))) {
                        return;
                    }
                }
            }
        }
    }

    private final void multipleHandlersError(Function1<? super Throwable, Unit> handler, Object state) {
        throw new IllegalStateException(("It's prohibited to register multiple handlers, tried to register " + handler + ", already has " + state).toString());
    }

    private final CancelHandler makeCancelHandler(Function1<? super Throwable, Unit> handler) {
        return handler instanceof CancelHandler ? (CancelHandler) handler : new InvokeOnCancel(handler);
    }

    private final void dispatchResume(int mode) {
        if (tryResume()) {
            return;
        }
        DispatchedTaskKt.dispatch(this, mode);
    }

    private final Object resumedState(NotCompleted state, Object proposedUpdate, int resumeMode, Function1<? super Throwable, Unit> onCancellation, Object idempotent) {
        if (proposedUpdate instanceof CompletedExceptionally) {
            if (DebugKt.getASSERTIONS_ENABLED()) {
                if (!(idempotent == null)) {
                    throw new AssertionError();
                }
            }
            if (!DebugKt.getASSERTIONS_ENABLED()) {
                return proposedUpdate;
            }
            if (onCancellation == null) {
                return proposedUpdate;
            }
            throw new AssertionError();
        }
        if (!DispatchedTaskKt.isCancellableMode(resumeMode) && idempotent == null) {
            return proposedUpdate;
        }
        if (onCancellation == null && ((!(state instanceof CancelHandler) || (state instanceof BeforeResumeCancelHandler)) && idempotent == null)) {
            return proposedUpdate;
        }
        if (!(state instanceof CancelHandler)) {
            state = null;
        }
        return new CompletedContinuation(proposedUpdate, (CancelHandler) state, onCancellation, idempotent, null, 16, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    static /* synthetic */ void resumeImpl$default(CancellableContinuationImpl cancellableContinuationImpl, Object obj, int i, Function1 function1, int i2, Object obj2) {
        if (obj2 != null) {
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: resumeImpl");
        }
        if ((i2 & 4) != 0) {
            function1 = (Function1) null;
        }
        cancellableContinuationImpl.resumeImpl(obj, i, function1);
    }

    private final Void alreadyResumedError(Object proposedUpdate) {
        throw new IllegalStateException(("Already resumed, but proposed with update " + proposedUpdate).toString());
    }

    private final void detachChildIfNonResuable() {
        if (isReusable()) {
            return;
        }
        detachChild$kotlinx_coroutines_core();
    }

    public final void detachChild$kotlinx_coroutines_core() {
        DisposableHandle parentHandle = getParentHandle();
        if (parentHandle != null) {
            parentHandle.dispose();
        }
        setParentHandle(NonDisposableHandle.INSTANCE);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public Object tryResume(T value, Object idempotent) {
        return tryResumeImpl(value, idempotent, null);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public Object tryResume(T value, Object idempotent, Function1<? super Throwable, Unit> onCancellation) {
        return tryResumeImpl(value, idempotent, onCancellation);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public Object tryResumeWithException(Throwable exception) {
        return tryResumeImpl(new CompletedExceptionally(exception, false, 2, null), null, null);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void completeResume(Object token) {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(token == CancellableContinuationImplKt.RESUME_TOKEN)) {
                throw new AssertionError();
            }
        }
        dispatchResume(this.resumeMode);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void resumeUndispatched(CoroutineDispatcher coroutineDispatcher, T t) {
        Continuation<T> continuation = this.delegate;
        if (!(continuation instanceof DispatchedContinuation)) {
            continuation = null;
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
        resumeImpl$default(this, t, (dispatchedContinuation != null ? dispatchedContinuation.dispatcher : null) == coroutineDispatcher ? 4 : this.resumeMode, null, 4, null);
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public void resumeUndispatchedWithException(CoroutineDispatcher coroutineDispatcher, Throwable th) {
        Continuation<T> continuation = this.delegate;
        if (!(continuation instanceof DispatchedContinuation)) {
            continuation = null;
        }
        DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
        resumeImpl$default(this, new CompletedExceptionally(th, false, 2, null), (dispatchedContinuation != null ? dispatchedContinuation.dispatcher : null) == coroutineDispatcher ? 4 : this.resumeMode, null, 4, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // kotlinx.coroutines.DispatchedTask
    public <T> T getSuccessfulResult$kotlinx_coroutines_core(Object state) {
        return state instanceof CompletedContinuation ? (T) ((CompletedContinuation) state).result : state;
    }

    @Override // kotlinx.coroutines.DispatchedTask
    public Throwable getExceptionalResult$kotlinx_coroutines_core(Object state) {
        Throwable exceptionalResult$kotlinx_coroutines_core = super.getExceptionalResult$kotlinx_coroutines_core(state);
        if (exceptionalResult$kotlinx_coroutines_core == null) {
            return null;
        }
        Continuation<T> continuation = this.delegate;
        return (DebugKt.getRECOVER_STACK_TRACES() && (continuation instanceof CoroutineStackFrame)) ? StackTraceRecoveryKt.recoverFromStackFrame(exceptionalResult$kotlinx_coroutines_core, (CoroutineStackFrame) continuation) : exceptionalResult$kotlinx_coroutines_core;
    }

    public String toString() {
        return nameString() + '(' + DebugStringsKt.toDebugString(this.delegate) + "){" + get_state() + "}@" + DebugStringsKt.getHexAddress(this);
    }

    @Override // kotlinx.coroutines.DispatchedTask
    public void cancelCompletedResult$kotlinx_coroutines_core(Object takenState, Throwable cause) {
        while (true) {
            Object obj = this._state;
            if (obj instanceof NotCompleted) {
                throw new IllegalStateException("Not completed".toString());
            }
            if (obj instanceof CompletedExceptionally) {
                return;
            }
            if (obj instanceof CompletedContinuation) {
                CompletedContinuation completedContinuation = (CompletedContinuation) obj;
                if (!(!completedContinuation.getCancelled())) {
                    throw new IllegalStateException("Must be called at most once".toString());
                }
                if (_state$FU.compareAndSet(this, obj, CompletedContinuation.copy$default(completedContinuation, null, null, null, null, cause, 15, null))) {
                    completedContinuation.invokeHandlers(this, cause);
                    return;
                }
            } else if (_state$FU.compareAndSet(this, obj, new CompletedContinuation(obj, null, null, null, cause, 14, null))) {
                return;
            }
        }
    }

    @Override // kotlinx.coroutines.CancellableContinuation
    public boolean cancel(Throwable cause) {
        Object obj;
        boolean z;
        do {
            obj = this._state;
            if (!(obj instanceof NotCompleted)) {
                return false;
            }
            z = obj instanceof CancelHandler;
        } while (!_state$FU.compareAndSet(this, obj, new CancelledContinuation(this, cause, z)));
        if (!z) {
            obj = null;
        }
        CancelHandler cancelHandler = (CancelHandler) obj;
        if (cancelHandler != null) {
            callCancelHandler(cancelHandler, cause);
        }
        detachChildIfNonResuable();
        dispatchResume(this.resumeMode);
        return true;
    }

    private final void callCancelHandler(Function1<? super Throwable, Unit> handler, Throwable cause) {
        try {
            handler.invoke(cause);
        } catch (Throwable th) {
            CoroutineExceptionHandlerKt.handleCoroutineException(getContext(), new CompletionHandlerException("Exception in invokeOnCancellation handler for " + this, th));
        }
    }

    private final boolean trySuspend() {
        do {
            int i = this._decision;
            if (i != 0) {
                if (i == 2) {
                    return false;
                }
                throw new IllegalStateException("Already suspended".toString());
            }
        } while (!_decision$FU.compareAndSet(this, 0, 1));
        return true;
    }

    private final boolean tryResume() {
        do {
            int i = this._decision;
            if (i != 0) {
                if (i == 1) {
                    return false;
                }
                throw new IllegalStateException("Already resumed".toString());
            }
        } while (!_decision$FU.compareAndSet(this, 0, 2));
        return true;
    }

    private final void resumeImpl(Object proposedUpdate, int resumeMode, Function1<? super Throwable, Unit> onCancellation) {
        Object obj;
        do {
            obj = this._state;
            if (obj instanceof NotCompleted) {
            } else {
                if (obj instanceof CancelledContinuation) {
                    CancelledContinuation cancelledContinuation = (CancelledContinuation) obj;
                    if (cancelledContinuation.makeResumed()) {
                        if (onCancellation != null) {
                            callOnCancellation(onCancellation, cancelledContinuation.cause);
                            return;
                        }
                        return;
                    }
                }
                alreadyResumedError(proposedUpdate);
                throw new KotlinNothingValueException();
            }
        } while (!_state$FU.compareAndSet(this, obj, resumedState((NotCompleted) obj, proposedUpdate, resumeMode, onCancellation, null)));
        detachChildIfNonResuable();
        dispatchResume(resumeMode);
    }

    private final Symbol tryResumeImpl(Object proposedUpdate, Object idempotent, Function1<? super Throwable, Unit> onCancellation) {
        Object obj;
        do {
            obj = this._state;
            if (obj instanceof NotCompleted) {
            } else {
                if (!(obj instanceof CompletedContinuation) || idempotent == null) {
                    return null;
                }
                CompletedContinuation completedContinuation = (CompletedContinuation) obj;
                if (completedContinuation.idempotentResume != idempotent) {
                    return null;
                }
                if (!DebugKt.getASSERTIONS_ENABLED() || Intrinsics.areEqual(completedContinuation.result, proposedUpdate)) {
                    return CancellableContinuationImplKt.RESUME_TOKEN;
                }
                throw new AssertionError();
            }
        } while (!_state$FU.compareAndSet(this, obj, resumedState((NotCompleted) obj, proposedUpdate, this.resumeMode, onCancellation, idempotent)));
        detachChildIfNonResuable();
        return CancellableContinuationImplKt.RESUME_TOKEN;
    }
}
