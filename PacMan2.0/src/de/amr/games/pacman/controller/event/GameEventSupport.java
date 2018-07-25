package de.amr.games.pacman.controller.event;

import java.util.LinkedHashSet;
import java.util.Set;

public class GameEventSupport {

	private final Set<GameEventListener> observers = new LinkedHashSet<>();

	public void addObserver(GameEventListener observer) {
		observers.add(observer);
	}

	public void removeObserver(GameEventListener observer) {
		observers.remove(observer);
	}

	public void fireGameEvent(GameEvent event) {
		observers.forEach(observer -> observer.onGameEvent(event));
	}
}