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

	public static final byte WATER = -127;

	public static final byte AIRCRAFT_CARRIER = 0;
	public static final byte BATTLESHIP = 1;
	public static final byte CRUISER = 2;
	public static final byte SUBMARINE = 3;
	public static final byte DESTROYER = 4;

	public static final int PLAYER_1 = 0;
	public static final int PLAYER_2 = 1;

	public static int shipSize(byte type) {
		return switch (type) {
		case AIRCRAFT_CARRIER -> 5;
		case BATTLESHIP -> 4;
		case CRUISER -> 3;
		case DESTROYER -> 2;
		case SUBMARINE -> 1;
		default -> throw new IllegalArgumentException();
		};
	}

	public static int shipsAvailable(byte type) {
		return switch (type) {
		case AIRCRAFT_CARRIER -> 1;
		case BATTLESHIP -> 1;
		case CRUISER -> 1;
		case DESTROYER -> 2;
		case SUBMARINE -> 2;
		default -> throw new IllegalArgumentException();
		};
	}

	public static char shipCode(byte type) {
		return switch (type) {
		case AIRCRAFT_CARRIER -> 'A';
		case BATTLESHIP -> 'B';
		case CRUISER -> 'C';
		case SUBMARINE -> 'S';
		case DESTROYER -> 'D';
		default -> throw new IllegalArgumentException();
		};
	}

	private final Player[] players = new Player[2];

	public BattleshipGame() {
		players[0] = new Player();
		players[1] = new Player();
	}

	public Player getPlayer(int playerIndex) {
		return players[playerIndex];
	}
}