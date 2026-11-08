import org.jspecify.annotations.NullMarked;

@NullMarked
module fr.fidorial.bootstrap {
    exports fr.euphyllia.fidorial.bootstrap;

    requires java.net.http;

    requires static org.jspecify;
}
