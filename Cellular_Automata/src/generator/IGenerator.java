package generator;

import java.awt.image.BufferedImage;

/**
 * An Interface Definition for the implementation of a Picture Generator.
 */
public interface IGenerator extends IObservable {

    /**
     *
     * @return The Name of the Generator
     */
    public String getName();

    /**
     *
     * @param name Sets the Name for the Generator
     */
    public void setName(String name);
    
    /**
     *
     * @return The Number of the Generations to be run
     */
    public int getGenerations();

    /**
     * Saves a PNG with the current settings of the generator.
     */
    public void save();

    /**
     *
     * @return The Current Generator Status
     */
    public String getStatus();

    /**
     *
     * Setup Generator
     */
    public void setup();
    
    /**
     *
     * @return An initial Image for the current settings.
     */   
    public BufferedImage initialize();
    
    /**
     *
     * @return A generated Image for the current settings.
     */
    public BufferedImage generate();

    /**
     * Possible Generator Status Values
     */
    enum GeneratorStatusType {

        /**
         * Generator is ready
         */
        READY,
        /**
         * Generator is currently calculating
         */
        CALCULATE,
        /**
         * Generator is displaying a setup dialog
         */
        SETUP,
        /**
         * Generator is currently saving the generated image
         */
        SAVE,
        /**
         * Generator is finished with calculating the image
         */
        FINISHED,
        /**
         * Generator is not configured
         */
        UNCONFIGURED
    }
}
