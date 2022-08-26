return {
    name = "bow",
    rarity = 15,
    damage = 20,
    type = "usable",
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(self.damage) then
            game:log(string.format("%s was impaled from an arrow by %s's hands.",
                    victim:getUserName(), lobbyEntry:getUserName()))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s is firing their bow at %s. It deals %d damage, their HP is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.damage, victim:getHealth()))
            lobbyEntry:addDamageDone(self.damage)
        end
    end,
}
