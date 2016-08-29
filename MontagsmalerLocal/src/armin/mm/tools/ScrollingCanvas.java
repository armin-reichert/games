package armin.mm.tools;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * A drawing canvas with the capability of scrolling its content (text and images) smoothly
 * from the bottom to the top.
 */
public class ScrollingCanvas extends DrawingCanvas 
  implements ActionListener {

  private final Timer timer;
  private int y;
  private int scrollPos;
  private int outputHeight;
  private Runnable afterScrollingCallback;

  public ScrollingCanvas(Dimension size, int initialDelay, int delay) {
    super(size);
    timer = new Timer(0, this);
    timer.setInitialDelay(initialDelay);
    timer.setDelay(delay);
    setEnabled(false); // disable interactive drawing capability
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  /**
   * Should be overridden by subclasses and use the {@link #text(String)} and 
   * {@link #image(Image)} methods to compose the content to be scrolled.
   */
  protected void drawContent() {
  }
  
  /**
   * Starts scrolling of the content and runs the specified callback afterwards.
   */
  public void startScrolling(Runnable afterScrollingCallback) {
    this.afterScrollingCallback = afterScrollingCallback;
    scrollPos = getHeight();
    outputHeight = 0;
    clear();
    timer.start();
  }
  
  /**
   * Tells whether scrolling is currently running.
   */
  public boolean isScrolling() {
    return timer.isRunning();
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    if (scrollPos < -outputHeight) {
      timer.stop();
      if (afterScrollingCallback != null) {
        afterScrollingCallback.run();
      }
    } else {
      clear();
      y = scrollPos;
      int yBeforeContent = y;
      drawContent();
      outputHeight = y - yBeforeContent;
      repaint();
      --scrollPos;
    }
  }

  private int computeX(int width) {
    return (getWidth() - width) / 2;
  }
  
  protected int getLineHeight() {
    return getFont().getSize() * 3/2;
  }
  
  protected void linefeed(int pixel) {
    y += pixel;
  }
  
  protected void text(String text) {
    gfx.setBackground(getBackground());
    gfx.setFont(getFont());
    gfx.setColor(getForeground());
    int w = (int) gfx.getFontMetrics(getFont()).getStringBounds(text, gfx).getWidth();
    gfx.drawString(text, computeX(w), y);
    linefeed(getLineHeight());
  }
  
  protected void image(Image img) {
    gfx.setBackground(getBackground());
    gfx.setFont(getFont());
    gfx.setColor(getForeground());
    gfx.drawImage(img, computeX(img.getWidth(null)), y, null);
    linefeed(img.getHeight(null));
  }

}
