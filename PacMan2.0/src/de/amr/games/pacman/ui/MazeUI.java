package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Content.ENERGIZER;
import static de.amr.games.pacman.model.Content.PELLET;
import static de.amr.games.pacman.model.Content.isFood;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.Bonus;
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
	private Bonus bonus;
	private int bonusTimeLeft;

	public MazeUI(Maze maze, GameActors actors) {
		this.maze = maze;
		this.actors = actors;
		s_normal = Spritesheet.maze().scale(getWidth(), getHeight());
		s_flashing = Spritesheet.flashingMaze().scale(getWidth(), getHeight());
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
	public void init() {
		removeBonus();
	}

	@Override
	public void update() {
		if (bonus != null) {
			if (bonusTimeLeft-- == 0) {
				removeBonus();
			}
		}
		actors.getPacMan().update();
		actors.getActiveGhosts().forEach(Ghost::update);
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

	public void addBonus(Bonus bonus, int ticks) {
		this.bonus = bonus;
		bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
		bonusTimeLeft = ticks;
		actors.getPacMan().interests.add(bonus);
	}

	public void removeBonus() {
		if (bonus != null) {
			bonus = null;
			bonusTimeLeft = 0;
			actors.getPacMan().interests.remove(bonus);
		}
	}

	public void honorBonusAndRemoveAfter(int ticks) {
		if (bonus != null) {
			bonus.setHonored();
			bonusTimeLeft = ticks;
		}
	}

	@Override
	public void enableAnimation(boolean on) {
		super.enableAnimation(on);
		energizer.enableAnimation(on);
		actors.getPacMan().enableAnimation(on);
		actors.getActiveGhosts().forEach(ghost -> ghost.enableAnimation(on));
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
			if (bonus != null) {
				bonus.draw(g);
			}
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