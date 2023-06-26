package org.elegantobjects.jpages.App2.core;

class Pair<T, U> {
    private final T first;
    private final U second;

    Pair(T first, U second) {
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
