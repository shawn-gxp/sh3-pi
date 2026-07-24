package kotlin.collections;

import kotlin.Metadata;
import kotlin.UByte;
import kotlin.UByteArray;
import kotlin.UIntArray;
import kotlin.ULongArray;
import kotlin.UShort;
import kotlin.UShortArray;
import kotlin.UnsignedKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: UArraySorting.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0010\u001a*\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u0006\u0010\u0007\u001a*\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\t\u0010\n\u001a*\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\f\u0010\r\u001a*\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u000e2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u000f\u0010\u0010\u001a*\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u0013\u0010\u0014\u001a*\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\b2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u0015\u0010\u0016\u001a*\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u0017\u0010\u0018\u001a*\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u000e2\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0003ø\u0001\u0000¢\u0006\u0004\b\u0019\u0010\u001a\u001a*\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u0001H\u0001ø\u0001\u0000¢\u0006\u0004\b\u001e\u0010\u0014\u001a*\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u0001H\u0001ø\u0001\u0000¢\u0006\u0004\b\u001f\u0010\u0016\u001a*\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u000b2\u0006\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u0001H\u0001ø\u0001\u0000¢\u0006\u0004\b \u0010\u0018\u001a*\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u0002\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u0001H\u0001ø\u0001\u0000¢\u0006\u0004\b!\u0010\u001a\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\""}, d2 = {"partition", "", "array", "Lkotlin/UByteArray;", "left", "right", "partition-4UcCI2c", "([BII)I", "Lkotlin/UIntArray;", "partition-oBK06Vg", "([III)I", "Lkotlin/ULongArray;", "partition--nroSd4", "([JII)I", "Lkotlin/UShortArray;", "partition-Aa5vz7o", "([SII)I", "quickSort", "", "quickSort-4UcCI2c", "([BII)V", "quickSort-oBK06Vg", "([III)V", "quickSort--nroSd4", "([JII)V", "quickSort-Aa5vz7o", "([SII)V", "sortArray", "fromIndex", "toIndex", "sortArray-4UcCI2c", "sortArray-oBK06Vg", "sortArray--nroSd4", "sortArray-Aa5vz7o", "kotlin-stdlib"}, k = 2, mv = {1, 4, 1})
/* loaded from: classes.dex */
public final class UArraySortingKt {
    /* renamed from: partition-4UcCI2c, reason: not valid java name */
    private static final int m431partition4UcCI2c(byte[] bArr, int i, int i2) {
        int i3;
        byte m82getw2LRezQ = UByteArray.m82getw2LRezQ(bArr, (i + i2) / 2);
        while (i <= i2) {
            while (true) {
                int m82getw2LRezQ2 = UByteArray.m82getw2LRezQ(bArr, i) & UByte.MAX_VALUE;
                i3 = m82getw2LRezQ & UByte.MAX_VALUE;
                if (Intrinsics.compare(m82getw2LRezQ2, i3) >= 0) {
                    break;
                }
                i++;
            }
            while (Intrinsics.compare(UByteArray.m82getw2LRezQ(bArr, i2) & UByte.MAX_VALUE, i3) > 0) {
                i2--;
            }
            if (i <= i2) {
                byte m82getw2LRezQ3 = UByteArray.m82getw2LRezQ(bArr, i);
                UByteArray.m87setVurrAj0(bArr, i, UByteArray.m82getw2LRezQ(bArr, i2));
                UByteArray.m87setVurrAj0(bArr, i2, m82getw2LRezQ3);
                i++;
                i2--;
            }
        }
        return i;
    }

    /* renamed from: quickSort-4UcCI2c, reason: not valid java name */
    private static final void m435quickSort4UcCI2c(byte[] bArr, int i, int i2) {
        int m431partition4UcCI2c = m431partition4UcCI2c(bArr, i, i2);
        int i3 = m431partition4UcCI2c - 1;
        if (i < i3) {
            m435quickSort4UcCI2c(bArr, i, i3);
        }
        if (m431partition4UcCI2c < i2) {
            m435quickSort4UcCI2c(bArr, m431partition4UcCI2c, i2);
        }
    }

    /* renamed from: partition-Aa5vz7o, reason: not valid java name */
    private static final int m432partitionAa5vz7o(short[] sArr, int i, int i2) {
        int i3;
        short m318getMh2AYeg = UShortArray.m318getMh2AYeg(sArr, (i + i2) / 2);
        while (i <= i2) {
            while (true) {
                int m318getMh2AYeg2 = UShortArray.m318getMh2AYeg(sArr, i) & UShort.MAX_VALUE;
                i3 = m318getMh2AYeg & UShort.MAX_VALUE;
                if (Intrinsics.compare(m318getMh2AYeg2, i3) >= 0) {
                    break;
                }
                i++;
            }
            while (Intrinsics.compare(UShortArray.m318getMh2AYeg(sArr, i2) & UShort.MAX_VALUE, i3) > 0) {
                i2--;
            }
            if (i <= i2) {
                short m318getMh2AYeg3 = UShortArray.m318getMh2AYeg(sArr, i);
                UShortArray.m323set01HTLdE(sArr, i, UShortArray.m318getMh2AYeg(sArr, i2));
                UShortArray.m323set01HTLdE(sArr, i2, m318getMh2AYeg3);
                i++;
                i2--;
            }
        }
        return i;
    }

