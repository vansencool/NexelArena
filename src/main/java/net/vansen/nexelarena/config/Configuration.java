package net.vansen.nexelarena.config;

import net.vansen.fursconfig.FursConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a configuration class
 * <p>
 * A configuration class is a class that loads values from config.conf
 */
public interface Configuration {

    /**
     * Loads all the values from the fursconfig (node)
     * <p>
     * <b>NOTE: THIS WILL ONLY HAVE THE VALUES OF THE NODE AND THE CHILDREN, NOT THE PARENT NODE</b>
     *
     * @param node The node to load from
     */
    void config(@NotNull FursConfig node);

    /**
     * The path this class loads from
     *
     * @return The path, for example, "behavior" meaning it loads the behavior and it's children (MAY NOT COVER ALL THE CHILDREN.) May also be something like "behavior.explosion"
     * @see net.vansen.fursconfig.lang.Node for branch structure explanation
     */
    String loadsFrom();
}
