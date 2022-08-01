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
			var command = readCommand(sc);
			executeCommand(command);
		} while (!quit);
		message("Goodbye");
		sc.close();
	}

	private String readCommand(Scanner sc) {
		message("Enter command (%s), enter 'help' for help:", player == BattleshipGame.PLAYER1 ? "Player 1" : "Player 2");
		return sc.nextLine().trim();
	}

	private void executeCommand(String commandString) {
		if (commandString.startsWith("add ")) {
			executeAddCommand(commandString.substring(4));
			game.printPlayer(player);
		} else if ("help".equals(commandString)) {
			printHelp();
		} else if ("map".equals(commandString)) {
			game.printPlayer(player);
		} else if (commandString.startsWith("player ")) {
			executePlayerCommand(commandString.substring(7));
		} else if ("quit".equals(commandString)) {
			quit = true;
		} else {
			message("Did not understand");
		}
	}

	private void printHelp() {
		message("Available commands:");
		message("\thelp: Print this help text");
		message("\tquit: Quit program");
		message("\tmap:  Prints the map for the current player");
		message("\tplayer <n>: Select player (1 or 2)");
		message("\tadd <ship> <orient> <coord>: Add ship to map");
		message("\t\t<ship>:   battleship, carrier, cruiser, destroyer, submarine");
		message("\t\t<orient>: h, v");
		message("\t\t<coord>:  A1, ..., J10");
	}

	private void executePlayerCommand(String paramString) {
		var params = paramString.trim().split(" ");
		if (params.length != 1) {
			error("Command 'player' needs 1 parameter: number (1 or 2)");
			return;
		}
		int number = -1;
		try {
			number = Integer.parseInt(params[0]);
		} catch (Exception e) {
			error("Not a valid player number: %s", params[0]);
			return;
		}
		if (number != 1 && number != 2) {
			error("Player number must be 1 or 2");
			return;
		}
		player = number == 1 ? BattleshipGame.PLAYER1 : BattleshipGame.PLAYER2;
	}

	// Example: add carrier h I3
	private void executeAddCommand(String paramString) {
		String[] params = paramString.trim().split(" ");
		if (params.length != 3) {
			error("add needs 3 parameters: shiptype orientation coordinate");
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
		if ("h".equalsIgnoreCase(orientString)) {
			game.addShip(player, BattleshipGame.shipType(typeString), coord.x(), coord.y(), BattleshipGame.HORIZONTAL);
		} else {
			game.addShip(player, BattleshipGame.shipType(typeString), coord.x(), coord.y(), BattleshipGame.VERTICAL);
		}
	}
}