package com.shaded.fasterxml.jackson.core.io;

import com.firebase.client.core.Constants;
import com.google.appinventor.components.runtime.util.Ev3Constants.Opcode;

public final class NumberOutput {
    private static int BILLION = 1000000000;
    static final char[] FULL_TRIPLETS = new char[4000];
    static final byte[] FULL_TRIPLETS_B = new byte[4000];
    static final char[] LEADING_TRIPLETS = new char[4000];
    private static long MAX_INT_AS_LONG = 2147483647L;
    private static int MILLION = 1000000;
    private static long MIN_INT_AS_LONG = -2147483648L;
    private static final char NULL_CHAR = 0;
    static final String SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);
    private static long TEN_BILLION_L = 10000000000L;
    private static long THOUSAND_L = 1000;
    static final String[] sSmallIntStrs = {"0", "1", "2", "3", "4", Constants.WIRE_PROTOCOL_VERSION, "6", "7", "8", "9", "10"};
    static final String[] sSmallIntStrs2 = {"-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10"};

    static {
        char c;
        char c2;
        int i = 0;
        for (int i2 = 0; i2 < 10; i2++) {
            char c3 = (char) (i2 + 48);
            if (i2 == 0) {
                c = 0;
            } else {
                c = c3;
            }
            int i3 = 0;
            while (i3 < 10) {
                char c4 = (char) (i3 + 48);
                if (i2 == 0 && i3 == 0) {
                    c2 = 0;
                } else {
                    c2 = c4;
                }
                int i4 = i;
                for (int i5 = 0; i5 < 10; i5++) {
                    char c5 = (char) (i5 + 48);
                    LEADING_TRIPLETS[i4] = c;
                    LEADING_TRIPLETS[i4 + 1] = c2;
                    LEADING_TRIPLETS[i4 + 2] = c5;
                    FULL_TRIPLETS[i4] = c3;
                    FULL_TRIPLETS[i4 + 1] = c4;
                    FULL_TRIPLETS[i4 + 2] = c5;
                    i4 += 4;
                }
                i3++;
                i = i4;
            }
        }
        for (int i6 = 0; i6 < 4000; i6++) {
            FULL_TRIPLETS_B[i6] = (byte) FULL_TRIPLETS[i6];
        }
    }

    public static int outputInt(int i, char[] cArr, int i2) {
        int outputLeadingTriplet;
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                return outputLong((long) i, cArr, i2);
            }
            int i3 = i2 + 1;
            cArr[i2] = '-';
            i = -i;
            i2 = i3;
        }
        if (i >= MILLION) {
            boolean z = i >= BILLION;
            if (z) {
                i -= BILLION;
                if (i >= BILLION) {
                    i -= BILLION;
                    int i4 = i2 + 1;
                    cArr[i2] = '2';
                    i2 = i4;
                } else {
                    int i5 = i2 + 1;
                    cArr[i2] = '1';
                    i2 = i5;
                }
            }
            int i6 = i / 1000;
            int i7 = i - (i6 * 1000);
            int i8 = i6 / 1000;
            int i9 = i6 - (i8 * 1000);
            if (z) {
                outputLeadingTriplet = outputFullTriplet(i8, cArr, i2);
            } else {
                outputLeadingTriplet = outputLeadingTriplet(i8, cArr, i2);
            }
            return outputFullTriplet(i7, cArr, outputFullTriplet(i9, cArr, outputLeadingTriplet));
        } else if (i >= 1000) {
            int i10 = i / 1000;
            return outputFullTriplet(i - (i10 * 1000), cArr, outputLeadingTriplet(i10, cArr, i2));
        } else if (i >= 10) {
            return outputLeadingTriplet(i, cArr, i2);
        } else {
            int i11 = i2 + 1;
            cArr[i2] = (char) (i + 48);
            return i11;
        }
    }

    public static int outputInt(int i, byte[] bArr, int i2) {
        int outputLeadingTriplet;
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                return outputLong((long) i, bArr, i2);
            }
            int i3 = i2 + 1;
            bArr[i2] = Opcode.RL16;
            i = -i;
            i2 = i3;
        }
        if (i >= MILLION) {
            boolean z = i >= BILLION;
            if (z) {
                i -= BILLION;
                if (i >= BILLION) {
                    i -= BILLION;
                    int i4 = i2 + 1;
                    bArr[i2] = Opcode.MOVE8_32;
                    i2 = i4;
                } else {
                    int i5 = i2 + 1;
                    bArr[i2] = Opcode.MOVE8_16;
                    i2 = i5;
                }
            }
            int i6 = i / 1000;
            int i7 = i - (i6 * 1000);
            int i8 = i6 / 1000;
            int i9 = i6 - (i8 * 1000);
            if (z) {
                outputLeadingTriplet = outputFullTriplet(i8, bArr, i2);
            } else {
                outputLeadingTriplet = outputLeadingTriplet(i8, bArr, i2);
            }
            return outputFullTriplet(i7, bArr, outputFullTriplet(i9, bArr, outputLeadingTriplet));
        } else if (i >= 1000) {
            int i10 = i / 1000;
            return outputFullTriplet(i - (i10 * 1000), bArr, outputLeadingTriplet(i10, bArr, i2));
        } else if (i >= 10) {
            return outputLeadingTriplet(i, bArr, i2);
        } else {
            int i11 = i2 + 1;
            bArr[i2] = (byte) (i + 48);
            return i11;
        }
    }

    public static int outputLong(long j, char[] cArr, int i) {
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, cArr, i);
            }
            if (j == Long.MIN_VALUE) {
                int length = SMALLEST_LONG.length();
                SMALLEST_LONG.getChars(0, length, cArr, i);
                return i + length;
            }
            int i2 = i + 1;
            cArr[i] = '-';
            j = -j;
            i = i2;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, cArr, i);
        }
        int calcLongStrLength = i + calcLongStrLength(j);
        int i3 = calcLongStrLength;
        while (j > MAX_INT_AS_LONG) {
            i3 -= 3;
            long j2 = j / THOUSAND_L;
            outputFullTriplet((int) (j - (THOUSAND_L * j2)), cArr, i3);
            j = j2;
        }
        int i4 = i3;
        int i5 = (int) j;
        while (i5 >= 1000) {
            int i6 = i4 - 3;
            int i7 = i5 / 1000;
            outputFullTriplet(i5 - (i7 * 1000), cArr, i6);
            i5 = i7;
            i4 = i6;
        }
        outputLeadingTriplet(i5, cArr, i);
        return calcLongStrLength;
    }

    public static int outputLong(long j, byte[] bArr, int i) {
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, bArr, i);
            }
            if (j == Long.MIN_VALUE) {
                int length = SMALLEST_LONG.length();
                int i2 = 0;
                int i3 = i;
                while (i2 < length) {
                    int i4 = i3 + 1;
                    bArr[i3] = (byte) SMALLEST_LONG.charAt(i2);
                    i2++;
                    i3 = i4;
                }
                return i3;
            }
            int i5 = i + 1;
            bArr[i] = Opcode.RL16;
            j = -j;
            i = i5;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, bArr, i);
        }
        int calcLongStrLength = i + calcLongStrLength(j);
        int i6 = calcLongStrLength;
        while (j > MAX_INT_AS_LONG) {
            i6 -= 3;
            long j2 = j / THOUSAND_L;
            outputFullTriplet((int) (j - (THOUSAND_L * j2)), bArr, i6);
            j = j2;
        }
        int i7 = i6;
        int i8 = (int) j;
        while (i8 >= 1000) {
            int i9 = i7 - 3;
            int i10 = i8 / 1000;
            outputFullTriplet(i8 - (i10 * 1000), bArr, i9);
            i8 = i10;
            i7 = i9;
        }
        outputLeadingTriplet(i8, bArr, i);
        return calcLongStrLength;
    }

    public static String toString(int i) {
        if (i < sSmallIntStrs.length) {
            if (i >= 0) {
                return sSmallIntStrs[i];
            }
            int i2 = (-i) - 1;
            if (i2 < sSmallIntStrs2.length) {
                return sSmallIntStrs2[i2];
            }
        }
        return Integer.toString(i);
    }

    public static String toString(long j) {
        if (j > 2147483647L || j < -2147483648L) {
            return Long.toString(j);
        }
        return toString((int) j);
    }

    public static String toString(double d) {
        return Double.toString(d);
    }

    private static int outputLeadingTriplet(int i, char[] cArr, int i2) {
        int i3 = i << 2;
        int i4 = i3 + 1;
        char c = LEADING_TRIPLETS[i3];
        if (c != 0) {
            int i5 = i2 + 1;
            cArr[i2] = c;
            i2 = i5;
        }
        int i6 = i4 + 1;
        char c2 = LEADING_TRIPLETS[i4];
        if (c2 != 0) {
            int i7 = i2 + 1;
            cArr[i2] = c2;
            i2 = i7;
        }
        int i8 = i2 + 1;
        cArr[i2] = LEADING_TRIPLETS[i6];
        return i8;
    }

    private static int outputLeadingTriplet(int i, byte[] bArr, int i2) {
        int i3 = i << 2;
        int i4 = i3 + 1;
        char c = LEADING_TRIPLETS[i3];
        if (c != 0) {
            int i5 = i2 + 1;
            bArr[i2] = (byte) c;
            i2 = i5;
        }
        int i6 = i4 + 1;
        char c2 = LEADING_TRIPLETS[i4];
        if (c2 != 0) {
            int i7 = i2 + 1;
            bArr[i2] = (byte) c2;
            i2 = i7;
        }
        int i8 = i2 + 1;
        bArr[i2] = (byte) LEADING_TRIPLETS[i6];
        return i8;
    }

    private static int outputFullTriplet(int i, char[] cArr, int i2) {
        int i3 = i << 2;
        int i4 = i2 + 1;
        int i5 = i3 + 1;
        cArr[i2] = FULL_TRIPLETS[i3];
        int i6 = i4 + 1;
        int i7 = i5 + 1;
        cArr[i4] = FULL_TRIPLETS[i5];
        int i8 = i6 + 1;
        cArr[i6] = FULL_TRIPLETS[i7];
        return i8;
    }

    private static int outputFullTriplet(int i, byte[] bArr, int i2) {
        int i3 = i << 2;
        int i4 = i2 + 1;
        int i5 = i3 + 1;
        bArr[i2] = FULL_TRIPLETS_B[i3];
        int i6 = i4 + 1;
        int i7 = i5 + 1;
        bArr[i4] = FULL_TRIPLETS_B[i5];
        int i8 = i6 + 1;
        bArr[i6] = FULL_TRIPLETS_B[i7];
        return i8;
    }

    private static int calcLongStrLength(long j) {
        int i = 10;
        for (long j2 = TEN_BILLION_L; j >= j2 && i != 19; j2 = (j2 << 1) + (j2 << 3)) {
            i++;
        }
        return i;
    }
}
