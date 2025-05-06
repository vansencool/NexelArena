package net.vansen.nexelarena.logging;

import net.vansen.nexelarena.config.Variables;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to filter out specific log messages from the console.
 * <p>
 * This is used to filter out block entity errors that are not relevant.
 */
public class LogFilter implements Filter {

    public static List<String> list = new ArrayList<>(List.of(
            "Failed to create block entity",
            "Skipping BlockEntity with id",
            "Invalid block entity",
            "Invalid for ticking",
            "Tried to load a block entity for block"
    ));

    public static void register() {
        if (Variables.ENABLE_LOG_FILTER) {
            if (Variables.BLOCK_THE_ERROR_FROM_UNSAFE) {
                list.add("Exception thrown from thread");
            }
            ((Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
        }
    }

    public Filter.Result check(@Nullable String message) {
        if (message == null) return Filter.Result.NEUTRAL;
        for (String s : list) {
            if (!message.toLowerCase().contains(s.toLowerCase())) continue;
            return Filter.Result.DENY;
        }

        return Filter.Result.NEUTRAL;
    }


    @Override
    public LifeCycle.State getState() {
        try {
            return LifeCycle.State.STARTED;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return check(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return check(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return check(msg.toString());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return check(msg.getFormattedMessage());
    }

    @Override
    public Result filter(LogEvent event) {
        return check(event.getMessage().getFormattedMessage());
    }
}
