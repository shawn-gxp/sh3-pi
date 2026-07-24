package jp.co.nipro.cocoron.common.extension;

import kotlin.Metadata;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KProperty;

/* compiled from: FieldProperty.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000*\u0004\b\u0000\u0010\u0001*\b\b\u0001\u0010\u0002*\u00020\u00032\u00020\u0003B\u001b\u0012\u0014\b\u0002\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u0005¢\u0006\u0002\u0010\u0006J\"\u0010\u000b\u001a\u00028\u00012\u0006\u0010\f\u001a\u00028\u00002\n\u0010\r\u001a\u0006\u0012\u0002\b\u00030\u000eH\u0086\u0002¢\u0006\u0002\u0010\u000fJ*\u0010\u0010\u001a\u00028\u00012\u0006\u0010\f\u001a\u00028\u00002\n\u0010\r\u001a\u0006\u0012\u0002\b\u00030\u000e2\u0006\u0010\u0011\u001a\u00028\u0001H\u0086\u0002¢\u0006\u0002\u0010\u0012R\u001d\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\nX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0013"}, d2 = {"Ljp/co/nipro/cocoron/common/extension/FieldProperty;", "R", "T", "", "initializer", "Lkotlin/Function1;", "(Lkotlin/jvm/functions/Function1;)V", "getInitializer", "()Lkotlin/jvm/functions/Function1;", "map", "Ljp/co/nipro/cocoron/common/extension/WeakIdentityHashMap;", "getValue", "thisRef", "property", "Lkotlin/reflect/KProperty;", "(Ljava/lang/Object;Lkotlin/reflect/KProperty;)Ljava/lang/Object;", "setValue", "value", "(Ljava/lang/Object;Lkotlin/reflect/KProperty;Ljava/lang/Object;)Ljava/lang/Object;", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class FieldProperty<R, T> {
    private final Function1<R, T> initializer;
    private final WeakIdentityHashMap<R, T> map;

    /* JADX WARN: Multi-variable type inference failed */
    public FieldProperty() {
        this(null, 1, 0 == true ? 1 : 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public FieldProperty(Function1<? super R, ? extends T> initializer) {
        Intrinsics.checkNotNullParameter(initializer, "initializer");
        this.initializer = initializer;
        this.map = new WeakIdentityHashMap<>();
    }

    public /* synthetic */ FieldProperty(AnonymousClass1 anonymousClass1, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? new Function1() { // from class: jp.co.nipro.cocoron.common.extension.FieldProperty.1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Object invoke(Object obj) {
                return invoke((AnonymousClass1) obj);
            }

            @Override // kotlin.jvm.functions.Function1
            public final Void invoke(R r) {
                throw new IllegalStateException("Not initialized.");
            }
        } : anonymousClass1);
    }

    public final Function1<R, T> getInitializer() {
        return this.initializer;
    }

    public final T getValue(R thisRef, KProperty<?> property) {
        Intrinsics.checkNotNullParameter(property, "property");
        T t = this.map.get(thisRef);
        return t != null ? t : setValue(thisRef, property, this.initializer.invoke(thisRef));
    }

    public final T setValue(R thisRef, KProperty<?> property, T value) {
        Intrinsics.checkNotNullParameter(property, "property");
        Intrinsics.checkNotNullParameter(value, "value");
        this.map.put(thisRef, value);
        return value;
    }
}
