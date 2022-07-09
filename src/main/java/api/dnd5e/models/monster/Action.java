package api.dnd5e.models.monster;

public class Action {

	private String name;
	private String description;
	private Option options;
	private long attackBonus;
	private Dice dice;

	public Action() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Option getOptions() {
		return options;
	}

	public void setOptions(Option options) {
		this.options = options;
	}

	public long getAttackBonus() {
		return attackBonus;
	}

	public void setAttackBonus(long attackBonus) {
		this.attackBonus = attackBonus;
	}
}
