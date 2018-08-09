package de.amr.games.pacman.actor;

import static de.amr.games.pacman.routing.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.routing.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.routing.impl.NavigationSystem.chase;
import static de.amr.games.pacman.routing.impl.NavigationSystem.flee;
import static de.amr.games.pacman.routing.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.routing.impl.NavigationSystem.goHome;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.TURQUOISE_GHOST;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.EventManager;
import de.amr.games.pacman.controller.event.core.Observer;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.routing.Navigation;

public class GameActors {

	private final Game game;
	private final EventManager<GameEvent> eventMgr;
	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Ghost> activeGhosts = new HashSet<>();

	public GameActors(Game game) {
		this.game = game;
		eventMgr = new EventManager<>("[GameActors]");
		pacMan = createPacMan();
		blinky = createBlinky();
		pinky = createPinky();
		inky = createInky();
		clyde = createClyde();
	}

	public void subscribe(Observer<GameEvent> observer) {
		eventMgr.subscribe(observer);
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

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(game, eventMgr);
		Navigation<MazeMover<?>> manual = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setNavigation(PacMan.State.VULNERABLE, manual);
		pacMan.setNavigation(PacMan.State.STEROIDS, manual);
		return pacMan;
	}

	private Ghost createBlinky() {
		Ghost blinky = new Ghost(GhostName.BLINKY, pacMan, game, game.maze.blinkyHome, RED_GHOST);
		blinky.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		blinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.SAFE, bounce());
		return blinky;
	}

	private Ghost createPinky() {
		Ghost pinky = new Ghost(GhostName.PINKY, pacMan, game, game.maze.pinkyHome, PINK_GHOST);
		pinky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		pinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.SAFE, bounce());
		return pinky;
	}

	private Ghost createInky() {
		Ghost inky = new Ghost(GhostName.INKY, pacMan, game, game.maze.inkyHome, TURQUOISE_GHOST);
		inky.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		inky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.SAFE, bounce());
		return inky;
	}

	private Ghost createClyde() {
		Ghost clyde = new Ghost(GhostName.CLYDE, pacMan, game, game.maze.clydeHome, ORANGE_GHOST);
		clyde.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		clyde.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.SAFE, bounce());
		return clyde;
	}

	public void init() {
		pacMan.init();
		activeGhosts.forEach(ghost -> ghost.init());
		blinky.setDir(Top4.E);
		pinky.setDir(Top4.S);
		inky.setDir(Top4.N);
		clyde.setDir(Top4.N);
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public boolean isGhostActive(Ghost ghost) {
		return activeGhosts.contains(ghost);
	}

	public void setGhostActive(Ghost ghost, boolean active) {
		if (active) {
			activeGhosts.add(ghost);
			ghost.init();
		} else {
			activeGhosts.remove(ghost);
		}
	}

	public Stream<Ghost> getActiveGhosts() {
		return activeGhosts.stream();
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}
}