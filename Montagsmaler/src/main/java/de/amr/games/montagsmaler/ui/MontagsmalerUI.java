package de.amr.games.montagsmaler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.amr.games.montagsmaler.game.Game;
import de.amr.games.montagsmaler.game.Team;
import de.amr.games.montagsmaler.sounds.Sound;
import de.amr.games.montagsmaler.tools.Tools;
import de.amr.games.montagsmaler.ui.action.AddPlayerAction;
import de.amr.games.montagsmaler.ui.action.ClearPointsAction;
import de.amr.games.montagsmaler.ui.action.ClearTabletAction;
import de.amr.games.montagsmaler.ui.action.EditPlayerAction;
import de.amr.games.montagsmaler.ui.action.EditTeamNameAction;
import de.amr.games.montagsmaler.ui.action.ExitGameAction;
import de.amr.games.montagsmaler.ui.action.ShowCeremonyAction;
import de.amr.games.montagsmaler.ui.action.SolutionFoundAction;
import de.amr.games.montagsmaler.ui.action.StartGameAction;

/**
 * The Montagsmaler game UI.
 * 
 * @author <a href="mailto:armin.reichert@web.de">Armin Reichert</a>
 */
public class MontagsmalerUI extends JFrame implements PropertyChangeListener {

	static final int TEAMPANEL_WIDTH = 210;
	static final Dimension PLAYGROUND_SIZE = new Dimension(800, 640);

	private final Game game;
	private final Playground playground;
	private final Ceremony ceremony;
	private final JPanel actionPanel;
	private final GameEditor gameEditor;
	private final JLabel logo;
	private final JTabbedPane teamSwitcher;
	private final TeamPanel[] teamPanels;

	public final Action actionClearTablet = new ClearTabletAction(this);
	public final Action actionStartGame = new StartGameAction(this);
	public final Action actionClearPoints = new ClearPointsAction(this);
	public final Action actionSolutionFound = new SolutionFoundAction(this);
	public final Action actionEditTeamName = new EditTeamNameAction(this);
	public final Action actionEditPlayer = new EditPlayerAction(this);
	public final Action actionAddPlayer = new AddPlayerAction(this);
	public final Action actionExitGame = new ExitGameAction(this);
	public final Action actionShowCeremony = new ShowCeremonyAction(this);

	public MontagsmalerUI() {
		game = new Game(new Team("Team1"), new Team("Team2"));
		game.addPropertyChangeListener(this);

		actionSolutionFound.setEnabled(false);

		actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(Box.createHorizontalStrut(TEAMPANEL_WIDTH));
		actionPanel.add(new JButton(actionStartGame));
		actionPanel.add(Box.createHorizontalGlue());

		gameEditor = new GameEditor(this, game);
		logo = new JLabel(Tools.loadImageIcon("images/logo.jpg"));
		ceremony = new Ceremony(PLAYGROUND_SIZE);
		playground = new Playground(PLAYGROUND_SIZE);
		playground.setEnabled(false);
		playground.setForeground(Color.WHITE);

		teamPanels = new TeamPanel[2];
		teamPanels[0] = new TeamPanel(game, game.getTeam1());
		teamPanels[0].setOnPlayerImageClicked(actionEditPlayer);
		teamPanels[1] = new TeamPanel(game, game.getTeam2());
		teamPanels[1].setOnPlayerImageClicked(actionEditPlayer);

		teamSwitcher = new JTabbedPane();
		teamSwitcher.addTab(game.getTeam1().getName(), teamPanels[0]);
		teamSwitcher.addTab(game.getTeam2().getName(), teamPanels[1]);
		teamSwitcher.setPreferredSize(new Dimension(TEAMPANEL_WIDTH, 600));
		teamSwitcher.setMaximumSize(teamSwitcher.getPreferredSize());
		teamSwitcher.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int tabIndex = teamSwitcher.getSelectedIndex();
				game.setCurrentTeam(tabIndex == 0 ? game.getTeam1() : game.getTeam2());
			}
		});

		requestFocus();
		buildMenus();
		defineKeyBindings();
		showContent(logo);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Die Montagsmaler - \u00a9 2013 Geräteschuppen Software");
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}

	private void buildMenus() {
		JMenu actionMenu = new JMenu("Aktionen");
		actionMenu.add(new JMenuItem(actionAddPlayer));
		actionMenu.add(new JMenuItem(actionEditTeamName));
		actionMenu.add(new JMenuItem(actionClearPoints));
		actionMenu.add(new JMenuItem(actionShowCeremony));
		actionMenu.add(new JSeparator());
		actionMenu.add(new JMenuItem(actionExitGame));
		JMenuBar menubar = new JMenuBar();
		menubar.add(actionMenu);
		setJMenuBar(menubar);
	}

	private void defineKeyBindings() {
		InputMap inputMap = actionPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "startGame");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "solutionFound");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearTablet");

		actionPanel.getActionMap().put("startGame", actionStartGame);
		actionPanel.getActionMap().put("solutionFound", actionSolutionFound);
		actionPanel.getActionMap().put("clearTablet", actionClearTablet);
	}

	public void showContent(Component content) {
		BorderLayout bl = (BorderLayout) getContentPane().getLayout();
		if (bl.getLayoutComponent(BorderLayout.CENTER) != content) {
			getContentPane().removeAll();
			getContentPane().add(teamSwitcher, BorderLayout.WEST);
			getContentPane().add(content, BorderLayout.CENTER);
			getContentPane().add(actionPanel, BorderLayout.SOUTH);
			revalidate();
			repaint();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		final String property = e.getPropertyName();
		if ("running".equals(property) && e.getNewValue() == Boolean.TRUE) {
			// game started
			playground.clear();
			playground.setEnabled(true);
			if (game.getCurrentPlayer() != null) {
				playground.setForeground(game.getCurrentPlayer().getPenColor());
			}
			actionStartGame.setEnabled(false);
			actionSolutionFound.setEnabled(true);
		} else if ("running".equals(property) && e.getNewValue() == Boolean.FALSE) {
			// game stopped
			Sound.GAMEOVER.start();
			playground.setEnabled(false);
			actionStartGame.setEnabled(true);
			// TODO when game has ended and drawing has been guessed, enable action
			// actionImageSolved.setEnabled(false);
		} else if ("elapsed".equals(property)) {
			// elapsed time changed
			Sound.TICK.start();
			playground.setElapsed((Integer) e.getNewValue());
		} else if ("currentTeam".equals(property) || "currentPlayer".equals(property)) {
			updateCurrentTeamPanel();
			if (game.getCurrentPlayer() != null) {
				playground.setForeground(game.getCurrentPlayer().getPenColor());
			}
		}
	}

	public void updateCurrentTeamPanel() {
		if (game.getCurrentTeam() == game.getTeam1()) {
			teamSwitcher.setSelectedIndex(0);
			teamPanels[0].updateDisplay();
		} else if (game.getCurrentTeam() == game.getTeam2()) {
			teamSwitcher.setSelectedIndex(1);
			teamPanels[1].updateDisplay();
		}
	}

	public Playground getPlayground() {
		return playground;
	}

	public Game getGame() {
		return game;
	}

	public GameEditor getGameEditor() {
		return gameEditor;
	}

	public JTabbedPane getTeamSwitcher() {
		return teamSwitcher;
	}

	public Ceremony getCeremony() {
		return ceremony;
	}

	public JLabel getLogo() {
		return logo;
	}
}