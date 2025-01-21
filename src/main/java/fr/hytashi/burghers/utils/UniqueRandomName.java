package fr.hytashi.burghers.utils;

import java.util.*;

public class UniqueRandomName {

    private static final Set<String> USED = new HashSet<>();
    private static final Random RANDOM = new Random();

    public static final int MAX_ATTEMPTS = 10;
    private static final String CHARSET = "@*^!?/:;.#&[]{}+-=°()<>";

    /**
     * Generates a new unique random name of the specified length.
     *
     * @param length the length of the random name to generate; must be greater than 0
     * @return a unique random name
     * @throws RandomGenerationException if a unique name could not be generated after MAX_ATTEMPTS attempts
     * @throws IllegalArgumentException if the specified length is less than or equal to 0
     */
    public static String generate(int length) throws RandomGenerationException {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            StringBuilder buff = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                buff.append(CHARSET.charAt(RANDOM.nextInt(0, CHARSET.length())));
            }
            String candidate = buff.toString();

            if (!USED.contains(candidate)) {
                USED.add(candidate);
                return candidate;
            }
        }

        throw new RandomGenerationException("Unable to generate a unique random name after " + MAX_ATTEMPTS + " attempts.");
    }

    public static class RandomGenerationException extends RuntimeException {
        public RandomGenerationException(String message) {
            super(message);
        }
    }
}
