package de.amr.games.breakout.scenes;

public enum PlayEvent {
	BallHitsBrick, BallHitsBat;

	private Object userData;

	public <T> void setUserData(T t) {
		userData = t;
	}

	@SuppressWarnings("unchecked")
	public <T> T getUserData() {
		return (T) userData;
	}
}