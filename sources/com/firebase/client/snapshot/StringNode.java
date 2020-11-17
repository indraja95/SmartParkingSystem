package com.firebase.client.snapshot;

import com.firebase.client.snapshot.Node.HashVersion;
import com.firebase.client.utilities.Utilities;

public class StringNode extends LeafNode<StringNode> {
    private final String value;

    public StringNode(String value2, Node priority) {
        super(priority);
        this.value = value2;
    }

    public Object getValue() {
        return this.value;
    }

    public String getHashRepresentation(HashVersion version) {
        switch (version) {
            case V1:
                return getPriorityHash(version) + "string:" + this.value;
            case V2:
                return getPriorityHash(version) + "string:" + Utilities.stringHashV2Representation(this.value);
            default:
                throw new IllegalArgumentException("Invalid hash version for string node: " + version);
        }
    }

    public StringNode updatePriority(Node priority) {
        return new StringNode(this.value, priority);
    }

    /* access modifiers changed from: protected */
    public LeafType getLeafType() {
        return LeafType.String;
    }

    /* access modifiers changed from: protected */
    public int compareLeafValues(StringNode other) {
        return this.value.compareTo(other.value);
    }

    public boolean equals(Object other) {
        if (!(other instanceof StringNode)) {
            return false;
        }
        StringNode otherStringNode = (StringNode) other;
        if (!this.value.equals(otherStringNode.value) || !this.priority.equals(otherStringNode.priority)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.value.hashCode() + this.priority.hashCode();
    }
}
