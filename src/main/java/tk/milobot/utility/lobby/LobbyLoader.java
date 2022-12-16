package tk.milobot.utility.lobby;

import tk.milobot.commands.ButtonHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LobbyLoader {

    public static void load() {
        ButtonHandler buttonHandler = ButtonHandler.getInstance();

        ExecutorService lobbyService = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("joinLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.addPlayer(event.getUser());
            }
        });

        buttonHandler.registerButton("leaveLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.removePlayer(event.getUser());
            }
        });

        buttonHandler.registerButton("fillLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                if (lobby instanceof BotLobby botLobby) {
                    botLobby.fill();
                } else {
                    throw new ClassCastException("Only a bot lobby can be filled with random bots.");
                }
            }
        });

        buttonHandler.registerButton("startLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.start();
            }
        });

        buttonHandler.registerButton("deleteLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.remove();
            }
        });
    }
}
