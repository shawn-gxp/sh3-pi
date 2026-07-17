package crc648da4b5c1bc226b8e;

import android.content.Context;
import android.util.AttributeSet;
import crc64f204578e57a29fff.C1Border;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class CalendarSlotPresenter extends C1Border implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Android.Calendar.CalendarSlotPresenter, C1.Android.Calendar", CalendarSlotPresenter.class, "");
    }

    public CalendarSlotPresenter(Context context) {
        super(context);
        if (CalendarSlotPresenter.class == CalendarSlotPresenter.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarSlotPresenter, C1.Android.Calendar", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public CalendarSlotPresenter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (CalendarSlotPresenter.class == CalendarSlotPresenter.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarSlotPresenter, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public CalendarSlotPresenter(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (CalendarSlotPresenter.class == CalendarSlotPresenter.class) {
            TypeManager.Activate("C1.Android.Calendar.CalendarSlotPresenter, C1.Android.Calendar", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // crc64f204578e57a29fff.C1Border, mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // crc64f204578e57a29fff.C1Border, mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
