package jp.co.nipro.cocoron.data.db.dto;

import kotlin.Metadata;

/* compiled from: MaxMinRriDto.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0087\b\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005¢\u0006\u0002\u0010\u0007J\t\u0010\u0012\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0005HÆ\u0003J'\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0005HÆ\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019HÖ\u0003J\t\u0010\u001a\u001a\u00020\u0005HÖ\u0001J\t\u0010\u001b\u001a\u00020\u001cHÖ\u0001R\u001a\u0010\u0004\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0006\u001a\u00020\u0005X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\t\"\u0004\b\r\u0010\u000bR\u001e\u0010\u0002\u001a\u00020\u00038\u0016@\u0016X\u0097\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011¨\u0006\u001e"}, d2 = {"Ljp/co/nipro/cocoron/data/db/dto/MaxMinRri1mDto;", "Ljp/co/nipro/cocoron/data/db/dto/MaxMinRriDto;", "start", "", "max", "", "min", "(JII)V", "getMax", "()I", "setMax", "(I)V", "getMin", "setMin", "getStart", "()J", "setStart", "(J)V", "component1", "component2", "component3", "copy", "equals", "", "other", "", "hashCode", "toString", "", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final /* data */ class MaxMinRri1mDto implements MaxMinRriDto {
    public static final long cnt = 60;
    public static final long duration = 43200000;
    public static final long range = 2592000000L;
    private int max;
    private int min;
    private long start;

    public static /* synthetic */ MaxMinRri1mDto copy$default(MaxMinRri1mDto maxMinRri1mDto, long j, int i, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            j = maxMinRri1mDto.getStart();
        }
        if ((i3 & 2) != 0) {
            i = maxMinRri1mDto.getMax();
        }
        if ((i3 & 4) != 0) {
            i2 = maxMinRri1mDto.getMin();
        }
        return maxMinRri1mDto.copy(j, i, i2);
    }

    public final long component1() {
        return getStart();
    }

    public final int component2() {
        return getMax();
    }

    public final int component3() {
        return getMin();
    }

    public final MaxMinRri1mDto copy(long start, int max, int min) {
        return new MaxMinRri1mDto(start, max, min);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MaxMinRri1mDto)) {
            return false;
        }
        MaxMinRri1mDto maxMinRri1mDto = (MaxMinRri1mDto) other;
        return getStart() == maxMinRri1mDto.getStart() && getMax() == maxMinRri1mDto.getMax() && getMin() == maxMinRri1mDto.getMin();
    }

    public int hashCode() {
        return (((ECGDto$$ExternalSynthetic0.m0(getStart()) * 31) + getMax()) * 31) + getMin();
    }

    public String toString() {
        return "MaxMinRri1mDto(start=" + getStart() + ", max=" + getMax() + ", min=" + getMin() + ")";
    }

    public MaxMinRri1mDto(long j, int i, int i2) {
        this.start = j;
        this.max = i;
        this.min = i2;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public long getStart() {
        return this.start;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public void setStart(long j) {
        this.start = j;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public int getMax() {
        return this.max;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public void setMax(int i) {
        this.max = i;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public int getMin() {
        return this.min;
    }

    @Override // jp.co.nipro.cocoron.data.db.dto.MaxMinRriDto
    public void setMin(int i) {
        this.min = i;
    }
}
