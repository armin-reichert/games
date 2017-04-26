package de.amr.easy.game;

import static java.awt.event.KeyEvent.VK_CONTROL;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.config.ApplicationSettings;
import de.amr.easy.game.entity.EntitySet;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.game.input.Key;
import de.amr.easy.game.input.KeyboardHandler;
import de.amr.easy.game.timing.GameLoop;
import de.amr.easy.game.ui.ApplicationShell;
import de.amr.easy.game.view.DefaultView;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewManager;

public abstract class Application {

	private static final int PAUSE_TOGGLE_KEY = KeyEvent.VK_P;

	public static final Logger Log = Logger.getLogger(Application.class.getName());
	public static final ApplicationSettings Settings = new ApplicationSettings();
	public static final Assets Assets = new Assets();
	public static final EntitySet Entities = new EntitySet();
	public static final ViewManager Views = new ViewManager();
	public static GameLoop GameLoop;

	private boolean paused;
	private ApplicationShell shell;
	private View defaultView;

	public static void launch(Application app) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
			} catch (Exception e) {
				Log.warning("Could not set Nimbus Look&Feel");
			}
			ApplicationShell shell = new ApplicationShell(app);
			shell.show();
			app.start();
		});
	}

	protected Application() {
		GameLoop = new GameLoop(this::update, this::render);
		GameLoop.setFrameRate(Settings.fps);
		defaultView = new DefaultView(this);
		Log.info("Application " + getClass().getSimpleName() + " created.");
	}

	private final void _init() {
		defaultView.init();
		init();
		Log.info("Application initialized.");
		Log.info("Application Assets:\n" + Assets.overview());
	}

	protected abstract void init();

	public final void start() {
		_init();
		GameLoop.start();
		Log.info("Application started.");
	}

	public final void stop() {
		GameLoop.stop();
		Log.info("Application stopped.");
	}

	public final void pause(boolean state) {
		paused = state;
		Log.info("Application" + (state ? " paused." : " resumed."));
	}

	public final void exit() {
		stop();
		Log.info("Application terminated.");
		System.exit(0);
	}

	private void update() {
		KeyboardHandler.poll();
		if (Key.down(VK_CONTROL) && Key.pressedOnce(PAUSE_TOGGLE_KEY)) {
			pause(!paused);
		}
		if (!paused) {
			if (Views.current() != null) {
				CollisionHandler.update();
				Views.current().update();
			} else {
				defaultView.update();
			}
		}
	}

	private void render() {
		View currentView = Views.current();
		shell.draw(currentView != null ? currentView : defaultView);
	}

	public void setShell(ApplicationShell shell) {
		this.shell = shell;
	}

	public int getWidth() {
		return Settings.width;
	}

	public int getHeight() {
		return Settings.height;
	}

	public boolean isPaused() {
		return paused;
	}
}