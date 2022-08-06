local damage = 40

return {
    name = "gun",
    rarity = 5,
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(damage) then
            game:log(string.format("%s shot %s and it looked pretty painful.",
                    lobbyEntry:getUserName(), victim:getUserName()))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s shoots their gun at  %s. It deals %d damage, their HP is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), damage, victim:getHealth()))
            lobbyEntry:addDamageDone(damage)
        end
    end,
}
