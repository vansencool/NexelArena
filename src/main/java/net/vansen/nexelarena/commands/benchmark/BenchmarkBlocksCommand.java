package net.vansen.nexelarena.commands.benchmark;

import dev.vansen.commandutils.CommandUtils;
import dev.vansen.commandutils.argument.CommandArgument;
import dev.vansen.commandutils.command.CommandWrapper;
import dev.vansen.commandutils.permission.CommandPermission;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.modification.NexelLevel;
import net.vansen.nexelarena.modification.update.utility.RegionUtils;
import net.vansen.nexelarena.utils.NumberFormatter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class BenchmarkBlocksCommand {

    public static void register() {
        CommandUtils.command("benchmarkblocks")
                .permission(CommandPermission.OP)
                .argument(CommandArgument.integer("width", 1, 10000)
                        .argument(CommandArgument.integer("height", 1, 10000)
                                .playerExecute(BenchmarkBlocksCommand::execute)
                                .argument(CommandArgument.integer("runs", 1, 10000)
                                        .playerExecute(BenchmarkBlocksCommand::execute)
                                        .argument(CommandArgument.integer("delay_between_runs", 0, 10000)
                                                .playerExecute(BenchmarkBlocksCommand::execute)
                                                .argument(CommandArgument.string("what_to_benchmark")
                                                        .completion((context, wrapper) -> {
                                                            wrapper.suggestIfValueStartsWithCurrent("bukkit", "nexel_arena", "both");
                                                            return wrapper.build();
                                                        })
                                                        .playerExecute(BenchmarkBlocksCommand::execute))))))
                .register();
    }

    private static void execute(@NotNull CommandWrapper context) {
        int width = context.argInt("width");
        int height = context.argInt("height");
        int runs = context.argInt("runs", 5);
        int delay = context.argInt("delay_between_runs", 3);
        String whatToBenchmark = context.argString("what_to_benchmark", "both").toUpperCase();

        try {
            WhatToBenchmark benchmark = WhatToBenchmark.valueOf(whatToBenchmark);
            Location origin = context.location();
            World world = origin.getWorld();
            Material material = Material.STONE;

            int totalBlocks = width * height * width;
            context.response("<#c2d8ff>Running benchmark... " + NumberFormatter.format(totalBlocks) + " blocks per method, " + runs + " runs each");

            CompletableFuture<Void> benchmarkFuture = CompletableFuture.completedFuture(null);

            if (benchmark == WhatToBenchmark.BUKKIT || benchmark == WhatToBenchmark.BOTH) {
                benchmarkFuture = CompletableFuture.runAsync(() -> {
                    context.response("");
                    context.response("<#c2d8ff>Bukkit Set started...");
                    long defaultTime = benchmark(context, () -> runBukkitSet(world, origin, width, height, width, material), runs, world, origin, width, height, width, delay);
                    context.response("<#c2d8ff>Bukkit Set complete!");
                    context.response("<#c2d8ff>Bukkit Set: " + formatResults(defaultTime, totalBlocks));
                });
            }

            if (benchmark == WhatToBenchmark.NEXEL_ARENA || benchmark == WhatToBenchmark.BOTH) {
                benchmarkFuture = benchmarkFuture.thenRunAsync(() -> {
                    context.response("");
                    context.response("<#c2d8ff>NexelArena Set started...");
                    long nexelTime = benchmark(context, () -> nexelArena(world, origin, width, height, width), runs, world, origin, width, height, width, delay);
                    context.response("<#c2d8ff>NexelArena Set complete!");
                    context.response("<#c2d8ff>NexelArena Set Results: " + formatResults(nexelTime, totalBlocks));
                });
            }

            // TODO: add tick based benchmarking

            benchmarkFuture.exceptionally(ex -> {
                context.response("<#ff5286>An error occurred during benchmarking: " + ex.getMessage());
                return null;
            });
        } catch (IllegalArgumentException e) {
            context.response("Invalid benchmark type. Use 'bukkit', 'nexel_arena', or 'both'.");
        }
    }

    private static long benchmark(CommandWrapper context, TimeRunnable method, int runs, World world, Location origin, int width, int height, int depth, int delay) {
        long totalTime = 0;
        context.response("");
        for (int i = 0; i < runs; i++) {
            long time = method.run();
            context.response("<#c2d8ff>Run " + (i + 1) + ": " + formatResults(time, width * height * depth));
            totalTime += time;
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            revertAir(world, origin, width, height, depth);
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        context.response("");
        return totalTime / runs;
    }

    private static String formatResults(long timeNanos, int totalBlocks) {
        double timeMillis = timeNanos / 1_000_000.0;
        double blocksPerSec = (totalBlocks / (timeMillis / 1000));
        return String.format("%.2f ms (%.2fM blocks/sec)", timeMillis, blocksPerSec / 1_000_000);
    }

    private static long runBukkitSet(World world, Location origin, int width, int height, int depth, Material material) {
        long start = System.nanoTime();
        CompletableFuture.runAsync(() -> {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < depth; z++) {
                        world.getBlockAt(origin.clone().add(x, y, z)).setType(material, false);
                    }
                }
            }
        }, NexelArena.instance().getServer().getScheduler().getMainThreadExecutor(NexelArena.instance())).join();
        long end = System.nanoTime();
        return end - start;
    }

    private static long nexelArena(World world, Location origin, int width, int height, int depth) {
        NexelLevel level = new NexelLevel(world).clearAfterApply(false).updates(RegionUtils.setChunkState(
                new Location(world, origin.getX(), origin.getY(), origin.getZ()),
                new Location(world, origin.getX() + width - 1, origin.getY() + height - 1, origin.getZ() + depth - 1),
                Material.STONE.createBlockData().createBlockState()));


        CompletableFuture<Long> future = new CompletableFuture<>();
        long startTime = System.nanoTime();
        level.blockCallback(callback -> {
            long endTime = System.nanoTime();
            future.complete(endTime - startTime);
        });

        level.applyPendingBlockUpdates();

        try {
            return future.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while waiting for NexelArena callback", e);
        }
    }

    private static void revertAir(World world, Location origin, int width, int height, int depth) {
        NexelLevel level = new NexelLevel(world).updates(RegionUtils.setChunkState(
                new Location(world, origin.getX(), origin.getY(), origin.getZ()),
                new Location(world, origin.getX() + width - 1, origin.getY() + height - 1, origin.getZ() + depth - 1),
                Material.AIR.createBlockData().createBlockState()));
        CompletableFuture<Void> future = new CompletableFuture<>();
        level.callback(callback -> future.complete(null));
        level.applyPendingBlockUpdates();
        try {
            future.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error while waiting for revertAir callback", e);
        }
    }

    public enum WhatToBenchmark {
        BUKKIT,
        NEXEL_ARENA,
        BOTH
    }

    public interface TimeRunnable {
        long run();
    }
}
