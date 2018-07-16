package de.amr.games.pacman.controller;

public interface Brain<E> {

	int recommendNextMoveDirection(E entity);
}
