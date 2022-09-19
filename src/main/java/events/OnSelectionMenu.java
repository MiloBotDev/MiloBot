package events;

import database.dao.HungerGamesDao;
import database.dao.WordleDao;
import database.model.HungerGames;
import database.model.Wordle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import utility.Paginator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static commands.games.wordle.WordleLeaderboardCmd.buildEmbeds;
import static commands.games.hungergames.HungerGamesLeaderboardCmd.buildHgEmbeds;

public class OnSelectionMenu extends ListenerAdapter {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(OnSelectionMenu.class);
    private final WordleDao wordleDao = WordleDao.getInstance();
    private final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        String[] id = event.getComponentId().split(":");
        String authorId = id[0];
        String type = id[1];
        User user = event.getUser();

        if (user.getId().equals(authorId)) {
            event.deferEdit().queue();
            switch (type) {
                case "wordleLeaderboard" -> {
                    String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                    List<MessageEmbed> embeds = new ArrayList<>();
                    try {
                        switch (option) {
                            case "highestStreak" -> {
                                List<Wordle> topHighestStreak = this.wordleDao.getTopHighestStreak();
                                embeds = buildEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
                            }
                            case "fastestTime" -> {
                                List<Wordle> topFastestTime = this.wordleDao.getTopFastestTime();
                                embeds = buildEmbeds(topFastestTime, "Fastest Time", event.getJDA());
                            }
                            case "totalWins" -> {
                                List<Wordle> topTotalWins = this.wordleDao.getTopTotalWins();
                                embeds = buildEmbeds(topTotalWins, "Total Wins", event.getJDA());
                            }
                            case "totalGames" -> {
                                List<Wordle> topTotalGames = this.wordleDao.getTopTotalGames();
                                embeds = buildEmbeds(topTotalGames, "Total Games", event.getJDA());
                            }
                            case "currentStreak" -> {
                                List<Wordle> topCurrentStreak = this.wordleDao.getTopCurrentStreak();
                                embeds = buildEmbeds(topCurrentStreak, "Current Streak", event.getJDA());
                            }
                        }
                    } catch (SQLException e) {
                        logger.error("Failed to get the wordle leaderboard", e);
                    }
                    Paginator paginator = new Paginator(user, embeds);
                    event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                            .queue(paginator::initialize);
                }
                case "hgLeaderboard" -> {
                    String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                    List<MessageEmbed> embeds = new ArrayList<>();
                    try {
                        switch (option) {
                            case "totalKills" -> {
                                List<HungerGames> topTotalKills = this.hungerGamesDao.getTopTotalKills();
                                embeds = buildHgEmbeds(topTotalKills, "Total Kills", event.getJDA());
                            }
                            case "totalDamageDone" -> {
                                List<HungerGames> topTotalDamageDone = this.hungerGamesDao.getTopTotalDamageDone();
                                embeds = buildHgEmbeds(topTotalDamageDone, "Total Damage Done", event.getJDA());
                            }
                            case "totalDamageTaken" -> {
                                List<HungerGames> topTotalDamageTaken = this.hungerGamesDao.getTopTotalDamageTaken();
                                embeds = buildHgEmbeds(topTotalDamageTaken, "Total Damage Taken", event.getJDA());
                            }
                            case "totalHealingDone" -> {
                                List<HungerGames> topTotalHealingDone = this.hungerGamesDao.getTopTotalHealingDone();
                                embeds = buildHgEmbeds(topTotalHealingDone, "Total Healing Done", event.getJDA());
                            }
                            case "totalItemsCollected" -> {
                                List<HungerGames> topTotalItemsCollected = this.hungerGamesDao.getTopTotalItemsCollected();
                                embeds = buildHgEmbeds(topTotalItemsCollected, "Total Items Collected", event.getJDA());
                            }
                            case "totalGamesPlayed" -> {
                                List<HungerGames> topTotalGames = this.hungerGamesDao.getTopTotalGamesPlayed();
                                embeds = buildHgEmbeds(topTotalGames, "Total Games", event.getJDA());
                            }
                            case "totalWins" -> {
                                List<HungerGames> topTotalWins = this.hungerGamesDao.getTopTotalWins();
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
        }
    }
}
