package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.behavior.ChaseTarget;
import de.amr.games.pacman.controller.behavior.DoNothing;
import de.amr.games.pacman.controller.behavior.Flee;
import de.amr.games.pacman.controller.behavior.GoHome;
import de.amr.games.pacman.controller.behavior.KeyboardSteering;
import de.amr.games.pacman.controller.behavior.LackingBehindMoveBehavior;
import de.amr.games.pacman.controller.behavior.MoodyMoveBehavior;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostDeadEndsEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.PacMan.State;

public class PlayScene extends ActiveScene<PacManApp> implements GameEventListener {

	private Game game;
	private Maze maze;
	private PacMan pacMan;
	private Ghost blinky, pinky, inky, clyde;
	private MazeUI mazeUI;
	private HUD hud;
	private StatusUI status;

	public PlayScene(PacManApp app) {
		super(app);
	}

	@Override
	public void processGameEvent(GameEvent e) {
		if (e instanceof GhostContactEvent) {
			onGhostContact((GhostContactEvent) e);
		} else if (e instanceof FoodFoundEvent) {
			onFoodFound((FoodFoundEvent) e);
		} else if (e instanceof BonusFoundEvent) {
			onBonusFound((BonusFoundEvent) e);
		} else if (e instanceof PacManDiedEvent) {
			onPacManDied((PacManDiedEvent) e);
		} else if (e instanceof GhostFrightenedEndsEvent) {
			onGhostFrightenedEnds((GhostFrightenedEndsEvent) e);
		} else if (e instanceof GhostDeadEndsEvent) {
			onGhostDeadEnds((GhostDeadEndsEvent) e);
		}
	}

	public PacManApp getApp() {
		return app;
	}

	public Game getGame() {
		return game;
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	private void createUI() {
		hud = new HUD(game);
		mazeUI = new MazeUI(getWidth(), getHeight() - 5 * TS, maze, pacMan, blinky, pinky, inky, clyde);
		status = new StatusUI(game);
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * TS);
		status.tf.moveTo(0, getHeight() - 2 * TS);
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		mazeUI.draw(g);
		status.draw(g);
	}

	@Override
	public void init() {
		game = new Game();
		maze = Maze.of(Assets.text("maze.txt"));
		createEntities();
		createUI();
		initEntities();
	}

	private void createEntities() {
		blinky = new Ghost(maze, "Blinky", RED_GHOST);
		pinky = new Ghost(maze, "Pinky", PINK_GHOST);
		inky = new Ghost(maze, "Inky", BLUE_GHOST);
		clyde = new Ghost(maze, "Clyde", ORANGE_GHOST);
		pacMan = new PacMan(maze);
		getGhosts().forEach(pacMan::addEnemy);

		getGhosts().forEach(ghost -> ghost.addObserver(this));
		pacMan.addObserver(this);

		// define move behavior
		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(Ghost.State.STARRED, new DoNothing(ghost));
			ghost.setMoveBehavior(Ghost.State.FRIGHTENED, new Flee(maze, ghost, pacMan));
		});
		blinky.setMoveBehavior(Ghost.State.ATTACKING, new ChaseTarget(maze, blinky, pacMan));
		blinky.setMoveBehavior(Ghost.State.DEAD, new GoHome(maze, blinky, maze.blinkyHome));
		// pinky.setMoveBehavior(Ghost.State.ATTACKING, new AmbushTarget(maze, pinky, pacMan));
		pinky.setMoveBehavior(Ghost.State.ATTACKING, new ChaseTarget(maze, pinky, pacMan));
		pinky.setMoveBehavior(Ghost.State.DEAD, new GoHome(maze, pinky, maze.pinkyHome));
		inky.setMoveBehavior(Ghost.State.ATTACKING, new MoodyMoveBehavior(inky));
		inky.setMoveBehavior(Ghost.State.DEAD, new GoHome(maze, inky, maze.inkyHome));
		clyde.setMoveBehavior(Ghost.State.ATTACKING, new LackingBehindMoveBehavior(clyde));
		clyde.setMoveBehavior(Ghost.State.DEAD, new GoHome(maze, clyde, maze.clydeHome));

		pacMan.setMoveBehavior(PacMan.State.ALIVE, new KeyboardSteering(pacMan, VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
	}

	private void initEntities() {
		blinky.setTile(maze.blinkyHome);
		blinky.setMoveDirection(Top4.E);

		pinky.setTile(maze.pinkyHome);
		pinky.setMoveDirection(Top4.S);

		inky.setTile(maze.inkyHome);
		inky.setMoveDirection(Top4.N);

		clyde.setTile(maze.clydeHome);
		clyde.setMoveDirection(Top4.N);

		getGhosts().forEach(ghost -> {
			ghost.setSpeed(7 * TS / 60);
			ghost.setState(Ghost.State.ATTACKING);
		});

		pacMan.setTile(maze.pacManHome);
		pacMan.setSpeed(9 * TS / 60);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);
		pacMan.setState(State.ALIVE);
	}

	private void initLevel() {
		maze.loadContent();
		game.dotsEatenInLevel = 0;
		initEntities();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			Debug.DEBUG = !Debug.DEBUG;
		}
		pacMan.update();
		getGhosts().forEach(Ghost::update);
	}

	private void onPacManDied(PacManDiedEvent e) {
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		if (pacMan.getState() == PacMan.State.DYING) {
			return;
		}
		if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
			e.ghost.setState(Ghost.State.DEAD);
			Debug.log(() -> String.format("PacMan killed %s at tile %s", e.ghost.getName(), e.tile));
		} else if (e.ghost.getState() == Ghost.State.DEAD) {
			// do nothing
		} else {
			pacMan.setState(State.DYING);
			pacMan.setSpeed(0);
			pacMan.enemies().forEach(enemy -> {
				enemy.setSpeed(0);
				enemy.setAnimated(false);
				enemy.setState(Ghost.State.STARRED);
			});
			game.lives -= 1;
			Debug.log(() -> String.format("PacMan got killed by %s at tile %s", e.ghost.getName(), e.tile));
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		maze.setContent(e.tile, EMPTY);
		game.dotsEatenInLevel += 1;
		if (game.dotsEatenInLevel == 70) {
			maze.setContent(maze.bonusTile, Tile.BONUS_CHERRIES);
		} else if (game.dotsEatenInLevel == 170) {
			maze.setContent(maze.bonusTile, Tile.BONUS_STRAWBERRY);
		}
		game.score += e.food == ENERGIZER ? 50 : 10;
		if (maze.tiles().map(maze::getContent).noneMatch(c -> c == PELLET || c == ENERGIZER)) {
			++game.level;
			initLevel();
		} else if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at tile %s", e.tile));
			pacMan.enemies().forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		maze.setContent(e.tile, EMPTY);
		Debug.log(() -> String.format("PacMan found bonus %s at tile=%s", e.bonus, e.tile));
	}

	private void onGhostFrightenedEnds(GhostFrightenedEndsEvent e) {
		// TODO depends on currently running wave (scattering or attacking wave)
		e.ghost.setState(Ghost.State.ATTACKING);
	}

	private void onGhostDeadEnds(GhostDeadEndsEvent e) {
		// TODO depends on currently running wave (scattering or attacking wave)
		e.ghost.setState(Ghost.State.ATTACKING);
	}

}