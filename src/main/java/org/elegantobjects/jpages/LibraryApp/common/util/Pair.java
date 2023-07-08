package org.elegantobjects.jpages.LibraryApp.common.util;

/**
 * Pair Utility class.
 *
 * Utility class to hold a pair of objects.
 *
 * @param <T> Type of first object
 * @param <U> Type of second object
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    T getFirst() {
        return first;
    }

    U getSecond() {
        return second;
    }
}
