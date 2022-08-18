local damage = 50

return {
    name = "land mine",
    rarity = 3,
    onTrigger = function(self, lobbyEntry)
        lobbyEntry:getHealth()
        if lobbyEntry:damage(damage) then
            game:log(string.format("%s stepped on a land mine and did not live to see another day.",
                    lobbyEntry:getUserName()))
            lobbyEntry:onDeath()
        else
            game:log(string.format("%s stepped on a landmine and took %d damage. Their hp is now %d.",
                    lobbyEntry:getUserName(), damage, lobbyEntry:getHealth()))
        end
    end,
}
