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
public class Converter {

	private Converter() {
	}

	public static int parseOrientation(String s) {
		if ("h".equals(s)) {
			return BattleshipGame.HORIZONTAL;
		}
		if ("v".equals(s)) {
			return BattleshipGame.VERTICAL;
		}
		throw new ConvertException("Illegal orientation value '%s'", s);
	}

	public static String orientationName(int orientation) {
		if (orientation == BattleshipGame.HORIZONTAL) {
			return "horizontal";
		}
		if (orientation == BattleshipGame.VERTICAL) {
			return "vertical";
		}
		throw new ConvertException("Illegal orientation value '%s'", orientation);
	}

	public static MapCoordinate parseMapCoordinate(String s) {
		return MapCoordinate.valueOf(s);
	}

	public static byte parseShipType(String s) {
		var ss = s.trim().toLowerCase();
		if ("battleship".equals(ss)) {
			return BattleshipGame.MAP_BATTLESHIP;
		}
		if ("carrier".equals(ss)) {
			return BattleshipGame.MAP_CARRIER;
		}
		if ("cruiser".equals(ss)) {
			return BattleshipGame.MAP_CRUISER;
		}
		if ("destroyer".equals(ss)) {
			return BattleshipGame.MAP_DESTROYER;
		}
		if ("submarine".equals(ss)) {
			return BattleshipGame.MAP_SUBMARINE;
		}
		throw new ConvertException("Illegal ship type value '%s'", s);
	}

	public static String shipTypeName(byte type) {
		return switch (type) {
		case BattleshipGame.MAP_BATTLESHIP -> "Battleship";
		case BattleshipGame.MAP_CARRIER -> "Carrier";
		case BattleshipGame.MAP_CRUISER -> "Cruiser";
		case BattleshipGame.MAP_DESTROYER -> "Destroyer";
		case BattleshipGame.MAP_SUBMARINE -> "Submarine";
		default -> throw new IllegalArgumentException();
		};
	}

	public static String playerName(int player) {
		if (player == BattleshipGame.PLAYER1) {
			return "Player #1";
		}
		if (player == BattleshipGame.PLAYER2) {
			return "Player #2";
		}
		throw new IllegalArgumentException();
	}

}