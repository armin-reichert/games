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

	private static void error(String msg, Object... args) {
		System.err.println(msg.formatted(args));
	}

	private static void message(String msg, Object... args) {
		System.out.println(msg.formatted(args));
	}

	private final Scanner sc = new Scanner(System.in);
	private final BattleshipGame game;
	private int player;
	private boolean quit;

	public CommandInterpreter(BattleshipGame game) {
		this.game = game;
		player = BattleshipGame.PLAYER1;
	}

	public void run() {
		do {
			var input = readInput();
			parseInput(input);
		} while (!quit);
		sc.close();
		message("Goodbye");
	}

	private String readInput() {
		message("(%s): Enter 'help' for help:", BattleshipGame.playerName(player));
		return sc.nextLine().trim();
	}

	private void parseInput(String input) {
		if (input.startsWith("add ")) {
			doAddShip(input.substring(4));
			game.printPlayerMap(player);
		} else if (input.startsWith("del ")) {
			doDeleteShip(input.substring(4));
		} else if ("help".equals(input)) {
			printHelp();
		} else if ("map".equals(input)) {
			game.printPlayerMap(player);
		} else if ("player1".equals(input)) {
			player = BattleshipGame.PLAYER1;
		} else if ("player2".equals(input)) {
			player = BattleshipGame.PLAYER2;
		} else if ("reset".equals(input)) {
			game.resetPlayer(player);
		} else if ("quit".equals(input)) {
			quit = true;
		} else {
			message("Did not understand");
		}
	}

	private void printHelp() {
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

	// Example: add carrier h I3
	private void doAddShip(String paramString) {
		String[] params = paramString.trim().split(" ");
		if (params.length != 3) {
			error("Command 'add' needs 3 parameters: <shiptype> <orientation> <coordinate>");
			return;
		}
		var typeString = params[0];
		if (!BattleshipGame.isValidShipType(typeString)) {
			error("Invalid ship type: %s", typeString);
			return;
		}
		var orientString = params[1];
		if (!BattleshipGame.isValidOrientation(orientString)) {
			error("Invalid orientation: %s", orientString);
			return;
		}
		var coordString = params[2];
		MapCoordinate coord = null;
		try {
			coord = MapCoordinate.valueOf(coordString);
		} catch (IllegalArgumentException x) {
			error("Illegal coordinate: %s", coordString);
			return;
		}
		var type = BattleshipGame.shipType(typeString);
		if ("h".equalsIgnoreCase(orientString)) {
			game.addShip(player, type, coord.x(), coord.y(), BattleshipGame.HORIZONTAL);
		} else {
			game.addShip(player, type, coord.x(), coord.y(), BattleshipGame.VERTICAL);
		}
	}

	private void doDeleteShip(String paramString) {
		String[] params = paramString.trim().split(" ");
		if (params.length != 1) {
			error("Command 'delete' needs 1 parameter: <shiptype>");
			return;
		}
		var typeString = params[0];
		if (!BattleshipGame.isValidShipType(typeString)) {
			error("Invalid ship type: %s", typeString);
			return;
		}
		var type = BattleshipGame.shipType(typeString);
		game.deleteShip(player, type);
	}
}