    /* renamed from: quickSort-Aa5vz7o, reason: not valid java name */
    private static final void m436quickSortAa5vz7o(short[] sArr, int i, int i2) {
        int m432partitionAa5vz7o = m432partitionAa5vz7o(sArr, i, i2);
        int i3 = m432partitionAa5vz7o - 1;
        if (i < i3) {
            m436quickSortAa5vz7o(sArr, i, i3);
        }
        if (m432partitionAa5vz7o < i2) {
            m436quickSortAa5vz7o(sArr, m432partitionAa5vz7o, i2);
        }
    }

    /* renamed from: partition-oBK06Vg, reason: not valid java name */
    private static final int m433partitionoBK06Vg(int[] iArr, int i, int i2) {
        int m152getpVg5ArA = UIntArray.m152getpVg5ArA(iArr, (i + i2) / 2);
        while (i <= i2) {
            while (UnsignedKt.uintCompare(UIntArray.m152getpVg5ArA(iArr, i), m152getpVg5ArA) < 0) {
                i++;
            }
            while (UnsignedKt.uintCompare(UIntArray.m152getpVg5ArA(iArr, i2), m152getpVg5ArA) > 0) {
                i2--;
            }
            if (i <= i2) {
                int m152getpVg5ArA2 = UIntArray.m152getpVg5ArA(iArr, i);
                UIntArray.m157setVXSXFK8(iArr, i, UIntArray.m152getpVg5ArA(iArr, i2));
                UIntArray.m157setVXSXFK8(iArr, i2, m152getpVg5ArA2);
                i++;
                i2--;
            }
        }
        return i;
    }

    /* renamed from: quickSort-oBK06Vg, reason: not valid java name */
    private static final void m437quickSortoBK06Vg(int[] iArr, int i, int i2) {
        int m433partitionoBK06Vg = m433partitionoBK06Vg(iArr, i, i2);
        int i3 = m433partitionoBK06Vg - 1;
        if (i < i3) {
            m437quickSortoBK06Vg(iArr, i, i3);
        }
        if (m433partitionoBK06Vg < i2) {
            m437quickSortoBK06Vg(iArr, m433partitionoBK06Vg, i2);
        }
    }

    /* renamed from: partition--nroSd4, reason: not valid java name */
    private static final int m430partitionnroSd4(long[] jArr, int i, int i2) {
        long m222getsVKNKU = ULongArray.m222getsVKNKU(jArr, (i + i2) / 2);
        while (i <= i2) {
            while (UnsignedKt.ulongCompare(ULongArray.m222getsVKNKU(jArr, i), m222getsVKNKU) < 0) {
                i++;
            }
            while (UnsignedKt.ulongCompare(ULongArray.m222getsVKNKU(jArr, i2), m222getsVKNKU) > 0) {
                i2--;
            }
            if (i <= i2) {
                long m222getsVKNKU2 = ULongArray.m222getsVKNKU(jArr, i);
                ULongArray.m227setk8EXiF4(jArr, i, ULongArray.m222getsVKNKU(jArr, i2));
                ULongArray.m227setk8EXiF4(jArr, i2, m222getsVKNKU2);
                i++;
                i2--;
            }
        }
        return i;
    }

    /* renamed from: quickSort--nroSd4, reason: not valid java name */
    private static final void m434quickSortnroSd4(long[] jArr, int i, int i2) {
        int m430partitionnroSd4 = m430partitionnroSd4(jArr, i, i2);
        int i3 = m430partitionnroSd4 - 1;
        if (i < i3) {
            m434quickSortnroSd4(jArr, i, i3);
        }
        if (m430partitionnroSd4 < i2) {
            m434quickSortnroSd4(jArr, m430partitionnroSd4, i2);
        }
    }

    /* renamed from: sortArray-4UcCI2c, reason: not valid java name */
    public static final void m439sortArray4UcCI2c(byte[] array, int i, int i2) {
        Intrinsics.checkNotNullParameter(array, "array");
        m435quickSort4UcCI2c(array, i, i2 - 1);
    }

    /* renamed from: sortArray-Aa5vz7o, reason: not valid java name */
    public static final void m440sortArrayAa5vz7o(short[] array, int i, int i2) {
        Intrinsics.checkNotNullParameter(array, "array");
        m436quickSortAa5vz7o(array, i, i2 - 1);
    }

    /* renamed from: sortArray-oBK06Vg, reason: not valid java name */
    public static final void m441sortArrayoBK06Vg(int[] array, int i, int i2) {
        Intrinsics.checkNotNullParameter(array, "array");
        m437quickSortoBK06Vg(array, i, i2 - 1);
    }

    /* renamed from: sortArray--nroSd4, reason: not valid java name */
    public static final void m438sortArraynroSd4(long[] array, int i, int i2) {
        Intrinsics.checkNotNullParameter(array, "array");
        m434quickSortnroSd4(array, i, i2 - 1);
    }
}
