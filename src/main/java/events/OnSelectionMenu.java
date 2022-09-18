package events;

import database.dao.WordleDao;
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

public class OnSelectionMenu extends ListenerAdapter {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(OnSelectionMenu.class);
    private final WordleDao wordleDao = WordleDao.getInstance();

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
                        logger.error("Failed to get wordle leaderboard", e);
                    }
                    Paginator paginator = new Paginator(user, embeds);
                    event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                            .queue(paginator::initialize);
                }
            }
        }
    }
}
