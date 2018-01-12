package com.eulersbridge.isegoria.auth.signup;

/**
 * Matches the interface of java.util.function.Consumer, currently Java 8 & Android API 24+ only,
 * in order to allow feature usage on Android API <24.
 */
@FunctionalInterface
interface Consumer<T> {
    void accept(T var1);
}