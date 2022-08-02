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

import java.util.Arrays;

public class Player {

	public final byte[][] map = new byte[BattleshipGame.MAPSIZE][BattleshipGame.MAPSIZE];
	private final int[] shipsUsed = new int[5];

	public Player() {
		reset();
	}

	public void reset() {
		for (int x = 0; x < BattleshipGame.MAPSIZE; ++x) {
			for (int y = 0; y < BattleshipGame.MAPSIZE; ++y) {
				map[x][y] = BattleshipGame.MAP_WATER;
			}
		}
		Arrays.fill(shipsUsed, 0);
	}

	public Result addShip(byte type, int x, int y, int orientation) {
		if (orientation == BattleshipGame.HORIZONTAL) {
			return addShip(type, x, y, BattleshipGame.shipSize(type), 1);
		} else {
			return addShip(type, x, y, 1, BattleshipGame.shipSize(type));
		}
	}

	private Result addShip(byte type, int x, int y, int sizeX, int sizeY) {
		if (numShipsUsed(type) == BattleshipGame.shipsAvailable(type)) {
			return new Result(false, "No more ships available");
		}
		if (x + sizeX > BattleshipGame.MAPSIZE) {
			return new Result(false, "Map size exceeded in x dimension");
		}
		if (y + sizeY > BattleshipGame.MAPSIZE) {
			return new Result(false, "Map size exceeded in x dimension");
		}
		for (int i = 0; i < sizeX; ++i) {
			for (int j = 0; j < sizeY; ++j) {
				byte value = map[x + i][y + j];
				if (value != BattleshipGame.MAP_WATER) {
					return new Result(false, "Map already used at " + new MapCoordinate(x, y).toLetterDigitFormat());
				}
				map[x + i][y + j] = type;
			}
		}
		addShipUsage(type);
		return new Result(true, "");
	}

	public Result deleteAllShips(byte type) {
		if (numShipsUsed(type) == 0) {
			return new Result(false, "Ship type not used yet");
		}
		for (int x = 0; x < BattleshipGame.MAPSIZE; ++x) {
			for (int y = 0; y < BattleshipGame.MAPSIZE; ++y) {
				byte value = map[x][y];
				if (value == type) {
					map[x][y] = BattleshipGame.MAP_WATER;
				}
			}
		}
		removeShipUsage(type);
		return new Result(true, "");
	}

	public boolean isShipAvailable(byte type) {
		return shipsUsed[type] < BattleshipGame.shipsAvailable(type);
	}

	public int numShipsUsed(byte type) {
		return shipsUsed[type];
	}

	public void addShipUsage(byte type) {
		shipsUsed[type]++;
	}

	public void removeShipUsage(byte type) {
		if (shipsUsed[type] > 0) {
			shipsUsed[type]--;
		}
	}
}