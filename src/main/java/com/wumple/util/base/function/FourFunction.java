package com.wumple.util.base.function;

import java.util.Objects;
import java.util.function.Function;

//inspired by https://github.com/ddickstein/Java-Library/blob/master/java8/function/TriConsumer.java
@FunctionalInterface
public interface FourFunction<A, B, C, D, R>
{
	public R apply(A a, B b, C c, D d);

	default <V> FourFunction<A, B, C, D, V> andThen(Function<? super R, ? extends V> after)
	{
		Objects.requireNonNull(after);
		return (A a, B b, C c, D d) -> after.apply(apply(a, b, c, d));
	}
}