package com.Qwikkspell.WorkshopPractice.game;

import com.Qwikkspell.WorkshopPractice.WorkshopPractice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Append-only audit log of personal-best runs ({@code pb_games.log}).
 *
 * <p>Only runs that set a new PB are recorded, each with the mode, total result, seed (so the run
 * can be replayed with {@code /play <mode> seed <n>}), and the time taken for every individual
 * craft. The per-craft breakdown is what makes it possible to spot suspiciously fast crafts (e.g.
 * an item "crafted" faster than its materials could physically be gathered) when reviewing a
 * record-holder's run.</p>
 */
public class PbLogger {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WorkshopPractice plugin;
    private final File file;

    public PbLogger(WorkshopPractice plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pb_games.log");
    }

    /**
     * Append one PB entry.
     *
     * @param result a short result string, e.g. {@code "time=12.345s"} or {@code "crafts=42"}
     * @param crafts the per-craft records (material + seconds) in play order
     */
    public synchronized void logPersonalBest(String playerName, UUID uuid, GameMode mode, long seed,
                                             String result, List<StatsManager.CraftRecord> crafts) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append('[').append(LocalDateTime.now().format(TS)).append("] NEW PB\n");
        sb.append("player : ").append(playerName).append(" (").append(uuid).append(")\n");
        sb.append("mode   : ").append(mode.leaderboardKey()).append('\n');
        sb.append("result : ").append(result).append('\n');
        sb.append("seed   : ").append(seed).append("   (replay: /play ").append(mode.getAlias()).append(" seed ").append(seed).append(")\n");
        sb.append("crafts :\n");

        double sum = 0;
        int i = 1;
        for (StatsManager.CraftRecord cr : crafts) {
            sum += cr.seconds;
            sb.append(String.format("  %2d. %-22s %8.3fs%n", i++, cr.material.name(), cr.seconds));
        }
        sb.append(String.format("sum of craft times: %.3fs over %d craft(s)%n", sum, crafts.size()));

        write(sb.toString());
    }

    private void write(String text) {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(text);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write pb_games.log: " + e.getMessage());
        }
    }
}
