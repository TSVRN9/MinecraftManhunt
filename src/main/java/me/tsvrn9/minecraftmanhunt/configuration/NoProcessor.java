package me.tsvrn9.minecraftmanhunt.configuration;

import java.util.function.UnaryOperator;

public class NoProcessor implements UnaryOperator<Object> {
    @Override
    public Object apply(Object o) {
        return o;
    }
}
