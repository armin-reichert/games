package de.amr.games.montagsmaler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.amr.games.montagsmaler.game.Game;
import de.amr.games.montagsmaler.game.Player;
import de.amr.games.montagsmaler.game.Team;

public class TeamPanel extends JPanel {

	private static final int TEAMPANEL_WIDTH = MontagsMalerUI.TEAMPANEL_WIDTH;

	private final Game game;
	private final Team team;
	private final JLabel playerInfo;
	private Action playerImageClickedAction;
	private final JTable scoreTable;

	private final Action actionToFirstPlayer = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			game.setCurrentPlayer(team.getPlayers().get(0));
			updateDisplay();
		}
	};
	private final Action actionToPrevPlayer = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!firstPlayerSelected()) {
				game.setCurrentPlayer(team.getPlayers().get(currentIndex() - 1));
				updateDisplay();
			}
		}
	};
	private final Action actionNextPlayer = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!lastPlayerSelected()) {
				game.setCurrentPlayer(team.getPlayers().get(currentIndex() + 1));
				updateDisplay();
			}
		}
	};
	private final Action actionLastPlayer = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			game.setCurrentPlayer(team.getPlayers().get(team.getPlayers().size() - 1));
			updateDisplay();
		}
	};

	private class ScoreTableModel extends AbstractTableModel {

		private final String[] COLUMN_NAMES = { "Maler", "Punkte" };

		private int sum() {
			int s = 0;
			for (Player p : team.getPlayers()) {
				s += p.getPoints();
			}
			return s;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				if (rowIndex == team.getPlayers().size()) {
					return "Gesamt:";
				}
				return team.getPlayers().get(rowIndex).getName();
			case 1:
				if (rowIndex == team.getPlayers().size()) {
					return sum();
				}
				return team.getPlayers().get(rowIndex).getPoints();
			default:
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return Integer.class;
			default:
				return Object.class;
			}
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}

		@Override
		public int getRowCount() {
			return team.getPlayers().size() + 1;
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}
	}

	private int currentIndex() {
		return team.getPlayers().indexOf(game.getCurrentPlayer());
	}

	private boolean firstPlayerSelected() {
		return team.isEmpty() || currentIndex() == 0;
	}

	private boolean lastPlayerSelected() {
		return team.isEmpty() || currentIndex() == team.getPlayers().size() - 1;
	}

	private void updateActionState() {
		actionToFirstPlayer.setEnabled(!firstPlayerSelected());
		actionToPrevPlayer.setEnabled(!firstPlayerSelected());
		actionNextPlayer.setEnabled(!lastPlayerSelected());
		actionLastPlayer.setEnabled(!lastPlayerSelected());
	}

	private void updateScoreTable() {
		AbstractTableModel tm = (AbstractTableModel) scoreTable.getModel();
		tm.fireTableDataChanged();
	}

	void updateDisplay() {
		updateActionState();
		updatePlayerInfo();
		updateScoreTable();
	}

	private void updatePlayerInfo() {
		final Player player = game.getCurrentPlayer();
		if (player != null) {
			playerInfo.setForeground(player.getPenColor());
			playerInfo.setText(player.getName());
			ImageIcon photo = player.getImage();
			if (photo != null) {
				Image scaledImage = photo.getImage().getScaledInstance(TEAMPANEL_WIDTH - 20, -1, Image.SCALE_SMOOTH);
				photo.setImage(scaledImage);
			}
			playerInfo.setIcon(photo);
		} else {
			playerInfo.setForeground(Color.BLACK);
			playerInfo.setText("");
			playerInfo.setIcon(null);
		}
	}

	public TeamPanel(final Game game, final Team team) {
		this.game = game;
		this.team = team;

		playerInfo = new JLabel();
		playerInfo.setFont(new Font("Arial", Font.BOLD, 20));
		playerInfo.setVerticalTextPosition(JLabel.BOTTOM);
		playerInfo.setHorizontalTextPosition(JLabel.CENTER);
		playerInfo.setToolTipText("Zum Bearbeiten doppelklicken");
		playerInfo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					playerImageClickedAction.actionPerformed(null);
				}
			}
		});

		scoreTable = new JTable();
		scoreTable.setModel(new ScoreTableModel());
		scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
		setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		Dimension d = new Dimension(TEAMPANEL_WIDTH - 20, 500);
		playerInfo.setPreferredSize(d);
		contentPanel.add(playerInfo);
		contentPanel.add(new JScrollPane(scoreTable));

		add(contentPanel, BorderLayout.CENTER);
		add(createActionPanel(), BorderLayout.SOUTH);

		updateDisplay();
	}

	private JPanel createActionPanel() {
		JPanel actionPanel = new JPanel();
		JButton btn = new JButton();
		btn.setAction(actionToFirstPlayer);
		btn.setText("<<");
		actionPanel.add(btn);
		btn = new JButton();
		btn.setAction(actionToPrevPlayer);
		btn.setText("<");
		actionPanel.add(btn);
		btn = new JButton();
		btn.setAction(actionNextPlayer);
		btn.setText(">");
		actionPanel.add(btn);
		btn = new JButton();
		btn.setAction(actionLastPlayer);
		btn.setText(">>");
		actionPanel.add(btn);
		return actionPanel;
	}

	public void setOnPlayerImageClicked(Action playerImageClickedAction) {
		this.playerImageClickedAction = playerImageClickedAction;
	}
}
