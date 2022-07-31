package games.hungergames.models;

public class Player {

    private String userName;
    private String userId;

    Player(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }
}
