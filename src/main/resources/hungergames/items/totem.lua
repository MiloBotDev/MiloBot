return {
    name = "totem of undying",
    rarity = 1,
    type = "usable",
    onDeath = function(self, lobbyEntry)
        lobbyEntry:removeItem(self.name)
        lobbyEntry:heal(200)
        game:log(string.format("%s has used their 'totem of not being dead', and is now back alive!",
                lobbyEntry:getUserName()))
        return true
    end
}
