package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;

public class ServerCmd extends Command implements UtilityCmd {

    public ServerCmd() {
        this.commandName = "server";
        this.commandDescription = "Shows information on the guild you are using this command in.";
        this.aliases = new String[]{"guild"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        EmbedBuilder embedBuilder = generateGuildEmbed(event.getGuild(), event.getAuthor());
        event.getChannel().sendMessageEmbeds(embedBuilder.build())
                .setActionRow(Button.secondary(event.getAuthor().getId() + ":delete", "Delete"))
                .queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        EmbedBuilder embedBuilder = generateGuildEmbed(event.getGuild(), event.getUser());
        event.replyEmbeds(embedBuilder.build())
                .addActionRow(Button.secondary(event.getUser().getId() + ":delete", "Delete"))
                .queue();
    }

    private @NotNull EmbedBuilder generateGuildEmbed(@NotNull Guild guild, User user) {
        EmbedBuilder guildEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(guildEmbed, user);

        int boostCount = guild.getBoostCount();
        guildEmbed.addField("Boosts'", String.valueOf(boostCount), true);
        int categoryCount = guild.getCategories().size();
        guildEmbed.addField("Categories", String.valueOf(categoryCount), true);
        int textChannelsSize = guild.getTextChannels().size();
        guildEmbed.addField("Text Channels", String.valueOf(textChannelsSize), true);
        int voiceChannelsSize = guild.getVoiceChannels().size();
        guildEmbed.addField("Voice Channels", String.valueOf(voiceChannelsSize), true);
        int memberCount = guild.getMemberCount();
        guildEmbed.addField("Members", String.valueOf(memberCount), true);
        int roleCount = guild.getRoles().size();
        guildEmbed.addField("Roles", String.valueOf(roleCount), true);
        int emoteCount = guild.getEmotes().size();
        guildEmbed.addField("Emotes", String.valueOf(emoteCount), true);

        String iconUrl = guild.getIconUrl();
        guildEmbed.setThumbnail(iconUrl);
        String bannerUrl = guild.getBannerUrl();
        guildEmbed.setImage(bannerUrl);

        return guildEmbed;
    }

}
