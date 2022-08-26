return {
    name = "cliff drop",
    rarity = 1,
    onTrigger = function(self, lobbyEntry)
        game:log(string.format("%s fell off a cliff to their death.",
                lobbyEntry:getUserName()))
        lobbyEntry:onDeath()
    end,
}
