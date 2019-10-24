package services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Implementation of the IBufferedImageService Interface
 */

public class BufferedImageSaveService implements IBufferedImageSaveService {

    @Override
    public void saveBufferedImage(String filename, BufferedImage image) {
        try {
            //Add FileTypeExtension if missing in Filename
            if (!filename.endsWith(".png")) {
                filename += ".png";
            }
            //Write Picture
            ImageIO.write(image, "png", new File(filename));
        } catch (IOException ex) {
            Logger.getLogger(BufferedImageSaveService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
