package de.amr.games.pacman.ui;

import static de.amr.games.pacman.ui.GameUI.SPRITES;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Animation;
import de.amr.easy.game.sprite.CyclicAnimation;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final GameActors actors;
	private final Animation energizerBlinking;

	private final Sprite s_maze_normal;
	private final Sprite s_maze_flashing;

	private boolean flashing;
	private int bonusTimer;

	public MazeUI(Maze maze, GameActors actors) {
		this.maze = maze;
		this.actors = actors;
		s_maze_normal = SPRITES.mazeFull().scale(getWidth(), getHeight());
		s_maze_flashing = SPRITES.mazeFlashing().scale(getWidth(), getHeight());
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(250);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_maze_normal, s_maze_flashing);
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? s_maze_flashing : s_maze_normal;
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				actors.removeBonus();
			}
		}
		energizerBlinking.update();
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
		energizerBlinking.setEnabled(on);
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
			s_maze_flashing.draw(g);
		} else {
			s_maze_normal.draw(g);
			maze.tiles().filter(tile -> Content.isFood(maze.getContent(tile))).forEach(tile -> drawFood(g, tile));
			actors.getActiveGhosts().filter(ghost -> ghost.getState() != Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
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
		g.setColor(Color.BLACK);
		if (maze.getContent(tile) == Content.EATEN) {
			g.fillRect(0, 0, TS, TS);
		} else if (maze.getContent(tile) == Content.ENERGIZER && energizerBlinking.currentFrame() % 2 != 0) {
			g.fillRect(0, 0, TS, TS);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}