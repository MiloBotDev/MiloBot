# Contributing

- [Contributing](#contributing)
    * [Creating Commands](#creating-commands)
        + [Arguments](#arguments)
        + [Cooldown](#cooldown)
        + [Aliases](#aliases)
        + [Permissions](#permissions)
    * [Adding Command Functionality](#adding-command-functionality)
        + [Regular Commands](#regular-commands)
        + [Slash Commands](#slash-commands)
    * [Loading Commands](#loading-commands)
        + [Regular Commands](#regular-commands-1)
        + [Slash Commands](#slash-commands-1)

## Configuration

If this is your first time trying to run the bot on your local device make sure you create a config.yml file.
This file holds all the configuration settings for the bot, you can copy the contents of the config.yml.example and edit 
it based on your own settings.

```yaml
# bot
prefix: ";"
token:
testGuildId:
loggingChannelName:

# github
personalAccessToken:
repositoryName: ""

# paths
levelsJsonPath: /levels.json
wordleWordsPath: wordle_words.txt
monstersCsvPath: monsters.csv
hungerGamesPath: /hungergames

# database
connectionUrl: "jdbc:mysql://localhost/milobot"
user: "root"
password: ""
```

Some fields like the connection url might already have a default value that depending on your own database needs to be changed.
The GitHub fields can be left empty but will result in a not working bug tracker command.

## Creating Commands

All commands must extend the `Command` class. This abstract class provides the basic functionality for every command. 
Commands are organized into categories, each category has its own directory and interface. If the command you're adding has
sub commands create a new directory for it. In this example il be adding a new utility command which belongs in the 
utility directory:

```java
public class ExampleCmd extends Command implements UtilityCmd {
    
    public ExampleCmd{
        
    }
    
}
```

Since we're extending the abstract class `Command` we inherit its functionality and properties. We can use them to 
configure our command:

```java
public class ExampleCmd extends Command implements UtilityCmd {

    public ExampleCmd{
        this.commandName = "example";
        this.commandDescription = "An example command.";
        this.commandArgs = new String[]{"argument1", "argument2*"};
        this.cooldown = 60;
        this.aliases = new String[]{"ex"};
        this.permissions.put("Administrator", Permission.ADMINISTRATOR);
    }
    
}
```

### Arguments

We can simply add arguments to a command by creating a new String array in the commands constructor: 

```java
this.commandArgs = new String[]{"argument1", "argument2*"};
```

The command handler automatically checks if all the required arguments (arguments marked without an *) are present and 
will send feedback to the user on how to properly call the command. However, we still need to retrieve the arguments 
ourselves. 

If you want to make an argument optional add an * after the name of the argument. If you add it two ** it tells the user
that the argument accepts multiple inputs.

### Cooldown

To add a cooldown, or time out to your command simple adjust the cooldown value inside the commands constructor:

```java
this.cooldown = 60;
```

This will make it so that the user who issued the command can only use it again after 60 seconds.

### Aliases

When your command name is long you might want to add an alias or shortcut so it's easier for the user to call. To do
this create a new String array in the commands constructor:

```java
this.aliases = new String[]{"ex", "shortcut"};
```

Just make sure that no other commands also use this alias.

### Permissions

Some commands like kicking and banning require the user to have server permissions. Commands can automatically check if 
the user has such permissions by adding them to the permissions map like so:

```java
this.permissions.put("Administrator", Permission.ADMINISTRATOR);
```

## Adding Command Functionality

### Regular Commands

To add functionality to our command we need to override the `executeCommand()` method:

```java
@Override
public void executeCommand(MessageReceivedEvent event, List<String> args) {
    
}
```

### Slash Commands

To add slash command functionality to our command we need to override the `executeSlashCommand()` method:

```java
@Override
public void executeSlashCommand(SlashCommandEvent event) {
	
}
```

## Loading Commands

### Regular Commands

To add a command to the bot we need to load it. This is done in the `CommandLoader` class found in the commands directory.
Simple add it to the list of commands in the `loadAllCommands()` method:

```java
commands.add(new ExampleCmd);
```

### Slash Commands

Slash commands require some additional work since they need to be registered to the api. Note that this might take a few
minutes to register. In our `CommandLoader` class we have a variable called `slashCommands`. We can simply add our slash 
command to this variable:

```java
slashCommands.addCommands(Commands.slash("example", "Our example command.")
    .addOption(OptionType.STRING, "argument", "A not required argument", false));
```

You can read more about this [here](https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java).

