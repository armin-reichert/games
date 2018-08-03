package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.TileContent.ENERGIZER;
import static de.amr.games.pacman.model.TileContent.PELLET;
import static de.amr.games.pacman.model.TileContent.isFood;
import static de.amr.games.pacman.routing.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.routing.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.routing.impl.NavigationSystem.chase;
import static de.amr.games.pacman.routing.impl.NavigationSystem.flee;
import static de.amr.games.pacman.routing.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.routing.impl.NavigationSystem.goHome;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.TS;
import static de.amr.games.pacman.ui.Spritesheet.TURQUOISE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.getMazeImage;
import static de.amr.games.pacman.ui.Spritesheet.getMazeImageWhite;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.GameEventManager;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.Bonus;
import de.amr.games.pacman.ui.actor.Energizer;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.GhostName;
import de.amr.games.pacman.ui.actor.PacMan;
import de.amr.games.pacman.ui.actor.Pellet;

public class MazeUI extends GameEntity {

	public final GameEventManager eventMgr = new GameEventManager();

	private final Game game;
	private final Maze maze;
	private final PacMan pacMan;
	private final Set<GameEntity> pacManInterests = new HashSet<>();
	private final Map<GhostName, Ghost> ghostsByName = new EnumMap<>(GhostName.class);
	private final Map<GhostName, Ghost> activeGhostsByName = new EnumMap<>(GhostName.class);
	private final Energizer energizer;
	private final Pellet pellet;

	private final Sprite s_normal;
	private final Sprite s_flashing;

	private boolean flashing;
	private String infoText;
	private Color infoTextColor;
	private Bonus bonus;
	private int bonusTimeLeft;

	public MazeUI(Game game, Maze maze) {
		this.game = game;
		this.maze = maze;
		s_normal = new Sprite(getMazeImage()).scale(getWidth(), getHeight());
		s_flashing = new Sprite(getMazeImage(), getMazeImageWhite()).scale(getWidth(), getHeight())
				.animation(AnimationMode.CYCLIC, 100);

		energizer = new Energizer();
		pellet = new Pellet();

		pacMan = createPacMan();
		createBlinky(pacMan);
		createPinky(pacMan);
		createInky(pacMan);
		createClyde(pacMan);

		setGhostActive(GhostName.BLINKY, true);
		setGhostActive(GhostName.PINKY, true);
		setGhostActive(GhostName.INKY, true);
		setGhostActive(GhostName.CLYDE, true);
	}

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(game, maze, maze.pacManHome, pacManInterests);
		pacMan.setNavigation(PacMan.State.NORMAL, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setNavigation(PacMan.State.EMPOWERED, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		return pacMan;
	}

	private void createBlinky(PacMan pacMan) {
		Ghost blinky = new Ghost(GhostName.BLINKY, pacMan, game, maze, maze.blinkyHome, RED_GHOST);
		blinky.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		blinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.SAFE, bounce());
		ghostsByName.put(blinky.getName(), blinky);
	}

	private void createPinky(PacMan pacMan) {
		Ghost pinky = new Ghost(GhostName.PINKY, pacMan, game, maze, maze.pinkyHome, PINK_GHOST);
		pinky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		pinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.SAFE, bounce());
		ghostsByName.put(pinky.getName(), pinky);
	}

	private void createInky(PacMan pacMan) {
		Ghost inky = new Ghost(GhostName.INKY, pacMan, game, maze, maze.inkyHome, TURQUOISE_GHOST);
		inky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		inky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.SAFE, bounce());
		ghostsByName.put(inky.getName(), inky);
	}

	private void createClyde(PacMan pacMan) {
		Ghost clyde = new Ghost(GhostName.CLYDE, pacMan, game, maze, maze.clydeHome, ORANGE_GHOST);
		clyde.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		clyde.setNavigation(Ghost.State.AFRAID, goHome());
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.SAFE, bounce());
		ghostsByName.put(clyde.getName(), clyde);
	}

	public void initActors() {
		pacMan.init();

		activeGhostsByName.values().forEach(ghost -> {
			ghost.init();
			ghost.setMazePosition(ghost.homeTile);
			ghost.getSprites().forEach(Sprite::resetAnimation);
			ghost.setSpeed(game::getGhostSpeed);
		});
		getActiveGhost(GhostName.BLINKY).ifPresent(blinky -> blinky.setDir(Top4.E));
		getActiveGhost(GhostName.PINKY).ifPresent(pinky -> pinky.setDir(Top4.S));
		getActiveGhost(GhostName.INKY).ifPresent(inky -> inky.setDir(Top4.N));
		getActiveGhost(GhostName.CLYDE).ifPresent(clyde -> clyde.setDir(Top4.N));
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
		activeGhostsByName.values().forEach(Ghost::update);
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

	public boolean isGhostActive(GhostName name) {
		return activeGhostsByName.containsKey(name);
	}

	public void setGhostActive(GhostName name, boolean activate) {
		Ghost ghost = ghostsByName.get(name);
		if (activate) {
			activeGhostsByName.put(name, ghost);
			pacManInterests.add(ghost);
		} else {
			activeGhostsByName.remove(name);
			pacManInterests.remove(ghost);
		}
	}

	public Stream<Ghost> getActiveGhosts() {
		return activeGhostsByName.values().stream();
	}

	public Optional<Ghost> getActiveGhost(GhostName name) {
		return Optional.ofNullable(activeGhostsByName.get(name));
	}

	public void showInfo(String text, Color color) {
		infoText = text;
		infoTextColor = color;
	}

	public void hideInfo() {
		this.infoText = null;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public void addBonus(Bonus bonus, int ticks) {
		this.bonus = bonus;
		bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
		bonusTimeLeft = ticks;
		pacManInterests.add(bonus);
	}

	public void removeBonus() {
		if (bonus != null) {
			bonus = null;
			bonusTimeLeft = 0;
			pacManInterests.remove(bonus);
		}
	}

	public void consumeBonusAfter(int ticks) {
		if (bonus != null) {
			bonus.setHonored();
			bonusTimeLeft = ticks;
		}
	}

	@Override
	public void enableAnimation(boolean on) {
		super.enableAnimation(on);
		energizer.enableAnimation(on);
		pacMan.enableAnimation(on);
		activeGhostsByName.values().forEach(ghost -> ghost.enableAnimation(on));
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			s_flashing.draw(g);
		} else {
			s_normal.draw(g);
			maze.tiles().filter(tile -> isFood(maze.getContent(tile))).forEach(tile -> drawFood(g, tile));
			activeGhostsByName.values().stream().filter(ghost -> ghost.getState() != Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			activeGhostsByName.values().stream().filter(ghost -> ghost.getState() == Ghost.State.DYING)
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
		Graphics2D g2 = (Graphics2D)g.create();
		Tile tile = maze.infoTile;
		g2.translate((tile.col + 1) * TS, tile.row * TS + TS / 4);
		g2.setFont(Assets.font("scoreFont"));
		g2.setColor(infoTextColor);
		Rectangle2D box = g2.getFontMetrics().getStringBounds(infoText, g2);
		g2.drawString(infoText, (int) (-box.getWidth() / 2), (int) (box.getHeight() / 2));
		g2.dispose();
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