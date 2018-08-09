package de.amr.games.pacman.controller.event.core;

import java.util.LinkedHashSet;
import java.util.Set;

import de.amr.easy.game.Application;

public class EventManager<E> {

	private final String description;
	private final Set<Observer<E>> observers = new LinkedHashSet<>();

	public EventManager(String description) {
		this.description = description;
	}

	public void subscribe(Observer<E> observer) {
		observers.add(observer);
	}

	public void unsubscribe(Observer<E> observer) {
		observers.remove(observer);
	}

	public void publish(E event) {
		observers.forEach(observer -> observer.observe(event));
		Application.LOG.info(String.format("%s published event '%s'", description, event));
	}
}