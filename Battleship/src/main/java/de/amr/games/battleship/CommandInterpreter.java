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
public class CommandInterpreter {

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
			var prompt = "[%s] (Enter 'help' for help):".formatted(ui.playerName(player));
			var input = ui.readLine(prompt);
			parseInput(input);
		} while (!quit);
		ui.close();
	}

	private void parseInput(String input) {
		var parts = splitIntoParts(input);
		var command = parts[0];
		if ("add".equals(command)) {
			doAddShip(parts);
			ui.printPlayerMap(game, player);
		} else if ("del".equals(command)) {
			doDeleteShip(parts);
			ui.printPlayerMap(game, player);
		} else if ("help".equals(command)) {
			ui.printHelp();
		} else if ("map".equals(command)) {
			ui.printPlayerMap(game, player);
		} else if ("player1".equals(command)) {
			player = BattleshipGame.PLAYER1;
			ui.printPlayerMap(game, player);
		} else if ("player2".equals(command)) {
			player = BattleshipGame.PLAYER2;
			ui.printPlayerMap(game, player);
		} else if ("reset".equals(command)) {
			game.getPlayer(player).reset();
			ui.printPlayerMap(game, player);
		} else if ("quit".equals(command)) {
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

	// Example: add carrier h i3
	private void doAddShip(String[] parts) {
		if (parts.length != 4) {
			ui.message("Command 'add' needs 3 parameters: shiptype orientation coordinate");
			return;
		}

		if (!ui.isValidShipType(parts[1])) {
			ui.message("Invalid ship type: %s", parts[1]);
			return;
		}
		var type = ui.shipType(parts[1]);

		int orientation = -1;
		try {
			orientation = parseOrientation(parts[2]);
		} catch (Exception e) {
			ui.message("Invalid orientation: %s", parts[2]);
			return;
		}

		MapCoordinate coord = null;
		try {
			coord = MapCoordinate.valueOf(parts[3]);
		} catch (IllegalArgumentException x) {
			ui.message("Illegal coordinate: %s", parts[3]);
			return;
		}

		var result = game.addShip(player, type, coord.x(), coord.y(), orientation);
		if (result.success()) {
			ui.message("%s: added %s %s at %s", ui.playerName(player), ui.shipTypeName(type), ui.orientationName(orientation),
					coord.toLetterDigitFormat());
		} else {
			ui.message(result.message());
		}
	}

	private void doDeleteShip(String[] parts) {
		if (parts.length != 2) {
			ui.message("Command 'delete' needs 1 parameter: shiptype");
			return;
		}

		if (!ui.isValidShipType(parts[1])) {
			ui.message("Invalid ship type: %s", parts[1]);
			return;
		}
		var type = ui.shipType(parts[1]);

		var result = game.deleteShip(player, type);
		if (result.success()) {
			ui.message("%s: %s deleted", ui.playerName(player), ui.shipTypeName(type));
		} else {
			ui.message(result.message());
		}
	}

	private String[] splitIntoParts(String s) {
		String[] parts = s.trim().split(" ");
		for (int i = 0; i < parts.length; ++i) {
			parts[i] = parts[i].trim();
		}
		return parts;
	}
}