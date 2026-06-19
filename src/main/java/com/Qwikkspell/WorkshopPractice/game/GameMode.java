package com.Qwikkspell.WorkshopPractice.game;

import java.util.Optional;

/**
 * Describes a playable mode. Encapsulates the parameters that used to be passed around the
 * engine as loose {@code side} / {@code difficulty} / {@code duration} values so that adding
 * a mode is a single enum entry rather than scattered branching.
 *
 * <p>The four base modes map exactly onto the original {@code side x difficulty} combinations,
 * preserving existing gameplay.</p>
 */
public enum GameMode {
    LEFT       ("left",        "Left",          Direction.NORTH, "normal", 5,                  150L, ScoringType.FASTEST_TIME, true),
    LEFT_EASY  ("lefteasy",    "Left Easy",     Direction.NORTH, "easy",   5,                  150L, ScoringType.FASTEST_TIME, true),
    RIGHT      ("right",       "Right",         Direction.SOUTH, "normal", 5,                  150L, ScoringType.FASTEST_TIME, true),
    RIGHT_EASY ("righteasy",   "Right Easy",    Direction.SOUTH, "easy",   5,                  150L, ScoringType.FASTEST_TIME, true),
    // -1 / -2 below are the CRAFT_COUNT_ALL / CRAFT_COUNT_ENDLESS sentinels; they are written as
    // literals here because enum constants are initialized before static fields (a named reference
    // would be an illegal forward reference).
    ALL_CRAFTS_LEFT      ("allcraftsleft",      "All Crafts (Left)",       Direction.NORTH, "normal", -1, 0L, ScoringType.FASTEST_TIME, false),
    ALL_CRAFTS_LEFT_EASY ("allcraftslefteasy",  "All Crafts (Left Easy)",  Direction.NORTH, "easy",   -1, 0L, ScoringType.FASTEST_TIME, false),
    ALL_CRAFTS_RIGHT     ("allcraftsright",     "All Crafts (Right)",      Direction.SOUTH, "normal", -1, 0L, ScoringType.FASTEST_TIME, false),
    ALL_CRAFTS_RIGHT_EASY("allcraftsrighteasy", "All Crafts (Right Easy)", Direction.SOUTH, "easy",   -1, 0L, ScoringType.FASTEST_TIME, false),
    TIME_TRIAL_60("timetrial60", "Time Trial 60s", Direction.ANY, "normal", -2, 60L, ScoringType.MOST_CRAFTS, false);

    /** Sentinel craft counts. */
    public static final int CRAFT_COUNT_ALL = -1;      // every craft in the universe, once
    public static final int CRAFT_COUNT_ENDLESS = -2;  // unbounded supply (time-limited mode)

    public enum ScoringType {
        /** Lower completion time is better (base modes + all crafts). */
        FASTEST_TIME,
        /** More crafts completed in the window is better (time trial). */
        MOST_CRAFTS
    }

    public enum Direction {
        NORTH, SOUTH, ANY;

        /** Station direction string used by {@link CraftStation#getDirection()}. */
        public String station() {
            return name().toLowerCase();
        }
    }

    private final String alias;
    private final String displayName;
    private final Direction direction;
    private final String difficulty;
    private final int craftCount;
    private final long timeLimitSeconds; // 0 = no hard limit
    private final ScoringType scoringType;
    private final boolean countsTowardOverall;

    GameMode(String alias, String displayName, Direction direction, String difficulty, int craftCount,
             long timeLimitSeconds, ScoringType scoringType, boolean countsTowardOverall) {
        this.alias = alias;
        this.displayName = displayName;
        this.direction = direction;
        this.difficulty = difficulty;
        this.craftCount = craftCount;
        this.timeLimitSeconds = timeLimitSeconds;
        this.scoringType = scoringType;
        this.countsTowardOverall = countsTowardOverall;
    }

    public String getAlias() { return alias; }
    public String getDisplayName() { return displayName; }
    public Direction getDirection() { return direction; }
    public String getDifficulty() { return difficulty; }
    public int getCraftCount() { return craftCount; }
    public long getTimeLimitSeconds() { return timeLimitSeconds; }
    public ScoringType getScoringType() { return scoringType; }
    public boolean countsTowardOverall() { return countsTowardOverall; }

    /** Leaderboard key for this mode (same as the command alias). */
    public String leaderboardKey() { return alias; }

    public boolean isAnyDirection() { return direction == Direction.ANY; }

    public boolean hasTimeLimit() { return timeLimitSeconds > 0; }

    /** Resolve a {@code /play <mode>} alias to a {@link GameMode}, case-insensitive. */
    public static Optional<GameMode> fromAlias(String alias) {
        if (alias == null) return Optional.empty();
        String key = alias.toLowerCase();
        // Bare "allcrafts" defaults to the most popular variant (left easy).
        if (key.equals("allcrafts")) {
            return Optional.of(ALL_CRAFTS_LEFT_EASY);
        }
        for (GameMode mode : values()) {
            if (mode.alias.equals(key)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve an All Crafts variant. A null/blank variant defaults to left easy (the most popular).
     * Returns null for an unrecognised variant.
     */
    public static GameMode allCrafts(String variant) {
        String v = (variant == null || variant.isBlank()) ? "lefteasy" : variant.toLowerCase();
        switch (v) {
            case "left":      return ALL_CRAFTS_LEFT;
            case "lefteasy":  return ALL_CRAFTS_LEFT_EASY;
            case "right":     return ALL_CRAFTS_RIGHT;
            case "righteasy": return ALL_CRAFTS_RIGHT_EASY;
            default:          return null;
        }
    }

    /** The four modes that feed the overall leaderboard, in display order. */
    public static GameMode[] overallModes() {
        return new GameMode[]{ LEFT, LEFT_EASY, RIGHT, RIGHT_EASY };
    }
}
