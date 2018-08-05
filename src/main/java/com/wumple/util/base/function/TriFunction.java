package com.wumple.util.base.function;

import java.util.Objects;
import java.util.function.Function;

// from https://github.com/ddickstein/Java-Library/blob/master/java8/function/TriConsumer.java
@FunctionalInterface
interface TriFunction<A, B, C, R>
{
    R apply(A a, B b, C c);

    default <V> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after)
    {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }
}
