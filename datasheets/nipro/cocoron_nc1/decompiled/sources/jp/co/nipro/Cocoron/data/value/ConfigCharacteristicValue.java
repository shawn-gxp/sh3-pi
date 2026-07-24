package jp.co.nipro.cocoron.data.value;

import java.nio.charset.Charset;
import java.util.Objects;
import jp.co.nipro.cocoron.common.Config;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: ConfigCharacteristicValue.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B\u0005¢\u0006\u0002\u0010\u0002J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fR\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\b¨\u0006\u0011"}, d2 = {"Ljp/co/nipro/cocoron/data/value/ConfigCharacteristicValue;", "", "()V", "interval", "", "getInterval", "()I", "setInterval", "(I)V", "mode", "getMode", "setMode", "readConfigPacket", "", "packet", "", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class ConfigCharacteristicValue {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static byte RRT_ECG_FREE_RUN = 2;
    private int interval = 2;
    private int mode;

    /* compiled from: ConfigCharacteristicValue.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\fR\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b¨\u0006\u000e"}, d2 = {"Ljp/co/nipro/cocoron/data/value/ConfigCharacteristicValue$Companion;", "", "()V", "RRT_ECG_FREE_RUN", "", "getRRT_ECG_FREE_RUN", "()B", "setRRT_ECG_FREE_RUN", "(B)V", "writeToDate", "", "mode", "", "interval", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final byte getRRT_ECG_FREE_RUN() {
            return ConfigCharacteristicValue.RRT_ECG_FREE_RUN;
        }

        public final void setRRT_ECG_FREE_RUN(byte b) {
            ConfigCharacteristicValue.RRT_ECG_FREE_RUN = b;
        }

        public final byte[] writeToDate(int mode, int interval) {
            byte[] bArr = new byte[2];
            for (int i = 0; i < 2; i++) {
                bArr[i] = 0;
            }
            bArr[0] = mode != 1 ? (byte) 0 : (byte) 2;
            bArr[1] = (byte) interval;
            String uuid = Config.INSTANCE.getUUID();
            Charset charset = Charsets.UTF_8;
            Objects.requireNonNull(uuid, "null cannot be cast to non-null type java.lang.String");
            byte[] bytes = uuid.getBytes(charset);
            Intrinsics.checkNotNullExpressionValue(bytes, "(this as java.lang.String).getBytes(charset)");
            return ArraysKt.plus(bArr, bytes);
        }
    }

    public final int getInterval() {
        return this.interval;
    }

    public final void setInterval(int i) {
        this.interval = i;
    }

    public final int getMode() {
        return this.mode;
    }

    public final void setMode(int i) {
        this.mode = i;
    }

    public final void readConfigPacket(byte[] packet) {
        Intrinsics.checkNotNullParameter(packet, "packet");
        this.mode = (byte) (packet[0] & RRT_ECG_FREE_RUN);
        this.interval = packet[1];
    }
}
