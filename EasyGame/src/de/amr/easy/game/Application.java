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
 * Application base class.
 * 
 * @author Armin Reichert
 */
public abstract class Application {

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

	public static final Logger LOG = Logger.getLogger(Application.class.getName());
	private static final int PAUSE_TOGGLE_KEY = KeyEvent.VK_P;

	public final AppSettings settings = new AppSettings();
	public final Assets assets = new Assets();
	public final EntitySet entities = new EntitySet();
	public final ViewManager views = new ViewManager();
	public final Pulse pulse = new Pulse(this::update, this::render);
	public final CollisionHandler collisionHandler = new CollisionHandler();

	private boolean paused;
	private ApplicationShell shell;
	private View defaultView;

	public Application() {
		pulse.setFrequency(60);
		defaultView = new DefaultView(this);
		LOG.info("Application " + getClass().getSimpleName() + " created.");
	}

	public abstract void init();

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

	public void setShell(ApplicationShell shell) {
		this.shell = shell;
	}

	public ApplicationShell getShell() {
		return shell;
	}

	public int getWidth() {
		return settings.width;
	}

	public int getHeight() {
		return settings.height;
	}

	public boolean isPaused() {
		return paused;
	}
}