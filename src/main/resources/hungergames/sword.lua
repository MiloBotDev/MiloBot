damage = 20

return {
    name = "sword",
    rarity = 20,
    onUse = function(player)
        enemy = game:getRandomPlayer()
        player:damage(damage)
        game:log(string.format("%s is swinging their sword at %s. It deals %d damage, their HP is now %d.",
                player:getUserName(), enemy:getUserName(), damage, player:getHealth()))
    end,
}
