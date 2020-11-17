package com.firebase.client.core;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.annotations.NotNull;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;

public class ChildEventRegistration extends EventRegistration {
    private final ChildEventListener eventListener;
    private final Repo repo;
    private final QuerySpec spec;

    public ChildEventRegistration(@NotNull Repo repo2, @NotNull ChildEventListener eventListener2, @NotNull QuerySpec spec2) {
        this.repo = repo2;
        this.eventListener = eventListener2;
        this.spec = spec2;
    }

    public boolean respondsTo(EventType eventType) {
        return eventType != EventType.VALUE;
    }

    public boolean equals(Object other) {
        return (other instanceof ChildEventRegistration) && ((ChildEventRegistration) other).eventListener.equals(this.eventListener) && ((ChildEventRegistration) other).repo.equals(this.repo) && ((ChildEventRegistration) other).spec.equals(this.spec);
    }

    public int hashCode() {
        return this.eventListener.hashCode();
    }

    public DataEvent createEvent(Change change, QuerySpec query) {
        return new DataEvent(change.getEventType(), this, new DataSnapshot(new Firebase(this.repo, query.getPath().child(change.getChildKey())), change.getIndexedNode()), change.getPrevName() != null ? change.getPrevName().asString() : null);
    }

    public void fireEvent(DataEvent eventData) {
        if (!isZombied()) {
            switch (eventData.getEventType()) {
                case CHILD_ADDED:
                    this.eventListener.onChildAdded(eventData.getSnapshot(), eventData.getPreviousName());
                    return;
                case CHILD_CHANGED:
                    this.eventListener.onChildChanged(eventData.getSnapshot(), eventData.getPreviousName());
                    return;
                case CHILD_MOVED:
                    this.eventListener.onChildMoved(eventData.getSnapshot(), eventData.getPreviousName());
                    return;
                case CHILD_REMOVED:
                    this.eventListener.onChildRemoved(eventData.getSnapshot());
                    return;
                default:
                    return;
            }
        }
    }

    public void fireCancelEvent(FirebaseError error) {
        this.eventListener.onCancelled(error);
    }

    public EventRegistration clone(QuerySpec newQuery) {
        return new ChildEventRegistration(this.repo, this.eventListener, newQuery);
    }

    public boolean isSameListener(EventRegistration other) {
        return (other instanceof ChildEventRegistration) && ((ChildEventRegistration) other).eventListener.equals(this.eventListener);
    }

    @NotNull
    public QuerySpec getQuerySpec() {
        return this.spec;
    }

    public String toString() {
        return "ChildEventRegistration";
    }

    /* access modifiers changed from: 0000 */
    public Repo getRepo() {
        return this.repo;
    }
}
