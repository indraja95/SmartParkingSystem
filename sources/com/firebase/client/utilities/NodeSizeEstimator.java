package com.firebase.client.utilities;

import com.firebase.client.snapshot.BooleanNode;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.DoubleNode;
import com.firebase.client.snapshot.LeafNode;
import com.firebase.client.snapshot.LongNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.StringNode;

public class NodeSizeEstimator {
    static final /* synthetic */ boolean $assertionsDisabled = (!NodeSizeEstimator.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int LEAF_PRIORITY_OVERHEAD = 24;

    private static long estimateLeafNodeSize(LeafNode<?> node) {
        long valueSize;
        if (node instanceof DoubleNode) {
            valueSize = 8;
        } else if (node instanceof LongNode) {
            valueSize = 8;
        } else if (node instanceof BooleanNode) {
            valueSize = 4;
        } else if (node instanceof StringNode) {
            valueSize = (long) (((String) node.getValue()).length() + 2);
        } else {
            throw new IllegalArgumentException("Unknown leaf node type: " + node.getClass());
        }
        return node.getPriority().isEmpty() ? valueSize : 24 + valueSize + estimateLeafNodeSize((LeafNode) node.getPriority());
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=com.firebase.client.snapshot.Node, code=com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, for r8v0, types: [com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, com.firebase.client.snapshot.Node, java.lang.Object] */
    public static long estimateSerializedNodeSize(Node<NamedNode> node) {
        if (node.isEmpty()) {
            return 4;
        }
        if (node.isLeafNode()) {
            return estimateLeafNodeSize((LeafNode) node);
        }
        if ($assertionsDisabled || (node instanceof ChildrenNode)) {
            long sum = 1;
            for (NamedNode entry : node) {
                sum = sum + ((long) entry.getName().asString().length()) + 4 + estimateSerializedNodeSize(entry.getNode());
            }
            if (!node.getPriority().isEmpty()) {
                return sum + 12 + estimateLeafNodeSize((LeafNode) node.getPriority());
            }
            return sum;
        }
        throw new AssertionError("Unexpected node type: " + node.getClass());
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=com.firebase.client.snapshot.Node, code=com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, for r6v0, types: [com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, com.firebase.client.snapshot.Node, java.lang.Object] */
    public static int nodeCount(Node<NamedNode> node) {
        if (node.isEmpty()) {
            return 0;
        }
        if (node.isLeafNode()) {
            return 1;
        }
        if ($assertionsDisabled || (node instanceof ChildrenNode)) {
            int sum = 0;
            for (NamedNode entry : node) {
                sum += nodeCount(entry.getNode());
            }
            return sum;
        }
        throw new AssertionError("Unexpected node type: " + node.getClass());
    }
}
