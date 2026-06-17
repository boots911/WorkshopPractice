package com.Qwikkspell.WorkshopPractice.game;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deterministic, seed-driven craft generation.
 *
 * <p>A run is fully defined by {@code (mode, seed)}. Given the same seed and mode this always
 * produces the same crafts in the same order, across server restarts and JVMs
 * ({@link java.util.Random#Random(long)} is contractually stable). The method is pure and
 * stateless and operates on a defensive copy, so it is safe to call concurrently from multiple
 * games — it never mutates the shared craft universe (this replaces the old
 * {@code GameManager.findItems} which shuffled a shared list in place).</p>
 */
public final class CraftSequence {

    private CraftSequence() {}

    /**
     * Generate the ordered craft list for a run.
     *
     * @param universe the full set of craftable materials (not mutated)
     * @param mode     the game mode (determines how many crafts and whether endless)
     * @param seed     the run seed
     * @return an immutable, ordered list of crafts for the run
     */
    public static List<Material> generate(List<Material> universe, GameMode mode, long seed) {
        Random rng = new Random(seed);
        int count = mode.getCraftCount();

        if (count == GameMode.CRAFT_COUNT_ALL) {
            List<Material> pool = new ArrayList<>(universe);
            Collections.shuffle(pool, rng);
            return Collections.unmodifiableList(pool);
        }

        if (count == GameMode.CRAFT_COUNT_ENDLESS) {
            // Build a sequence long enough that a timed run can never exhaust it. Each cycle
            // is reshuffled with the SAME rng, so the whole sequence stays deterministic.
            int target = Math.max(150, universe.size() * 3);
            List<Material> sequence = new ArrayList<>(target);
            while (sequence.size() < target) {
                List<Material> cycle = new ArrayList<>(universe);
                Collections.shuffle(cycle, rng);
                sequence.addAll(cycle);
            }
            return Collections.unmodifiableList(sequence);
        }

        // Finite N (base modes): shuffle a copy and take the first N.
        List<Material> pool = new ArrayList<>(universe);
        Collections.shuffle(pool, rng);
        int n = Math.min(count, pool.size());
        return Collections.unmodifiableList(new ArrayList<>(pool.subList(0, n)));
    }

    /**
     * Per-craft block-layout RNG. Derived from the run seed and the craft index so the wall
     * placement for a given craft is reproducible for the same {@code (seed, craftIndex)}.
     */
    public static Random layoutRandom(long seed, int craftIndex) {
        return new Random(seed * 31L + craftIndex);
    }

    /** Generate a fresh, shareable seed (<= 9 digits, like the spec's {@code 123456}). */
    public static long randomSeed() {
        return ThreadLocalRandom.current().nextLong(0, 1_000_000_000L);
    }
}
