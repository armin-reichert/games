package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.Navigation;

public interface NavigationSystem {

	public static Navigation<MazeMover<?>> ambush(MazeMover<?> victim) {
		return new Ambush(victim);
	}

	public static Navigation<MazeMover<?>> bounce() {
		return new Bounce();
	}

	public static Navigation<MazeMover<?>> chase(MazeMover<?> victim) {
		return new Chase(victim);
	}

	public static Navigation<MazeMover<?>> flee(MazeMover<?> chaser) {
		return new Flee(chaser);
	}

	public static Navigation<MazeMover<?>> followKeyboard(int... nesw) {
		return new FollowKeyboard(nesw);
	}

	public static Navigation<MazeMover<?>> forward() {
		return new Forward();
	}

	public static Navigation<MazeMover<?>> goHome() {
		return new GoHome();
	}

	public static Navigation<MazeMover<?>> moody() {
		return new Moody();
	}

	public static Navigation<MazeMover<?>> stayBehind() {
		return new StayBehind();
	}
}
