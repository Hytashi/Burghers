package fr.hytashi.burghers.utils;

import java.util.*;

public class UniqueRandomName {

    private static final Set<String> USED = new HashSet<>();

    private static final String SYMBOLS = "@*^!?/:;.#&[]{}+-=°()<>";
    private final Random random;
    private final char[] charset;
    private final char[] buf;

    public UniqueRandomName(final int length) {
        this.random = new Random();
        this.charset = "@*^!?/:;.#&[]{}+-=°()<>".toCharArray();
        this.buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < this.buf.length; ++idx) {
            this.buf[idx] = this.charset[this.random.nextInt(this.charset.length)];
        }
        String s = new String(buf);
        if (USED.contains(s)) {
            return nextString();
        }
        USED.add(s);
        return new String(this.buf);
    }

}
