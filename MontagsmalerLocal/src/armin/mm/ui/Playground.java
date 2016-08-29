package armin.mm.ui;

import java.awt.Dimension;
import java.awt.Graphics;

import armin.mm.tools.DrawingCanvas;


/**
 * The playground of the Montagsmaler game. In addition to the drawing capabilities,
 * a clock is displayed and a countdown can be shown.
 * 
 * @author Armin Reichert
 */
public class Playground extends DrawingCanvas {
  private Countdown countdown;
  private ClockRenderer clock;
  private int elapsed;
  
  @Override
  protected void paintBelowDrawing(Graphics g) {
    super.paintBelowDrawing(g);
    if (elapsed > 0) {
      clock.drawClockTicks(elapsed);
    }
  }

  public Playground(Dimension size) {
    super(size);
    clock = new ClockRenderer(buffer);
    countdown = new Countdown(this, buffer, gfx);
    countdown.setMaximumSize(Math.min(size.width, size.height));
  }
  
  public void showCountdownAndRun(long durationMillis, Runnable callback) {
    countdown.setCallback(callback);
    clear();
    setElapsed(0);
    countdown.run((int)durationMillis/1000);
  }

  public void setElapsed(int seconds) {
    elapsed = seconds;
    repaint();
  }
  
}

