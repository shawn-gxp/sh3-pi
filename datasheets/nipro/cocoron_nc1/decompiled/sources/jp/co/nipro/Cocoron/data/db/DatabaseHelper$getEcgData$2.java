package jp.co.nipro.cocoron.data.db;

import com.contrarywind.timer.MessageHandler;
import java.util.ArrayList;
import java.util.List;
import jp.co.nipro.cocoron.data.db.DatabaseHelper;
import jp.co.nipro.cocoron.data.db.dto.ECGDto;
import jp.co.nipro.cocoron.data.entity.ECGData;
import jp.co.nipro.cocoron.data.value.ECGMeasurementCharacteristicValue;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "Ljp/co/nipro/cocoron/data/entity/ECGData;", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$getEcgData$2", f = "DatabaseHelper.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class DatabaseHelper$getEcgData$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ECGData>, Object> {
    final /* synthetic */ long $date;
    final /* synthetic */ long $range;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$getEcgData$2(DatabaseHelper databaseHelper, long j, long j2, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$date = j;
        this.$range = j2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$getEcgData$2(this.this$0, this.$date, this.$range, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ECGData> continuation) {
        return ((DatabaseHelper$getEcgData$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper.DataBase dataBase;
        long j;
        long j2;
        DatabaseHelper.DataBase dataBase2;
        DatabaseHelper.DataBase dataBase3;
        ArrayList arrayList;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            ECGData eCGData = new ECGData();
            long j3 = this.$date;
            do {
                dataBase = this.this$0.database;
                ECGDto filterOne = dataBase.ecgDao().filterOne(j3, MessageHandler.WHAT_SMOOTH_SCROLL + j3);
                j = 0;
                j2 = 255;
                if (filterOne != null) {
                    j3 = filterOne.getDate();
                    if (eCGData.getStartDate() == 0) {
                        eCGData.setStartDate(j3);
                    }
                    ECGMeasurementCharacteristicValue eCGMeasurementCharacteristicValue = new ECGMeasurementCharacteristicValue();
                    eCGMeasurementCharacteristicValue.readECGMeasurementPacket(filterOne.getEcg());
                    if (eCGMeasurementCharacteristicValue.getTrigger1() != 255) {
                        eCGData.getTrigger().add(Boxing.boxLong(eCGMeasurementCharacteristicValue.getTrigger1() + eCGData.getEcg().size()));
                    }
                    if (eCGMeasurementCharacteristicValue.getTrigger2() != 255) {
                        eCGData.getTrigger().add(Boxing.boxLong(eCGMeasurementCharacteristicValue.getTrigger2() + eCGData.getEcg().size()));
                    }
                } else {
                    filterOne = null;
                }
                if (filterOne == null) {
                    break;
                }
            } while (j3 - this.$date <= 10000);
            if (eCGData.getEcg().size() > 0 && j3 - this.$date > 10000) {
                return eCGData;
            }
            ECGData eCGData2 = new ECGData();
            dataBase2 = this.this$0.database;
            ECGDto filterOne2 = dataBase2.ecgDao().filterOne(j3, this.$range + j3);
            if (filterOne2 == null) {
                return null;
            }
            eCGData2.setStartDate(filterOne2.getDate());
            long date = filterOne2.getDate();
            long j4 = date;
            while (true) {
                dataBase3 = this.this$0.database;
                ECGDto filterOne3 = dataBase3.ecgDao().filterOne(j4, this.$range + j4);
                if (filterOne3 != null) {
                    j4 = filterOne3.getDate();
                    if (eCGData2.getStartDate() == j) {
                        eCGData2.setStartDate(j4);
                    }
                    ECGMeasurementCharacteristicValue eCGMeasurementCharacteristicValue2 = new ECGMeasurementCharacteristicValue();
                    double[] readECGMeasurementPacket = eCGMeasurementCharacteristicValue2.readECGMeasurementPacket(filterOne3.getEcg());
                    if (eCGMeasurementCharacteristicValue2.getTrigger1() != j2) {
                        eCGData2.getTrigger().add(Boxing.boxLong(eCGMeasurementCharacteristicValue2.getTrigger1() + eCGData2.getEcg().size()));
                    }
                    if (eCGMeasurementCharacteristicValue2.getTrigger2() != j2) {
                        eCGData2.getTrigger().add(Boxing.boxLong(eCGMeasurementCharacteristicValue2.getTrigger2() + eCGData2.getEcg().size()));
                    }
                    List<Double> ecg = eCGData2.getEcg();
                    if (readECGMeasurementPacket == null || (arrayList = ArraysKt.toList(readECGMeasurementPacket)) == null) {
                        arrayList = new ArrayList();
                    }
                    ecg.addAll(arrayList);
                } else {
                    filterOne3 = null;
                }
                if (filterOne3 == null || j4 - date > 10000) {
                    break;
                }
                j = 0;
                j2 = 255;
            }
            return eCGData2;
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
}
