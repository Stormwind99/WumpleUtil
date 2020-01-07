package com.wumple.util.base.function;

import java.util.Objects;
import java.util.function.Function;

// inspired by https://github.com/ddickstein/Java-Library/blob/master/java8/function/TriConsumer.java
@FunctionalInterface
public interface SixFunction<A, B, C, D, E, F, R>
{
	public R apply(A a, B b, C c, D d, E e, F f);
	
    default <V> SixFunction<A, B, C, D, E, F, V> andThen(Function<? super R, ? extends V> after)
    {
        Objects.requireNonNull(after);
        return (A a, B b, C c, D d, E e, F f) -> after.apply(apply(a, b, c, d, e ,f));
    }
}