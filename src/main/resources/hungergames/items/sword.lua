damage = 20

return {
    name = "sword",
    rarity = 20,
    onUse = function(self, player)
        victim = game:getRandomPlayer()
        if victim:damage(damage) then
            game:log(string.format("%s killed %s using their %s.",
                    player:getUserName(), victim:getUserName(), self.name))
            victim:onDeath();
        else
            game:log(string.format("%s is swinging their sword at %s. It deals %d damage, their HP is now %d.",
                    player:getUserName(), victim:getUserName(), damage, victim:getHealth()))
        end
    end,
}
