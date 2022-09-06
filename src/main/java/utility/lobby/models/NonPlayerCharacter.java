package utility.lobby.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NonPlayerCharacter {

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
        String[] randomNames = {"Morbius", "Milo", "Jane Foster", "Captain America", "Walter White", "Jesse Pinkman",
                "Obama", "Kanye West", "Bill Gates", "Elon Musk", "Steve Harrington", "John Oliver",
                "Mother of Bram", "Nancy Wheeler", "Jonathan Byers", "Will Byers", "Vecna", "Darth Vader",
                "Lilo & Stitch", "Your Mom", "Riot Games", "The Rock", "The Joker", "Batman", "The Flash",
                "Jack Daniels"};
        List<String> randomNamesList = new ArrayList<>(Arrays.asList(randomNames));
        Collections.shuffle(randomNamesList);
        return randomNamesList.get(0);
    }

    public String getName() {
        return name;
    }
}
