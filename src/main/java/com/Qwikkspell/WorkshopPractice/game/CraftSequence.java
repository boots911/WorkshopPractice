package com.Qwikkspell.WorkshopPractice.game;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deterministic craft generation.
 *
 * <p><b>Base modes</b> (the 5-craft game) use a <i>bijective</i> seed: the seed is a compact index
 * of the exact ordered selection of crafts. Decoding a seed yields the crafts (for playing);
 * encoding crafts yields the seed (for {@code /findseed}). Every ordered selection of distinct
 * crafts has exactly one seed in {@code [0, perms(N, k))}, so a seed can always be produced for any
 * crafts the user asks for — no searching required.</p>
 *
 * <p><b>All Crafts</b> and <b>Time Trial</b> sequences are too long to encode in a single number
 * ({@code 61!} overflows a long), so they fall back to a normal seeded RNG shuffle. {@code /findseed}
 * does not apply to those modes.</p>
 *
 * <p>All methods are pure and operate on copies, so they are safe to call concurrently and never
 * mutate the shared craft universe. Determinism comes only from {@code (seed, mode, craftIndex)}, so
 * a seed reproduces the same run across restarts and JVMs.</p>
 */
public final class CraftSequence {

    private CraftSequence() {}

    /** Ordered craft sequence for a run. */
    public static List<Material> generate(List<Material> universe, GameMode mode, long seed) {
        int count = mode.getCraftCount();
        if (count == GameMode.CRAFT_COUNT_ALL) {
            return shuffleAll(universe, seed);
        }
        if (count == GameMode.CRAFT_COUNT_ENDLESS) {
            return endless(universe, seed);
        }
        return decodeSelection(universe, count, seed);
    }

    // ------------------------------------------------------------------ base modes (bijective)

    /** Number of ordered selections of {@code k} items from {@code n} (n*(n-1)*...*(n-k+1)). */
    public static long permutations(int n, int k) {
        long product = 1;
        for (int i = 0; i < k; i++) {
            product *= (n - i);
        }
        return product;
    }

    /**
     * Decode a seed into its ordered craft selection (inverse of {@link #encodeSelection}).
     * Runs a front-based partial Fisher-Yates driven by the seed's mixed-radix digits.
     */
    public static List<Material> decodeSelection(List<Material> universe, int k, long seed) {
        int n = universe.size();
        long max = permutations(n, k);
        long s = Math.floorMod(seed, max); // any long maps into the valid range
        int[] a = identity(n);
        List<Material> result = new ArrayList<>(k);
        for (int step = 0; step < k; step++) {
            int base = n - step;
            int r = (int) (s % base);
            s /= base;
            int j = step + r;
            swap(a, step, j);
            result.add(universe.get(a[step]));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Encode an ordered list of distinct crafts into its seed (inverse of {@link #decodeSelection}).
     * Always succeeds for valid input — this is what powers {@code /findseed}.
     *
     * @throws IllegalArgumentException if a craft is not in the universe or is duplicated
     */
    public static long encodeSelection(List<Material> universe, List<Material> ordered) {
        int n = universe.size();
        int k = ordered.size();
        int[] a = identity(n);
        long seed = 0;
        long multiplier = 1;
        for (int step = 0; step < k; step++) {
            int target = universe.indexOf(ordered.get(step));
            if (target < 0) {
                throw new IllegalArgumentException("Craft not available: " + ordered.get(step));
            }
            int p = -1;
            for (int i = step; i < n; i++) {
                if (a[i] == target) {
                    p = i;
                    break;
                }
            }
            if (p < 0) {
                throw new IllegalArgumentException("Duplicate craft: " + ordered.get(step));
            }
            int r = p - step;
            int base = n - step;
            seed += multiplier * r;
            multiplier *= base;
            swap(a, step, p);
        }
        return seed;
    }

    // ------------------------------------------------------------------ long modes (RNG)

    private static List<Material> shuffleAll(List<Material> universe, long seed) {
        List<Material> pool = new ArrayList<>(universe);
        Collections.shuffle(pool, new Random(seed));
        return Collections.unmodifiableList(pool);
    }

    private static List<Material> endless(List<Material> universe, long seed) {
        Random rng = new Random(seed);
        int target = Math.max(150, universe.size() * 3);
        List<Material> sequence = new ArrayList<>(target);
        while (sequence.size() < target) {
            List<Material> cycle = new ArrayList<>(universe);
            Collections.shuffle(cycle, rng);
            sequence.addAll(cycle);
        }
        return Collections.unmodifiableList(sequence);
    }

    // ------------------------------------------------------------------ shared

    /**
     * Per-craft block-layout RNG, derived from the run seed and craft index so the wall placement
     * for a given craft is reproducible for the same {@code (seed, craftIndex)}.
     */
    public static Random layoutRandom(long seed, int craftIndex) {
        return new Random(seed * 31L + craftIndex);
    }

    /** A fresh seed appropriate for the mode (within the bijective range for base modes). */
    public static long randomSeed(List<Material> universe, GameMode mode) {
        int count = mode.getCraftCount();
        if (count == GameMode.CRAFT_COUNT_ALL || count == GameMode.CRAFT_COUNT_ENDLESS) {
            return ThreadLocalRandom.current().nextLong(0, 1_000_000_000L);
        }
        return ThreadLocalRandom.current().nextLong(0, permutations(universe.size(), count));
    }

    private static int[] identity(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }
        return a;
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
