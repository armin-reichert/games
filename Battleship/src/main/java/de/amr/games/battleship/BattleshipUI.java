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
import java.util.Scanner;

/**
 * @author Armin Reichert
 */
public class BattleshipUI {

	private final Scanner sc = new Scanner(System.in);

	public String readLine(String prompt) {
		message(prompt);
		return sc.nextLine().trim();
	}

	public void close() {
		sc.close();
		message("Goodbye");
	}

	public void message(String msg, Object... args) {
		System.out.println(msg.formatted(args));
	}

	public void printHelp() {
		message("Available commands:");
		message("\thelp:       Print this help text");
		message("\tquit:       Quit program");
		message("\tplayer1:    Select player 1");
		message("\tplayer2:    Select player 2");
		message("\tmap:        Print map for current player");
		message("\tadd ship orientation coord: Add ship to map");
		message("\t\tship:        battleship, carrier, cruiser, destroyer, submarine");
		message("\t\torientation: h, v");
		message("\t\tcoord:       a1, ..., j10");
		message("\tdel ship: Delete ship from map");
		message("\t\tship:        battleship, carrier, cruiser, destroyer, submarine");
	}

	public void printPlayerMap(BattleshipGame game, int player) {
		message("\n      %s", Converter.playerName(player));
		printMap(game.getPlayer(player).map, new PrintWriter(System.out, true));
	}

	private void printMap(byte[][] map, PrintWriter w) {
		w.print("  ");
		for (int i = 1; i <= BattleshipGame.MAPSIZE; ++i) {
			w.print(i + " ");
		}
		w.println();
		for (int y = 0; y < BattleshipGame.MAPSIZE; ++y) {
			for (int x = 0; x < BattleshipGame.MAPSIZE; ++x) {
				byte value = map[x][y];
				char ch = value == BattleshipGame.MAP_WATER ? '~' : BattleshipGame.shipCode(value);
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