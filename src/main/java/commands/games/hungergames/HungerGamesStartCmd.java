package commands.games.hungergames;

import commands.Command;
import commands.SubCmd;
import games.HungerGames;
import models.hungergames.Item;
import models.hungergames.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;
import utility.Lobby;

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

    public static void runGame(ButtonClickEvent event, @NotNull HungerGames game) {
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

            RestAction<Void> voidRestAction = event.getChannel()
                    .sendMessageEmbeds(embed.build())
                    .delay(25, TimeUnit.SECONDS)
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
                EmbedBuilder embedBuilder = HungerGamesStartCmd.generateRecapEmbed(game, event.getUser());
                event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            }
        };
        timer.schedule(sendMessages, 0);
    }

    private static @NotNull EmbedBuilder generateRecapEmbed(HungerGames game, User user) {
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

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        User author = event.getAuthor();
        String id = author.getId();

        Lobby lobby = new Lobby(id, author.getName(), 8);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Hunger Games Lobby");
        EmbedUtils.styleEmbed(embed, author);
        embed.setDescription(lobby.generateDescription());

        event.getChannel()
                .sendMessageEmbeds(embed.build())
                .setActionRows(ActionRow.of(
                        Button.primary(id + ":joinLobby", "Join"),
                        Button.primary(id + ":leaveLobby", "Leave"),
                        Button.primary(id + ":fillLobby", "Fill"),
                        Button.primary(id + ":startHg", "Start"),
                        Button.secondary(id + ":delete", "Delete")
                ))
                .queue(message -> {
                    String messageId = message.getId();
                    lobby.initialize(messageId);
                });
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {

    }
}

