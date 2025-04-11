package net.vansen.nexelarena.modification;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.collections.BlockMap;
import net.vansen.nexelarena.collections.impl.FastBlockMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This class is used to batch block updates in a very fast (low level) way.
 * <p>
 * THIS CLASS IS NOT THREAD SAFE! you can run this async of course, but you cannot run the same instance in multiple threads at the same time.
 */
@SuppressWarnings("unused")
public class NexelLevel {

    private final BlockMap pendingBlockUpdates;
    private final Set<Long> updatedChunks = new TreeSet<>();
    private Consumer<Integer> callback = ignored -> {
        // do nothing
    };
    private final World world;

    /**
     * Creates a new NexelLevel instance.
     *
     * @param world The world to use.
     */
    public NexelLevel(@NotNull World world) {
        this.world = world;
        this.pendingBlockUpdates = new FastBlockMap();
    }

    /**
     * Creates a new NexelLevel instance with a custom BlockMap.
     *
     * @param world               The world to use.
     * @param pendingBlockUpdates The BlockMap to use for pending block updates.
     */
    public NexelLevel(@NotNull World world, @NotNull BlockMap pendingBlockUpdates) {
        this.world = world;
        this.pendingBlockUpdates = pendingBlockUpdates;
    }

    /**
     * Sets a block at the given position without updating.
     * This method is used to batch block updates for performance reasons and should not be used for individual block updates!
     *
     * @param pos   The position of the block to set.
     * @param state The block state to set.
     */
    public void block(@NotNull BlockPos pos, @NotNull BlockState state) {
        pendingBlockUpdates.put(pos.getX(), pos.getY(), pos.getZ(), state);
        boolean bool = updatedChunks.add(Chunk.getChunkKey(pos.getX() >> 4, pos.getZ() >> 4));
        if (bool) {
            world.addPluginChunkTicket(pos.getX() >> 4, pos.getZ() >> 4, NexelArena.instance());
        }
    }

    /**
     * Applies the pending block updates to the world, it is async and will not block the main thread.
     *
     * @return The number of blocks that are going to get updated.
     */
    @SuppressWarnings("all")
    public int applyPendingBlockUpdates() {
        int updatedCount = pendingBlockUpdates.size();
        CompletableFuture.runAsync(() -> {
            ConcurrentHashMap<Long, ChunkAccess> chunkCache = new ConcurrentHashMap<>();

            AtomicInteger did = new AtomicInteger();
            pendingBlockUpdates.forEach((x, y, z, state) -> {
                long chunkKey = Chunk.getChunkKey(x >> 4, z >> 4);
                ChunkAccess chunk = chunkCache.computeIfAbsent(chunkKey, key -> ((CraftWorld) world).getHandle().getChunk(x >> 4, z >> 4));

                try {
                    int sectionIndex = chunk.getSectionIndex(y);
                    LevelChunkSection section = chunk.getSection(sectionIndex);

                    if (section != null) {
                        int localX = x & 15;
                        int localY = y & 15;
                        int localZ = z & 15;
                        section.setBlockState(localX, localY, localZ, state, false);
                    }
                } catch (Exception e) {
                    // don't care
                }
            });
            if (callback != null) {
                callback.accept(updatedCount);
            }

            CompletableFuture.runAsync(() -> {
                for (long chunkKey : updatedChunks) {
                    int chunkX = (int) chunkKey;
                    int chunkZ = (int) (chunkKey >> 32);
                    world.refreshChunk(chunkX, chunkZ);
                    world.removePluginChunkTicket(chunkX, chunkZ, NexelArena.instance());
                }
                ((CraftWorld) world).getHandle().getLightEngine().starlight$serverRelightChunks(updatedChunks.stream()
                        .map(chunkKey -> new ChunkPos((int) (chunkKey & 0xFFFFFFFFL), (int) (chunkKey >> 32)))
                        .toList(), null, null);
                pendingBlockUpdates.clear();
                updatedChunks.clear();
            }, Bukkit.getScheduler().getMainThreadExecutor(net.vansen.nexelarena.NexelArena.instance()));
        });
        return updatedCount;
    }

    /**
     * Sets a callback to be called when the block updates are applied.
     *
     * @param callback The callback to be called when the block updates are applied.
     */
    public void callback(@NotNull Consumer<Integer> callback) {
        this.callback = callback;
    }
}
