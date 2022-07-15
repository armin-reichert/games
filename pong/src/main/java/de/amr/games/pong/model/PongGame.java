package de.amr.games.pong.model;

public class PongGame {

	public enum PlayMode {
		Player1_Player2, Player1_Computer, Computer_Player2, Computer_Computer
	}

	public int scoreLeft;
	public int scoreRight;
	public PlayMode playMode;

	public PongGame() {
		scoreLeft = 11;
		scoreRight = 11;
		playMode = PlayMode.Player1_Player2;
	}
}