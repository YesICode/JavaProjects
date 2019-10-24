package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


public class BufferedImageDisplayPanel extends JPanel {

    private BufferedImage image;

    public BufferedImageDisplayPanel() {
        super();
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.setSize(image.getWidth(), image.getHeight());
        super.repaint();
    }

    public BufferedImage getImage() {
        return this.image;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }
}
