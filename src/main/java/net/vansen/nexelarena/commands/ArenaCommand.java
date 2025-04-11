package net.vansen.nexelarena.commands;

import dev.vansen.commandutils.CommandUtils;
import dev.vansen.commandutils.argument.CommandArgument;
import dev.vansen.commandutils.command.CheckType;
import dev.vansen.commandutils.permission.CommandPermission;
import dev.vansen.commandutils.subcommand.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.corners.PositionManager;
import net.vansen.nexelarena.corners.entry.PositionEntry;
import net.vansen.nexelarena.modification.Schematic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ArenaCommand {

    public static void register() {
        CommandUtils.command("arena")
                .permission(CommandPermission.permission("wellarenas.command"))
                .subCommand(SubCommand.of("save")
                        .argument(CommandArgument.string("arena")
                                .defaultExecute(context -> {
                                    context.check(CheckType.PLAYER);
                                    PositionEntry entry = PositionManager.get(context.playerName());
                                    File file = new File(NexelArena.instance().getDataFolder(), "arenas/" + context.arg("arena", String.class) + ".zea");

                                    if (Files.exists(file.toPath())) {
                                        context.response(Component.text("Schematic file already exists! Click here if you want to forcefully overwrite the current file", TextColor.fromHexString("#ff577e"))
                                                .clickEvent(ClickEvent.runCommand("arena save " + context.arg("arena", String.class) + " force")));
                                        return;
                                    }

                                    if (entry.second().isEmpty() || entry.second().isEmpty()) {
                                        context.response("<#ff577e>pos1 or pos2 is not set!");
                                        return;
                                    }

                                    Schematic schematic = new Schematic();
                                    schematic.saveRegion(context.player(), context.world(), entry.first().orElseThrow(), entry.second().orElseThrow(), file);
                                })
                                .subCommand(SubCommand.of("force")
                                        .defaultExecute(context -> {
                                            context.check(CheckType.PLAYER);
                                            PositionEntry entry = PositionManager.get(context.playerName());

                                            if (entry.second().isEmpty() || entry.second().isEmpty()) {
                                                context.response("<#ff577e>pos1 or pos2 is not set!");
                                                return;
                                            }

                                            Schematic schematic = new Schematic();
                                            schematic.saveRegion(context.player(), context.world(),
                                                    entry.first().orElseThrow(), entry.second().orElseThrow(),
                                                    new File(NexelArena.instance().getDataFolder(), "arenas/" + context.arg("arena", String.class) + ".zea"));
                                        }))))
                .subCommand(SubCommand.of("paste")
                        .argument(CommandArgument.string("arena")
                                .completion((context, wrapper) -> {
                                    File[] files = NexelArena.instance()
                                            .getDataFolder()
                                            .toPath()
                                            .resolve("arenas")
                                            .toFile()
                                            .listFiles();
                                    if (files == null) {
                                        return wrapper.build();
                                    }
                                    Arrays.stream(files)
                                            .filter(file -> file != null && file.isFile())
                                            .filter(file -> file.getName().endsWith(".zea"))
                                            .map(file -> file.getName().replace(".zea", ""))
                                            .filter(file -> file.toLowerCase().startsWith(wrapper.builder().getRemainingLowerCase()))
                                            .forEach(wrapper::suggest);
                                    return wrapper.build();
                                })
                                .defaultExecute(context -> {
                                    String arena = context.arg("arena", String.class);
                                    File file = new File(NexelArena.instance().getDataFolder(), "arenas/" + arena + ".zea");

                                    if (!file.exists()) {
                                        context.response("<#ff577e>Schematic file not found");
                                        return;
                                    }

                                    CompletableFuture.runAsync(() -> {
                                        try {
                                            context.response("<#8336ff>Loading schematic...");
                                            Schematic schematic = Schematic.load(file);
                                            context.response("<#8336ff>Schematic loaded");
                                            context.response("<#8336ff>Regenerating from the schematic...");
                                            schematic.paste(context.world()).thenAccept(nexel -> {
                                                long start = System.nanoTime();
                                                nexel.callback(blocks -> {
                                                    long end = System.nanoTime();
                                                    context.response("<#8336ff>Schematic pasted, applied " + blocks + " blocks, " + ((end - start) / 1000000) + " ms");
                                                });
                                                nexel.applyPendingBlockUpdates();
                                            });
                                        } catch (IOException e) {
                                            context.response("<#ff577e>Failed to regenerate!");
                                            NexelArena.instance()
                                                    .getSLF4JLogger()
                                                    .error("Failed to load schematic", e);
                                        }
                                    });
                                })))
                .subCommand(SubCommand.of("pos")
                        .subCommand(SubCommand.of("1")
                                .defaultExecute(context -> {
                                    context.check(CheckType.PLAYER);
                                    PositionEntry entry = PositionManager.get(context.playerName());
                                    PositionManager.set(context.playerName(), context.player().getLocation(), entry.second().orElse(null));
                                    context.response("<#8336ff>Position 1 is set!");
                                }))
                        .subCommand(SubCommand.of("2")
                                .defaultExecute(context -> {
                                    context.check(CheckType.PLAYER);
                                    PositionEntry entry = PositionManager.get(context.playerName());
                                    PositionManager.set(context.playerName(), entry.first().orElse(null), context.player().getLocation());
                                    context.response("<#8336ff>Position 2 is set!");
                                })))
                .register();
        init();
    }


    /**
     * This method creates the arenas folder if it doesn't exist.
     */
    @SuppressWarnings("all")
    public static void init() {
        new File(NexelArena.instance().getDataFolder(), "arenas").mkdirs();
    }
}
