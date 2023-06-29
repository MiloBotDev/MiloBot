return {
    name = "iced coffee",
    rarity = 10,
    heals = 7,
    type = "consumable",
    description = "Its iced coffee.",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s drank an %s. It healed %d hp, their hp is now %d.",
                lobbyEntry:getUserName(), self.name, self.heals, lobbyEntry:getHealth()))
    end,
}
