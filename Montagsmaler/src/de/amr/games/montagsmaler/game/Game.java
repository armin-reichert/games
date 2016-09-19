package de.amr.games.montagsmaler.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Timer;

/**
 * Represents the game with two teams, a current player and operations to
 * start, pause and stop a game. The state changes of the game trigger events
 * that can be handled for example by the user interface objects.
 */
public class Game extends PropertyChangeSource {
  
  private static final int TICK_COUNT = 60;
  private static final int PULSE_MILLIS = 2000;
  
  private final Timer clock;
  private final Team team1;
  private final Team team2;
  private Team currentTeam;
  private Player currentPlayer;
  private int elapsed;
  private int speed;

  private void setElapsed(int newValue) {
    int oldValue = elapsed;
    elapsed = newValue;
    pcs.firePropertyChange("elapsed", oldValue, newValue);
  }

  private void doTick() {
    if (elapsed < TICK_COUNT) {
      setElapsed(elapsed + 1);
    } else {
      stop();
    }
  }
  
  public void start() {
    if (clock.isRunning()) {
      return;
    }
    setSpeed(currentPlayer != null ? currentPlayer.getSpeed() : 1);
    setElapsed(0);
    clock.start();
    pcs.firePropertyChange("running", false, true);
  }
  
  public void stop() {
    if (!clock.isRunning()) {
      return;
    }
    clock.stop();
    pcs.firePropertyChange("running", true, false);
  }
  
  public void pause(int seconds) {
    clock.stop();
    try {
      Thread.sleep(1000*seconds);
    } catch (InterruptedException e) {
      
    }
    clock.start();
  }
  
  public Game(Team team1, Team team2) {
    this.team1 = team1;
    this.team2 = team2;
    currentTeam = team1;
    currentPlayer = team1.isEmpty() ? null : team1.getPlayers().get(0);
    elapsed = 0;
    clock = new Timer(0, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        doTick();
      }
    });
    setSpeed(1);
  }

  public void setSpeed(int speed) {
    this.speed = speed;
    clock.setDelay(PULSE_MILLIS/speed);
  }
  
  public int getSpeed() {
    return speed;
  }
  
  public Team getTeam1() {
    return team1;
  }
  
  public Team getTeam2() {
    return team2;
  }
  
  public Player getCurrentPlayer() {
    return currentPlayer;
  }
  
  public Team getCurrentTeam() {
    return currentTeam;
  }
  
  public void setCurrentPlayer(Player newPlayer) {
    Player oldPlayer = currentPlayer;
    this.currentPlayer = newPlayer;
    if (newPlayer != null) {
      setSpeed(newPlayer.getSpeed());
    }
    pcs.firePropertyChange("currentPlayer", oldPlayer, newPlayer);
  }
  
  public void nextPlayer() {
    if (currentPlayer == null || currentTeam == null) {
      return;
    }
    List<Player> players = currentTeam.getPlayers();
    int i = players.indexOf(currentPlayer);
    setCurrentPlayer(i == players.size() - 1 ? players.get(0) : players.get(i+1));
  }
  
  public void setCurrentTeam(Team newTeam) {
    Team oldTeam = currentTeam;
    this.currentTeam = newTeam;
    if (currentTeam != null) {
      setCurrentPlayer(currentTeam.isEmpty() ? null : currentTeam.getPlayers().get(0));
    }
    pcs.firePropertyChange("currentTeam", oldTeam, newTeam);
  }
  
  public Team getWinnerTeam() {
    int p1 = team1.getPoints();
    int p2 = team2.getPoints();
    if (p1 == p2) {
      return null;
    } else if (p1 > p2) {
      return team1;
    } else {
      return team2;
    }
  }
  
}
