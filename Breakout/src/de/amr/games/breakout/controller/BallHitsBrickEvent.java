package de.amr.games.breakout.controller;

import de.amr.games.breakout.entities.Brick;

public class BallHitsBrickEvent implements PlayEvent {

	public final Brick brick;

	public BallHitsBrickEvent(Brick brick) {
		this.brick = brick;
	}
}