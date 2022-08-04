local heals = 10

return {
    name = "apple",
    rarity = 20,
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s ate an %s. It healed %d hp.",
                lobbyEntry:getUserName(), self.name, heals))
    end,
}
