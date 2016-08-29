package de.amr.games.birdy;

import java.awt.event.KeyEvent;

public class Globals {

	public static final int JUMP_KEY = KeyEvent.VK_UP;

	public static final float WORLD_GRAVITY = 0.3f;
	public static final float WORLD_SPEED = -2.5f;
	public static final int TIME_BEFORE_PLAY = 300;
	public static final int CITY_MAX_STARS = 5;
	public static final float BIRD_JUMP_SPEED = WORLD_GRAVITY * 2.5f;
	public static final int BIRD_FLAP_DURATION_MILLIS = 50;
	public static final int BIRD_INJURED_TIME = 60;
	public static final int OBSTACLE_MIN_CREATION_TIME = 60;
	public static final int OBSTACLE_MAX_CREATION_TIME = 90;
	public static final int OBSTACLE_PIPE_HEIGHT = 320;
	public static final int OBSTACLE_PIPE_WIDTH = 52;
	public static final int OBSTACLE_MIN_PIPE_HEIGHT = 100;
	public static final int OBSTACLE_PASSAGE_HEIGHT = 100;
}