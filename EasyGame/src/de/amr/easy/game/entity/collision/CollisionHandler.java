package de.amr.easy.game.entity.collision;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;

public enum CollisionHandler {

	INSTANCE;

	public static void detectCollisionStart(GameEntity x, GameEntity y, Object event) {
		INSTANCE.startMap.put(new CollisionPair(x, y), event);
	}

	public static void detectCollisionEnd(GameEntity x, GameEntity y, Object event) {
		INSTANCE.endMap.put(new CollisionPair(x, y), event);
	}

	public static void ignoreCollisionStart(GameEntity x, GameEntity y) {
		INSTANCE.startMap.remove(new CollisionPair(x, y));
	}

	public static void ignoreCollisionEnd(GameEntity x, GameEntity y) {
		INSTANCE.endMap.remove(new CollisionPair(x, y));
	}

	public static Iterable<Collision> collisions() {
		return INSTANCE.events;
	}

	public static void update() {
		INSTANCE.updateInternal();
	}

	public static void clear() {
		INSTANCE.clearInternal();
	}

	private final Map<CollisionPair, Object> startMap = new HashMap<>();
	private final Map<CollisionPair, Object> endMap = new HashMap<>();
	private final Set<CollisionPair> newCollisions = new HashSet<>();
	private final Set<CollisionPair> oldCollisions = new HashSet<>();
	private final Set<Collision> events = new HashSet<>();

	private void clearInternal() {
		newCollisions.clear();
		oldCollisions.clear();
		events.clear();
		startMap.clear();
		endMap.clear();
	}

	private void updateInternal() {
		oldCollisions.clear();
		oldCollisions.addAll(newCollisions);
		newCollisions.clear();
		events.clear();
		for (CollisionPair pair : startMap.keySet()) {
			if (checkCollision(pair)) {
				newCollisions.add(pair);
			}
		}
		for (CollisionPair pair : endMap.keySet()) {
			if (checkCollision(pair)) {
				newCollisions.add(pair);
			}
		}
		for (CollisionPair pair : startMap.keySet()) {
			if (newCollisions.contains(pair) && !oldCollisions.contains(pair)) {
				Collision event = new Collision(pair.either(), pair.other(), pair.getIntersection(),
						startMap.get(pair), true);
				events.add(event);
				Application.LOG.fine(event.toString());
			}
		}
		for (CollisionPair pair : endMap.keySet()) {
			if (!newCollisions.contains(pair) && oldCollisions.contains(pair)) {
				Collision event = new Collision(pair.either(), pair.other(), pair.getIntersection(),
						endMap.get(pair), false);
				events.add(event);
				Application.LOG.fine(event.toString());
			}
		}
	}

	private static boolean checkCollision(CollisionPair p) {
		Rectangle intersection = p.either().getCollisionBox().intersection(p.other().getCollisionBox());
		if (!intersection.isEmpty()) {
			p.setIntersection(intersection);
			return true;
		}
		return false;
	}
}
