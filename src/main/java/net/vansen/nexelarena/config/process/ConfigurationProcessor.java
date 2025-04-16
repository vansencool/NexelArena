package net.vansen.nexelarena.config.process;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarFile;

public class ConfigurationProcessor {
    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to get Unsafe instance", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void process(@NotNull FursConfig config) {
        if (!config.getString("version").equals(NexelArena.instance().getPluginMeta().getVersion())) {
            try {
                Logger logger = NexelArena.instance().getSLF4JLogger();
                logger.info("Configuration version seems to be outdated!");
                logger.info("For now, we are creating a copy of the config file.");

                Files.createDirectories(Path.of(NexelArena.instance().getDataFolder().getPath(), "copies"));
                int copy = 1;
                while (Files.exists(Path.of(NexelArena.instance().getDataFolder().getPath(), "copies/config copy " + copy + ".conf"))) {
                    copy++;
                }
                Path configPath = NexelArena.instance().getDataFolder().toPath().resolve("config.conf");
                Files.copy(configPath, Path.of(NexelArena.instance().getDataFolder().getPath(), "copies/config copy " + copy + ".conf"));
                logger.info("Created a copy of the config file: config copy {}.conf", copy);
                NexelArena.instance()
                        .saveResource("config.conf", true);
                config = FursConfig.createAndParseFile(configPath);
            } catch (Exception e) {
                NexelArena.instance()
                        .getSLF4JLogger()
                        .error("Failed to create a copy of the config file", e);
            }
        }
        try (JarFile jar = new JarFile(new File(((URLClassLoader) NexelArena.instance().getClass().getClassLoader()).getURLs()[0].toURI()))) {
            FursConfig finalConfig = config;
            jar.stream()
                    .parallel()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .map(entry -> entry.getName().replace('/', '.').replace(".class", ""))
                    .filter(className -> className.startsWith("net.vansen.nexelarena.config.impl"))
                    .map(ConfigurationProcessor::load)
                    .filter(Objects::nonNull)
                    .filter(Configuration.class::isAssignableFrom)
                    .map(clazz -> {
                        try {
                            return (Configuration) unsafe.allocateInstance(clazz);
                        } catch (InstantiationException e) {
                            NexelArena.instance()
                                    .getSLF4JLogger()
                                    .error("Failed to create instance of Configuration class", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(instance -> {
                        try {
                            String loadsFrom = instance.loadsFrom();
                            if (loadsFrom.isEmpty()) instance.config(finalConfig);
                            else instance.config(finalConfig.getFursConfig(loadsFrom));
                        } catch (Exception e) {
                            NexelArena.instance()
                                    .getSLF4JLogger()
                                    .error("Failed to process configuration", e);
                        }
                    });
        } catch (Exception e) {
            NexelArena.instance()
                    .getSLF4JLogger()
                    .error("Failed to process annotations", e);
        }
    }

    private static Class<?> load(@NotNull String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            return null;
        }
    }
}
