package net.vansen.nexelarena.corners;

import net.vansen.nexelarena.corners.entry.PositionEntry;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to manage the positions of the arenas.
 * <p>
 * It stores the positions in a map, where the key is the name of the arena and the value is the position entry.
 */
public class PositionManager {
    private static final Map<String, PositionEntry> positions = new HashMap<>();

    /**
     * Sets the positions of the arena.
     *
     * @param name   the name of the arena
     * @param first  the first position
     * @param second the second position
     */
    public static void set(@NotNull String name, @Nullable Location first, @Nullable Location second) {
        positions.put(name, new PositionEntry(first, second));
    }

    /**
     * Gets the positions of the arena.
     *
     * @param name the name of the arena
     * @return the position entry
     */
    public static PositionEntry get(@NotNull String name) {
        return positions.getOrDefault(name, new PositionEntry(null, null));
    }
}
