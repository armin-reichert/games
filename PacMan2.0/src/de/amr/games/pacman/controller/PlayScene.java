package de.amr.games.pacman.controller;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
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
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.behavior.Ambush;
import de.amr.games.pacman.controller.behavior.Bounce;
import de.amr.games.pacman.controller.behavior.Chase;
import de.amr.games.pacman.controller.behavior.Forward;
import de.amr.games.pacman.controller.behavior.Flee;
import de.amr.games.pacman.controller.behavior.FollowKeyboard;
import de.amr.games.pacman.controller.behavior.GoHome;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostDeadIsOverEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Debug;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.HUD;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.StatusUI;

public class PlayScene extends ActiveScene<PacManApp> implements GameEventListener {

	public enum State {
		READY, RUNNING, COMPLETE
	};

	private State state;

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
		} else if (e instanceof GhostDeadIsOverEvent) {
			onGhostDeadIsOver((GhostDeadIsOverEvent) e);
		} else if (e instanceof GhostRecoveringCompleteEvent) {
			onGhostRecoveringComplete((GhostRecoveringCompleteEvent) e);
		}
	}

	public PacManApp getApp() {
		return app;
	}

	public Game getGame() {
		return game;
	}

	public MazeUI getMazeUI() {
		return mazeUI;
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
		Debug.draw(g, this);
	}

	@Override
	public void init() {
		state = State.READY;
		game = new Game();
		maze = Maze.of(Assets.text("maze.txt"));
		createEntities();
		createUI();
		initEntities();
		pacMan.enableAnimation(false);
		mazeUI.enableAnimation(false);
		getGhosts().forEach(ghost -> ghost.enableAnimation(false));
	}

	private void createEntities() {
		blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		inky = new Ghost(maze, "Inky", BLUE_GHOST, maze.inkyHome);
		clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.enemies.addAll(Arrays.asList(blinky, pinky, inky, clyde));

		getGhosts().forEach(ghost -> ghost.addObserver(this));
		pacMan.addObserver(this);

		// define move behavior
		pacMan.setMoveBehavior(PacMan.State.ALIVE, new FollowKeyboard(pacMan, VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(Ghost.State.STARRED, new Forward());
			ghost.setMoveBehavior(Ghost.State.FRIGHTENED, new Flee(pacMan));
		});
		blinky.setMoveBehavior(Ghost.State.ATTACKING, new Chase(pacMan));
		blinky.setMoveBehavior(Ghost.State.DEAD, new GoHome());
		blinky.setMoveBehavior(Ghost.State.RECOVERING, new GoHome());

		pinky.setMoveBehavior(Ghost.State.ATTACKING, new Ambush(pacMan));
		pinky.setMoveBehavior(Ghost.State.DEAD, new GoHome());
		pinky.setMoveBehavior(Ghost.State.RECOVERING, new Bounce());

		// inky.setMoveBehavior(Ghost.State.ATTACKING, new Moody());
		inky.setMoveBehavior(Ghost.State.DEAD, new GoHome());
		inky.setMoveBehavior(Ghost.State.RECOVERING, new Bounce());

		// clyde.setMoveBehavior(Ghost.State.ATTACKING, new StayBehind());
		clyde.setMoveBehavior(Ghost.State.DEAD, new GoHome());
		clyde.setMoveBehavior(Ghost.State.RECOVERING, new Bounce());
	}

	private void initEntities() {
		blinky.setMoveDirection(Top4.E);
		pinky.setMoveDirection(Top4.S);
		inky.setMoveDirection(Top4.N);
		clyde.setMoveDirection(Top4.N);
		getGhosts().forEach(ghost -> {
			ghost.setState(Ghost.State.RECOVERING);
			ghost.setTile(ghost.getHome());
			ghost.setSpeed(game::getGhostSpeed);
			ghost.enableAnimation(true);
		});
		pacMan.setState(PacMan.State.ALIVE);
		pacMan.setTile(maze.pacManHome);
		pacMan.setSpeed(game::getPacManSpeed);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);
		pacMan.enableAnimation(true);
	}

	private void initLevel() {
		maze.loadContent();
		game.dotsEatenInLevel = 0;
		initEntities();
	}

	// Game event handling

	/**
	 * Called on every clock tick.
	 */
	@Override
	public void update() {
		Debug.update(this);
		switch (state) {
		case READY:
			mazeUI.enableAnimation(true);
			pacMan.enableAnimation(true);
			getGhosts().forEach(ghost -> ghost.enableAnimation(true));
			state = State.RUNNING;
			break;
		case RUNNING:
			pacMan.update();
			getGhosts().forEach(Ghost::update);
			break;
		case COMPLETE:
			mazeUI.enableAnimation(false);
			pacMan.enableAnimation(false);
			getGhosts().forEach(ghost -> ghost.enableAnimation(false));
			if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
				game = new Game();
				state = State.READY;
			}
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onGhostContact(GhostContactEvent e) {
		switch (e.ghost.getState()) {
		case ATTACKING:
		case RECOVERING:
		case SCATTERING:
		case STARRED:
			pacMan.setState(PacMan.State.DYING);
			pacMan.enemies.forEach(enemy -> {
				enemy.enableAnimation(false);
				enemy.setState(Ghost.State.STARRED);
			});
			game.lives -= 1;
			Debug.log(() -> String.format("PacMan got killed by %s at tile %s", e.ghost.getName(), e.ghost.getTile()));
			if (game.lives == 0) {
				state = State.COMPLETE;
			}
			break;
		case DEAD:
			break;
		case FRIGHTENED:
			e.ghost.setState(Ghost.State.DEAD);
			e.ghost.setSpeed(game::getGhostSpeed);
			Debug.log(() -> String.format("PacMan killed %s at tile %s", e.ghost.getName(), e.ghost.getTile()));
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onPacManDied(PacManDiedEvent e) {
		initEntities();
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
		if (maze.tiles().map(maze::getContent).noneMatch(Tile::isFood)) {
			++game.level;
			initLevel();
		} else if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at tile %s", e.tile));
			pacMan.enemies.stream().filter(ghost -> ghost.getState() != Ghost.State.DEAD)
					.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
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

	private void onGhostDeadIsOver(GhostDeadIsOverEvent e) {
		e.ghost.setState(Ghost.State.RECOVERING);
		e.ghost.setMoveDirection(Top4.N);
	}

	private void onGhostRecoveringComplete(GhostRecoveringCompleteEvent e) {
		e.ghost.setState(Ghost.State.ATTACKING);
	}
}