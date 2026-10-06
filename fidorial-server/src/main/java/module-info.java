import org.jspecify.annotations.NullMarked;

@NullMarked
module fr.fidorial.server {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.google.common;
    requires com.google.errorprone.annotations;
    requires com.google.gson;
    requires dev.faststats.config;
    requires dev.faststats;
    requires fr.fidorial.auth;
    requires fr.fidorial;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.transport.classes.epoll;
    requires io.netty.transport.classes.io_uring;
    requires io.netty.transport.classes.kqueue;
    requires io.netty.transport.unix.common;
    requires io.netty.transport;
    requires java.management;
    requires net.kyori.adventure.api;
    requires net.kyori.adventure.key;
    requires net.kyori.adventure.text.logger.slf4j;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.adventure.text.serializer.ansi;
    requires net.kyori.adventure.text.serializer.gson;
    requires net.kyori.adventure.text.serializer.plain;
    requires org.jline.reader;
    requires org.jline.terminal;
    requires org.slf4j;

    requires static org.jetbrains.annotations;
    requires static org.jspecify;
    requires net.kyori.adventure.nbt;
    requires io.github.classgraph;
    requires it.unimi.dsi.fastutil;
    requires io.papermc.adventurex.nbt.dfu;
    requires java.logging;
    requires java.instrument;
    requires org.lz4.java;
    requires com.github.luben.zstd_jni;
    requires fr.fidorial.bootstrap;

    opens fr.euphyllia.fidorial.server.tests to fr.fidorial;
}
