package commands.morbconomy.bank;

import commands.Command;
import commands.ParentCmd;
import commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class BankCmd extends Command implements MorbconomyCmd, ParentCmd {

    public BankCmd() {
        this.commandName = "bank";
        this.commandDescription = "All commands related to your virtual bank.";
        this.subCommands.add(new BankBalanceCmd());
        this.subCommands.add(new BankTransferCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
    }
}
