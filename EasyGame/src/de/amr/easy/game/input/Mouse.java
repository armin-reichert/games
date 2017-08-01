package de.amr.easy.game.input;

public class Mouse {

	public static boolean clicked() {
		return MouseHandler.INSTANCE.clicked();
	}

	public static int getX() {
		return MouseHandler.INSTANCE.getX();
	}

	public static int getY() {
		return MouseHandler.INSTANCE.getY();
	}

}
