package com.firebase.client.core;

import com.firebase.client.FirebaseError;
import com.firebase.client.annotations.NotNull;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EventRegistration {
    static final /* synthetic */ boolean $assertionsDisabled = (!EventRegistration.class.desiredAssertionStatus());
    private boolean isUserInitiated = false;
    private EventRegistrationZombieListener listener;
    private AtomicBoolean zombied = new AtomicBoolean(false);

    public abstract EventRegistration clone(QuerySpec querySpec);

    public abstract DataEvent createEvent(Change change, QuerySpec querySpec);

    public abstract void fireCancelEvent(FirebaseError firebaseError);

    public abstract void fireEvent(DataEvent dataEvent);

    @NotNull
    public abstract QuerySpec getQuerySpec();

    public abstract boolean isSameListener(EventRegistration eventRegistration);

    public abstract boolean respondsTo(EventType eventType);

    public void zombify() {
        if (this.zombied.compareAndSet(false, true) && this.listener != null) {
            this.listener.onZombied(this);
            this.listener = null;
        }
    }

    public boolean isZombied() {
        return this.zombied.get();
    }

    public void setOnZombied(EventRegistrationZombieListener listener2) {
        if (!$assertionsDisabled && isZombied()) {
            throw new AssertionError();
        } else if ($assertionsDisabled || this.listener == null) {
            this.listener = listener2;
        } else {
            throw new AssertionError();
        }
    }

    public boolean isUserInitiated() {
        return this.isUserInitiated;
    }

    public void setIsUserInitiated(boolean isUserInitiated2) {
        this.isUserInitiated = isUserInitiated2;
    }

    /* access modifiers changed from: 0000 */
    public Repo getRepo() {
        return null;
    }
}
