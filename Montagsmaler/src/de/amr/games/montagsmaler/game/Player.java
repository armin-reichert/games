package de.amr.games.montagsmaler.game;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * Represents a player of the game.
 */
public class Player {

	private Team team;
	private String name;
	private ImageIcon image;
	private Color penColor = Color.WHITE;
	private int speed = 1;
	private int points;

	public Player() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public ImageIcon getImage() {
		return image;
	}

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	public Color getPenColor() {
		return penColor;
	}

	public void setPenColor(Color penColor) {
		this.penColor = penColor;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}
