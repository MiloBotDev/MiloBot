local heals = 11

return {
    name = "pear",
    rarity = 20,
    type = "consumable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(heals)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s ate an %s. It healed %d hp, their hp is now %d.",
                lobbyEntry:getUserName(), self.name, heals, lobbyEntry:getHealth()))
    end,
}
