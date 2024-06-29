package me.tsvrn9.minecraftmanhunt.configuration;

public class NoValidator implements Validator {
    @Override
    public boolean validate(Object o) {
        return true;
    }
}
