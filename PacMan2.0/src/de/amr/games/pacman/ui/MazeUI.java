package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private final Sprite spriteMaze;
	private final Sprite spriteEnergizer;

	public MazeUI(int width, int height, Maze maze, PacMan pacMan, Ghost... ghosts) {
		this.maze = maze;
		this.pacMan = pacMan;
		this.ghosts = ghosts;
		spriteMaze = new Sprite(Spritesheet.getMaze()).scale(width, height);
		spriteEnergizer = new Sprite(Spritesheet.getEnergizerImages()).scale(TS, TS);
		spriteEnergizer.makeAnimated(AnimationMode.BACK_AND_FORTH, 250);
	}

	public Maze getMaze() {
		return maze;
	}
	
	public Sprite getSpriteEnergizer() {
		return spriteEnergizer;
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
		Arrays.stream(ghosts).forEach(ghost -> ghost.draw(g));
		pacMan.draw(g);
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