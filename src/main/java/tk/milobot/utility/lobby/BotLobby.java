package tk.milobot.utility.lobby;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

public class BotLobby extends AbstractLobby {

    private final Set<User> players = new HashSet<>();
    private final Set<NonPlayerCharacter> bots = new HashSet<>();
    private final BiConsumer<Map<List<User>, List<NonPlayerCharacter>>, Message> startConsumer;
    private final int minPlayers;
    private int maxPlayers;
    private final String title;
    private final User creator;
    private boolean started = false;

    public BotLobby(String title, User creator, BiConsumer<Map<List<User>, List<NonPlayerCharacter>>, Message> startConsumer,
                 int minPlayers, int maxPlayers) {
        this.creator = creator;
        players.add(this.creator);
        this.title = title;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startConsumer = startConsumer;
    }

    @Override
    protected ActionRow getEmbedActionsRows() {
        ActionRow ret;
        if (started) {
            ret = null;
        } else {
            Button joinButton;
            if (players.size() == maxPlayers) {
                joinButton = Button.danger(creator.getId() + ":" + "joinLobby", "Join");
            } else {
                joinButton = Button.primary(creator.getId() + ":" + "joinLobby", "Join");
            }
            Button leaveButton = Button.primary(creator.getId() + ":" + "leaveLobby", "Leave");
            Button startButton = Button.primary(creator.getId() + ":" + "startLobby", "Start");
            Button deleteButton = Button.secondary(creator.getId() + ":" + "deleteLobby", "Delete");
            Button fillLobby = Button.primary(creator.getId() + ":fillLobby", "Fill");
            ret = ActionRow.of(joinButton, leaveButton, fillLobby, startButton ,deleteButton);
        }
        return ret;
    }

    @Override
    protected MessageEmbed getEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(generateDescription());
        eb.setColor(Color.BLUE);
        eb.setTimestamp(new Date().toInstant());
        return eb.build();
    }

    private @NotNull String generateDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Minimum Players:** ").append(this.minPlayers).append("\n");
        sb.append("**Maximum Players:** ").append(this.maxPlayers).append("\n");
        sb.append("**Current Players:** \n");
        for (User user : players) {
            sb.append("- ").append(user.getAsMention()).append("\n");
        }
        for (NonPlayerCharacter nonPlayerCharacter : bots) {
            sb.append("- ").append(nonPlayerCharacter.getName()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void addPlayer(User user) {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if ((players.size() + bots.size()) < maxPlayers) {
            if (players.add(user)) {
                editMessage();
            } else {
                message.reply(user.getAsMention() + " You are already in this lobby.").queue();
            }
        }
        setIdleInstanceCleanup();
    }

    public void addBot(NonPlayerCharacter nonPlayerCharacter) {
        if ((players.size() + bots.size()) < maxPlayers) {
            bots.add(nonPlayerCharacter);
        }
    }

    public void fill() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        while (players.size() + bots.size() < maxPlayers) {
            NonPlayerCharacter nonPlayerCharacter = new NonPlayerCharacter();
            while(bots.stream().anyMatch(npc -> npc.getName().equals(nonPlayerCharacter.getName()))) {
                nonPlayerCharacter.changeName();
            }
            addBot(nonPlayerCharacter);
        }
        editMessage();
        setIdleInstanceCleanup();
    }

    @Override
    public void removePlayer(User user) {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (!user.equals(creator)) {
            if (players.remove(user)) {
                editMessage();
            } else {
                message.reply(user.getAsMention() + " You are not in this lobby.").queue();
            }
        } else {
            message.reply(user.getAsMention() + " You can't leave your own lobby.").queue();
        }
        setIdleInstanceCleanup();
    }

    @Override
    public void start() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (players.size() + bots.size() >= minPlayers) {
            lobbyInstances.remove(message);
            started = true;
            editMessage();
            HashMap<List<User>, List<NonPlayerCharacter>> participants = new HashMap<>();
            participants.put(players.stream().toList(), bots.stream().toList());
            startConsumer.accept(participants, message);
        } else {
            message.reply("Not enough players to start the lobby.").queue();
            setIdleInstanceCleanup();
        }
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setBotsOnly() {
        for(User user : players) {
            players.remove(user);
        }
        fill();
    }

    public static class NonPlayerCharacter {

            private String name;

            public NonPlayerCharacter() {
                this.name = generateRandomName();
            }

            public NonPlayerCharacter(String name) {
                this.name = name;
            }

            public void changeName() {
                this.name = generateRandomName();
            }

            public String generateRandomName() {
                // add random names from movies to this list
                String[] randomNames = {"Morbius", "Milo", "Jane Foster", "Captain America", "Walter White", "Jesse Pinkman",
                        "Obama", "Kanye West", "Bill Gates", "Elon Musk", "Steve Harrington", "John Oliver",
                        "Mother of Bram", "Nancy Wheeler", "Jonathan Byers", "Will Byers", "Vecna", "Darth Vader",
                        "Lilo & Stitch", "Your Mom", "Riot Games", "The Rock", "The Joker", "Batman", "The Flash",
                        "Jack Daniels", "Pachycephalosaurus", "Ian Malcolm", "Alan Grant", "Ellie Satler", "John Hammond",
                         "The Terminator", "Arnold Schwarzenegger"};
                List<String> randomNamesList = new ArrayList<>(Arrays.asList(randomNames));
                Collections.shuffle(randomNamesList);
                return randomNamesList.get(0);
            }

            public String getName() {
                return name;
            }
        }
}
