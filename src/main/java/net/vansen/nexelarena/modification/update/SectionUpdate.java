package net.vansen.nexelarena.modification.update;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Represents updates to a specific section within a chunk.
 * This improves performance by grouping block updates by section,
 * reducing the number of section lookups needed.
 */
public class SectionUpdate {

    /**
     * The Y section index
     */
    public final int sectionIndex;

    /**
     * List of block updates for this section
     */
    public final List<BlockUpdate> updates = new ObjectArrayList<>();

    /**
     * Creates a new SectionUpdate for a specific section index
     *
     * @param sectionIndex The Y section index
     */
    public SectionUpdate(int sectionIndex) {
        this.sectionIndex = sectionIndex;
    }
}
