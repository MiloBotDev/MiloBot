package api.dnd5e.models;

import java.util.List;

public class Monster {

	private String index;
	private String name;
	private String url;
	private long charisma;
	private long constitution;
	private long dexterity;
	private long intelligence;
	private long strength;
	private long wisdom;
	private String size;
	private String type;
	private String subtype;
	private String alignment;
	private int armor_class;
	private int hit_points;
	private int hit_dice;
	private List<Action> actions;

	public Monster() {
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("{\n");
		string.append("\tindex: " + index + "\n");
		string.append("\tname: " + name + "\n");
		string.append("\tsize: " + size + "\n");
		string.append("\ttype: " + type + "\n");
		string.append("\talignment: " + alignment + "\n");
		string.append("\tarmor_class: " + armor_class + "\n");
		string.append("\thit_points: " + hit_points + "\n");
		string.append("\thit_dice: " + hit_dice + "\n");
		string.append("\tactions: [\n");
		for (Action action : actions) {
			string.append("\t\t{\n");
			string.append("\t\tname: " + action.getName() + "\n");
			string.append("\t\tdescription: " + action.getDescription() + "\n");
			string.append("\t\tattack_bonus:" + action.getAttackBonus() + "\n");
			if(action.getOptions() != null) {
				string.append("\t\toptions: {\n");
				string.append("\t\t\tchoose: " + action.getOptions().getChoose() + "\n");
				string.append("\t\t\tfrom: [\n");
				for(Choice choice : action.getOptions().getFrom()) {
					string.append("\t\t\t\t" + choice.getIndex() + ": {\n");
					string.append("\t\t\t\t\tname: " + choice.getName() + "\n");
					string.append("\t\t\t\t\tcount: " + choice.getCount() + "\n");
					string.append("\t\t\t\t\ttype: " + choice.getType() + "\n");
					string.append("\t\t\t\t}\n");
				}
			}
			string.append("\t\t}\n");
		}

		string.append("}");
		return string.toString();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getCharisma() {
		return charisma;
	}

	public void setCharisma(long charisma) {
		this.charisma = charisma;
	}

	public long getConstitution() {
		return constitution;
	}

	public void setConstitution(long constitution) {
		this.constitution = constitution;
	}

	public long getDexterity() {
		return dexterity;
	}

	public void setDexterity(long dexterity) {
		this.dexterity = dexterity;
	}

	public long getIntelligence() {
		return intelligence;
	}

	public void setIntelligence(long intelligence) {
		this.intelligence = intelligence;
	}

	public long getStrength() {
		return strength;
	}

	public void setStrength(long strength) {
		this.strength = strength;
	}

	public long getWisdom() {
		return wisdom;
	}

	public void setWisdom(long wisdom) {
		this.wisdom = wisdom;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public int getArmor_class() {
		return armor_class;
	}

	public void setArmor_class(int armor_class) {
		this.armor_class = armor_class;
	}

	public int getHit_points() {
		return hit_points;
	}

	public void setHit_points(int hit_points) {
		this.hit_points = hit_points;
	}

	public int getHit_dice() {
		return hit_dice;
	}

	public void setHit_dice(int hit_dice) {
		this.hit_dice = hit_dice;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
}
