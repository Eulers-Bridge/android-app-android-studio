package com.eulersbridge.isegoria.util.data;

/**
 * Matches the interface of java.util.function.Consumer, currently Java 8 & Android API 24+ only,
 * in order to allow feature usage on Android API <24.
 */
@FunctionalInterface
public interface Consumer<T> {
    void accept(T var1);
}