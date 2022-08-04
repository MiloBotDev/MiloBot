local heals = 30

return {
    name = "band aid",
    rarity = 10,
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s used a %s. It heals %d hp.",
                lobbyEntry:getUserName(), self.name, heals))
    end,
}
