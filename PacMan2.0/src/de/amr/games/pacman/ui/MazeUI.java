package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.ENERGIZER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	public final GameEventSupport observers = new GameEventSupport();

	private final Maze maze;
	private PacMan pacMan;
	private Ghost[] ghosts = new Ghost[0];

	private Sprite spriteMaze;
	private Sprite spriteMazeFlashing;
	private Sprite spriteEnergizer;

	private boolean flashing;
	private String text = "";
	private Points points;

	public MazeUI(int width, int height, Maze maze) {
		this.maze = maze;
		createSprites(width, height);
	}

	public void populate(PacMan pacMan, Ghost... ghosts) {
		this.pacMan = pacMan;
		this.ghosts = ghosts;
	}

	private void createSprites(int width, int height) {
		spriteMaze = new Sprite(Spritesheet.getMaze()).scale(width, height);
		spriteMazeFlashing = new Sprite(Spritesheet.getMaze(), Spritesheet.getMazeWhite()).scale(width,
				height);
		spriteMazeFlashing.createAnimation(AnimationMode.CYCLIC, 100);
		spriteEnergizer = new Sprite(Spritesheet.getEnergizer()).scale(TS, TS);
		spriteEnergizer.createAnimation(AnimationMode.BACK_AND_FORTH, 250);
	}

	public Maze getMaze() {
		return maze;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public Sprite getSpriteEnergizer() {
		return spriteEnergizer;
	}

	public void showPoints(int value, Tile tile) {
		points = new Points(value);
		points.tf.moveTo(tile.col * TS, tile.row * TS);
	}

	public void hidePoints() {
		points = null;
	}

	public void showText(String text) {
		this.text = text;
	}

	@Override
	protected Stream<Sprite> getSprites() {
		List<Sprite> sprites = new ArrayList<>();
		sprites.add(spriteMaze);
		sprites.add(spriteEnergizer);
		return sprites.stream();
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? spriteMazeFlashing : spriteMaze;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		currentSprite().draw(g);
		if (!flashing) {
			maze.tiles().forEach(tile -> drawTile(g, tile));
			drawCenteredText(g, maze.bonusTile);
			Arrays.stream(ghosts).filter(ghost -> ghost.visibility.getAsBoolean() == true)
					.forEach(ghost -> ghost.draw(g));
			if (points != null) {
				points.draw(g);
			}
			pacMan.draw(g);
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawCenteredText(Graphics2D g, Tile tile) {
		if (text.length() > 0) {
			g.translate((tile.col + 1) * TS, tile.row * TS);
			g.setFont(Assets.font("scoreFont"));
			g.setColor(Color.YELLOW);
			Rectangle2D box = g.getFontMetrics().getStringBounds(text, g);
			g.drawString(text, (int) (-box.getWidth() / 2), (int) (box.getHeight() / 2));
			g.translate(-tile.col * TS, -tile.row * TS);
		}
	}

	private void drawTile(Graphics2D g, Tile tile) {
		g.translate(tile.col * TS, tile.row * TS);
		char content = maze.getContent(tile);
		if (content == Tile.PELLET) {
			g.setColor(Color.PINK);
			g.fillRect(TS * 3 / 8, TS * 3 / 8, TS / 4, TS / 4);
		} else if (content == ENERGIZER) {
			spriteEnergizer.draw(g);
		} else if (Tile.isBonus(content)) {
			g.drawImage(Spritesheet.getBonus(content), 0, -TS / 2, TS * 2, TS * 2, null);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}