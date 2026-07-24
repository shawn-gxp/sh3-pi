package jp.co.nipro.cocoron.data.value;

import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Huffman.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0015\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000 $2\u00020\u0001:\u0001$B\u0005¢\u0006\u0002\u0010\u0002J\u0016\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0004J\u0016\u0010\u001e\u001a\u00020\u001a2\u0006\u0010\u001f\u001a\u00020\u001c2\u0006\u0010 \u001a\u00020\u0004J\u000e\u0010!\u001a\u00020\u00042\u0006\u0010\u001b\u001a\u00020\u001cJ\u0006\u0010\"\u001a\u00020#R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001a\u0010\f\u001a\u00020\rX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0012\u001a\u00020\u0013¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u001a\u0010\u0016\u001a\u00020\u0004X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0006\"\u0004\b\u0018\u0010\b¨\u0006%"}, d2 = {"Ljp/co/nipro/cocoron/data/value/Huffman;", "", "()V", "bits_in_buffer", "", "getBits_in_buffer", "()I", "setBits_in_buffer", "(I)V", "current_bit", "getCurrent_bit", "setCurrent_bit", "eof_buffer", "", "getEof_buffer", "()Z", "setEof_buffer", "(Z)V", "node", "Ljp/co/nipro/cocoron/data/value/HuffmanNodes;", "getNode", "()Ljp/co/nipro/cocoron/data/value/HuffmanNodes;", "num_nodes", "getNum_nodes", "setNum_nodes", "decode", "", "buffer", "", "size", "decode_bit_stream", "encoded_data", "original_size", "read_bit", "read_node", "", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class Huffman {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final int END_OF_BUFFER = -1;
    private int bits_in_buffer;
    private int current_bit;
    private boolean eof_buffer;
    private final HuffmanNodes node = new HuffmanNodes();
    private int num_nodes;

    /* compiled from: Huffman.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006¨\u0006\u0007"}, d2 = {"Ljp/co/nipro/cocoron/data/value/Huffman$Companion;", "", "()V", "END_OF_BUFFER", "", "getEND_OF_BUFFER", "()I", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final int getEND_OF_BUFFER() {
            return Huffman.END_OF_BUFFER;
        }
    }

    public final int getNum_nodes() {
        return this.num_nodes;
    }

    public final void setNum_nodes(int i) {
        this.num_nodes = i;
    }

    public final int getBits_in_buffer() {
        return this.bits_in_buffer;
    }

    public final void setBits_in_buffer(int i) {
        this.bits_in_buffer = i;
    }

    public final int getCurrent_bit() {
        return this.current_bit;
    }

    public final void setCurrent_bit(int i) {
        this.current_bit = i;
    }

    public final boolean getEof_buffer() {
        return this.eof_buffer;
    }

    public final void setEof_buffer(boolean z) {
        this.eof_buffer = z;
    }

    public final HuffmanNodes getNode() {
        return this.node;
    }

    public final int[] decode(byte[] buffer, int size) {
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        int[] decode_bit_stream = decode_bit_stream(buffer, size);
        decode_bit_stream[0] = decode_bit_stream[0] > 1023 ? decode_bit_stream[0] - 2048 : decode_bit_stream[0];
        int length = decode_bit_stream.length;
        for (int i = 1; i < length; i++) {
            decode_bit_stream[i] = decode_bit_stream[i - 1] + (decode_bit_stream[i] > 1023 ? decode_bit_stream[i] - 2048 : decode_bit_stream[i]);
        }
        return decode_bit_stream;
    }

    public final void read_node() {
        this.num_nodes = this.node.getNodeIndex().length;
    }

    public final int[] decode_bit_stream(byte[] encoded_data, int original_size) {
        Intrinsics.checkNotNullParameter(encoded_data, "encoded_data");
        int[] iArr = new int[original_size];
        int i = 0;
        for (int i2 = 0; i2 < original_size; i2++) {
            iArr[i2] = 0;
        }
        read_node();
        short s = this.node.getNodeIndex()[this.num_nodes - 1];
        this.eof_buffer = false;
        this.current_bit = 0;
        this.bits_in_buffer = 0;
        while (true) {
            int read_bit = read_bit(encoded_data);
            if (read_bit == END_OF_BUFFER) {
                break;
            }
            s = this.node.getNodeIndex()[(s * 2) - read_bit];
            if (s < 0) {
                iArr[i] = (-s) - 1;
                i++;
                if (i == original_size) {
                    break;
                }
                s = this.node.getNodeIndex()[this.num_nodes - 1];
            }
        }
        return iArr;
    }

    public final int read_bit(byte[] buffer) {
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        if (this.current_bit == this.bits_in_buffer) {
            if (this.eof_buffer) {
                return END_OF_BUFFER;
            }
            this.eof_buffer = true;
            this.bits_in_buffer = buffer.length << 3;
            this.current_bit = 0;
        }
        if (this.bits_in_buffer == 0) {
            return END_OF_BUFFER;
        }
        int i = this.current_bit;
        int i2 = (buffer[i >> 3] >> (7 - (i % 8))) & 1;
        this.current_bit = i + 1;
        return i2;
    }
}
