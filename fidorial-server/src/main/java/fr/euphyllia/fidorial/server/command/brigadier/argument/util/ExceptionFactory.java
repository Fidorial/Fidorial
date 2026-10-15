package fr.euphyllia.fidorial.server.command.brigadier.argument.util;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;

public final class ExceptionFactory {

    private ExceptionFactory() {
    }

    public static SimpleCommandExceptionType simple(final String translationKey) {
        return new SimpleCommandExceptionType(Message.component(Component.translatable(translationKey)));
    }

    public static DynamicCommandExceptionType dynamic(final String translationKey) {
        return new DynamicCommandExceptionType(value -> Message.component(
                Component.translatable(translationKey, Component.text(String.valueOf(value)))));
    }

    public static Dynamic2CommandExceptionType dynamic2(final String translationKey) {
        return new Dynamic2CommandExceptionType((first, second) -> Message.component(
                Component.translatable(translationKey, Component.text(String.valueOf(first)), Component.text(String.valueOf(second)))));
    }

    public static Dynamic2CommandExceptionType dynamic2Reversed(final String translationKey) {
        return new Dynamic2CommandExceptionType((first, second) -> Message.component(
                Component.translatable(translationKey, Component.text(String.valueOf(second)), Component.text(String.valueOf(first)))));
    }
}
