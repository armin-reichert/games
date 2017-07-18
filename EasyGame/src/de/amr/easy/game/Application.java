package de.amr.easy.game;

import static java.awt.event.KeyEvent.VK_CONTROL;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.config.AppSettings;
import de.amr.easy.game.entity.EntitySet;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.KeyboardHandler;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.game.ui.ApplicationShell;
import de.amr.easy.game.view.DefaultView;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewManager;

/**
 * Application base class. To start an application, create an application instance, defined its
 * settings and call the {@link #launch(Application)} method.
 * 
 * @author Armin Reichert
 */
public abstract class Application {

	/**
	 * Starts the given application inside a window or in full-screen mode according to the settings
	 * defined after its creation.
	 * 
	 * @param app
	 *          the application
	 */
	public static void launch(Application app) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
			} catch (Exception e) {
				LOG.warning("Could not set Nimbus Look&Feel");
			}
			ApplicationShell shell = new ApplicationShell(app);
			shell.show();
			app.start();
		});
	}

	private static final int PAUSE_TOGGLE_KEY = KeyEvent.VK_P;

	/** A logger that may be used by any application. */
	public static final Logger LOG = Logger.getLogger(Application.class.getName());

	/** The settings of this application. */
	public final AppSettings settings = new AppSettings();

	/** The assets used by this application. */
	public final Assets assets = new Assets();

	/** The set of entities used by this application. */
	public final EntitySet entities = new EntitySet();

	/** The views of this application. */
	public final ViewManager views = new ViewManager();

	/** The pulse (tact) of this application. */
	public final Pulse pulse = new Pulse(this::update, this::render);

	/** The collision handler of this application. */
	public final CollisionHandler collisionHandler = new CollisionHandler();

	private boolean paused;
	private ApplicationShell shell;
	private View defaultView;

	/**
	 * Base class constructor. By default, applications run at 60 frames/second.
	 */
	protected Application() {
		pulse.setFrequency(60);
		defaultView = new DefaultView(this);
		LOG.info("Application " + getClass().getSimpleName() + " created.");
	}

	/** Called when the application should be initialized. */
	public abstract void init();

	/** Called after initialization and starts the pulse. */
	private final void start() {
		defaultView.init();
		LOG.info("Default view initialized.");
		init();
		LOG.info("Application initialized.");
		pulse.start();
		LOG.info("Application started.");
	}

	private final void pause(boolean state) {
		paused = state;
		LOG.info("Application" + (state ? " paused." : " resumed."));
	}

	/**
	 * Exits the application and the Java VM.
	 */
	public final void exit() {
		pulse.stop();
		LOG.info("Application terminated.");
		System.exit(0);
	}

	private void update() {
		KeyboardHandler.poll();
		if (Keyboard.keyDown(VK_CONTROL) && Keyboard.keyPressedOnce(PAUSE_TOGGLE_KEY)) {
			pause(!paused);
		}
		if (!paused) {
			if (views.current() != null) {
				collisionHandler.update();
				views.current().update();
			} else {
				defaultView.update();
			}
		}
	}

	private void render() {
		View currentView = views.current();
		shell.draw(currentView != null ? currentView : defaultView);
	}

	/**
	 * Sets the application shell. Not to be called by application code.
	 * 
	 * @param shell
	 */
	public void setShell(ApplicationShell shell) {
		this.shell = shell;
	}

	/**
	 * Returns the application shell.
	 * 
	 * @return the application shell
	 */
	public ApplicationShell getShell() {
		return shell;
	}

	/**
	 * The width of this application (without scaling).
	 * 
	 * @return the width in pixels
	 */
	public int getWidth() {
		return settings.width;
	}

	/**
	 * The height of this application (without scaling).
	 * 
	 * @return the height in pixels
	 */
	public int getHeight() {
		return settings.height;
	}

	/**
	 * Tells if the application is paused.
	 * 
	 * @return if the application is paused
	 */
	public boolean isPaused() {
		return paused;
	}
}