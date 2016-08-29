package de.amr.games.montagsmaler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import de.amr.games.montagsmaler.Tools;
import de.amr.games.montagsmaler.game.Game;
import de.amr.games.montagsmaler.game.Player;
import de.amr.games.montagsmaler.game.Team;
import de.amr.games.montagsmaler.tools.EnhancedFileChooser;

public class GameEditor extends JDialog {
  
  private final ImageIcon defaultPlayerImage = Tools.loadImageIcon("images/default_player.jpg");
  
  private String name;
  private ImageIcon image;
  private int speed;
  private Color penColor;
  
  private final Game game;
  private final JPanel contentPanel;
  private final JTextField nameField;
  private final JLabel imagePreview;
  private final JSlider speedSlider;
  private final JLabel penColorPreview;
  private final JButton addPlayerButton;
  private final JButton deletePlayerButton;
  private final JButton applyChangesButton;
  private final JButton changeTeamButton;
  
  private Action actionApplyChanges = new AbstractAction() {
    {
      putValue(NAME, "Änderungen speichern");
      putValue(LARGE_ICON_KEY, Tools.loadImageIcon("icons32/group_edit.png"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      Player player = game.getCurrentPlayer();
      player.setName(nameField.getText());
      player.setImage(image != null ? image : defaultPlayerImage);
      player.setSpeed(speedSlider.getValue());
      player.setPenColor(penColor != null ? penColor : Color.WHITE);
      game.setCurrentPlayer(null);
      game.setCurrentPlayer(player);
    }
  };
  
  private Action actionChangeTeam = new AbstractAction() {
    {
      putValue(NAME, "Team wechseln");
      putValue(LARGE_ICON_KEY, Tools.loadImageIcon("icons32/group_edit.png"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      Player player = game.getCurrentPlayer();
      if (player == null) {
        return;
      }
      Team oldTeam = player.getTeam();
      Team newTeam = oldTeam == game.getTeam1()? game.getTeam2() : game.getTeam1();
      oldTeam.deletePlayer(player);
      newTeam.addPlayer(player);
      player.setTeam(newTeam);
      game.setCurrentTeam(newTeam);
      game.setCurrentPlayer(player);
      updateDisplay();
      nameField.requestFocus();
    }
  };
  
  private Action actionAddPlayer = new AbstractAction() {
    {
      putValue(NAME, "Maler hinzufügen");
      putValue(LARGE_ICON_KEY, Tools.loadImageIcon("icons32/group_add.png"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      if (nameField.getText() == null || nameField.getText().trim().length() == 0) {
        nameField.requestFocus();
        nameField.selectAll();
        return;
      }
      final Player player = new Player();
      player.setTeam(game.getCurrentTeam());
      player.setName(nameField.getText());
      player.setImage(image != null ? image : defaultPlayerImage);
      player.setSpeed(speedSlider.getValue());
      player.setPenColor(penColor != null ? penColor : Color.WHITE);
      game.getCurrentTeam().addPlayer(player);
      game.setCurrentPlayer(player);
      setDefaultValues();
      updateDisplay();
      nameField.requestFocus();
      nameField.selectAll();
    }
  };
  
  private Action actionDeletePlayer = new AbstractAction() {
    {
      putValue(NAME, "Maler löschen");
      putValue(LARGE_ICON_KEY, Tools.loadImageIcon("icons32/group_delete.png"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      final Team team = game.getCurrentTeam();
      int i = team.getPlayers().indexOf(game.getCurrentPlayer());
      team.deletePlayer(game.getCurrentPlayer());
      if (i == team.getPlayers().size()) {
        --i;
      }
      game.setCurrentPlayer(team.isEmpty() ? null : team.getPlayers().get(i));
      GameEditor.this.setVisible(false);
    }
  };
  
  private String getNameProposal() {
    Set<String> playerNames = new HashSet<String>();
    for (Player p : game.getTeam1().getPlayers()) {
      playerNames.add(p.getName());
    }
    for (Player p : game.getTeam2().getPlayers()) {
      playerNames.add(p.getName());
    }
    int i = 1;
    String proposal = "Maler1";
    while (playerNames.contains(proposal)) {
      ++i;
      proposal = "Maler" + i;
    }
    return proposal;
  }
  
  private void setDefaultValues() {
    name = getNameProposal();
    image = defaultPlayerImage;
    speed = 1;
    penColor = Tools.randomPenColor();
  }
  
  private void updateDisplay() {
    nameField.setText(name);
    imagePreview.setIcon(image);
    speedSlider.setValue(speed);
    penColorPreview.setBackground(penColor);
  }
  
  public void open(boolean edit) {
    if (edit) {
      // Edit existing player
      setTitle("Maler bearbeiten");
      name = game.getCurrentPlayer().getName();
      image = game.getCurrentPlayer().getImage();
      penColor = game.getCurrentPlayer().getPenColor();
      speed = game.getCurrentPlayer().getSpeed();
      applyChangesButton.setVisible(true);
      addPlayerButton.setVisible(false);
      changeTeamButton.setVisible(true);
      deletePlayerButton.setVisible(true);
      getRootPane().setDefaultButton(applyChangesButton);
    } else {
      // Add new player
      setTitle("Neuen Maler hinzufügen");
      setDefaultValues();
      applyChangesButton.setVisible(false);
      addPlayerButton.setVisible(true);
      changeTeamButton.setVisible(false);
      deletePlayerButton.setVisible(false);
      getRootPane().setDefaultButton(addPlayerButton);
    }
    updateDisplay();
    nameField.requestFocus();
    nameField.selectAll();
    setLocationRelativeTo(getParent());
    setModal(true);
    setVisible(true);
 }
  
  public GameEditor(Frame owner, Game game) {
    super(owner, "Maler hinzufügen/bearbeiten");
    this.game = game;
    
    // Name
    nameField = new JTextField(15);
    nameField.setFont(new Font("Fixed", Font.BOLD, 20));
    
    // Image
    imagePreview = new JLabel();
    imagePreview.setPreferredSize(new Dimension(300, 200));
    imagePreview.setMaximumSize(imagePreview.getPreferredSize());
    final EnhancedFileChooser fc = new EnhancedFileChooser();
    final JButton chooseImageButton = new JButton(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fc.showOpenDialog(GameEditor.this) == JFileChooser.APPROVE_OPTION) {
          image = new ImageIcon(fc.getSelectedFile().getPath());
          imagePreview.setIcon(image);
          if (nameField.getText() == null || nameField.getText().trim().length() == 0) {
            name = fc.getSelectedFile().getName();
            int dot = name.lastIndexOf('.');
            name = name.substring(0, dot);
            nameField.setText(name);
          }
        } 
      }
    });
    chooseImageButton.setText("Bild auswählen...");
    
    // Speed
    speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 1);
    speedSlider.setMajorTickSpacing(10);
    speedSlider.setMinorTickSpacing(1);
    speedSlider.setPaintTicks(true);
    speedSlider.setLabelTable(speedSlider.createStandardLabels(1));
    speedSlider.setPaintLabels(true);

    // Pen color
    final JButton choosePenColorButton = new JButton(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final Color selectedColor = JColorChooser.showDialog(GameEditor.this, 
          "Stiftfarbe auswählen", penColor);
        if (selectedColor != null) {
          penColor = selectedColor;
          penColorPreview.setBackground(penColor);
        }
      }
    });
    choosePenColorButton.setText("Stiftfarbe wählen...");
    penColorPreview = new JLabel();
    penColorPreview.setOpaque(true);

    // Layout
    contentPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints left = new GridBagConstraints();
    left.gridwidth = GridBagConstraints.RELATIVE;
    left.insets = new Insets(5, 5, 5, 5);
    final GridBagConstraints right = new GridBagConstraints();
    right.gridwidth = GridBagConstraints.REMAINDER;
    right.fill = GridBagConstraints.BOTH;
    right.insets = new Insets(5, 5, 5, 5);
    
    contentPanel.add(chooseImageButton, left);
    contentPanel.add(imagePreview, right);
    contentPanel.add(new JLabel("Name"), left);
    contentPanel.add(nameField, right);
    contentPanel.add(new JLabel("Geschwindigkeit der Uhr"), left);
    contentPanel.add(speedSlider, right);
    contentPanel.add(choosePenColorButton, left);
    contentPanel.add(penColorPreview, right);

    // Actions
    final JPanel buttonPanel = new JPanel();
    buttonPanel.add(addPlayerButton = new JButton(actionAddPlayer));
    buttonPanel.add(applyChangesButton = new JButton(actionApplyChanges));
    buttonPanel.add(changeTeamButton = new JButton(actionChangeTeam));
    buttonPanel.add(deletePlayerButton = new JButton(actionDeletePlayer));
    
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
    pack();
    
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
  }
}

