package games;

import models.cards.CardDeck;
import models.cards.PlayingCards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.*;

public class Poker {
    private static final ArrayList<Poker> games = new ArrayList<>();
    private final User masterUser;
    private final List<User> players = new ArrayList<>();
    private final Map<User, PlayerData> playerData = new HashMap<>();
    private final TextChannel channel;
    private final CardDeck mainDeck = new CardDeck();
    private PokerState state;
    private int nextPlayerIndex;
    private int playerRaise;
    private int requiredMoneyInPot;
    private boolean waitingForUserRaise = false;
    private boolean firstRound = true;
    private int lastRaisePlayerIndex;

    public enum PokerState {
        WAITING_FOR_PLAYERS,
        BET_ROUND_1,
        REPLACING_CARDS,
        BET_ROUND_2,
        GAME_END
    }

    public enum PlayerAction {
        FOLD,
        CALL,
        RAISE,
        CHECK
    }

    private class PlayerData {
        private volatile Message embed;
        private List<PlayingCards> hand;
        private int moneyInPot = 0;
        private boolean inGame = true;
    }

    public Poker(User user, TextChannel channel) {
        this.masterUser = user;
        this.players.add(user);
        this.channel = channel;
        games.add(this);
        state = PokerState.WAITING_FOR_PLAYERS;
    }

    public static Poker getGameByChannel(TextChannel channel) {
        return games.stream().filter(game -> game.getChannel().equals(channel)).findFirst().orElse(null);
    }

    public TextChannel getChannel() {
        return channel;
    }

    public User getMasterUser() {
        return masterUser;
    }

    public void addPlayer(User user) {
        if (!players.contains(user)) {
            players.add(user);
        }
    }

    public boolean containsPlayer(User user) {
        return players.contains(user);
    }

