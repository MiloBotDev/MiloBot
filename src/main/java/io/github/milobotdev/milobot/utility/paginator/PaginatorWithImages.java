package io.github.milobotdev.milobot.utility.paginator;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.List;
import java.util.function.BiFunction;

public class PaginatorWithImages extends Paginator {
    private List<byte[]> images;
    private BiFunction<Integer, MessageAction, MessageAction> imageAdder;

    public PaginatorWithImages(User creator, List<MessageEmbed> pages, BiFunction<Integer, MessageAction, MessageAction> imageAdder) {
        super(creator, pages);
        this.imageAdder = imageAdder;
    }

    private boolean first = false;

    @Override
    protected MessageAction getUpdateMessageAction() {
        return imageAdder.apply(getCurrentPage(), super.getUpdateMessageAction().retainFilesById(List.of("chart0.png")));

    }
}
