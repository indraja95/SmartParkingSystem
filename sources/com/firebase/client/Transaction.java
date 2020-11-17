package com.firebase.client;

import com.firebase.client.snapshot.Node;

public class Transaction {

    public interface Handler {
        Result doTransaction(MutableData mutableData);

        void onComplete(FirebaseError firebaseError, boolean z, DataSnapshot dataSnapshot);
    }

    public static class Result {
        private Node data;
        private boolean success;

        private Result(boolean success2, Node data2) {
            this.success = success2;
            this.data = data2;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Node getNode() {
            return this.data;
        }
    }

    public static Result abort() {
        return new Result(false, null);
    }

    public static Result success(MutableData resultData) {
        return new Result(true, resultData.getNode());
    }
}
