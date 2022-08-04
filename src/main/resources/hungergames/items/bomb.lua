local damage = 40

return {
    name = "bomb",
    rarity = 5,
    onUse = function(self, lobbyEntry)
        lobbyEntry:removeItem(self.name)
        victim = game:getRandomPlayer(lobbyEntry)
        if victim:damage(damage) then
            lobbyEntry:addKill()
            game:log(string.format("%s killed %s using their %s.",
                    lobbyEntry:getUserName(), victim:getUserName(), self.name))
            victim:onDeath()
        else
            game:log(string.format("%s is done messing around and throws a %s at %s. It deals %d damage, their HP is now %d.",
                    lobbyEntry:getUserName(), self.name, victim:getUserName(), damage, victim:getHealth()))
        end
        lobbyEntry:addDamageDone(damage)
    end,
}