package commands.morbconomy.bank;

import commands.Command;
import commands.ParentCmd;
import commands.morbconomy.MorbconomyCmd;

public class BankCmd extends Command implements MorbconomyCmd, ParentCmd {

    public BankCmd() {
        this.commandName = "bank";
        this.commandDescription = "All commands related to your virtual bank.";
        this.subCommands.add(new BankBalanceCmd());
        this.subCommands.add(new BankTransferCmd());
    }
}
