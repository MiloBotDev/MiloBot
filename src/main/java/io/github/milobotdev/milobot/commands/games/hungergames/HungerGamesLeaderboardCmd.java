package io.github.milobotdev.milobot.commands.games.hungergames;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.HungerGamesDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.HungerGames;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.Users;
import io.github.milobotdev.milobot.utility.paginator.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class HungerGamesLeaderboardCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes, EventListeners, Aliases {

    private final ExecutorService executorService;
    private static final UserDao userDao = UserDao.getInstance();
    private static final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(HungerGamesLeaderboardCmd.class);
    private static final Users userUtil = Users.getInstance();

    public HungerGamesLeaderboardCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("leaderboard", "View the hungergames leaderboards.");
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("lb", "leaderboards");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        SelectionMenu menu = SelectionMenu.create(event.getAuthor().getId() + ":hgLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Total Kills", "totalKills")
                .addOption("Total Damage Done", "totalDamageDone")
                .addOption("Total Damage Taken", "totalDamageTaken")
                .addOption("Total Healing Done", "totalHealingDone")
                .addOption("Total Items Collected", "totalItemsCollected")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Wins", "totalWins")
                .build();
        event.getChannel().sendMessage("HungerGames Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":hgLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Total Kills", "totalKills")
                .addOption("Total Damage Done", "totalDamageDone")
                .addOption("Total Damage Taken", "totalDamageTaken")
                .addOption("Total Healing Done", "totalHealingDone")
                .addOption("Total Items Collected", "totalItemsCollected")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Wins", "totalWins")
                .build();
        event.reply("HungerGames Leaderboard Selection").addActionRow(menu).queue();
    }


    private final ListenerAdapter HungerGamesLeaderboardListener = new ListenerAdapter() {
        @Override
        public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
            String[] id = event.getComponentId().split(":");
            String authorId = id[0];
            String type = id[1];
            User user = event.getUser();

            if (user.getId().equals(authorId) && type.equals("hgLeaderboard")) {
                event.deferEdit().queue();
                String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                List<MessageEmbed> embeds = new ArrayList<>();
                try {
                    switch (option) {
                        case "totalKills" -> {
                            List<HungerGames> topTotalKills = hungerGamesDao.getTopTotalKills();
                            embeds = buildHgEmbeds(topTotalKills, "Total Kills", event.getJDA());
                        }
                        case "totalDamageDone" -> {
                            List<HungerGames> topTotalDamageDone = hungerGamesDao.getTopTotalDamageDone();
                            embeds = buildHgEmbeds(topTotalDamageDone, "Total Damage Done", event.getJDA());
                        }
                        case "totalDamageTaken" -> {
                            List<HungerGames> topTotalDamageTaken = hungerGamesDao.getTopTotalDamageTaken();
                            embeds = buildHgEmbeds(topTotalDamageTaken, "Total Damage Taken", event.getJDA());
                        }
                        case "totalHealingDone" -> {
                            List<HungerGames> topTotalHealingDone = hungerGamesDao.getTopTotalHealingDone();
                            embeds = buildHgEmbeds(topTotalHealingDone, "Total Healing Done", event.getJDA());
                        }
                        case "totalItemsCollected" -> {
                            List<HungerGames> topTotalItemsCollected = hungerGamesDao.getTopTotalItemsCollected();
                            embeds = buildHgEmbeds(topTotalItemsCollected, "Total Items Collected", event.getJDA());
                        }
                        case "totalGamesPlayed" -> {
                            List<HungerGames> topTotalGames = hungerGamesDao.getTopTotalGamesPlayed();
                            embeds = buildHgEmbeds(topTotalGames, "Total Games Played", event.getJDA());
                        }
                        case "totalWins" -> {
                            List<HungerGames> topTotalWins = hungerGamesDao.getTopTotalWins();
                            embeds = buildHgEmbeds(topTotalWins, "Total Wins", event.getJDA());
                        }
                    }
                    Paginator paginator = new Paginator(user, embeds);
                    event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                            .queue(paginator::initialize);
                } catch (SQLException e) {
                    logger.error("Failed to get the hunger games leaderboard", e);
                }
            }
        }
    };

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(HungerGamesLeaderboardListener);
    }

    public static @NotNull ArrayList<MessageEmbed> buildHgEmbeds(@NotNull List<HungerGames> hungerGames, String title, JDA jda) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] counter = {1};
        hungerGames.forEach((hg) -> {
            try(Connection con = DatabaseConnection.getConnection()) {
                long discordId = Objects.requireNonNull(userDao.getUserById(con, hg.getUserId(), RowLockType.NONE)).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Total Kills" -> desc[0].append(String.format("`%d.` %s - %d kills.\n", counter[0], name, hg.getTotalKills()));
                    case "Total Damage Done" -> desc[0].append(String.format("`%d.` %s - %d damage done.\n", counter[0], name, hg.getTotalDamageDone()));
                    case "Total Damage Taken" -> desc[0].append(String.format("`%d.` %s - %d damage taken.\n", counter[0], name, hg.getTotalDamageTaken()));
                    case "Total Healing Done" -> desc[0].append(String.format("`%d.` %s - %d healing done.\n", counter[0], name, hg.getTotalHealingDone()));
                    case "Total Items Collected" -> desc[0].append(String.format("`%d.` %s - %d items collected.\n", counter[0], name, hg.getTotalItemsCollected()));
                    case "Total Games Played" -> desc[0].append(String.format("`%d.` %s - %d games played.\n", counter[0], name, hg.getTotalGamesPlayed()));
                    case "Total Wins" -> desc[0].append(String.format("`%d.` %s - %d wins.\n", counter[0], name, hg.getTotalWins()));
                }
                counter[0]++;
                if(counter[0] % 10 == 0) {
                    embed[0].setDescription(desc[0]);
                    embeds.add(embed[0].build());
                    embed[0] = new EmbedBuilder();
                    desc[0] = new StringBuilder();
                    embed[0].setTitle(title);
                    embed[0].setColor(Color.BLUE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

        embed[0].setDescription(desc[0]);
        embeds.add(embed[0].build());

        return embeds;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
