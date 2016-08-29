package de.amr.easy.game.entity.collision;

import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;

public class Collision {

	private final GameEntity first;
	private final GameEntity second;
	private final Rectangle intersection;
	private final boolean collisionStart;
	private final Object appEvent;

	public Collision(GameEntity first, GameEntity second, Rectangle intersection, Object appEvent,
			boolean collisionStart) {
		this.first = first;
		this.second = second;
		this.intersection = intersection;
		this.appEvent = appEvent;
		this.collisionStart = collisionStart;
	}

	@Override
	public String toString() {
		return "Collision" + (collisionStart ? "Start" : "End") + "(" + first.getClass().getSimpleName()
				+ "<->" + second.getClass().getSimpleName() + ") -> " + appEvent;
	}

	@SuppressWarnings("unchecked")
	public <T extends GameEntity> T getFirst() {
		return (T) first;
	}

	@SuppressWarnings("unchecked")
	public <T extends GameEntity> T getSecond() {
		return (T) second;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAppEvent() {
		return (T) appEvent;
	}

	public boolean isCollisionStart() {
		return collisionStart;
	}

	public Rectangle getIntersection() {
		return intersection;
	}
}
