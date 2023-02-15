return {
    name = "pear",
    rarity = 20,
    heals = 11,
    type = "consumable",
    description = "A green pear.",
    messages = {
        "%s ate an %s and felt invigorated. It healed %d hp, their hp is now %d.",
        "%s gobbled up a juicy %s. It restored %d hp, their hp is now %d.",
        "%s munched on a delicious %s. It recovered %d hp, their hp is now %d.",
        "%s savored a sweet %s. It brought their hp up by %d, their hp is now %d.",
    },
    onUse = function(self, lobbyEntry)
        lobbyEntry:heal(self.heals)
        lobbyEntry:removeItem(self.name)
        local message = self.messages[math.random(#self.messages)]
        game:log(string.format(message,
                lobbyEntry:getUserName(), self.name, self.heals, lobbyEntry:getHealth()))
    end,
}
