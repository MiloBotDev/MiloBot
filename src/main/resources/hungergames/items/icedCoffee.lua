local heals = 7

return {
    name = "iced coffee",
    rarity = 10,
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s drank an %s. It healed %d hp, their hp is now %d.",
                lobbyEntry:getUserName(), self.name, heals, lobbyEntry:getHealth()))
    end,
}
