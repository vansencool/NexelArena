package net.vansen.nexelarena.modification;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.config.Variables;
import net.vansen.nexelarena.modification.update.BlockUpdate;
import net.vansen.nexelarena.modification.update.ChunkUpdates;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class is used to batch chunk (blocks) updates in a very fast, async way.
 */
@SuppressWarnings("unused")
public class NexelLevel {

    private List<ChunkUpdates> updates = new ArrayList<>();
    private Consumer<Integer> callback = ignored -> {
        // do nothing
    };
    private final World world;
    private Schematic schematic;
    private boolean clearAfterApply = true;
    private final Object lock = new Object();
    private final int maxHeight;
    private final int minHeight;

    /**
     * Creates a new NexelLevel instance.
     *
     * @param world The world to use.
     */
    public NexelLevel(@NotNull World world) {
        this.world = world;
        maxHeight = world.getMaxHeight();
        minHeight = world.getMinHeight();
    }

    /**
     * Applies the pending block updates to the world, it is async and will not block the main thread.
     *
     * @return The number of blocks that are going to get updated.
     */
    @SuppressWarnings("all")
    public int applyPendingBlockUpdates() {
        int total = 0;
        for (ChunkUpdates chunkUpdates : updates) {
            total += chunkUpdates.updates.size();
        }
        final int totalUpdates = total;
        if (totalUpdates == 0) {
            return 0;
        }

        CompletableFuture.runAsync(() -> {
            synchronized (lock) {
                for (ChunkUpdates chunkUpdates : updates) {
                    ChunkAccess chunk = ((CraftWorld) world).getHandle().getChunk(chunkUpdates.chunkX, chunkUpdates.chunkZ);
                    for (BlockUpdate update : chunkUpdates.updates) {
                        int y = update.y;
                        if (y > maxHeight) {
                            warnHeight("max", maxHeight, y, chunkUpdates);
                            return;
                        }
                        if (y < minHeight) {
                            warnHeight("min", minHeight, y, chunkUpdates);
                            return;
                        }
                        chunk.getSection(chunk.getSectionIndex(y))
                                .setBlockState(update.x & 15, y & 15, update.z & 15, update.state, false);
                    }
                }

                if (Variables.REFRESH_CHUNKS_ASYNC) afterwards(totalUpdates);
                else
                    CompletableFuture.runAsync(() -> afterwards(totalUpdates), Bukkit.getScheduler().getMainThreadExecutor(NexelArena.instance()));
            }
        });

        return totalUpdates;
    }

    private void afterwards(int totalUpdates) {
        synchronized (lock) {
            for (ChunkUpdates chunkUpdates : updates) {
                world.refreshChunk(chunkUpdates.chunkX, chunkUpdates.chunkZ);
                if (clearAfterApply && Variables.ADD_CHUNKS_TO_FORCE_LOAD)
                    world.removePluginChunkTicket(chunkUpdates.chunkX, chunkUpdates.chunkZ, NexelArena.instance());
            }

            List<ChunkPos> chunkPositions = new ArrayList<>();
            for (ChunkUpdates chunkUpdates : updates) {
                chunkPositions.add(new ChunkPos(chunkUpdates.chunkX, chunkUpdates.chunkZ));
            }
            ((CraftWorld) world).getHandle().getLightEngine().starlight$serverRelightChunks(chunkPositions, null, null);
            if (clearAfterApply) updates.clear();
            if (callback != null) callback.accept(totalUpdates);
        }
    }

    private void warnHeight(@NotNull String height, int heightShouldBe, int y, @NotNull ChunkUpdates chunkUpdates) {
        NexelArena.instance()
                .getSLF4JLogger()
                .warn("Tried to set block at y: {} in chunk: {}, {} but the {} height is: {}!", y, chunkUpdates.chunkX, chunkUpdates.chunkZ, height, heightShouldBe);
        NexelArena.instance()
                .getSLF4JLogger()
                .warn("Please try to minimize air blocks in your schematics! it will be much faster, less memory consumption, and less storage usage!");
    }

    /**
     * Sets a callback to be called when the block updates are applied.
     * <p>
     * Note, this callback might be called on a different thread, or the main thread, depending on the config.
     *
     * @param callback The callback to be called when the block updates are applied.
     */
    public void callback(@NotNull Consumer<Integer> callback) {
        this.callback = callback;
    }

    /**
     * Sets the block updates to be applied.
     *
     * @param updates The block updates to be applied.
     * @return The current instance of NexelLevel.
     */
    public NexelLevel updates(@NotNull List<ChunkUpdates> updates) {
        this.updates = updates;
        return this;
    }

    /**
     * If true, the chunk updates will be cleared after applying the block updates.
     *
     * @param clearAfterApply If true, the chunk updates will be cleared after applying the block updates.
     * @return The current instance of NexelLevel.
     */
    public NexelLevel clearAfterApply(boolean clearAfterApply) {
        this.clearAfterApply = clearAfterApply;
        return this;
    }
}
