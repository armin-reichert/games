package de.amr.games.pacman.board;

public interface Tile {

	public static final char EMPTY = ' ';
	public static final char OUTSIDE = 'x';
	public static final char WALL = '#';
	public static final char DOOR = 'D';
	public static final char GHOSTHOUSE = 'G';
	public static final char PELLET = '.';
	public static final char ENERGIZER = 'O';
	public static final char TUNNEL = 'T';
	public static final char WORMHOLE = 'W';
	// Bonus tiles:
	public static final char BONUS_CHERRIES = '1';
	public static final char BONUS_STRAWBERRY = '2';
	public static final char BONUS_PEACH = '3';
	public static final char BONUS_APPLE = '4';
	public static final char BONUS_GRAPES = '5';
	public static final char BONUS_GALAXIAN = '6';
	public static final char BONUS_BELL = '7';
	public static final char BONUS_KEY = '8';
}