package com.google.android.gms.internal.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/* compiled from: com.google.android.gms:play-services-basement@@17.6.0 */
/* loaded from: classes.dex */
public abstract class zzu<E> extends zzq<E> implements List<E>, RandomAccess {
    private static final zzy<Object> zza = new zzs(zzw.zza, 0);

    zzu() {
    }

    public static <E> zzu<E> zzi() {
        return (zzu<E>) zzw.zza;
    }

    public static <E> zzu<E> zzj(E e) {
        Object[] objArr = {e};
        zzv.zza(objArr, 1);
        return zzn(objArr, 1);
    }

    public static <E> zzu<E> zzk(E e, E e2) {
        Object[] objArr = {e, e2};
        zzv.zza(objArr, 2);
        return zzn(objArr, 2);
    }

    public static <E> zzu<E> zzl(Iterable<? extends E> iterable) {
        if (iterable == null) {
            throw null;
        }
        if (iterable instanceof Collection) {
            return zzm((Collection) iterable);
        }
        Iterator<? extends E> it = iterable.iterator();
        if (!it.hasNext()) {
            return (zzu<E>) zzw.zza;
        }
        E next = it.next();
        if (!it.hasNext()) {
            return zzj(next);
        }
        zzr zzrVar = new zzr(4);
        zzrVar.zzb((zzr) next);
        zzrVar.zzc(it);
        zzrVar.zzc = true;
        return zzn(zzrVar.zza, zzrVar.zzb);
    }

    public static <E> zzu<E> zzm(Collection<? extends E> collection) {
        if (!(collection instanceof zzq)) {
            Object[] array = collection.toArray();
            int length = array.length;
            zzv.zza(array, length);
            return zzn(array, length);
        }
        zzu<E> zze = ((zzq) collection).zze();
        if (!zze.zzf()) {
            return zze;
        }
        Object[] array2 = zze.toArray();
        return zzn(array2, array2.length);
    }

    static <E> zzu<E> zzn(Object[] objArr, int i) {
        return i == 0 ? (zzu<E>) zzw.zza : new zzw(objArr, i);
    }

    @Override // java.util.List
    @Deprecated
    public final void add(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.List
    @Deprecated
    public final boolean addAll(int i, Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
    public final boolean contains(@NullableDecl Object obj) {
        return indexOf(obj) >= 0;
    }

    @Override // java.util.Collection, java.util.List
    public final boolean equals(@NullableDecl Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof List) {
            List list = (List) obj;
            int size = size();
            if (size == list.size()) {
                if (list instanceof RandomAccess) {
                    for (int i = 0; i < size; i++) {
                        if (zzk.zza(get(i), list.get(i))) {
                        }
                    }
                    return true;
                }
                Iterator<E> it = iterator();
                Iterator<E> it2 = list.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (!it2.hasNext() || !zzk.zza(it.next(), it2.next())) {
                            break;
                        }
                    } else if (!it2.hasNext()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override // java.util.Collection, java.util.List
    public final int hashCode() {
        int size = size();
        int i = 1;
        for (int i2 = 0; i2 < size; i2++) {
            i = (i * 31) + get(i2).hashCode();
        }
        return i;
    }

    @Override // java.util.List
    public final int indexOf(@NullableDecl Object obj) {
        if (obj == null) {
            return -1;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            if (obj.equals(get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override // com.google.android.gms.internal.common.zzq, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
    public final /* bridge */ /* synthetic */ Iterator iterator() {
        return listIterator(0);
    }

    @Override // java.util.List
    public final int lastIndexOf(@NullableDecl Object obj) {
        if (obj == null) {
            return -1;
        }
        for (int size = size() - 1; size >= 0; size--) {
            if (obj.equals(get(size))) {
                return size;
            }
        }
        return -1;
    }

    @Override // java.util.List
    public final /* bridge */ /* synthetic */ ListIterator listIterator() {
        return listIterator(0);
    }

    @Override // java.util.List
    @Deprecated
    public final E remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.List
    @Deprecated
    public final E set(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override // com.google.android.gms.internal.common.zzq
    /* renamed from: zza */
    public final zzx<E> iterator() {
        return listIterator(0);
    }

    @Override // com.google.android.gms.internal.common.zzq
    public final zzu<E> zze() {
        return this;
    }

    @Override // com.google.android.gms.internal.common.zzq
    int zzg(Object[] objArr, int i) {
        int size = size();
        for (int i2 = 0; i2 < size; i2++) {
            objArr[i2] = get(i2);
        }
        return size;
    }

    @Override // java.util.List
    /* renamed from: zzh */
    public zzu<E> subList(int i, int i2) {
        zzl.zzc(i, i2, size());
        int i3 = i2 - i;
        return i3 == size() ? this : i3 == 0 ? (zzu<E>) zzw.zza : new zzt(this, i, i3);
    }

    @Override // java.util.List
    /* renamed from: zzo, reason: merged with bridge method [inline-methods] */
    public final zzy<E> listIterator(int i) {
        zzl.zzb(i, size(), "index");
        return isEmpty() ? (zzy<E>) zza : new zzs(this, i);
    }
}
