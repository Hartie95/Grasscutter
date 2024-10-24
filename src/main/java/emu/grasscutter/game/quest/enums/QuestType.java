package emu.grasscutter.game.quest.enums;

public enum QuestType {
    /**
     * Archon quest
     */
	AQ (0),
	FQ (1),
    /**
     * Legendary/character Quest
     */
	LQ (2),
    /**
     * Event quest
     */
	EQ (3),
	DQ (4),
    /**
     * daily quest?
     */
	IQ (5),
	VQ (6),
    /**
     * World quest
     */
	WQ (7);

	private final int value;

	QuestType(int id) {
		this.value = id;
	}

	public int getValue() {
		return value;
	}
}
