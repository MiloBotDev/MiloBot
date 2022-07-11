package utility;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.*;

public class Paginator {

	public final static Map<String, Paginator> paginatorInstances = new HashMap<>();

	private final List<EmbedBuilder> pages;
	private int currentPage;

	public Paginator(EmbedBuilder embed) {
		pages = new ArrayList<>();
		pages.add(embed);
		this.currentPage = 0;
	}

	public void initialize(String messageId) {
		paginatorInstances.put(messageId, this);
	}

	public void addPage(EmbedBuilder embed) {
		pages.add(embed);
	}

	public Optional<EmbedBuilder> nextPage() {
		if(currentPage + 1 < pages.size()) {
			currentPage++;
			return Optional.of(pages.get(currentPage));
		}
		return Optional.empty();
	}

	public Optional<EmbedBuilder> previousPage() {
		if(currentPage - 1 >= 0) {
			currentPage--;
			return Optional.of(pages.get(currentPage));
		}
		return Optional.empty();
	}

	public EmbedBuilder currentPage() {
		return pages.get(currentPage);
	}
}
