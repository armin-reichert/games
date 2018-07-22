package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.ui.MazeMover;

public interface Behaviors {

	public static MoveBehavior ambush(MazeMover<?> victim) {
		return new Ambush(victim);
	}

	public static MoveBehavior bounce() {
		return new Bounce();
	}

	public static MoveBehavior chase(MazeMover<?> victim) {
		return new Chase(victim);
	}

	public static MoveBehavior flee(MazeMover<?> chaser) {
		return new Flee(chaser);
	}

	public static MoveBehavior followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static MoveBehavior forward() {
		return new Forward();
	}

	public static MoveBehavior goHome() {
		return new GoHome();
	}

	public static MoveBehavior moody() {
		return new Moody();
	}

	public static MoveBehavior stayBehind() {
		return new StayBehind();
	}
}
