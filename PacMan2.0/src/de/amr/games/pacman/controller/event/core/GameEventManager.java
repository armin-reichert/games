package de.amr.games.pacman.controller.event.core;

import java.util.LinkedHashSet;
import java.util.Set;

import de.amr.easy.game.Application;

public class GameEventManager {

	private final String description;
	private final Set<GameEventListener> observers = new LinkedHashSet<>();

	public GameEventManager(String description) {
		this.description = description;
	}

	public void subscribe(GameEventListener observer) {
		observers.add(observer);
	}

	public void unsubscribe(GameEventListener observer) {
		observers.remove(observer);
	}

	public void publish(GameEvent event) {
		observers.forEach(observer -> observer.onGameEvent(event));
		Application.LOG.info(String.format("%s fired event '%s'", description, event));
	}
}