package com.Qwikkspell.WorkshopPractice.discord;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes Discord interactions to {@link DiscordManager}: the {@code /leaderboard} and
 * {@code /findseed} slash commands, the leaderboard pagination buttons, and item/craft autocomplete.
 */
public class BotCommandListener extends ListenerAdapter {

    private static final String BUTTON_PREFIX = "wplb";
    private static final String DELIM = "|";

    private final DiscordManager manager;

    public BotCommandListener(DiscordManager manager) {
        this.manager = manager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "leaderboard":
                handleLeaderboard(event);
                break;
            case "findseed":
                handleFindSeed(event);
                break;
            default:
                break;
        }
    }

    private void handleLeaderboard(SlashCommandInteractionEvent event) {
        String category = event.getOption("category").getAsString();
        String item = event.getOption("item") != null ? event.getOption("item").getAsString() : null;

        if (category.equals("item") && (item == null || item.isBlank())) {
            event.reply("Pick an item too — e.g. `/leaderboard category:Item item:DIAMOND_SWORD`.")
                    .setEphemeral(true).queue();
            return;
        }

        DiscordManager.Page page = manager.renderPage(category, item, 0);
        if (page == null) {
            event.reply("Couldn't build that leaderboard — check the item id.").setEphemeral(true).queue();
            return;
        }
        event.reply(page.content).addComponents(ActionRow.of(buttons(page))).queue();
    }

    private void handleFindSeed(SlashCommandInteractionEvent event) {
        List<String> crafts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            crafts.add(event.getOption("craft" + i).getAsString());
        }
        DiscordManager.SeedResult result = manager.findSeed(crafts);
        event.reply(result.message).setEphemeral(!result.success).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(BUTTON_PREFIX + DELIM)) {
            return;
        }
        String[] parts = id.split("\\" + DELIM);
        if (parts.length != 4) {
            event.deferEdit().queue();
            return;
        }
        String category = parts[1];
        String item = parts[2].equals("-") ? null : parts[2];
        int page;
        try {
            page = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            event.deferEdit().queue();
            return;
        }
        DiscordManager.Page rendered = manager.renderPage(category, item, page);
        if (rendered == null) {
            event.deferEdit().queue();
            return;
        }
        event.editMessage(rendered.content).setComponents(ActionRow.of(buttons(rendered))).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        String option = event.getFocusedOption().getName();
        if (!option.equals("item") && !option.startsWith("craft")) {
            return;
        }
        String input = event.getFocusedOption().getValue().toUpperCase();
        List<Command.Choice> choices = new ArrayList<>();
        for (String name : manager.craftNames()) {
            if (name.contains(input)) {
                choices.add(new Command.Choice(name, name));
                if (choices.size() >= 25) {
                    break;
                }
            }
        }
        event.replyChoices(choices).queue();
    }

    private List<Button> buttons(DiscordManager.Page page) {
        String itemKey = page.item == null ? "-" : page.item;
        Button prev = Button.primary(buttonId(page.category, itemKey, page.page - 1), "◀ Prev")
                .withDisabled(!page.hasPrev);
        Button next = Button.primary(buttonId(page.category, itemKey, page.page + 1), "Next ▶")
                .withDisabled(!page.hasNext);
        List<Button> row = new ArrayList<>();
        row.add(prev);
        row.add(next);
        return row;
    }

    private String buttonId(String category, String itemKey, int page) {
        return BUTTON_PREFIX + DELIM + category + DELIM + itemKey + DELIM + page;
    }
}
