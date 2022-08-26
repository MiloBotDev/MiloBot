return {
    name = "infinity gauntlet",
    rarity = 1,
    type = "usable",
    onUse = function(self, lobbyEntry)
        lobbyEntry:removeItem(self.name)
        game:log(string.format("%s used their infinity gauntlet.",
                lobbyEntry:getUserName()))
        local died = math.floor((game:getAlivePlayers():size() + 1) / 2)
        if died == 0 then
            game:log(string.format("it had no effect..."))
        else
            for i = 1, died, 1 do
                victim = game:getRandomPlayer(lobbyEntry)
                game:log(string.format("%s doesn't feel so good.", victim:getUserName()))
                victim:onDeath()
                lobbyEntry:addKill()
            end
        end
        return true
    end,
}
