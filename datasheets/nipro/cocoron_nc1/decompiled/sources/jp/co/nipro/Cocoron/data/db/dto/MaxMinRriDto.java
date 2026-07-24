package jp.co.nipro.cocoron.data.db.dto;

import kotlin.Metadata;

/* compiled from: MaxMinRriDto.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001R\u0018\u0010\u0002\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\u0004\u0010\u0005\"\u0004\b\u0006\u0010\u0007R\u0018\u0010\b\u001a\u00020\u0003X¦\u000e¢\u0006\f\u001a\u0004\b\t\u0010\u0005\"\u0004\b\n\u0010\u0007R\u0018\u0010\u000b\u001a\u00020\fX¦\u000e¢\u0006\f\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010¨\u0006\u0011"}, d2 = {"Ljp/co/nipro/cocoron/data/db/dto/MaxMinRriDto;", "", "max", "", "getMax", "()I", "setMax", "(I)V", "min", "getMin", "setMin", "start", "", "getStart", "()J", "setStart", "(J)V", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public interface MaxMinRriDto {
    int getMax();

    int getMin();

    long getStart();

    void setMax(int i);

    void setMin(int i);

    void setStart(long j);
}
