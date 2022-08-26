return {
    name = "bomb",
    rarity = 5,
    damage = 40,
    type = "usable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:removeItem(self.name)
        victim = game:getRandomPlayer(lobbyEntry)
        remainingHealth = victim:getHealth()
        if victim:damage(self.damage) then
            game:log(string.format("%s killed %s using their %s.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.name))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s is done messing around and throws a %s at %s. It deals %d damage, their HP is now %d.",
                    lobbyEntry:getUserName(), self.name, victim:getUserName(), self.damage, victim:getHealth()))
            lobbyEntry:addDamageDone(self.damage)
        end
    end,
    onDeath = function(self, lobbyEntry)
        victim = game:getRandomPlayer(lobbyEntry)
        if victim:damage(self.damage) then
            remainingHealth = victim:getHealth()
            game:log(string.format("%s had a bomb when they died, sadly %s was standing in the way and died as well.",
                    lobbyEntry:getUserName(), victim:getUserName()))
            lobbyEntry:addDamageDone(remainingHealth)
            lobbyEntry:addKill()
            victim:onDeath()
        else
            game:log(string.format("%s had a bomb when they died, sadly %s was standing in the way and took %d damage. Their hp is now %d.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.damage, victim:getHealth()))
            lobbyEntry:addDamageDone(self.damage)
        end
        return false
    end
}