package api.dnd5e.models;

public class Dice {

	private DiceType diceType;
	private long diceValue;
	private String successType;

	public Dice() {
	}

	public DiceType getDiceType() {
		return diceType;
	}

	public void setDiceType(DiceType diceType) {
		this.diceType = diceType;
	}

	public long getDiceValue() {
		return diceValue;
	}

	public void setDiceValue(long diceValue) {
		this.diceValue = diceValue;
	}

	public String getSuccessType() {
		return successType;
	}

	public void setSuccessType(String successType) {
		this.successType = successType;
	}
}
