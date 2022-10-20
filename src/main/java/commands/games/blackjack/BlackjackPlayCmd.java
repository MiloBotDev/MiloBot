package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.util.NewDatabaseConnection;
import database.util.RowLockType;
import games.Blackjack;
import models.cards.PlayingCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import database.dao.BlackjackDao;
import database.dao.UserDao;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlackjackPlayCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(BlackjackPlayCmd.class);
    public static Map<Long, Blackjack> blackjackGames = new HashMap<>();
    private final UserDao userDao = UserDao.getInstance();
    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();

    public BlackjackPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of blackjack on discord.";
        this.commandArgs = new String[]{"bet*"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }

    public static @NotNull EmbedBuilder generateBlackjackEmbed(@NotNull User user, Blackjack.BlackjackStates state) {
        Blackjack game = blackjackGames.get(user.getIdLong());

        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Blackjack");

        if (game.getPlayerBet() > 0) {
            embed.setDescription("You have bet `" + game.getPlayerBet() + "` Morbcoins.");
        }

        embed.addField("------------", "**Dealer Hand**", false);
        List<PlayingCard> dealerHand = game.getDealerHand();
        for (int i = 0; i < dealerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), dealerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", game.calculateHandValue(dealerHand)), false);

        embed.addField("------------", "**Player Hand**", false);
        List<PlayingCard> playerHand = game.getPlayerHand();
        for (int i = 0; i < playerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), playerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", game.calculateHandValue(playerHand)), false);

        if (state != null) {
            if (!game.isDealerStand()) {
                if (state.equals(Blackjack.BlackjackStates.DEALER_WIN)) {
                    String value = "**Dealer Wins!**\n";
                    if (game.getPlayerBet() > 0) {
                        value += String.format("You lose `%d` Morbcoins!\n", game.getWinnings());
                    }
                    embed.addField("------------", value, false);
                    game.setFinished(true);
                }
            } else {
                if (state.equals(Blackjack.BlackjackStates.PLAYER_WIN)) {
                    String format = String.format("**%s** wins!\n", user.getName());
                    if (game.getPlayerBet() > 0) {
                        format += String.format("You win `%d` Morbcoins!", game.getWinnings());
                    }
                    embed.addField("------------", format, false);
                    game.setFinished(true);
                } else if (state.equals(Blackjack.BlackjackStates.DRAW)) {
                    String value = "Its a draw!\n";
                    if (game.getPlayerBet() > 0) {
                        value += "You lose nothing.";
                    }
                    embed.addField("------------", value, false);
                    game.setFinished(true);
                } else if (state.equals(Blackjack.BlackjackStates.DEALER_WIN)) {
                    String format = "Dealer wins!\n";
                    if (game.getPlayerBet() > 0) {
                        format += String.format("You lose `%d` Morbcoins!", game.getWinnings());
                    }
                    embed.addField("------------", format, false);
                    game.setFinished(true);
                } else if (state.equals(Blackjack.BlackjackStates.DEALER_BLACKJACK)) {
                    String format = "Dealer wins with blackjack!\n";
                    if (game.getPlayerBet() > 0) {
                        format += String.format("You lose `%d` Morbcoins!", game.getWinnings());
                    }
                    embed.addField("------------", format, false);
                    game.setFinished(true);
                } else if (state.equals(Blackjack.BlackjackStates.PLAYER_BLACKJACK)) {
                    String format = String.format("**%s** wins with blackjack!\n", user.getName());
                    if (game.getPlayerBet() > 0) {
                        format += String.format("You win `%d` Morbcoins!", game.getWinnings());
                    }
                    embed.addField("------------", format, false);
                    game.setFinished(true);
                }
            }

        }
        return embed;
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String authorId = event.getAuthor().getId();
        long authorIdLong = event.getAuthor().getIdLong();

        int bet = 0;
        if (args.size() > 0) {
            try {
                int playerBet = Integer.parseInt(args.get(0));
                if (playerBet < 0) {
                    event.getChannel().sendMessage("You can't bet a negative amount of Morbcoins.").queue();
                    return;
                } else if (playerBet == 0) {
                    event.getChannel().sendMessage("You can't bet `0` Morbcoins.").queue();
                    return;
                } else if (playerBet > 10000) {
                    event.getChannel().sendMessage("You can't bet more than `10000` Morbcoins.").queue();
                    return;
                } else {
                    try (Connection con = NewDatabaseConnection.getConnection()) {
                        con.setAutoCommit(false);
                        database.model.User user = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
                        int playerWallet = Objects.requireNonNull(user).getCurrency();
                        int newWallet = playerWallet - playerBet;
                        if (newWallet < 0) {
                            event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();
                            con.commit();
                            return;
                        }
                        user.setCurrency(newWallet);
                        userDao.update(con, user);
                        if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                            blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                                    userDao.getUserByDiscordId(authorIdLong)).getId()));
                        }
                        con.commit();
                    } catch (SQLException e) {
                        logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                        return;
                    }
                    bet = playerBet;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid bet amount.").queue();
                return;
            }
        }

        if (blackjackGames.containsKey(authorIdLong)) {
            event.getChannel().sendMessage("You are already in a game of blackjack.").queue();
            return;
        }

        Blackjack blackJack = new Blackjack(authorIdLong, bet);
        blackJack.initializeGame();

        blackjackGames.put(authorIdLong, blackJack);

        Blackjack.BlackjackStates blackjackStates = blackJack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(Blackjack.BlackjackStates.PLAYER_BLACKJACK)) {
            blackJack.dealerHit();
            blackJack.setDealerStand(true);
            blackjackStates = blackJack.checkWin(true);
            embed = generateBlackjackEmbed(event.getAuthor(), blackjackStates);
            BlackjackPlayCmd.blackjackGames.remove(authorIdLong);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = generateBlackjackEmbed(event.getAuthor(), null);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue();
        }
    }

    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        String authorId = event.getUser().getId();
        long authorIdLong = event.getUser().getIdLong();
        int bet;
        if (event.getOption("bet") == null) {
            bet = 0;
            try (Connection con = NewDatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                    blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                            userDao.getUserByDiscordId(authorIdLong)).getId()));
                }
                con.commit();
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                return;
            }
        } else {
            bet = Math.toIntExact(Objects.requireNonNull(event.getOption("bet")).getAsLong());
            try (Connection con = NewDatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                database.model.User user = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                int playerWallet = Objects.requireNonNull(user).getCurrency();
                int newWallet = playerWallet - bet;
                if (newWallet < 0) {
                    event.getHook().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
                    con.commit();
                    return;
                }
                user.setCurrency(newWallet);
                userDao.update(con, user);
                if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                    blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                            userDao.getUserByDiscordId(authorIdLong)).getId()));
                }
                con.commit();
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                return;
            }
        }

        if (blackjackGames.containsKey(authorIdLong)) {
            event.getHook().sendMessage("You are already in a game of blackjack.").queue();
            return;
        }

        Blackjack blackJack = new Blackjack(authorIdLong, bet);
        blackJack.initializeGame();

        blackjackGames.put(authorIdLong, blackJack);

        Blackjack.BlackjackStates blackjackStates = blackJack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(Blackjack.BlackjackStates.PLAYER_BLACKJACK)) {
            blackJack.dealerHit();
            blackJack.setDealerStand(true);
            blackjackStates = blackJack.checkWin(true);
            embed = generateBlackjackEmbed(event.getUser(), blackjackStates);
            BlackjackPlayCmd.blackjackGames.remove(authorIdLong);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = generateBlackjackEmbed(event.getUser(), null);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue();
        }
    }
}
