package com.firebase.client.core.utilities;

import com.firebase.client.core.Path;
import com.firebase.client.snapshot.ChildKey;
import java.util.Map.Entry;

public class Tree<T> {
    static final /* synthetic */ boolean $assertionsDisabled = (!Tree.class.desiredAssertionStatus());
    private ChildKey name;
    private TreeNode<T> node;
    private Tree<T> parent;

    public interface TreeFilter<T> {
        boolean filterTreeNode(Tree<T> tree);
    }

    public interface TreeVisitor<T> {
        void visitTree(Tree<T> tree);
    }

    public Tree(ChildKey name2, Tree<T> parent2, TreeNode<T> node2) {
        this.name = name2;
        this.parent = parent2;
        this.node = node2;
    }

    public Tree() {
        this(null, null, new TreeNode());
    }

    public TreeNode<T> lastNodeOnPath(Path path) {
        TreeNode<T> current = this.node;
        ChildKey next = path.getFront();
        while (next != null) {
            TreeNode<T> childNode = current.children.containsKey(next) ? (TreeNode) current.children.get(next) : null;
            if (childNode == null) {
                break;
            }
            current = childNode;
            path = path.popFront();
            next = path.getFront();
        }
        return current;
    }

    public Tree<T> subTree(Path path) {
        ChildKey next = path.getFront();
        Tree tree = this;
        while (next != null) {
            Tree<T> child = new Tree<>(next, tree, tree.node.children.containsKey(next) ? (TreeNode) tree.node.children.get(next) : new TreeNode());
            path = path.popFront();
            next = path.getFront();
            tree = child;
        }
        return tree;
    }

    public T getValue() {
        return this.node.value;
    }

    public void setValue(T value) {
        this.node.value = value;
        updateParents();
    }

    public Tree<T> getParent() {
        return this.parent;
    }

    public ChildKey getName() {
        return this.name;
    }

    public Path getPath() {
        if (this.parent != null) {
            if ($assertionsDisabled || this.name != null) {
                return this.parent.getPath().child(this.name);
            }
            throw new AssertionError();
        } else if (this.name == null) {
            return Path.getEmptyPath();
        } else {
            return new Path(this.name);
        }
    }

    public boolean hasChildren() {
        return !this.node.children.isEmpty();
    }

    public boolean isEmpty() {
        return this.node.value == null && this.node.children.isEmpty();
    }

    public void forEachDescendant(TreeVisitor<T> visitor) {
        forEachDescendant(visitor, false, false);
    }

    public void forEachDescendant(TreeVisitor<T> visitor, boolean includeSelf) {
        forEachDescendant(visitor, includeSelf, false);
    }

    public void forEachDescendant(final TreeVisitor<T> visitor, boolean includeSelf, final boolean childrenFirst) {
        if (includeSelf && !childrenFirst) {
            visitor.visitTree(this);
        }
        forEachChild(new TreeVisitor<T>() {
            public void visitTree(Tree<T> tree) {
                tree.forEachDescendant(visitor, true, childrenFirst);
            }
        });
        if (includeSelf && childrenFirst) {
            visitor.visitTree(this);
        }
    }

    public boolean forEachAncestor(TreeFilter<T> filter) {
        return forEachAncestor(filter, false);
    }

    public boolean forEachAncestor(TreeFilter<T> filter, boolean includeSelf) {
        for (Tree tree = includeSelf ? this : this.parent; tree != null; tree = tree.parent) {
            if (filter.filterTreeNode(tree)) {
                return true;
            }
        }
        return false;
    }

    public void forEachChild(TreeVisitor<T> visitor) {
        Object[] entries = this.node.children.entrySet().toArray();
        for (Object obj : entries) {
            Entry<ChildKey, TreeNode<T>> entry = (Entry) obj;
            visitor.visitTree(new Tree<>((ChildKey) entry.getKey(), this, (TreeNode) entry.getValue()));
        }
    }

    private void updateParents() {
        if (this.parent != null) {
            this.parent.updateChild(this.name, this);
        }
    }

    private void updateChild(ChildKey name2, Tree<T> child) {
        boolean childEmpty = child.isEmpty();
        boolean childExists = this.node.children.containsKey(name2);
        if (childEmpty && childExists) {
            this.node.children.remove(name2);
            updateParents();
        } else if (!childEmpty && !childExists) {
            this.node.children.put(name2, child.node);
            updateParents();
        }
    }

    public String toString() {
        return toString("");
    }

    /* access modifiers changed from: 0000 */
    public String toString(String prefix) {
        return prefix + (this.name == null ? "<anon>" : this.name.asString()) + "\n" + this.node.toString(prefix + "\t");
    }
}
