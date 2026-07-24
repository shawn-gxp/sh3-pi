package jp.co.nipro.cocoron.data.db.dto;

import kotlin.Metadata;

/* compiled from: RRIDto.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\r\b\u0007\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005¢\u0006\u0002\u0010\u0007R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001a\u0010\u0006\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\r\"\u0004\b\u0011\u0010\u000f¨\u0006\u0012"}, d2 = {"Ljp/co/nipro/cocoron/data/db/dto/RRIDto;", "", "date", "", "rri", "", "outservice", "(JII)V", "getDate", "()J", "setDate", "(J)V", "getOutservice", "()I", "setOutservice", "(I)V", "getRri", "setRri", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class RRIDto {
    private long date;
    private int outservice;
    private int rri;

    public RRIDto(long j, int i, int i2) {
        this.date = j;
        this.rri = i;
        this.outservice = i2;
    }

    public final long getDate() {
        return this.date;
    }

    public final void setDate(long j) {
        this.date = j;
    }

    public final int getRri() {
        return this.rri;
    }

    public final void setRri(int i) {
        this.rri = i;
    }

    public final int getOutservice() {
        return this.outservice;
    }

    public final void setOutservice(int i) {
        this.outservice = i;
    }
}
