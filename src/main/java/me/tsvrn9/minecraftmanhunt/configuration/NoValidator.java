package me.tsvrn9.minecraftmanhunt.configuration;

import java.util.function.Predicate;

public class NoValidator implements Predicate<Object> {
    @Override
    public boolean test(Object o) {
        return true;
    }
}
