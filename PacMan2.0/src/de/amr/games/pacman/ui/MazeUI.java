package de.amr.games.pacman.ui;

import static de.amr.games.pacman.behavior.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.chase;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.flee;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.goHome;
import static de.amr.games.pacman.model.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.model.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.model.Spritesheet.RED_GHOST;
import static de.amr.games.pacman.model.Spritesheet.TURQUOISE_GHOST;
import static de.amr.games.pacman.model.Spritesheet.getMazeImage;
import static de.amr.games.pacman.model.Spritesheet.getMazeImageWhite;
import static de.amr.games.pacman.model.TileContent.ENERGIZER;
import static de.amr.games.pacman.model.TileContent.PELLET;
import static de.amr.games.pacman.model.TileContent.isFood;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.PacMan;

public class MazeUI extends GameEntity {

	/** Tile size. */
	public static final int TS = 16;

	public final GameEventSupport eventing = new GameEventSupport();

	private final Maze maze;
	private final PacMan pacMan;
	private final Ghost blinky;
	private final Ghost pinky;
	private final Ghost inky;
	private final Ghost clyde;
	private final Set<Ghost> ghosts = new HashSet<>();
	private final Energizer energizer;
	private final Pellet pellet;

	private final Sprite s_normal;
	private final Sprite s_flashing;

	private boolean flashing;
	private String infoText;
	private Bonus bonus;
	private int bonusTimeLeft;

	public MazeUI(Maze maze) {
		this.maze = maze;
		s_normal = new Sprite(getMazeImage()).scale(getWidth(), getHeight());
		s_flashing = new Sprite(getMazeImage(), getMazeImageWhite()).scale(getWidth(), getHeight())
				.animation(AnimationMode.CYCLIC, 100);

		energizer = new Energizer();
		pellet = new Pellet();

		pacMan = createPacMan();
		blinky = createBlinky();
		pinky = createPinky();
		inky = createInky();
		clyde = createClyde();
		addGhost(blinky);
		addGhost(pinky);
		addGhost(inky);
		addGhost(clyde);
	}

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.setNavigation(PacMan.State.ALIVE, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		return pacMan;
	}

	private Ghost createBlinky() {
		Ghost blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		blinky.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		blinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		blinky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.SAFE, bounce());
		return blinky;
	}

	private Ghost createPinky() {
		Ghost pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		pinky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		pinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		pinky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.SAFE, bounce());
		return pinky;
	}

	private Ghost createInky() {
		Ghost inky = new Ghost(maze, "Inky", TURQUOISE_GHOST, maze.inkyHome);
		inky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		inky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		inky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.SAFE, bounce());
		return inky;
	}

	private Ghost createClyde() {
		Ghost clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		clyde.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		clyde.setNavigation(Ghost.State.AFRAID, goHome());
		clyde.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.SAFE, bounce());
		return clyde;
	}

	public void initActors(Game game) {
		pacMan.setState(PacMan.State.ALIVE);
		pacMan.placeAt(maze.pacManHome);
		pacMan.setSpeed(game::getPacManSpeed);
		pacMan.setDir(Top4.E);
		pacMan.setNextDir(Top4.E);

		ghosts.forEach(ghost -> {
			ghost.setState(Ghost.State.SAFE);
			ghost.placeAt(ghost.homeTile);
			ghost.setSpeed(game::getGhostSpeed);
		});
		blinky.setDir(Top4.E);
		pinky.setDir(Top4.S);
		inky.setDir(Top4.N);
		clyde.setDir(Top4.N);
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
		if (bonus != null) {
			if (bonusTimeLeft-- == 0) {
				removeBonus();
			}
		}
		pacMan.update();
		ghosts.forEach(Ghost::update);
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

	public PacMan getPacMan() {
		return pacMan;
	}

	public Ghost getBlinky() {
		return blinky;
	}

	public Ghost getPinky() {
		return pinky;
	}

	public Ghost getInky() {
		return inky;
	}

	public Ghost getClyde() {
		return clyde;
	}

	public void addGhost(Ghost ghost) {
		ghosts.add(ghost);
		pacMan.lookFor.add(ghost);
	}

	public void removeGhost(Ghost ghost) {
		ghosts.remove(ghost);
		pacMan.lookFor.remove(ghost);
	}

	public boolean containsGhost(Ghost ghost) {
		return ghosts.contains(ghost);
	}

	public Stream<Ghost> getGhosts() {
		return ghosts.stream();
	}

	public void showInfo(String text) {
		this.infoText = text;
	}

	public void hideInfo() {
		this.infoText = null;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public void addBonus(BonusSymbol symbol, int value, int ticks) {
		bonus = new Bonus(symbol, value);
		bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
		bonusTimeLeft = ticks;
		pacMan.lookFor.add(bonus);
	}

	public void removeBonus() {
		if (bonus != null) {
			pacMan.lookFor.remove(bonus);
			bonus = null;
			bonusTimeLeft = 0;
		}
	}

	public void honorAndRemoveBonus(int ticks) {
		if (bonus != null) {
			pacMan.lookFor.remove(bonus);
			bonus.setHonored();
			bonusTimeLeft = ticks;
		}
	}

	@Override
	public void enableAnimation(boolean on) {
		super.enableAnimation(on);
		energizer.enableAnimation(on);
		pacMan.enableAnimation(on);
		ghosts.forEach(ghost -> ghost.enableAnimation(on));
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			s_flashing.draw(g);
		} else {
			s_normal.draw(g);
			maze.tiles().filter(tile -> isFood(maze.getContent(tile))).forEach(tile -> drawFood(g, tile));
			ghosts.stream().filter(ghost -> ghost.getState() != Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			ghosts.stream().filter(ghost -> ghost.getState() == Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			pacMan.draw(g);
			if (bonus != null) {
				bonus.draw(g);
			}
			if (infoText != null) {
				drawInfoText(g);
			}
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawInfoText(Graphics2D g) {
		Tile tile = maze.infoTile;
		g.translate((tile.col + 1) * TS, tile.row * TS + TS / 4);
		g.setFont(Assets.font("scoreFont"));
		g.setColor(Color.YELLOW);
		Rectangle2D box = g.getFontMetrics().getStringBounds(infoText, g);
		g.drawString(infoText, (int) (-box.getWidth() / 2), (int) (box.getHeight() / 2));
		g.translate(-tile.col * TS, -tile.row * TS);
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