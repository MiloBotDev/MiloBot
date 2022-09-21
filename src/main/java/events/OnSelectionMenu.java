package events;

import database.dao.BlackjackDao;
import database.dao.HungerGamesDao;
import database.dao.WordleDao;
import database.model.Blackjack;
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

import static commands.games.wordle.WordleLeaderboardCmd.buildWordleEmbeds;
import static commands.games.hungergames.HungerGamesLeaderboardCmd.buildHgEmbeds;
import static commands.games.blackjack.BlackjackLeaderboardCmd.buildBlackjackEmbeds;

public class OnSelectionMenu extends ListenerAdapter {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(OnSelectionMenu.class);
    private final WordleDao wordleDao = WordleDao.getInstance();
    private final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();

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
                                embeds = buildWordleEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
                            }
                            case "fastestTime" -> {
                                List<Wordle> topFastestTime = this.wordleDao.getTopFastestTime();
                                embeds = buildWordleEmbeds(topFastestTime, "Fastest Time", event.getJDA());
                            }
                            case "totalWins" -> {
                                List<Wordle> topTotalWins = this.wordleDao.getTopTotalWins();
                                embeds = buildWordleEmbeds(topTotalWins, "Total Wins", event.getJDA());
                            }
                            case "totalGames" -> {
                                List<Wordle> topTotalGames = this.wordleDao.getTopTotalGames();
                                embeds = buildWordleEmbeds(topTotalGames, "Total Games Played", event.getJDA());
                            }
                            case "currentStreak" -> {
                                List<Wordle> topCurrentStreak = this.wordleDao.getTopCurrentStreak();
                                embeds = buildWordleEmbeds(topCurrentStreak, "Current Streak", event.getJDA());
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
                                embeds = buildHgEmbeds(topTotalGames, "Total Games Played", event.getJDA());
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
                case "blackjackLeaderboard" -> {
                    String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                    List<MessageEmbed> embeds = new ArrayList<>();
                    try {
                        switch (option) {
                            case "totalWins" -> {
                                List<Blackjack> topTotalWins = this.blackjackDao.getTopTotalWins();
                                embeds = buildBlackjackEmbeds(topTotalWins, "Total Wins", event.getJDA());
                            }
                            case "totalDraws" -> {
                                List<Blackjack> topTotalDraws = this.blackjackDao.getTopTotalDraws();
                                embeds = buildBlackjackEmbeds(topTotalDraws, "Total Draws", event.getJDA());
                            }
                            case "totalGamesPlayed" -> {
                                List<Blackjack> topTotalGamesPlayed = this.blackjackDao.getTopTotalGamesPlayed();
                                embeds = buildBlackjackEmbeds(topTotalGamesPlayed, "Total Games Played", event.getJDA());
                            }
                            case "highestStreak" -> {
                                List<Blackjack> topHighestStreak = this.blackjackDao.getTopHighestStreak();
                                embeds = buildBlackjackEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
                            }
                            case "currentStreak" -> {
                                List<Blackjack> topCurrentStreak = this.blackjackDao.getTopCurrentStreak();
                                embeds = buildBlackjackEmbeds(topCurrentStreak, "Current Streak", event.getJDA());
                            }
                            case "totalEarnings" -> {
                                List<Blackjack> topTotalEarnings = this.blackjackDao.getTopTotalEarnings();
                                embeds = buildBlackjackEmbeds(topTotalEarnings, "Total Earnings", event.getJDA());
                            }
                        }
                        Paginator paginator = new Paginator(user, embeds);
                        event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                                .queue(paginator::initialize);
                    } catch (SQLException e) {
                        logger.error("Failed to get the blackjack leaderboard", e);
                    }
                }
            }
        }
    }
}
