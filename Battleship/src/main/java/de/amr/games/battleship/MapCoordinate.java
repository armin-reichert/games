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

import java.util.regex.Pattern;

/**
 * @author Armin Reichert
 *
 */
public record MapCoordinate(int x, int y) {

	private static final String LETTERS = "ABCDEFGHIJ";
	private static final char[] LETTERS_ARRAY = LETTERS.toCharArray();
	private static final Pattern PATTERN = Pattern.compile("[a-jA-J]([1-9]|10)");

	public static char letter(int y) {
		return LETTERS_ARRAY[y];
	}

	public static MapCoordinate valueOf(String letterDigit) {
		if (letterDigit.length() != 2 && letterDigit.length() != 3) {
			throw new IllegalArgumentException();
		}
		var matcher = PATTERN.matcher(letterDigit);
		if (matcher.matches()) {
			var letter = letterDigit.substring(0, 1);
			var number = Integer.valueOf(letterDigit.substring(1));
			var x = number - 1;
			var y = LETTERS.indexOf(letter.toUpperCase());
			return new MapCoordinate(x, y);
		}
		throw new IllegalArgumentException();
	}

	public String asLetterDigit() {
		return String.valueOf(LETTERS_ARRAY[y]) + String.valueOf(x + 1);
	}
}