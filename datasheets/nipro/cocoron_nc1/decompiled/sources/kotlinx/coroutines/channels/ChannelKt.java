package kotlinx.coroutines.channels;

import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* compiled from: Channel.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u00022\b\b\u0002\u0010\u0003\u001a\u00020\u0004H\u0007\u001a>\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u00022\b\b\u0002\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u0016\b\u0002\u0010\u0007\u001a\u0010\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u00020\t\u0018\u00010\b¨\u0006\n"}, d2 = {"Channel", "Lkotlinx/coroutines/channels/Channel;", "E", "capacity", "", "onBufferOverflow", "Lkotlinx/coroutines/channels/BufferOverflow;", "onUndeliveredElement", "Lkotlin/Function1;", "", "kotlinx-coroutines-core"}, k = 2, mv = {1, 4, 0})
/* loaded from: classes.dex */
public final class ChannelKt {
    public static /* synthetic */ Channel Channel$default(int i, BufferOverflow bufferOverflow, Function1 function1, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        if ((i2 & 2) != 0) {
            bufferOverflow = BufferOverflow.SUSPEND;
        }
        if ((i2 & 4) != 0) {
            function1 = (Function1) null;
        }
        return Channel(i, bufferOverflow, function1);
    }

    public static final <E> Channel<E> Channel(int i, BufferOverflow bufferOverflow, Function1<? super E, Unit> function1) {
        ArrayChannel arrayChannel;
        ArrayChannel arrayChannel2;
        if (i == -2) {
            return new ArrayChannel(bufferOverflow == BufferOverflow.SUSPEND ? Channel.INSTANCE.getCHANNEL_DEFAULT_CAPACITY$kotlinx_coroutines_core() : 1, bufferOverflow, function1);
        }
        if (i == -1) {
            if ((bufferOverflow != BufferOverflow.SUSPEND ? 0 : 1) == 0) {
                throw new IllegalArgumentException("CONFLATED capacity cannot be used with non-default onBufferOverflow".toString());
            }
            return new ConflatedChannel(function1);
        }
        if (i == 0) {
            if (bufferOverflow == BufferOverflow.SUSPEND) {
                arrayChannel = new RendezvousChannel(function1);
            } else {
                arrayChannel = new ArrayChannel(1, bufferOverflow, function1);
            }
            return arrayChannel;
        }
        if (i == Integer.MAX_VALUE) {
            return new LinkedListChannel(function1);
        }
        if (i == 1 && bufferOverflow == BufferOverflow.DROP_OLDEST) {
            arrayChannel2 = new ConflatedChannel(function1);
        } else {
            arrayChannel2 = new ArrayChannel(i, bufferOverflow, function1);
        }
        return arrayChannel2;
    }

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Since 1.4.0, binary compatibility with earlier versions")
    public static final /* synthetic */ <E> Channel<E> Channel(int i) {
        return Channel$default(i, null, null, 6, null);
    }

    public static /* synthetic */ Channel Channel$default(int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        return Channel(i);
    }
}
