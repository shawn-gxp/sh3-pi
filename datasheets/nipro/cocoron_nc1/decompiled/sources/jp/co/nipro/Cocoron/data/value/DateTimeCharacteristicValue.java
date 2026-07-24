package jp.co.nipro.cocoron.data.value;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import kotlin.Metadata;
import kotlin.UByte;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: DateTimeCharacteristicValue.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u0012\n\u0002\b\f\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u0000 #2\u00020\u0001:\u0001#B\u0005¢\u0006\u0002\u0010\u0002J\u0006\u0010\u001e\u001a\u00020\u0013J\u0010\u0010\u001f\u001a\u00020 2\u0006\u0010\u0012\u001a\u00020\u0013H\u0007J\u0006\u0010!\u001a\u00020 J\u0006\u0010\"\u001a\u00020 R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001a\u0010\f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u0006\"\u0004\b\u000e\u0010\bR\u001a\u0010\u000f\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0006\"\u0004\b\u0011\u0010\bR\u001a\u0010\u0012\u001a\u00020\u0013X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u001a\u0010\u0018\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0006\"\u0004\b\u001a\u0010\bR\u001a\u0010\u001b\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u0006\"\u0004\b\u001d\u0010\b¨\u0006$"}, d2 = {"Ljp/co/nipro/cocoron/data/value/DateTimeCharacteristicValue;", "", "()V", "day", "", "getDay", "()I", "setDay", "(I)V", "hour", "getHour", "setHour", "min", "getMin", "setMin", "month", "getMonth", "setMonth", "packet", "", "getPacket", "()[B", "setPacket", "([B)V", "sec", "getSec", "setSec", "year", "getYear", "setYear", "getDateTimePacket", "readDateTimePacket", "", "setDateTimeNow", "setECGConfigPacket", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class DateTimeCharacteristicValue {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private int day;
    private int hour;
    private int min;
    private int month;
    private byte[] packet;
    private int sec;
    private int year;

    public DateTimeCharacteristicValue() {
        byte[] bArr = new byte[7];
        for (int i = 0; i < 7; i++) {
            bArr[i] = 0;
        }
        this.packet = bArr;
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

    public final byte[] getPacket() {
        return this.packet;
    }

    public final void setPacket(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "<set-?>");
        this.packet = bArr;
    }

    public final void setDateTimeNow() {
        Calendar calendar = Calendar.getInstance();
        this.year = calendar.get(1);
        this.month = calendar.get(2) + 1;
        this.day = calendar.get(5);
        this.hour = calendar.get(11);
        this.min = calendar.get(12);
        this.sec = calendar.get(13);
    }

    public final void readDateTimePacket(byte[] packet) {
        Intrinsics.checkNotNullParameter(packet, "packet");
        this.year = (packet[0] & UByte.MAX_VALUE) | ((packet[1] & UByte.MAX_VALUE) << 8);
        this.month = packet[2];
        this.day = packet[3];
        this.hour = packet[4];
        this.min = packet[5];
        this.sec = packet[6];
    }

    public final void setECGConfigPacket() {
        setDateTimeNow();
        byte[] bArr = this.packet;
        int i = this.year;
        bArr[0] = (byte) (i & 255);
        bArr[1] = (byte) ((i >> 8) & 255);
        bArr[2] = (byte) this.month;
        bArr[3] = (byte) this.day;
        bArr[4] = (byte) this.hour;
        bArr[5] = (byte) this.min;
        bArr[6] = (byte) this.sec;
    }

    public final byte[] getDateTimePacket() {
        setECGConfigPacket();
        return this.packet;
    }

    /* compiled from: DateTimeCharacteristicValue.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004¨\u0006\u0005"}, d2 = {"Ljp/co/nipro/cocoron/data/value/DateTimeCharacteristicValue$Companion;", "", "()V", "getDateTimeStringNow", "", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final String getDateTimeStringNow() {
            String format = new SimpleDateFormat("HH:mm:ss.SSSS").format(new Date());
            Intrinsics.checkNotNullExpressionValue(format, "simpleDateFormat.format(Date())");
            return format;
        }
    }
}
