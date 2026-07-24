package jp.co.nipro.cocoron.data.value;

import jp.co.nipro.cocoron.data.noise.Ntf;
import kotlin.Metadata;
import kotlin.UByte;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;

/* compiled from: ECGMeasurementCharacteristicValue.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0013\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010\u0012\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u000e\u0018\u0000 :2\u00020\u0001:\u0001:B\u0005¢\u0006\u0002\u0010\u0002J\u0012\u00108\u001a\u0004\u0018\u00010\u00042\u0006\u00109\u001a\u00020$H\u0007R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\f\"\u0004\b\u0011\u0010\u000eR\u001a\u0010\u0012\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\f\"\u0004\b\u0014\u0010\u000eR\u001a\u0010\u0015\u001a\u00020\u0016X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u001a\u0010\u001a\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\f\"\u0004\b\u001c\u0010\u000eR\u001a\u0010\u001d\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\f\"\u0004\b\u001f\u0010\u000eR\u001a\u0010 \u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\f\"\u0004\b\"\u0010\u000eR\u001c\u0010#\u001a\u0004\u0018\u00010$X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b%\u0010&\"\u0004\b'\u0010(R\u001a\u0010)\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\f\"\u0004\b+\u0010\u000eR\u001a\u0010,\u001a\u00020-X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b.\u0010/\"\u0004\b0\u00101R\u001a\u00102\u001a\u00020-X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b3\u0010/\"\u0004\b4\u00101R\u001a\u00105\u001a\u00020\nX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b6\u0010\f\"\u0004\b7\u0010\u000e¨\u0006;"}, d2 = {"Ljp/co/nipro/cocoron/data/value/ECGMeasurementCharacteristicValue;", "", "()V", "ECG", "", "getECG", "()[D", "setECG", "([D)V", "dataSize", "", "getDataSize", "()I", "setDataSize", "(I)V", "day", "getDay", "setDay", "hour", "getHour", "setHour", "isInDateTime", "", "()Z", "setInDateTime", "(Z)V", "min", "getMin", "setMin", "month", "getMonth", "setMonth", "packetCounter", "getPacketCounter", "setPacketCounter", "receivedPacket", "", "getReceivedPacket", "()[B", "setReceivedPacket", "([B)V", "sec", "getSec", "setSec", "trigger1", "", "getTrigger1", "()J", "setTrigger1", "(J)V", "trigger2", "getTrigger2", "setTrigger2", "year", "getYear", "setYear", "readECGMeasurementPacket", "packet", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class ECGMeasurementCharacteristicValue {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static byte ECG_DATASIZE = 63;
    private static byte ECG_DATETIME = 64;
    private static int dataCounter;
    private static int packetCounterOld;
    private double[] ECG;
    private int dataSize;
    private int day;
    private int hour;
    private boolean isInDateTime;
    private int min;
    private int month;
    private int packetCounter;
    private byte[] receivedPacket;
    private int sec;
    private long trigger1 = 255;
    private long trigger2 = 255;
    private int year;

    /* compiled from: ECGMeasurementCharacteristicValue.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001a\u0010\f\u001a\u00020\rX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001a\u0010\u0012\u001a\u00020\rX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u000f\"\u0004\b\u0014\u0010\u0011¨\u0006\u0015"}, d2 = {"Ljp/co/nipro/cocoron/data/value/ECGMeasurementCharacteristicValue$Companion;", "", "()V", "ECG_DATASIZE", "", "getECG_DATASIZE", "()B", "setECG_DATASIZE", "(B)V", "ECG_DATETIME", "getECG_DATETIME", "setECG_DATETIME", "dataCounter", "", "getDataCounter", "()I", "setDataCounter", "(I)V", "packetCounterOld", "getPacketCounterOld", "setPacketCounterOld", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final byte getECG_DATASIZE() {
            return ECGMeasurementCharacteristicValue.ECG_DATASIZE;
        }

        public final void setECG_DATASIZE(byte b) {
            ECGMeasurementCharacteristicValue.ECG_DATASIZE = b;
        }

        public final byte getECG_DATETIME() {
            return ECGMeasurementCharacteristicValue.ECG_DATETIME;
        }

        public final void setECG_DATETIME(byte b) {
            ECGMeasurementCharacteristicValue.ECG_DATETIME = b;
        }

        public final int getDataCounter() {
            return ECGMeasurementCharacteristicValue.dataCounter;
        }

        public final void setDataCounter(int i) {
            ECGMeasurementCharacteristicValue.dataCounter = i;
        }

        public final int getPacketCounterOld() {
            return ECGMeasurementCharacteristicValue.packetCounterOld;
        }

        public final void setPacketCounterOld(int i) {
            ECGMeasurementCharacteristicValue.packetCounterOld = i;
        }
    }

    /* renamed from: isInDateTime, reason: from getter */
    public final boolean getIsInDateTime() {
        return this.isInDateTime;
    }

    public final void setInDateTime(boolean z) {
        this.isInDateTime = z;
    }

    public final int getDataSize() {
        return this.dataSize;
    }

    public final void setDataSize(int i) {
        this.dataSize = i;
    }

    public final int getPacketCounter() {
        return this.packetCounter;
    }

    public final void setPacketCounter(int i) {
        this.packetCounter = i;
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

    public final double[] getECG() {
        return this.ECG;
    }

    public final void setECG(double[] dArr) {
        this.ECG = dArr;
    }

    public final byte[] getReceivedPacket() {
        return this.receivedPacket;
    }

    public final void setReceivedPacket(byte[] bArr) {
        this.receivedPacket = bArr;
    }

    public final long getTrigger1() {
        return this.trigger1;
    }

    public final void setTrigger1(long j) {
        this.trigger1 = j;
    }

    public final long getTrigger2() {
        return this.trigger2;
    }

    public final void setTrigger2(long j) {
        this.trigger2 = j;
    }

    public final double[] readECGMeasurementPacket(byte[] packet) {
        Intrinsics.checkNotNullParameter(packet, "packet");
        int i = 0;
        byte b = packet[0];
        byte b2 = ECG_DATETIME;
        boolean z = ((byte) (b & b2)) == b2;
        this.isInDateTime = z;
        this.dataSize = (byte) (packet[0] & ECG_DATASIZE);
        this.receivedPacket = packet;
        this.packetCounter = (packet[1] & UByte.MAX_VALUE) | ((packet[2] & UByte.MAX_VALUE) << 8);
        int i2 = 3;
        if (z) {
            this.year = packet[3] + 2000;
            this.month = packet[4];
            this.day = packet[5];
            this.hour = packet[6];
            this.min = packet[7];
            this.sec = packet[8];
            i2 = 9;
        }
        this.trigger1 = packet[i2];
        this.trigger2 = packet[r2];
        int length = new Huffman().decode(ArraysKt.sliceArray(packet, new IntRange(i2 + 1 + 1, packet.length - 1)), this.dataSize).length;
        int i3 = this.dataSize;
        if (length != i3) {
            return null;
        }
        packetCounterOld = this.packetCounter;
        double[] dArr = new double[i3];
        for (int i4 = 0; i4 < i3; i4++) {
            dArr[i4] = 0.0d;
        }
        this.ECG = dArr;
        int i5 = this.dataSize - 1;
        if (i5 >= 0) {
            while (true) {
                double d = (r8[i] - 512.0d) * 0.01d;
                double[] dArr2 = this.ECG;
                if (dArr2 != null) {
                    dArr2[i] = Ntf.INSTANCE.ntf().apply(d);
                }
                if (i == i5) {
                    break;
                }
                i++;
            }
        }
        return this.ECG;
    }
}
