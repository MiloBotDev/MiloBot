return {
    name = "strawberry",
    rarity = 20,
    heals = 3,
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s ate an %s. It healed %d hp, their hp is now %d.",
                lobbyEntry:getUserName(), self.name, self.heals, lobbyEntry:getHealth()))
    end,
}