    public void start() {
        if (players.size() == 1) {
            channel.sendMessage("You need at least 2 players to play poker.").queue();
        } else {
            players.forEach(player -> {
                playerData.put(player, new PlayerData());
                List<PlayingCards> hand = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    hand.add(mainDeck.drawCard());
                }
                playerData.get(player).hand = hand;
            });
            channel.sendMessage("Poker game started.").queue();
            state = PokerState.BET_ROUND_1;
            requiredMoneyInPot = 0;
            lastRaisePlayerIndex = 0;
            nextPlayerIndex = -1;
            next();
            /*players.forEach(player -> player.openPrivateChannel().queue(channel -> channel
                    .sendMessageEmbeds(generatePlayerEmbed(player).build()).queue(message -> {
                                playerEmbeds.put(player, message);

                                next();
                            }
                            )));*/

        }
    }

    private void next() {
        if (state == PokerState.BET_ROUND_1) {
            if (!firstRound && lastRaisePlayerIndex == nextPlayerIndex && !waitingForUserRaise) {
                state = PokerState.GAME_END;
                games.remove(this);
                User user = players.get(nextPlayerIndex);
                EmbedBuilder eb = generatePlayerEmbed(user);
                playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows().queue();
                players.forEach(player -> {
                    EmbedBuilder eb2 = generatePlayerEmbed(player);
                    eb2.addField("---------", "Game end", false);
                    playerData.get(player).embed.editMessageEmbeds(eb2.build()).queue();
                });
                //nextPlayerIndex = -1;
                //state = PokerState.REPLACING_CARDS;
                //players.forEach(player -> player.openPrivateChannel().queue(channel ->
                //        channel.sendMessage("Which cards would you like to replace? Enter card numbers separated " +
                //                "by spaces.").queue()));
                return;
            }
            if (waitingForUserRaise) {
                int raise = playerRaise;
                requiredMoneyInPot += raise;
                playerData.get(players.get(nextPlayerIndex)).moneyInPot = requiredMoneyInPot;
                lastRaisePlayerIndex = nextPlayerIndex;
                waitingForUserRaise = false;
            }
            if (nextPlayerIndex >= 0) {
                User user = players.get(nextPlayerIndex);
                EmbedBuilder eb = generatePlayerEmbed(user);
                playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows().queue();
            }
            if (nextPlayerIndex == players.size() - 1) {
                nextPlayerIndex = 0;
                firstRound = false;
            } else {
                nextPlayerIndex++;
            }
            User user = players.get(nextPlayerIndex);
            String authorId = user.getId();
            EmbedBuilder eb = generatePlayerEmbed(user);
            Button check = Button.primary(authorId + ":poker_check", "Check");
            Button fold = Button.primary(authorId + ":poker_fold", "Fold");
            Button call = Button.primary(authorId + ":poker_call", "Call");
            Button raise = Button.primary(authorId + ":poker_raise", "Raise");
            int moneyInPot = playerData.get(user).moneyInPot;
            ActionRow actionRow;
            if (moneyInPot < requiredMoneyInPot) {
                actionRow = ActionRow.of(fold, call, raise);
            } else {
                actionRow = ActionRow.of(check, raise);
            }
            if (firstRound && nextPlayerIndex == 0) {
                players.stream().filter(player -> !player.equals(user)).forEach(player -> player.openPrivateChannel()
                        .queue(channel ->
                        channel.sendMessageEmbeds(generatePlayerEmbed(player).build()).queue(message -> {
                            playerData.get(player).embed =  message;
                        })));
                user.openPrivateChannel().queue(channel ->
                        channel.sendMessageEmbeds(eb.build()).setActionRows(actionRow).queue(message -> {
                            playerData.get(user).embed = message;
                        }));
            } else {
                playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows(actionRow).queue();
            }
        } else if (state == PokerState.REPLACING_CARDS) {

        }
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getChannelType() == ChannelType.PRIVATE) {
            new ArrayList<>(games).forEach(game -> game.onMessage(event));
        }
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        if (state == PokerState.BET_ROUND_1 && event.getAuthor().equals(players.get(nextPlayerIndex)) &&
                waitingForUserRaise) {
            try {
                playerRaise = Integer.parseInt(event.getMessage().getContentRaw());
                if (playerRaise >= 1) {
                    next();
                } else {
                    event.getChannel().sendMessage("Raise must be at least 1.").queue();
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Please enter a number.").queue();
            }
        } else if (state == PokerState.REPLACING_CARDS) {
            if (event.getAuthor().equals(players.get(nextPlayerIndex))) {
                try {
                    String[] cards = event.getMessage().getContentRaw().split(" ");
                    List<PlayingCards> hand = playerData.get(event.getAuthor()).hand;
                    for (String card : cards) {
                        hand.set(Integer.parseInt(card) - 1, mainDeck.drawCard());
                    }
                    playerData.get(event.getAuthor()).embed.editMessageEmbeds(
                            generatePlayerEmbed(event.getAuthor()).build()).queue();
                    event.getChannel().sendMessage("Your cards have been replaced.").queue();
                    next();
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Please enter card numbers separated by spaces.").queue();
                }
            }
        }
    }

    private EmbedBuilder generatePlayerEmbed(User user) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Poker");
        embed.addField("------------", "**Your hand**", false);
        List<PlayingCards> hand = playerData.get(user).hand;
        for(int i = 0; i < hand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), hand.get(i).getLabel(), true);
        }
        embed.addBlankField(false);
        embed.addField("Your bet", String.valueOf(playerData.get(user).moneyInPot), true);
        embed.addField("Required money in pot", String.valueOf(requiredMoneyInPot), true);
        return embed;
    }

    public static Poker getUserGame(User user) {
        return games.stream().filter(game -> game.players.contains(user)).findAny().orElse(null);
    }

    public void setPlayerAction(PlayerAction action) {
        switch (action) {
            case CHECK -> next();
            case FOLD -> {
                players.remove(nextPlayerIndex);
                next();
            }
            case CALL -> {
                playerData.get(players.get(nextPlayerIndex)).moneyInPot = requiredMoneyInPot;
                next();
            }
            case RAISE -> {
                players.get(nextPlayerIndex).openPrivateChannel().queue(channel ->
                        channel.sendMessage("How much would you like to raise by?")
                                .queue(msg -> waitingForUserRaise = true));
            }
        }
    }
}
