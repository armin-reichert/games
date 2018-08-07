package de.amr.games.pacman.actor;

import static de.amr.easy.game.Application.LOG;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.GameEventListener;
import de.amr.games.pacman.controller.event.core.GameEventManager;
import de.amr.games.pacman.model.Game;

public class GameActors {

	private final Game game;
	private final GameEventManager eventMgr;
	private final PacMan pacMan;
	private final Map<GhostName, Ghost> ghostsByName = new EnumMap<>(GhostName.class);
	private final Map<GhostName, Ghost> activeGhostsByName = new EnumMap<>(GhostName.class);

	public GameActors(Game game) {
		this.game = game;
		this.eventMgr = new GameEventManager("[GameActorEvents]");
		pacMan = createPacMan();
		createBlinky(pacMan);
		createPinky(pacMan);
		createInky(pacMan);
		createClyde(pacMan);
		setGhostActive(GhostName.BLINKY, true);
		setGhostActive(GhostName.PINKY, false);
		setGhostActive(GhostName.INKY, false);
		setGhostActive(GhostName.CLYDE, false);
		pacMan.getStateMachine().traceTo(LOG);
		getActiveGhosts().forEach(ghost -> ghost.getStateMachine().traceTo(LOG));
	}
	
	public void addEventHandler(GameEventListener observer) {
		eventMgr.subscribe(observer);
	}

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(game, eventMgr, game.maze.pacManHome);
		pacMan.setNavigation(PacMan.State.NORMAL, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		pacMan.setNavigation(PacMan.State.EMPOWERED, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		return pacMan;
	}

	private void createBlinky(PacMan pacMan) {
		Ghost blinky = new Ghost(GhostName.BLINKY, pacMan, game, eventMgr, game.maze.blinkyHome, RED_GHOST);
		ghostsByName.put(blinky.getName(), blinky);
		blinky.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		blinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.SAFE, bounce());
	}

	private void createPinky(PacMan pacMan) {
		Ghost pinky = new Ghost(GhostName.PINKY, pacMan, game, eventMgr, game.maze.pinkyHome, PINK_GHOST);
		ghostsByName.put(pinky.getName(), pinky);
		pinky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		pinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.SAFE, bounce());
	}

	private void createInky(PacMan pacMan) {
		Ghost inky = new Ghost(GhostName.INKY, pacMan, game, eventMgr, game.maze.inkyHome, TURQUOISE_GHOST);
		ghostsByName.put(inky.getName(), inky);
		inky.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		inky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.SAFE, bounce());
	}

	private void createClyde(PacMan pacMan) {
		Ghost clyde = new Ghost(GhostName.CLYDE, pacMan, game, eventMgr, game.maze.clydeHome, ORANGE_GHOST);
		ghostsByName.put(clyde.getName(), clyde);
		clyde.setNavigation(Ghost.State.AGGRO, ambush(pacMan)); // TODO
		clyde.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.SAFE, bounce());
	}

	public void init() {
		pacMan.init();
		activeGhostsByName.values().forEach(ghost -> {
			ghost.init();
		});
		getActiveGhost(GhostName.BLINKY).ifPresent(blinky -> blinky.setDir(Top4.E));
		getActiveGhost(GhostName.PINKY).ifPresent(pinky -> pinky.setDir(Top4.S));
		getActiveGhost(GhostName.INKY).ifPresent(inky -> inky.setDir(Top4.N));
		getActiveGhost(GhostName.CLYDE).ifPresent(clyde -> clyde.setDir(Top4.N));
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
			ghost.init();
		} else {
			activeGhostsByName.remove(name);
		}
	}

	public Stream<Ghost> getActiveGhosts() {
		return activeGhostsByName.values().stream();
	}

	public Optional<Ghost> getActiveGhost(GhostName name) {
		return Optional.ofNullable(activeGhostsByName.get(name));
	}

}
