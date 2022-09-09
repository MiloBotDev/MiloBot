package commands.games.hungergames;

import commands.Command;
import commands.SubCmd;
import games.hungergames.HungerGames;
import games.hungergames.models.LobbyEntry;
import games.hungergames.models.Item;
import games.hungergames.models.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import utility.lobby.BotLobby;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HungerGamesStartCmd extends Command implements SubCmd {

    public HungerGamesStartCmd() {
        this.commandName = "start";
        this.aliases = new String[]{"s", "host"};
        this.commandDescription = "Starts the Hunger Games";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        User author = event.getAuthor();
        new BotLobby("Hunger Games Lobby", author,
                (entries, message) -> {
                    ArrayList<LobbyEntry> participants = new ArrayList<>();
                    entries.forEach((players, npcs) -> {
                        npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                        players.forEach(user -> participants.add(new LobbyEntry(user.getIdLong(), user.getName(), user.getAsMention())));
                    });
                    HungerGames game = new HungerGames(participants);
                    game.startGame();
                    HungerGamesStartCmd.runGame(event.getChannel(), game);
                }, 2, 10)
                .initialize(event.getChannel());
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {

    }

    public static void runGame(MessageChannel channel, @NotNull HungerGames game) {
        Map<Integer, Map<List<String>, List<Player>>> roundData = game.getRoundData();
        List<RestAction<Void>> messages = new ArrayList<>();

        roundData.forEach((key1, value1) -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.BLUE);
            embed.setTimestamp(new Date().toInstant());
            embed.setTitle("Round " + key1);

            StringBuilder logs = new StringBuilder();
            value1.forEach((key, value) -> {
                for (String s : key) {
                    logs.append(s).append("\n");
                }

                for (Player player : value) {
                    StringBuilder playerDesc = new StringBuilder();
                    playerDesc.append("**Health:** ").append(player.getHealth());
                    playerDesc.append("\n**Inventory:** ");
                    List<Item> inventory = player.getInventory();
                    for (int i = 0; i < inventory.size(); i++) {
                        playerDesc.append(inventory.get(i).getName());
                        if (i != inventory.size() - 1) {
                            playerDesc.append(", ");
                        }
                    }
                    embed.addField(player.getUserName(), playerDesc.toString(), true);
                }
            });
            embed.setDescription(logs.toString());

            RestAction<Void> voidRestAction = channel.sendMessageEmbeds(embed.build())
                    .delay(15, TimeUnit.SECONDS)
                    .flatMap(Message::delete);
            messages.add(voidRestAction);
        });

        Timer timer = new Timer();
        TimerTask sendMessages = new TimerTask() {
            @Override
            public void run() {
                messages.forEach(messageAction -> {
                    messageAction.queue();
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                EmbedBuilder embedBuilder = HungerGamesStartCmd.generateRecapEmbed(game);
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        };
        timer.schedule(sendMessages, 0);
    }

    private static @NotNull EmbedBuilder generateRecapEmbed(@NotNull HungerGames game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Hunger Games Recap");
        embed.setColor(Color.BLUE);
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription("**Winner:** " + game.getWinner().getUserName());

        List<Player> players = game.getPlayers();
        for (Player player : players) {
            int damageDone = player.getDamageDone();
            int kills = player.getKills();
            int healingDone = player.getHealingDone();
            int itemsCollected = player.getItemsCollected();
            int damageTaken = player.getDamageTaken();

            String playerDesc = "*Kills:* " + kills + "\n" +
                    "*Damage Done:* " + damageDone + "\n" +
                    "*Damage Taken:* " + damageTaken + "\n" +
                    "*Healing Done:* " + healingDone + "\n" +
                    "*Items Collected:* " + itemsCollected + "\n";

            embed.addField(player.getUserName(), playerDesc, true);
        }

        return embed;
    }

}

