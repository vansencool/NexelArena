package net.vansen.nexelarena.schematic;

import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.config.Variables;
import net.vansen.nexelarena.modification.NexelLevel;
import net.vansen.nexelarena.modification.update.BlockUpdate;
import net.vansen.nexelarena.modification.update.ChunkUpdates;
import net.vansen.nexelarena.modification.update.SectionUpdate;
import net.vansen.nexelarena.schematic.cache.SchematicCache;
import net.vansen.nexelarena.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to save and load schematics, basically a snapshot of a region.
 */
@SuppressWarnings("unused")
public class Schematic {

    public static final String HEADER = "ZEAx9104817V2";
    private static final SchematicCache cache = new SchematicCache();

    private final List<ChunkUpdates> updates = new ArrayList<>();
    public int originX, originY, originZ;
    private int sizeX, sizeY, sizeZ;
    private World world;
    private NexelLevel level;

    public Schematic() {
    }

    /**
     * Saves a region to a schematic file.
     *
     * @param player The player who initiated the save.
     * @param world  The world to save the region from.
     * @param pos1   The first corner of the region.
     * @param pos2   The second corner of the region.
     * @param file   The file to save the schematic to.
     */
    public void saveRegion(@NotNull Player player, @NotNull World world, Location pos1, Location pos2, File file) {
        CompletableFuture.runAsync(() -> {
            this.world = world;
            player.sendRichMessage("<#8336ff>Getting blocks...");
            long start = System.nanoTime();
            originX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            originY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            originZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

            sizeX = maxX - originX + 1;
            sizeY = maxY - originY + 1;
            sizeZ = maxZ - originZ + 1;

            int chunkMinX = originX >> 4;
            int chunkMaxX = maxX >> 4;
            int chunkMinZ = originZ >> 4;
            int chunkMaxZ = maxZ >> 4;

            int minHeight = world.getMinHeight();
            int minSection = minHeight >> 4;

            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    var chunk = ((CraftWorld) world).getHandle().getChunk(chunkX, chunkZ);
                    ChunkUpdates chunkUpdates = new ChunkUpdates(chunkX, chunkZ);

                    for (int x = Math.max(originX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                        for (int y = originY; y <= maxY; y++) {
                            int global = y >> 4;
                            int sectionIndex = global - minSection;

                            for (int z = Math.max(originZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                                chunkUpdates.addBlockUpdate(new BlockUpdate(x, y, z, chunk.getBlockStateFinal(x, y, z)), sectionIndex);
                            }
                        }
                    }

                    if (!chunkUpdates.getSectionUpdates().isEmpty()) {
                        updates.add(chunkUpdates);
                        world.addPluginChunkTicket(chunkX, chunkZ, NexelArena.instance());
                    }
                }
            }

            long end = System.nanoTime();
            player.sendRichMessage("<#8336ff>Done getting blocks (took: " + (end - start) / 1000000 + " ms, total: " +
                    NumberFormatter.format(updates.stream().mapToInt(ChunkUpdates::getBlockUpdateCount).sum()) + "), now saving...");
            long start2 = System.nanoTime();
            save(file).thenRun(() -> {
                long end2 = System.nanoTime();
                player.sendRichMessage("");
                player.sendRichMessage("<#8336ff>Done saving schematic (took: " + (end2 - start2) / 1000000 + " ms)");
            });
            this.level = new NexelLevel(world).clearAfterApply(!Variables.ENABLE_SCHEMATIC_CACHE).updates(updates);
        });
    }

