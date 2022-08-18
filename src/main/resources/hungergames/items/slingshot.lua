local damage = 10

return {
    name = "slingshot",
    rarity = 20,
    type = "usable",
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(damage) then
            game:log(string.format("%s loads up their slingshot with their own feces and kills %s with it.",
                    lobbyEntry:getUserName(), victim:getUserName()))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s loads up their slingshot with a rock and fires at %s. It deals %d damage, their hp is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), damage, victim:getHealth()))
            lobbyEntry:addDamageDone(damage)
        end
    end,
}
