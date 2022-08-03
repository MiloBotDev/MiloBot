heals = 10

return {
    name = "apple",
    rarity = 20,
    onUse = function(self, player)
        player:heal(heals)
        player:removeItem(self.name)
        game:log(string.format("%s ate an %s. It healed %d hp.",
                player:getUserName(), self.name, heals))
    end,
}
