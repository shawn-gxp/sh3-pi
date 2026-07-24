package jp.co.nipro.cocoron.data.value;

import kotlin.Metadata;
import kotlin.UByte;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: BatteryLevelCharacteristicValue.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000f\n\u0002\u0010\u0012\n\u0000\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u0014H\u0007R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001a\u0010\f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u0006\"\u0004\b\u000e\u0010\bR\u001a\u0010\u000f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0006\"\u0004\b\u0011\u0010\b¨\u0006\u0015"}, d2 = {"Ljp/co/nipro/cocoron/data/value/BatteryLevelCharacteristicValue;", "", "()V", "bugfixVer", "", "getBugfixVer", "()I", "setBugfixVer", "(I)V", "level", "getLevel", "setLevel", "majorVer", "getMajorVer", "setMajorVer", "minorVer", "getMinorVer", "setMinorVer", "readBatteryLevelPacket", "packet", "", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class BatteryLevelCharacteristicValue {
    private int bugfixVer;
    private int level;
    private int majorVer;
    private int minorVer;

    public final int getLevel() {
        return this.level;
    }

    public final void setLevel(int i) {
        this.level = i;
    }

    public final int getMajorVer() {
        return this.majorVer;
    }

    public final void setMajorVer(int i) {
        this.majorVer = i;
    }

    public final int getMinorVer() {
        return this.minorVer;
    }

    public final void setMinorVer(int i) {
        this.minorVer = i;
    }

    public final int getBugfixVer() {
        return this.bugfixVer;
    }

    public final void setBugfixVer(int i) {
        this.bugfixVer = i;
    }

    public final int readBatteryLevelPacket(byte[] packet) {
        Intrinsics.checkNotNullParameter(packet, "packet");
        if (packet.length < 5) {
            return 0;
        }
        int i = (packet[0] & UByte.MAX_VALUE) | ((packet[1] & UByte.MAX_VALUE) << 8);
        this.level = i;
        this.majorVer = packet[2];
        this.minorVer = packet[3];
        this.bugfixVer = packet[4];
        return i;
    }
}
