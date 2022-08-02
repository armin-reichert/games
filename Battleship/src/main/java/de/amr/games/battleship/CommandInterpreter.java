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
			var prompt = "[%s] (Enter 'help' for help):".formatted(Converter.playerName(player));
			var input = ui.readLine(prompt);
			parseInput(input);
		} while (!quit);
		ui.close();
	}

	private void parseInput(String input) {
		var parts = splitIntoParts(input);
		var command = parts[0];
		switch (command) {
		case "add" -> {
			doAddShip(parts);
			ui.printPlayerMap(game, player);
		}
		case "del" -> {
			doDeleteShip(parts);
			ui.printPlayerMap(game, player);
		}
		case "help" -> {
			ui.printHelp();
		}
		case "map" -> {
			ui.printPlayerMap(game, player);
		}
		case "player1" -> {
			player = BattleshipGame.PLAYER1;
			ui.printPlayerMap(game, player);
		}
		case "player2" -> {
			player = BattleshipGame.PLAYER2;
			ui.printPlayerMap(game, player);
		}
		case "quit" -> {
			quit = true;
		}
		case "reset" -> {
			game.getPlayer(player).reset();
			ui.printPlayerMap(game, player);
		}
		default -> {
			ui.message("Did not understand");
		}
		}
	}

	// Example: add carrier h i3
	private void doAddShip(String[] parts) {
		if (parts.length - 1 != 3) {
			ui.message("Command 'add' needs 3 parameters: shiptype orientation coordinate");
			return;
		}

		byte type;
		int orientation;
		MapCoordinate coord;
		try {
			type = Converter.parseShipType(parts[1]);
			orientation = Converter.parseOrientation(parts[2]);
			coord = Converter.parseMapCoordinate(parts[3]);
		} catch (ConvertException x) {
			ui.message(x.getMessage());
			return;
		}

		var result = game.addShip(player, type, coord.x(), coord.y(), orientation);
		if (result.success()) {
			ui.message("%s: added %s %s at %s", Converter.playerName(player), Converter.shipTypeName(type),
					Converter.orientationName(orientation), coord.toLetterDigitFormat());
		} else {
			ui.message(result.message());
		}
	}

	private void doDeleteShip(String[] parts) {
		if (parts.length - 1 != 1) {
			ui.message("Command 'delete' needs 1 parameter: shiptype");
			return;
		}

		byte type;
		try {
			type = Converter.parseShipType(parts[1]);
		} catch (ConvertException x) {
			ui.message(x.getMessage());
			return;
		}

		var result = game.deleteShip(player, type);
		if (result.success()) {
			ui.message("%s: %s deleted", Converter.playerName(player), Converter.shipTypeName(type));
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