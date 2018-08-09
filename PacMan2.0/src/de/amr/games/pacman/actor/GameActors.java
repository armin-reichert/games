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

/**
 * Factory and container for the game actors.
 * 
 * @author Armin Reichert
 */
public class GameActors {

	public enum Name {
		PacMan, Blinky, Pinky, Inky, Clyde
	}

	private static PacMan createPacMan(Game game, EventManager<GameEvent> eventMgr) {
		PacMan pacMan = new PacMan(game);
		Navigation<MazeMover<?>> keySteering = followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT);
		pacMan.setNavigation(PacMan.State.VULNERABLE, keySteering);
		pacMan.setNavigation(PacMan.State.STEROIDS, keySteering);
		pacMan.setEventMgr(eventMgr);
		return pacMan;
	}

	private static Ghost createBlinky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(Name.Blinky, pacMan, game, game.maze.blinkyHome, Top4.E, RED_GHOST);
		ghost.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		ghost.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		ghost.setNavigation(Ghost.State.DEAD, goHome());
		ghost.setNavigation(Ghost.State.SAFE, bounce());
		return ghost;
	}

	private static Ghost createPinky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(Name.Pinky, pacMan, game, game.maze.pinkyHome, Top4.S, PINK_GHOST);
		ghost.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		ghost.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		ghost.setNavigation(Ghost.State.DEAD, goHome());
		ghost.setNavigation(Ghost.State.SAFE, bounce());
		return ghost;
	}

	private static Ghost createInky(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(Name.Inky, pacMan, game, game.maze.inkyHome, Top4.N, TURQUOISE_GHOST);
		ghost.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		ghost.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		ghost.setNavigation(Ghost.State.DEAD, goHome());
		ghost.setNavigation(Ghost.State.SAFE, bounce());
		return ghost;
	}

	private static Ghost createClyde(Game game, PacMan pacMan) {
		Ghost ghost = new Ghost(Name.Clyde, pacMan, game, game.maze.clydeHome, Top4.N, ORANGE_GHOST);
		ghost.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		ghost.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		ghost.setNavigation(Ghost.State.DEAD, goHome());
		ghost.setNavigation(Ghost.State.SAFE, bounce());
		return ghost;
	}

	private final EventManager<GameEvent> eventMgr;
	private final PacMan pacMan;
	private final Ghost blinky, pinky, inky, clyde;
	private final Set<Ghost> activeGhosts = new HashSet<>();

	public GameActors(Game game) {
		eventMgr = new EventManager<>("[GameActorEvents]");
		pacMan = createPacMan(game, eventMgr);
		blinky = createBlinky(game, pacMan);
		pinky = createPinky(game, pacMan);
		inky = createInky(game, pacMan);
		clyde = createClyde(game, pacMan);
	}

	public void subscribeActorEvents(Observer<GameEvent> observer) {
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

	public void init() {
		pacMan.init();
		activeGhosts.forEach(Ghost::init);
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