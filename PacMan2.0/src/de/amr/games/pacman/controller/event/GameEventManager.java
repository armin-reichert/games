package de.amr.games.pacman.controller.event;

import java.util.LinkedHashSet;
import java.util.Set;

public class GameEventManager {

	private final Set<GameEventListener> observers = new LinkedHashSet<>();

	public void subscribe(GameEventListener observer) {
		observers.add(observer);
	}

	public void unsubscribe(GameEventListener observer) {
		observers.remove(observer);
	}

	public void publish(GameEvent event) {
		observers.forEach(observer -> observer.onGameEvent(event));
	}
}