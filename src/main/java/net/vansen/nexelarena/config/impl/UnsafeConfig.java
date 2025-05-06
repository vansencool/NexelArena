package net.vansen.nexelarena.config.impl;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.config.Configuration;
import net.vansen.nexelarena.config.Variables;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UnsafeConfig implements Configuration {

    @Override
    public void config(@NotNull FursConfig node) {
        Variables.SET_BLOCKS_UNSAFE = node.getBoolean("set_blocks_unsafe", false);
        Variables.BLOCK_THE_ERROR_FROM_UNSAFE = node.getBoolean("block_the_error_from_unsafe", false);
        if (Variables.SET_BLOCKS_UNSAFE) {
            NexelArena.instance()
                    .getSLF4JLogger()
                    .warn("Unsafe block setting is enabled, YOU ARE RESPONSIBLE FOR ANY ISSUES THIS CAUSES.");
        }
    }

    @Override
    public String loadsFrom() {
        return "unsafe";
    }
}
