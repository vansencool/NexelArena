package net.vansen.nexelarena.corners.entry;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PositionEntry {
    private final Location first;
    private final Location second;

    public PositionEntry(@Nullable Location first, @Nullable Location second) {
        this.first = first;
        this.second = second;
    }

    public Optional<Location> first() {
        return Optional.ofNullable(first);
    }

    public Optional<Location> second() {
        return Optional.ofNullable(second);
    }
}
