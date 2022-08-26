return {
    name = "band aid",
    rarity = 10,
    heals = 30,
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s used a %s. It heals %d hp.",
                lobbyEntry:getUserName(), self.name, self.heals))
    end,
}
