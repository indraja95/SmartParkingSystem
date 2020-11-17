package com.firebase.client.collection;

import com.firebase.client.collection.ImmutableSortedMap.Builder;
import com.firebase.client.collection.ImmutableSortedMap.Builder.KeyTranslator;
import com.firebase.client.collection.LLRBNode.NodeVisitor;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ArraySortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private final Comparator<K> comparator;
    /* access modifiers changed from: private */
    public final K[] keys;
    /* access modifiers changed from: private */
    public final V[] values;

    public static <A, B, C> ArraySortedMap<A, C> buildFrom(List<A> keys2, Map<B, C> values2, KeyTranslator<A, B> translator, Comparator<A> comparator2) {
        Collections.sort(keys2, comparator2);
        int size = keys2.size();
        A[] keyArray = (Object[]) new Object[size];
        C[] valueArray = (Object[]) new Object[size];
        int pos = 0;
        for (A k : keys2) {
            keyArray[pos] = k;
            valueArray[pos] = values2.get(translator.translate(k));
            pos++;
        }
        return new ArraySortedMap<>(comparator2, keyArray, valueArray);
    }

    public static <K, V> ArraySortedMap<K, V> fromMap(Map<K, V> map, Comparator<K> comparator2) {
        return buildFrom(new ArrayList(map.keySet()), map, Builder.identityTranslator(), comparator2);
    }

    public ArraySortedMap(Comparator<K> comparator2) {
        this.keys = (Object[]) new Object[0];
        this.values = (Object[]) new Object[0];
        this.comparator = comparator2;
    }

    private ArraySortedMap(Comparator<K> comparator2, K[] keys2, V[] values2) {
        this.keys = keys2;
        this.values = values2;
        this.comparator = comparator2;
    }

    public boolean containsKey(K key) {
        return findKey(key) != -1;
    }

    public V get(K key) {
        int pos = findKey(key);
        if (pos != -1) {
            return this.values[pos];
        }
        return null;
    }

    public ImmutableSortedMap<K, V> remove(K key) {
        int pos = findKey(key);
        if (pos == -1) {
            return this;
        }
        return new ArraySortedMap(this.comparator, removeFromArray(this.keys, pos), removeFromArray(this.values, pos));
    }

    public ImmutableSortedMap<K, V> insert(K key, V value) {
        int pos = findKey(key);
        if (pos != -1) {
            if (this.keys[pos] == key && this.values[pos] == value) {
                return this;
            }
            return new ArraySortedMap(this.comparator, replaceInArray(this.keys, pos, key), replaceInArray(this.values, pos, value));
        } else if (this.keys.length > 25) {
            Map<K, V> map = new HashMap<>(this.keys.length + 1);
            for (int i = 0; i < this.keys.length; i++) {
                map.put(this.keys[i], this.values[i]);
            }
            map.put(key, value);
            return RBTreeSortedMap.fromMap(map, this.comparator);
        } else {
            int newPos = findKeyOrInsertPosition(key);
            return new ArraySortedMap(this.comparator, addToArray(this.keys, newPos, key), addToArray(this.values, newPos, value));
        }
    }

    public K getMinKey() {
        if (this.keys.length > 0) {
            return this.keys[0];
        }
        return null;
    }

    public K getMaxKey() {
        if (this.keys.length > 0) {
            return this.keys[this.keys.length - 1];
        }
        return null;
    }

    public int size() {
        return this.keys.length;
    }

    public boolean isEmpty() {
        return this.keys.length == 0;
    }

    public void inOrderTraversal(NodeVisitor<K, V> visitor) {
        for (int i = 0; i < this.keys.length; i++) {
            visitor.visitEntry(this.keys[i], this.values[i]);
        }
    }

    private Iterator<Entry<K, V>> iterator(final int pos, final boolean reverse) {
        return new Iterator<Entry<K, V>>() {
            int currentPos = pos;

            public boolean hasNext() {
                return reverse ? this.currentPos >= 0 : this.currentPos < ArraySortedMap.this.keys.length;
            }

            public Entry<K, V> next() {
                K key = ArraySortedMap.this.keys[this.currentPos];
                V value = ArraySortedMap.this.values[this.currentPos];
                this.currentPos = reverse ? this.currentPos - 1 : this.currentPos + 1;
                return new SimpleImmutableEntry(key, value);
            }

            public void remove() {
                throw new UnsupportedOperationException("Can't remove elements from ImmutableSortedMap");
            }
        };
    }

    public Iterator<Entry<K, V>> iterator() {
        return iterator(0, false);
    }

    public Iterator<Entry<K, V>> iteratorFrom(K key) {
        return iterator(findKeyOrInsertPosition(key), false);
    }

    public Iterator<Entry<K, V>> reverseIteratorFrom(K key) {
        int pos = findKeyOrInsertPosition(key);
        if (pos >= this.keys.length || this.comparator.compare(this.keys[pos], key) != 0) {
            return iterator(pos - 1, true);
        }
        return iterator(pos, true);
    }

    public Iterator<Entry<K, V>> reverseIterator() {
        return iterator(this.keys.length - 1, true);
    }

    public K getPredecessorKey(K key) {
        int pos = findKey(key);
        if (pos == -1) {
            throw new IllegalArgumentException("Can't find predecessor of nonexistent key");
        } else if (pos > 0) {
            return this.keys[pos - 1];
        } else {
            return null;
        }
    }

    public K getSuccessorKey(K key) {
        int pos = findKey(key);
        if (pos == -1) {
            throw new IllegalArgumentException("Can't find successor of nonexistent key");
        } else if (pos < this.keys.length - 1) {
            return this.keys[pos + 1];
        } else {
            return null;
        }
    }

    public Comparator<K> getComparator() {
        return this.comparator;
    }

    private static <T> T[] removeFromArray(T[] arr, int pos) {
        int newSize = arr.length - 1;
        T[] newArray = (Object[]) new Object[newSize];
        System.arraycopy(arr, 0, newArray, 0, pos);
        System.arraycopy(arr, pos + 1, newArray, pos, newSize - pos);
        return newArray;
    }

    private static <T> T[] addToArray(T[] arr, int pos, T value) {
        int newSize = arr.length + 1;
        T[] newArray = (Object[]) new Object[newSize];
        System.arraycopy(arr, 0, newArray, 0, pos);
        newArray[pos] = value;
        System.arraycopy(arr, pos, newArray, pos + 1, (newSize - pos) - 1);
        return newArray;
    }

    private static <T> T[] replaceInArray(T[] arr, int pos, T value) {
        int size = arr.length;
        T[] newArray = (Object[]) new Object[size];
        System.arraycopy(arr, 0, newArray, 0, size);
        newArray[pos] = value;
        return newArray;
    }

    private int findKeyOrInsertPosition(K key) {
        int newPos = 0;
        while (newPos < this.keys.length && this.comparator.compare(this.keys[newPos], key) < 0) {
            newPos++;
        }
        return newPos;
    }

    private int findKey(K key) {
        int i = 0;
        for (K otherKey : this.keys) {
            if (this.comparator.compare(key, otherKey) == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }
}
