return {
    name = "boba",
    rarity = 8,
    heals = 7,
    description = "Bubble thea.",
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s drank some %s. It healed %d hp, their hp is now %d.",
                lobbyEntry:getUserName(), self.name, self.heals, lobbyEntry:getHealth()))
    end,
}
