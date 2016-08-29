package armin.mm.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import armin.mm.Tools;
import armin.mm.game.Game;
import armin.mm.game.Player;
import armin.mm.game.Team;
import armin.mm.sounds.Sound;
import armin.mm.tools.ScrollingCanvas;

/**
 * The ceremony for the winners (or for both teams in case of a draw). Plays a hymn and
 * shows the photos of the honored players in a scrolling area.
 */
public class Ceremony extends ScrollingCanvas {

  private static final Image OLYMPIC_RINGS = Tools.loadImageIcon("images/olympische-ringe.jpg").getImage();
  private static final Font FONT = new Font("Cooper Black", Font.PLAIN, 36);
  
  private final List<Player> honoredPlayers = new ArrayList<Player>();
  private Team winnerTeam;
  
  public Ceremony(Dimension size) {
    super(size, 3000, 15);
    setBackground(Color.WHITE);
  }
  
  public void start(Game game, final Runnable afterCelebration) {
    if (isScrolling()) {
      return;
    }
    computeHonoredPlayers(game);
    Sound.HYMN.start();
    startScrolling(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        Sound.HYMN.stop();
        afterCelebration.run();
      }
    });
  }

  private void computeHonoredPlayers(Game game) {
    winnerTeam = game.getWinnerTeam();
    honoredPlayers.clear();
    if (winnerTeam == null) {
      honoredPlayers.addAll(game.getTeam1().getPlayers());
      honoredPlayers.addAll(game.getTeam2().getPlayers());
    } else {
      honoredPlayers.addAll(winnerTeam.getPlayers());
    }
  }
  
  @Override
  protected void drawContent() {
    setBackground(Color.WHITE);
    setForeground(Color.BLUE);
    setFont(FONT);
    image(OLYMPIC_RINGS);
    linefeed(3*getFont().getSize());
    if (winnerTeam == null) {
      text("Beide Mannschaften haben gewonnen!");
    } else {
      text("Sieger wurde das Team ");
      linefeed(getFont().getSize());
      setForeground(Color.RED);
      text(winnerTeam.getName());
      linefeed(getFont().getSize());
      setForeground(Color.BLUE);
      text("mit " + winnerTeam.getPoints() + " Punkten");
    }
    if (honoredPlayers.size() > 0) {
      linefeed(getLineHeight());
      text("Und hier die siegreichen Maler:");
      linefeed(getLineHeight());
      for (Player player : honoredPlayers) {
        image(player.getImage().getImage());
        linefeed(getFont().getSize());
        text(player.getName() + " (" + player.getPoints() + " Punkte)");
      }
    }
    linefeed(3*getFont().getSize());
    image(OLYMPIC_RINGS);
  }

}
