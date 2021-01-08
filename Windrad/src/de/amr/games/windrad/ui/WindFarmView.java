package de.amr.games.windrad.ui;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import de.amr.games.windrad.model.WindFarm;
import de.amr.games.windrad.model.WindTurbine;

/**
 * Windpark (View).
 */
public class WindFarmView extends JPanel {

	private final WindFarm farm;
	private final List<WindTurbineView> turbineViews = new ArrayList<>();
	private final HelpView helpView = new HelpView();
	private boolean helpOn;
	private Image bgImage;
	private int selectedIndex;
	private Timer sunAnimation;
	private final MouseHandler mouseHandler = new MouseHandler();

	private class MouseHandler implements MouseListener, MouseMotionListener {

		private boolean dragging;
		private float offsetX;
		private float offsetY;
		private WindTurbine turbine;

		@Override
		public void mousePressed(MouseEvent e) {
			int modelX = e.getX() - getWidth() / 2;
			int modelY = getHeight() - e.getY();
			for (WindTurbineView view : turbineViews) {
				if (view.getTurbine().getTower().contains(modelX, modelY)) {
					turbine = view.getTurbine();
					offsetX = turbine.getPosition().x - modelX;
					offsetY = turbine.getPosition().y - modelY;
					dragging = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				dragging = false;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragging) {
				int modelX = e.getX() - getWidth() / 2;
				int modelY = getHeight() - e.getY();
				turbine.moveTo(modelX + offsetX, modelY + offsetY);
				repaint();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int modelX = e.getX() - getWidth() / 2;
			int modelY = getHeight() - e.getY();
			selectByPosition(modelX, modelY);
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}

	public WindFarmView(WindFarm farm, int width, int height) {
		this.farm = farm;
		helpOn = true;
		selectedIndex = 0;
		try (InputStream is = getClass().getResourceAsStream("/hintergrund.png")) {
			bgImage = ImageIO.read(is);
		} catch (Exception x) {
			throw new RuntimeException("Could not load background image", x);
		}
		for (WindTurbine turbine : farm.turbines) {
			turbineViews.add(new WindTurbineView(this, turbine));
		}
		createSunAnimation(farm, width, height);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addKeyBindings();
	}

	private void addKeyBindings() {
		bindKey("F1", this::action_toggleHelp);
		bindKey("SPACE", this::action_startStopSelectedTurbine);
		bindKey("shift SPACE", this::action_startAllTurbines);
		bindKey("S", this::action_selectNextTurbine);
		bindKey("N", this::action_newTurbine);
		bindKey("DELETE", this::action_removeTurbine);
		bindKey("I", this::action_changeDirection);
		bindKey("UP", this::action_moveUp);
		bindKey("DOWN", this::action_moveDown);
		bindKey("LEFT", this::action_moveLeft);
		bindKey("RIGHT", this::action_moveRight);
		bindKey("PLUS", this::action_increaseHeight);
		bindKey("MINUS", this::action_decreaseHeight);
		bindKey("control PLUS", this::action_increaseRotorLength);
		bindKey("control MINUS", this::action_decreaseRotorLength);
		bindKey("shift PLUS", this::action_increaseNacelleRadius);
		bindKey("shift MINUS", this::action_decreaseNacelleRadius);
		bindKey("Z", this::action_zwickyTurbine);
	}

	private void bindKey(String key, Consumer<ActionEvent> action) {
		getInputMap().put(KeyStroke.getKeyStroke(key), action.toString());
		getActionMap().put(action.toString(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				action.accept(e);
			}
		});
	}

	private void createSunAnimation(WindFarm farm, int width, int height) {
		farm.sunCenter.setLocation(-width / 2, height / 2);
		sunAnimation = new Timer(0, e -> {
			int deltaX = 1;
			farm.sunCenter.x += deltaX;
			if (farm.sunCenter.x > width / 2) {
				farm.sunCenter.x = -width / 2;
			}
			float radiusSquared = (float) 0.25 * (width * width + height * height);
			float xSquared = farm.sunCenter.x * farm.sunCenter.x;
			farm.sunCenter.y = (float) sqrt(radiusSquared - xSquared);
			repaint();
		});
		sunAnimation.setDelay(600);
		sunAnimation.start();
	}

	@Override
	protected void paintComponent(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		super.paintComponent(g);

		if (bgImage != null) {
			g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
		} else {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		// Koordinaten-Transformation Model -> View
		AffineTransform ot = g.getTransform();
		g.translate(getWidth() / 2, getHeight());
		g.scale(1, -1);

		// Sonne
		int sunDiameter = 40;
		g.setColor(Color.YELLOW);
		g.fillOval((int) (farm.sunCenter.x - sunDiameter / 2), (int) (farm.sunCenter.y - sunDiameter / 2), sunDiameter,
				sunDiameter);

		// Windräder
		if (!turbineViews.isEmpty()) {
			for (WindTurbineView w : turbineViews) {
				if (turbineViews.get(selectedIndex) != w) {
					w.draw(g, false);
				}
			}
			turbineViews.get(selectedIndex).draw(g, true);
		}

		g.setTransform(ot);

		// Hilfetext
		if (helpOn) {
			helpView.setLocation(getWidth() - 360, 20);
			helpView.draw(g);
		}
		g.setColor(Color.RED);
	}

	private void selectByPosition(double modelX, double modelY) {
		for (WindTurbineView view : turbineViews) {
			WindTurbine turbine = view.getTurbine();
			if (turbine.getNacelle().contains(modelX, modelY) || turbine.getTower().contains(modelX, modelY)) {
				selectedIndex = turbineViews.indexOf(view);
				break;
			}
		}
	}

	public void selectTurbine(WindTurbine turbine) {
		for (WindTurbineView view : turbineViews) {
			if (turbine == view.getTurbine()) {
				selectedIndex = turbineViews.indexOf(view);
			}
		}
	}

	public void selectNextTurbine() {
		// create [0, 1, 2, ... ]
		Integer[] sortedByX = new Integer[turbineViews.size()];
		for (int i = 0; i < sortedByX.length; ++i) {
			sortedByX[i] = i;
		}
		// sort by x-coordinate
		Arrays.sort(sortedByX, (i1, i2) -> {
			Point2D.Float pos1 = turbineViews.get(i1).getTurbine().getPosition();
			Point2D.Float pos2 = turbineViews.get(i2).getTurbine().getPosition();
			if (pos1.x == pos2.x) {
				return pos1.y < pos2.y ? -1 : pos1.y > pos2.y ? 1 : 0;
			}
			return pos1.x < pos2.x ? -1 : 1;
		});
		// find selected index in new order
		int index = 0;
		while (sortedByX[index] != selectedIndex) {
			++index;
		}
		// set selection to next index in new order
		selectedIndex = sortedByX[index + 1 < sortedByX.length ? index + 1 : 0];
		repaint();
	}

	public WindTurbineView getSelectedView() {
		return turbineViews.get(selectedIndex);
	}

	private WindTurbine getSelectedTurbine() {
		return getSelectedView().getTurbine();
	}

	// Action-Handlers

	public void action_toggleHelp(ActionEvent e) {
		helpOn = !helpOn;
		repaint();
	}

	public void action_newTurbine(ActionEvent e) {
		int y = getHeight() / 4;
		float towerHeight = getHeight() / 4;
		float towerWidthBottom = towerHeight / 10;
		float towerWidthTop = towerWidthBottom * 0.80f;
		float nacelleRadius = towerWidthTop;
		float rotorLength = towerHeight / 2 - WindTurbine.getMinBottomDistance();
		float rotorThickness = rotorLength / 10;
		WindTurbine turbine = new WindTurbine(0, y, towerHeight, towerWidthBottom, towerWidthTop, nacelleRadius,
				rotorLength, rotorThickness);
		farm.turbines.add(turbine);
		turbineViews.add(new WindTurbineView(this, turbine));
		selectTurbine(turbine);
		repaint();
	}

	public void action_removeTurbine(ActionEvent e) {
		if (farm.turbines.size() <= 1) {
			return;
		}
		WindTurbineView view = turbineViews.remove(selectedIndex);
		farm.turbines.remove(view.getTurbine());
		repaint();
		selectedIndex = 0;
	}

	public void action_selectNextTurbine(ActionEvent e) {
		selectNextTurbine();
	}

	public void action_startStopSelectedTurbine(ActionEvent e) {
		if (getSelectedView().isRunning()) {
			getSelectedView().stop();
		} else {
			getSelectedView().start();
		}
	}

	public void action_startAllTurbines(ActionEvent e) {
		for (WindTurbineView view : turbineViews) {
			if (!view.isRunning()) {
				view.start();
			}
		}
	}

	public void action_changeDirection(ActionEvent e) {
		getSelectedView().changeDirection();
	}

	public void action_moveUp(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		Point2D.Float oldPosition = turbine.getPosition();
		if (oldPosition.y + 5 < getHeight() - turbine.getTotalHeight()) {
			turbine.moveTo(oldPosition.x, oldPosition.y + 5);
			repaint();
		}
	}

	public void action_moveDown(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		Point2D.Float oldPosition = turbine.getPosition();
		if (oldPosition.y - 5 >= 0) {
			turbine.moveTo(oldPosition.x, oldPosition.y - 5);
			repaint();
		}
	}

	public void action_moveLeft(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		Point2D.Float oldPosition = turbine.getPosition();
		if (oldPosition.x - 5 > turbine.getTotalWidth() / 2 - getWidth() / 2) {
			turbine.moveTo(oldPosition.x - 5, oldPosition.y);
			repaint();
		}
	}

	public void action_moveRight(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		Point2D.Float oldPosition = turbine.getPosition();
		if (oldPosition.x + 5 < getWidth() / 2 - turbine.getTotalWidth() / 2) {
			turbine.moveTo(oldPosition.x + 5, oldPosition.y);
			repaint();
		}
	}

	public void action_increaseHeight(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		if (turbine.getPosition().y + 20 < getHeight() - turbine.getTotalHeight()) {
			try {
				turbine.erect(turbine.getTowerHeight() + 20);
				repaint();
			} catch (IllegalStateException x) {
				System.out.println(x.getMessage());
			}
		}
	}

	public void action_decreaseHeight(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.erect(turbine.getTowerHeight() - 20);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_increaseRotorLength(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.setRotorLength(turbine.getRotorLength() + 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_decreaseRotorLength(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.setRotorLength(turbine.getRotorLength() - 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_increaseNacelleRadius(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.setNacelleRadius(turbine.getNacelleRadius() + 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_decreaseNacelleRadius(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.setNacelleRadius(turbine.getNacelleRadius() - 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_zwickyTurbine(ActionEvent e) {
		WindTurbine turbine = getSelectedTurbine();
		try {
			turbine.minimize();
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}
}