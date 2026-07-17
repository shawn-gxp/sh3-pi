package crc645e9136c190843a3c;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* loaded from: classes.dex */
public class GridGroupRowCell extends LinearLayout implements IGCUserPeer {
    public static final String __md_methods = "";
    private ArrayList refList;

    static {
        Runtime.register("C1.Android.Grid.GridGroupRowCell, C1.Android.Grid", GridGroupRowCell.class, "");
    }

    public GridGroupRowCell(Context context) {
        super(context);
        if (GridGroupRowCell.class == GridGroupRowCell.class) {
            TypeManager.Activate("C1.Android.Grid.GridGroupRowCell, C1.Android.Grid", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public GridGroupRowCell(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (GridGroupRowCell.class == GridGroupRowCell.class) {
            TypeManager.Activate("C1.Android.Grid.GridGroupRowCell, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public GridGroupRowCell(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (GridGroupRowCell.class == GridGroupRowCell.class) {
            TypeManager.Activate("C1.Android.Grid.GridGroupRowCell, C1.Android.Grid", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, mscorlib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
