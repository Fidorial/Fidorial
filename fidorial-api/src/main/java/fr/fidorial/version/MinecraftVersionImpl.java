package fr.fidorial.version;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

record MinecraftVersionImpl(int major, int minor, int patch, Stage stage, int iteration) implements MinecraftVersion {

    private static final Pattern PATTERN = Pattern.compile(
            "(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?(?:-(?<stage>%s)-(?<iteration>\\d+))?".formatted(
                    Arrays.stream(Stage.values())
                            .filter(stage -> stage != Stage.RELEASE)
                            .map(stage -> stage.id)
                            .collect(Collectors.joining("|"))));

    private static final Comparator<MinecraftVersion> ORDER = Comparator.comparingInt(MinecraftVersion::major)
            .thenComparingInt(MinecraftVersion::minor)
            .thenComparingInt(MinecraftVersion::patch)
            .thenComparing(MinecraftVersion::stage)
            .thenComparingInt(MinecraftVersion::iteration);

    MinecraftVersionImpl {
        checkArgument(stage != null, "Stage cannot be null");
        checkArgument((major | minor | patch | iteration) >= 0, "Negative number in %s.%s.%s iteration %s", major, minor, patch, iteration);
        checkArgument((stage == Stage.RELEASE) == (iteration == 0), "Stage %s cannot have iteration %s", stage, iteration);
    }

    static MinecraftVersion parse(final String id) {
        final Matcher matcher = PATTERN.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a Minecraft version id: " + id);
        }
        return new MinecraftVersionImpl(
                number(matcher.group("major")),
                number(matcher.group("minor")),
                number(matcher.group("patch")),
                stageOf(matcher.group("stage")),
                number(matcher.group("iteration")));
    }

    @Override
    public String id() {
        return format("-", stage.id);
    }

    @Override
    public String name() {
        return format(" ", stage.displayName);
    }

    @Override
    public int compareTo(final MinecraftVersion other) {
        return ORDER.compare(this, other);
    }

    private String format(final String separator, final String label) {
        final String base = patch == 0
                ? "%d.%d".formatted(major, minor)
                : "%d.%d.%d".formatted(major, minor, patch);
        return stage == Stage.RELEASE ? base : String.join(separator, base, label, Integer.toString(iteration));
    }

    private static int number(final @Nullable String group) {
        return group == null ? 0 : Integer.parseInt(group);
    }

    private static Stage stageOf(final @Nullable String id) {
        if (id == null) {
            return Stage.RELEASE;
        }

        return Arrays.stream(Stage.values())
                .filter(stage -> stage.id.equals(id))
                .findFirst()
                .orElseThrow();
    }
}
