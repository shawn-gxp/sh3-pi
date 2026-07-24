package jp.co.nipro.cocoron.ui.viewmodel;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import jp.co.nipro.cocoron.common.Event;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: BaseModel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0016\u0018\u00002\u00020\u0001:\u0003\u0014\u0015\u0016B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010\u0013\u001a\u00020\u0012H\u0016R\u001d\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u001d\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\bR\u001d\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00050\u0004¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\bR\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00050\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\b¨\u0006\u0017"}, d2 = {"Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel;", "Landroidx/lifecycle/ViewModel;", "()V", "dialogEvent", "Landroidx/lifecycle/MutableLiveData;", "Ljp/co/nipro/cocoron/common/Event;", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$DialogInfo;", "getDialogEvent", "()Landroidx/lifecycle/MutableLiveData;", NotificationCompat.CATEGORY_EVENT, "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$EventParam;", "getEvent", "navEvent", "getNavEvent", "progressEvent", "Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$ProgressEvent;", "getProgressEvent", "setUp", "", "tearDown", "DialogInfo", "EventParam", "ProgressEvent", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public class BaseModel extends ViewModel {
    private final MutableLiveData<Event<ProgressEvent>> progressEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<DialogInfo>> dialogEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<EventParam>> navEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<EventParam>> event = new MutableLiveData<>();

    /* compiled from: BaseModel.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004¨\u0006\u0005"}, d2 = {"Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$ProgressEvent;", "", "(Ljava/lang/String;I)V", "START", "END", "app_release"}, k = 1, mv = {1, 4, 2})
    public enum ProgressEvent {
        START,
        END
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    /* compiled from: BaseModel.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u001b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BY\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\n\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007¢\u0006\u0002\u0010\u000bJ\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0011\u0010\u001f\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007HÆ\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u0011\u0010!\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007HÆ\u0003J]\u0010\"\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u00072\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\n\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007HÆ\u0001J\u0013\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010&\u001a\u00020'HÖ\u0001J\t\u0010(\u001a\u00020\u0003HÖ\u0001R\"\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\"\u0010\n\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\r\"\u0004\b\u0015\u0010\u000fR\u001c\u0010\t\u001a\u0004\u0018\u00010\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0011\"\u0004\b\u0017\u0010\u0013R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0011\"\u0004\b\u0019\u0010\u0013R\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u0011\"\u0004\b\u001b\u0010\u0013¨\u0006)"}, d2 = {"Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$DialogInfo;", "", "title", "", "message", "actionTitle", "action", "Lkotlin/Function0;", "", "cancelTitle", "cancel", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V", "getAction", "()Lkotlin/jvm/functions/Function0;", "setAction", "(Lkotlin/jvm/functions/Function0;)V", "getActionTitle", "()Ljava/lang/String;", "setActionTitle", "(Ljava/lang/String;)V", "getCancel", "setCancel", "getCancelTitle", "setCancelTitle", "getMessage", "setMessage", "getTitle", "setTitle", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final /* data */ class DialogInfo {
        private Function0<Unit> action;
        private String actionTitle;
        private Function0<Unit> cancel;
        private String cancelTitle;
        private String message;
        private String title;

        public DialogInfo() {
            this(null, null, null, null, null, null, 63, null);
        }

        public static /* synthetic */ DialogInfo copy$default(DialogInfo dialogInfo, String str, String str2, String str3, Function0 function0, String str4, Function0 function02, int i, Object obj) {
            if ((i & 1) != 0) {
                str = dialogInfo.title;
            }
            if ((i & 2) != 0) {
                str2 = dialogInfo.message;
            }
            String str5 = str2;
            if ((i & 4) != 0) {
                str3 = dialogInfo.actionTitle;
            }
            String str6 = str3;
            if ((i & 8) != 0) {
                function0 = dialogInfo.action;
            }
            Function0 function03 = function0;
            if ((i & 16) != 0) {
                str4 = dialogInfo.cancelTitle;
            }
            String str7 = str4;
            if ((i & 32) != 0) {
                function02 = dialogInfo.cancel;
            }
            return dialogInfo.copy(str, str5, str6, function03, str7, function02);
        }

        /* renamed from: component1, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        /* renamed from: component2, reason: from getter */
        public final String getMessage() {
            return this.message;
        }

        /* renamed from: component3, reason: from getter */
        public final String getActionTitle() {
            return this.actionTitle;
        }

        public final Function0<Unit> component4() {
            return this.action;
        }

        /* renamed from: component5, reason: from getter */
        public final String getCancelTitle() {
            return this.cancelTitle;
        }

        public final Function0<Unit> component6() {
            return this.cancel;
        }

        public final DialogInfo copy(String title, String message, String actionTitle, Function0<Unit> action, String cancelTitle, Function0<Unit> cancel) {
            return new DialogInfo(title, message, actionTitle, action, cancelTitle, cancel);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof DialogInfo)) {
                return false;
            }
            DialogInfo dialogInfo = (DialogInfo) other;
            return Intrinsics.areEqual(this.title, dialogInfo.title) && Intrinsics.areEqual(this.message, dialogInfo.message) && Intrinsics.areEqual(this.actionTitle, dialogInfo.actionTitle) && Intrinsics.areEqual(this.action, dialogInfo.action) && Intrinsics.areEqual(this.cancelTitle, dialogInfo.cancelTitle) && Intrinsics.areEqual(this.cancel, dialogInfo.cancel);
        }

        public int hashCode() {
            String str = this.title;
            int hashCode = (str != null ? str.hashCode() : 0) * 31;
            String str2 = this.message;
            int hashCode2 = (hashCode + (str2 != null ? str2.hashCode() : 0)) * 31;
            String str3 = this.actionTitle;
            int hashCode3 = (hashCode2 + (str3 != null ? str3.hashCode() : 0)) * 31;
            Function0<Unit> function0 = this.action;
            int hashCode4 = (hashCode3 + (function0 != null ? function0.hashCode() : 0)) * 31;
            String str4 = this.cancelTitle;
            int hashCode5 = (hashCode4 + (str4 != null ? str4.hashCode() : 0)) * 31;
            Function0<Unit> function02 = this.cancel;
            return hashCode5 + (function02 != null ? function02.hashCode() : 0);
        }

        public String toString() {
            return "DialogInfo(title=" + this.title + ", message=" + this.message + ", actionTitle=" + this.actionTitle + ", action=" + this.action + ", cancelTitle=" + this.cancelTitle + ", cancel=" + this.cancel + ")";
        }

        public DialogInfo(String str, String str2, String str3, Function0<Unit> function0, String str4, Function0<Unit> function02) {
            this.title = str;
            this.message = str2;
            this.actionTitle = str3;
            this.action = function0;
            this.cancelTitle = str4;
            this.cancel = function02;
        }

        public /* synthetic */ DialogInfo(String str, String str2, String str3, Function0 function0, String str4, Function0 function02, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? (String) null : str, (i & 2) != 0 ? (String) null : str2, (i & 4) != 0 ? (String) null : str3, (i & 8) != 0 ? (Function0) null : function0, (i & 16) != 0 ? (String) null : str4, (i & 32) != 0 ? (Function0) null : function02);
        }

        public final String getTitle() {
            return this.title;
        }

        public final void setTitle(String str) {
            this.title = str;
        }

        public final String getMessage() {
            return this.message;
        }

        public final void setMessage(String str) {
            this.message = str;
        }

        public final String getActionTitle() {
            return this.actionTitle;
        }

        public final void setActionTitle(String str) {
            this.actionTitle = str;
        }

        public final Function0<Unit> getAction() {
            return this.action;
        }

        public final void setAction(Function0<Unit> function0) {
            this.action = function0;
        }

        public final String getCancelTitle() {
            return this.cancelTitle;
        }

        public final void setCancelTitle(String str) {
            this.cancelTitle = str;
        }

        public final Function0<Unit> getCancel() {
            return this.cancel;
        }

        public final void setCancel(Function0<Unit> function0) {
            this.cancel = function0;
        }
    }

    /* compiled from: BaseModel.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0001¢\u0006\u0002\u0010\u0005J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J\u000b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÆ\u0003J\u001f\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0001HÆ\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0014\u001a\u00020\u0015HÖ\u0001J\t\u0010\u0016\u001a\u00020\u0003HÖ\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0001X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\r¨\u0006\u0017"}, d2 = {"Ljp/co/nipro/cocoron/ui/viewmodel/BaseModel$EventParam;", "", "name", "", "value", "(Ljava/lang/String;Ljava/lang/Object;)V", "getName", "()Ljava/lang/String;", "setName", "(Ljava/lang/String;)V", "getValue", "()Ljava/lang/Object;", "setValue", "(Ljava/lang/Object;)V", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final /* data */ class EventParam {
        private String name;
        private Object value;

        public static /* synthetic */ EventParam copy$default(EventParam eventParam, String str, Object obj, int i, Object obj2) {
            if ((i & 1) != 0) {
                str = eventParam.name;
            }
            if ((i & 2) != 0) {
                obj = eventParam.value;
            }
            return eventParam.copy(str, obj);
        }

        /* renamed from: component1, reason: from getter */
        public final String getName() {
            return this.name;
        }

        /* renamed from: component2, reason: from getter */
        public final Object getValue() {
            return this.value;
        }

        public final EventParam copy(String name, Object value) {
            Intrinsics.checkNotNullParameter(name, "name");
            return new EventParam(name, value);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof EventParam)) {
                return false;
            }
            EventParam eventParam = (EventParam) other;
            return Intrinsics.areEqual(this.name, eventParam.name) && Intrinsics.areEqual(this.value, eventParam.value);
        }

        public int hashCode() {
            String str = this.name;
            int hashCode = (str != null ? str.hashCode() : 0) * 31;
            Object obj = this.value;
            return hashCode + (obj != null ? obj.hashCode() : 0);
        }

        public String toString() {
            return "EventParam(name=" + this.name + ", value=" + this.value + ")";
        }

        public EventParam(String name, Object obj) {
            Intrinsics.checkNotNullParameter(name, "name");
            this.name = name;
            this.value = obj;
        }

        public final String getName() {
            return this.name;
        }

        public final void setName(String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.name = str;
        }

        public /* synthetic */ EventParam(String str, Object obj, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(str, (i & 2) != 0 ? null : obj);
        }

        public final Object getValue() {
            return this.value;
        }

        public final void setValue(Object obj) {
            this.value = obj;
        }
    }

    public final MutableLiveData<Event<ProgressEvent>> getProgressEvent() {
        return this.progressEvent;
    }

    public final MutableLiveData<Event<DialogInfo>> getDialogEvent() {
        return this.dialogEvent;
    }

    public final MutableLiveData<Event<EventParam>> getNavEvent() {
        return this.navEvent;
    }

    public final MutableLiveData<Event<EventParam>> getEvent() {
        return this.event;
    }
}
