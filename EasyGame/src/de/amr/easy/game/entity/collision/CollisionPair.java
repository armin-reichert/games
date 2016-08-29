package de.amr.easy.game.entity.collision;

import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;

public class CollisionPair {

	private final GameEntity either;
	private final GameEntity other;
	private Rectangle intersection;

	public CollisionPair(GameEntity x, GameEntity y) {
		this.either = x;
		this.other = y;
	}

	public GameEntity either() {
		return either;
	}

	public GameEntity other() {
		return other;
	}

	public Rectangle getIntersection() {
		return intersection;
	}

	public void setIntersection(Rectangle intersection) {
		this.intersection = intersection;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		CollisionPair pair = (CollisionPair) obj;
		return pair.either == either && pair.other == other
				|| pair.either == other && pair.other == either;
	}

	@Override
	public int hashCode() {
		return either.hashCode() + other.hashCode();
	}
}
