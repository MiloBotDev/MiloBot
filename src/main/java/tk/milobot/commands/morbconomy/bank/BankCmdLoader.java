package tk.milobot.commands.morbconomy.bank;

import tk.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankCmdLoader {

    public static void load() {
        ExecutorService bankExecutor = Executors.newSingleThreadExecutor();

        BankCmd bankCmd = new BankCmd(bankExecutor);
        bankCmd.addSubCommand(new BankBalanceCmd(bankExecutor));
        bankCmd.addSubCommand(new BankTransferCmd(bankExecutor));
        CommandHandler.getInstance().registerCommand(bankCmd);
    }

}
