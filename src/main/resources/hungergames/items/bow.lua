local damage = 30

return {
    name = "bow",
    rarity = 15,
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(damage) then
            game:log(string.format("%s was impaled from an arrow by %s's hands.",
                    victim:getUserName(), lobbyEntry:getUserName()))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s is firing their bow at %s. It deals %d damage, their HP is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), damage, victim:getHealth()))
            lobbyEntry:addDamageDone(damage)
        end
    end,
}
