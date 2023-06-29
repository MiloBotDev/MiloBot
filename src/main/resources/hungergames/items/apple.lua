return {
    name = "apple",
    rarity = 20,
    heals = 10,
    description = "A red apple.",
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s ate an %s. It healed %d hp.",
                lobbyEntry:getUserName(), self.name, self.heals))
    end,
}
