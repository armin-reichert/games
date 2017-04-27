package de.amr.easy.game.input;

public class Keyboard {

	public static boolean pressedOnce(int key) {
		return KeyboardHandler.INSTANCE.pressedOnce(key);
	}

	public static boolean pressedOnce(int modifier, int key) {
		return down(modifier) && pressedOnce(key);
	}

	public static boolean down(int key) {
		return KeyboardHandler.INSTANCE.pressed(key) || pressedOnce(key);
	}
}