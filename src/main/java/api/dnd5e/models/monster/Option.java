package api.dnd5e.models.monster;

import java.util.List;

public class Option {

	private long choose;
	private String type;
	private List<Choice> from;

	public Option() {
	}

	public long getChoose() {
		return choose;
	}

	public void setChoose(long choose) {
		this.choose = choose;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Choice> getFrom() {
		return from;
	}

	public void setFrom(List<Choice> from) {
		this.from = from;
	}
}
