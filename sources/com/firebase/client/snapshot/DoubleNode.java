package com.firebase.client.snapshot;

import com.firebase.client.snapshot.Node.HashVersion;
import com.firebase.client.utilities.Utilities;

public class DoubleNode extends LeafNode<DoubleNode> {
    static final /* synthetic */ boolean $assertionsDisabled = (!DoubleNode.class.desiredAssertionStatus());
    private final Double value;

    public DoubleNode(Double value2, Node priority) {
        super(priority);
        this.value = value2;
    }

    public Object getValue() {
        return this.value;
    }

    public String getHashRepresentation(HashVersion version) {
        return (getPriorityHash(version) + "number:") + Utilities.doubleToHashString(this.value.doubleValue());
    }

    public DoubleNode updatePriority(Node priority) {
        if ($assertionsDisabled || PriorityUtilities.isValidPriority(priority)) {
            return new DoubleNode(this.value, priority);
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public LeafType getLeafType() {
        return LeafType.Number;
    }

    /* access modifiers changed from: protected */
    public int compareLeafValues(DoubleNode other) {
        return this.value.compareTo(other.value);
    }

    public boolean equals(Object other) {
        if (!(other instanceof DoubleNode)) {
            return false;
        }
        DoubleNode otherDoubleNode = (DoubleNode) other;
        if (!this.value.equals(otherDoubleNode.value) || !this.priority.equals(otherDoubleNode.priority)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.value.hashCode() + this.priority.hashCode();
    }
}
