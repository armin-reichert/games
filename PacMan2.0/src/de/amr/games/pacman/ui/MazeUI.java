package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Content.ENERGIZER;
import static de.amr.games.pacman.model.Content.PELLET;
import static de.amr.games.pacman.model.Content.isFood;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.Energizer;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.Pellet;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final GameActors actors;
	private final Energizer energizer;
	private final Pellet pellet;

	private final Sprite s_normal;
	private final Sprite s_flashing;

	private boolean flashing;
	private int bonusTimer;

	public MazeUI(Maze maze, GameActors actors) {
		this.maze = maze;
		this.actors = actors;
		s_normal = GameUI.SPRITES.maze().scale(getWidth(), getHeight());
		s_flashing = GameUI.SPRITES.flashingMaze().scale(getWidth(), getHeight());
		energizer = new Energizer();
		pellet = new Pellet();
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_normal, s_flashing);
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? s_flashing : s_normal;
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				actors.removeBonus();
			}
		}
	}

	@Override
	public int getWidth() {
		return maze.numCols() * TS;
	}

	@Override
	public int getHeight() {
		return maze.numRows() * TS;
	}

	public Maze getMaze() {
		return maze;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	@Override
	public void enableAnimation(boolean on) {
		super.enableAnimation(on);
		energizer.enableAnimation(on);
		actors.getPacMan().enableAnimation(on);
		actors.getActiveGhosts().forEach(ghost -> ghost.enableAnimation(on));
	}

	public void showBonus(int ticks) {
		bonusTimer = ticks;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			s_flashing.draw(g);
		} else {
			s_normal.draw(g);
			maze.tiles().filter(tile -> isFood(maze.getContent(tile))).forEach(tile -> drawFood(g, tile));
			actors.getActiveGhosts().filter(ghost -> ghost.getState() != Ghost.State.DYING).forEach(ghost -> ghost.draw(g));
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(ghost -> ghost.draw(g));
			actors.getPacMan().draw(g);
			actors.getBonus().ifPresent(bonus -> {
				bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
				bonus.draw(g);
			});
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawFood(Graphics2D g, Tile tile) {
		g.translate(tile.col * TS, tile.row * TS);
		char c = maze.getContent(tile);
		if (c == PELLET) {
			pellet.draw(g);
		} else if (c == ENERGIZER) {
			energizer.draw(g);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}