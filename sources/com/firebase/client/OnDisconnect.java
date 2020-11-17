package com.firebase.client;

import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.core.Path;
import com.firebase.client.core.Repo;
import com.firebase.client.core.ValidationPath;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.utilities.Validation;
import com.firebase.client.utilities.encoding.JsonHelpers;
import java.util.Map;

public class OnDisconnect {
    /* access modifiers changed from: private */
    public Path path;
    /* access modifiers changed from: private */
    public Repo repo;

    OnDisconnect(Repo repo2, Path path2) {
        this.repo = repo2;
        this.path = path2;
    }

    public void setValue(Object value) {
        onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), null);
    }

    public void setValue(Object value, String priority) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), null);
    }

    public void setValue(Object value, double priority) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(Double.valueOf(priority)), null);
    }

    public void setValue(Object value, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), listener);
    }

    public void setValue(Object value, String priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), listener);
    }

    public void setValue(Object value, double priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(Double.valueOf(priority)), listener);
    }

    public void setValue(Object value, Map priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), listener);
    }

    private void onDisconnectSetInternal(Object value, Node priority, final CompletionListener onComplete) {
        Validation.validateWritablePath(this.path);
        ValidationPath.validateWithObject(this.path, value);
        try {
            Object bouncedValue = JsonHelpers.getMapper().convertValue(value, Object.class);
            Validation.validateWritableObject(bouncedValue);
            final Node node = NodeUtilities.NodeFromJSON(bouncedValue, priority);
            this.repo.scheduleNow(new Runnable() {
                public void run() {
                    OnDisconnect.this.repo.onDisconnectSetValue(OnDisconnect.this.path, node, onComplete);
                }
            });
        } catch (IllegalArgumentException e) {
            throw new FirebaseException("Failed to parse to snapshot", e);
        }
    }

    public void updateChildren(Map<String, Object> update) {
        updateChildren(update, null);
    }

    public void updateChildren(final Map<String, Object> update, final CompletionListener listener) {
        final Map<Path, Node> parsedUpdate = Validation.parseAndValidateUpdate(this.path, update);
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                OnDisconnect.this.repo.onDisconnectUpdate(OnDisconnect.this.path, parsedUpdate, listener, update);
            }
        });
    }

    public void removeValue() {
        setValue(null);
    }

    public void removeValue(CompletionListener listener) {
        setValue((Object) null, listener);
    }

    public void cancel() {
        cancel(null);
    }

    public void cancel(final CompletionListener listener) {
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                OnDisconnect.this.repo.onDisconnectCancel(OnDisconnect.this.path, listener);
            }
        });
    }
}
