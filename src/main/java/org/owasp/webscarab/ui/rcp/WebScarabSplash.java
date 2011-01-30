/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.springframework.richclient.application.splash.ProgressSplashScreen;
import org.springframework.richclient.application.splash.SimpleSplashScreen;

/**
 *
 * @author Lpz
 */
public class WebScarabSplash extends ProgressSplashScreen {
    public class ImageCanvas extends SimpleSplashScreen.ImageCanvas {
        public ImageCanvas(Image image) {
            super(image);
        }
    }
    @Override
    protected Image getImage() {
        //compiled code
        return new Transparency().makeColorTransparent(super.getImage(), Color.BLACK);
    }
    @Override
    protected Component createContentPane() {
        Color clr = new Color(255,0,0,124);
        JPanel old = (JPanel) super.createContentPane();
        Container ctnr = old.getParent();
        JPanel c = new JPanel(new BorderLayout()); //(JPanel) super.createContentPane();
        c.add(new JLabel(new ImageIcon(this.getImage())), BorderLayout.CENTER);
        c.add(this.getProgressBar(), BorderLayout.SOUTH);
        c.setBackground(clr);

        return c;
    }
    public class Transparency {

        public Image makeColorTransparent(Image im, final Color color) {
            ImageFilter filter = new RGBImageFilter() {
                // the color we are looking for... Alpha bits are set to opaque

                public int markerRGB = color.getRGB() | 0xFF000000;

                public final int filterRGB(int x, int y, int rgb) {
                    if ((rgb | 0xFF000000) == markerRGB) {
                        // Mark the alpha bits as zero - transparent
                        return 0x00FFFFFF & rgb;
                    } else {
                        // nothing to do
                        return rgb;
                    }
                }
            };

            ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
            return Toolkit.getDefaultToolkit().createImage(ip);
        }
    }
}
