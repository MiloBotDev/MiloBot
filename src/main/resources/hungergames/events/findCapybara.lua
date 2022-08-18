local heals = 5

return {
    name = "find capybara",
    rarity = 2,
    onTrigger = function(self, lobbyEntry)
        lobbyEntry:heal(heals)
        game:log(string.format("A Capybara pulled up to %s together they pulled up and healed %d hp.",
                lobbyEntry:getUserName(), heals))
    end,
}
