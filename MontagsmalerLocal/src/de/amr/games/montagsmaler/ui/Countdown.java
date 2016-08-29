package de.amr.games.montagsmaler.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.Timer;

import de.amr.games.montagsmaler.Tools;

public class Countdown implements ActionListener {
 
  private static final Font COUNTDOWN_FONT = new Font("Serif", Font.BOLD, 48);
  
  private final Timer timer = new Timer(0, this);
  private final Component target;
  private final Image buffer;
  private final Graphics2D gfx;
  private int remaining;
  private Runnable callback;
  private int maximumSize;
  
  @Override
  public void actionPerformed(ActionEvent e) {
    if (remaining == 0) {
      timer.stop();
      if (callback != null) {
        callback.run();
      }
    } else {
      final Font font = COUNTDOWN_FONT.deriveFont((float)(maximumSize/(remaining)));
      final FontMetrics fm = gfx.getFontMetrics(font);
      final String digit = String.valueOf(remaining);
      gfx.setFont(font);
      gfx.setColor(Color.BLACK);
      gfx.fillRect(0, 0, buffer.getWidth(null), buffer.getHeight(null));
      Rectangle2D r = fm.getStringBounds(digit, gfx);
      double x = (buffer.getWidth(null) - r.getWidth()) / 2;
      double y = (buffer.getHeight(null) - r.getHeight()) / 2 + fm.getAscent();
      gfx.setColor(Tools.randomPenColor()); 
      gfx.drawString(digit, (int)x, (int)y);
      --remaining;
      target.repaint();
    }
  }

  Countdown(Component target, Image buffer, Graphics2D gfx) {
    this.target = target;
    this.buffer = buffer;
    this.gfx = gfx;
    maximumSize = Math.min(target.getPreferredSize().width, target.getPreferredSize().height);
    timer.setDelay(1000);
  }
  
  void setCallback(Runnable callback) {
    this.callback = callback;
  }
  
  void run(int duration) {
    remaining = duration;
    timer.start();
  }
  
  public void setMaximumSize(int maximumSize) {
    this.maximumSize = maximumSize;
  }

}
