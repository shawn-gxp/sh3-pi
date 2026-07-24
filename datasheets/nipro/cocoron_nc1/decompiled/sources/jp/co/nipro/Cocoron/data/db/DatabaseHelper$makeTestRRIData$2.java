package jp.co.nipro.cocoron.data.db;

import java.util.Calendar;
import java.util.Date;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DatabaseHelper.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@¢\u0006\u0004\b\u0003\u0010\u0004"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, k = 3, mv = {1, 4, 2})
@DebugMetadata(c = "jp.co.nipro.cocoron.data.db.DatabaseHelper$makeTestRRIData$2", f = "DatabaseHelper.kt", i = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11}, l = {331, 332, 333, 340, 341, 342, 348, 349, 350, 356, 357, 358}, m = "invokeSuspend", n = {"now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d", "now", "calendar", "date_12h", "date_24h", "date_7d"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4", "L$0", "L$1", "L$2", "L$3", "L$4"})
/* loaded from: classes.dex */
final class DatabaseHelper$makeTestRRIData$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $mode;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    int label;
    final /* synthetic */ DatabaseHelper this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    DatabaseHelper$makeTestRRIData$2(DatabaseHelper databaseHelper, int i, Continuation continuation) {
        super(2, continuation);
        this.this$0 = databaseHelper;
        this.$mode = i;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        return new DatabaseHelper$makeTestRRIData$2(this.this$0, this.$mode, completion);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((DatabaseHelper$makeTestRRIData$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x00a8  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0279  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:19:0x0115 -> B:7:0x0270). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:64:0x024a -> B:7:0x0270). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:67:0x026d -> B:7:0x0270). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object invokeSuspend(Object obj) {
        DatabaseHelper$makeTestRRIData$2 databaseHelper$makeTestRRIData$2;
        Date date;
        Date date2;
        Calendar calendar;
        Date date3;
        Date date4;
        char c;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = 5;
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure(obj);
                Date date5 = new Date();
                Calendar calendar2 = Calendar.getInstance();
                Intrinsics.checkNotNullExpressionValue(calendar2, "Calendar.getInstance()");
                calendar2.add(2, -1);
                Calendar it = Calendar.getInstance();
                it.add(11, -12);
                Intrinsics.checkNotNullExpressionValue(it, "it");
                Date time = it.getTime();
                Calendar it2 = Calendar.getInstance();
                it2.add(5, -1);
                Intrinsics.checkNotNullExpressionValue(it2, "it");
                Date time2 = it2.getTime();
                Calendar it3 = Calendar.getInstance();
                it3.add(5, -8);
                Intrinsics.checkNotNullExpressionValue(it3, "it");
                Date time3 = it3.getTime();
                databaseHelper$makeTestRRIData$2 = this;
                date = date5;
                date2 = time3;
                calendar = calendar2;
                date3 = time2;
                date4 = time;
                if (calendar.getTime().compareTo(date) < 0) {
                    if (calendar.getTime().compareTo(date4) > 0) {
                        int i2 = databaseHelper$makeTestRRIData$2.$mode;
                        if (i2 == 0) {
                            DatabaseHelper databaseHelper = databaseHelper$makeTestRRIData$2.this$0;
                            Date time4 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 1;
                            if (databaseHelper.insertRRI(0, 0, time4, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i2 == 1) {
                            DatabaseHelper databaseHelper2 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine = databaseHelper2.randomLine(30, 121);
                            Date time5 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 2;
                            if (databaseHelper2.insertRRI(randomLine, 0, time5, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i2 == 2) {
                            DatabaseHelper databaseHelper3 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine2 = databaseHelper3.randomLine(30, 181);
                            Date time6 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 3;
                            if (databaseHelper3.insertRRI(randomLine2, 0, time6, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        }
                    } else if (calendar.getTime().compareTo(date3) > 0) {
                        int i3 = databaseHelper$makeTestRRIData$2.$mode;
                        if (i3 == 0) {
                            DatabaseHelper databaseHelper4 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine3 = databaseHelper4.randomLine(30, 90);
                            Date time7 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 4;
                            if (databaseHelper4.insertRRI(randomLine3, 0, time7, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i3 == 1) {
                            DatabaseHelper databaseHelper5 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine4 = databaseHelper5.randomLine(30, 150);
                            Date time8 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = i;
                            if (databaseHelper5.insertRRI(randomLine4, 0, time8, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i3 == 2) {
                            DatabaseHelper databaseHelper6 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine5 = databaseHelper6.randomLine(30, 240);
                            Date time9 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 6;
                            if (databaseHelper6.insertRRI(randomLine5, 0, time9, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        }
                    } else if (calendar.getTime().compareTo(date2) > 0) {
                        int i4 = databaseHelper$makeTestRRIData$2.$mode;
                        if (i4 == 0) {
                            DatabaseHelper databaseHelper7 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine6 = databaseHelper7.randomLine(30, 91);
                            Date time10 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 7;
                            if (databaseHelper7.insertRRI(randomLine6, 0, time10, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i4 == 1) {
                            DatabaseHelper databaseHelper8 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine7 = databaseHelper8.randomLine(30, 151);
                            Date time11 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 8;
                            if (databaseHelper8.insertRRI(randomLine7, 0, time11, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i4 == 2) {
                            DatabaseHelper databaseHelper9 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine8 = databaseHelper9.randomLine(30, 241);
                            Date time12 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 9;
                            if (databaseHelper9.insertRRI(randomLine8, 0, time12, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        }
                    } else {
                        int i5 = databaseHelper$makeTestRRIData$2.$mode;
                        if (i5 == 0) {
                            c = 11;
                            DatabaseHelper databaseHelper10 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine9 = databaseHelper10.randomLine(30, 120);
                            Date time13 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 10;
                            if (databaseHelper10.insertRRI(randomLine9, 0, time13, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i5 == 1) {
                            DatabaseHelper databaseHelper11 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine10 = databaseHelper11.randomLine(30, 180);
                            Date time14 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            c = 11;
                            databaseHelper$makeTestRRIData$2.label = 11;
                            if (databaseHelper11.insertRRI(randomLine10, 0, time14, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        } else if (i5 == 2) {
                            DatabaseHelper databaseHelper12 = databaseHelper$makeTestRRIData$2.this$0;
                            int randomLine11 = databaseHelper12.randomLine(30, 350);
                            Date time15 = calendar.getTime();
                            databaseHelper$makeTestRRIData$2.L$0 = date;
                            databaseHelper$makeTestRRIData$2.L$1 = calendar;
                            databaseHelper$makeTestRRIData$2.L$2 = date4;
                            databaseHelper$makeTestRRIData$2.L$3 = date3;
                            databaseHelper$makeTestRRIData$2.L$4 = date2;
                            databaseHelper$makeTestRRIData$2.label = 12;
                            if (databaseHelper12.insertRRI(randomLine11, 0, time15, databaseHelper$makeTestRRIData$2) == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                        }
                        calendar.add(13, 30);
                        i = 5;
                        if (calendar.getTime().compareTo(date) < 0) {
                        }
                    }
                    c = 11;
                    calendar.add(13, 30);
                    i = 5;
                    if (calendar.getTime().compareTo(date) < 0) {
                    }
                } else {
                    return Unit.INSTANCE;
                }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 12:
                date2 = (Date) this.L$4;
                date3 = (Date) this.L$3;
                date4 = (Date) this.L$2;
                calendar = (Calendar) this.L$1;
                date = (Date) this.L$0;
                ResultKt.throwOnFailure(obj);
                databaseHelper$makeTestRRIData$2 = this;
                c = 11;
                calendar.add(13, 30);
                i = 5;
                if (calendar.getTime().compareTo(date) < 0) {
                }
                break;
            case 10:
            case 11:
                date2 = (Date) this.L$4;
                date3 = (Date) this.L$3;
                date4 = (Date) this.L$2;
                calendar = (Calendar) this.L$1;
                date = (Date) this.L$0;
                ResultKt.throwOnFailure(obj);
                databaseHelper$makeTestRRIData$2 = this;
                c = 11;
                calendar.add(13, 30);
                i = 5;
                if (calendar.getTime().compareTo(date) < 0) {
                }
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
