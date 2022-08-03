package games;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Poker {
    private static final List<Poker> games = new ArrayList<>();
    private final User masterUser;
    private final List<User> players = new ArrayList<>();
    private final TextChannel channel;

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
            channel.sendMessage("Poker game started.").queue();
            players.forEach(player -> player.openPrivateChannel().queue(channel -> channel
                    .sendMessage("You have been added to a poker game.").queue()));
        }
    }
}
