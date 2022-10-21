# MiloBot

Play games with your friends, earn Morbcoins and climb up the leaderboards.

## Contributing

Please read our [contributing](https://github.com/RubenJ01/MiloBot/blob/master/docs/contributing.md) guide before submitting
a pull request.

## Commands

| **Utility**                  |                          |                    |                  |
|------------------------------|--------------------------|--------------------|------------------|
| [!prefix](#prefix)           | [!usage](#usage)         | [!help](#help)     | [!user](#user)   |
| [!invite](#invite)           |                          |                    |                  |
| **Morbconomy**               |                          |                    |                  |
| [!wallet](#wallet)           | [!profile](#profile)     | [!daily](#daily)   |                  |
| **Games**                    |                          |                    |                  |
| [!hungergames](#hungergames) | [!blackjackGame](#blackjackGame) | [!wordle](#wordle) | [!pokerGame](#pokerGame) |
| **Bot**                      |                          |                    |                  |
| [!bug](#bug)                 | [!status](#status)       |                    |                  |
| **Dungeons & Dragons**       |                          |                    |                  |
| [!encounter](#encounter)     |                          |                    |                  |

---

<h3 id="wallet">wallet</h3>

Check your wallet.

#### Usage

`!wallet`

---

<h3 id="hungergames">hungergames</h3>

Hunger Games

#### Usage

`!hungergames`

#### Sub Commands

`!hungergames start {*maxPlayers}`
Starts the Hunger Games


`!hungergames stats`
View your own hungergames statistics.

---

<h3 id="blackjackGame">blackjackGame</h3>

Blackjack brought to discord.

#### Usage

`!blackjackGame`

#### Sub Commands

`!blackjackGame play {bet*}`
Play a game of blackjackGame on discord.


`!blackjackGame stats`
View your own blackjackGame statistics.


`!blackjackGame info`
A simple tutorial on the rules of blackjackGame.

---

<h3 id="prefix">prefix</h3>

Change the prefix of the guild you're in.

#### Usage

`!prefix {prefix}`


#### Cooldown

60 seconds.

#### Permissions

`Administrator`

---

<h3 id="usage">usage</h3>

See the amount of times each or a specific command has been used.

#### Usage

`!usage {*command}`


#### Cooldown

60 seconds.

---

<h3 id="profile">profile</h3>

View your own or someone else's profile.

#### Usage

`!profile {*user}`


---

<h3 id="pokerGame">pokerGame</h3>

5-card Poker brought to discord.

#### Usage

`!pokerGame`

#### Sub Commands

`!pokerGame play`
Play a game of pokerGame on discord.

---

<h3 id="encounter">encounter</h3>

D&D 5e encounter generator.

#### Usage

`!encounter`

#### Sub Commands

`!encounter generate {party size, party level, difficulty, *environment}`
Generate a random encounter for a given average party level, party size, difficulty and an optional environment.

---

<h3 id="help">help</h3>

Shows the user a list of available commands.

#### Usage

`!help {*command}`


---

<h3 id="wordle">wordle</h3>

Wordle brought to discord.

#### Usage

`!wordle`

#### Sub Commands

`!wordle play`
Play a game of wordle.


`!wordle leaderboard`
View the wordle leaderboards.


`!wordle stats`
View your own wordle statistics

---

<h3 id="bug">bug</h3>

Add bugs to the bots issue tracker, or view them.

#### Usage

`!bug`

#### Sub Commands

`!bug report`
Report a bug you found.


`!bug view {id}`
Lookup a specific bug on the issue tracker.


`!bug list`
Shows a list of all reported bugs.

---

<h3 id="daily">daily</h3>

Collect your daily reward.

#### Usage

`!daily`

#### Sub Commands

`!daily streak`
View your current streak.


`!daily claim`
Claim your daily reward.

---

<h3 id="invite">invite</h3>

Sends an invite link to add the bot to another server.

#### Usage

`!invite`

---

<h3 id="status">status</h3>

The status of the bot.

#### Usage

`!status`

#### Cooldown

60 seconds.

---

<h3 id="user">user</h3>

Shows information about a user.

#### Usage

`!user {*user}`