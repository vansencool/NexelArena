package net.vansen.nexelarena.schematic.cache;

import net.vansen.nexelarena.config.Variables;
import net.vansen.nexelarena.schematic.Schematic;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to cache schematics in memory.
 */
@SuppressWarnings("unused")
public class SchematicCache {

    /**
     * Cached schematics.
     */
    private final Map<String, Schematic> cache = new ConcurrentHashMap<>();

    /**
     * Gets a schematic from the cache.
     *
     * @param file The file to get the schematic from.
     * @return The schematic, or null if not found.
     */
    public Schematic get(@NotNull File file) {
        if (!Variables.ENABLE_SCHEMATIC_CACHE) return null;
        return cache.get(file.getAbsolutePath());
    }

    /**
     * Puts a schematic in the cache.
     *
     * @param file      The file that represents the schematic.
     * @param schematic The schematic to put in the cache.
     */
    public void put(@NotNull File file, @NotNull Schematic schematic) {
        if (!Variables.ENABLE_SCHEMATIC_CACHE) return;
        cache.put(file.getAbsolutePath(), schematic);
    }

    /**
     * Checks if the cache contains a schematic.
     *
     * @param file The file to check.
     * @return True if the cache contains the schematic, false otherwise.
     */
    public boolean contains(@NotNull File file) {
        if (!Variables.ENABLE_SCHEMATIC_CACHE) return false;
        return cache.containsKey(file.getAbsolutePath());
    }

    /**
     * Removes a schematic from the cache.
     *
     * @param file The file to remove.
     */
    public void remove(@NotNull File file) {
        if (!Variables.ENABLE_SCHEMATIC_CACHE) return;
        cache.remove(file.getAbsolutePath());
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }
}
