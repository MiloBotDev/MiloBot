package tk.milobot.commands.games.hungergames;

import tk.milobot.commands.Command;
import tk.milobot.commands.ParentCmd;
import tk.milobot.commands.games.GamesCmd;
import net.dv8tion.jda.api.entities.ChannelType;

public class HungerGamesCmd extends Command implements ParentCmd, GamesCmd {

    public HungerGamesCmd() {
        this.commandName = "hungergames";
        this.aliases = new String[]{"hg"};
        this.commandDescription = "Hunger Games";
        this.subCommands.add(new HungerGamesStartCmd());
        this.subCommands.add(new HungerGamesStatsCmd());
        this.subCommands.add(new HungerGamesLeaderboardCmd());
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }
}