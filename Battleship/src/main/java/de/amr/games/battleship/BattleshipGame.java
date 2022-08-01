/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.battleship;

import java.io.PrintWriter;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class BattleshipGame {

	public static final int MAPSIZE = 10;

	public static final byte MAP_WATER = -1;
	public static final byte MAP_CARRIER = 0;
	public static final byte MAP_BATTLESHIP = 1;
	public static final byte MAP_CRUISER = 2;
	public static final byte MAP_SUBMARINE = 3;
	public static final byte MAP_DESTROYER = 4;

	private static final int[] SHIP_SIZES = { 5, 4, 3, 3, 2 };
	private static final char[] SHIP_CODES = { 'C', 'B', 'U', 'S', 'D' };
	private static final List<String> SHIP_TYPE_NAMES = List.of("carrier", "battleship", "cruiser", "submarine",
			"destroyer");

	public static final int PLAYER1 = 0;
	public static final int PLAYER2 = 1;

	public static class PlayerData {
		public final byte[][] map = new byte[MAPSIZE][MAPSIZE];
		public final boolean[] shipUsed = new boolean[5];

		public PlayerData() {
			reset();
		}

		public void reset() {
			for (int x = 0; x < MAPSIZE; ++x) {
				for (int y = 0; y < MAPSIZE; ++y) {
					map[x][y] = MAP_WATER;
				}
			}
			for (int i = 0; i < shipUsed.length; ++i) {
				shipUsed[i] = false;
			}
		}
	}

	public static int shipSize(byte type) {
		return SHIP_SIZES[type];
	}

	public static char shipCode(byte type) {
		return SHIP_CODES[type];
	}

	public static String shipTypeName(byte type) {
		return SHIP_TYPE_NAMES.get(type);
	}

	public static byte shipType(String shipType) {
		int index = SHIP_TYPE_NAMES.indexOf(shipType.toLowerCase());
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return (byte) (index);
	}

	public static boolean isValidOrientation(String orientation) {
		return "h".equalsIgnoreCase(orientation) || "v".equalsIgnoreCase(orientation);
	}

	public static boolean isValidShipType(String shipType) {
		return SHIP_TYPE_NAMES.stream().anyMatch(name -> name.equalsIgnoreCase(shipType));
	}

	public static boolean isValidCoordinate(String coordinate) {
		return MapCoordinate.valueOf(coordinate) != null;
	}

	public static void message(String msg, Object... args) {
		System.out.println(msg.formatted(args));
	}

	public PlayerData[] playerData = new PlayerData[2];

	public BattleshipGame() {
		playerData[0] = new PlayerData();
		playerData[1] = new PlayerData();
	}

	public boolean addShipHori(int player, byte type, int x, int y) {
		message("Player %d: %s horizontal at %s", player, shipTypeName(type),
				new MapCoordinate(x, y).toLetterDigitFormat());
		return addShip(playerData[player].map, type, x, y, shipSize(type), 1);
	}

	public boolean addShipVert(int player, byte type, int x, int y) {
		message("Player %d: %s vertical at %s", player, shipTypeName(type), new MapCoordinate(x, y).toLetterDigitFormat());
		return addShip(playerData[player].map, type, x, y, 1, shipSize(type));
	}

	private boolean addShip(byte[][] map, byte type, int x, int y, int sizeX, int sizeY) {
		if (x + sizeX > MAPSIZE) {
			message("Cannot place ship. x exceeds map");
			return false;
		}
		if (y + sizeY > MAPSIZE) {
			message("Cannot place ship. y exceeds map");
			return false;
		}
		for (int i = 0; i < sizeX; ++i) {
			for (int j = 0; j < sizeY; ++j) {
				byte value = map[x + i][y + j];
				if (value != MAP_WATER) {
					message("Cannot place ship. Overlaps with %s at %s", shipTypeName(value),
							new MapCoordinate(x, y).toLetterDigitFormat());
					return false;
				}
				map[x + i][y + j] = type;
			}
		}
		return true;
	}

	public void printPlayer(int player) {
		var playerName = player == PLAYER1 ? "Player #1" : "Player #2";
		message("\n        %s", playerName);
		printMap(playerData[player].map, new PrintWriter(System.out, true));
	}

	private void printMap(byte[][] map, PrintWriter w) {
		w.print("  ");
		for (int i = 1; i <= MAPSIZE; ++i) {
			w.print(i + " ");
		}
		w.println();
		for (int y = 0; y < MAPSIZE; ++y) {
			for (int x = 0; x < MAPSIZE; ++x) {
				byte value = map[x][y];
				char ch = value == MAP_WATER ? '~' : shipCode(value);
				if (x == 0) {
					w.print(MapCoordinate.letter(y) + " ");
				}
				w.print(ch + " ");
			}
			w.println();
		}
		w.println();
	}
}