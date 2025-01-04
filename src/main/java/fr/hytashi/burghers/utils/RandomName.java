package fr.hytashi.burghers.utils;

import java.util.*;

public class RandomName {

    private static final String SYMBOLS = "@*^!?/:;.#&[]{}+-=°()<>";
    private final Random random;
    private final char[] charset;
    private final char[] buf;

    public RandomName(final int length) {
        this.random = new Random();
        this.charset = "@*^!?/:;.#&[]{}+-=°()<>".toCharArray();
        this.buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < this.buf.length; ++idx) {
            this.buf[idx] = this.charset[this.random.nextInt(this.charset.length)];
        }
        return new String(this.buf);
    }

}
