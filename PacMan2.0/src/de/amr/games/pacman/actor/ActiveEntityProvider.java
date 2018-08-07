package de.amr.games.pacman.actor;

import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;

public interface ActiveEntityProvider {
	
	Stream<GameEntity> activeEntities();

}
