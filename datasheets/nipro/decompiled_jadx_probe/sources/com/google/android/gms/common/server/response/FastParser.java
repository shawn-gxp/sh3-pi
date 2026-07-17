package com.google.android.gms.common.server.response;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RecentlyNonNull;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.ShowFirstParty;
import com.google.android.gms.common.server.response.FastJsonResponse;
import com.google.android.gms.common.util.Base64Utils;
import com.google.android.gms.common.util.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
@ShowFirstParty
@KeepForSdk
/* loaded from: classes.dex */
public class FastParser<T extends FastJsonResponse> {
    private static final char[] zaf = {'u', 'l', 'l'};
    private static final char[] zag = {'r', 'u', 'e'};
    private static final char[] zah = {'r', 'u', 'e', '\"'};
    private static final char[] zai = {'a', 'l', 's', 'e'};
    private static final char[] zaj = {'a', 'l', 's', 'e', '\"'};
    private static final char[] zak = {'\n'};
    private static final zai<Integer> zam = new zaa();
    private static final zai<Long> zan = new zab();
    private static final zai<Float> zao = new zac();
    private static final zai<Double> zap = new zad();
    private static final zai<Boolean> zaq = new zae();
    private static final zai<String> zar = new zaf();
    private static final zai<BigInteger> zas = new zag();
    private static final zai<BigDecimal> zat = new zah();
    private final char[] zaa = new char[1];
    private final char[] zab = new char[32];
    private final char[] zac = new char[1024];
    private final StringBuilder zad = new StringBuilder(32);
    private final StringBuilder zae = new StringBuilder(1024);
    private final Stack<Integer> zal = new Stack<>();

    /* compiled from: com.google.android.gms:play-services-base@@17.6.0 */
    @ShowFirstParty
    @KeepForSdk
    public static class ParseException extends Exception {
        public ParseException(@RecentlyNonNull String str) {
            super(str);
        }

        public ParseException(@RecentlyNonNull String str, @RecentlyNonNull Throwable th) {
            super("Error instantiating inner object", th);
        }

