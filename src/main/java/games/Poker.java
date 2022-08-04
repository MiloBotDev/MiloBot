package games;

import models.cards.CardDeck;
import models.cards.PlayingCards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Poker {
    private static final List<Poker> games = new ArrayList<>();
    private final User masterUser;
    private final List<User> players = new ArrayList<>();
    private final HashMap<User, List<PlayingCards>> playerHands = new HashMap<>();
    private final TextChannel channel;
    private final CardDeck mainDeck = new CardDeck();

    public Poker(User user, TextChannel channel) {
        this.masterUser = user;
        this.players.add(user);
        this.channel = channel;
        games.add(this);
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
                List<PlayingCards> hand = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    hand.add(mainDeck.drawCard());
                }
                playerHands.put(player, hand);
            });
            channel.sendMessage("Poker game started.").queue();
            players.forEach(player -> player.openPrivateChannel().queue(channel -> channel
                    .sendMessageEmbeds(generatePlayerEmbed(player)).queue()));
        }
    }

    private MessageEmbed generatePlayerEmbed(User user) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Blackjack");
        embed.addField("------------", "**Your hand**", false);
        List<PlayingCards> hand = playerHands.get(user);
        for(int i = 0; i < hand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), hand.get(i).getLabel(), true);
        }
        return embed.build();
    }
}
