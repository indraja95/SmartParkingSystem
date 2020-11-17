package com.firebase.client.core;

import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RangeMerge {
    static final /* synthetic */ boolean $assertionsDisabled = (!RangeMerge.class.desiredAssertionStatus());
    private final Path optExclusiveStart;
    private final Path optInclusiveEnd;
    private final Node snap;

    public RangeMerge(Path optExclusiveStart2, Path optInclusiveEnd2, Node snap2) {
        this.optExclusiveStart = optExclusiveStart2;
        this.optInclusiveEnd = optInclusiveEnd2;
        this.snap = snap2;
    }

    public Node applyTo(Node node) {
        return updateRangeInNode(Path.getEmptyPath(), node, this.snap);
    }

    /* access modifiers changed from: 0000 */
    public Path getStart() {
        return this.optExclusiveStart;
    }

    /* access modifiers changed from: 0000 */
    public Path getEnd() {
        return this.optInclusiveEnd;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=com.firebase.client.snapshot.Node, code=com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, for r18v0, types: [com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, com.firebase.client.snapshot.Node] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=com.firebase.client.snapshot.Node, code=com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, for r19v0, types: [com.firebase.client.snapshot.Node<com.firebase.client.snapshot.NamedNode>, com.firebase.client.snapshot.Node] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0057 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0061 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x008f A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00a0 A[LOOP:0: B:47:0x009a->B:49:0x00a0, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00b8 A[LOOP:1: B:51:0x00b2->B:53:0x00b8, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00fb  */
    private Node updateRangeInNode(Path currentPath, Node<NamedNode> node, Node<NamedNode> updateNode) {
        boolean startInNode;
        boolean endInNode;
        int startComparison = this.optExclusiveStart == null ? 1 : currentPath.compareTo(this.optExclusiveStart);
        int endComparison = this.optInclusiveEnd == null ? -1 : currentPath.compareTo(this.optInclusiveEnd);
        if (this.optExclusiveStart != null) {
            if (currentPath.contains(this.optExclusiveStart)) {
                startInNode = true;
                if (this.optInclusiveEnd != null) {
                    if (currentPath.contains(this.optInclusiveEnd)) {
                        endInNode = true;
                        if (startComparison <= 0 && endComparison < 0 && !endInNode) {
                            return updateNode;
                        }
                        if (startComparison <= 0 && endInNode && updateNode.isLeafNode()) {
                            return updateNode;
                        }
                        if (startComparison > 0 || endComparison != 0) {
                            if (!startInNode || endInNode) {
                                Set<ChildKey> allChildren = new HashSet<>();
                                for (NamedNode child : node) {
                                    allChildren.add(child.getName());
                                }
                                for (NamedNode child2 : updateNode) {
                                    allChildren.add(child2.getName());
                                }
                                List<ChildKey> inOrder = new ArrayList<>(allChildren.size() + 1);
                                inOrder.addAll(allChildren);
                                if (!updateNode.getPriority().isEmpty() || !node.getPriority().isEmpty()) {
                                    inOrder.add(ChildKey.getPriorityKey());
                                }
                                Node newNode = node;
                                for (ChildKey key : inOrder) {
                                    Node currentChild = node.getImmediateChild(key);
                                    Node updatedChild = updateRangeInNode(currentPath.child(key), node.getImmediateChild(key), updateNode.getImmediateChild(key));
                                    if (updatedChild != currentChild) {
                                        newNode = newNode.updateImmediateChild(key, updatedChild);
                                    }
                                }
                                return newNode;
                            } else if ($assertionsDisabled || endComparison > 0 || startComparison <= 0) {
                                return node;
                            } else {
                                throw new AssertionError();
                            }
                        } else if (!$assertionsDisabled && !endInNode) {
                            throw new AssertionError();
                        } else if ($assertionsDisabled || !updateNode.isLeafNode()) {
                            return node.isLeafNode() ? EmptyNode.Empty() : node;
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
                endInNode = false;
                if (startComparison <= 0) {
                }
                if (startComparison <= 0) {
                }
                if (startComparison > 0) {
                }
                if (!startInNode) {
                }
                Set<ChildKey> allChildren2 = new HashSet<>();
                while (i$.hasNext()) {
                }
                while (i$.hasNext()) {
                }
                List<ChildKey> inOrder2 = new ArrayList<>(allChildren2.size() + 1);
                inOrder2.addAll(allChildren2);
                inOrder2.add(ChildKey.getPriorityKey());
                Node newNode2 = node;
                for (ChildKey key2 : inOrder2) {
                }
                return newNode2;
            }
        }
        startInNode = false;
        if (this.optInclusiveEnd != null) {
        }
        endInNode = false;
        if (startComparison <= 0) {
        }
        if (startComparison <= 0) {
        }
        if (startComparison > 0) {
        }
        if (!startInNode) {
        }
        Set<ChildKey> allChildren22 = new HashSet<>();
        while (i$.hasNext()) {
        }
        while (i$.hasNext()) {
        }
        List<ChildKey> inOrder22 = new ArrayList<>(allChildren22.size() + 1);
        inOrder22.addAll(allChildren22);
        inOrder22.add(ChildKey.getPriorityKey());
        Node newNode22 = node;
        for (ChildKey key22 : inOrder22) {
        }
        return newNode22;
    }

    public String toString() {
        return "RangeMerge{optExclusiveStart=" + this.optExclusiveStart + ", optInclusiveEnd=" + this.optInclusiveEnd + ", snap=" + this.snap + '}';
    }
}
