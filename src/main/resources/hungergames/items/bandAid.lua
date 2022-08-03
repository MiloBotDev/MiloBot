heals = 20

return {
    name = "band aid",
    rarity = 10,
    onUse = function(self, player)
        player:heal(heals)
        player:removeItem(self.name)
        game:log(string.format("%s used a %s. It heals %d hp.",
                player:getUserName(), self.name, heals))
    end,
}
