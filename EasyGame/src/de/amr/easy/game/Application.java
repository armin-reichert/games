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
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.KeyboardHandler;
import de.amr.easy.game.timing.Motor;
import de.amr.easy.game.ui.ApplicationShell;
import de.amr.easy.game.view.DefaultView;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewManager;

public abstract class Application {

	private static final int PAUSE_TOGGLE_KEY = KeyEvent.VK_P;

	public static final Logger Log = Logger.getLogger(Application.class.getName());

	public final ApplicationSettings settings = new ApplicationSettings();

	public final Assets assets = new Assets();

	public final EntitySet entities = new EntitySet();

	public final ViewManager views = new ViewManager();

	public final Motor motor;

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
		motor = new Motor(this::update, this::render);
		motor.setFrequency(60);
		defaultView = new DefaultView(this);
		Log.info("Application " + getClass().getSimpleName() + " created.");
	}

	protected abstract void init();

	public final void start() {
		defaultView.init();
		init();
		Log.info("Application Assets:\n" + assets.overview());
		Log.info("Application initialized.");
		motor.start();
		Log.info("Application started.");
	}

	public final void stop() {
		motor.stop();
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
		if (Keyboard.down(VK_CONTROL) && Keyboard.pressedOnce(PAUSE_TOGGLE_KEY)) {
			pause(!paused);
		}
		if (!paused) {
			if (views.current() != null) {
				CollisionHandler.update();
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