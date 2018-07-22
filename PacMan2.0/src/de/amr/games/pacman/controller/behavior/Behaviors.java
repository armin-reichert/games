package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.controller.behavior.impl.Ambush;
import de.amr.games.pacman.ui.MazeMover;

public class Behaviors {

	public static MoveBehavior ambush(MazeMover<?> victim) {
		return new Ambush(victim);
	}

}
