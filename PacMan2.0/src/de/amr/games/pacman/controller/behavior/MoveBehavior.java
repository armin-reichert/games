package de.amr.games.pacman.controller.behavior;

import java.util.function.Function;

import de.amr.games.pacman.ui.MazeMover;

public interface MoveBehavior extends Function<MazeMover<?>, MoveData>
{

}
