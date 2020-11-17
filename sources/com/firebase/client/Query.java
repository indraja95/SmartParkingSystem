package com.firebase.client;

import com.firebase.client.core.ChildEventRegistration;
import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.Path;
import com.firebase.client.core.Repo;
import com.firebase.client.core.ValueEventRegistration;
import com.firebase.client.core.ZombieEventManager;
import com.firebase.client.core.view.QueryParams;
import com.firebase.client.core.view.QuerySpec;
import com.firebase.client.snapshot.BooleanNode;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.DoubleNode;
import com.firebase.client.snapshot.EmptyNode;
import com.firebase.client.snapshot.KeyIndex;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.PathIndex;
import com.firebase.client.snapshot.PriorityIndex;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.snapshot.StringNode;
import com.firebase.client.snapshot.ValueIndex;
import com.firebase.client.utilities.Utilities;
import com.firebase.client.utilities.Validation;

public class Query {
    static final /* synthetic */ boolean $assertionsDisabled = (!Query.class.desiredAssertionStatus());
    private final boolean orderByCalled;
    protected final QueryParams params;
    protected final Path path;
    protected final Repo repo;

    Query(Repo repo2, Path path2, QueryParams params2, boolean orderByCalled2) throws FirebaseException {
        this.repo = repo2;
        this.path = path2;
        this.params = params2;
        this.orderByCalled = orderByCalled2;
        Utilities.hardAssert(params2.isValid(), "Validation of queries failed.");
    }

    Query(Repo repo2, Path path2) {
        this.repo = repo2;
        this.path = path2;
        this.params = QueryParams.DEFAULT_PARAMS;
        this.orderByCalled = false;
    }

    private void validateQueryEndpoints(QueryParams params2) {
        if (params2.getIndex().equals(KeyIndex.getInstance())) {
            String message = "You must use startAt(String value), endAt(String value) or equalTo(String value) in combination with orderByKey(). Other type of values or using the version with 2 parameters is not supported";
            if (params2.hasStart()) {
                Node startNode = params2.getIndexStartValue();
                if (params2.getIndexStartName() != ChildKey.getMinName() || !(startNode instanceof StringNode)) {
                    throw new IllegalArgumentException(message);
                }
            }
            if (params2.hasEnd()) {
                Node endNode = params2.getIndexEndValue();
                if (params2.getIndexEndName() != ChildKey.getMaxName() || !(endNode instanceof StringNode)) {
                    throw new IllegalArgumentException(message);
                }
            }
        } else if (!params2.getIndex().equals(PriorityIndex.getInstance())) {
        } else {
            if ((params2.hasStart() && !PriorityUtilities.isValidPriority(params2.getIndexStartValue())) || (params2.hasEnd() && !PriorityUtilities.isValidPriority(params2.getIndexEndValue()))) {
                throw new IllegalArgumentException("When using orderByPriority(), values provided to startAt(), endAt(), or equalTo() must be valid priorities.");
            }
        }
    }

    private void validateLimit(QueryParams params2) {
        if (params2.hasStart() && params2.hasEnd() && params2.hasLimit() && !params2.hasAnchoredLimit()) {
            throw new IllegalArgumentException("Can't combine startAt(), endAt() and limit(). Use limitToFirst() or limitToLast() instead");
        }
    }

    private void validateEqualToCall() {
        if (this.params.hasStart()) {
            throw new IllegalArgumentException("Can't call equalTo() and startAt() combined");
        } else if (this.params.hasEnd()) {
            throw new IllegalArgumentException("Can't call equalTo() and endAt() combined");
        }
    }

    private void validateNoOrderByCall() {
        if (this.orderByCalled) {
            throw new IllegalArgumentException("You can't combine multiple orderBy calls!");
        }
    }

    public ValueEventListener addValueEventListener(ValueEventListener listener) {
        addEventRegistration(new ValueEventRegistration(this.repo, listener, getSpec()));
        return listener;
    }

    public ChildEventListener addChildEventListener(ChildEventListener listener) {
        addEventRegistration(new ChildEventRegistration(this.repo, listener, getSpec()));
        return listener;
    }

