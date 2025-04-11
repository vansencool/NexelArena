package net.vansen.nexelarena.modification;

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
        return cache.get(file.getAbsolutePath());
    }

    /**
     * Puts a schematic in the cache.
     *
     * @param file      The file that represents the schematic.
     * @param schematic The schematic to put in the cache.
     */
    public void put(@NotNull File file, @NotNull Schematic schematic) {
        cache.put(file.getAbsolutePath(), schematic);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }
}
