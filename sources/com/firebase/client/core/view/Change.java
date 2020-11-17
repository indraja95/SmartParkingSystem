package com.firebase.client.core.view;

import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.Node;
import com.shaded.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class Change {
    private final ChildKey childKey;
    private final EventType eventType;
    private final IndexedNode indexedNode;
    private final IndexedNode oldIndexedNode;
    private final ChildKey prevName;

    private Change(EventType eventType2, IndexedNode indexedNode2, ChildKey childKey2, ChildKey prevName2, IndexedNode oldIndexedNode2) {
        this.eventType = eventType2;
        this.indexedNode = indexedNode2;
        this.childKey = childKey2;
        this.prevName = prevName2;
        this.oldIndexedNode = oldIndexedNode2;
    }

    public static Change valueChange(IndexedNode snapshot) {
        return new Change(EventType.VALUE, snapshot, null, null, null);
    }

    public static Change childAddedChange(ChildKey childKey2, Node snapshot) {
        return childAddedChange(childKey2, IndexedNode.from(snapshot));
    }

    public static Change childAddedChange(ChildKey childKey2, IndexedNode snapshot) {
        return new Change(EventType.CHILD_ADDED, snapshot, childKey2, null, null);
    }

    public static Change childRemovedChange(ChildKey childKey2, Node snapshot) {
        return childRemovedChange(childKey2, IndexedNode.from(snapshot));
    }

    public static Change childRemovedChange(ChildKey childKey2, IndexedNode snapshot) {
        return new Change(EventType.CHILD_REMOVED, snapshot, childKey2, null, null);
    }

    public static Change childChangedChange(ChildKey childKey2, Node newSnapshot, Node oldSnapshot) {
        return childChangedChange(childKey2, IndexedNode.from(newSnapshot), IndexedNode.from(oldSnapshot));
    }

    public static Change childChangedChange(ChildKey childKey2, IndexedNode newSnapshot, IndexedNode oldSnapshot) {
        return new Change(EventType.CHILD_CHANGED, newSnapshot, childKey2, null, oldSnapshot);
    }

    public static Change childMovedChange(ChildKey childKey2, Node snapshot) {
        return childMovedChange(childKey2, IndexedNode.from(snapshot));
    }

    public static Change childMovedChange(ChildKey childKey2, IndexedNode snapshot) {
        return new Change(EventType.CHILD_MOVED, snapshot, childKey2, null, null);
    }

    public Change changeWithPrevName(ChildKey prevName2) {
        return new Change(this.eventType, this.indexedNode, this.childKey, prevName2, this.oldIndexedNode);
    }

    public ChildKey getChildKey() {
        return this.childKey;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public IndexedNode getIndexedNode() {
        return this.indexedNode;
    }

    public ChildKey getPrevName() {
        return this.prevName;
    }

    public IndexedNode getOldIndexedNode() {
        return this.oldIndexedNode;
    }

    public String toString() {
        return "Change: " + this.eventType + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + this.childKey;
    }
}