    public void addListenerForSingleValueEvent(final ValueEventListener listener) {
        addEventRegistration(new ValueEventRegistration(this.repo, new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                Query.this.removeEventListener((ValueEventListener) this);
                listener.onDataChange(snapshot);
            }

            public void onCancelled(FirebaseError error) {
                listener.onCancelled(error);
            }
        }, getSpec()));
    }

    public void removeEventListener(ValueEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        removeEventRegistration(new ValueEventRegistration(this.repo, listener, getSpec()));
    }

    public void removeEventListener(ChildEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        removeEventRegistration(new ChildEventRegistration(this.repo, listener, getSpec()));
    }

    private void removeEventRegistration(final EventRegistration registration) {
        ZombieEventManager.getInstance().zombifyForRemove(registration);
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.removeEventCallback(registration);
            }
        });
    }

    private void addEventRegistration(final EventRegistration listener) {
        ZombieEventManager.getInstance().recordEventRegistration(listener);
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                Query.this.repo.addEventCallback(listener);
            }
        });
    }

    public void keepSynced(final boolean keepSynced) {
        if (this.path.isEmpty() || !this.path.getFront().equals(ChildKey.getInfoKey())) {
            this.repo.scheduleNow(new Runnable() {
                public void run() {
                    Query.this.repo.keepSynced(Query.this.getSpec(), keepSynced);
                }
            });
            return;
        }
        throw new FirebaseException("Can't call keepSynced() on .info paths.");
    }

    public Query startAt() {
        return startAt((Node) EmptyNode.Empty(), (String) null);
    }

    public Query startAt(String value) {
        return startAt(value, (String) null);
    }

    public Query startAt(double value) {
        return startAt(value, (String) null);
    }

    public Query startAt(boolean value) {
        return startAt(value, (String) null);
    }

    public Query startAt(String value, String key) {
        return startAt(value != null ? new StringNode(value, PriorityUtilities.NullPriority()) : EmptyNode.Empty(), key);
    }

    public Query startAt(double value, String key) {
        return startAt((Node) new DoubleNode(Double.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    public Query startAt(boolean value, String key) {
        return startAt((Node) new BooleanNode(Boolean.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    private Query startAt(Node node, String key) {
        Validation.validateNullableKey(key);
        if (!node.isLeafNode() && !node.isEmpty()) {
            throw new IllegalArgumentException("Can only use simple values for startAt()");
        } else if (this.params.hasStart()) {
            throw new IllegalArgumentException("Can't call startAt() or equalTo() multiple times");
        } else {
            QueryParams newParams = this.params.startAt(node, key != null ? ChildKey.fromString(key) : null);
            validateLimit(newParams);
            validateQueryEndpoints(newParams);
            if ($assertionsDisabled || newParams.isValid()) {
                return new Query(this.repo, this.path, newParams, this.orderByCalled);
            }
            throw new AssertionError();
        }
    }

    public Query endAt() {
        return endAt((Node) Node.MAX_NODE, (String) null);
    }

    public Query endAt(String value) {
        return endAt(value, (String) null);
    }

    public Query endAt(double value) {
        return endAt(value, (String) null);
    }

    public Query endAt(boolean value) {
        return endAt(value, (String) null);
    }

    public Query endAt(String value, String key) {
        return endAt(value != null ? new StringNode(value, PriorityUtilities.NullPriority()) : EmptyNode.Empty(), key);
    }

    public Query endAt(double value, String key) {
        return endAt((Node) new DoubleNode(Double.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    public Query endAt(boolean value, String key) {
        return endAt((Node) new BooleanNode(Boolean.valueOf(value), PriorityUtilities.NullPriority()), key);
    }

    private Query endAt(Node node, String key) {
        Validation.validateNullableKey(key);
        if (node.isLeafNode() || node.isEmpty()) {
            ChildKey childKey = key != null ? ChildKey.fromString(key) : null;
            if (this.params.hasEnd()) {
                throw new IllegalArgumentException("Can't call endAt() or equalTo() multiple times");
            }
            QueryParams newParams = this.params.endAt(node, childKey);
            validateLimit(newParams);
            validateQueryEndpoints(newParams);
            if ($assertionsDisabled || newParams.isValid()) {
                return new Query(this.repo, this.path, newParams, this.orderByCalled);
            }
            throw new AssertionError();
        }
        throw new IllegalArgumentException("Can only use simple values for endAt()");
    }

    public Query equalTo(String value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(double value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(boolean value) {
        validateEqualToCall();
        return startAt(value).endAt(value);
    }

    public Query equalTo(String value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    public Query equalTo(double value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    public Query equalTo(boolean value, String key) {
        validateEqualToCall();
        return startAt(value, key).endAt(value, key);
    }

    @Deprecated
    public Query limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (this.params.hasLimit()) {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        } else {
            QueryParams newParams = this.params.limit(limit);
            validateLimit(newParams);
            return new Query(this.repo, this.path, newParams, this.orderByCalled);
        }
    }

    public Query limitToFirst(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (!this.params.hasLimit()) {
            return new Query(this.repo, this.path, this.params.limitToFirst(limit), this.orderByCalled);
        } else {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        }
    }

    public Query limitToLast(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer!");
        } else if (!this.params.hasLimit()) {
            return new Query(this.repo, this.path, this.params.limitToLast(limit), this.orderByCalled);
        } else {
            throw new IllegalArgumentException("Can't call limitToLast on query with previously set limit!");
        }
    }

    public Query orderByChild(String path2) {
        if (path2 == null) {
            throw new NullPointerException("Key can't be null");
        } else if (path2.equals("$key") || path2.equals(".key")) {
            throw new IllegalArgumentException("Can't use '" + path2 + "' as path, please use orderByKey() instead!");
        } else if (path2.equals("$priority") || path2.equals(".priority")) {
            throw new IllegalArgumentException("Can't use '" + path2 + "' as path, please use orderByPriority() instead!");
        } else if (path2.equals("$value") || path2.equals(".value")) {
            throw new IllegalArgumentException("Can't use '" + path2 + "' as path, please use orderByValue() instead!");
        } else {
            Validation.validatePathString(path2);
            validateNoOrderByCall();
            Path indexPath = new Path(path2);
            if (indexPath.size() == 0) {
                throw new IllegalArgumentException("Can't use empty path, use orderByValue() instead!");
            }
            return new Query(this.repo, this.path, this.params.orderBy(new PathIndex(indexPath)), true);
        }
    }

    public Query orderByPriority() {
        validateNoOrderByCall();
        QueryParams newParams = this.params.orderBy(PriorityIndex.getInstance());
        validateQueryEndpoints(newParams);
        return new Query(this.repo, this.path, newParams, true);
    }

    public Query orderByKey() {
        validateNoOrderByCall();
        QueryParams newParams = this.params.orderBy(KeyIndex.getInstance());
        validateQueryEndpoints(newParams);
        return new Query(this.repo, this.path, newParams, true);
    }

    public Query orderByValue() {
        validateNoOrderByCall();
        return new Query(this.repo, this.path, this.params.orderBy(ValueIndex.getInstance()), true);
    }

    public Firebase getRef() {
        return new Firebase(this.repo, getPath());
    }

    public Path getPath() {
        return this.path;
    }

    public Repo getRepo() {
        return this.repo;
    }

    public QuerySpec getSpec() {
        return new QuerySpec(this.path, this.params);
    }
}
