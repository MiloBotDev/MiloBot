local damage = 5

return {
    name = "banana peel",
    rarity = 20,
    onTrigger = function(self, lobbyEntry)
        lobbyEntry:getHealth()
        if lobbyEntry:damage(damage) then
            game:log(string.format("%s slipped over a banana peel and died.",
                    lobbyEntry:getUserName()))
            lobbyEntry:onDeath()
        else
            game:log(string.format("%s slipped over a banana peel and took %d damage. Their hp is now %d.",
                    lobbyEntry:getUserName(), damage, lobbyEntry:getHealth()))
        end
    end,
}
