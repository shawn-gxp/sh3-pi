package jp.co.nipro.cocoron.data.value;

import kotlin.Metadata;
import kotlin.UByte;
import kotlin.UShort;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: RRTimeCharacteristicValue.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0019\n\u0002\u0010\u0012\n\u0002\b\u0004\b\u0007\u0018\u0000 42\u00020\u0001:\u00014B\u0005¢\u0006\u0002\u0010\u0002J\u001e\u0010/\u001a\u00020\u00042\u0006\u00100\u001a\u000201ø\u0001\u0000ø\u0001\u0002ø\u0001\u0001¢\u0006\u0004\b2\u00103R%\u0010\u0003\u001a\u00020\u0004X\u0086\u000eø\u0001\u0000ø\u0001\u0001ø\u0001\u0002¢\u0006\u0010\n\u0002\u0010\t\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0010\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\r\"\u0004\b\u0012\u0010\u000fR\u001a\u0010\u0013\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\r\"\u0004\b\u0015\u0010\u000fR\u001a\u0010\u0016\u001a\u00020\u0017X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u001a\u0010\u001b\u001a\u00020\u0017X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u0018\"\u0004\b\u001c\u0010\u001aR\u001a\u0010\u001d\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\r\"\u0004\b\u001f\u0010\u000fR\u001a\u0010 \u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\r\"\u0004\b\"\u0010\u000fR\u001a\u0010#\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b$\u0010\r\"\u0004\b%\u0010\u000fR\u001a\u0010&\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b'\u0010\r\"\u0004\b(\u0010\u000fR\u001a\u0010)\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\r\"\u0004\b+\u0010\u000fR\u001a\u0010,\u001a\u00020\u000bX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b-\u0010\r\"\u0004\b.\u0010\u000f\u0082\u0002\u000f\n\u0002\b\u0019\n\u0005\b¡\u001e0\u0001\n\u0002\b!¨\u00065"}, d2 = {"Ljp/co/nipro/cocoron/data/value/RRTimeCharacteristicValue;", "", "()V", "RRTime", "Lkotlin/UShort;", "getRRTime-Mh2AYeg", "()S", "setRRTime-xj2QHRw", "(S)V", "S", "day", "", "getDay", "()I", "setDay", "(I)V", "hour", "getHour", "setHour", "interval", "getInterval", "setInterval", "isIllegalIphone", "", "()Z", "setIllegalIphone", "(Z)V", "isInDateTime", "setInDateTime", "min", "getMin", "setMin", "mode", "getMode", "setMode", "month", "getMonth", "setMonth", "outservice", "getOutservice", "setOutservice", "sec", "getSec", "setSec", "year", "getYear", "setYear", "readRRTimePacket", "packet", "", "readRRTimePacket-BwKQO78", "([B)S", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class RRTimeCharacteristicValue {
    private short RRTime;
    private int day;
    private int hour;
    private int interval;
    private boolean isIllegalIphone;
    private boolean isInDateTime;
    private int min;
    private int mode;
    private int month;
    private int outservice;
    private int sec;
    private int year;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static byte RRT_DATETIME = 16;
    private static byte RRT_LEADS_OFF_DETECT = 32;
    private static byte RRT_ECG_FREE_RUN = 2;
    private static byte RRT_ILLEGAL_IPHONE = 64;

    /* compiled from: RRTimeCharacteristicValue.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0002\b\u000e\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001a\u0010\f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u0006\"\u0004\b\u000e\u0010\bR\u001a\u0010\u000f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0006\"\u0004\b\u0011\u0010\b¨\u0006\u0012"}, d2 = {"Ljp/co/nipro/cocoron/data/value/RRTimeCharacteristicValue$Companion;", "", "()V", "RRT_DATETIME", "", "getRRT_DATETIME", "()B", "setRRT_DATETIME", "(B)V", "RRT_ECG_FREE_RUN", "getRRT_ECG_FREE_RUN", "setRRT_ECG_FREE_RUN", "RRT_ILLEGAL_IPHONE", "getRRT_ILLEGAL_IPHONE", "setRRT_ILLEGAL_IPHONE", "RRT_LEADS_OFF_DETECT", "getRRT_LEADS_OFF_DETECT", "setRRT_LEADS_OFF_DETECT", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final byte getRRT_DATETIME() {
            return RRTimeCharacteristicValue.RRT_DATETIME;
        }

        public final void setRRT_DATETIME(byte b) {
            RRTimeCharacteristicValue.RRT_DATETIME = b;
        }

        public final byte getRRT_LEADS_OFF_DETECT() {
            return RRTimeCharacteristicValue.RRT_LEADS_OFF_DETECT;
        }

        public final void setRRT_LEADS_OFF_DETECT(byte b) {
            RRTimeCharacteristicValue.RRT_LEADS_OFF_DETECT = b;
        }

        public final byte getRRT_ECG_FREE_RUN() {
            return RRTimeCharacteristicValue.RRT_ECG_FREE_RUN;
        }

        public final void setRRT_ECG_FREE_RUN(byte b) {
            RRTimeCharacteristicValue.RRT_ECG_FREE_RUN = b;
        }

        public final byte getRRT_ILLEGAL_IPHONE() {
            return RRTimeCharacteristicValue.RRT_ILLEGAL_IPHONE;
        }

        public final void setRRT_ILLEGAL_IPHONE(byte b) {
            RRTimeCharacteristicValue.RRT_ILLEGAL_IPHONE = b;
        }
    }

    /* renamed from: isInDateTime, reason: from getter */
    public final boolean getIsInDateTime() {
        return this.isInDateTime;
    }

    public final void setInDateTime(boolean z) {
        this.isInDateTime = z;
    }

    /* renamed from: isIllegalIphone, reason: from getter */
    public final boolean getIsIllegalIphone() {
        return this.isIllegalIphone;
    }

    public final void setIllegalIphone(boolean z) {
        this.isIllegalIphone = z;
    }

    public final int getYear() {
        return this.year;
    }

    public final void setYear(int i) {
        this.year = i;
    }

    public final int getMonth() {
        return this.month;
    }

    public final void setMonth(int i) {
        this.month = i;
    }

    public final int getDay() {
        return this.day;
    }

    public final void setDay(int i) {
        this.day = i;
    }

    public final int getHour() {
        return this.hour;
    }

    public final void setHour(int i) {
        this.hour = i;
    }

    public final int getMin() {
        return this.min;
    }

    public final void setMin(int i) {
        this.min = i;
    }

    public final int getSec() {
        return this.sec;
    }

    public final void setSec(int i) {
        this.sec = i;
    }

    /* renamed from: getRRTime-Mh2AYeg, reason: not valid java name and from getter */
    public final short getRRTime() {
        return this.RRTime;
    }

    /* renamed from: setRRTime-xj2QHRw, reason: not valid java name */
    public final void m9setRRTimexj2QHRw(short s) {
        this.RRTime = s;
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

    public final int getOutservice() {
        return this.outservice;
    }

    public final void setOutservice(int i) {
        this.outservice = i;
    }

    /* renamed from: readRRTimePacket-BwKQO78, reason: not valid java name */
    public final short m8readRRTimePacketBwKQO78(byte[] packet) {
        Intrinsics.checkNotNullParameter(packet, "packet");
        byte b = packet[0];
        byte b2 = RRT_DATETIME;
        int i = 1;
        boolean z = ((byte) (b & b2)) == b2;
        this.isInDateTime = z;
        byte b3 = packet[0];
        byte b4 = RRT_ILLEGAL_IPHONE;
        this.isIllegalIphone = ((byte) (b3 & b4)) == b4;
        this.mode = ((byte) (packet[0] & RRT_ECG_FREE_RUN)) == b4 ? 1 : 0;
        byte b5 = packet[0];
        byte b6 = RRT_LEADS_OFF_DETECT;
        this.outservice = ((byte) (b5 & b6)) == b6 ? 1 : 0;
        if (z) {
            this.year = packet[1] + 2000;
            this.month = packet[2];
            this.day = packet[3];
            this.hour = packet[4];
            this.min = packet[5];
            this.sec = packet[6];
            i = 7;
        }
        short m269constructorimpl = UShort.m269constructorimpl((short) ((packet[i] & UByte.MAX_VALUE) | ((packet[i + 1] & UByte.MAX_VALUE) << 8)));
        this.RRTime = m269constructorimpl;
        this.interval = packet[i + 2];
        return m269constructorimpl;
    }
}
