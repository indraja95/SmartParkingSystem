package com.firebase.client.utilities;

import java.util.Random;

public class PushIdGenerator {
    static final /* synthetic */ boolean $assertionsDisabled = (!PushIdGenerator.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    private static long lastPushTime = 0;
    private static final int[] lastRandChars = new int[12];
    private static final Random randGen = new Random();

    public static synchronized String generatePushChildName(long now) {
        String sb;
        synchronized (PushIdGenerator.class) {
            boolean duplicateTime = now == lastPushTime ? true : $assertionsDisabled;
            lastPushTime = now;
            char[] timeStampChars = new char[8];
            StringBuilder result = new StringBuilder(20);
            for (int i = 7; i >= 0; i--) {
                timeStampChars[i] = PUSH_CHARS.charAt((int) (now % 64));
                now /= 64;
            }
            if ($assertionsDisabled || now == 0) {
                result.append(timeStampChars);
                if (!duplicateTime) {
                    for (int i2 = 0; i2 < 12; i2++) {
                        lastRandChars[i2] = randGen.nextInt(64);
                    }
                } else {
                    incrementArray();
                }
                for (int i3 = 0; i3 < 12; i3++) {
                    result.append(PUSH_CHARS.charAt(lastRandChars[i3]));
                }
                if ($assertionsDisabled || result.length() == 20) {
                    sb = result.toString();
                } else {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }
        }
        return sb;
    }

    private static void incrementArray() {
        int i = 11;
        while (i >= 0) {
            if (lastRandChars[i] != 63) {
                lastRandChars[i] = lastRandChars[i] + 1;
                return;
            } else {
                lastRandChars[i] = 0;
                i--;
            }
        }
    }
}
