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

import java.util.Scanner;

/**
 * @author Armin Reichert
 */
public class CommandInterpreter {

	private final Scanner sc = new Scanner(System.in);
	private final BattleshipGame game;
	private final BattleshipUI ui;
	private int player;
	private boolean quit;

	public CommandInterpreter(BattleshipGame game) {
		this.game = game;
		this.ui = new BattleshipUI();
		player = BattleshipGame.PLAYER1;
	}

	public void run() {
		do {
			var input = readInput();
			parseInput(input);
		} while (!quit);
		sc.close();
		ui.message("Goodbye");
	}

	private String readInput() {
		ui.message("(%s): Enter 'help' for help:", ui.playerName(player));
		return sc.nextLine().trim();
	}

	private void parseInput(String input) {
		if (input.startsWith("add ")) {
			doAddShip(input.substring(4));
			ui.printPlayerMap(game, player);
		} else if (input.startsWith("del ")) {
			doDeleteShip(input.substring(4));
			ui.printPlayerMap(game, player);
		} else if ("help".equals(input)) {
			ui.printHelp();
		} else if ("map".equals(input)) {
			ui.printPlayerMap(game, player);
		} else if ("player1".equals(input)) {
			player = BattleshipGame.PLAYER1;
			ui.printPlayerMap(game, player);
		} else if ("player2".equals(input)) {
			player = BattleshipGame.PLAYER2;
			ui.printPlayerMap(game, player);
		} else if ("reset".equals(input)) {
			game.resetPlayer(player);
			ui.printPlayerMap(game, player);
		} else if ("quit".equals(input)) {
			quit = true;
		} else {
			ui.message("Did not understand");
		}
	}

	private static int parseOrientation(String orientation) {
		if ("h".equals(orientation)) {
			return BattleshipGame.HORIZONTAL;
		}
		if ("v".equals(orientation)) {
			return BattleshipGame.VERTICAL;
		}
		throw new IllegalArgumentException();
	}

	// Example: add carrier h I3
	private void doAddShip(String paramString) {
		String[] params = paramString.trim().split(" ");
		if (params.length != 3) {
			ui.message("Command 'add' needs 3 parameters: <shiptype> <orientation> <coordinate>");
			return;
		}

		var typeString = params[0];
		if (!ui.isValidShipType(typeString)) {
			ui.message("Invalid ship type: %s", typeString);
			return;
		}

		var orientString = params[1];
		int orientation = -1;
		try {
			orientation = parseOrientation(orientString);
		} catch (Exception e) {
			ui.message("Invalid orientation: %s", orientString);
			return;
		}

		var coordString = params[2];
		MapCoordinate coord = null;
		try {
			coord = MapCoordinate.valueOf(coordString);
		} catch (IllegalArgumentException x) {
			ui.message("Illegal coordinate: %s", coordString);
			return;
		}

		var type = ui.shipType(typeString);
		ui.message("%s: %s %s at %s", ui.playerName(player), ui.orientationName(orientation), ui.shipTypeName(type),
				coord.toLetterDigitFormat());

		var result = game.addShip(player, type, coord.x(), coord.y(), orientation);
		if (!result.success()) {
			ui.message(result.message());
		}
	}

	private void doDeleteShip(String paramString) {
		String[] params = paramString.trim().split(" ");
		if (params.length != 1) {
			ui.message("Command 'delete' needs 1 parameter: <shiptype>");
			return;
		}

		var typeString = params[0];
		if (!ui.isValidShipType(typeString)) {
			ui.message("Invalid ship type: %s", typeString);
			return;
		}
		var type = ui.shipType(typeString);

		var result = game.deleteShip(player, type);
		if (!result.success()) {
			ui.message(result.message());
		}
	}
}