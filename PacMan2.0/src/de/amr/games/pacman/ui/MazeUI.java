package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final PacMan pacMan;
	private final Ghost[] ghosts;

	private Sprite spriteMaze;
	private Sprite spriteEnergizer;
	private Sprite[] spriteGreenNumber;

	private Ghost killedGhost;
	private int killedGhostPoints;

	public MazeUI(int width, int height, Maze maze, PacMan pacMan, Ghost... ghosts) {
		this.maze = maze;
		this.pacMan = pacMan;
		this.ghosts = ghosts;
		createSprites(width, height);
	}

	private void createSprites(int width, int height) {
		spriteMaze = new Sprite(Spritesheet.getMaze()).scale(width, height);
		spriteEnergizer = new Sprite(Spritesheet.getEnergizerImages()).scale(TS, TS);
		spriteEnergizer.createAnimation(AnimationMode.BACK_AND_FORTH, 250);
		spriteGreenNumber = new Sprite[4];
		for (int i = 0; i < 4; ++i) {
			spriteGreenNumber[i] = new Sprite(Spritesheet.getGreenNumber(i)).scale(2 * TS, 2 * TS);
		}
	}

	public Maze getMaze() {
		return maze;
	}

	public Sprite getSpriteEnergizer() {
		return spriteEnergizer;
	}

	public Sprite getSpriteGreenNumber(int i) {
		return spriteGreenNumber[i];
	}

	public void showGhostPoints(Ghost killedGhost, int points) {
		this.killedGhost = killedGhost;
		killedGhostPoints = points;
	}

	public void hideGhostPoints() {
		killedGhost = null;
	}

	@Override
	protected Stream<Sprite> getSprites() {
		List<Sprite> sprites = new ArrayList<>();
		sprites.add(spriteMaze);
		sprites.add(spriteEnergizer);
		sprites.addAll(Arrays.asList(spriteGreenNumber));
		return sprites.stream();
	}

	@Override
	public Sprite currentSprite() {
		return spriteMaze;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		spriteMaze.draw(g);
		maze.tiles().forEach(tile -> drawTile(g, tile));
		Arrays.stream(ghosts).filter(ghost -> ghost != killedGhost).forEach(ghost -> ghost.draw(g));
		if (killedGhost != null) {
			g.translate(killedGhost.tf.getX(), killedGhost.tf.getY());
			Sprite spritePoints = spriteGreenNumber[Arrays.asList(200, 400, 800, 1600).indexOf(killedGhostPoints)];
			spritePoints.draw(g);
			g.translate(-killedGhost.tf.getX(), -killedGhost.tf.getY());
		} else {
			pacMan.draw(g);
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawTile(Graphics2D g, Tile tile) {
		g.translate(tile.col * TS, tile.row * TS);
		char content = maze.getContent(tile);
		if (content == PELLET) {
			int size = TS / 4;
			g.setColor(Color.PINK);
			g.fillRect((TS - size) / 2, (TS - size) / 2, size, size);
		} else if (content == ENERGIZER) {
			spriteEnergizer.draw(g);
		} else if (Tile.isBonus(content)) {
			g.drawImage(Spritesheet.getBonus(content), 0, -TS / 2, TS * 2, TS * 2, null);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}