    /**
     * Saves the schematic to the given file.
     *
     * @param file The file to save the schematic to.
     * @return A CompletableFuture.
     */
    public CompletableFuture<Void> save(@NotNull File file) {
        return CompletableFuture.runAsync(() -> {
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                dos.writeUTF(HEADER);

                dos.writeUTF(world.getName());
                dos.writeInt(originX);
                dos.writeInt(originY);
                dos.writeInt(originZ);
                dos.writeInt(sizeX);
                dos.writeInt(sizeY);
                dos.writeInt(sizeZ);

                Map<BlockState, Integer> indexes = new HashMap<>();
                List<String> states = new ArrayList<>();
                int index = 1;

                for (ChunkUpdates chunkUpdates : updates) {
                    for (SectionUpdate sectionUpdate : chunkUpdates.getSectionUpdates()) {
                        for (BlockUpdate update : sectionUpdate.updates) {
                            if (update.state == null || indexes.containsKey(update.state)) continue;
                            indexes.put(update.state, index++);
                            states.add(update.state.toString());
                        }
                    }
                }

                dos.writeInt(states.size());
                for (String s : states) {
                    dos.writeUTF(s);
                }

                int totalBlocks = sizeX * sizeY * sizeZ;
                int[] flatData = new int[totalBlocks];
                Arrays.fill(flatData, 0);

                for (ChunkUpdates chunkUpdates : updates) {
                    for (SectionUpdate sectionUpdate : chunkUpdates.getSectionUpdates()) {
                        for (BlockUpdate update : sectionUpdate.updates) {
                            int relX = update.x - originX;
                            int relY = update.y - originY;
                            int relZ = update.z - originZ;

                            if (relX < 0 || relX >= sizeX || relY < 0 || relY >= sizeY || relZ < 0 || relZ >= sizeZ) {
                                continue;
                            }

                            int flatIndex = (relY * sizeX * sizeZ) + (relX * sizeZ) + relZ;
                            flatData[flatIndex] = update.state == null ? 0 : indexes.get(update.state);
                        }
                    }
                }

                int i = 0;
                while (i < totalBlocks) {
                    int value = flatData[i];
                    int runLength = 1;

                    while (i + runLength < totalBlocks && flatData[i + runLength] == value && runLength < 0xFFFF) {
                        runLength++;
                    }

                    dos.writeInt(value);
                    dos.writeShort(runLength);
                    i += runLength;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Loads a schematic from the given file.
     *
     * @param file The file to load the schematic from.
     * @return The loaded schematic.
     * @throws IOException If an I/O error occurs.
     */
    public static Schematic load(@NotNull File file) throws IOException {
        Schematic cached = cache.get(file);
        if (cached != null) return cached;

        Schematic schematic = new Schematic();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            String header;
            try {
                header = dis.readUTF();
            } catch (Exception e) {
                throw new IOException("Invalid schematic file: " + file.getAbsolutePath(), e);
            }
            if (!header.equals(HEADER)) {
                throw new IOException("Invalid schematic file: " + file.getAbsolutePath() + " (found header: " + header + ")");
            }

            String worldName = dis.readUTF();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IOException("World not found: " + worldName);
            }
            schematic.world = world;
            schematic.originX = dis.readInt();
            schematic.originY = dis.readInt();
            schematic.originZ = dis.readInt();
            schematic.sizeX = dis.readInt();
            schematic.sizeY = dis.readInt();
            schematic.sizeZ = dis.readInt();

            int count = dis.readInt();
            List<BlockState> states = new ArrayList<>();
            states.add(null);
            for (int i = 0; i < count; i++) {
                String serialized = dis.readUTF();
                BlockState state = ((CraftBlockData) Bukkit.createBlockData(serialized)).getState();
                states.add(state);
            }

            int totalBlocks = schematic.sizeX * schematic.sizeY * schematic.sizeZ;
            int[] flatData = new int[totalBlocks];
            Arrays.fill(flatData, 0);

            int i = 0;
            while (i < totalBlocks) {
                int value = dis.readInt();
                int runLength = dis.readUnsignedShort();

                for (int j = 0; j < runLength && i + j < totalBlocks; j++) {
                    flatData[i + j] = value;
                }
                i += runLength;
            }

            int minChunkX = schematic.originX >> 4;
            int maxChunkX = (schematic.originX + schematic.sizeX - 1) >> 4;
            int minChunkZ = schematic.originZ >> 4;
            int maxChunkZ = (schematic.originZ + schematic.sizeZ - 1) >> 4;

            int minHeight = schematic.world.getMinHeight();
            int minSection = minHeight >> 4;

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    ChunkUpdates chunkUpdates = new ChunkUpdates(chunkX, chunkZ);

                    int startX = Math.max(schematic.originX, chunkX << 4);
                    int endX = Math.min(schematic.originX + schematic.sizeX - 1, (chunkX << 4) + 15);
                    int startZ = Math.max(schematic.originZ, chunkZ << 4);
                    int endZ = Math.min(schematic.originZ + schematic.sizeZ - 1, (chunkZ << 4) + 15);

                    for (int x = startX; x <= endX; x++) {
                        for (int y = schematic.originY; y < schematic.originY + schematic.sizeY; y++) {
                            int global = y >> 4;
                            int sectionIndex = global - minSection;

                            for (int z = startZ; z <= endZ; z++) {
                                int relX = x - schematic.originX;
                                int relY = y - schematic.originY;
                                int relZ = z - schematic.originZ;

                                if (relX < 0 || relX >= schematic.sizeX ||
                                        relY < 0 || relY >= schematic.sizeY ||
                                        relZ < 0 || relZ >= schematic.sizeZ) {
                                    continue;
                                }

                                int flatIndex = (relY * schematic.sizeX * schematic.sizeZ) + (relX * schematic.sizeZ) + relZ;
                                if (flatIndex < 0 || flatIndex >= flatData.length) {
                                    continue;
                                }

                                int stateIndex = flatData[flatIndex];
                                if (stateIndex > 0 && stateIndex < states.size()) {
                                    chunkUpdates.addBlockUpdate(new BlockUpdate(x, y, z, states.get(stateIndex)), sectionIndex);
                                }
                            }
                        }
                    }

                    if (!chunkUpdates.getSectionUpdates().isEmpty()) {
                        schematic.updates.add(chunkUpdates);
                    }
                }
            }

            schematic.level = new NexelLevel(schematic.world).clearAfterApply(!Variables.ENABLE_SCHEMATIC_CACHE).updates(schematic.updates);
            cache.put(file, schematic);
        }

        return schematic;
    }

    /**
     * The nexel level of the schematic, you can directly use apply blocks on it.
     *
     * @return The nexel level of the schematic.
     */
    public NexelLevel asLevel() {
        return level;
    }

    /**
     * The cache of schematics.
     *
     * @return The schematic cache.
     */
    public static SchematicCache cache() {
        return cache;
    }
}