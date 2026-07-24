package jp.co.nipro.cocoron.common.extension;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/* loaded from: classes.dex */
public class WeakIdentityHashMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1073741824;
    private static final Object NULL_KEY = new Object();
    private transient Set<Map.Entry<K, V>> entrySet;
    volatile transient Set keySet;
    private final float loadFactor;
    private volatile int modCount;
    private final ReferenceQueue queue;
    private int size;
    private Entry<K, V>[] table;
    private int threshold;
    volatile transient Collection values;

    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    public WeakIdentityHashMap(int initialCapacity, float loadFactor) {
        this.queue = new ReferenceQueue();
        this.keySet = null;
        this.values = null;
        this.entrySet = null;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Initial Capacity: " + initialCapacity);
        }
        initialCapacity = initialCapacity > 1073741824 ? 1073741824 : initialCapacity;
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        }
        int i = 1;
        while (i < initialCapacity) {
            i <<= 1;
        }
        this.table = new Entry[i];
        this.loadFactor = loadFactor;
        this.threshold = (int) (i * loadFactor);
    }

    public WeakIdentityHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public WeakIdentityHashMap() {
        this.queue = new ReferenceQueue();
        this.keySet = null;
        this.values = null;
        this.entrySet = null;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = 16;
        this.table = new Entry[16];
    }

    public WeakIdentityHashMap(Map t) {
        this(Math.max(((int) (t.size() / DEFAULT_LOAD_FACTOR)) + 1, 16), DEFAULT_LOAD_FACTOR);
        putAll(t);
    }

    private static <T> T maskNull(T t) {
        return t == null ? (T) NULL_KEY : t;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <T> T unmaskNull(T key) {
        if (key == NULL_KEY) {
            return null;
        }
        return key;
    }

    int hash(Object x) {
        int identityHashCode = System.identityHashCode(x);
        return identityHashCode - (identityHashCode << 7);
    }

    private void expungeStaleEntries() {
        while (true) {
            Reference poll = this.queue.poll();
            if (poll == null) {
                return;
            }
            Entry<K, V> entry = (Entry) poll;
            int indexFor = indexFor(((Entry) entry).hash, this.table.length);
            Entry<K, V> entry2 = this.table[indexFor];
            Entry<K, V> entry3 = entry2;
            while (true) {
                if (entry2 != null) {
                    Entry<K, V> entry4 = ((Entry) entry2).next;
                    if (entry2 == entry) {
                        if (entry3 == entry) {
                            this.table[indexFor] = entry4;
                        } else {
                            ((Entry) entry3).next = entry4;
                        }
                        ((Entry) entry).next = null;
                        ((Entry) entry).value = null;
                        this.size--;
                    } else {
                        entry3 = entry2;
                        entry2 = entry4;
                    }
                }
            }
        }
    }

    private Entry<K, V>[] getTable() {
        expungeStaleEntries();
        return this.table;
    }

    @Override // java.util.Map
    public int size() {
        if (this.size == 0) {
            return 0;
        }
        expungeStaleEntries();
        return this.size;
    }

    @Override // java.util.Map
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override // java.util.Map
    public V get(Object obj) {
        Object maskNull = maskNull(obj);
        int hash = hash(maskNull);
        Entry<K, V>[] table = getTable();
        for (Entry<K, V> entry = table[indexFor(hash, table.length)]; entry != null; entry = ((Entry) entry).next) {
            if (((Entry) entry).hash == hash && maskNull == entry.get()) {
                return (V) ((Entry) entry).value;
            }
        }
        return null;
    }

    @Override // java.util.Map
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    Entry<K, V> getEntry(Object key) {
        Object maskNull = maskNull(key);
        int hash = hash(maskNull);
        Entry<K, V>[] table = getTable();
        Entry<K, V> entry = table[indexFor(hash, table.length)];
        while (entry != null && (((Entry) entry).hash != hash || maskNull != entry.get())) {
            entry = ((Entry) entry).next;
        }
        return entry;
    }

    @Override // java.util.Map
    public V put(K k, V v) {
        Object maskNull = maskNull(k);
        int hash = hash(maskNull);
        Entry<K, V>[] table = getTable();
        int indexFor = indexFor(hash, table.length);
        for (Entry<K, V> entry = table[indexFor]; entry != null; entry = ((Entry) entry).next) {
            if (hash == ((Entry) entry).hash && maskNull == entry.get()) {
                V v2 = (V) ((Entry) entry).value;
                if (v != v2) {
                    ((Entry) entry).value = v;
                }
                return v2;
            }
        }
        this.modCount++;
        table[indexFor] = new Entry<>(maskNull, v, this.queue, hash, table[indexFor]);
        int i = this.size + 1;
        this.size = i;
        if (i < this.threshold) {
            return null;
        }
        resize(table.length * 2);
        return null;
    }

    void resize(int newCapacity) {
        Entry<K, V>[] table = getTable();
        int length = table.length;
        if (this.size < this.threshold || length > newCapacity) {
            return;
        }
        Entry<K, V>[] entryArr = new Entry[newCapacity];
        transfer(table, entryArr);
        this.table = entryArr;
        if (this.size >= this.threshold / 2) {
            this.threshold = (int) (newCapacity * this.loadFactor);
            return;
        }
        expungeStaleEntries();
        transfer(entryArr, table);
        this.table = table;
    }

    private void transfer(Entry<K, V>[] src, Entry<K, V>[] dest) {
        for (int i = 0; i < src.length; i++) {
            Entry<K, V> entry = src[i];
            src[i] = null;
            while (entry != null) {
                Entry<K, V> entry2 = ((Entry) entry).next;
                if (entry.get() == null) {
                    ((Entry) entry).next = null;
                    ((Entry) entry).value = null;
                    this.size--;
                } else {
                    int indexFor = indexFor(((Entry) entry).hash, dest.length);
                    ((Entry) entry).next = dest[indexFor];
                    dest[indexFor] = entry;
                }
                entry = entry2;
            }
        }
    }

    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> t) {
        int size = t.size();
        if (size == 0) {
            return;
        }
        if (size >= this.threshold) {
            int i = (int) ((size / this.loadFactor) + 1.0f);
            if (i > 1073741824) {
                i = 1073741824;
            }
            int length = this.table.length;
            while (length < i) {
                length <<= 1;
            }
            resize(length);
        }
        for (Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override // java.util.Map
    public V remove(Object obj) {
        Object maskNull = maskNull(obj);
        int hash = hash(maskNull);
        Entry<K, V>[] table = getTable();
        int indexFor = indexFor(hash, table.length);
        Entry<K, V> entry = table[indexFor];
        Entry<K, V> entry2 = entry;
        while (entry != null) {
            Entry<K, V> entry3 = ((Entry) entry).next;
            if (hash == ((Entry) entry).hash && maskNull == entry.get()) {
                this.modCount++;
                this.size--;
                if (entry2 == entry) {
                    table[indexFor] = entry3;
                } else {
                    ((Entry) entry2).next = entry3;
                }
                return (V) ((Entry) entry).value;
            }
            entry2 = entry;
            entry = entry3;
        }
        return null;
    }

    Entry removeMapping(Object o) {
        if (!(o instanceof Map.Entry)) {
            return null;
        }
        Entry<K, V>[] table = getTable();
        Map.Entry entry = (Map.Entry) o;
        int hash = hash(maskNull(entry.getKey()));
        int indexFor = indexFor(hash, table.length);
        Entry<K, V> entry2 = table[indexFor];
        Entry<K, V> entry3 = entry2;
        while (entry2 != null) {
            Entry<K, V> entry4 = ((Entry) entry2).next;
            if (hash == ((Entry) entry2).hash && entry2.equals(entry)) {
                this.modCount++;
                this.size--;
                if (entry3 == entry2) {
                    table[indexFor] = entry4;
                } else {
                    ((Entry) entry3).next = entry4;
                }
                return entry2;
            }
            entry3 = entry2;
            entry2 = entry4;
        }
        return null;
    }

    @Override // java.util.Map
    public void clear() {
        while (this.queue.poll() != null) {
        }
        this.modCount++;
        Entry<K, V>[] entryArr = this.table;
        for (int i = 0; i < entryArr.length; i++) {
            entryArr[i] = null;
        }
        this.size = 0;
        while (this.queue.poll() != null) {
        }
    }

    @Override // java.util.Map
    public boolean containsValue(Object value) {
        if (value == null) {
            return containsNullValue();
        }
        Entry<K, V>[] table = getTable();
        int length = table.length;
        while (true) {
            int i = length - 1;
            if (length <= 0) {
                return false;
            }
            for (Entry<K, V> entry = table[i]; entry != null; entry = ((Entry) entry).next) {
                if (value.equals(((Entry) entry).value)) {
                    return true;
                }
            }
            length = i;
        }
    }

    private boolean containsNullValue() {
        Entry<K, V>[] table = getTable();
        int length = table.length;
        while (true) {
            int i = length - 1;
            if (length <= 0) {
                return false;
            }
            for (Entry<K, V> entry = table[i]; entry != null; entry = ((Entry) entry).next) {
                if (((Entry) entry).value == null) {
                    return true;
                }
            }
            length = i;
        }
    }

    public boolean removeValue(Object value) {
        if (value == null) {
            return removeNullValue();
        }
        Entry<K, V>[] table = getTable();
        HashSet hashSet = new HashSet();
        int length = table.length;
        while (true) {
            int i = length - 1;
            if (length <= 0) {
                break;
            }
            for (Entry<K, V> entry = table[i]; entry != null; entry = ((Entry) entry).next) {
                if (value.equals(((Entry) entry).value)) {
                    hashSet.add(entry.getKey());
                }
            }
            length = i;
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            remove(it.next());
        }
        return !hashSet.isEmpty();
    }

    private boolean removeNullValue() {
        Entry<K, V>[] table = getTable();
        HashSet hashSet = new HashSet();
        int length = table.length;
        while (true) {
            int i = length - 1;
            if (length <= 0) {
                break;
            }
            for (Entry<K, V> entry = table[i]; entry != null; entry = ((Entry) entry).next) {
                if (((Entry) entry).value == null) {
                    hashSet.add(entry.getKey());
                }
            }
            length = i;
        }
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            remove(it.next());
        }
        return !hashSet.isEmpty();
    }

    private static class Entry<K, V> extends WeakReference<K> implements Map.Entry<K, V> {
        private final int hash;
        private Entry<K, V> next;
        private V value;

        Entry(K key, V value, ReferenceQueue queue, int hash, Entry<K, V> next) {
            super(key, queue);
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        @Override // java.util.Map.Entry
        public K getKey() {
            return (K) WeakIdentityHashMap.unmaskNull(get());
        }

        @Override // java.util.Map.Entry
        public V getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public V setValue(V newValue) {
            V v = this.value;
            this.value = newValue;
            return v;
        }

        @Override // java.util.Map.Entry
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) o;
            if (getKey() == entry.getKey()) {
                V value = getValue();
                Object value2 = entry.getValue();
                if (value == value2) {
                    return true;
                }
                if (value != null && value.equals(value2)) {
                    return true;
                }
            }
            return false;
        }

        @Override // java.util.Map.Entry
        public int hashCode() {
            K key = getKey();
            V value = getValue();
            return (key == null ? 0 : System.identityHashCode(key)) ^ (value != null ? value.hashCode() : 0);
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        int expectedModCount;
        int index;
        Entry<K, V> entry = null;
        Entry<K, V> lastReturned = null;
        Object nextKey = null;
        Object currentKey = null;

        HashIterator() {
            this.expectedModCount = WeakIdentityHashMap.this.modCount;
            this.index = WeakIdentityHashMap.this.size() != 0 ? WeakIdentityHashMap.this.table.length : 0;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            Entry<K, V>[] entryArr = WeakIdentityHashMap.this.table;
            while (this.nextKey == null) {
                Entry<K, V> entry = this.entry;
                int i = this.index;
                while (entry == null && i > 0) {
                    i--;
                    entry = entryArr[i];
                }
                this.entry = entry;
                this.index = i;
                if (entry == null) {
                    this.currentKey = null;
                    return false;
                }
                Object obj = entry.get();
                this.nextKey = obj;
                if (obj == null) {
                    this.entry = ((Entry) this.entry).next;
                }
            }
            return true;
        }

        protected Entry<K, V> nextEntry() {
            if (WeakIdentityHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (this.nextKey == null && !hasNext()) {
                throw new NoSuchElementException();
            }
            Entry<K, V> entry = this.entry;
            this.lastReturned = entry;
            this.entry = ((Entry) entry).next;
            this.currentKey = this.nextKey;
            this.nextKey = null;
            return this.lastReturned;
        }

        @Override // java.util.Iterator
        public void remove() {
            if (this.lastReturned != null) {
                if (WeakIdentityHashMap.this.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                WeakIdentityHashMap.this.remove(this.currentKey);
                this.expectedModCount = WeakIdentityHashMap.this.modCount;
                this.lastReturned = null;
                this.currentKey = null;
                return;
            }
            throw new IllegalStateException();
        }
    }

    private class ValueIterator extends HashIterator {
        private ValueIterator() {
            super();
        }

        @Override // java.util.Iterator
        public Object next() {
            return ((Entry) nextEntry()).value;
        }
    }

    private class KeyIterator extends HashIterator {
        private KeyIterator() {
            super();
        }

        @Override // java.util.Iterator
        public Object next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends WeakIdentityHashMap<K, V>.HashIterator<Map.Entry<K, V>> {
        private EntryIterator() {
            super();
        }

        @Override // java.util.Iterator
        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    @Override // java.util.Map
    public Set keySet() {
        Set set = this.keySet;
        if (set != null) {
            return set;
        }
        KeySet keySet = new KeySet();
        this.keySet = keySet;
        return keySet;
    }

    private class KeySet extends AbstractSet {
        private KeySet() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
        public Iterator iterator() {
            return new KeyIterator();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public int size() {
            return WeakIdentityHashMap.this.size();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean contains(Object o) {
            return WeakIdentityHashMap.this.containsKey(o);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean remove(Object o) {
            if (!WeakIdentityHashMap.this.containsKey(o)) {
                return false;
            }
            WeakIdentityHashMap.this.remove(o);
            return true;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public void clear() {
            WeakIdentityHashMap.this.clear();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public Object[] toArray() {
            ArrayList arrayList = new ArrayList(size());
            Iterator it = iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
            return arrayList.toArray();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public Object[] toArray(Object[] a) {
            ArrayList arrayList = new ArrayList(size());
            Iterator it = iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
            return arrayList.toArray(a);
        }
    }

    @Override // java.util.Map
    public Collection values() {
        Collection collection = this.values;
        if (collection != null) {
            return collection;
        }
        Values values = new Values();
        this.values = values;
        return values;
    }

    private class Values extends AbstractCollection {
        private Values() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
        public Iterator iterator() {
            return new ValueIterator();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public int size() {
            return WeakIdentityHashMap.this.size();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public boolean contains(Object o) {
            return WeakIdentityHashMap.this.containsValue(o);
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public void clear() {
            WeakIdentityHashMap.this.clear();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public Object[] toArray() {
            ArrayList arrayList = new ArrayList(size());
            Iterator it = iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
            return arrayList.toArray();
        }

        @Override // java.util.AbstractCollection, java.util.Collection
        public Object[] toArray(Object[] a) {
            ArrayList arrayList = new ArrayList(size());
            Iterator it = iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
            return arrayList.toArray(a);
        }
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = this.entrySet;
        if (set != null) {
            return set;
        }
        EntrySet entrySet = new EntrySet();
        this.entrySet = entrySet;
        return entrySet;
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) o;
            entry.getKey();
            Entry<K, V> entry2 = WeakIdentityHashMap.this.getEntry(entry.getKey());
            return entry2 != null && entry2.equals(entry);
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public boolean remove(Object o) {
            return WeakIdentityHashMap.this.removeMapping(o) != null;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public int size() {
            return WeakIdentityHashMap.this.size();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public void clear() {
            WeakIdentityHashMap.this.clear();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public Object[] toArray() {
            ArrayList arrayList = new ArrayList(size());
            Iterator<Map.Entry<K, V>> it = iterator();
            while (it.hasNext()) {
                arrayList.add(new SimpleEntry(it.next()));
            }
            return arrayList.toArray();
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.Set
        public Object[] toArray(Object[] a) {
            ArrayList arrayList = new ArrayList(size());
            Iterator<Map.Entry<K, V>> it = iterator();
            while (it.hasNext()) {
                arrayList.add(new SimpleEntry(it.next()));
            }
            return arrayList.toArray(a);
        }
    }

    static class SimpleEntry implements Map.Entry {
        Object key;
        Object value;

        public SimpleEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Map.Entry e) {
            this.key = e.getKey();
            this.value = e.getValue();
        }

        @Override // java.util.Map.Entry
        public Object getKey() {
            return this.key;
        }

        @Override // java.util.Map.Entry
        public Object getValue() {
            return this.value;
        }

        @Override // java.util.Map.Entry
        public Object setValue(Object value) {
            Object obj = this.value;
            this.value = value;
            return obj;
        }

        @Override // java.util.Map.Entry
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) o;
            return eq(this.key, entry.getKey()) && eq(this.value, entry.getValue());
        }

        @Override // java.util.Map.Entry
        public int hashCode() {
            Object obj = this.key;
            int hashCode = obj == null ? 0 : obj.hashCode();
            Object obj2 = this.value;
            return hashCode ^ (obj2 != null ? obj2.hashCode() : 0);
        }

        public String toString() {
            return this.key + "=" + this.value;
        }

        private static boolean eq(Object o1, Object o2) {
            if (o1 == null) {
                return o2 == null;
            }
            return o1.equals(o2);
        }
    }
}
