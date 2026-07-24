package jp.co.nipro.cocoron.data.db.dto;

import java.util.Arrays;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ECGDto.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0012\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\t\u0010\u000f\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0010\u001a\u00020\u0005HÆ\u0003J\u001d\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0015\u001a\u00020\u0016HÖ\u0001J\t\u0010\u0017\u001a\u00020\u0018HÖ\u0001R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e¨\u0006\u0019"}, d2 = {"Ljp/co/nipro/cocoron/data/db/dto/ECGDto;", "", "date", "", "ecg", "", "(J[B)V", "getDate", "()J", "setDate", "(J)V", "getEcg", "()[B", "setEcg", "([B)V", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final /* data */ class ECGDto {
    private long date;
    private byte[] ecg;

    public static /* synthetic */ ECGDto copy$default(ECGDto eCGDto, long j, byte[] bArr, int i, Object obj) {
        if ((i & 1) != 0) {
            j = eCGDto.date;
        }
        if ((i & 2) != 0) {
            bArr = eCGDto.ecg;
        }
        return eCGDto.copy(j, bArr);
    }

    /* renamed from: component1, reason: from getter */
    public final long getDate() {
        return this.date;
    }

    /* renamed from: component2, reason: from getter */
    public final byte[] getEcg() {
        return this.ecg;
    }

    public final ECGDto copy(long date, byte[] ecg) {
        Intrinsics.checkNotNullParameter(ecg, "ecg");
        return new ECGDto(date, ecg);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ECGDto)) {
            return false;
        }
        ECGDto eCGDto = (ECGDto) other;
        return this.date == eCGDto.date && Intrinsics.areEqual(this.ecg, eCGDto.ecg);
    }

    public int hashCode() {
        int m0 = ECGDto$$ExternalSynthetic0.m0(this.date) * 31;
        byte[] bArr = this.ecg;
        return m0 + (bArr != null ? Arrays.hashCode(bArr) : 0);
    }

    public String toString() {
        return "ECGDto(date=" + this.date + ", ecg=" + Arrays.toString(this.ecg) + ")";
    }

    public ECGDto(long j, byte[] ecg) {
        Intrinsics.checkNotNullParameter(ecg, "ecg");
        this.date = j;
        this.ecg = ecg;
    }

    public final long getDate() {
        return this.date;
    }

    public final void setDate(long j) {
        this.date = j;
    }

    public final byte[] getEcg() {
        return this.ecg;
    }

    public final void setEcg(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "<set-?>");
        this.ecg = bArr;
    }
}
