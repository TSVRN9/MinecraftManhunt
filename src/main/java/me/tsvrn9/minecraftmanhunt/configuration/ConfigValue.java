package me.tsvrn9.minecraftmanhunt.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    String path();
    Class<? extends Predicate<Object>> validator() default NoValidator.class;
    Class<? extends UnaryOperator<Object>> processor() default NoProcessor.class;
}