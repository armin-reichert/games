package de.amr.games.pong;

import java.awt.Color;
import java.awt.Font;

public class PongGlobals {

	public static final int WINNING_SCORE = 11;

	public static final Font FONT = new Font("Arial Black", Font.PLAIN, 28);

	public static final Color MENU_BACKGROUND = Color.LIGHT_GRAY;
	public static final Color MENU_SELECTED_BACKGROUND = MENU_BACKGROUND.darker();
	public static final Color MENU_HIGHLIGHT = Color.YELLOW;

	public static final Color COURT_BACKGROUND = Color.BLACK;
	public static final Color COURT_LINES_COLOR = Color.WHITE;
	public static final int COURT_LINE_WIDTH = 5;

	public static final int SERVING_TIME = 120;
	public static final int BALL_SIZE = 16;
	public static final int BALL_SPEED = 10;
	public static final Color BALL_COLOR = Color.YELLOW;

	public static final int PADDLE_WIDTH = 15;
	public static final int PADDLE_HEIGHT = 60;
	public static final int PADDLE_SPEED = 5;
	public static final Color PADDLE_COLOR = Color.LIGHT_GRAY;

	public static final Color SCORE_COLOR = Color.WHITE;
}