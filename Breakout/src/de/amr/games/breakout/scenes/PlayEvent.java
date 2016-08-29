package de.amr.games.breakout.scenes;

import de.amr.easy.game.entity.collision.Collision;

public enum PlayEvent {
	Tick, BallHitsBrick, BallHitsBat;

	public Collision collision;
}