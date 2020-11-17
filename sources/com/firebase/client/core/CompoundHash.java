package com.firebase.client.core;

import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.ChildrenNode;
import com.firebase.client.snapshot.ChildrenNode.ChildVisitor;
import com.firebase.client.snapshot.LeafNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.Node.HashVersion;
import com.firebase.client.utilities.NodeSizeEstimator;
import com.firebase.client.utilities.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class CompoundHash {
    private final List<String> hashes;
    private final List<Path> posts;

    static class CompoundHashBuilder {
        /* access modifiers changed from: private */
        public final List<String> currentHashes = new ArrayList();
        private Stack<ChildKey> currentPath = new Stack<>();
        private int currentPathDepth;
        /* access modifiers changed from: private */
        public final List<Path> currentPaths = new ArrayList();
        private int lastLeafDepth = -1;
        private boolean needsComma = true;
        private StringBuilder optHashValueBuilder = null;
        private final SplitStrategy splitStrategy;

        public CompoundHashBuilder(SplitStrategy strategy) {
            this.splitStrategy = strategy;
        }

        public boolean buildingRange() {
            return this.optHashValueBuilder != null;
        }

        public int currentHashLength() {
            return this.optHashValueBuilder.length();
        }

        public Path currentPath() {
            return currentPath(this.currentPathDepth);
        }

        private Path currentPath(int depth) {
            ChildKey[] segments = new ChildKey[depth];
            for (int i = 0; i < depth; i++) {
                segments[i] = (ChildKey) this.currentPath.get(i);
            }
            return new Path(segments);
        }

        private void ensureRange() {
            if (!buildingRange()) {
                this.optHashValueBuilder = new StringBuilder();
                this.optHashValueBuilder.append("(");
                Iterator i$ = currentPath(this.currentPathDepth).iterator();
                while (i$.hasNext()) {
                    appendKey(this.optHashValueBuilder, (ChildKey) i$.next());
                    this.optHashValueBuilder.append(":(");
                }
                this.needsComma = false;
            }
        }

        private void appendKey(StringBuilder builder, ChildKey key) {
            builder.append(Utilities.stringHashV2Representation(key.asString()));
        }

        /* access modifiers changed from: private */
        public void processLeaf(LeafNode<?> node) {
            ensureRange();
            this.lastLeafDepth = this.currentPathDepth;
            this.optHashValueBuilder.append(node.getHashRepresentation(HashVersion.V2));
            this.needsComma = true;
            if (this.splitStrategy.shouldSplit(this)) {
                endRange();
            }
        }

        /* access modifiers changed from: private */
        public void startChild(ChildKey key) {
            ensureRange();
            if (this.needsComma) {
                this.optHashValueBuilder.append(",");
            }
            appendKey(this.optHashValueBuilder, key);
            this.optHashValueBuilder.append(":(");
            if (this.currentPathDepth == this.currentPath.size()) {
                this.currentPath.add(key);
            } else {
                this.currentPath.set(this.currentPathDepth, key);
            }
            this.currentPathDepth++;
            this.needsComma = false;
        }

        /* access modifiers changed from: private */
        public void endChild() {
            this.currentPathDepth--;
            if (buildingRange()) {
                this.optHashValueBuilder.append(")");
            }
            this.needsComma = true;
        }

        /* access modifiers changed from: private */
        public void finishHashing() {
            Utilities.hardAssert(this.currentPathDepth == 0, "Can't finish hashing in the middle processing a child");
            if (buildingRange()) {
                endRange();
            }
            this.currentHashes.add("");
        }

        private void endRange() {
            Utilities.hardAssert(buildingRange(), "Can't end range without starting a range!");
            for (int i = 0; i < this.currentPathDepth; i++) {
                this.optHashValueBuilder.append(")");
            }
            this.optHashValueBuilder.append(")");
            Path lastLeafPath = currentPath(this.lastLeafDepth);
            this.currentHashes.add(Utilities.sha1HexDigest(this.optHashValueBuilder.toString()));
            this.currentPaths.add(lastLeafPath);
            this.optHashValueBuilder = null;
        }
    }

    private static class SimpleSizeSplitStrategy implements SplitStrategy {
        private final long splitThreshold;

        public SimpleSizeSplitStrategy(Node node) {
            this.splitThreshold = Math.max(512, (long) Math.sqrt((double) (100 * NodeSizeEstimator.estimateSerializedNodeSize(node))));
        }

        public boolean shouldSplit(CompoundHashBuilder state) {
            return ((long) state.currentHashLength()) > this.splitThreshold && !state.currentPath().getBack().equals(ChildKey.getPriorityKey());
        }
    }

    public interface SplitStrategy {
        boolean shouldSplit(CompoundHashBuilder compoundHashBuilder);
    }

    private CompoundHash(List<Path> posts2, List<String> hashes2) {
        if (posts2.size() != hashes2.size() - 1) {
            throw new IllegalArgumentException("Number of posts need to be n-1 for n hashes in CompoundHash");
        }
        this.posts = posts2;
        this.hashes = hashes2;
    }

    public List<Path> getPosts() {
        return Collections.unmodifiableList(this.posts);
    }

    public List<String> getHashes() {
        return Collections.unmodifiableList(this.hashes);
    }

    public static CompoundHash fromNode(Node node) {
        return fromNode(node, new SimpleSizeSplitStrategy(node));
    }

    public static CompoundHash fromNode(Node node, SplitStrategy strategy) {
        if (node.isEmpty()) {
            return new CompoundHash(Collections.emptyList(), Collections.singletonList(""));
        }
        CompoundHashBuilder state = new CompoundHashBuilder(strategy);
        processNode(node, state);
        state.finishHashing();
        return new CompoundHash(state.currentPaths, state.currentHashes);
    }

    /* access modifiers changed from: private */
    public static void processNode(Node node, final CompoundHashBuilder state) {
        if (node.isLeafNode()) {
            state.processLeaf((LeafNode) node);
        } else if (node.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate hash on empty node!");
        } else if (!(node instanceof ChildrenNode)) {
            throw new IllegalStateException("Expected children node, but got: " + node);
        } else {
            ((ChildrenNode) node).forEachChild(new ChildVisitor() {
                public void visitChild(ChildKey name, Node child) {
                    state.startChild(name);
                    CompoundHash.processNode(child, state);
                    state.endChild();
                }
            }, true);
        }
    }
}
