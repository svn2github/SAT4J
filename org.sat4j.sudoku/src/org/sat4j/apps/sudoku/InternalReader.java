package org.sat4j.apps.sudoku;

import java.io.Reader;

public class InternalReader extends Reader {

    InternalReader(String buffer) {
        this.buffer = buffer;
        p = 0;
    }

    String buffer = null;

    int p = 0;

    public void reopen() {
        p = 0;
    }

    @Override
    public void close() {
    }

    @Override
    public int read(char[] chs, int offset, int length) {
        if (p >= buffer.length()) {
            return -1;
        }

        int n = 0;
        for (n = 0; (n < length) && (p < buffer.length()); n++) {
            chs[offset + n] = buffer.charAt(p);
            p++;
        }
        return n;
    }

    char[] c;
}
