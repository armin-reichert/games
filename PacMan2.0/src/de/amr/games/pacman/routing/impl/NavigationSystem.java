package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.routing.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

public interface NavigationSystem {

	public static RoutePlanner ambush(MazeMover<?> victim) {
		return new Ambush(victim);
	}

	public static RoutePlanner bounce() {
		return new Bounce();
	}

	public static RoutePlanner chase(MazeMover<?> victim) {
		return new Chase(victim);
	}

	public static RoutePlanner flee(MazeMover<?> chaser) {
		return new Flee(chaser);
	}

	public static RoutePlanner followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static RoutePlanner forward() {
		return new Forward();
	}

	public static RoutePlanner goHome() {
		return new GoHome();
	}

	public static RoutePlanner moody() {
		return new Moody();
	}

	public static RoutePlanner stayBehind() {
		return new StayBehind();
	}
}
