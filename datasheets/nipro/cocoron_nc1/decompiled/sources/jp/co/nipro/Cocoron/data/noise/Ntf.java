package jp.co.nipro.cocoron.data.noise;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;

/* compiled from: Ntf.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \t2\u00020\u0001:\u0001\tB\u0005¢\u0006\u0002\u0010\u0002J\u0011\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0086 J\t\u0010\u0006\u001a\u00020\u0007H\u0086 J\t\u0010\b\u001a\u00020\u0007H\u0086 ¨\u0006\n"}, d2 = {"Ljp/co/nipro/cocoron/data/noise/Ntf;", "", "()V", "apply", "", "dx0", "endNftOc", "", "initNft", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class Ntf {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static Ntf ntf;

    public final native double apply(double dx0);

    public final native void endNftOc();

    public final native void initNft();

    /* compiled from: Ntf.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0003\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006\u0007"}, d2 = {"Ljp/co/nipro/cocoron/data/noise/Ntf$Companion;", "", "()V", "ntf", "Ljp/co/nipro/cocoron/data/noise/Ntf;", "destroyInstance", "", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final Ntf ntf() {
            if (Ntf.ntf == null) {
                synchronized (Reflection.getOrCreateKotlinClass(Ntf.class)) {
                    Ntf.ntf = new Ntf();
                    Ntf ntf = Ntf.ntf;
                    Intrinsics.checkNotNull(ntf);
                    ntf.initNft();
                    Unit unit = Unit.INSTANCE;
                }
            }
            Ntf ntf2 = Ntf.ntf;
            Intrinsics.checkNotNull(ntf2);
            return ntf2;
        }

        public final void destroyInstance() {
            Ntf ntf = Ntf.ntf;
            Intrinsics.checkNotNull(ntf);
            ntf.endNftOc();
            Ntf.ntf = (Ntf) null;
        }
    }

    static {
        System.loadLibrary("ntf-lib");
    }
}
