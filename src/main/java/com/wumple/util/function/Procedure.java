package com.wumple.util.function;

// from https://stackoverflow.com/questions/23868733/java-8-functional-interface-with-no-arguments-and-no-return-value
@FunctionalInterface
public interface Procedure
{
    void run();

    default Procedure andThen(Procedure after)
    {
        return () -> {
            this.run();
            after.run();
        };
    }

    default Procedure compose(Procedure before)
    {
        return () -> {
            before.run();
            this.run();
        };
    }
}