return {
    name = "totem of not being dead",
    rarity = 1,
    onDeath = function(self, player)
        player:removeItem(self.name)
        game:log(string.format("%s has used their 'totem of not being dead', and is now back alive!",
                player:getUserName()))
        return true
    end
}
