package de.amr.games.pacman.controller.event.core;

import static de.amr.easy.game.Application.LOG;

import java.util.LinkedHashSet;
import java.util.Set;

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
		LOG.info(String.format("%s publishing event '%s'", description, event));
		observers.forEach(observer -> observer.observe(event));
	}
}