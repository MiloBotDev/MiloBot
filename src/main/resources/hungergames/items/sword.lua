local damage = 20

return {
    name = "sword",
    rarity = 20,
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(damage) then
            game:log(string.format("%s killed %s using their %s.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.name))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s is swinging their sword at %s. It deals %d damage, their hp is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), damage, victim:getHealth()))
            lobbyEntry:addDamageDone(damage)
        end
    end,
}