        public ParseException(@RecentlyNonNull Throwable th) {
            super(th);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0031, code lost:
    
        throw new com.google.android.gms.common.server.response.FastParser.ParseException("Unexpected control character while reading string");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static final String zaA(BufferedReader bufferedReader, char[] cArr, StringBuilder sb, @Nullable char[] cArr2) throws ParseException, IOException {
        sb.setLength(0);
        bufferedReader.mark(cArr.length);
        boolean z = false;
        boolean z2 = false;
        loop0: while (true) {
            int read = bufferedReader.read(cArr);
            if (read == -1) {
                throw new ParseException("Unexpected EOF while parsing string");
            }
            for (int i = 0; i < read; i++) {
                char c = cArr[i];
                if (Character.isISOControl(c)) {
                    if (cArr2 == null) {
                        break loop0;
                    }
                    for (char c2 : cArr2) {
                        if (c2 != c) {
                        }
                    }
                    break loop0;
                }
                if (c == '\"') {
                    if (!z2) {
                        sb.append(cArr, 0, i);
                        bufferedReader.reset();
                        bufferedReader.skip(i + 1);
                        return z ? JsonUtils.unescapeString(sb.toString()) : sb.toString();
                    }
                } else if (c == '\\') {
                    z2 = !z2;
                    z = true;
                }
                z2 = false;
            }
            sb.append(cArr, 0, read);
            bufferedReader.mark(cArr.length);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:18:0x028f A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0271 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final boolean zai(BufferedReader bufferedReader, FastJsonResponse fastJsonResponse) throws ParseException, IOException {
        int i;
        HashMap hashMap;
        char zaw;
        Map<String, FastJsonResponse.Field<?, ?>> fieldMappings = fastJsonResponse.getFieldMappings();
        String zaj2 = zaj(bufferedReader);
        if (zaj2 == null) {
            zaz(1);
            return false;
        }
        while (zaj2 != null) {
            FastJsonResponse.Field<?, ?> field = fieldMappings.get(zaj2);
            if (field == null) {
                zaj2 = zak(bufferedReader);
            } else {
                this.zal.push(4);
                int i2 = field.zaa;
                switch (i2) {
                    case 0:
                        if (field.zab) {
                            fastJsonResponse.zab(field, zam(bufferedReader, zam));
                        } else {
                            fastJsonResponse.zaa(field, zao(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                            zaj2 = zaj(bufferedReader);
                            break;
                        } else {
                            if (zaw != '}') {
                                StringBuilder sb = new StringBuilder(55);
                                sb.append("Expected end of object or field separator, but found: ");
                                sb.append(zaw);
                                throw new ParseException(sb.toString());
                            }
                            zaj2 = null;
                            break;
                        }
                    case 1:
                        if (field.zab) {
                            fastJsonResponse.zad(field, zam(bufferedReader, zas));
                        } else {
                            fastJsonResponse.zac(field, zaq(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 2:
                        if (field.zab) {
                            fastJsonResponse.zaf(field, zam(bufferedReader, zan));
                        } else {
                            fastJsonResponse.zae(field, zap(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 3:
                        if (field.zab) {
                            fastJsonResponse.zah(field, zam(bufferedReader, zao));
                        } else {
                            fastJsonResponse.zag(field, zas(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 4:
                        if (field.zab) {
                            fastJsonResponse.zaj(field, zam(bufferedReader, zap));
                        } else {
                            fastJsonResponse.zai(field, zat(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 5:
                        if (field.zab) {
                            fastJsonResponse.zal(field, zam(bufferedReader, zat));
                        } else {
                            fastJsonResponse.zak(field, zau(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 6:
                        if (field.zab) {
                            fastJsonResponse.zan(field, zam(bufferedReader, zaq));
                        } else {
                            fastJsonResponse.zam(field, zar(bufferedReader, false));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 7:
                        if (field.zab) {
                            fastJsonResponse.zap(field, zam(bufferedReader, zar));
                        } else {
                            fastJsonResponse.zao(field, zal(bufferedReader));
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 8:
                        fastJsonResponse.zaq(field, Base64Utils.decode(zan(bufferedReader, this.zac, this.zae, zak)));
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 9:
                        fastJsonResponse.zaq(field, Base64Utils.decodeUrlSafe(zan(bufferedReader, this.zac, this.zae, zak)));
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 10:
                        char zaw2 = zaw(bufferedReader);
                        if (zaw2 == 'n') {
                            zay(bufferedReader, zaf);
                            hashMap = null;
                        } else {
                            if (zaw2 != '{') {
                                throw new ParseException("Expected start of a map object");
                            }
                            this.zal.push(1);
                            hashMap = new HashMap();
                            while (true) {
                                char zaw3 = zaw(bufferedReader);
                                if (zaw3 == 0) {
                                    throw new ParseException("Unexpected EOF");
                                }
                                if (zaw3 == '\"') {
                                    String zaA = zaA(bufferedReader, this.zab, this.zad, null);
                                    if (zaw(bufferedReader) != ':') {
                                        String valueOf = String.valueOf(zaA);
                                        throw new ParseException(valueOf.length() != 0 ? "No map value found for key ".concat(valueOf) : new String("No map value found for key "));
                                    }
                                    if (zaw(bufferedReader) != '\"') {
                                        String valueOf2 = String.valueOf(zaA);
                                        throw new ParseException(valueOf2.length() != 0 ? "Expected String value for key ".concat(valueOf2) : new String("Expected String value for key "));
                                    }
                                    hashMap.put(zaA, zaA(bufferedReader, this.zab, this.zad, null));
                                    char zaw4 = zaw(bufferedReader);
                                    if (zaw4 != ',') {
                                        if (zaw4 != '}') {
                                            StringBuilder sb2 = new StringBuilder(48);
                                            sb2.append("Unexpected character while parsing string map: ");
                                            sb2.append(zaw4);
                                            throw new ParseException(sb2.toString());
                                        }
                                        zaz(1);
                                    }
                                } else if (zaw3 == '}') {
                                    zaz(1);
                                }
                                i = 4;
                                zaz(i);
                                zaz(2);
                                zaw = zaw(bufferedReader);
                                if (zaw == ',') {
                                }
                            }
                        }
                        fastJsonResponse.zar(field, hashMap);
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    case 11:
                        if (field.zab) {
                            char zaw5 = zaw(bufferedReader);
                            if (zaw5 == 'n') {
                                zay(bufferedReader, zaf);
                                fastJsonResponse.addConcreteTypeArrayInternal(field, field.zae, null);
                            } else {
                                this.zal.push(5);
                                if (zaw5 != '[') {
                                    throw new ParseException("Expected array start");
                                }
                                fastJsonResponse.addConcreteTypeArrayInternal(field, field.zae, zav(bufferedReader, field));
                            }
                        } else {
                            char zaw6 = zaw(bufferedReader);
                            if (zaw6 == 'n') {
                                zay(bufferedReader, zaf);
                                fastJsonResponse.addConcreteTypeInternal(field, field.zae, null);
                            } else {
                                this.zal.push(1);
                                if (zaw6 != '{') {
                                    throw new ParseException("Expected start of object");
                                }
                                try {
                                    FastJsonResponse zaf2 = field.zaf();
                                    zai(bufferedReader, zaf2);
                                    fastJsonResponse.addConcreteTypeInternal(field, field.zae, zaf2);
                                } catch (IllegalAccessException e) {
                                    throw new ParseException("Error instantiating inner object", e);
                                } catch (InstantiationException e2) {
                                    throw new ParseException("Error instantiating inner object", e2);
                                }
                            }
                        }
                        i = 4;
                        zaz(i);
                        zaz(2);
                        zaw = zaw(bufferedReader);
                        if (zaw == ',') {
                        }
                        break;
                    default:
                        StringBuilder sb3 = new StringBuilder(30);
                        sb3.append("Invalid field type ");
                        sb3.append(i2);
                        throw new ParseException(sb3.toString());
                }
            }
        }
        zaz(1);
        return true;
    }

    @Nullable
    private final String zaj(BufferedReader bufferedReader) throws ParseException, IOException {
        this.zal.push(2);
        char zaw = zaw(bufferedReader);
        if (zaw == '\"') {
            this.zal.push(3);
            String zaA = zaA(bufferedReader, this.zab, this.zad, null);
            zaz(3);
            if (zaw(bufferedReader) == ':') {
                return zaA;
            }
            throw new ParseException("Expected key/value separator");
        }
        if (zaw == ']') {
            zaz(2);
            zaz(1);
            zaz(5);
            return null;
        }
        if (zaw == '}') {
            zaz(2);
            return null;
        }
        StringBuilder sb = new StringBuilder(19);
        sb.append("Unexpected token: ");
        sb.append(zaw);
        throw new ParseException(sb.toString());
    }

    @Nullable
    private final String zak(BufferedReader bufferedReader) throws ParseException, IOException {
        bufferedReader.mark(1024);
        char zaw = zaw(bufferedReader);
        int i = 1;
        if (zaw == '\"') {
            if (bufferedReader.read(this.zaa) == -1) {
                throw new ParseException("Unexpected EOF while parsing string");
            }
            char c = this.zaa[0];
            boolean z = false;
            do {
                if (c == '\"') {
                    if (z) {
                        c = '\"';
                        z = true;
                    }
                }
                z = c == '\\' ? !z : false;
                if (bufferedReader.read(this.zaa) == -1) {
                    throw new ParseException("Unexpected EOF while parsing string");
                }
                c = this.zaa[0];
            } while (!Character.isISOControl(c));
            throw new ParseException("Unexpected control character while reading string");
        }
        if (zaw == ',') {
            throw new ParseException("Missing value");
        }
        if (zaw == '[') {
            this.zal.push(5);
            bufferedReader.mark(32);
            if (zaw(bufferedReader) == ']') {
                zaz(5);
            } else {
                bufferedReader.reset();
                boolean z2 = false;
                boolean z3 = false;
                while (i > 0) {
                    char zaw2 = zaw(bufferedReader);
                    if (zaw2 == 0) {
                        throw new ParseException("Unexpected EOF while parsing array");
                    }
                    if (Character.isISOControl(zaw2)) {
                        throw new ParseException("Unexpected control character while reading array");
                    }
                    if (zaw2 == '\"') {
                        if (!z3) {
                            z2 = !z2;
                        }
                        zaw2 = '\"';
                    }
                    if (zaw2 == '[') {
                        if (!z2) {
                            i++;
                        }
                        zaw2 = '[';
                    }
                    if (zaw2 == ']' && !z2) {
                        i--;
                    }
                    z3 = (zaw2 == '\\' && z2) ? !z3 : false;
                }
                zaz(5);
            }
        } else if (zaw != '{') {
            bufferedReader.reset();
            zax(bufferedReader, this.zac);
        } else {
            this.zal.push(1);
            bufferedReader.mark(32);
            char zaw3 = zaw(bufferedReader);
            if (zaw3 == '}') {
                zaz(1);
            } else {
                if (zaw3 != '\"') {
                    StringBuilder sb = new StringBuilder(18);
                    sb.append("Unexpected token ");
                    sb.append(zaw3);
                    throw new ParseException(sb.toString());
                }
                bufferedReader.reset();
                zaj(bufferedReader);
                while (zak(bufferedReader) != null) {
                }
                zaz(1);
            }
        }
        char zaw4 = zaw(bufferedReader);
        if (zaw4 == ',') {
            zaz(2);
            return zaj(bufferedReader);
        }
        if (zaw4 == '}') {
            zaz(2);
            return null;
        }
        StringBuilder sb2 = new StringBuilder(18);
        sb2.append("Unexpected token ");
        sb2.append(zaw4);
        throw new ParseException(sb2.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public final String zal(BufferedReader bufferedReader) throws ParseException, IOException {
        return zan(bufferedReader, this.zab, this.zad, null);
    }

    @Nullable
    private final <O> ArrayList<O> zam(BufferedReader bufferedReader, zai<O> zaiVar) throws ParseException, IOException {
        char zaw = zaw(bufferedReader);
        if (zaw == 'n') {
            zay(bufferedReader, zaf);
            return null;
        }
        if (zaw != '[') {
            throw new ParseException("Expected start of array");
        }
        this.zal.push(5);
        ArrayList<O> arrayList = new ArrayList<>();
        while (true) {
            bufferedReader.mark(1024);
            char zaw2 = zaw(bufferedReader);
            if (zaw2 == 0) {
                throw new ParseException("Unexpected EOF");
            }
            if (zaw2 != ',') {
                if (zaw2 == ']') {
                    zaz(5);
                    return arrayList;
                }
                bufferedReader.reset();
                arrayList.add(zaiVar.zaa(this, bufferedReader));
            }
        }
    }

    @Nullable
    private final String zan(BufferedReader bufferedReader, char[] cArr, StringBuilder sb, @Nullable char[] cArr2) throws ParseException, IOException {
        char zaw = zaw(bufferedReader);
        if (zaw == '\"') {
            return zaA(bufferedReader, cArr, sb, cArr2);
        }
        if (zaw != 'n') {
            throw new ParseException("Expected string");
        }
        zay(bufferedReader, zaf);
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int zao(BufferedReader bufferedReader) throws ParseException, IOException {
        int i;
        int i2;
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return 0;
        }
        char[] cArr = this.zac;
        if (zax <= 0) {
            throw new ParseException("No number to parse");
        }
        char c = cArr[0];
        int i3 = c == '-' ? Integer.MIN_VALUE : -2147483647;
        int i4 = c == '-' ? 1 : 0;
        if (i4 < zax) {
            i2 = i4 + 1;
            int digit = Character.digit(cArr[i4], 10);
            if (digit < 0) {
                throw new ParseException("Unexpected non-digit character");
            }
            i = -digit;
        } else {
            i = 0;
            i2 = i4;
        }
        while (i2 < zax) {
            int i5 = i2 + 1;
            int digit2 = Character.digit(cArr[i2], 10);
            if (digit2 < 0) {
                throw new ParseException("Unexpected non-digit character");
            }
            if (i < -214748364) {
                throw new ParseException("Number too large");
            }
            int i6 = i * 10;
            if (i6 < i3 + digit2) {
                throw new ParseException("Number too large");
            }
            i = i6 - digit2;
            i2 = i5;
        }
        if (i4 == 0) {
            return -i;
        }
        if (i2 > 1) {
            return i;
        }
        throw new ParseException("No digits to parse");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long zap(BufferedReader bufferedReader) throws ParseException, IOException {
        long j;
        int i;
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return 0L;
        }
        char[] cArr = this.zac;
        if (zax <= 0) {
            throw new ParseException("No number to parse");
        }
        char c = cArr[0];
        long j2 = c == '-' ? Long.MIN_VALUE : -9223372036854775807L;
        int i2 = c == '-' ? 1 : 0;
        if (i2 < zax) {
            i = i2 + 1;
            int digit = Character.digit(cArr[i2], 10);
            if (digit < 0) {
                throw new ParseException("Unexpected non-digit character");
            }
            j = -digit;
        } else {
            j = 0;
            i = i2;
        }
        while (i < zax) {
            int i3 = i + 1;
            int digit2 = Character.digit(cArr[i], 10);
            if (digit2 < 0) {
                throw new ParseException("Unexpected non-digit character");
            }
            if (j < -922337203685477580L) {
                throw new ParseException("Number too large");
            }
            long j3 = j * 10;
            int i4 = zax;
            long j4 = digit2;
            if (j3 < j2 + j4) {
                throw new ParseException("Number too large");
            }
            j = j3 - j4;
            zax = i4;
            i = i3;
        }
        if (i2 == 0) {
            return -j;
        }
        if (i > 1) {
            return j;
        }
        throw new ParseException("No digits to parse");
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public final BigInteger zaq(BufferedReader bufferedReader) throws ParseException, IOException {
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return null;
        }
        return new BigInteger(new String(this.zac, 0, zax));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean zar(BufferedReader bufferedReader, boolean z) throws ParseException, IOException {
        char zaw = zaw(bufferedReader);
        if (zaw == '\"') {
            if (z) {
                throw new ParseException("No boolean value found in string");
            }
            return zar(bufferedReader, true);
        }
        if (zaw == 'f') {
            zay(bufferedReader, z ? zaj : zai);
            return false;
        }
        if (zaw == 'n') {
            zay(bufferedReader, zaf);
            return false;
        }
        if (zaw == 't') {
            zay(bufferedReader, z ? zah : zag);
            return true;
        }
        StringBuilder sb = new StringBuilder(19);
        sb.append("Unexpected token: ");
        sb.append(zaw);
        throw new ParseException(sb.toString());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final float zas(BufferedReader bufferedReader) throws ParseException, IOException {
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return 0.0f;
        }
        return Float.parseFloat(new String(this.zac, 0, zax));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double zat(BufferedReader bufferedReader) throws ParseException, IOException {
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return 0.0d;
        }
        return Double.parseDouble(new String(this.zac, 0, zax));
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public final BigDecimal zau(BufferedReader bufferedReader) throws ParseException, IOException {
        int zax = zax(bufferedReader, this.zac);
        if (zax == 0) {
            return null;
        }
        return new BigDecimal(new String(this.zac, 0, zax));
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Nullable
    private final <T extends FastJsonResponse> ArrayList<T> zav(BufferedReader bufferedReader, FastJsonResponse.Field<?, ?> field) throws ParseException, IOException {
        ArrayList<T> arrayList = (ArrayList<T>) new ArrayList();
        char zaw = zaw(bufferedReader);
        if (zaw == ']') {
            zaz(5);
            return arrayList;
        }
        if (zaw == 'n') {
            zay(bufferedReader, zaf);
            zaz(5);
            return null;
        }
        if (zaw != '{') {
            StringBuilder sb = new StringBuilder(19);
            sb.append("Unexpected token: ");
            sb.append(zaw);
            throw new ParseException(sb.toString());
        }
        this.zal.push(1);
        while (true) {
            try {
                FastJsonResponse zaf2 = field.zaf();
                if (!zai(bufferedReader, zaf2)) {
                    return arrayList;
                }
                arrayList.add(zaf2);
                char zaw2 = zaw(bufferedReader);
                if (zaw2 != ',') {
                    if (zaw2 == ']') {
                        zaz(5);
                        return arrayList;
                    }
                    StringBuilder sb2 = new StringBuilder(19);
                    sb2.append("Unexpected token: ");
                    sb2.append(zaw2);
                    throw new ParseException(sb2.toString());
                }
                if (zaw(bufferedReader) != '{') {
                    throw new ParseException("Expected start of next object in array");
                }
                this.zal.push(1);
            } catch (IllegalAccessException e) {
                throw new ParseException("Error instantiating inner object", e);
            } catch (InstantiationException e2) {
                throw new ParseException("Error instantiating inner object", e2);
            }
        }
    }

    private final char zaw(BufferedReader bufferedReader) throws ParseException, IOException {
        if (bufferedReader.read(this.zaa) == -1) {
            return (char) 0;
        }
        while (Character.isWhitespace(this.zaa[0])) {
            if (bufferedReader.read(this.zaa) == -1) {
                return (char) 0;
            }
        }
        return this.zaa[0];
    }

    private final int zax(BufferedReader bufferedReader, char[] cArr) throws ParseException, IOException {
        int i;
        char zaw = zaw(bufferedReader);
        if (zaw == 0) {
            throw new ParseException("Unexpected EOF");
        }
        if (zaw == ',') {
            throw new ParseException("Missing value");
        }
        if (zaw == 'n') {
            zay(bufferedReader, zaf);
            return 0;
        }
        bufferedReader.mark(1024);
        if (zaw == '\"') {
            i = 0;
            boolean z = false;
            while (i < cArr.length && bufferedReader.read(cArr, i, 1) != -1) {
                char c = cArr[i];
                if (Character.isISOControl(c)) {
                    throw new ParseException("Unexpected control character while reading string");
                }
                if (c == '\"') {
                    if (!z) {
                        bufferedReader.reset();
                        bufferedReader.skip(i + 1);
                        return i;
                    }
                } else if (c == '\\') {
                    z = !z;
                    i++;
                }
                z = false;
                i++;
            }
        } else {
            cArr[0] = zaw;
            i = 1;
            while (i < cArr.length && bufferedReader.read(cArr, i, 1) != -1) {
                char c2 = cArr[i];
                if (c2 == '}' || c2 == ',' || Character.isWhitespace(c2) || cArr[i] == ']') {
                    bufferedReader.reset();
                    bufferedReader.skip(i - 1);
                    cArr[i] = 0;
                    return i;
                }
                i++;
            }
        }
        if (i == cArr.length) {
            throw new ParseException("Absurdly long value");
        }
        throw new ParseException("Unexpected EOF");
    }

    private final void zay(BufferedReader bufferedReader, char[] cArr) throws ParseException, IOException {
        int i = 0;
        while (true) {
            int length = cArr.length;
            if (i >= length) {
                return;
            }
            int read = bufferedReader.read(this.zab, 0, length - i);
            if (read == -1) {
                throw new ParseException("Unexpected EOF");
            }
            for (int i2 = 0; i2 < read; i2++) {
                if (cArr[i2 + i] != this.zab[i2]) {
                    throw new ParseException("Unexpected character");
                }
            }
            i += read;
        }
    }

    private final void zaz(int i) throws ParseException {
        if (this.zal.isEmpty()) {
            StringBuilder sb = new StringBuilder(46);
            sb.append("Expected state ");
            sb.append(i);
            sb.append(" but had empty stack");
            throw new ParseException(sb.toString());
        }
        int intValue = this.zal.pop().intValue();
        if (intValue == i) {
            return;
        }
        StringBuilder sb2 = new StringBuilder(46);
        sb2.append("Expected state ");
        sb2.append(i);
        sb2.append(" but had ");
        sb2.append(intValue);
        throw new ParseException(sb2.toString());
    }

    @KeepForSdk
    public void parse(@RecentlyNonNull InputStream inputStream, @RecentlyNonNull T t) throws ParseException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1024);
        try {
            try {
                this.zal.push(0);
                char zaw = zaw(bufferedReader);
                if (zaw == 0) {
                    throw new ParseException("No data to parse");
                }
                if (zaw == '[') {
                    this.zal.push(5);
                    Map<String, FastJsonResponse.Field<?, ?>> fieldMappings = t.getFieldMappings();
                    if (fieldMappings.size() != 1) {
                        throw new ParseException("Object array response class must have a single Field");
                    }
                    FastJsonResponse.Field<?, ?> value = fieldMappings.entrySet().iterator().next().getValue();
                    t.addConcreteTypeArrayInternal(value, value.zae, zav(bufferedReader, value));
                } else {
                    if (zaw != '{') {
                        StringBuilder sb = new StringBuilder(19);
                        sb.append("Unexpected token: ");
                        sb.append(zaw);
                        throw new ParseException(sb.toString());
                    }
                    this.zal.push(1);
                    zai(bufferedReader, t);
                }
                zaz(0);
            } catch (IOException e) {
                throw new ParseException(e);
            }
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException unused) {
                Log.w("FastParser", "Failed to close reader while parsing.");
            }
        }
    }
}
