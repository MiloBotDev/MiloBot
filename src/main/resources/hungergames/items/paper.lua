return {
    name = "paper",
    rarity = 5,
    damage = 6,
    type = "usable",
    onUse = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(self.damage) then
            game:log(string.format("%s killed %s using their %s.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.name))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s uses their %s to give %s a paper cut. It deals %d damage, their hp is now %d.",
                    lobbyEntry:getUserName(), self.name ,victim:getUserName(), self.damage, victim:getHealth()))
            lobbyEntry:addDamageDone(self.damage)
        end
    end,
}