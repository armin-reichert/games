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

	public static final byte MAP_CARRIER = 1;
	public static final byte MAP_BATTLESHIP = 2;
	public static final byte MAP_CRUISER = 3;
	public static final byte MAP_SUBMARINE = 4;
	public static final byte MAP_DESTROYER = 5;

	public static final byte MAP_CARRIER_HIT = -1;
	public static final byte MAP_BATTLESHIP_HIT = -2;
	public static final byte MAP_CRUISER_HIT = -3;
	public static final byte MAP_SUBMARINE_HIT = -4;
	public static final byte MAP_DESTROYER_HIT = -5;

	public static final int PLAYER1 = 0;
	public static final int PLAYER2 = 1;

	public static int shipSize(byte type) {
		return switch (type) {
		case MAP_CARRIER -> 5;
		case MAP_BATTLESHIP -> 4;
		case MAP_CRUISER -> 3;
		case MAP_SUBMARINE -> 3;
		case MAP_DESTROYER -> 2;
		default -> throw new IllegalArgumentException();
		};
	}

	public static char shipCode(byte type) {
		return switch (type) {
		case MAP_CARRIER -> 'C';
		case MAP_BATTLESHIP -> 'B';
		case MAP_CRUISER -> 'U';
		case MAP_SUBMARINE -> 'S';
		case MAP_DESTROYER -> 'D';
		default -> throw new IllegalArgumentException();
		};
	}

	private Player[] players = new Player[2];

	public BattleshipGame() {
		players[0] = new Player();
		players[1] = new Player();
	}

	public Player getPlayer(int player) {
		if (player == PLAYER1) {
			return players[PLAYER1];
		} else if (player == PLAYER2) {
			return players[PLAYER2];
		} else {
			throw new IllegalArgumentException();
		}
	}

	public Result deleteShip(int player, byte type) {
		if (!getPlayer(player).isShipUsed(type)) {
			return new Result(false, "Ship type not used yet");
		}
		for (int x = 0; x < MAPSIZE; ++x) {
			for (int y = 0; y < MAPSIZE; ++y) {
				byte value = getPlayer(player).map[x][y];
				if (value == type) {
					getPlayer(player).map[x][y] = MAP_WATER;
				}
			}
		}
		getPlayer(player).setShipUsed(type, false);
		return new Result(true, "");
	}

	public Result addShip(int player, byte type, int x, int y, int orientation) {
		if (orientation == HORIZONTAL) {
			return addShip(players[player], type, x, y, shipSize(type), 1);
		} else {
			return addShip(players[player], type, x, y, 1, shipSize(type));
		}
	}

	private Result addShip(Player playerData, byte type, int x, int y, int sizeX, int sizeY) {
		if (playerData.isShipUsed(type)) {
			return new Result(false, "Ship type already used");
		}
		if (x + sizeX > MAPSIZE) {
			return new Result(false, "Map size exceeded in x dimension");
		}
		if (y + sizeY > MAPSIZE) {
			return new Result(false, "Map size exceeded in x dimension");
		}
		for (int i = 0; i < sizeX; ++i) {
			for (int j = 0; j < sizeY; ++j) {
				byte value = playerData.map[x + i][y + j];
				if (value != MAP_WATER) {
					return new Result(false, "Map already used at " + new MapCoordinate(x, y).toLetterDigitFormat());
				}
				playerData.map[x + i][y + j] = type;
			}
		}
		playerData.setShipUsed(type, true);
		return new Result(true, "");
	}
}