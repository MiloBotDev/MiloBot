damage = 50

return {
    name = "bomb",
    rarity = 5,
    onUse = function(self, player)
        player:removeItem(self.name)
        victim = game:getRandomPlayer(player)
        if victim:damage(damage) then
            game:log(string.format("%s killed %s using their %s.",
                    player:getUserName(), victim:getUserName(), self.name))
            victim:onDeath();
        else
            game:log(string.format("%s is done messing around and throws a %s at %s. It deals %d damage, their HP is now %d.",
                    player:getUserName(), self.name, victim:getUserName(), damage, victim:getHealth()))
        end
    end,
}