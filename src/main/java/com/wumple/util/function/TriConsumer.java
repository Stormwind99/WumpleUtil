package com.wumple.util.function;

import java.util.Objects;

// from https://github.com/ddickstein/Java-Library/blob/master/java8/function/TriConsumer.java
@FunctionalInterface
public interface TriConsumer<T, U, V>
{
    public void accept(T t, U u, V v);

    public default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after)
    {
        Objects.requireNonNull(after);
        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}