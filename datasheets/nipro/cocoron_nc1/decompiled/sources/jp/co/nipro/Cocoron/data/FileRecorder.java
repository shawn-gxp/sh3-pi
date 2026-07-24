package jp.co.nipro.cocoron.data;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import jp.co.nipro.cocoron.common.BaseApplication;
import jp.co.nipro.cocoron.common.Config;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.text.Charsets;

/* compiled from: FileRecorder.kt */
@Metadata(bv = {1, 0, 3}, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\n\u0018\u0000 $2\u00020\u0001:\u0001$B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0014J\u0010\u0010\u0015\u001a\u00020\u000f2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017J\u0010\u0010\u0018\u001a\u00020\u000f2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017J\u000e\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0014J\u000e\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u001bJ\u000e\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u001bJ\u000e\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012J\u0016\u0010\u001f\u001a\u00020\u000f2\u0006\u0010 \u001a\u00020\u001b2\u0006\u0010!\u001a\u00020\u0014J\u000e\u0010\"\u001a\u00020\u000f2\u0006\u0010#\u001a\u00020\u001bR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\u0004R\u001c\u0010\b\u001a\u0004\u0018\u00010\tX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\r¨\u0006%"}, d2 = {"Ljp/co/nipro/cocoron/data/FileRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "getContext", "()Landroid/content/Context;", "setContext", "file", "Ljava/io/File;", "getFile", "()Ljava/io/File;", "setFile", "(Ljava/io/File;)V", "open", "", "writeBatteryLevel", "value", "", "writeConfig", "", "writeConnectGatt", "gatt", "Landroid/bluetooth/BluetoothGatt;", "writeDisconnectGatt", "writeECGMeasurement", "writeError", "", "writeOutService", "writePeripheralName", "writeRRTime", "writeSendBle", "type", "data", "writeText", "string", "Companion", "app_release"}, k = 1, mv = {1, 4, 2})
/* loaded from: classes.dex */
public final class FileRecorder {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static FileRecorder INSTANCE = null;
    public static final String TAG = "FileRecorder";
    private Context context;
    private File file;

    public FileRecorder(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        this.context = context;
    }

    public final File getFile() {
        return this.file;
    }

    public final void setFile(File file) {
        this.file = file;
    }

    public final Context getContext() {
        return this.context;
    }

    public final void setContext(Context context) {
        Intrinsics.checkNotNullParameter(context, "<set-?>");
        this.context = context;
    }

    /* compiled from: FileRecorder.kt */
    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u001a\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\nJ\u0006\u0010\f\u001a\u00020\u0004J\u0006\u0010\r\u001a\u00020\bJ\u0006\u0010\u000e\u001a\u00020\bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T¢\u0006\u0002\n\u0000¨\u0006\u000f"}, d2 = {"Ljp/co/nipro/cocoron/data/FileRecorder$Companion;", "", "()V", "INSTANCE", "Ljp/co/nipro/cocoron/data/FileRecorder;", "TAG", "", "copy", "", "src", "Ljava/io/File;", "dst", "getInstance", "moveFileToSd", "removeOldFile", "app_release"}, k = 1, mv = {1, 4, 2})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final FileRecorder getInstance() {
            if (FileRecorder.INSTANCE == null) {
                synchronized (Reflection.getOrCreateKotlinClass(FileRecorder.class)) {
                    FileRecorder.INSTANCE = new FileRecorder(BaseApplication.INSTANCE.getContext());
                    Unit unit = Unit.INSTANCE;
                }
            }
            FileRecorder fileRecorder = FileRecorder.INSTANCE;
            Intrinsics.checkNotNull(fileRecorder);
            return fileRecorder;
        }

        public final void removeOldFile() {
            final String format = new SimpleDateFormat("yyyy-MM", new Locale("en_GB")).format(new Date(System.currentTimeMillis() - Config.INSTANCE.getREMOVE_TIMECOUNT()));
            File[] listFiles = BaseApplication.INSTANCE.getContext().getFilesDir().listFiles(new FileFilter() { // from class: jp.co.nipro.cocoron.data.FileRecorder$Companion$removeOldFile$1
                @Override // java.io.FileFilter
                public boolean accept(File pathname) {
                    if (!Intrinsics.areEqual(pathname != null ? FilesKt.getExtension(pathname) : null, "csv")) {
                        return false;
                    }
                    String nameWithoutExtension = pathname != null ? FilesKt.getNameWithoutExtension(pathname) : null;
                    String removeLine = format;
                    Intrinsics.checkNotNullExpressionValue(removeLine, "removeLine");
                    return nameWithoutExtension.compareTo(removeLine) < 0;
                }
            });
            Intrinsics.checkNotNullExpressionValue(listFiles, "BaseApplication.context.…         }\n            })");
            for (File file : listFiles) {
                file.delete();
            }
        }

        public final void moveFileToSd() {
            Context context = BaseApplication.INSTANCE.getContext();
            File file = new File(context.getExternalFilesDir(null), "backfile");
            file.mkdir();
            File[] listFiles = BaseApplication.INSTANCE.getContext().getFilesDir().listFiles(new FileFilter() { // from class: jp.co.nipro.cocoron.data.FileRecorder$Companion$moveFileToSd$1
                @Override // java.io.FileFilter
                public boolean accept(File pathname) {
                    return !(Intrinsics.areEqual(pathname != null ? FilesKt.getExtension(pathname) : null, "csv") ^ true);
                }
            });
            Intrinsics.checkNotNullExpressionValue(listFiles, "BaseApplication.context.…         }\n            })");
            for (File it : listFiles) {
                Intrinsics.checkNotNullExpressionValue(it, "it");
                FileRecorder.INSTANCE.copy(it, new File(file, it.getName()));
            }
            copy(new File(Environment.getDataDirectory(), "//data/" + context.getPackageName() + "//databases//heartline.sqlite"), new File(file, "heartline.sqlite"));
        }

        public final void copy(File src, File dst) throws IOException {
            FileInputStream fileInputStream = new FileInputStream(src);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(dst);
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = fileInputStream.read(bArr);
                        if (read <= 0) {
                            return;
                        } else {
                            fileOutputStream.write(bArr, 0, read);
                        }
                    }
                } finally {
                    fileOutputStream.close();
                }
            } finally {
                fileInputStream.close();
            }
        }
    }

    public final void open() {
        this.file = new File(this.context.getFilesDir(), new SimpleDateFormat("yyyy-MM", new Locale("en_GB")).format(new Date()) + ".csv");
    }

    public final void writeText(String string) {
        Intrinsics.checkNotNullParameter(string, "string");
        open();
        String str = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", new Locale("en_GB")).format(new Date()) + "," + string + "\n";
        Writer outputStreamWriter = new OutputStreamWriter(new FileOutputStream(this.file, true), Charsets.UTF_8);
        BufferedWriter bufferedWriter = outputStreamWriter instanceof BufferedWriter ? (BufferedWriter) outputStreamWriter : new BufferedWriter(outputStreamWriter, 8192);
        Throwable th = (Throwable) null;
        try {
            bufferedWriter.write(str);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(bufferedWriter, th);
        } finally {
        }
    }

    public final void writePeripheralName(String value) {
        Intrinsics.checkNotNullParameter(value, "value");
        String format = String.format("SN, %s", Arrays.copyOf(new Object[]{value}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        writeText(format);
    }

    public final void writeBatteryLevel(int value) {
        String format = String.format("BATTERY, %d", Arrays.copyOf(new Object[]{Integer.valueOf(value)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        writeText(format);
    }

    public final void writeECGMeasurement(byte[] value) {
        Intrinsics.checkNotNullParameter(value, "value");
        StringBuilder sb = new StringBuilder();
        sb.append("ECG,");
        String joinToString$default = ArraysKt.joinToString$default(value, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.data.FileRecorder$writeECGMeasurement$writeString$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                return invoke(b.byteValue());
            }

            public final CharSequence invoke(byte b) {
                String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                return format;
            }
        }, 30, (Object) null);
        Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
        String upperCase = joinToString$default.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        writeText(sb.toString());
    }

    public final void writeRRTime(int value) {
        String format = String.format("RRI, %d", Arrays.copyOf(new Object[]{Integer.valueOf(value)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        writeText(format);
    }

    public final void writeOutService(int value) {
        String format = String.format("NONWEARING, %d", Arrays.copyOf(new Object[]{Integer.valueOf(value)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        writeText(format);
    }

    public final void writeConfig(byte[] value) {
        Intrinsics.checkNotNullParameter(value, "value");
        StringBuilder sb = new StringBuilder();
        sb.append("CONFIG,");
        String joinToString$default = ArraysKt.joinToString$default(value, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.data.FileRecorder$writeConfig$writeString$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                return invoke(b.byteValue());
            }

            public final CharSequence invoke(byte b) {
                String format = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
                return format;
            }
        }, 30, (Object) null);
        Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
        String upperCase = joinToString$default.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        writeText(sb.toString());
    }

    public final void writeError(String value) {
        Intrinsics.checkNotNullParameter(value, "value");
        String format = String.format("ERROR,%s", Arrays.copyOf(new Object[]{value}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        writeText(format);
    }

    public final void writeSendBle(String type, byte[] data) {
        Intrinsics.checkNotNullParameter(type, "type");
        Intrinsics.checkNotNullParameter(data, "data");
        StringBuilder sb = new StringBuilder();
        String format = String.format("SEND %s,", Arrays.copyOf(new Object[]{type}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(this, *args)");
        sb.append(format);
        String joinToString$default = ArraysKt.joinToString$default(data, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() { // from class: jp.co.nipro.cocoron.data.FileRecorder$writeSendBle$writeString$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Byte b) {
                return invoke(b.byteValue());
            }

            public final CharSequence invoke(byte b) {
                String format2 = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(b)}, 1));
                Intrinsics.checkNotNullExpressionValue(format2, "java.lang.String.format(this, *args)");
                return format2;
            }
        }, 30, (Object) null);
        Objects.requireNonNull(joinToString$default, "null cannot be cast to non-null type java.lang.String");
        String upperCase = joinToString$default.toUpperCase();
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
        sb.append(upperCase);
        writeText(sb.toString());
    }

    public final void writeConnectGatt(BluetoothGatt gatt) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("CONNECT gatt:");
        if (gatt == null || (str = gatt.toString()) == null) {
            str = "null";
        }
        sb.append(str);
        writeText(sb.toString());
    }

    public final void writeDisconnectGatt(BluetoothGatt gatt) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("DISCONNECT gatt:");
        if (gatt == null || (str = gatt.toString()) == null) {
            str = "null";
        }
        sb.append(str);
        writeText(sb.toString());
    }
}
