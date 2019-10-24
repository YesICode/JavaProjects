package services;

import java.awt.image.BufferedImage;

/**
 * Interface Definition for a service that implements methods to save a
 * bufferedImage to disk.
 */

public interface IBufferedImageSaveService {

    /**
     *
     * @param filename Filepath with Filename for the image
     * @param image The image that shoul be written to disk
     */
    public void saveBufferedImage(String filename, BufferedImage image);
}
