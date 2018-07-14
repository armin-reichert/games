package de.amr.games.pacman.control;

public class StartLevelEvent implements GameEvent {

	public final int level;

	public StartLevelEvent(int level) {
		this.level = level;
	}
}
