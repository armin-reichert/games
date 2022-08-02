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

/**
 * @author Armin Reichert
 */
public class BattleshipGame {

	public static final int MAPSIZE = 10;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	public static final byte MAP_WATER = -127;
	public static final byte MAP_CARRIER = 0;
	public static final byte MAP_BATTLESHIP = 1;
	public static final byte MAP_CRUISER = 2;
	public static final byte MAP_SUBMARINE = 3;
	public static final byte MAP_DESTROYER = 4;

	private static final int[] SHIP_SIZES = { 5, 4, 3, 3, 2 };
	private static final char[] SHIP_CODES = { 'C', 'B', 'U', 'S', 'D' };

	public static final int PLAYER1 = 0;
	public static final int PLAYER2 = 1;

	public static int shipSize(byte type) {
		return SHIP_SIZES[type];
	}

	public static char shipCode(byte type) {
		return SHIP_CODES[type];
	}

	public static void message(String msg, Object... args) {
		System.out.println(msg.formatted(args));
	}

	public PlayerData[] playerData = new PlayerData[2];

	public BattleshipGame() {
		playerData[0] = new PlayerData();
		playerData[1] = new PlayerData();
	}

	public PlayerData playerData(int player) {
		if (player == PLAYER1) {
			return playerData[PLAYER1];
		} else if (player == PLAYER2) {
			return playerData[PLAYER2];
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void resetPlayer(int player) {
		if (player == PLAYER1) {
			playerData[PLAYER1].reset();
		} else if (player == PLAYER2) {
			playerData[PLAYER2].reset();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void deleteShip(int player, byte type) {
		if (!playerData(player).shipUsed[type]) {
			return;
		}
		for (int x = 0; x < MAPSIZE; ++x) {
			for (int y = 0; y < MAPSIZE; ++y) {
				byte value = playerData(player).map[x][y];
				if (value == type) {
					playerData(player).map[x][y] = MAP_WATER;
				}
			}
		}
		playerData(player).shipUsed[type] = false;

	}

	public boolean addShip(int player, byte type, int x, int y, int orientation) {
		if (orientation == HORIZONTAL) {
			return addShip(playerData[player], type, x, y, shipSize(type), 1);
		} else {
			return addShip(playerData[player], type, x, y, 1, shipSize(type));
		}
	}

	private boolean addShip(PlayerData playerData, byte type, int x, int y, int sizeX, int sizeY) {
		if (playerData.shipUsed[type]) {
			return false;
		}
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
				byte value = playerData.map[x + i][y + j];
				if (value != MAP_WATER) {
					return false;
				}
				playerData.map[x + i][y + j] = type;
			}
		}
		playerData.shipUsed[type] = true;
		return true;
	}
}