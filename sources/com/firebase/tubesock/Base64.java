package com.firebase.tubesock;

import com.google.appinventor.components.runtime.util.Ev3Constants.Opcode;
import java.util.Arrays;

public class Base64 {
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[256];

    static {
        Arrays.fill(IA, -1);
        int iS = CA.length;
        for (int i = 0; i < iS; i++) {
            IA[CA[i]] = i;
        }
        IA[61] = 0;
    }

    public static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new char[0];
        }
        int eLen = (sLen / 3) * 3;
        int cCnt = (((sLen - 1) / 3) + 1) << 2;
        int dLen = cCnt + (lineSep ? ((cCnt - 1) / 76) << 1 : 0);
        char[] dArr = new char[dLen];
        int cc = 0;
        int d = 0;
        int s = 0;
        while (s < eLen) {
            int s2 = s + 1;
            int s3 = s2 + 1;
            int s4 = s3 + 1;
            int i = ((sArr[s] & Opcode.TST) << 16) | ((sArr[s2] & Opcode.TST) << 8) | (sArr[s3] & 255);
            int d2 = d + 1;
            dArr[d] = CA[(i >>> 18) & 63];
            int d3 = d2 + 1;
            dArr[d2] = CA[(i >>> 12) & 63];
            int d4 = d3 + 1;
            dArr[d3] = CA[(i >>> 6) & 63];
            int d5 = d4 + 1;
            dArr[d4] = CA[i & 63];
            if (lineSep) {
                cc++;
                if (cc == 19 && d5 < dLen - 2) {
                    int d6 = d5 + 1;
                    dArr[d5] = 13;
                    d5 = d6 + 1;
                    dArr[d6] = 10;
                    cc = 0;
                }
            }
            d = d5;
            s = s4;
        }
        int left = sLen - eLen;
        if (left <= 0) {
            return dArr;
        }
        int i2 = ((sArr[eLen] & Opcode.TST) << 10) | (left == 2 ? (sArr[sLen - 1] & Opcode.TST) << 2 : 0);
        dArr[dLen - 4] = CA[i2 >> 12];
        dArr[dLen - 3] = CA[(i2 >>> 6) & 63];
        dArr[dLen - 2] = left == 2 ? CA[i2 & 63] : '=';
        dArr[dLen - 1] = '=';
        return dArr;
    }

    public static final byte[] decode(char[] sArr) {
        int sLen;
        int s;
        if (sArr != null) {
            sLen = sArr.length;
        } else {
            sLen = 0;
        }
        if (sLen == 0) {
            return new byte[0];
        }
        int sepCnt = 0;
        for (int i = 0; i < sLen; i++) {
            if (IA[sArr[i]] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        int i2 = sLen;
        while (i2 > 1) {
            i2--;
            if (IA[sArr[i2]] > 0) {
                break;
            } else if (sArr[i2] == '=') {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s2 = 0;
        int d = 0;
        while (d < len) {
            int i3 = 0;
            int j = 0;
            while (true) {
                s = s2;
                if (j >= 4) {
                    break;
                }
                s2 = s + 1;
                int c = IA[sArr[s]];
                if (c >= 0) {
                    i3 |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
            }
            int d2 = d + 1;
            dArr[d] = (byte) (i3 >> 16);
            if (d2 < len) {
                int d3 = d2 + 1;
                dArr[d2] = (byte) (i3 >> 8);
                if (d3 < len) {
                    d2 = d3 + 1;
                    dArr[d3] = (byte) i3;
                } else {
                    d2 = d3;
                }
            }
            d = d2;
            s2 = s;
        }
        return dArr;
    }

    public static final byte[] decodeFast(char[] sArr) {
        int sepCnt;
        int sIx;
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx2 = 0;
        int eIx = sLen - 1;
        while (sIx2 < eIx && IA[sArr[sIx2]] < 0) {
            sIx2++;
        }
        while (eIx > 0 && IA[sArr[eIx]] < 0) {
            eIx--;
        }
        int pad = sArr[eIx] == '=' ? sArr[eIx + -1] == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx2) + 1;
        if (sLen > 76) {
            sepCnt = (sArr[76] == 13 ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx3 = sIx2;
        while (d < eLen) {
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int sIx6 = sIx5 + 1;
            int sIx7 = sIx6 + 1;
            int i = (IA[sArr[sIx3]] << 18) | (IA[sArr[sIx4]] << 12) | (IA[sArr[sIx5]] << 6) | IA[sArr[sIx6]];
            int d2 = d + 1;
            dArr[d] = (byte) (i >> 16);
            int d3 = d2 + 1;
            dArr[d2] = (byte) (i >> 8);
            int d4 = d3 + 1;
            dArr[d3] = (byte) i;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx7 + 2;
                    cc = 0;
                    d = d4;
                    sIx3 = sIx;
                }
            }
            sIx = sIx7;
            d = d4;
            sIx3 = sIx;
        }
        if (d < len) {
            int i2 = 0;
            int j = 0;
            while (sIx3 <= eIx - pad) {
                i2 |= IA[sArr[sIx3]] << (18 - (j * 6));
                j++;
                sIx3++;
            }
            int r = 16;
            while (d < len) {
                int d5 = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = d5;
            }
        }
        int i3 = d;
        int i4 = sIx3;
        return dArr;
    }

    public static final byte[] encodeToByte(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }
        int eLen = (sLen / 3) * 3;
        int cCnt = (((sLen - 1) / 3) + 1) << 2;
        int dLen = cCnt + (lineSep ? ((cCnt - 1) / 76) << 1 : 0);
        byte[] dArr = new byte[dLen];
        int cc = 0;
        int d = 0;
        int s = 0;
        while (s < eLen) {
            int s2 = s + 1;
            int s3 = s2 + 1;
            int s4 = s3 + 1;
            int i = ((sArr[s] & Opcode.TST) << 16) | ((sArr[s2] & Opcode.TST) << 8) | (sArr[s3] & 255);
            int d2 = d + 1;
            dArr[d] = (byte) CA[(i >>> 18) & 63];
            int d3 = d2 + 1;
            dArr[d2] = (byte) CA[(i >>> 12) & 63];
            int d4 = d3 + 1;
            dArr[d3] = (byte) CA[(i >>> 6) & 63];
            int d5 = d4 + 1;
            dArr[d4] = (byte) CA[i & 63];
            if (lineSep) {
                cc++;
                if (cc == 19 && d5 < dLen - 2) {
                    int d6 = d5 + 1;
                    dArr[d5] = 13;
                    d5 = d6 + 1;
                    dArr[d6] = 10;
                    cc = 0;
                }
            }
            d = d5;
            s = s4;
        }
        int left = sLen - eLen;
        if (left <= 0) {
            return dArr;
        }
        int i2 = ((sArr[eLen] & Opcode.TST) << 10) | (left == 2 ? (sArr[sLen - 1] & Opcode.TST) << 2 : 0);
        dArr[dLen - 4] = (byte) CA[i2 >> 12];
        dArr[dLen - 3] = (byte) CA[(i2 >>> 6) & 63];
        dArr[dLen - 2] = left == 2 ? (byte) CA[i2 & 63] : Opcode.MOVEF_16;
        dArr[dLen - 1] = Opcode.MOVEF_16;
        return dArr;
    }

    public static final byte[] decode(byte[] sArr) {
        int s;
        int sepCnt = 0;
        for (byte b : sArr) {
            if (IA[b & Opcode.TST] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        int i = sLen;
        while (i > 1) {
            i--;
            if (IA[sArr[i] & Opcode.TST] > 0) {
                break;
            } else if (sArr[i] == 61) {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s2 = 0;
        int d = 0;
        while (d < len) {
            int i2 = 0;
            int j = 0;
            while (true) {
                s = s2;
                if (j >= 4) {
                    break;
                }
                s2 = s + 1;
                int c = IA[sArr[s] & Opcode.TST];
                if (c >= 0) {
                    i2 |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
            }
            int d2 = d + 1;
            dArr[d] = (byte) (i2 >> 16);
            if (d2 < len) {
                int d3 = d2 + 1;
                dArr[d2] = (byte) (i2 >> 8);
                if (d3 < len) {
                    d2 = d3 + 1;
                    dArr[d3] = (byte) i2;
                } else {
                    d2 = d3;
                }
            }
            d = d2;
            s2 = s;
        }
        return dArr;
    }

    public static final byte[] decodeFast(byte[] sArr) {
        int sepCnt;
        int sIx;
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx2 = 0;
        int eIx = sLen - 1;
        while (sIx2 < eIx && IA[sArr[sIx2] & Opcode.TST] < 0) {
            sIx2++;
        }
        while (eIx > 0 && IA[sArr[eIx] & Opcode.TST] < 0) {
            eIx--;
        }
        int pad = sArr[eIx] == 61 ? sArr[eIx + -1] == 61 ? 2 : 1 : 0;
        int cCnt = (eIx - sIx2) + 1;
        if (sLen > 76) {
            sepCnt = (sArr[76] == 13 ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx3 = sIx2;
        while (d < eLen) {
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int sIx6 = sIx5 + 1;
            int sIx7 = sIx6 + 1;
            int i = (IA[sArr[sIx3]] << 18) | (IA[sArr[sIx4]] << 12) | (IA[sArr[sIx5]] << 6) | IA[sArr[sIx6]];
            int d2 = d + 1;
            dArr[d] = (byte) (i >> 16);
            int d3 = d2 + 1;
            dArr[d2] = (byte) (i >> 8);
            int d4 = d3 + 1;
            dArr[d3] = (byte) i;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx7 + 2;
                    cc = 0;
                    d = d4;
                    sIx3 = sIx;
                }
            }
            sIx = sIx7;
            d = d4;
            sIx3 = sIx;
        }
        if (d < len) {
            int i2 = 0;
            int j = 0;
            while (sIx3 <= eIx - pad) {
                i2 |= IA[sArr[sIx3]] << (18 - (j * 6));
                j++;
                sIx3++;
            }
            int r = 16;
            while (d < len) {
                int d5 = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = d5;
            }
        }
        int i3 = d;
        int i4 = sIx3;
        return dArr;
    }

    public static final String encodeToString(byte[] sArr, boolean lineSep) {
        return new String(encodeToChar(sArr, lineSep));
    }

    public static final byte[] decode(String str) {
        int sLen;
        int s;
        if (str != null) {
            sLen = str.length();
        } else {
            sLen = 0;
        }
        if (sLen == 0) {
            return new byte[0];
        }
        int sepCnt = 0;
        for (int i = 0; i < sLen; i++) {
            if (IA[str.charAt(i)] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        int i2 = sLen;
        while (i2 > 1) {
            i2--;
            if (IA[str.charAt(i2)] > 0) {
                break;
            } else if (str.charAt(i2) == '=') {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s2 = 0;
        int d = 0;
        while (d < len) {
            int i3 = 0;
            int j = 0;
            while (true) {
                s = s2;
                if (j >= 4) {
                    break;
                }
                s2 = s + 1;
                int c = IA[str.charAt(s)];
                if (c >= 0) {
                    i3 |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
            }
            int d2 = d + 1;
            dArr[d] = (byte) (i3 >> 16);
            if (d2 < len) {
                int d3 = d2 + 1;
                dArr[d2] = (byte) (i3 >> 8);
                if (d3 < len) {
                    d2 = d3 + 1;
                    dArr[d3] = (byte) i3;
                } else {
                    d2 = d3;
                }
            }
            d = d2;
            s2 = s;
        }
        return dArr;
    }

    public static final byte[] decodeFast(String s) {
        int sepCnt;
        int sIx;
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx2 = 0;
        int eIx = sLen - 1;
        while (sIx2 < eIx && IA[s.charAt(sIx2) & 255] < 0) {
            sIx2++;
        }
        while (eIx > 0 && IA[s.charAt(eIx) & 255] < 0) {
            eIx--;
        }
        int pad = s.charAt(eIx) == '=' ? s.charAt(eIx + -1) == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx2) + 1;
        if (sLen > 76) {
            sepCnt = (s.charAt(76) == 13 ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx3 = sIx2;
        while (d < eLen) {
            int sIx4 = sIx3 + 1;
            int sIx5 = sIx4 + 1;
            int sIx6 = sIx5 + 1;
            int sIx7 = sIx6 + 1;
            int i = (IA[s.charAt(sIx3)] << 18) | (IA[s.charAt(sIx4)] << 12) | (IA[s.charAt(sIx5)] << 6) | IA[s.charAt(sIx6)];
            int d2 = d + 1;
            dArr[d] = (byte) (i >> 16);
            int d3 = d2 + 1;
            dArr[d2] = (byte) (i >> 8);
            int d4 = d3 + 1;
            dArr[d3] = (byte) i;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx7 + 2;
                    cc = 0;
                    d = d4;
                    sIx3 = sIx;
                }
            }
            sIx = sIx7;
            d = d4;
            sIx3 = sIx;
        }
        if (d < len) {
            int i2 = 0;
            int j = 0;
            while (sIx3 <= eIx - pad) {
                i2 |= IA[s.charAt(sIx3)] << (18 - (j * 6));
                j++;
                sIx3++;
            }
            int r = 16;
            while (d < len) {
                int d5 = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = d5;
            }
        }
        int i3 = d;
        int i4 = sIx3;
        return dArr;
    }
}
