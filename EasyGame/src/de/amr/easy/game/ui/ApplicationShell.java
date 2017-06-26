package de.amr.easy.game.ui;

import static de.amr.easy.game.Application.LOG;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.KeyboardHandler;
import de.amr.easy.game.view.Drawable;

public class ApplicationShell implements PropertyChangeListener {

	private final Application app;
	private final Canvas canvas;
	private final JFrame frame;
	private final GraphicsDevice device;
	private BufferStrategy buffer;
	private boolean fullScreen;
	private int ups, fps;

	public ApplicationShell(Application app) {
		this.app = app;
		app.setShell(this);
		app.motor.addRenderListener(this);
		app.motor.addUpdateListener(this);
		fullScreen = app.settings.fullScreenOnStart;
		device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		canvas = createCanvas();
		frame = createFrame();
		updateTitle();
		LOG.info("Application shell created.");
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if ("ups".equals(e.getPropertyName())) {
			ups = (int) e.getNewValue();
		} else if ("fps".equals(e.getPropertyName())) {
			fps = (int) e.getNewValue();
		}
		EventQueue.invokeLater(this::updateTitle);
	}

	private void updateTitle() {
		frame.setTitle(app.settings.title + " - " + fps + " frames/s" + " - " + ups + " updates/s");
	}

	public void show() {
		if (fullScreen) {
			if (app.settings.fullScreenMode == null) {
				LOG.info("Cannot enter full-screen mode: No full-screen mode specified.");
				return;
			}
			DisplayMode mode = app.settings.fullScreenMode.getDisplayMode();
			if (!isValidDisplayMode(mode)) {
				LOG.info("Cannot enter full-screen mode: Display mode not supported: " + format(mode));
				return;
			}
			showFullScreen();
		} else {
			showAsWindow();
		}
	}

	public int getWidth() {
		return fullScreen ? frame.getWidth() : canvas.getWidth();
	}

	public int getHeight() {
		return fullScreen ? frame.getHeight() : canvas.getHeight();
	}

	public void draw(Drawable view) {
		do {
			do {
				Graphics2D g = null;
				try {
					g = (Graphics2D) buffer.getDrawGraphics();
					if (g != null) {
						g.setColor(app.settings.bgColor);
						g.fillRect(0, 0, getWidth(), getHeight());
						if (fullScreen) {
							DisplayMode mode = app.settings.fullScreenMode.getDisplayMode();
							float scaledWidth = app.settings.width * app.settings.scale;
							if (mode.getWidth() > scaledWidth) {
								g.translate((mode.getWidth() - scaledWidth) / 2, 0);
							}
						}
						g.scale(app.settings.scale, app.settings.scale);
						view.draw(g);
					}
				} catch (Exception x) {
					 x.printStackTrace(System.err);
					 LOG.info("Exception occured when rendering current view");
				} finally {
					if (g != null) {
						g.dispose();
					}
				}
			} while (buffer.contentsRestored());
			try {
				buffer.show();
			} catch (Exception x) {
				// Log.info("Exception occured showing buffer");
			}
		} while (buffer.contentsLost());
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame(app.settings.title);
		frame.setBackground(app.settings.bgColor);
		frame.setResizable(false);
		frame.setFocusable(true);
		frame.setIgnoreRepaint(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(canvas, BorderLayout.CENTER);
		KeyboardHandler.handleKeyEventsFor(frame);
		frame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ESCAPE) {
					app.exit();
				} else if (key == KeyEvent.VK_F11) {
					toggleFullScreen();
				} else if (key == KeyEvent.VK_F2) {
					showControlDialog();
				}
			}
		});
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				LOG.info("Application window closing, app will exit...");
				app.exit();
			}
		});
		return frame;
	}

	private AppControlDialog controlDialog;

	private void showControlDialog() {
		if (controlDialog == null) {
			controlDialog = new AppControlDialog(frame, app);
		}
		controlDialog.setVisible(true);
	}

	private Canvas createCanvas() {
		Canvas canvas = new Canvas();
		Dimension size = new Dimension(Math.round(app.settings.width * app.settings.scale),
				Math.round(app.settings.height * app.settings.scale));
		canvas.setPreferredSize(size);
		canvas.setSize(size);
		canvas.setBackground(app.settings.bgColor);
		canvas.setIgnoreRepaint(true);
		canvas.setFocusable(false);
		return canvas;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}

	private void toggleFullScreen() {
		fullScreen = !fullScreen;
		show();
	}

	private void showFullScreen() {
		if (!device.isFullScreenSupported()) {
			LOG.info("Full-screen mode not supported for this device.");
			return;
		}
		DisplayMode mode = app.settings.fullScreenMode.getDisplayMode();
		frame.setVisible(false);
		frame.dispose();
		frame.setUndecorated(true);
		device.setFullScreenWindow(frame);
		device.setDisplayMode(mode);
		frame.createBufferStrategy(2);
		buffer = frame.getBufferStrategy();
		LOG.info("Full-screen mode: " + format(mode));
	}

	private void showAsWindow() {
		device.setFullScreenWindow(null);
		frame.dispose();
		frame.setUndecorated(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		canvas.createBufferStrategy(2);
		canvas.requestFocus();
		buffer = canvas.getBufferStrategy();
		LOG.info("Window-mode: " + app.getWidth() + "x" + app.getHeight());
	}

	private boolean isValidDisplayMode(DisplayMode displayMode) {
		for (DisplayMode dm : device.getDisplayModes()) {
			if (dm.getWidth() == displayMode.getWidth() && dm.getHeight() == displayMode.getHeight()
					&& dm.getBitDepth() == displayMode.getBitDepth()) {
				return true;
			}
		}
		return false;
	}

	private String format(DisplayMode mode) {
		return String.format("%d x %d, depth: %d, refresh rate: %d", mode.getWidth(), mode.getHeight(), mode.getBitDepth(),
				mode.getRefreshRate());
	}
}
