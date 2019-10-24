
package generator.gol;

import generator.IGenerator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import services.IBufferedImageSaveService;

/**Implementation of a Game of Life as a generator.
 * This cellular automaton has been modelled as a two dimensional array.
 * The following configuration has been used:
 *      - the edges are fixed,
 *      - the game starts with randomly distributed living cells,
 *      - von Moore neighborhood has been used.
 *      - the user can choose the width/height of the image as well as
 *        the size of the quadrants which will be drawn.
 *      The following game parameters are choosable:
 *      - the percentage of the living cells at the beginning,
 *      - the number of generations.
 */
public class GameOfLife extends Observable implements IGenerator{
    
    private int height = 600;
    private int width = 600;
    private int size = 10; // size of the quadrat in pixels
    
    private boolean setupDialogIsOpen = false; //Semaphore for displaying the setup dialog only once
    private GeneratorStatusType generatorStatus;
    private final IBufferedImageSaveService saveService;
    private String name = "GameOfLifeGenerator";
    
    private int generations = 100;
    private int currentGeneration = 0;
    
    private double alive = 0.5; // Living cells ratio at the beginning
    
    private int rows;
    private int columns;
    private int [][] cells;
    
    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     */
    public GameOfLife(IBufferedImageSaveService saveService) {
        this.saveService = saveService;
    }

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     * @param name Name for this generator
     */
    public GameOfLife(IBufferedImageSaveService saveService, String name) {
        this.saveService = saveService;
        this.name = name;
    }

    @Override
    public BufferedImage initialize() {
        updateStatus(IGenerator.GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        
        // STARTCONFIGURATION
        rows = (int)this.height/size;
        columns = (int)this.width/size;
        cells = new int[rows][columns];
        
        // Fill randomly with zeros and ones
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                int number =(Math.random() < (1.0 - alive))?0:1; 
                cells[i][j] = number;
            } 
        }
        currentGeneration = 1;
        
        // DRAW IMAGE
        Graphics2D g2d = image.createGraphics();
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l] == 1){
                    // Paint a black rectangle to visualize an alive cell.
                    g2d.setColor(Color.BLACK);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                else { // Paint a white rectangle to visualize a dead cell.
                    g2d.setColor(Color.WHITE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }  
                
            }
        }
        g2d.dispose();
        return image;    
    }
    
    @Override
    public BufferedImage generate(){
        updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        // ALGORITHM -> Game of Life
        //int  g = currentGeneration;
        int[][] next = new int[rows][columns];
        for(int x = 1; x < rows - 1; x++){
            for(int y = 1; y < columns - 1; y++){
                int neighbors = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        neighbors += cells[x+i][y+j];
                    }
                }   
                neighbors -= cells[x][y];
                if      ((cells[x][y] == 1) && (neighbors <  2)) next[x][y] = 0;
                else if ((cells[x][y] == 1) && (neighbors >  3)) next[x][y] = 0;
                else if ((cells[x][y] == 0) && (neighbors == 3)) next[x][y] = 1;
                else next[x][y] = cells[x][y];     
            } 
        }
        cells = next;
        if(currentGeneration < generations - 1){
              currentGeneration++;
        }
        
        // DRAW IMAGE
        Graphics2D g2d = image.createGraphics();
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l] == 1){
                    // Paint a black rectangle to visualize an alive cell.
                    g2d.setColor(Color.BLACK);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                else { // Paint a white rectangle to visualize a dead cell.
                    g2d.setColor(Color.WHITE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }  
            }
        }
            
        g2d.dispose(); 
        if(currentGeneration == generations - 1){
            updateStatus(GeneratorStatusType.FINISHED);
        }
        return image;
    }
    
     @Override
    public void save() {
        updateStatus(GeneratorStatusType.SAVE);
        this.saveService.saveBufferedImage("new", this.generate());
        updateStatus(GeneratorStatusType.FINISHED);
    }

    @Override
    public String getStatus() {
        return this.generatorStatus.toString();
    }
    
       private void showDialog() {
        //Load Setup Dialog with Parmameters for Width/Height and Size of the quadrats
        SpinnerModel widthHeightModel = new SpinnerNumberModel(this.width, 0, 9000, 1);
        SpinnerModel sizeModel = new SpinnerNumberModel(this.size, 1, 50, 1);
        
        //Load Setup Dialog with Parmameters for generation number
        // and the percentage of alive cells.
        SpinnerModel generationModel = new SpinnerNumberModel(this.generations, 0, 10000, 1);
        SpinnerModel aliveModel = new SpinnerNumberModel(this.alive*100.0, 0.0, 100.0, 1.0);
        
        JSpinner widthHeightSpinner = new JSpinner(widthHeightModel);
        JSpinner sizeSpinner = new JSpinner(sizeModel);
        
        JSpinner generationSpinner = new JSpinner(generationModel);
        JSpinner aliveSpinner = new JSpinner(aliveModel);
        
        JButton generateButton = new JButton("Generate");
        JButton cancelButton = new JButton("Cancel");

        JOptionPane optionPane = new JOptionPane(
                "Parameters",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                new Object[]{
                    new JLabel("Width/Height:"),
                    widthHeightSpinner,
                    new JLabel("Quadrat Size:"),
                    sizeSpinner,
                    new JLabel("Generations:"),
                    generationSpinner,
                    new JLabel("Alive cells at the start:"),
                    aliveSpinner,
                    new JLabel(" in %."),
                    // here some more parameter Spinners, JComboBoxes
                    generateButton,
                    cancelButton},
                null);
        JDialog setupDialog = optionPane.createDialog(optionPane, "Setup Game of Life Generator");

        //Add WindowsListener for CloseEvent
        setupDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setupDialog.dispose();
                setupDialogIsOpen = false;
                updateStatus(GeneratorStatusType.UNCONFIGURED);
            }
        });

        //Add ActionListeners
        cancelButton.addActionListener((ActionEvent e) -> {
            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(GeneratorStatusType.UNCONFIGURED);
        });

        generateButton.addActionListener((ActionEvent e) -> {
            this.width = (int) widthHeightSpinner.getValue();
            this.height = (int) widthHeightSpinner.getValue();
            this.size = (int)sizeSpinner.getValue();
            
            this.generations = (int) generationSpinner.getValue();
            this.alive = ((double)aliveSpinner.getValue())/100.0;
            
            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(GeneratorStatusType.READY);
        });

        setupDialog.setModal(false);
        setupDialog.setVisible(true);
        setupDialogIsOpen = true;
    }
    
    @Override
    public void setup() {
        if (!setupDialogIsOpen) {
            updateStatus(GeneratorStatusType.SETUP);
            showDialog();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Updates the generator status and notifies all registered observer that
     * the status has changed.
     *
     * @param newGeneratorStatusValue The new status for this generator
     */
    private void updateStatus(GeneratorStatusType newGeneratorStatusValue) {
        this.generatorStatus = newGeneratorStatusValue;
        //Notify Observers
        setChanged();
        notifyObservers();
    }

    @Override
    public int getGenerations() {
        return generations;
    }
   
}
